let currentType = 'teacher';
let currentPage = 1;
let pageSize = 12;
let currentData = null;
let currentDetail = null;
let currentUserRole = 'teacher';
let selectedInitial = 'all';
let classesList = [];
let expertiseList = [];

const ALL_LETTERS = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','#'];

async function initDirectory(role) {
    currentUserRole = role;

    try {
        const [classesRes, expertiseRes] = await Promise.all([
            api.directory.getClasses(),
            api.directory.getExpertise()
        ]);

        if (classesRes && classesRes.success) {
            classesList = classesRes.data;
            populateClassSelect();
        }
        if (expertiseRes && expertiseRes.success) {
            expertiseList = expertiseRes.data;
            populateExpertiseSelect();
        }
    } catch (e) {
        console.error('Load options failed:', e);
    }

    loadData();
}

function populateClassSelect() {
    const select = document.getElementById('studentClass');
    if (!select) return;
    classesList.forEach(c => {
        const option = document.createElement('option');
        option.value = c.cname;
        option.textContent = c.cname;
        select.appendChild(option);
    });
}

function populateExpertiseSelect() {
    const select = document.getElementById('teacherExpertise');
    if (!select) return;
    expertiseList.forEach(e => {
        const option = document.createElement('option');
        option.value = e.value;
        option.textContent = e.label;
        select.appendChild(option);
    });
}

function switchTab(type) {
    currentType = type;
    currentPage = 1;
    selectedInitial = 'all';

    document.querySelectorAll('#directoryTabs .nav-link').forEach(link => {
        link.classList.toggle('active', link.dataset.type === type);
    });

    document.getElementById('teacherFilters').classList.toggle('d-none', type !== 'teacher');
    document.getElementById('studentFilters').classList.toggle('d-none', type !== 'student');

    loadData();
}

function showLoading() {
    const loadingEl = document.getElementById('loadingState');
    let html = '';
    for (let i = 0; i < 8; i++) {
        html += `
            <div class="skeleton-card">
                <div class="skeleton-avatar skeleton"></div>
                <div class="skeleton-line skeleton"></div>
                <div class="skeleton-line skeleton short"></div>
                <div class="skeleton-line skeleton short"></div>
            </div>
        `;
    }
    loadingEl.innerHTML = html;
    loadingEl.classList.remove('d-none');
    document.getElementById('contentArea').innerHTML = '';
    document.getElementById('emptyState').classList.add('d-none');
    document.getElementById('pagination').innerHTML = '';
}

function hideLoading() {
    document.getElementById('loadingState').classList.add('d-none');
}

function getQueryParams() {
    const params = {
        pageNum: currentPage,
        pageSize: pageSize,
        nameInitial: selectedInitial
    };

    if (currentType === 'teacher') {
        const keyword = document.getElementById('teacherKeyword')?.value || '';
        const expertise = document.getElementById('teacherExpertise')?.value || 'all';
        const groupBy = document.getElementById('teacherGroupBy')?.value || '';
        const sortBy = document.getElementById('teacherSortBy')?.value || 'tno';

        if (keyword) params.keyword = keyword;
        if (expertise !== 'all') params.expertise = expertise;
        if (groupBy) params.groupBy = groupBy;
        params.sortBy = sortBy;
    } else {
        const keyword = document.getElementById('studentKeyword')?.value || '';
        const className = document.getElementById('studentClass')?.value || 'all';
        const status = document.getElementById('studentStatus')?.value || 'all';
        const groupBy = document.getElementById('studentGroupBy')?.value || '';
        const sortBy = document.getElementById('studentSortBy')?.value || 'classname';

        if (keyword) params.keyword = keyword;
        if (className !== 'all') params.className = className;
        if (status !== 'all') params.status = status;
        if (groupBy) params.groupBy = groupBy;
        params.sortBy = sortBy;
    }

    return params;
}

async function loadData() {
    showLoading();

    try {
        const params = getQueryParams();
        let res;

        if (currentType === 'teacher') {
            res = await api.directory.getTeachers(params);
        } else {
            res = await api.directory.getStudents(params);
        }

        hideLoading();

        if (res && res.success) {
            currentData = res.data;
            renderAlphabetIndex(res.data.availableInitials || []);

            if ((!res.data.records || res.data.records.length === 0) &&
                (!res.data.groupedRecords || Object.keys(res.data.groupedRecords).length === 0)) {
                showEmptyState();
            } else {
                renderContent(res.data);
                renderPagination(res.data);
            }
        } else {
            showEmptyState();
            api.showToast(res?.message || '加载失败', 'danger');
        }
    } catch (e) {
        hideLoading();
        showEmptyState();
        console.error('Load data failed:', e);
        api.showToast('网络错误，请稍后重试', 'danger');
    }
}

