// 백엔드 API 호출 함수
const API_BASE = document.querySelector('meta[name="api-base"]')?.content ?? '';

async function apiFetch(path, options = {}) {
    return fetch(`${API_BASE}${path}`, {
        credentials: 'include',
        ...options,
    });
}
