const API_BASE = 'http://localhost:8357/api'; // Use local mapping for browser access

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
    }
};
