const WareflowAuth = (function () {
    const TOKEN_KEY = 'inventra-token';
    const USER_KEY = 'inventra-user';

    function decodeJwtPayload(token) {
        try {
            const payload = token.split('.')[1];
            const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
            return JSON.parse(decoded);
        } catch (_) { return null; }
    }
    function getToken() { return localStorage.getItem(TOKEN_KEY); }
    function getUser() {
        try { const raw = localStorage.getItem(USER_KEY); return raw ? JSON.parse(raw) : null; }
        catch (_) { return null; }
    }
    function isTokenExpired(token) {
        const payload = decodeJwtPayload(token);
        return !payload || !payload.exp || Date.now() >= payload.exp * 1000;
    }
    function isAuthenticated() { const token = getToken(); return !!token && !isTokenExpired(token); }
    function login(authResponse) {
        localStorage.setItem(TOKEN_KEY, authResponse.token);
        localStorage.setItem(USER_KEY, JSON.stringify({userId:authResponse.userId, username:authResponse.username, role:authResponse.role}));
    }
    function logout() { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); window.location.href='/login'; }
    async function fetchAuth(url, options={}) {
        const token = getToken();
        const headers = new Headers(options.headers || {});
        if (token) headers.set('Authorization', `Bearer ${token}`);
        const response = await fetch(url, {...options, headers});
        if (response.status === 401) { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); window.location.href='/login'; }
        return response;
    }
    function requireAuth() { if (!isAuthenticated()) window.location.href='/login'; }
    return {getToken,getUser,isAuthenticated,login,logout,fetchAuth,requireAuth};
})();
