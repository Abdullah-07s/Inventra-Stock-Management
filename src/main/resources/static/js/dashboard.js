// Role-aware presentation layer. Backend authorization remains authoritative.
document.addEventListener('DOMContentLoaded', async function () {
    WareflowAuth.requireAuth();
    const user = WareflowAuth.getUser();
    if (!user) return;

    const roleLabel = {SUPERADMIN:'Super Admin', ADMIN:'Admin', USER:'User'}[user.role] || user.role;
    document.getElementById('welcome-role').textContent = roleLabel;
    document.getElementById('welcome-username').textContent = user.username || 'there';
    document.getElementById('sidebar-username').textContent = user.username || 'Account';
    document.getElementById('sidebar-role').textContent = roleLabel;
    document.getElementById('sidebar-avatar').textContent = (user.username || 'I').slice(0,1).toUpperCase();

    const permissions = await resolvePermissions(user);
    renderNav(user.role, permissions);
    showInitialSection(user.role, permissions);
    setupMobileNav();
    setupSearchInputs();
});

async function resolvePermissions(user) {
    if (user.role === 'SUPERADMIN') return new Set(['MANAGE_PRODUCTS','MANAGE_SUPPLIERS','MANAGE_ORDERS','MANAGE_STOCK','VIEW_REPORTS','MANAGE_ADMINS']);
    if (user.role === 'USER') return new Set();
    try {
        const res = await WareflowAuth.fetchAuth('/api/admins');
        if (!res.ok) return new Set();
        const admins = await res.json();
        const me = admins.find(a => Number(a.userId) === Number(user.userId));
        return new Set(me?.grantedActivities || []);
    } catch (_) { return new Set(); }
}

function navItems(role, permissions) {
    const common = [{label:'Products', icon:'▦', section:'products-section', visible:true}];
    if (role === 'USER') return [...common, {label:'My Orders', icon:'↗', section:'orders-section', visible:true}];
    const items = [];
    if (role === 'SUPERADMIN' || permissions.has('MANAGE_ADMINS')) items.push({label:'Admin Management',icon:'♙',section:'admin-section',visible:true});
    if (role === 'SUPERADMIN' || permissions.has('MANAGE_PRODUCTS')) items.push({label:'Products',icon:'▦',section:'products-section',visible:true});
    if (role === 'SUPERADMIN' || permissions.has('MANAGE_SUPPLIERS')) items.push({label:'Suppliers',icon:'◈',section:'suppliers-section',visible:true});
    if (role === 'SUPERADMIN' || permissions.has('MANAGE_STOCK')) items.push({label:'Stock',icon:'▤',section:'stock-section',visible:true});
    if (role === 'SUPERADMIN' || permissions.has('MANAGE_ORDERS')) items.push({label:'Orders',icon:'↗',section:'orders-section',visible:true});
    return items;
}

function renderNav(role, permissions) {
    const navList = document.getElementById('nav-list');
    if (!navList) return;
    const items = navItems(role, permissions);
    navList.innerHTML = items.map((item, i) => `<li class="nav-item${i === 0 ? ' active' : ''}" data-section="${item.section}"><span class="nav-icon">${item.icon}</span><span>${item.label}</span></li>`).join('');
    navList.querySelectorAll('.nav-item').forEach(el => el.addEventListener('click', () => activateSection(el.dataset.section)));
}

function showInitialSection(role, permissions) {
    const items = navItems(role, permissions);
    activateSection(items[0]?.section || 'empty-section');
}

function activateSection(sectionId) {
    document.querySelectorAll('.nav-item').forEach(n => n.classList.toggle('active', n.dataset.section === sectionId));
    document.querySelectorAll('.dashboard-section').forEach(s => s.style.display = 'none');
    const target = document.getElementById(sectionId);
    if (target) target.style.display = 'block';
    document.getElementById('sidebar')?.classList.remove('open');
}

function setupMobileNav() {
    document.getElementById('menu-btn')?.addEventListener('click', () => document.getElementById('sidebar')?.classList.toggle('open'));
    document.getElementById('mobile-theme')?.addEventListener('click', () => document.getElementById('theme-toggle')?.click());
}

function setupSearchInputs() {
    document.getElementById('product-search')?.addEventListener('input', () => { productsPage = 1; renderProductsPage(); });
    document.getElementById('order-search')?.addEventListener('input', () => { ordersPage = 1; renderOrdersPage(); });
}
