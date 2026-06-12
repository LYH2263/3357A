package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.dto.*;
import com.school.entity.Classes;
import com.school.entity.Teacher;
import com.school.entity.User;
import com.school.mapper.ClassesMapper;
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

    public DirectoryPageResult<TeacherCard> queryTeachers(DirectoryQuery query, String role, Integer currentUserId) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();

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

        if (query.getNameInitial() != null && !query.getNameInitial().isEmpty() && !"all".equals(query.getNameInitial())) {
            List<Teacher> allTeachers = teacherMapper.selectList(wrapper);
            List<Teacher> filtered = allTeachers.stream()
                    .filter(t -> query.getNameInitial().equals(PinyinUtil.getFirstLetter(t.getTname())))
                    .collect(Collectors.toList());
            return buildTeacherPageResult(filtered, query, role);
        }

        String sortBy = query.getSortBy() != null ? query.getSortBy() : "tno";
        boolean asc = "asc".equals(query.getSortOrder());
        if ("tname".equals(sortBy)) {
            wrapper.orderBy(true, asc, Teacher::getTname);
        } else {
            wrapper.orderBy(true, asc, Teacher::getTno);
        }

        Page<Teacher> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Teacher> resultPage = teacherMapper.selectPage(page, wrapper);

        return buildTeacherPageResult(resultPage.getRecords(), query, role,
                resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize(), resultPage.getPages());
    }

    public DirectoryPageResult<StudentCard> queryStudents(DirectoryQuery query, String role, Integer currentUserId) {
        boolean isStudent = "student".equals(role);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (isStudent && currentUserId != null) {
            User currentUser = userMapper.selectById(currentUserId);
            if (currentUser != null && currentUser.getClassId() != null) {
                wrapper.eq(User::getClassId, currentUser.getClassId());
            } else {
                wrapper.eq(User::getUid, currentUserId);
            }
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

        if (query.getNameInitial() != null && !query.getNameInitial().isEmpty() && !"all".equals(query.getNameInitial())) {
            List<User> allStudents = userMapper.selectList(wrapper);
            List<User> filtered = allStudents.stream()
                    .filter(s -> query.getNameInitial().equals(PinyinUtil.getFirstLetter(s.getUsername())))
                    .collect(Collectors.toList());
            return buildStudentPageResult(filtered, query, role);
        }

        String sortBy = query.getSortBy() != null ? query.getSortBy() : "classname";
        boolean asc = "asc".equals(query.getSortOrder());
        if ("username".equals(sortBy)) {
            wrapper.orderBy(true, asc, User::getUsername);
        } else if ("userno".equals(sortBy)) {
            wrapper.orderBy(true, asc, User::getUserno);
        } else {
            wrapper.orderBy(true, asc, User::getClassId).orderByAsc(User::getUserno);
        }

        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<User> resultPage = userMapper.selectPage(page, wrapper);

        return buildStudentPageResult(resultPage.getRecords(), query, role,
                resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize(), resultPage.getPages());
    }

    private DirectoryPageResult<TeacherCard> buildTeacherPageResult(List<Teacher> teachers, DirectoryQuery query, String role) {
        return buildTeacherPageResult(teachers, query, role, teachers.size(), 1, teachers.size(), 1);
    }

    private DirectoryPageResult<TeacherCard> buildTeacherPageResult(List<Teacher> teachers, DirectoryQuery query, String role,
                                                                    long total, long pageNum, long pageSize, long pages) {
        List<TeacherCard> cards = teachers.stream()
                .map(this::convertToTeacherCard)
                .collect(Collectors.toList());

        DirectoryPageResult<TeacherCard> result = new DirectoryPageResult<>();
        result.setRecords(cards);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(pages);

        Set<String> initials = cards.stream()
                .map(TeacherCard::getNameInitial)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
        result.setAvailableInitials(new ArrayList<>(initials));

        if ("expertise".equals(query.getGroupBy())) {
            Map<String, List<TeacherCard>> grouped = cards.stream()
                    .collect(Collectors.groupingBy(
                            TeacherCard::getExpertise,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));
            result.setGroupedRecords(grouped);

            List<DirectoryPageResult.DirectoryGroupStats> stats = grouped.entrySet().stream()
                    .map(e -> {
                        DirectoryPageResult.DirectoryGroupStats s = new DirectoryPageResult.DirectoryGroupStats();
                        s.setGroupKey(e.getKey());
                        s.setGroupName(e.getKey());
                        s.setCount(e.getValue().size());
                        return s;
                    })
                    .collect(Collectors.toList());
            result.setGroupStats(stats);
        }

        return result;
    }

    private DirectoryPageResult<StudentCard> buildStudentPageResult(List<User> students, DirectoryQuery query, String role) {
        return buildStudentPageResult(students, query, role, students.size(), 1, students.size(), 1);
    }

    private DirectoryPageResult<StudentCard> buildStudentPageResult(List<User> students, DirectoryQuery query, String role,
                                                                    long total, long pageNum, long pageSize, long pages) {
        List<StudentCard> cards = students.stream()
                .map(this::convertToStudentCard)
                .collect(Collectors.toList());

        DirectoryPageResult<StudentCard> result = new DirectoryPageResult<>();
        result.setRecords(cards);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(pages);

        Set<String> initials = cards.stream()
                .map(StudentCard::getNameInitial)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
        result.setAvailableInitials(new ArrayList<>(initials));

        if ("class".equals(query.getGroupBy())) {
            Map<String, List<StudentCard>> grouped = cards.stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getClassname() != null ? s.getClassname() : "未分班",
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            Map<String, List<StudentCard>> sortedGrouped = new LinkedHashMap<>();
            grouped.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(e -> sortedGrouped.put(e.getKey(), e.getValue()));
            result.setGroupedRecords(sortedGrouped);

            List<DirectoryPageResult.DirectoryGroupStats> stats = sortedGrouped.entrySet().stream()
                    .map(e -> {
                        DirectoryPageResult.DirectoryGroupStats s = new DirectoryPageResult.DirectoryGroupStats();
                        s.setGroupKey(e.getKey());
                        s.setGroupName(e.getKey());
                        s.setCount(e.getValue().size());
                        return s;
                    })
                    .collect(Collectors.toList());
            result.setGroupStats(stats);
        }

        return result;
    }

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

    private StudentCard convertToStudentCard(User user) {
        StudentCard card = new StudentCard();
        card.setUid(user.getUid());
        card.setUsername(user.getUsername());
        card.setUserno(user.getUserno());
        card.setUsersex(user.getUsersex());
        card.setUpic(user.getUpic());
        card.setUserdescript(user.getUserdescript());
        card.setClassId(user.getClassId());
        card.setClassname(user.getClassname());
        card.setCheckedok(user.getCheckedok());
        card.setYouxiuok(user.getYouxiuok());
        card.setNameInitial(PinyinUtil.getFirstLetter(user.getUsername()));
        card.setPinyin(PinyinUtil.getPinyinInitials(user.getUsername()));
        return card;
    }

    public TeacherDetail getTeacherDetail(Integer id, String role) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) return null;

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

    public String exportTeachersToVcf(DirectoryQuery query, String role) {
        query.setPageSize(10000);
        DirectoryPageResult<TeacherCard> result = queryTeachers(query, role, null);
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

    public String exportTeachersToCsv(DirectoryQuery query, String role) {
        query.setPageSize(10000);
        DirectoryPageResult<TeacherCard> result = queryTeachers(query, role, null);
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

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
