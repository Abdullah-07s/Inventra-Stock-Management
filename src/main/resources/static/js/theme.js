(function () {
    const STORAGE_KEY = 'inventra-theme';
    function applyTheme(theme) {
        if (theme === 'light') document.documentElement.setAttribute('data-theme', 'light');
        else document.documentElement.removeAttribute('data-theme');
    }
    const saved = localStorage.getItem(STORAGE_KEY) || 'dark';
    applyTheme(saved);
    document.addEventListener('DOMContentLoaded', function () {
        const toggle = document.getElementById('theme-toggle');
        const mobile = document.getElementById('mobile-theme');
        function updateLabel(theme) {
            if (toggle) toggle.textContent = theme === 'light' ? '☾ Dark mode' : '☀ Light mode';
            if (mobile) mobile.textContent = theme === 'light' ? '☾' : '◐';
        }
        updateLabel(saved);
        toggle?.addEventListener('click', function () {
            const current = localStorage.getItem(STORAGE_KEY) || 'dark';
            const next = current === 'light' ? 'dark' : 'light';
            localStorage.setItem(STORAGE_KEY, next); applyTheme(next); updateLabel(next);
        });
    });
})();
