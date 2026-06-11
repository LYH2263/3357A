const API_BASE = 'http://localhost:8357/api';

const api = {
    baseUrl: API_BASE,
    async request(url, options = {}) {
        try {
            const response = await fetch(`${API_BASE}${url}`, {
                headers: {
                    'Content-Type': 'application/json',
                },
                ...options,
            });
            if (!response.ok) throw new Error('Network response was not ok');
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            this.showToast('网络请求失败，请稍后重试', 'danger');
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
    }
};
