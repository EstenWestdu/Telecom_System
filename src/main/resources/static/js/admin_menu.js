(() => {
    const apiPrefix = "/admin";
    let editingRow = null;

    const wrapper = document.getElementById("userTableWrapper");
    const pageIndicator = document.getElementById("pageIndicator");
    const tableLoading = document.getElementById("tableLoading");
    let currentPage = wrapper ? Number(wrapper.dataset.currentPage || 0) : 0;
    let totalPages = wrapper ? Number(wrapper.dataset.totalPages || 1) : 1;
    const pageSize = wrapper ? Number(wrapper.dataset.pageSize || 10) : 10;
    let wheelDebounceTimer = null;
    let scrollRestore = null;
    let isLoading = false;

    const reportFrontendError = (action, error, extra = {}) => {
        const payload = {
            action,
            message: error && error.message ? error.message : String(error),
            stack: error && error.stack ? error.stack : null,
            url: extra.url || window.location.pathname,
            userAgent: navigator.userAgent
        };
        return fetch('/logs/frontend', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        }).catch(() => { /* 忽略日志上报失败 */ });
    };

    const handleRequest = (url, method, body) => {
        return fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: body ? JSON.stringify(body) : undefined
        }).then(async resp => {
            if (resp.ok) {
                return resp.json().catch(() => ({}));
            }
            let errBody = null;
            try {
                errBody = await resp.json();
            } catch (e) {
                // ignore
            }
            const message = (errBody && (errBody.message || errBody.error || errBody.msg))
                || `请求失败，状态码：${resp.status}`;
            const err = new Error(message);
            reportFrontendError('handleRequest-error', err, { url, method });
            throw err;
        });
    };

    const escapeHtml = (text) => String(text ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");

    const setLoading = (visible) => {
        isLoading = visible;
        if (!tableLoading) return;
        tableLoading.style.display = visible ? "flex" : "none";
    };

    const updatePageIndicator = () => {
        if (!pageIndicator) return;
        const total = totalPages && totalPages > 0 ? totalPages : 1;
        const current = Math.min(total, currentPage + 1);
        pageIndicator.textContent = `第 ${current} 页 / 共 ${total} 页`;
    };

    const renderUsers = (users) => {
        const tbody = document.getElementById("userTableBody");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!users || users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">暂无数据</td></tr>';
            return;
        }

        users.forEach(user => {
            const tr = document.createElement("tr");
            const balanceVal = user.balance ?? "";
            const accountDisplay = escapeHtml(user.account ?? "");
            const nameDisplay = escapeHtml(user.name ?? "");
            const phoneDisplay = escapeHtml(user.phone ?? "");
            const packageDisplay = escapeHtml(user.packageId ?? "");
            const balanceDisplay = escapeHtml(balanceVal);
            tr.innerHTML = `
                <td>${accountDisplay}</td>
                <td data-field="name">${nameDisplay}</td>
                <td data-field="phone">${phoneDisplay}</td>
                <td data-field="packageId">${packageDisplay}</td>
                <td data-field="balance" data-original-value="${balanceVal}">￥<span>${balanceDisplay}</span></td>
                <td>
                    <button class="btn btn-warning btn-sm me-2 edit-btn" data-account="${user.account}">修改</button>
                    <button class="btn btn-danger btn-sm delete-btn" data-account="${user.account}">删除</button>
                </td>`;
            tbody.appendChild(tr);
        });
        attachRowHandlers();
    };

    const attachRowHandlers = () => {
        document.querySelectorAll('#userTableBody .edit-btn').forEach(btn => {
            if (btn.dataset.bound === 'true') return;
            btn.dataset.bound = 'true';
            btn.addEventListener('click', editBtnHandler);
        });
        document.querySelectorAll('#userTableBody .delete-btn').forEach(btn => {
            if (btn.dataset.bound === 'true') return;
            btn.dataset.bound = 'true';
            btn.addEventListener('click', deleteBtnHandler);
        });
    };

    const loadPage = (page, restorePosition) => {
        if (!wrapper) return Promise.resolve();
        if (page < 0) return Promise.resolve();
        if (isLoading) return Promise.resolve();
        setLoading(true);
        scrollRestore = restorePosition || null;
        return handleRequest(`${apiPrefix}/users?page=${page}&size=${pageSize}`, 'GET')
            .then(resp => {
                const content = Array.isArray(resp) ? resp : (resp.content || resp.data || []);
                currentPage = resp.pageNumber != null ? resp.pageNumber : page;
                if (resp.totalPages != null) {
                    totalPages = resp.totalPages;
                } else if (resp.totalElements != null) {
                    totalPages = Math.max(1, Math.ceil(resp.totalElements / pageSize));
                }
                renderUsers(content);
                updatePageIndicator();
            })
            .catch(err => {
                console.error('[loadPage] 获取分页失败', err);
                reportFrontendError('loadPage', err, { url: `${apiPrefix}/users?page=${page}&size=${pageSize}` });
                alert('获取用户列表失败：' + err.message);
            })
            .finally(() => {
                setLoading(false);
                if (wrapper) {
                    requestAnimationFrame(() => {
                        if (scrollRestore === 'top') {
                            wrapper.scrollTop = 1;
                        } else if (scrollRestore === 'bottom') {
                            wrapper.scrollTop = Math.max(0, wrapper.scrollHeight - wrapper.clientHeight - 1);
                        }
                        scrollRestore = null;
                    });
                }
            });
    };

    const refreshList = (page = currentPage, restore) => {
        if (!wrapper) {
            location.reload();
            return Promise.resolve();
        }
        return loadPage(page, restore);
    };

    function editBtnHandler(event) {
        const btn = event.currentTarget;
        const tr = btn.closest('tr');
        const deleteBtn = tr.querySelector('.delete-btn');

        if (btn.dataset.state === 'editing') {
            const account = btn.dataset.account;
            const payload = {};
            tr.querySelectorAll('input[name]').forEach(input => {
                payload[input.name] = input.type === 'number' ? Number(input.value) : input.value;
            });
            handleRequest(`${apiPrefix}/modify-${account}`, 'PUT', payload)
                .then(() => {
                    editingRow = null;
                    btn.dataset.state = '';
                    if (deleteBtn) deleteBtn.dataset.state = '';
                    refreshList(currentPage);
                })
                .catch(err => {
                    console.error('[edit] 修改失败', err);
                    alert('修改失败：' + err.message);
                });
            return;
        }

        if (editingRow && editingRow !== tr) {
            alert('同时只能编辑一个用户，请先保存或取消当前行');
            return;
        }

        tr.querySelectorAll('td[data-field]').forEach(td => {
            const field = td.getAttribute('data-field');
            const isNumber = field === 'balance' || field === 'packageId';
            const value = field === 'balance' ? td.getAttribute('data-original-value') : td.textContent.trim();
            td.innerHTML = `<input class="form-control form-control-sm" name="${field}" type="${isNumber ? 'number' : 'text'}" value="${escapeHtml(value)}">`;
        });

        btn.dataset.state = 'editing';
        btn.textContent = '保存';
        editingRow = tr;

        if (deleteBtn) {
            deleteBtn.dataset.state = 'cancel';
            deleteBtn.textContent = '取消';
            deleteBtn.classList.remove('btn-danger');
            deleteBtn.classList.add('btn-secondary');
        }
    }

    function deleteBtnHandler(event) {
        const btn = event.currentTarget;
        const account = btn.dataset.account;

        if (btn.dataset.state === 'cancel') {
            editingRow = null;
            refreshList(currentPage);
            return;
        }

        if (!confirm(`确认删除账号 ${account} 吗？`)) return;
        handleRequest(`${apiPrefix}/delete-${account}`, 'DELETE')
            .then(() => refreshList(currentPage))
            .catch(err => {
                console.error('[delete] 删除失败', err);
                alert('删除失败：' + err.message);
            });
    }

    document.getElementById('addUser').addEventListener('click', () => {
        const card = document.getElementById('addUserCard');
        const form = document.getElementById('addUserForm');
        form.reset();
        card.style.display = 'block';
        card.scrollIntoView({ behavior: 'smooth' });
    });

    document.getElementById('cancelAddUser').addEventListener('click', () => {
        document.getElementById('addUserCard').style.display = 'none';
    });

    document.getElementById('addUserForm').addEventListener('submit', function (e) {
        e.preventDefault();
        const formData = new FormData(this);
        const payload = {
            account: null,
            name: formData.get('name'),
            phone: formData.get('phone'),
            packageId: Number(formData.get('packageId')),
            balance: Number(formData.get('balance')),
            password: formData.get('password')
        };
        handleRequest(`${apiPrefix}/create-user`, 'POST', payload)
            .then(() => {
                document.getElementById('addUserCard').style.display = 'none';
                refreshList(0);
            })
            .catch(err => {
                console.error('[addUser] 失败', err);
                reportFrontendError('addUser', err);
                alert('添加用户失败：' + err.message);
            });
    });

    const initSlidingWindow = () => {
        attachRowHandlers();
        updatePageIndicator();
        if (!wrapper) return;
        wrapper.addEventListener('wheel', (ev) => {
            if (isLoading) return;
            if (wheelDebounceTimer) return;
            wheelDebounceTimer = setTimeout(() => {
                wheelDebounceTimer = null;
            }, 160);
            const delta = ev.deltaY;
            const nearTop = wrapper.scrollTop < 40;
            const nearBottom = (wrapper.scrollHeight - wrapper.clientHeight - wrapper.scrollTop) < 40;
            if (delta > 0 && nearBottom && currentPage + 1 < totalPages) {
                ev.preventDefault();
                loadPage(currentPage + 1, 'top');
            } else if (delta < 0 && nearTop && currentPage > 0) {
                ev.preventDefault();
                loadPage(currentPage - 1, 'bottom');
            }
        });
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initSlidingWindow);
    } else {
        initSlidingWindow();
    }

    document.getElementById('analyzeTraffic').addEventListener('click', () => {
        fetch('/admin/traffic-stats')
            .then(resp => {
                if (!resp.ok) throw new Error('请求失败，状态码：' + resp.status);
                return resp.json();
            })
            .then(data => {
                let hourlyData = data;
                if (!Array.isArray(hourlyData)) {
                    if (hourlyData && Array.isArray(hourlyData.data)) {
                        hourlyData = hourlyData.data;
                    } else if (hourlyData && Array.isArray(hourlyData.stats)) {
                        hourlyData = hourlyData.stats;
                    } else {
                        hourlyData = [hourlyData];
                    }
                }

                const hours = Array.from({ length: 24 }, (_, i) => i);
                const onlineUserCounts = Array(24).fill(0);
                hourlyData.forEach(item => {
                    const hour = item.hour;
                    const count = item.onlineUserCount || 0;
                    if (hour >= 0 && hour <= 23) {
                        onlineUserCounts[hour] = count;
                    }
                });

                const ctx = document.getElementById('hourlyChartCanvas').getContext('2d');
                if (window.hourlyChartInstance) {
                    window.hourlyChartInstance.destroy();
                }
                window.hourlyChartInstance = new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: hours.map(h => `${h}:00`),
                        datasets: [{
                            label: '小时活跃度',
                            data: onlineUserCounts,
                            borderColor: 'rgba(54, 162, 235, 1)',
                            backgroundColor: 'rgba(54, 162, 235, 0.2)',
                            tension: 0.4,
                            fill: true,
                            pointRadius: 3,
                        }]
                    },
                    options: {
                        responsive: true,
                        plugins: {
                            title: {
                                display: true,
                                text: '24小时用户流量分析'
                            },
                            tooltip: {
                                mode: 'index',
                                intersect: false
                            }
                        },
                        scales: {
                            x: {
                                title: {
                                    display: true,
                                    text: '小时'
                                },
                                grid: { display: true }
                            },
                            y: {
                                title: {
                                    display: true,
                                    text: '在线用户数'
                                },
                                beginAtZero: true,
                                grid: { display: true },
                                ticks: { precision: 0 }
                            }
                        },
                        interaction: { intersect: false, mode: 'nearest' }
                    }
                });

                const modalEl = document.getElementById('hourlyModal');
                const modal = new bootstrap.Modal(modalEl);
                modal.show();
            })
            .catch(err => {
                console.error('[analyzeTraffic] 流量分析接口不可用', err);
                reportFrontendError('analyzeTraffic', err, { url: '/admin/traffic-stats' });
                alert('流量分析接口暂不可用：' + err.message);
            });
    });
})();