function renderAlphabetIndex(availableInitials) {
    const container = document.getElementById('alphabetIndex');
    const availableSet = new Set(availableInitials);

    let html = '<span class="text-muted small me-2 align-self-center">拼音索引：</span>';

    html += `<span class="letter-btn ${selectedInitial === 'all' ? 'active' : ''}" onclick="selectInitial('all')">全</span>`;

    ALL_LETTERS.forEach(letter => {
        const isAvailable = availableSet.has(letter);
        const isActive = selectedInitial === letter;
        html += `<span class="letter-btn ${isActive ? 'active' : ''} ${!isAvailable ? 'disabled' : ''}"
                     onclick="${isAvailable ? `selectInitial('${letter}')` : ''}">${letter}</span>`;
    });

    container.innerHTML = html;
}

function selectInitial(letter) {
    selectedInitial = letter;
    currentPage = 1;
    loadData();
}

function showEmptyState() {
    document.getElementById('emptyState').classList.remove('d-none');
    document.getElementById('contentArea').innerHTML = '';
    document.getElementById('pagination').innerHTML = '';
}

function renderContent(data) {
    const container = document.getElementById('contentArea');

    if (data.groupedRecords && Object.keys(data.groupedRecords).length > 0) {
        let html = '';
        for (const [groupName, items] of Object.entries(data.groupedRecords)) {
            const groupStats = data.groupStats?.find(s => s.groupName === groupName);
            const count = groupStats?.count || items.length;
            html += `
                <div class="group-title">${groupName} <small>${count}人</small></div>
                <div class="card-grid">
                    ${items.map(item => renderCard(item)).join('')}
                </div>
            `;
        }
        container.innerHTML = html;
    } else {
        container.innerHTML = `
            <div class="card-grid">
                ${data.records.map(item => renderCard(item)).join('')}
            </div>
        `;
    }
}

function renderCard(item) {
    if (currentType === 'teacher') {
        return renderTeacherCard(item);
    } else {
        return renderStudentCard(item);
    }
}

function renderTeacherCard(teacher) {
    const avatarText = teacher.tname ? teacher.tname.charAt(0) : 'T';
    const tagClass = 'tag';

    return `
        <div class="person-card" onclick="showDetail(${teacher.tid}, 'teacher')">
            <div class="d-flex align-items-start gap-3 mb-3">
                <div class="avatar">${avatarText}</div>
                <div class="flex-grow-1">
                    <div class="name">${teacher.tname || '-'}</div>
                    <div class="no"><i class="bi bi-person-badge me-1"></i>${teacher.tno || '-'}</div>
                </div>
            </div>
            <div class="${tagClass}">
                <i class="bi bi-award me-1"></i>${teacher.expertise || '未设置'}
            </div>
            ${teacher.nameInitial ? `<span class="badge bg-light text-dark ms-2 float-end">${teacher.nameInitial}</span>` : ''}
        </div>
    `;
}

function renderStudentCard(student) {
    const avatarText = student.username ? student.username.charAt(0) : 'S';
    let tagClass = 'tag';
    let statusText = student.classname || '未分班';

    if (student.checkedok === '待审核') {
        tagClass = 'tag warning';
        statusText = '待审核';
    } else if (student.checkedok === '已拒绝') {
        tagClass = 'tag warning';
        statusText = '已拒绝';
    } else if (student.youxiuok === '是') {
        tagClass = 'tag success';
        statusText = '优秀';
    }

    return `
        <div class="person-card" onclick="showDetail(${student.uid}, 'student')">
            <div class="d-flex align-items-start gap-3 mb-3">
                <div class="avatar">${avatarText}</div>
                <div class="flex-grow-1">
                    <div class="name">${student.username || '-'}</div>
                    <div class="no"><i class="bi bi-person me-1"></i>${student.userno || '-'}</div>
                </div>
            </div>
            <div class="${tagClass}">
                <i class="bi bi-geo-alt me-1"></i>${statusText}
            </div>
            ${student.nameInitial ? `<span class="badge bg-light text-dark ms-2 float-end">${student.nameInitial}</span>` : ''}
        </div>
    `;
}

