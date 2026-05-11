# DBPJ-2026 校园活动报名系统

## 1. 项目简介

本项目是数据库系统课程实践项目，目标是实现一个以 MySQL 数据库为核心的校园活动报名 Web 系统。系统支持学生、组织者、管理员三类角色，覆盖活动资源维护、活动发布审核、学生报名、满员候补、取消递补、现场签到、反馈评价、信用分和统计看板等流程。

当前版本的基础业务功能已经基本完善：一期主流程和二期核心能力均已落地，可支持完整演示与联调。后端已按四层模型重构为 `controller / service / model/entity / model/mapper / model/vo`，并引入 MyBatis-Plus 承担实体映射和数据库访问封装。下一阶段重点将从“补齐基础功能”转向“接口契约验证、体验增强、数据运营能力和生产化准备”。

## 2. 功能概览

- 学生端：活动浏览、活动详情、报名、候补、取消报名、我的活动、签到码、活动评价、信用分与信用流水。
- 组织者端：活动创建、草稿编辑、提交审核、活动管理、报名名单、签到核销、缺勤标记、活动反馈看板。
- 管理员端：活动审核、校区/场地/分类维护、基础统计、全站反馈概览、信用风险概览。
- 数据库能力：主外键约束、唯一约束、检查约束、报名人数冗余字段、候补队列、签到状态。
- 工程能力：统一响应、统一异常、角色鉴权、分层目录、MyBatis-Plus 实体和 Mapper、Response VO、核心集成测试、前后端基础构建验证。

## 3. 技术栈

- 前端：Vue 3、Vite、TypeScript、Element Plus、Pinia、Vue Router、Axios
- 后端：Spring Boot 4、Spring WebMVC、MyBatis-Plus、Spring JDBC/HikariCP、Bean Validation
- 数据库：MySQL 8.4.8 LTS
- 部署辅助：Docker Compose

## 4. 目录结构

```text
DBPJ-2026
├── backend
│   ├── activity              # Spring Boot 后端项目
│   └── docs                  # 后端开发文档
├── frontend                  # Vue 前端项目
├── docs                      # 需求、ER 图、数据库设计文档
├── sql
│   ├── schema.sql            # 建表和演示数据初始化脚本
│   ├── phase2_feedback.sql   # 二期评价反馈增量迁移脚本
│   ├── phase2_credit.sql     # 二期信用分增量迁移脚本
│   └── fix_seed_utf8.sql     # 旧容器中文 seed 修复脚本
├── scripts
│   └── smoke-test.ps1        # API smoke test 脚本
├── work_docs                 # 过程文档和接口文档，本地忽略
└── docker-compose.yml        # MySQL Docker 配置
```

后端核心包结构：

```text
com.campus.activity
├── common                    # 统一响应、异常、鉴权上下文、枚举
├── controller                # REST API 入口，只做参数接收和服务调用
├── service                   # 业务规则、权限校验、事务和状态流转
└── model
    ├── dto                   # 请求参数对象
    ├── entity                # 数据库表实体映射
    ├── mapper                # MyBatis-Plus Mapper 与 SQL 封装
    └── vo                    # Response View Objects，稳定接口响应契约
```

当前后端分层状态：

- Controller 保持薄层，只负责参数接收、校验注解和调用 Service。
- Service 承担权限校验、事务边界、业务规则和状态流转。
- Mapper 封装数据库访问，`service/` 中不再直接拼接 SQL 或注入 `JdbcTemplate`。
- Entity 对应核心数据库表，VO 用于接口响应，避免直接向前端暴露数据库实体或松散 Map。

## 5. 环境要求

- JDK 25，或将 `backend/activity/pom.xml` 中的 `java.version` 改为本机 JDK 支持的版本。
- Maven 3.9+
- Node.js 18+
- npm
- Docker Desktop / Docker Engine

当前项目已在以下环境验证：

- Java 25.0.2
- Spring Boot 4.0.6
- MySQL Docker 镜像 `mysql:8.4.8`

## 6. 数据库启动

在项目根目录执行：

```bash
docker compose up -d mysql
```

首次启动会自动执行 `sql/schema.sql`，创建数据库 `campus_activity`、8 张核心表和演示数据。

数据库连接信息：

```text
Host: localhost
Port: 3306
Database: campus_activity
Username: campus
Password: campus123
Root password: root123
```

查看容器状态：

```bash
docker compose ps
```

如果需要完全重建数据库：

```bash
docker compose down -v
docker compose up -d mysql
```

如果旧容器里 seed 数据出现中文乱码，可执行：

```bash
docker cp sql/fix_seed_utf8.sql dbpj-2026-mysql:/tmp/fix_seed_utf8.sql
docker exec dbpj-2026-mysql sh -c "mysql --default-character-set=utf8mb4 -ucampus -pcampus123 -D campus_activity < /tmp/fix_seed_utf8.sql"
```

如果是在已有一期数据库上升级二期评价反馈功能，执行：

```bash
docker cp sql/phase2_feedback.sql dbpj-2026-mysql:/tmp/phase2_feedback.sql
docker exec dbpj-2026-mysql sh -c "mysql --default-character-set=utf8mb4 -ucampus -pcampus123 -D campus_activity < /tmp/phase2_feedback.sql"
```

