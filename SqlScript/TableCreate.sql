DROP TABLE IF EXISTS login_info;
DROP TABLE IF EXISTS admin_info;
DROP TABLE IF EXISTS user_info;
DROP TABLE IF EXISTS package_info;

-- 创建套餐信息表
CREATE TABLE package_info (
    id INT PRIMARY KEY,
    duration INTERVAL NOT NULL,
    cost DECIMAL(10,2) NOT NULL
);
-- 创建用户信息表
CREATE TABLE user_info (
    account INT PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    password VARCHAR(20) NOT NULL,
	phone VARCHAR(20) NOT NULL,
    balance DECIMAL(10,2) NOT NULL,
    packge_id INT NOT NULL,
    FOREIGN KEY (packge_id) REFERENCES package_info(id)
);

-- 创建管理信息表
CREATE TABLE admin_info (
    account INT PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    password VARCHAR(20) NOT NULL
);

-- 创建登录信息表
CREATE TABLE login_info (
    account_id INT NOT NULL,
    login_time TIMESTAMPTZ NOT NULL,
    logout_time TIMESTAMPTZ,
    PRIMARY KEY (account_id, login_time),
    FOREIGN KEY (account_id) REFERENCES user_info(account)
);

-- 创建简化的用户剩余时长视图
CREATE VIEW user_remaining_time AS
SELECT 
    u.account,
    u.name,
    u.phone,
    p.duration as total_duration,
    COALESCE(SUM(l.logout_time - l.login_time), '0 hours') as used_duration,
    (p.duration - COALESCE(SUM(l.logout_time - l.login_time), '0 hours')) as remaining_duration,
    ROUND(
        EXTRACT(EPOCH FROM (p.duration - COALESCE(SUM(l.logout_time - l.login_time), '0 hours'))) / 3600, 2
    ) as remaining_hours,
    u.balance,
    p.cost,
    CASE 
        WHEN (p.duration - COALESCE(SUM(l.logout_time - l.login_time), '0 hours')) < INTERVAL '0 hours' 
        THEN '已超时'
        ELSE '正常'
    END as status
FROM user_info u
JOIN package_info p ON u.packge_id = p.id
LEFT JOIN login_info l ON u.account = l.account_id 
    AND l.logout_time IS NOT NULL  -- 只计算已完成的会话
GROUP BY u.account, u.name, u.phone, p.duration, u.balance, p.cost;

-- 使用视图查询所有用户剩余时长
SELECT * FROM user_remaining_time ORDER BY remaining_hours DESC;