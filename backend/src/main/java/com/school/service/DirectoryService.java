package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.dto.*;
import com.school.entity.Classes;
import com.school.entity.Teacher;
import com.school.entity.TeacherClass;
import com.school.entity.User;
import com.school.mapper.ClassesMapper;
import com.school.mapper.TeacherClassMapper;
import com.school.mapper.TeacherMapper;
import com.school.mapper.UserMapper;
import com.school.util.PinyinUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DirectoryService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ClassesMapper classesMapper;

    @Autowired
    private TeacherClassMapper teacherClassMapper;

    // ========================================================================
    // 教师查询
    // ========================================================================
    public DirectoryPageResult<TeacherCard> queryTeachers(DirectoryQuery query, String role, Integer currentUserId) {
        boolean isStudent = "student".equals(role);

        // ============================================================
        // 问题4修复：学生角色仅可见本班任课教师
        // ============================================================
        Set<Integer> allowedTeacherIds = null;
        if (isStudent && currentUserId != null) {
            User currentUser = userMapper.selectById(currentUserId);
            if (currentUser != null && currentUser.getClassId() != null) {
                LambdaQueryWrapper<TeacherClass> tcWrapper = new LambdaQueryWrapper<>();
                tcWrapper.eq(TeacherClass::getClassId, currentUser.getClassId());
                List<TeacherClass> tcList = teacherClassMapper.selectList(tcWrapper);
                allowedTeacherIds = tcList.stream()
                        .map(TeacherClass::getTeacherId)
                        .collect(Collectors.toSet());
                if (allowedTeacherIds.isEmpty()) {
                    return buildEmptyTeacherPageResult(query);
                }
            } else {
                return buildEmptyTeacherPageResult(query);
            }
        }

        // ============================================================
        // 构建完整筛选条件（用于全量扫描，生成分组和首字母索引）
        // ============================================================
        LambdaQueryWrapper<Teacher> fullWrapper = buildTeacherWrapper(query, allowedTeacherIds);

        // 1. 先取全量数据（用于生成分组 + 可用首字母 + 全量分组统计）
        //    - 问题1修复：分组基于全量数据，而非当前页
        //    - 问题2修复：首字母索引基于全量数据，翻页保持稳定
        List<Teacher> allFiltered = teacherMapper.selectList(fullWrapper);
        List<TeacherCard> allCards = allFiltered.stream()
                .map(this::convertToTeacherCard)
                .collect(Collectors.toList());

        // ============================================================
        // 稳定的首字母索引（来自全量数据）
        // ============================================================
        Set<String> allInitials = allCards.stream()
                .map(TeacherCard::getNameInitial)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
        List<String> availableInitials = new ArrayList<>(allInitials);

        // ============================================================
        // 全量分组统计（用于展示每个组有多少人，即使当前页只含部分）
        // ============================================================
        Map<String, List<TeacherCard>> fullGrouped = null;
        List<DirectoryPageResult.DirectoryGroupStats> fullGroupStats = null;
        if ("expertise".equals(query.getGroupBy())) {
            fullGrouped = allCards.stream()
                    .collect(Collectors.groupingBy(
                            TeacherCard::getExpertise,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));
            final Map<String, List<TeacherCard>> fg = fullGrouped;
            fullGroupStats = fg.entrySet().stream()
                    .map(e -> {
                        DirectoryPageResult.DirectoryGroupStats s = new DirectoryPageResult.DirectoryGroupStats();
                        s.setGroupKey(e.getKey());
                        s.setGroupName(e.getKey());
                        s.setCount(e.getValue().size());
                        return s;
                    })
                    .collect(Collectors.toList());
        }

        // ============================================================
        // 问题3修复：首字母筛选也走正常分页
        //    - 先在全量卡片上过滤首字母，再手动分页
        // ============================================================
        List<TeacherCard> filteredByInitial = allCards;
        if (query.getNameInitial() != null && !query.getNameInitial().isEmpty() && !"all".equals(query.getNameInitial())) {
            String targetInitial = query.getNameInitial();
            filteredByInitial = allCards.stream()
                    .filter(card -> targetInitial.equals(card.getNameInitial()))
                    .collect(Collectors.toList());
        }

        // ============================================================
        // 应用排序（与分页）
        // ============================================================
        String sortBy = query.getSortBy() != null ? query.getSortBy() : "tno";
        boolean asc = !"desc".equals(query.getSortOrder());
        filteredByInitial.sort((a, b) -> {
            int cmp;
            if ("tname".equals(sortBy)) {
                cmp = nullSafeCompare(a.getTname(), b.getTname());
            } else {
                cmp = nullSafeCompare(a.getTno(), b.getTno());
            }
            return asc ? cmp : -cmp;
        });

        // ============================================================
        // 手动分页（兼容普通查询与首字母筛选两种路径）
        // ============================================================
        int pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 12;
        long total = filteredByInitial.size();
        long pages = (total + pageSize - 1) / pageSize;

        int fromIdx = Math.min((pageNum - 1) * pageSize, (int) total);
        int toIdx = Math.min(fromIdx + pageSize, (int) total);
        List<TeacherCard> pagedCards = filteredByInitial.subList(fromIdx, toIdx);

        // ============================================================
        // 当前页的分组（组头显示全量总数，卡片仅显示当前页卡片）
        // ============================================================
        Map<String, List<TeacherCard>> pagedGrouped = null;
        if ("expertise".equals(query.getGroupBy())) {
            pagedGrouped = new LinkedHashMap<>();
            // 保持全量分组顺序
            if (fullGroupStats != null) {
                for (DirectoryPageResult.DirectoryGroupStats stat : fullGroupStats) {
                    pagedGrouped.put(stat.getGroupKey(), new ArrayList<>());
                }
            }
            for (TeacherCard card : pagedCards) {
                String key = card.getExpertise();
                pagedGrouped.computeIfAbsent(key, k -> new ArrayList<>()).add(card);
            }
            // 去掉空组（在前端仍通过groupStats显示全量总数）
            pagedGrouped.values().removeIf(List::isEmpty);
        }

        DirectoryPageResult<TeacherCard> result = new DirectoryPageResult<>();
        result.setRecords(pagedCards);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(pages);
        result.setAvailableInitials(availableInitials);
        result.setGroupedRecords(pagedGrouped);
        result.setGroupStats(fullGroupStats);

        return result;
    }

    private LambdaQueryWrapper<Teacher> buildTeacherWrapper(DirectoryQuery query, Set<Integer> allowedTeacherIds) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();

        // 数据范围过滤（任课关联）
        if (allowedTeacherIds != null) {
            wrapper.in(Teacher::getTid, allowedTeacherIds);
        }

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(Teacher::getTname, kw)
                    .or().like(Teacher::getTno, kw)
                    .or().like(Teacher::getTdescript, kw));
        }

        if (query.getExpertise() != null && !query.getExpertise().isEmpty() && !"all".equals(query.getExpertise())) {
            wrapper.like(Teacher::getTdescript, query.getExpertise());
        }

        if (query.getTeacherNo() != null && !query.getTeacherNo().isEmpty()) {
            wrapper.like(Teacher::getTno, query.getTeacherNo());
        }

        // 首字母筛选：不在这里做，在DTO层处理
        // if (query.getNameInitial() != null ...) — 由调用方处理

        return wrapper;
    }

    private DirectoryPageResult<TeacherCard> buildEmptyTeacherPageResult(DirectoryQuery query) {
        DirectoryPageResult<TeacherCard> result = new DirectoryPageResult<>();
        result.setRecords(new ArrayList<>());
        result.setTotal(0);
        result.setPageNum(query.getPageNum() != null ? query.getPageNum() : 1);
        result.setPageSize(query.getPageSize() != null ? query.getPageSize() : 12);
        result.setPages(0);
        result.setAvailableInitials(new ArrayList<>());
        if ("expertise".equals(query.getGroupBy())) {
            result.setGroupedRecords(new LinkedHashMap<>());
            result.setGroupStats(new ArrayList<>());
        }
        return result;
    }

    // ========================================================================
    // 学生查询
    // ========================================================================
    public DirectoryPageResult<StudentCard> queryStudents(DirectoryQuery query, String role, Integer currentUserId) {
        boolean isStudent = "student".equals(role);

        // 学生角色：自动限定为本班
        Integer forcedClassId = null;
        if (isStudent && currentUserId != null) {
            User currentUser = userMapper.selectById(currentUserId);
            if (currentUser != null && currentUser.getClassId() != null) {
                forcedClassId = currentUser.getClassId();
            }
        }

        // ============================================================
        // 构建完整筛选条件
        // ============================================================
        LambdaQueryWrapper<User> fullWrapper = buildStudentWrapper(query, forcedClassId);

        // 1. 取全量数据（用于分组 + 首字母 + 统计）
        List<User> allFiltered = userMapper.selectList(fullWrapper);
        List<StudentCard> allCards = allFiltered.stream()
                .map(u -> convertToStudentCard(u, role))
                .collect(Collectors.toList());

        // ============================================================
        // 稳定的首字母索引（全量）
        // ============================================================
        Set<String> allInitials = allCards.stream()
                .map(StudentCard::getNameInitial)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
        List<String> availableInitials = new ArrayList<>(allInitials);

        // ============================================================
        // 全量分组统计
        // ============================================================
        Map<String, List<StudentCard>> fullGrouped = null;
        List<DirectoryPageResult.DirectoryGroupStats> fullGroupStats = null;
        if ("class".equals(query.getGroupBy())) {
            fullGrouped = allCards.stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getClassname() != null ? s.getClassname() : "未分班",
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));
            // 按班级名排序
            Map<String, List<StudentCard>> sortedFullGrouped = new LinkedHashMap<>();
            fullGrouped.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(e -> sortedFullGrouped.put(e.getKey(), e.getValue()));
            fullGrouped = sortedFullGrouped;

            final Map<String, List<StudentCard>> fg = fullGrouped;
            fullGroupStats = fg.entrySet().stream()
                    .map(e -> {
                        DirectoryPageResult.DirectoryGroupStats s = new DirectoryPageResult.DirectoryGroupStats();
                        s.setGroupKey(e.getKey());
                        s.setGroupName(e.getKey());
                        s.setCount(e.getValue().size());
                        return s;
                    })
                    .collect(Collectors.toList());
        }

        // ============================================================
        // 首字母筛选（基于全量），走正常分页
        // ============================================================
        List<StudentCard> filteredByInitial = allCards;
        if (query.getNameInitial() != null && !query.getNameInitial().isEmpty() && !"all".equals(query.getNameInitial())) {
            String targetInitial = query.getNameInitial();
            filteredByInitial = allCards.stream()
                    .filter(card -> targetInitial.equals(card.getNameInitial()))
                    .collect(Collectors.toList());
        }

        // ============================================================
        // 应用排序
        // ============================================================
        String sortBy = query.getSortBy() != null ? query.getSortBy() : "classname";
        boolean asc = !"desc".equals(query.getSortOrder());
        filteredByInitial.sort((a, b) -> {
            int cmp;
            if ("username".equals(sortBy)) {
                cmp = nullSafeCompare(a.getUsername(), b.getUsername());
            } else if ("userno".equals(sortBy)) {
                cmp = nullSafeCompare(a.getUserno(), b.getUserno());
            } else {
                cmp = nullSafeCompare(a.getClassname(), b.getClassname());
                if (cmp == 0) {
                    cmp = nullSafeCompare(a.getUserno(), b.getUserno());
                }
            }
            return asc ? cmp : -cmp;
        });

        // ============================================================
        // 手动分页
        // ============================================================
        int pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 12;
        long total = filteredByInitial.size();
        long pages = (total + pageSize - 1) / pageSize;

        int fromIdx = Math.min((pageNum - 1) * pageSize, (int) total);
        int toIdx = Math.min(fromIdx + pageSize, (int) total);
        List<StudentCard> pagedCards = filteredByInitial.subList(fromIdx, toIdx);

        // ============================================================
        // 当前页分组（按全量组顺序排列）
        // ============================================================
        Map<String, List<StudentCard>> pagedGrouped = null;
        if ("class".equals(query.getGroupBy())) {
            pagedGrouped = new LinkedHashMap<>();
            if (fullGroupStats != null) {
                for (DirectoryPageResult.DirectoryGroupStats stat : fullGroupStats) {
                    pagedGrouped.put(stat.getGroupKey(), new ArrayList<>());
                }
            }
            for (StudentCard card : pagedCards) {
                String key = card.getClassname() != null ? card.getClassname() : "未分班";
                pagedGrouped.computeIfAbsent(key, k -> new ArrayList<>()).add(card);
            }
            pagedGrouped.values().removeIf(List::isEmpty);
        }

        DirectoryPageResult<StudentCard> result = new DirectoryPageResult<>();
        result.setRecords(pagedCards);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(pages);
        result.setAvailableInitials(availableInitials);
        result.setGroupedRecords(pagedGrouped);
        result.setGroupStats(fullGroupStats);

        return result;
    }

    private LambdaQueryWrapper<User> buildStudentWrapper(DirectoryQuery query, Integer forcedClassId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 强制数据范围
        if (forcedClassId != null) {
            wrapper.eq(User::getClassId, forcedClassId);
        }

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(User::getUsername, kw)
                    .or().like(User::getUserno, kw)
                    .or().like(User::getClassname, kw));
        }

        if (query.getClassId() != null && query.getClassId() > 0) {
            wrapper.eq(User::getClassId, query.getClassId());
        }

        if (query.getClassName() != null && !query.getClassName().isEmpty() && !"all".equals(query.getClassName())) {
            wrapper.like(User::getClassname, query.getClassName());
        }

        if (query.getStudentNo() != null && !query.getStudentNo().isEmpty()) {
            wrapper.like(User::getUserno, query.getStudentNo());
        }

        if (query.getStatus() != null && !query.getStatus().isEmpty() && !"all".equals(query.getStatus())) {
            wrapper.eq(User::getCheckedok, query.getStatus());
        }

        return wrapper;
    }

    // ========================================================================
    // 详情查询
    // ========================================================================
    public TeacherDetail getTeacherDetail(Integer id, String role, Integer currentUserId) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) return null;

        // 问题4修复：学生角色需要验证该老师是本班任课老师
        if ("student".equals(role) && currentUserId != null) {
            User currentUser = userMapper.selectById(currentUserId);
            if (currentUser != null && currentUser.getClassId() != null) {
                LambdaQueryWrapper<TeacherClass> tcWrapper = new LambdaQueryWrapper<>();
                tcWrapper.eq(TeacherClass::getTeacherId, id)
                        .eq(TeacherClass::getClassId, currentUser.getClassId());
                Long count = teacherClassMapper.selectCount(tcWrapper);
                if (count == 0) {
                    return null;
                }
            } else {
                return null;
            }
        }

        TeacherDetail detail = new TeacherDetail();
        detail.setTid(teacher.getTid());
        detail.setTname(teacher.getTname());
        detail.setTno(teacher.getTno());
        detail.setTpic(teacher.getTpic());
        detail.setTdescript(teacher.getTdescript());
        detail.setExpertise(PinyinUtil.extractExpertise(teacher.getTdescript()));
        detail.setNameInitial(PinyinUtil.getFirstLetter(teacher.getTname()));
        detail.setPinyin(PinyinUtil.getPinyinInitials(teacher.getTname()));

        if ("teacher".equals(role)) {
            detail.setTdate(teacher.getTdate());
        }

        return detail;
    }

    public StudentDetail getStudentDetail(Integer id, String role, Integer currentUserId) {
        User user = userMapper.selectById(id);
        if (user == null) return null;

        boolean isStudent = "student".equals(role);
        if (isStudent && currentUserId != null) {
            User currentUser = userMapper.selectById(currentUserId);
            if (currentUser != null && currentUser.getClassId() != null
                    && !currentUser.getClassId().equals(user.getClassId())) {
                if (!currentUserId.equals(id)) {
                    return null;
                }
            }
        }

        StudentDetail detail = new StudentDetail();
        detail.setUid(user.getUid());
        detail.setUsername(user.getUsername());
        detail.setUserno(user.getUserno());
        detail.setUsersex(user.getUsersex());
        detail.setUpic(user.getUpic());
        detail.setUserdescript(user.getUserdescript());
        detail.setClassId(user.getClassId());
        detail.setClassname(user.getClassname());
        detail.setCheckedok(user.getCheckedok());
        detail.setYouxiuok(user.getYouxiuok());
        detail.setNameInitial(PinyinUtil.getFirstLetter(user.getUsername()));
        detail.setPinyin(PinyinUtil.getPinyinInitials(user.getUsername()));

        return detail;
    }

    // ========================================================================
    // 筛选选项 / 导出
    // ========================================================================
    public List<Classes> getAllClasses() {
        return classesMapper.selectList(null);
    }

    public List<Map<String, String>> getAllExpertise() {
        List<Teacher> teachers = teacherMapper.selectList(null);
        Set<String> expertiseSet = new LinkedHashSet<>();
        for (Teacher t : teachers) {
            String expertise = PinyinUtil.extractExpertise(t.getTdescript());
            if (expertise != null && !"未设置".equals(expertise)) {
                expertiseSet.add(expertise);
            }
        }
        List<Map<String, String>> result = new ArrayList<>();
        for (String e : expertiseSet) {
            Map<String, String> map = new HashMap<>();
            map.put("value", e);
            map.put("label", e);
            result.add(map);
        }
        return result;
    }

    public String exportTeachersToVcf(DirectoryQuery query, String role, Integer currentUserId) {
        query.setPageSize(10000);
        DirectoryPageResult<TeacherCard> result = queryTeachers(query, role, currentUserId);
        StringBuilder vcf = new StringBuilder();
        for (TeacherCard card : result.getRecords()) {
            vcf.append("BEGIN:VCARD\n");
            vcf.append("VERSION:3.0\n");
            vcf.append("N:").append(card.getTname()).append("\n");
            vcf.append("FN:").append(card.getTname()).append("\n");
            vcf.append("TITLE:").append(card.getExpertise()).append("\n");
            vcf.append("TEL;TYPE=WORK:").append(card.getTno()).append("\n");
            vcf.append("NOTE:").append(card.getTdescript() != null ? card.getTdescript().replace("\n", " ") : "").append("\n");
            vcf.append("END:VCARD\n");
        }
        return vcf.toString();
    }

    public String exportStudentsToVcf(DirectoryQuery query, String role, Integer currentUserId) {
        query.setPageSize(10000);
        DirectoryPageResult<StudentCard> result = queryStudents(query, role, currentUserId);
        StringBuilder vcf = new StringBuilder();
        for (StudentCard card : result.getRecords()) {
            vcf.append("BEGIN:VCARD\n");
            vcf.append("VERSION:3.0\n");
            vcf.append("N:").append(card.getUsername()).append("\n");
            vcf.append("FN:").append(card.getUsername()).append("\n");
            vcf.append("ORG:").append(card.getClassname() != null ? card.getClassname() : "").append("\n");
            vcf.append("TEL;TYPE=WORK:").append(card.getUserno()).append("\n");
            if (card.getUsersex() != null) {
                vcf.append("GENDER:").append(card.getUsersex()).append("\n");
            }
            vcf.append("NOTE:").append(card.getUserdescript() != null ? card.getUserdescript().replace("\n", " ") : "").append("\n");
            vcf.append("END:VCARD\n");
        }
        return vcf.toString();
    }

    public String exportTeachersToCsv(DirectoryQuery query, String role, Integer currentUserId) {
        query.setPageSize(10000);
        DirectoryPageResult<TeacherCard> result = queryTeachers(query, role, currentUserId);
        StringBuilder csv = new StringBuilder();
        csv.append("姓名,工号,研究方向,个人简介\n");
        for (TeacherCard card : result.getRecords()) {
            csv.append(escapeCsv(card.getTname())).append(",");
            csv.append(escapeCsv(card.getTno())).append(",");
            csv.append(escapeCsv(card.getExpertise())).append(",");
            csv.append(escapeCsv(card.getTdescript())).append("\n");
        }
        return csv.toString();
    }

    public String exportStudentsToCsv(DirectoryQuery query, String role, Integer currentUserId) {
        query.setPageSize(10000);
        DirectoryPageResult<StudentCard> result = queryStudents(query, role, currentUserId);
        StringBuilder csv = new StringBuilder();
        csv.append("姓名,学号,性别,班级,状态,个人简介\n");
        for (StudentCard card : result.getRecords()) {
            csv.append(escapeCsv(card.getUsername())).append(",");
            csv.append(escapeCsv(card.getUserno())).append(",");
            csv.append(escapeCsv(card.getUsersex())).append(",");
            csv.append(escapeCsv(card.getClassname())).append(",");
            csv.append(escapeCsv(card.getCheckedok())).append(",");
            csv.append(escapeCsv(card.getUserdescript())).append("\n");
        }
        return csv.toString();
    }

    // ========================================================================
    // 转换与工具
    // ========================================================================
    private TeacherCard convertToTeacherCard(Teacher teacher) {
        TeacherCard card = new TeacherCard();
        card.setTid(teacher.getTid());
        card.setTname(teacher.getTname());
        card.setTno(teacher.getTno());
        card.setTpic(teacher.getTpic());
        card.setTdescript(teacher.getTdescript());
        card.setExpertise(PinyinUtil.extractExpertise(teacher.getTdescript()));
        card.setNameInitial(PinyinUtil.getFirstLetter(teacher.getTname()));
        card.setPinyin(PinyinUtil.getPinyinInitials(teacher.getTname()));
        return card;
    }

    private StudentCard convertToStudentCard(User user, String role) {
        StudentCard card = new StudentCard();
        card.setUid(user.getUid());
        card.setUsername(user.getUsername());
        card.setUserno(user.getUserno());
        card.setUsersex(user.getUsersex());
        card.setUpic(user.getUpic());
        card.setUserdescript(user.getUserdescript());
        card.setClassId(user.getClassId());
        card.setClassname(user.getClassname());
        card.setNameInitial(PinyinUtil.getFirstLetter(user.getUsername()));
        card.setPinyin(PinyinUtil.getPinyinInitials(user.getUsername()));

        // 学生角色：脱敏审核状态与优秀标记
        if ("teacher".equals(role)) {
            card.setCheckedok(user.getCheckedok());
            card.setYouxiuok(user.getYouxiuok());
        }
        return card;
    }

    private int nullSafeCompare(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