function renderPagination(data) {
    const container = document.getElementById('pagination');
    const totalPages = data.pages || 1;
    const current = data.pageNum || 1;

    if (totalPages <= 1) {
        container.innerHTML = `<div class="text-muted small">共 ${data.total} 条记录</div>`;
        return;
    }

    let html = `<nav><ul class="pagination">`;

    html += `
        <li class="page-item ${current <= 1 ? 'disabled' : ''}">
            <a class="page-link page-btn" onclick="goToPage(${current - 1})"><i class="bi bi-chevron-left"></i></a>
        </li>
    `;

    const startPage = Math.max(1, current - 2);
    const endPage = Math.min(totalPages, current + 2);

    if (startPage > 1) {
        html += `<li class="page-item"><a class="page-link page-btn" onclick="goToPage(1)">1</a></li>`;
        if (startPage > 2) {
            html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        html += `
            <li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link page-btn" onclick="goToPage(${i})">${i}</a>
            </li>
        `;
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
        }
        html += `<li class="page-item"><a class="page-link page-btn" onclick="goToPage(${totalPages})">${totalPages}</a></li>`;
    }

    html += `
        <li class="page-item ${current >= totalPages ? 'disabled' : ''}">
            <a class="page-link page-btn" onclick="goToPage(${current + 1})"><i class="bi bi-chevron-right"></i></a>
        </li>
    `;

    html += `</ul></nav><span class="text-muted small ms-3 align-self-center">共 ${data.total} 条记录</span>`;
    container.innerHTML = html;
}

function goToPage(page) {
    currentPage = page;
    loadData();
}

async function showDetail(id, type) {
    try {
        let res;
        if (type === 'teacher') {
            res = await api.directory.getTeacherDetail(id);
        } else {
            res = await api.directory.getStudentDetail(id);
        }

        if (res && res.success) {
            currentDetail = res.data;
            currentDetail._type = type;
            renderDetailModal(res.data, type);
            const modal = new bootstrap.Modal(document.getElementById('detailModal'));
            modal.show();
        } else {
            api.showToast(res?.message || '获取详情失败', 'danger');
        }
    } catch (e) {
        console.error('Get detail failed:', e);
        api.showToast('网络错误，请稍后重试', 'danger');
    }
}

function renderDetailModal(data, type) {
    const container = document.getElementById('detailContent');

    if (type === 'teacher') {
        renderTeacherDetail(data, container);
    } else {
        renderStudentDetail(data, container);
    }
}

function renderTeacherDetail(data, container) {
    const avatarText = data.tname ? data.tname.charAt(0) : 'T';

    let infoHtml = `
        <div class="text-center mb-4">
            <div class="avatar-lg">${avatarText}</div>
            <h5 class="fw-bold mb-1">${data.tname || '-'}</h5>
            <p class="text-muted mb-0">${data.expertise || '教师'}</p>
        </div>
        <div class="info-item">
            <div class="info-label"><i class="bi bi-person-badge me-1"></i>工号</div>
            <div class="info-value">${data.tno || '-'}</div>
        </div>
        <div class="info-item">
            <div class="info-label"><i class="bi bi-award me-1"></i>研究方向</div>
            <div class="info-value">${data.expertise || '-'}</div>
        </div>
    `;

    if (data.tdate && currentUserRole === 'teacher') {
        infoHtml += `
            <div class="info-item">
                <div class="info-label"><i class="bi bi-calendar me-1"></i>出生日期</div>
                <div class="info-value">${data.tdate || '-'}</div>
            </div>
        `;
    }

    infoHtml += `
        <div class="info-item">
            <div class="info-label"><i class="bi bi-file-text me-1"></i>个人简介</div>
            <div class="info-value">${data.tdescript || '暂无简介'}</div>
        </div>
    `;

    if (data.pinyin) {
        infoHtml += `
            <div class="info-item">
                <div class="info-label"><i class="bi bi-fonts me-1"></i>拼音索引</div>
                <div class="info-value"><span class="badge bg-primary">${data.nameInitial || '-'}</span> ${data.pinyin || '-'}</div>
            </div>
        `;
    }

    container.innerHTML = infoHtml;
}

