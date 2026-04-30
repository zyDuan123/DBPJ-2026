-- ======================================================
-- 项目名称：校园活动报名系统 
-- ======================================================

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS Registration;
DROP TABLE IF EXISTS Activity;
DROP TABLE IF EXISTS Category;
DROP TABLE IF EXISTS Venue;
DROP TABLE IF EXISTS Campus;
DROP TABLE IF EXISTS User;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 用户表 (User) - 采用单表继承策略整合三个角色
CREATE TABLE User (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户唯一ID',
    role TINYINT NOT NULL COMMENT '角色标识: 0-学生, 1-组织者, 2-管理员',
    username VARCHAR(50) NOT NULL COMMENT '姓名',
    student_no VARCHAR(20) UNIQUE COMMENT '学号 (学生特有，全局唯一)',
    password VARCHAR(255) NOT NULL COMMENT '加密后的登录密码',
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT '联系电话 (唯一约束)',
    CONSTRAINT chk_role CHECK (role IN (0, 1, 2)) -- 检查约束：确保角色合法
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 校区表 (Campus)
CREATE TABLE Campus (
    campus_id INT AUTO_INCREMENT PRIMARY KEY,
    campus_name VARCHAR(100) NOT NULL UNIQUE COMMENT '校区名 (唯一)',
    location VARCHAR(255) COMMENT '校区物理位置'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 场地表 (Venue)
CREATE TABLE Venue (
    venue_id INT AUTO_INCREMENT PRIMARY KEY,
    venue_name VARCHAR(100) NOT NULL COMMENT '场馆名称',
    room_number VARCHAR(50) NOT NULL COMMENT '房间号',
    capacity INT NOT NULL COMMENT '场馆容纳人数',
    campus_id INT NOT NULL COMMENT '所属校区外键',
    CONSTRAINT fk_venue_campus FOREIGN KEY (campus_id) REFERENCES Campus(campus_id),
    CONSTRAINT chk_venue_capacity CHECK (capacity > 0) -- 检查约束：人数必须为正
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 分类表 (Category)
CREATE TABLE Category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE COMMENT '类别名称 (唯一)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 活动表 (Activity) - 核心业务表
CREATE TABLE Activity (
    activity_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    start_time DATETIME NOT NULL COMMENT '活动举办时间',
    capacity_limit INT NOT NULL COMMENT '名额上限 (CHECK约束保障)',
    current_enrollment INT DEFAULT 0 COMMENT '当前人数 (反范式冗余字段，用于查询优化)',
    description TEXT COMMENT '活动详情介绍',
    venue_id INT NOT NULL COMMENT '引用场地表外键',
    category_id INT NOT NULL COMMENT '引用分类表外键',
    organizer_id INT NOT NULL COMMENT '发布者(组织者)外键',
    admin_id INT COMMENT '审核者(管理员)外键',
    
    -- 外键定义
    CONSTRAINT fk_act_venue FOREIGN KEY (venue_id) REFERENCES Venue(venue_id),
    CONSTRAINT fk_act_category FOREIGN KEY (category_id) REFERENCES Category(category_id),
    CONSTRAINT fk_act_organizer FOREIGN KEY (organizer_id) REFERENCES User(user_id),
    CONSTRAINT fk_act_admin FOREIGN KEY (admin_id) REFERENCES User(user_id),
    
    -- 核心业务规则约束 
    CONSTRAINT chk_act_capacity CHECK (capacity_limit > 0), -- 上限必须大于0
    CONSTRAINT chk_enrollment_limit CHECK (current_enrollment <= capacity_limit) -- 当前人数不得超过上限
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 报名记录中间表 - 实现学生与活动的 M:N 关系
CREATE TABLE Registration (
    student_id INT NOT NULL COMMENT '引用用户表(学生)',
    activity_id INT NOT NULL COMMENT '引用活动表',
    registration_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间 (默认当前时间)',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待审核, 1-报名成功, 2-已取消',
    
    -- 复合主键：从底层杜绝同一学生重复报名同一活动
    PRIMARY KEY (student_id, activity_id), 
    CONSTRAINT fk_reg_student FOREIGN KEY (student_id) REFERENCES User(user_id),
    CONSTRAINT fk_reg_activity FOREIGN KEY (activity_id) REFERENCES Activity(activity_id),
    CONSTRAINT chk_reg_status CHECK (status IN (0, 1, 2)) -- 状态范围检查
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
