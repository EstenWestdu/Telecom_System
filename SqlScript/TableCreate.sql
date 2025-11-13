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
    package_id INT NOT NULL,
    FOREIGN KEY (package_id) REFERENCES package_info(id)
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