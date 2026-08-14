const PAGE_SIZE = 12;
let allProducts = [], allOrders = [], allSuppliers = [], allStock = [], allActivities = [];
let productsPage = 1, ordersPage = 1, stockPage = 1;

const ACTIVITY_LABELS = {
    MANAGE_PRODUCTS:'Manage products',
    MANAGE_SUPPLIERS:'Manage suppliers',
    MANAGE_ORDERS:'Manage orders',
    MANAGE_STOCK:'Manage stock',
    VIEW_REPORTS:'View reports',
    MANAGE_ADMINS:'Manage admins'
};

document.addEventListener('DOMContentLoaded', function () {
    const user = WareflowAuth.getUser();
    if (!user) return;
    loadProducts(); loadOrders();
    if (user.role === 'SUPERADMIN' || user.role === 'ADMIN') { loadSuppliers(); loadStock(); }
    if (user.role === 'SUPERADMIN') { loadAdmins(); loadActivities(); }
    updateOrdersHeading(user.role);
});

function paginate(array, page, pageSize=PAGE_SIZE) {
    const totalPages = Math.max(1, Math.ceil(array.length / pageSize));
    page = Math.min(Math.max(1, page), totalPages);
    const start = (page - 1) * pageSize;
    return {items:array.slice(start,start+pageSize), page, totalPages, total:array.length};
}
function renderPaginationControls(id, page, totalPages, total, onChange) {
    const el = document.getElementById(id); if (!el) return;
    const start = total ? (page-1)*PAGE_SIZE+1 : 0, end = Math.min(page*PAGE_SIZE,total);
    const pages = []; for(let p=Math.max(1,page-2); p<=Math.min(totalPages,page+2); p++) pages.push(p);
    el.innerHTML = `<div>${start}–${end} of ${total}</div><div class="pagination-controls">
        <button class="pagination-btn" ${page===1?'disabled':''} data-page="${page-1}">‹</button>
        ${pages.map(p=>`<button class="pagination-btn ${p===page?'active':''}" data-page="${p}">${p}</button>`).join('')}
        <button class="pagination-btn" ${page===totalPages?'disabled':''} data-page="${page+1}">›</button></div>`;
    el.querySelectorAll('[data-page]').forEach(b=>b.addEventListener('click',()=>onChange(Number(b.dataset.page))));
}
function setStat(id, value) { const el=document.getElementById(id); if(el) el.textContent = Number(value ?? 0).toLocaleString(); }
function setLoadError(id, message='Unable to load') { const el=document.getElementById(id); if(el) el.textContent = message; }
async function readPayload(res) {
    const payload = await res.json().catch(() => null);
    return payload;
}
function collectionFrom(payload, keys=[]) {
    if (Array.isArray(payload)) return payload;
    if (!payload || typeof payload !== 'object') return [];
    for (const key of keys) if (Array.isArray(payload[key])) return payload[key];
    if (Array.isArray(payload.content)) return payload.content;
    if (Array.isArray(payload.items)) return payload.items;
    if (Array.isArray(payload.data)) return payload.data;
    return [];
}
function totalFrom(payload, fallbackArray) {
    if (payload && typeof payload === 'object') {
        const candidates = [payload.totalElements, payload.total, payload.count, payload.totalCount];
        const found = candidates.find(v => Number.isFinite(Number(v)));
        if (found !== undefined) return Number(found);
    }
    return fallbackArray.length;
}
function escapeHtml(value) { const d=document.createElement('div'); d.textContent=value ?? ''; return d.innerHTML; }
function statusBadge(status) {
    const s=String(status||'').toUpperCase();
    const cls=s==='COMPLETED'?'badge-success':(s==='CANCELLED'?'badge-danger':(s==='PROCESSING'||s==='SHIPPED'?'badge-info':'badge-accent'));
    return `<span class="badge ${cls}">${escapeHtml(status || 'UNKNOWN')}</span>`;
}

