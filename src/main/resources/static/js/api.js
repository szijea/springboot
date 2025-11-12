// api.js
class PharmacyAPI {
    constructor() {
        this.baseURL = 'http://localhost:8080/api';
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;

        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            ...options,
        };

        if (config.body && typeof config.body === 'object') {
            config.body = JSON.stringify(config.body);
        }

        try {
            const response = await fetch(url, config);

            // 检查响应状态
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // 尝试解析JSON响应
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                const data = await response.json();
                return data;
            } else {
                // 如果不是JSON，返回文本
                const text = await response.text();
                return text;
            }
        } catch (error) {
            console.error('API request failed:', error);
            throw error;
        }
    }

    // 设置API
    settingAPI = {
        get: () => this.request('/settings'),
        update: (data) => this.request('/settings', {
            method: 'POST',
            body: data,
        }),
    };

    // 用户API
    userAPI = {
        changePassword: (oldPassword, newPassword) => this.request('/user/change-password', {
            method: 'POST',
            body: {
                oldPassword,
                newPassword,
                confirmPassword: newPassword,
            },
        }),
    };

    // 消息提示
    showMessage(message, type = 'success') {
        // 创建消息元素
        const messageEl = document.createElement('div');
        messageEl.className = `fixed top-4 right-4 px-6 py-3 rounded-lg shadow-lg z-50 transform transition-all duration-300 ${
            type === 'success' ? 'bg-green-500 text-white' :
            type === 'error' ? 'bg-red-500 text-white' :
            type === 'warning' ? 'bg-yellow-500 text-white' : 'bg-blue-500 text-white'
        }`;
        messageEl.textContent = message;

        // 添加到页面
        document.body.appendChild(messageEl);

        // 3秒后自动移除
        setTimeout(() => {
            messageEl.style.opacity = '0';
            messageEl.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (document.body.contains(messageEl)) {
                    document.body.removeChild(messageEl);
                }
            }, 300);
        }, 3000);
    }
}

// 创建全局API实例
window.api = new PharmacyAPI();