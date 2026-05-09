-- ======================================================
-- 项目名称：校园活动报名系统
-- 版本：一期课程可落地版
-- 数据库：MySQL 8.4.8 LTS
-- ======================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS ActivityFeedback;
DROP TABLE IF EXISTS Registration;
DROP TABLE IF EXISTS Activity;
DROP TABLE IF EXISTS Category;
DROP TABLE IF EXISTS Venue;
DROP TABLE IF EXISTS Campus;
DROP TABLE IF EXISTS User;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE User (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户唯一 ID',
    role VARCHAR(20) NOT NULL COMMENT 'STUDENT / ORGANIZER / ADMIN',
    username VARCHAR(50) NOT NULL COMMENT '姓名',
    student_no VARCHAR(20) UNIQUE COMMENT '学号，学生账号使用',
    password VARCHAR(255) NOT NULL COMMENT '加密后的登录密码；演示环境可使用明文初始化后再替换',
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT '联系电话',
    CONSTRAINT chk_user_role CHECK (role IN ('STUDENT', 'ORGANIZER', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Campus (
    campus_id INT AUTO_INCREMENT PRIMARY KEY,
    campus_name VARCHAR(100) NOT NULL UNIQUE COMMENT '校区名',
    location VARCHAR(255) COMMENT '校区位置'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Venue (
    venue_id INT AUTO_INCREMENT PRIMARY KEY,
    venue_name VARCHAR(100) NOT NULL COMMENT '场馆名称',
    room_number VARCHAR(50) NOT NULL COMMENT '房间号',
    capacity INT NOT NULL COMMENT '场地容量',
    campus_id INT NOT NULL COMMENT '所属校区',
    CONSTRAINT fk_venue_campus FOREIGN KEY (campus_id) REFERENCES Campus(campus_id),
    CONSTRAINT uq_venue_room UNIQUE (campus_id, venue_name, room_number),
    CONSTRAINT chk_venue_capacity CHECK (capacity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Activity (
    activity_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',
    enroll_deadline DATETIME NOT NULL COMMENT '报名截止时间',
    capacity_limit INT NOT NULL COMMENT '名额上限',
    current_enrollment INT NOT NULL DEFAULT 0 COMMENT '当前正选/已签到人数',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT '活动状态',
    reject_reason VARCHAR(500) COMMENT '审核驳回原因',
    poster_url VARCHAR(500) COMMENT '活动海报地址',
    description TEXT COMMENT '活动详情',
    venue_id INT NOT NULL,
    category_id INT NOT NULL,
    organizer_id INT NOT NULL,
    admin_id INT,
    CONSTRAINT fk_act_venue FOREIGN KEY (venue_id) REFERENCES Venue(venue_id),
    CONSTRAINT fk_act_category FOREIGN KEY (category_id) REFERENCES Category(category_id),
    CONSTRAINT fk_act_organizer FOREIGN KEY (organizer_id) REFERENCES User(user_id),
    CONSTRAINT fk_act_admin FOREIGN KEY (admin_id) REFERENCES User(user_id),
    CONSTRAINT chk_act_capacity CHECK (capacity_limit > 0),
    CONSTRAINT chk_act_current CHECK (current_enrollment >= 0 AND current_enrollment <= capacity_limit),
    CONSTRAINT chk_act_time CHECK (end_time > start_time),
    CONSTRAINT chk_act_deadline CHECK (enroll_deadline <= start_time),
    CONSTRAINT chk_act_status CHECK (status IN ('DRAFT', 'PENDING_REVIEW', 'REJECTED', 'PUBLISHED', 'ONGOING', 'FINISHED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Registration (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL COMMENT '报名学生',
    activity_id INT NOT NULL COMMENT '报名活动',
    registration_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    status VARCHAR(30) NOT NULL COMMENT '报名状态',
    queue_no INT COMMENT '候补序号',
    check_in_time DATETIME COMMENT '签到时间',
    CONSTRAINT fk_reg_student FOREIGN KEY (student_id) REFERENCES User(user_id),
    CONSTRAINT fk_reg_activity FOREIGN KEY (activity_id) REFERENCES Activity(activity_id),
    CONSTRAINT uq_reg_student_activity UNIQUE (student_id, activity_id),
    CONSTRAINT chk_reg_status CHECK (status IN ('ENROLLED', 'WAITLISTED', 'CANCELLED', 'CHECKED_IN', 'ABSENT')),
    CONSTRAINT chk_reg_queue CHECK (queue_no IS NULL OR queue_no > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ActivityFeedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    registration_id INT NOT NULL COMMENT '对应报名记录，一条报名最多一条评价',
    activity_id INT NOT NULL COMMENT '评价活动',
    student_id INT NOT NULL COMMENT '评价学生',
    rating TINYINT NOT NULL COMMENT '1-5 星评分',
    content VARCHAR(1000) COMMENT '文字反馈',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次评价时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    CONSTRAINT fk_feedback_registration FOREIGN KEY (registration_id) REFERENCES Registration(registration_id),
    CONSTRAINT fk_feedback_activity FOREIGN KEY (activity_id) REFERENCES Activity(activity_id),
    CONSTRAINT fk_feedback_student FOREIGN KEY (student_id) REFERENCES User(user_id),
    CONSTRAINT uq_feedback_registration UNIQUE (registration_id),
    CONSTRAINT chk_feedback_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_activity_status_time ON Activity(status, start_time);
CREATE INDEX idx_activity_venue_time ON Activity(venue_id, start_time, end_time);
CREATE INDEX idx_activity_organizer_status ON Activity(organizer_id, status);
CREATE INDEX idx_registration_activity_status_queue ON Registration(activity_id, status, queue_no);
CREATE INDEX idx_registration_student_status ON Registration(student_id, status);
CREATE INDEX idx_feedback_activity_rating ON ActivityFeedback(activity_id, rating);
CREATE INDEX idx_feedback_student_activity ON ActivityFeedback(student_id, activity_id);
CREATE INDEX idx_venue_campus ON Venue(campus_id);

-- 演示初始化数据，密码字段为演示明文；正式环境应替换为哈希。
INSERT INTO User(role, username, student_no, password, phone) VALUES
('STUDENT', '学生张三', '20230001', '123456', '13800000001'),
('ORGANIZER', '计算机协会', NULL, '123456', '13800000002'),
('ADMIN', '系统管理员', NULL, '123456', '13800000003');

INSERT INTO Campus(campus_name, location) VALUES
('邯郸校区', '上海市杨浦区邯郸路'),
('江湾校区', '上海市杨浦区淞沪路');

INSERT INTO Venue(venue_name, room_number, capacity, campus_id) VALUES
('光华楼', 'H3106', 80, 1),
('逸夫科技楼', 'A201', 50, 1),
('综合体育馆', '主馆', 200, 2);

INSERT INTO Category(category_name) VALUES
('学术讲座'),
('比赛竞赛'),
('文娱活动'),
('志愿服务');

INSERT INTO Activity(
    title, start_time, end_time, enroll_deadline, capacity_limit, current_enrollment,
    status, poster_url, description, venue_id, category_id, organizer_id, admin_id
) VALUES (
    '数据库系统项目分享会',
    '2026-05-20 14:00:00',
    '2026-05-20 16:00:00',
    '2026-05-19 22:00:00',
    80,
    0,
    'PUBLISHED',
    '',
    '面向数据库课程项目的小型分享会，介绍活动报名系统的设计与实现。',
    1,
    1,
    2,
    3
);
