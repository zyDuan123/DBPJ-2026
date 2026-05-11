-- 将旧演示库中的明文种子密码迁移为 PBKDF2 哈希。
-- 默认登录密码仍为 123456，仅存储方式从明文升级为哈希。

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

UPDATE User
SET password = 'pbkdf2$120000$ZGJwai0yMDI2LXN0dWRlbnQtc2VlZA==$8Qp3YWW+CCkG53R6RiFjJHtofp7RjSeqMnd2lIiWiNY='
WHERE student_no = '20230001'
  AND password = '123456';

UPDATE User
SET password = 'pbkdf2$120000$ZGJwai0yMDI2LW9yZ2FuaXplci1zZWVk$Viio6kNxospwiFm3piu3fJy2m2fe0A1eDo/IsGFZfI8='
WHERE phone = '13800000002'
  AND password = '123456';

UPDATE User
SET password = 'pbkdf2$120000$ZGJwai0yMDI2LWFkbWluLXNlZWQ=$6aTjSynvefikLgDvcDa2aX2kpo0dnT+guS8//Q6FKFQ='
WHERE phone = '13800000003'
  AND password = '123456';