function renderStudentDetail(data, container) {
    const avatarText = data.username ? data.username.charAt(0) : 'S';

    let statusBadge = '';
    if (data.checkedok === '已通过') {
        statusBadge = '<span class="badge bg-success">已通过</span>';
    } else if (data.checkedok === '待审核') {
        statusBadge = '<span class="badge bg-warning text-dark">待审核</span>';
    } else if (data.checkedok === '已拒绝') {
        statusBadge = '<span class="badge bg-danger">已拒绝</span>';
    }

    let youxiuBadge = '';
    if (data.youxiuok === '是') {
        youxiuBadge = '<span class="badge bg-info ms-2">优秀</span>';
    }

    let infoHtml = `
        <div class="text-center mb-4">
            <div class="avatar-lg">${avatarText}</div>
            <h5 class="fw-bold mb-1">${data.username || '-'} ${youxiuBadge}</h5>
            <p class="text-muted mb-0">${statusBadge} ${data.classname || '未分班'}</p>
        </div>
        <div class="row">
            <div class="col-md-6">
                <div class="info-item">
                    <div class="info-label"><i class="bi bi-person me-1"></i>学号</div>
                    <div class="info-value">${data.userno || '-'}</div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="info-item">
                    <div class="info-label"><i class="bi bi-gender-ambiguous me-1"></i>性别</div>
                    <div class="info-value">${data.usersex || '-'}</div>
                </div>
            </div>
        </div>
        <div class="info-item">
            <div class="info-label"><i class="bi bi-geo-alt me-1"></i>班级</div>
            <div class="info-value">${data.classname || '未分班'}</div>
        </div>
    `;

    if (currentUserRole === 'teacher') {
        infoHtml += `
            <div class="row">
                <div class="col-md-6">
                    <div class="info-item">
                        <div class="info-label"><i class="bi bi-check-circle me-1"></i>审核状态</div>
                        <div class="info-value">${data.checkedok || '-'}</div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="info-item">
                        <div class="info-label"><i class="bi bi-star me-1"></i>优秀标识</div>
                        <div class="info-value">${data.youxiuok || '-'}</div>
                    </div>
                </div>
            </div>
        `;
    }

    infoHtml += `
        <div class="info-item">
            <div class="info-label"><i class="bi bi-file-text me-1"></i>个人简介</div>
            <div class="info-value">${data.userdescript || '暂无简介'}</div>
        </div>
    `;

    if (data.pinyin) {
        infoHtml += `
            <div class="info-item">
                <div class="info-label"><i class="bi bi-fonts me-1"></i>拼音索引</div>
                <div class="info-value"><span class="badge bg-primary">${data.nameInitial || '-'}</span> ${data.pinyin || '-'}</div>
            </div>
        `;
    }

    container.innerHTML = infoHtml;
}

function copyInfo() {
    if (!currentDetail) return;

    let text = '';
    if (currentDetail._type === 'teacher') {
        text = `姓名：${currentDetail.tname || '-'}\n`;
        text += `工号：${currentDetail.tno || '-'}\n`;
        text += `研究方向：${currentDetail.expertise || '-'}\n`;
        if (currentDetail.tdate && currentUserRole === 'teacher') {
            text += `出生日期：${currentDetail.tdate || '-'}\n`;
        }
        text += `个人简介：${currentDetail.tdescript || '暂无'}`;
    } else {
        text = `姓名：${currentDetail.username || '-'}\n`;
        text += `学号：${currentDetail.userno || '-'}\n`;
        text += `性别：${currentDetail.usersex || '-'}\n`;
        text += `班级：${currentDetail.classname || '未分班'}\n`;
        if (currentUserRole === 'teacher') {
            text += `审核状态：${currentDetail.checkedok || '-'}\n`;
            text += `优秀标识：${currentDetail.youxiuok || '-'}\n`;
        }
        text += `个人简介：${currentDetail.userdescript || '暂无'}`;
    }

    api.directory.copyToClipboard(text);
}

function generateVCard() {
    if (!currentDetail) return;

    const vcf = api.directory.generateVCard(currentDetail, currentDetail._type);
    const filename = currentDetail._type === 'teacher'
        ? `${currentDetail.tname || 'teacher'}_名片.vcf`
        : `${currentDetail.username || 'student'}_名片.vcf`;

    api.directory.downloadVCard(vcf, filename);
    api.showToast('电子名片已生成', 'success');
}

async function exportData(format) {
    const params = getQueryParams();
    delete params.pageNum;
    delete params.pageSize;

    try {
        if (currentType === 'teacher') {
            if (format === 'vcf') {
                await api.directory.exportTeachersVcf(params);
            } else {
                await api.directory.exportTeachersCsv(params);
            }
        } else {
            if (format === 'vcf') {
                await api.directory.exportStudentsVcf(params);
            } else {
                await api.directory.exportStudentsCsv(params);
            }
        }
        api.showToast('导出成功', 'success');
    } catch (e) {
        console.error('Export failed:', e);
        api.showToast('导出失败，请稍后重试', 'danger');
    }
}
