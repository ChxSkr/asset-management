# 数字资产管理系统 (Digital Asset Management)

基于 **Spring Boot 3** + **Vue 3** 的全栈数字资产管理系统，支持文件上传/下载、版本管理、分类标签、权限控制、操作日志等功能。

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| ORM | MyBatis-Plus | 3.5.6 |
| 安全认证 | Spring Security + JWT (jjwt 0.12.5) | — |
| 数据库 | MySQL | 8.0+ |
| 构建工具 | Maven | 3.9+ |
| 前端框架 | Vue 3 (Composition API) | 3.5 |
| 状态管理 | Pinia | 3.0 |
| UI 组件库 | Element Plus | 2.14 |
| 构建工具 | Vite | 8.0 |
| 运行环境 | JDK 17 / Node.js 18+ | — |

---

## 功能模块

### 核心功能

- **用户认证** — 注册/登录，JWT Token 认证，BCrypt 密码加密
- **资产管理** — 文件上传、列表筛选、详情查看、编辑、下载
- **版本管理** — 支持同一资产多版本上传，版本历史追溯
- **回收站** — 软删除机制，支持恢复和永久删除
- **分类管理** — 无限级树形分类（增删改查）
- **标签管理** — 标签增删，实时统计关联资产数
- **全局搜索** — 关键词搜索 + 分类/文件类型/标签/日期 组合筛选
- **权限控制** — 管理员/普通用户 角色隔离（JWT拦截器 + 接口级鉴权）
- **操作日志** — 自动记录上传/编辑/删除/恢复等操作，含IP追踪
- **下载日志** — 记录每次文件下载的用户、时间、IP
- **深色模式** — 支持浅色/深色主题一键切换

---

## 项目结构

```
软工实践/
├── asset-management-backend/         # Spring Boot 后端
│   ├── src/main/java/com/dam/
│   │   ├── common/                   # 通用类（Result、全局异常处理）
│   │   ├── config/                   # 配置（Jwt拦截器、安全配置、跨域）
│   │   ├── controller/               # 控制器层（7个Controller）
│   │   ├── dto/                      # 数据传输对象
│   │   ├── entity/                   # 实体类（8张表）
│   │   ├── mapper/                   # MyBatis-Plus Mapper
│   │   ├── service/                  # 业务逻辑层
│   │   └── util/                     # 工具类（JWT、文件、IP）
│   ├── src/main/resources/
│   │   ├── application.yml           # 应用配置
│   │   └── init.sql                  # 数据库初始化脚本
│   ├── pom.xml
│   └── uploads/                      # 文件存储目录
│
├── asset-management-frontend/        # Vue 3 前端
│   ├── src/
│   │   ├── api/                      # API 请求封装（7个模块）
│   │   ├── layouts/                  # 布局组件（MainLayout）
│   │   ├── router/                   # 路由配置
│   │   ├── store/                    # Pinia 状态管理（user、theme）
│   │   ├── styles/                   # 全局样式和主题变量
│   │   └── views/                    # 页面视图（16个页面）
│   ├── vite.config.js
│   └── package.json
│
├── tools/                            # 内置工具
│   ├── jdk17/                        # JDK 17 运行环境
│   └── maven/                        # Maven 构建工具
│
└── start-backend.bat                 # Windows 一键启动脚本
```

---

## 数据库设计

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `user` | 用户表 | username, password(BCrypt), role(ADMIN/USER), status |
| `asset` | 资产表 | asset_name, file_type, file_size, storage_path, is_deleted |
| `category` | 分类表 | category_name, parent_id(树形), sort_order |
| `tag` | 标签表 | tag_name(唯一) |
| `asset_tag` | 资产-标签关联 | asset_id, tag_id (多对多) |
| `asset_version` | 版本表 | asset_id, version_number, storage_path, version_note |
| `download_log` | 下载记录 | asset_id, user_id, ip_address |
| `operation_log` | 操作日志 | user_id, operation_type, description, ip_address |

---

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.9+（项目已内置 tools/maven）

### 1. 初始化数据库

```bash
# 在 MySQL 中执行
source asset-management-backend/src/main/resources/init.sql
```

### 2. 配置数据库连接

编辑 `asset-management-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dam_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

### 3. 启动后端

```bash
# 方式一：使用内置脚本（Windows）
start-backend.bat

# 方式二：手动启动
cd asset-management-backend
mvn spring-boot:run

# 方式三：运行已编译的 jar
java -jar target/asset-management-backend-1.0.0.jar
```

后端默认运行在 `http://localhost:8080`

### 4. 启动前端

```bash
cd asset-management-frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 5. 创建管理员账号

方式一：通过前端注册页面注册第一个用户，然后在数据库中将该用户的 `role` 字段修改为 `ADMIN`。

方式二：直接执行 SQL（需要先生成 BCrypt 密码哈希）：

```sql
INSERT INTO user (username, password, role, real_name, status)
VALUES ('admin', '$2a$10$...', 'ADMIN', '管理员', 1);
```

---

## API 接口一览

### 用户模块 `/api/user`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/register` | 用户注册 | 公开 |
| POST | `/login` | 用户登录 | 公开 |
| GET | `/profile` | 获取个人信息 | 登录 |
| PUT | `/profile` | 修改个人信息 | 登录 |
| POST | `/create` | 管理员创建用户 | 管理员 |
| GET | `/list` | 用户列表 | 管理员 |
| GET | `/{userId}` | 查看用户详情 | 管理员 |
| PUT | `/{userId}` | 修改用户信息 | 管理员 |
| PUT | `/{userId}/password` | 重置用户密码 | 管理员 |

### 资产模块 `/api/asset`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/upload` | 上传资产 | 登录 |
| GET | `/list` | 资产列表（分页+筛选） | 登录 |
| GET | `/{id}` | 资产详情 | 登录 |
| PUT | `/{id}` | 更新资产 | 登录 |
| DELETE | `/{id}` | 软删除（移入回收站） | 登录 |
| GET | `/{id}/download` | 下载资产 | 登录 |
| POST | `/{id}/version` | 上传新版本 | 登录 |
| GET | `/{id}/versions` | 版本历史 | 登录 |
| GET | `/recycle` | 回收站列表 | 登录 |
| PUT | `/{id}/restore` | 恢复资产 | 登录 |
| DELETE | `/{id}/permanent` | 永久删除 | 登录 |

### 其他接口

| 模块 | 路径 | 说明 |
|------|------|------|
| 分类 | `/api/category/tree` | 获取分类树 |
| 分类 | `/api/category` | 增/改/删分类 |
| 标签 | `/api/tag/list` | 标签列表（含关联资产数） |
| 标签 | `/api/tag` | 增/删标签 |
| 搜索 | `/api/search` | 全局搜索 |
| 日志 | `/api/log/operation` | 操作日志（管理员） |
| 日志 | `/api/log/download` | 下载日志（管理员） |

---

## 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1717000000000
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录/Token过期 |
| 403 | 无权限 |

---

## 安全机制

- **密码加密**：BCrypt 哈希，不可逆
- **JWT 认证**：HMAC-SHA256 签名，24小时有效期
- **角色鉴权**：JWT拦截器 + 接口级 `@role` 属性判断
- **参数绑定**：MyBatis-Plus 防 SQL 注入
- **文件校验**：上传时校验文件类型白名单 + 大小限制 (10MB)

---

## 许可证

本项目仅供教育学习使用。
