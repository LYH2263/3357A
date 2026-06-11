# 教学平台系统-SchoolSystemC02

## 🛠 技术栈
- **Frontend**: Bootstrap 5 + Vanilla JS + Modern CSS
- **Backend**: Spring Boot 3.2 + MyBatis-Plus
- **Database**: MySQL 8.0
- **Infrastructure**: Docker & Docker Compose

## How to Run
1. 确保 Docker Desktop 已启动。
2. 在项目根目录执行：`docker compose up --build`
3. 容器就绪后，访问：[http://localhost:3357](http://localhost:3357)

## Services
- **Frontend UI**: [http://localhost:3357](http://localhost:3357)
- **Backend API**: [http://localhost:8357/api](http://localhost:8357/api)
- **Database**: `localhost:33570` (root/root)

## Verification
1. **登录验证**：使用 `张老师` / `123456` 登录教师后台。
2. **数据管理**：在“教学内容”中上传一个 PDF 文件，并确认可见。
3. **学生审批**：在“学生管理”中找到“小李”，点击“编辑”将其状态改为“已通过”。
4. **互动功能**：使用 `小明` / `123456` 登录学生中心，提交提问，并在教师后台回复，确认双向同步。

## ✨ 核心特性
- **100% 容器化**：一键水平扩展及环境隔离，不依赖宿主机系统组件。
- **双角色权限**：物理隔离教师后台与学生中心，内置自动身份识别。
- **注册审批流**：学生账号必须经过教师批准（Checkedok）后方可生效。
- **真实文件上传**：支持教学课件与实验附件的上传、持久化存储与在线查看。
- **规范化 UI**：符合 Prompt 要求的删除确认模态框及响应式布局。
