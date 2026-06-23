# 智慧宿舍管理系统 MVP

基于 Spring Boot 3、Vue 3 和 MySQL 8 的最小可运行版本。核心闭环为：

`楼层地图 → 寝室 → 床位 → 学生`

## 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 20+
- MySQL 8

## 1. 初始化数据库

先使用具有建库权限的 MySQL 账号执行：

```powershell
mysql -u root -p -e "SOURCE D:/Project/School/Ddormitory-system/database/init.sql"
```

后端第一次启动时会自动创建且仅创建六张表：`user`、`student`、`building`、`floor`、`room`、`bed`。

## 2. 启动后端

后端启动必须配置 `JWT_SECRET` 环境变量，否则应用会因为缺少 JWT 密钥而启动失败。
请使用一段足够长、不要提交到 Git 仓库的随机字符串：

```powershell
$env:JWT_SECRET="please-change-this-to-a-long-random-secret"
```

默认数据库账号为 `root/123456`。如本机密码不同，通过环境变量覆盖：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的MySQL密码"
cd backend
mvn spring-boot:run
```

也可以覆盖完整连接地址：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/dormitory_mvp?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
```

后端地址为 `http://localhost:8080`，健康检查为 `GET /api/health`。

## 3. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`。

启用演示数据时会补齐以下演示账号（不会覆盖同名账号）：

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin123` |
| 宿管 | `dorm` | `dorm123` |
| 辅导员 | `counselor` | `counselor123` |
| 学生 | `20260001` | `123456` |

学生账号用户名必须与学号一致，系统据此查询本人宿舍和床位。
管理员新增学生时会自动创建同学号的学生账号，统一初始密码默认为 `123456`。学生使用初始密码登录时会持续收到修改密码提醒。

首次启动会自动建立一个演示楼栋、一层、8 个寝室、床位及两名学生。生产使用时可设置 `$env:SEED_DEMO_DATA="false"` 关闭业务演示数据；管理员账号仍会在用户表为空时自动创建。

已有数据库启动时会自动为 `user` 表补充 `role` 字段，已有账号默认为 `ADMIN`。部署角色改造后，旧登录令牌需要退出并重新登录。

## 构建与测试

```powershell
cd backend
mvn test

cd ../frontend
npm run build
```

前端生产产物位于 `frontend/dist`，后端打包可执行 `mvn package`。

## MVP 业务规则

- `bed.student_id` 为空表示空床，不为空表示已入住。
- 一个学生最多关联一个床位。
- 换床必须先退宿，再分配新床位。
- 已入住学生不可直接删除。
- 有空床寝室显示蓝色，满员寝室显示绿色。
- 没有配置床位的寝室不显示在楼层地图中。
