const API_BASE = 'http://localhost:8357/api';

const api = {
    baseUrl: API_BASE,
    async request(url, options = {}) {
        const suppressToast = options.suppressToast === true;
        delete options.suppressToast;
        try {
            const response = await fetch(`${API_BASE}${url}`, {
                headers: {
                    'Content-Type': 'application/json',
                },
                ...options,
            });
            const text = await response.text();
            let data = null;
            if (text) {
                try {
                    data = JSON.parse(text);
                } catch (e) {
                    data = text;
                }
            }
            if (!response.ok) {
                const errMsg = (data && data.message) ? data.message : '网络请求失败，请稍后重试';
                const error = new Error(errMsg);
                error.responseData = data;
                error.status = response.status;
                if (!suppressToast) {
                    this.showToast(errMsg, 'danger');
                }
                throw error;
            }
            return data;
        } catch (error) {
            if (error.status) {
                throw error;
            }
            console.error('API Error:', error);
            if (!suppressToast) {
                this.showToast('网络请求失败，请稍后重试', 'danger');
            }
            throw error;
        }
    },

    async uploadFile(file, onProgress) {
        return new Promise((resolve, reject) => {
            const formData = new FormData();
            formData.append('file', file);
            const xhr = new XMLHttpRequest();
            xhr.open('POST', `${API_BASE}/file/upload`);
            xhr.upload.onprogress = (e) => {
                if (e.lengthComputable && onProgress) {
                    onProgress(Math.round((e.loaded / e.total) * 100));
                }
            };
            xhr.onload = () => {
                try {
                    const res = JSON.parse(xhr.responseText);
                    if (res.success) resolve(res);
                    else reject(new Error(res.message || '上传失败'));
                } catch (e) { reject(e); }
            };
            xhr.onerror = () => reject(new Error('网络错误'));
            xhr.send(formData);
        });
    },

    async uploadImage(file, onProgress) {
        return new Promise((resolve, reject) => {
            const formData = new FormData();
            formData.append('file', file);
            const xhr = new XMLHttpRequest();
            xhr.open('POST', `${API_BASE}/file/upload-image`);
            xhr.upload.onprogress = (e) => {
                if (e.lengthComputable && onProgress) {
                    onProgress(Math.round((e.loaded / e.total) * 100));
                }
            };
            xhr.onload = () => {
                try {
                    const res = JSON.parse(xhr.responseText);
                    if (res.success) resolve(res);
                    else reject(new Error(res.message || '上传失败'));
                } catch (e) { reject(e); }
            };
            xhr.onerror = () => reject(new Error('网络错误'));
            xhr.send(formData);
        });
    },

    validateImage(file) {
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/bmp'];
        const allowedExt = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp'];
        const maxSize = 10 * 1024 * 1024;
        const name = file.name.toLowerCase();
        const extOk = allowedExt.some(ext => name.endsWith(ext));
        const typeOk = allowedTypes.includes(file.type) || extOk;
        if (!typeOk) return { valid: false, message: '仅支持 JPG/PNG/GIF/WEBP/BMP 格式图片' };
        if (file.size > maxSize) return { valid: false, message: '图片大小不能超过 10MB' };
        return { valid: true };
    },

    showToast(message, type = 'primary') {
        const toastContainer = document.getElementById('toast-container') || this.createToastContainer();
        const toastId = 'toast-' + Date.now();
        const toastHtml = `
            <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0 show" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">${message}</div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        `;
        toastContainer.insertAdjacentHTML('beforeend', toastHtml);
        setTimeout(() => {
            const toastElement = document.getElementById(toastId);
            if (toastElement) {
                const toast = new bootstrap.Toast(toastElement);
                toast.hide();
                setTimeout(() => toastElement.remove(), 1000);
            }
        }, 3000);
    },

    createToastContainer() {
        const container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
        return container;
    },

    setSession(user, role) {
        localStorage.setItem('school_user', JSON.stringify(user));
        localStorage.setItem('school_role', role);
    },

    getSession() {
        return {
            user: JSON.parse(localStorage.getItem('school_user')),
            role: localStorage.getItem('school_role')
        };
    },

    clearSession() {
        localStorage.removeItem('school_user');
        localStorage.removeItem('school_role');
        location.href = 'index.html';
    },

    album: {
        async list(params = {}) {
            const query = new URLSearchParams(params).toString();
            return api.request(`/album/list${query ? '?' + query : ''}`);
        },
        async myClass(classId) {
            return api.request(`/album/my-class?classId=${classId}`);
        },
        async featured(limit = 10) {
            return api.request(`/album/featured?limit=${limit}`);
        },
        async detail(id, userId, userType = 'student') {
            let url = `/album/detail/${id}`;
            if (userId) url += `?userId=${userId}&userType=${userType}`;
            return api.request(url);
        },
        async save(data) {
            return api.request('/album/save', { method: 'POST', body: JSON.stringify(data) });
        },
        async delete(id) {
            return api.request(`/album/delete/${id}`, { method: 'DELETE' });
        },
        async uploadImages(albumId, files, uploaderId, uploaderName, onProgress) {
            return new Promise((resolve, reject) => {
                const formData = new FormData();
                formData.append('albumId', albumId);
                if (uploaderId) formData.append('uploaderId', uploaderId);
                if (uploaderName) formData.append('uploaderName', uploaderName);
                Array.from(files).forEach(f => formData.append('files', f));

                const xhr = new XMLHttpRequest();
                xhr.open('POST', `${API_BASE}/album/upload-images`);
                xhr.upload.onprogress = (e) => {
                    if (e.lengthComputable && onProgress) {
                        onProgress(Math.round((e.loaded / e.total) * 100));
                    }
                };
                xhr.onload = () => {
                    try {
                        const res = JSON.parse(xhr.responseText);
                        resolve(res);
                    } catch (e) { reject(e); }
                };
                xhr.onerror = () => reject(new Error('网络错误'));
                xhr.send(formData);
            });
        },
        async setCover(albumId, imageId) {
            return api.request('/album/image/set-cover', {
                method: 'POST',
                body: JSON.stringify({ albumId, imageId })
            });
        },
        async sortImages(sortList) {
            return api.request('/album/image/sort', {
                method: 'POST',
                body: JSON.stringify(sortList)
            });
        },
        async deleteImage(id) {
            return api.request(`/album/image/delete/${id}`, { method: 'DELETE' });
        },
        async toggleLike(albumId, userId, userType = 'student') {
            return api.request('/album/like', {
                method: 'POST',
                body: JSON.stringify({ albumId, userId, userType })
            });
        },
        async addComment(data) {
            return api.request('/album/comment/add', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },
        async deleteComment(id) {
            return api.request(`/album/comment/delete/${id}`, { method: 'DELETE' });
        },
        async toggleFeatured(id) {
            return api.request('/album/toggle-featured', {
                method: 'POST',
                body: JSON.stringify({ id })
            });
        }
    },

    discipline: {
        async addRecord(data) {
            return api.request('/discipline/add', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },
        async batchAdd(data) {
            return api.request('/discipline/batch-add', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },
        async revoke(data) {
            return api.request('/discipline/revoke', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },
        async studentRecords(studentId) {
            return api.request(`/discipline/student?studentId=${studentId}`);
        },
        async query(params = {}) {
            const query = new URLSearchParams(params).toString();
            return api.request(`/discipline/query${query ? '?' + query : ''}`);
        },
        async statistics(classId) {
            const query = classId ? `?classId=${classId}` : '';
            return api.request(`/discipline/statistics${query}`);
        }
    },

    directory: {
        async getTeachers(params = {}) {
            const session = api.getSession();
            const headers = {
                'Content-Type': 'application/json'
            };
            if (session.role) headers['X-User-Role'] = session.role;
            if (session.user && session.user.tid) headers['X-User-Id'] = session.user.tid;
            if (session.user && session.user.uid) headers['X-User-Id'] = session.user.uid;

            const query = new URLSearchParams(params).toString();
            return api.request(`/directory/teachers${query ? '?' + query : ''}`, { headers });
        },

        async getStudents(params = {}) {
            const session = api.getSession();
            const headers = {
                'Content-Type': 'application/json'
            };
            if (session.role) headers['X-User-Role'] = session.role;
            if (session.user && session.user.tid) headers['X-User-Id'] = session.user.tid;
            if (session.user && session.user.uid) headers['X-User-Id'] = session.user.uid;

            const query = new URLSearchParams(params).toString();
            return api.request(`/directory/students${query ? '?' + query : ''}`, { headers });
        },

        async getTeacherDetail(id) {
            const session = api.getSession();
            const headers = {
                'Content-Type': 'application/json'
            };
            if (session.role) headers['X-User-Role'] = session.role;
            if (session.user && session.user.tid) headers['X-User-Id'] = session.user.tid;
            if (session.user && session.user.uid) headers['X-User-Id'] = session.user.uid;

            return api.request(`/directory/teacher/${id}`, { headers });
        },

        async getStudentDetail(id) {
            const session = api.getSession();
            const headers = {
                'Content-Type': 'application/json'
            };
            if (session.role) headers['X-User-Role'] = session.role;
            if (session.user && session.user.tid) headers['X-User-Id'] = session.user.tid;
            if (session.user && session.user.uid) headers['X-User-Id'] = session.user.uid;

            return api.request(`/directory/student/${id}`, { headers });
        },

        async getClasses() {
            return api.request('/directory/classes');
        },

        async getExpertise() {
            return api.request('/directory/expertise');
        },

        async exportTeachersVcf(params = {}) {
            const session = api.getSession();
            const headers = {
                'Content-Type': 'application/json'
            };
            if (session.role) headers['X-User-Role'] = session.role;
            if (session.user && session.user.tid) headers['X-User-Id'] = session.user.tid;
            if (session.user && session.user.uid) headers['X-User-Id'] = session.user.uid;

            const response = await fetch(`${API_BASE}/directory/export/teachers/vcf`, {
                method: 'POST',
                headers,
                body: JSON.stringify(params)
            });
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = '教师通讯录.vcf';
            a.click();
            window.URL.revokeObjectURL(url);
        },

        async exportStudentsVcf(params = {}) {
            const session = api.getSession();
            const headers = {
                'Content-Type': 'application/json'
            };
            if (session.role) headers['X-User-Role'] = session.role;
            if (session.user && session.user.tid) headers['X-User-Id'] = session.user.tid;
            if (session.user && session.user.uid) headers['X-User-Id'] = session.user.uid;

            const response = await fetch(`${API_BASE}/directory/export/students/vcf`, {
                method: 'POST',
                headers,
                body: JSON.stringify(params)
            });
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = '学生通讯录.vcf';
            a.click();
            window.URL.revokeObjectURL(url);
        },

        async exportTeachersCsv(params = {}) {
            const session = api.getSession();
            const headers = {
                'Content-Type': 'application/json'
            };
            if (session.role) headers['X-User-Role'] = session.role;
            if (session.user && session.user.tid) headers['X-User-Id'] = session.user.tid;
            if (session.user && session.user.uid) headers['X-User-Id'] = session.user.uid;

            const response = await fetch(`${API_BASE}/directory/export/teachers/csv`, {
                method: 'POST',
                headers,
                body: JSON.stringify(params)
            });
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = '教师通讯录.csv';
            a.click();
            window.URL.revokeObjectURL(url);
        },

        async exportStudentsCsv(params = {}) {
            const session = api.getSession();
            const headers = {
                'Content-Type': 'application/json'
            };
            if (session.role) headers['X-User-Role'] = session.role;
            if (session.user && session.user.tid) headers['X-User-Id'] = session.user.tid;
            if (session.user && session.user.uid) headers['X-User-Id'] = session.user.uid;

            const response = await fetch(`${API_BASE}/directory/export/students/csv`, {
                method: 'POST',
                headers,
                body: JSON.stringify(params)
            });
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = '学生通讯录.csv';
            a.click();
            window.URL.revokeObjectURL(url);
        },

        generateVCard(data, type) {
            let vcf = 'BEGIN:VCARD\nVERSION:3.0\n';
            if (type === 'teacher') {
                vcf += `N:${data.tname}\n`;
                vcf += `FN:${data.tname}\n`;
                vcf += `TITLE:${data.expertise || ''}\n`;
                vcf += `TEL;TYPE=WORK:${data.tno || ''}\n`;
                if (data.tdescript) {
                    vcf += `NOTE:${data.tdescript.replace(/\n/g, ' ')}\n`;
                }
            } else {
                vcf += `N:${data.username}\n`;
                vcf += `FN:${data.username}\n`;
                vcf += `ORG:${data.classname || ''}\n`;
                vcf += `TEL;TYPE=WORK:${data.userno || ''}\n`;
                if (data.usersex) vcf += `GENDER:${data.usersex}\n`;
                if (data.userdescript) {
                    vcf += `NOTE:${data.userdescript.replace(/\n/g, ' ')}\n`;
                }
            }
            vcf += 'END:VCARD\n';
            return vcf;
        },

        downloadVCard(vcf, filename) {
            const blob = new Blob([vcf], { type: 'text/vcard;charset=utf-8' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            a.click();
            window.URL.revokeObjectURL(url);
        },

        async copyToClipboard(text) {
            try {
                await navigator.clipboard.writeText(text);
                api.showToast('已复制到剪贴板', 'success');
                return true;
            } catch (err) {
                const textarea = document.createElement('textarea');
                textarea.value = text;
                textarea.style.position = 'fixed';
                textarea.style.left = '-9999px';
                document.body.appendChild(textarea);
                textarea.select();
                try {
                    document.execCommand('copy');
                    api.showToast('已复制到剪贴板', 'success');
                    return true;
                } finally {
                    document.body.removeChild(textarea);
                }
            }
        }
    }
};
