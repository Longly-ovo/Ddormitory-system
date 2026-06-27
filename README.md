# 智慧宿舍管理系统使用说明书

智慧宿舍管理系统用于管理学校宿舍中的楼栋、楼层、寝室、床位和学生入住信息，支持查看楼层地图、维护宿舍结构、管理学生资料、分配床位、退宿和换床等基础宿舍业务。

本系统基于 Spring Boot 3、Vue 3 和 MySQL 8 开发，核心业务关系为：

```text
楼栋 → 楼层 → 寝室 → 床位 → 学生
```

## 适用对象

- 管理员：维护宿舍结构、管理学生信息、查看和调整床位入住情况。
- 宿管：维护楼栋、楼层、寝室、床位等宿舍结构，并查看楼层地图。
- 辅导员：查看楼层地图和学生信息。
- 学生：查看本人宿舍、寝室和床位信息，并修改初始密码。

## 运行环境

使用本系统前，请先在电脑上安装：

- JDK 17+
- Maven 3.9+
- Node.js 20+
- MySQL 8

## 系统运行步骤

### 1. 创建数据库

先使用具有建库权限的 MySQL 账号执行项目根目录下的 `database/init.sql`，创建数据库：

```powershell
mysql -u root -p -e "SOURCE D:/Project/School/Ddormitory-system/database/init.sql"
```

如果项目路径不同，请把命令里的 SQL 文件路径改成目标电脑上的实际路径。

`database/init.sql` 只负责创建数据库 `dormitory_mvp`，不负责建表。表结构文件位于：

```text
backend/src/main/resources/schema.sql
```

后端启动时会根据 `application.yml` 中的配置自动执行 `schema.sql`：

```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
```

因此推荐流程是：只手动创建数据库，不手动创建表，让后端启动时自动创建表。

后端第一次启动时会自动创建六张表：`user`、`student`、`building`、`floor`、`room`、`bed`。

如果已经手动执行过 `schema.sql` 建表，之后再启动后端通常也不会报错，因为建表语句使用的是 `CREATE TABLE IF NOT EXISTS`。但不要手动修改表结构，否则可能导致后端字段映射、外键或数据初始化失败。

### 2. 启动后端

后端启动必须配置 `JWT_SECRET` 环境变量，否则应用会因为缺少 JWT 密钥而启动失败。请使用一段足够长、不要提交到 Git 仓库的随机字符串：

```powershell
$env:JWT_SECRET="please-change-this-to-a-long-random-secret"
```

默认数据库账号为 `root/123456`。如果目标电脑的 MySQL 用户名或密码不同，请通过环境变量覆盖：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的MySQL密码"
cd backend
mvn spring-boot:run
```

也可以覆盖完整数据库连接地址：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/dormitory_mvp?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
```

后端默认地址为：

```text
http://localhost:8080
```

健康检查地址为：

```text
http://localhost:8080/api/health
```

### 3. 启动前端

另开一个 PowerShell 窗口，进入前端目录：

```powershell
cd frontend
npm install
npm run dev
```

前端启动成功后，浏览器访问：

```text
http://localhost:5173
```

前端开发服务器会把 `/api` 请求代理到后端 `http://localhost:8080`，所以使用系统时需要同时保持后端和前端两个命令窗口都在运行。

## 登录账号

启用演示数据时，系统会自动补齐以下演示账号，不会覆盖同名账号：

| 角色 | 用户名 | 密码 | 登录后可使用功能 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `admin123` | 楼层地图、学生管理、宿舍结构 |
| 宿管 | `dorm` | `dorm123` | 楼层地图、宿舍结构 |
| 辅导员 | `counselor` | `counselor123` | 楼层地图、学生信息 |
| 学生 | `20260001` | `123456` | 我的宿舍、修改密码 |

学生账号用户名必须与学号一致，系统据此查询本人宿舍和床位。管理员新增学生时会自动创建对应学号的学生账号，统一初始密码默认为 `123456`。学生使用初始密码登录时会持续收到修改密码提醒。

首次启动会自动建立一个演示楼栋、一层、8 个寝室、床位及两名学生。生产使用时可设置 `$env:SEED_DEMO_DATA="false"` 关闭业务演示数据；管理员账号仍会在用户表为空时自动创建。

已有数据库启动时会自动为 `user` 表补充 `role` 字段，已有账号默认为 `ADMIN`。部署角色改造后，旧登录令牌需要退出并重新登录。

## 系统使用流程

1. 启动 MySQL，并确认数据库 `dormitory_mvp` 已创建。
2. 启动后端，等待控制台出现 Spring Boot 启动成功信息。
3. 启动前端，浏览器打开 `http://localhost:5173`。
4. 在登录页输入演示账号或已有账号。
5. 根据登录角色进入对应页面：
   - 管理员可维护宿舍结构、管理学生信息、查看和调整床位入住情况。
   - 宿管可维护宿舍结构并查看楼层地图。
   - 辅导员可查看楼层地图和学生信息。
   - 学生可查看本人宿舍和床位信息，并修改初始密码。

## 常见问题

- 后端启动失败时，先检查 MySQL 是否已启动、数据库 `dormitory_mvp` 是否已创建、MySQL 用户名密码是否正确、`JWT_SECRET` 是否已设置。
- 如果提示端口被占用，检查 `8080` 或 `5173` 是否已经被其他程序占用。
- 如果前端页面能打开但接口报错，先确认后端窗口仍在运行，并确认后端地址是 `http://localhost:8080`。
- 不要直接双击 `frontend/index.html` 打开系统，应通过 `npm run dev` 启动前端后访问 `http://localhost:5173`。
- 如果修改了数据库表结构或手动导入了不一致的数据，可能导致后端启动或业务操作失败，建议保持表结构与 `backend/src/main/resources/schema.sql` 一致。

## 构建与测试

```powershell
cd backend
mvn test

cd ../frontend
npm run build
```

前端生产产物位于 `frontend/dist`，后端打包可执行：

```powershell
cd backend
mvn package
```

## 业务规则

- `bed.student_id` 为空表示空床，不为空表示已入住。
- 一个学生最多关联一个床位。
- 换床必须先退宿，再分配新床位。
- 已入住学生不可直接删除。
- 有空床寝室显示蓝色，满员寝室显示绿色。
- 没有配置床位的寝室不显示在楼层地图中。