如果继续升级二期信用分与缺勤处理功能，执行：

```bash
docker cp sql/phase2_credit.sql dbpj-2026-mysql:/tmp/phase2_credit.sql
docker exec dbpj-2026-mysql sh -c "mysql --default-character-set=utf8mb4 -ucampus -pcampus123 -D campus_activity < /tmp/phase2_credit.sql"
```

信用分规则：

- 初始信用分按 100 展示，不直接写入用户表。
- 组织者或管理员在活动结束后标记缺勤，未签到的正选报名会变为 `ABSENT`，信用分 `-10`。
- 学生完成签到时会写入信用流水，信用分 `+1`。
- 所有信用变化都保存在 `CreditRecord`，便于后续做申诉、审计和人工调整。

## 7. 后端运行

进入后端项目目录：

```bash
cd backend/activity
```

打包：

```bash
mvn -DskipTests package
```

运行：

```bash
java -jar target/activity-0.0.1-SNAPSHOT.jar
```

后端默认地址：

```text
http://localhost:8080
```

后端数据库配置位于：

```text
backend/activity/src/main/resources/application.yml
```

其中 JDBC 连接已配置 `utf8mb4` 初始化，避免中文数据乱码。

## 8. 前端运行

进入前端目录：

```bash
cd frontend
```

安装依赖：

```bash
npm install
```

启动开发服务器：

```bash
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

前端通过 Vite 代理访问后端 `/api`，代理配置在 `frontend/vite.config.ts`。

生产构建：

```bash
npm run build
```

## 9. 演示账号

初始化脚本内置 3 个账号，密码均为：

```text
123456
```

| 角色 | 登录名 |
| :--- | :--- |
| 学生 | `20230001` |
| 组织者 | `计算机协会` 或 `13800000002` |
| 管理员 | `系统管理员` 或 `13800000003` |

## 10. 测试与质量验证

项目提供了 PowerShell smoke test，覆盖登录、用户信息、字典、活动、审核、报名、取消、签到和统计接口。

运行前确保：

- MySQL 容器已启动。
- 后端 jar 已打包。
- 8080 或测试脚本指定端口未被占用。

执行：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1 -Port 18080
```

脚本会：

1. 临时启动后端到 `18080`。
2. 调用一期核心 API。
3. 输出每个接口的 `PASS`。
4. 测试结束后关闭临时后端进程。

最近一次验证结果：

```text
ALL API SMOKE TESTS PASSED
```

后端集成测试：

```bash
cd backend/activity
mvn test
```

当前后端测试覆盖：

- Spring Boot 应用上下文启动。
- 报名满员进入候补。
- 正选取消后候补第一位自动转正。
- 签到核销幂等，重复核销不重复写入信用流水。
- 已签到活动评价可重复提交并更新原评价。
- 活动结束后缺勤扣分只写入一次。
- 报名截止后不能报名。
- 学生不能标记缺勤。
- 组织者不能查看非本人活动名单。
- 学生不能取消他人报名。
- 已签到报名不能取消。
- 未签到活动不能提交评价。
- 学生不能生成他人签到码。
- 签到码格式错误会被拒绝。
- 组织者不能修改已发布活动。
- 审核状态非法和审核结果非法会被拒绝。

最近一次后端验证结果：

```text
Tests run: 14, Failures: 0, Errors: 0
BUILD SUCCESS
```

前端构建验证：

```bash
cd frontend
npm.cmd run build
```

## 11. 常见问题

### 11.1 登录时报 `Failed to obtain JDBC Connection`

优先检查 MySQL 容器：

```bash
docker compose ps
```

如果容器未启动：

```bash
docker compose up -d mysql
```

确认后端配置中的数据库账号为：

```text
username: campus
password: campus123
```

### 11.2 PowerShell 无法运行 npm

如果遇到 `npm.ps1 cannot be loaded`，使用：

```bash
npm.cmd install
npm.cmd run dev
```

### 11.3 中文乱码

本项目已在以下位置固定字符集：

- `sql/schema.sql`：`SET NAMES utf8mb4`
- `docker-compose.yml`：MySQL server charset/collation
- `application.yml`：JDBC + Hikari `SET NAMES utf8mb4`

旧容器数据乱码时执行 `sql/fix_seed_utf8.sql`，或重建 Docker volume。

## 12. 文档索引

- [需求文档](./docs/需求文档.md)
- [数据库设计文档](./docs/数据库设计文档.md)
- [ER 图](./docs/ER图.png)
- [前端开发文档](./frontend/docs/前端开发文档.md)
- [后端开发文档](./backend/docs/后端开发文档.md)

`work_docs` 目录用于开发过程文档和接口说明，默认被 `.gitignore` 忽略。

## 13. 下一阶段方向

基础功能完成后，建议优先审核以下方向：

- 工程质量：补充并发、接口级鉴权和 JSON 字段契约测试。
- 用户体验：站内通知、审核结果提醒、候补转正提醒、缺勤扣分提醒。
- 数据运营：反馈关键词优化、低分反馈跟踪、活动复盘导出、统计图表增强。
- 管理能力：审计日志、管理员操作留痕、报名名单 Excel 导出。
- 生产化准备：密码哈希、配置隔离、接口权限测试、部署说明整理。
