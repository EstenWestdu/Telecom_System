(() => {
    const cfg = window.__USER_MENU_CONFIG__ || {};
    const account = cfg.account || 0; // from injected config
    const apiBase = cfg.apiBase || "/user"; // default

    // 显示登录成功的 toast（如果有 flash attribute 'success'）
    let loginMsg = cfg.loginMsg || '';
        // Safety: backend template may render null/undefined as string 'null' or 'undefined'.
        if (typeof loginMsg === 'string') {
            loginMsg = loginMsg.trim();
            if (loginMsg.toLowerCase() === 'null' || loginMsg.toLowerCase() === 'undefined') {
                loginMsg = '';
            }
        } else {
            loginMsg = '';
        }
        if (loginMsg) {
        const toastEl = document.getElementById('loginToast');
        document.getElementById('loginToastBody').textContent = loginMsg;
        const t = new bootstrap.Toast(toastEl, { delay: 3000 });
        t.show();
    }

    const showResultModal = (obj) => {
        const pre = document.getElementById('apiResultPre');
        if (obj && typeof obj === 'object' && (obj.message || obj.error || obj.msg)) {
            pre.textContent = obj.message || obj.error || obj.msg;
        } else if (typeof obj === 'string') {
            pre.textContent = obj;
        } else {
            try {
                pre.textContent = JSON.stringify(obj, null, 2);
            } catch (e) {
                pre.textContent = String(obj);
            }
        }
        const modal = new bootstrap.Modal(document.getElementById('apiResultModal'));
        modal.show();
    };

    const reportFrontendError = (action, error, extra = {}) => {
        const payload = {
            action,
            message: error && error.message ? error.message : String(error),
            stack: error && error.stack ? error.stack : null,
            url: extra.url || window.location.pathname,
            userAgent: navigator.userAgent,
            detail: extra.detail || null
        };
        return fetch('/logs/frontend', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        }).catch(() => { /* 忽略日志上报失败 */ });
    };

    const handleRequest = (url, { method = 'GET', body, headers } = {}) => {
        const opts = { method, headers: headers ? { ...headers } : {} };
        if (body !== undefined) {
            opts.headers['Content-Type'] = opts.headers['Content-Type'] || 'application/json';
            opts.body = typeof body === 'string' ? body : JSON.stringify(body);
        }
        return fetch(url, opts)
            .then(async resp => {
                if (resp.ok) {
                    const text = await resp.text();
                    if (!text) return {};
                    try {
                        return JSON.parse(text);
                    } catch (e) {
                        reportFrontendError('handleRequest-parse', e, { url, method });
                        return {};
                    }
                }
                let errBody = null;
                try {
                    errBody = await resp.json();
                } catch (e) {
                    // ignore parse failure
                }
                const message = (errBody && (errBody.message || errBody.error || errBody.msg)) || `请求失败，状态码：${resp.status}`;
                const err = new Error(message);
                err.response = errBody;
                reportFrontendError('handleRequest-error', err, { url, method });
                throw err;
            })
            .catch(err => {
                if (!err.response) {
                    reportFrontendError('handleRequest-network', err, { url, method });
                }
                throw err;
            });
    };

    const handleApiError = (err, fallbackMsg) => {
        console.error(err);
        const resp = err && err.response;
        const msg = resp && (resp.message || resp.error || resp.msg) || (err && err.message);

        if (msg && /余额不足|insufficient balance|Insufficient balance/i.test(msg)) {
            alert('余额不足，请先充值。');
            reportFrontendError('insufficient-balance', new Error(msg), { detail: resp });
            return;
        }

        if (msg) {
            showResultModal({ message: msg });
        } else if (fallbackMsg) {
            alert(fallbackMsg);
        } else {
            alert('操作失败');
        }

        try {
            reportFrontendError('handleApiError', new Error(msg || fallbackMsg || '操作失败'), { detail: resp });
        } catch (e) {
            // 忽略上报失败
        }
    };

    const fetchProfile = () => {
        console.log('[user_menu] fetchProfile', account);

        Promise.all([
            handleRequest(`${apiBase}/${account}`),
            handleRequest(`${apiBase}/${account}/remaining-time`)
        ])
            .then(([user, remainingInfo]) => {
                document.getElementById('field-account').textContent = user.account ?? '';
                document.getElementById('field-name').textContent = user.name ?? '';
                document.getElementById('field-phone').textContent = user.phone ?? '';
                document.getElementById('field-packageId').textContent = user.packageId ?? '';
                document.getElementById('field-balance').innerHTML = '￥' + (user.balance ?? '');
                document.getElementById('field-usedTime').textContent = remainingInfo?.usedDurationText ?? '0小时';
                document.getElementById('field-remainingTime').textContent = remainingInfo?.remainingDurationText ?? '0小时';
            })
            .catch(err => handleApiError(err, '获取信息失败'));
    };

    document.getElementById('refreshProfile').addEventListener('click', fetchProfile);

    // 套餐选择显示逻辑
    const select = document.getElementById('selectPackage');
    const descEl = document.getElementById('packageDesc');
    const priceEl = document.getElementById('packagePrice');

    function updateDetailFromOption(opt){
        if(!opt) return;
        const duration = opt.getAttribute('data-duration') || '';
        const cost = opt.getAttribute('data-cost');
        descEl.textContent = duration;
        priceEl.textContent = cost != null && cost !== '' ? cost + ' 元' : '—';
    }

    if(select){
        updateDetailFromOption(select.options[select.selectedIndex]);
        select.addEventListener('change', function(){
            updateDetailFromOption(this.options[this.selectedIndex]);
        });
    }

    const packageList = document.getElementById('packageList');
    if(packageList){
        packageList.addEventListener('click', function(e){
            const a = e.target.closest('a');
            if(!a) return;
            e.preventDefault();
            const id = a.getAttribute('data-id');
            const opts = Array.from(select.options);
            const match = opts.find(o => o.value === id);
            if(match){
                select.value = id;
                updateDetailFromOption(match);
                const modalEl = document.getElementById('packageModal');
                const modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                modal.hide();
            }
        });
    }

    document.getElementById('changePackageBtn').addEventListener('click', () => {
        const pkg = document.getElementById('selectPackage').value;
        if (!confirm('确认购买并且将套餐更改为：' + pkg + ' ?' + '\n新套餐将直接覆盖旧套餐,且费用立即扣除。')) return;
        handleRequest(`${apiBase}/${account}/change-package?packageId=${encodeURIComponent(pkg)}`, { method: 'POST' })
            .then(data => {
                showResultModal(data);
                document.getElementById('field-packageId').textContent = data.newPackage ?? pkg;
            })
            .catch(err => handleApiError(err, '更改套餐失败'));
    });

    document.getElementById('rechargeBtn').addEventListener('click', () => {
        const amtEl = document.getElementById('rechargeAmount');
        const amt = Number(amtEl.value);
        if (!amt || amt <= 0) { alert('请输入大于0的金额'); return; }
        if (!confirm('确认充值 ' + amt + ' 元？')) return;
        handleRequest(`${apiBase}/${account}/recharge?amount=${encodeURIComponent(amt)}`, { method: 'POST' })
            .then(data => {
                showResultModal(data);
                amtEl.value = '';
                if (data.newBalance !== undefined) {
                    document.getElementById('field-balance').textContent = "￥" + data.newBalance;
                } else {
                    fetchProfile();
                }
            })
            .catch(err => handleApiError(err, '充值失败'));
    });

    // 页面初始加载个人信息
    fetchProfile();

    // 每分钟自动刷新个人信息
    let refreshInterval = setInterval(fetchProfile, cfg.refreshIntervalMs || 30000);

    console.log('[user_menu] 自动刷新已启动，间隔:', refreshInterval);

    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            clearInterval(refreshInterval);
        } else {
            refreshInterval = setInterval(fetchProfile, cfg.refreshIntervalMs || 30000);
            fetchProfile();
        }
    });

})();
