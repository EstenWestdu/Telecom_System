-- src/test/resources/data.sql
INSERT INTO package_info (id, duration, cost) VALUES 
(1, '100 hours', 20.00),
(2, '300 hours', 48.00);

-- 修正列名: package_id
INSERT INTO user_info (account, name, password, balance, package_id, phone) VALUES 
(200001, '测试用户1', 'pass123', 100.00, 1, '13800138001'),
(200002, '测试用户2', 'pass123', 50.00, 2, '13800138002');

INSERT INTO admin_info (account, name, password) VALUES 
(100001, 'admin', 'admin123');