async function loadProducts() {
    const res=await WareflowAuth.fetchAuth('/api/products'); if(!res.ok)return;
    const payload=await readPayload(res); allProducts=collectionFrom(payload,['products']); productsPage=1; const total=totalFrom(payload,allProducts); setStat('stat-products',total); document.getElementById('product-count').textContent=`${total.toLocaleString()} items`; renderProductsPage();
}
function filteredProducts() {
    const q=(document.getElementById('product-search')?.value||'').trim().toLowerCase();
    if(!q)return allProducts;
    return allProducts.filter(p=>`${p.sku||''} ${p.name||''}`.toLowerCase().includes(q));
}
function renderProductsPage() {
    const list=filteredProducts(), data=paginate(list,productsPage); productsPage=data.page;
    const tbody=document.getElementById('products-table-body'); if(!tbody)return;
    if(!data.items.length){ tbody.innerHTML=`<tr><td colspan="6"><div class="empty-state"><div class="empty-icon">⌕</div><strong>No products found</strong><p class="muted">Try another search.</p></div></td></tr>`; }
    else tbody.innerHTML=data.items.map(p=>`<tr>
        <td class="primary-cell">${escapeHtml(p.sku)}</td>
        <td><div class="primary-cell">${escapeHtml(p.name)}</div><div class="secondary-cell">Product #${escapeHtml(p.id)}</div></td>
        <td>$${Number(p.price||0).toFixed(2)}</td><td>${escapeHtml(p.category?.name||'—')}</td><td>${escapeHtml(p.supplier?.name||'—')}</td>
        <td><button class="btn btn-secondary btn-small" onclick="placeOrder(${p.id})">Place order</button></td></tr>`).join('');
    renderPaginationControls('products-pagination',data.page,data.totalPages,data.total,p=>{productsPage=p;renderProductsPage();});
}
async function createProduct() {
    const msg=document.getElementById('product-msg'); msg.className='alert'; msg.classList.remove('hidden'); msg.textContent='Creating…';
    const body={sku:document.getElementById('new-sku').value.trim(),name:document.getElementById('new-name').value.trim(),price:parseFloat(document.getElementById('new-price').value),categoryId:parseInt(document.getElementById('new-category-id').value),supplierId:parseInt(document.getElementById('new-supplier-id').value)};
    const res=await WareflowAuth.fetchAuth('/api/products',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
    if(!res.ok){const err=await res.json().catch(()=>({}));msg.classList.add('alert-error');msg.textContent=err.message||'Failed to create product.';return;}
    msg.classList.add('alert-success');msg.textContent='Product created successfully.'; document.querySelectorAll('#add-product-card input').forEach(i=>i.value=''); loadProducts();
}
async function placeOrder(productId) {
    const qty=prompt('Quantity to order','1'); if(!qty||isNaN(qty)||parseInt(qty)<1)return;
    const res=await WareflowAuth.fetchAuth('/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({items:[{productId,quantity:parseInt(qty)}]})});
    if(!res.ok){const err=await res.json().catch(()=>({}));alert(err.message||'Failed to place order.');return;}
    alert('Order placed successfully.'); loadOrders();
}

async function loadSuppliers(){const res=await WareflowAuth.fetchAuth('/api/suppliers');if(!res.ok){setLoadError('stat-suppliers');return;}const payload=await readPayload(res);allSuppliers=collectionFrom(payload,['suppliers']);const total=totalFrom(payload,allSuppliers);setStat('stat-suppliers',total);document.getElementById('supplier-count').textContent=`${total.toLocaleString()} suppliers`;document.getElementById('suppliers-table-body').innerHTML=allSuppliers.map(s=>`<tr><td class="primary-cell">${escapeHtml(s.name)}</td><td>${escapeHtml(s.contactEmail||'—')}</td><td>${escapeHtml(s.contactPhone||'—')}</td></tr>`).join('')||`<tr><td colspan="3"><div class="empty-state">No suppliers available.</div></td></tr>`;}
async function loadStock(){
    const res=await WareflowAuth.fetchAuth('/api/stock');
    if(!res.ok)return;
    const payload=await readPayload(res);
    allStock=collectionFrom(payload,['stock','stockRecords']);
    stockPage=1;
    setStat('stat-stock',allStock.reduce((sum,r)=>sum+Number(r.quantity||0),0));
    document.getElementById('stock-count').textContent=`${totalFrom(payload,allStock).toLocaleString()} records`;
    renderStockPage();
}
function renderStockPage(){
    const data=paginate(allStock,stockPage);
    stockPage=data.page;
    const tbody=document.getElementById('stock-table-body');
    if(!tbody)return;
    if(!data.items.length){
        tbody.innerHTML=`<tr><td colspan="3"><div class="empty-state"><div class="empty-icon">▤</div><strong>No stock records found</strong><p class="muted">Stock records will appear here when inventory is available.</p></div></td></tr>`;
    }else{
        tbody.innerHTML=data.items.map(r=>`<tr><td class="primary-cell">${escapeHtml(r.product?.name||('Product #'+(r.product?.id||'—')))}</td><td>${escapeHtml(r.warehouseLocation||'—')}</td><td class="primary-cell">${Number(r.quantity||0).toLocaleString()}</td></tr>`).join('');
    }
    renderPaginationControls('stock-pagination',data.page,data.totalPages,data.total,p=>{stockPage=p;renderStockPage();});
}

async function loadOrders(){const res=await WareflowAuth.fetchAuth('/api/orders');if(!res.ok){setLoadError('stat-orders');return;}const payload=await readPayload(res);allOrders=collectionFrom(payload,['orders']);ordersPage=1;const total=totalFrom(payload,allOrders);setStat('stat-orders',total);document.getElementById('order-count').textContent=`${total.toLocaleString()} orders`;renderOrdersPage();}
function filteredOrders(){const q=(document.getElementById('order-search')?.value||'').trim().toLowerCase();if(!q)return allOrders;return allOrders.filter(o=>String(o.id).includes(q)||o.items?.some(i=>String(i.productName||'').toLowerCase().includes(q)));}
function updateOrdersHeading(role){const h=document.getElementById('orders-heading');if(h)h.textContent=role==='SUPERADMIN'?'All Orders':'My Orders';const m=document.getElementById('stat-orders-meta');if(m)m.textContent=role==='SUPERADMIN'?'system-wide orders':'orders visible to you';}
function renderOrdersPage(){const user=WareflowAuth.getUser(),canManage=user?.role==='SUPERADMIN'||user?.role==='ADMIN';const data=paginate(filteredOrders(),ordersPage);ordersPage=data.page;const tbody=document.getElementById('orders-table-body');if(!tbody)return;if(!data.items.length){tbody.innerHTML=`<tr><td colspan="5"><div class="empty-state"><div class="empty-icon">↗</div><strong>No orders found</strong><p class="muted">Your order activity will appear here.</p></div></td></tr>`;}else tbody.innerHTML=data.items.map(o=>`<tr><td class="primary-cell">#${escapeHtml(o.id)}</td><td>${statusBadge(o.status)}</td><td>${o.createdAt?new Date(o.createdAt).toLocaleDateString():'—'}</td><td>${(o.items||[]).map(i=>`${escapeHtml(i.productName)} × ${escapeHtml(i.quantity)}`).join('<br>')||'—'}</td><td>${canManage?`<select onchange="updateOrderStatus(${o.id},this.value)"><option value="">Change status…</option>${['PENDING','PROCESSING','SHIPPED','COMPLETED','CANCELLED'].map(s=>`<option value="${s}">${s}</option>`).join('')}</select>`:'<span class="muted">View only</span>'}</td></tr>`).join('');renderPaginationControls('orders-pagination',data.page,data.totalPages,data.total,p=>{ordersPage=p;renderOrdersPage();});}
async function updateOrderStatus(orderId,status){if(!status)return;const res=await WareflowAuth.fetchAuth(`/api/orders/${orderId}/status`,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({status})});if(!res.ok){const err=await res.json().catch(()=>({}));alert(err.message||'Failed to update status.');return;}loadOrders();}

async function loadActivities(){const res=await WareflowAuth.fetchAuth('/api/activities');if(!res.ok)return;const payload=await readPayload(res);allActivities=collectionFrom(payload,['activities']);renderAdmins();}
async function loadAdmins(){const res=await WareflowAuth.fetchAuth('/api/admins');if(!res.ok)return;const payload=await readPayload(res);window.inventraAdmins=collectionFrom(payload,['admins','users']);renderAdmins();}
function activityName(a){return typeof a==='string'?a:(a.name||a.activity||a.code||'');}
function renderAdmins(){const tbody=document.getElementById('admins-table-body');if(!tbody||!window.inventraAdmins)return;const activities=(allActivities.length?allActivities:['MANAGE_PRODUCTS','MANAGE_SUPPLIERS','MANAGE_ORDERS','MANAGE_STOCK','VIEW_REPORTS','MANAGE_ADMINS']).map(activityName).filter(Boolean);tbody.innerHTML=window.inventraAdmins.map(a=>{const granted=a.grantedActivities||[];return `<tr><td><div class="primary-cell">${escapeHtml(a.username)}</div><div class="secondary-cell">Admin #${escapeHtml(a.userId)}</div></td><td>${escapeHtml(a.email||'—')}</td><td>${granted.length?granted.map(x=>`<span class="badge badge-accent" style="margin:2px;">${escapeHtml(x)}</span>`).join(''):'<span class="muted">No permissions assigned</span>'}</td><td class="admin-permissions"><div class="permission-grid">${activities.map(act=>`<label class="permission-option"><input type="checkbox" ${granted.includes(act)?'checked':''} onchange="togglePermission(${a.userId},'${act}',this)"><span>${escapeHtml(ACTIVITY_LABELS[act]||act)}</span></label>`).join('')}</div></td></tr>`;}).join('')||`<tr><td colspan="4"><div class="empty-state">No administrators found.</div></td></tr>`;}
async function promoteUser(){const msg=document.getElementById('promote-msg'),id=document.getElementById('promote-user-id').value;if(!id){msg.textContent='Enter a user ID first.';msg.style.color='var(--danger)';return;}const res=await WareflowAuth.fetchAuth(`/api/admins/${id}/promote`,{method:'POST'});if(!res.ok){const err=await res.json().catch(()=>({}));msg.textContent=err.message||'Unable to promote user.';msg.style.color='var(--danger)';return;}msg.textContent='User promoted to Admin.';msg.style.color='var(--success)';document.getElementById('promote-user-id').value='';loadAdmins();}
async function togglePermission(userId,activity,checkbox){const res=await WareflowAuth.fetchAuth(`/api/admins/${userId}/permissions`,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({activity,grant:checkbox.checked})});if(!res.ok){checkbox.checked=!checkbox.checked;const err=await res.json().catch(()=>({}));alert(err.message||'Failed to update permission.');return;}loadAdmins();}
