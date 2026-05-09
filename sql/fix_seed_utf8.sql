SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

UPDATE User
SET username = CASE user_id
    WHEN 1 THEN '学生张三'
    WHEN 2 THEN '计算机协会'
    WHEN 3 THEN '系统管理员'
    ELSE username
END
WHERE user_id IN (1, 2, 3);

UPDATE Campus
SET campus_name = CASE campus_id
    WHEN 1 THEN '邯郸校区'
    WHEN 2 THEN '江湾校区'
    ELSE campus_name
END,
location = CASE campus_id
    WHEN 1 THEN '上海市杨浦区邯郸路'
    WHEN 2 THEN '上海市杨浦区淞沪路'
    ELSE location
END
WHERE campus_id IN (1, 2);

UPDATE Venue
SET venue_name = CASE venue_id
    WHEN 1 THEN '光华楼'
    WHEN 2 THEN '逸夫科技楼'
    WHEN 3 THEN '综合体育馆'
    ELSE venue_name
END,
room_number = CASE venue_id
    WHEN 3 THEN '主馆'
    ELSE room_number
END
WHERE venue_id IN (1, 2, 3);

UPDATE Category
SET category_name = CASE category_id
    WHEN 1 THEN '学术讲座'
    WHEN 2 THEN '比赛竞赛'
    WHEN 3 THEN '文娱活动'
    WHEN 4 THEN '志愿服务'
    ELSE category_name
END
WHERE category_id IN (1, 2, 3, 4);

UPDATE Activity
SET title = '数据库系统项目分享会',
    description = '面向数据库课程项目的小型分享会，介绍活动报名系统的设计与实现。'
WHERE activity_id = 1;
