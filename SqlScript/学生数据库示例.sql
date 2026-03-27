-- Active: 1766589323134@@127.0.0.1@5432@postgres
CREATE TABLE 学生 (
    学号 VARCHAR(20) PRIMARY KEY,
    姓名 VARCHAR(50),
    性别 CHAR(2),
    所在专业 VARCHAR(50)
);

CREATE TABLE 课程 (
    课程号 VARCHAR(20) PRIMARY KEY,
    课程名 VARCHAR(100),
    学分 INT,
    课时数 INT
);

CREATE TABLE 教师 (
    教师号 VARCHAR(20) PRIMARY KEY,
    姓名 VARCHAR(50),
    性别 CHAR(2),
    年龄 INT,
    教师职称 VARCHAR(50),
    所属学院 VARCHAR(50)
);

CREATE TABLE 讲授 (
    教师号 VARCHAR(20),
    课程号 VARCHAR(20),
    上课时间 VARCHAR(50),
    上课地点 VARCHAR(100),
    PRIMARY KEY (教师号, 课程号),
    FOREIGN KEY (教师号) REFERENCES 教师(教师号),
    FOREIGN KEY (课程号) REFERENCES 课程(课程号)
);

CREATE TABLE 选修 (
    学号 VARCHAR(20),
    课程号 VARCHAR(20),
    分数 DECIMAL(5,2),
    PRIMARY KEY (学号, 课程号),
    FOREIGN KEY (学号) REFERENCES 学生(学号),
    FOREIGN KEY (课程号) REFERENCES 课程(课程号)
);

-- 2. 插入测试数据
INSERT INTO 学生 VALUES 
('S001', '张三', '男', '计算机'),
('S002', '李四', '女', '计算机'),
('S003', '王五', '男', '数学');

INSERT INTO 课程 VALUES 
('304047030', '数据库原理', 3, 48),
('304156050', '操作系统', 3, 48),
('304178020', '数据结构', 4, 64);

INSERT INTO 教师 VALUES 
('T001', '张三', '男', 35, '教授', '计算机学院'),
('T002', '李四', '女', 40, '副教授', '软件学院');

INSERT INTO 讲授 VALUES 
('T001', '304047030', '第三大节', '望江基础教学楼B101'),
('T002', '304156050', '第二大节', '江安一教A101'),
('T001', '304178020', '第一大节', '望江基础教学楼A201');

INSERT INTO 选修 VALUES 
('S001', '304047030', 85.5),
('S001', '304156050', 92.0),
('S002', '304047030', 78.0),
('S002', '304178020', 88.5),
('S003', '304156050', 95.0);


-- 3. 查询示例
-- 查询没学课程号为“304047030”课程的学生信息，包括学号，姓名和专业；
SELECT 学生.学号, 学生.姓名, 学生.所在专业
FROM 学生
WHERE 学生.学号 NOT IN (
    SELECT 选修.学号
    FROM 选修
    WHERE 选修.课程号 = '304047030'
);
--查询至少学过课程号为“304047030”和“304156050”的课程的学生的学号、姓名和专业;
SELECT 学号, 姓名, 所在专业
FROM 学生 
WHERE 学号 IN (
    SELECT 学号
    FROM 选修
    WHERE 课程号 = '304047030'
) AND 学号 IN (
    SELECT 学号
    FROM 选修
    WHERE 课程号 = '304156050'
);
SELECT 学号, 姓名, 所在专业
FROM 学生 S
WHERE NOT EXISTS (
    (
    SELECT 课程号
    FROM 课程 
    WHERE 课程号 = '304047030' OR 课程号 = '304156050')
    EXCEPT  (
        SELECT 课程号
        FROM 选修 M
        WHERE M.学号 = S.学号
    )
);
-- 查询选修了课程号为“304047030”的课程但没有选修课程号为“304156050”的课程的学生的学号、姓名和专业;
SELECT 学号, 姓名, 所在专业
FROM 学生
WHERE 学号 IN (
    SELECT 选修.学号
    FROM 选修
    WHERE 选修.课程号 = '304047030'
) AND 学号 NOT IN (
    SELECT 选修.学号
    FROM 选修
    WHERE 选修.课程号 = '304156050'
);


-- 查询选修了所有计算机学院课程的学生的学号、姓名和专业。
SELECT 学号, 姓名, 所在专业
FROM 学生 S
WHERE NOT EXISTS (
    SELECT 课程号
    FROM 课程
    WHERE 课程号 IN (
        SELECT 课程号
        FROM 讲授
        WHERE 教师号 IN (
            SELECT 教师号
            FROM 教师
            WHERE 所属学院 = '计算机学院'
        )
    ) EXCEPT (
        SELECT 课程号
        FROM 选修
        WHERE 选修.学号 = S.学号
    )
);
