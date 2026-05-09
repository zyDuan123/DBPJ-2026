# DBPJ-2026 校园活动报名系统

## 1. 项目简介

本项目是数据库系统课程实践项目，目标是实现一个以 MySQL 数据库为核心的校园活动报名 Web 系统。系统支持学生、组织者、管理员三类角色，覆盖活动资源维护、活动发布审核、学生报名、满员候补、取消递补、现场签到和基础统计等一期核心流程。

当前版本为“课程可落地版”：优先保证主流程可运行、可联调、可演示。信用分、活动评价、站内通知、审计日志、Excel 导出等能力作为二期扩展预留。

## 2. 功能概览

- 学生端：活动浏览、活动详情、报名、候补、取消报名、我的活动、签到码。
- 组织者端：活动创建、草稿编辑、提交审核、活动管理、报名名单、签到核销。
- 管理员端：活动审核、校区/场地/分类维护、基础统计。
- 数据库能力：主外键约束、唯一约束、检查约束、报名人数冗余字段、候补队列、签到状态。

## 3. 技术栈

- 前端：Vue 3、Vite、TypeScript、Element Plus、Pinia、Vue Router、Axios
- 后端：Spring Boot 4、Spring WebMVC、Spring JDBC、Bean Validation
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
│   └── fix_seed_utf8.sql     # 旧容器中文 seed 修复脚本
├── scripts
│   └── smoke-test.ps1        # API smoke test 脚本
├── work_docs                 # 过程文档和接口文档，本地忽略
└── docker-compose.yml        # MySQL Docker 配置
```

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

首次启动会自动执行 `sql/schema.sql`，创建数据库 `campus_activity`、6 张核心表和演示数据。

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

## 10. API 测试

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
