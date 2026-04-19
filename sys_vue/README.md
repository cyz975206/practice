# sys 管理系统 — 前端

面向企业级组织的权限与资源管理核心系统，聚焦人员、机构、用户、角色、菜单、数据字典的全生命周期管理，构建完整的 RBAC（基于角色的访问控制）权限体系。

---

## 技术栈

| 类别       | 技术                                        |
| ---------- | ------------------------------------------- |
| 框架       | Vue 3.5 + TypeScript                        |
| 构建工具   | Vite 7                                      |
| UI 组件库  | Element Plus 2                              |
| 状态管理   | Pinia 2                                     |
| 路由       | Vue Router 4（Hash 模式，动态路由加载）     |
| HTTP 请求  | Axios（JWT Bearer Token 认证）              |
| 样式方案   | SCSS + UnoCSS + Element Plus 暗色模式 CSS 变量 |
| 工具库     | @vueuse/core、dayjs、lodash-es              |
| 其他       | nprogress、sortablejs、screenfull           |

---

## 项目结构

```
sys_vue/
├── src/
│   ├── api/                      # API 接口层（对接 Spring Boot 后端）
│   │   ├── types.ts              #   统一响应 R<T>、PageQuery、PageResult
│   │   ├── auth/                 #   登录 / 登出 / 用户信息
│   │   ├── org/                  #   机构 CRUD、树、状态、排序、导入导出
│   │   ├── person/               #   人员 CRUD、导入导出
│   │   ├── user/                 #   用户 CRUD、重置密码、修改密码、解锁、角色分配
│   │   ├── role/                 #   角色 CRUD、菜单权限分配
│   │   ├── menu/                 #   菜单 CRUD、菜单树
│   │   ├── config/               #   系统配置 CRUD
│   │   ├── dict/                 #   数据字典（类型 / 项 / 翻译）
│   │   ├── cache/                #   缓存管理（在线用户 / 统计 / 清理）
│   │   ├── task/                 #   定时任务管理（CRUD / 启用停用 / 手动触发）
│   │   └── log/                  #   日志查询（操作 / 登录 / 系统 / 安全）
│   ├── composables/              # 通用组合式函数（消除 CRUD 样板代码）
│   │   ├── useTable.ts           #   分页表格（数据加载 / 搜索 / 重置）
│   │   ├── useFormDialog.ts      #   新增/编辑弹窗（表单校验 / 提交）
│   │   ├── useConfirmDelete.ts   #   删除确认
│   │   ├── useStatusToggle.ts    #   状态切换（启用/停用）
│   │   ├── useExportImport.ts    #   Excel 导入导出
│   │   └── useDict.ts            #   数据字典翻译（缓存/去重/动态选项）
│   ├── constants/                # 共享常量
│   │   ├── status.ts             #   状态映射（菜单/角色）
│   │   ├── labels.ts             #   枚举标签映射（菜单类型/日志级别）
│   │   └── tagTypes.ts           #   字典项标签样式映射（前端 UI 关注点）
│   ├── assets/
│   │   ├── icons/                # 本地 SVG 图标
│   │   └── images/               # 静态图片资源
│   ├── components/               # 全局组件
│   │   ├── EditPassword/         #   修改密码弹窗
│   │   ├── Hamburger/            #   侧栏折叠按钮
│   │   ├── Icons/                #   图标组件
│   │   ├── Pagination/           #   分页组件
│   │   ├── SvgIcon/              #   SVG 图标封装
│   │   └── System/               #   业务组件
│   │       ├── SysOrgTree.vue        # 机构树
│   │       ├── SysOrgTreeSelect.vue # 机构下拉选择
│   │       └── MenuTree.vue         # 菜单权限树
│   ├── directive/                # 自定义指令
│   │   ├── debounce.ts           #   v-debounce 防抖
│   │   └── permission/           #   v-hasPerm 按钮权限
│   ├── enums/
│   │   ├── http.ts               #   ContentType
│   │   ├── MenuTypeEnum.ts       #   菜单类型（D/M/B）
│   │   └── SizeEnum.ts           #   组件尺寸
│   ├── views/                    # 业务页面
│   │   ├── login/                #   登录页
│   │   ├── org/                  #   机构管理
│   │   ├── person/               #   人员管理
│   │   ├── user/                 #   用户管理
│   │   ├── role/                 #   角色管理
│   │   ├── menu/                 #   菜单管理
│   │   ├── config/               #   系统配置
│   │   ├── dict/                 #   数据字典（类型 + 项）
│   │   ├── cache/                #   缓存管理
│   │   ├── task/                 #   定时任务管理
│   │   ├── log/                  #   日志管理（操作 / 登录 / 系统 / 安全）
│   │   ├── error/                #   401 / 404
│   │   └── redirect/             #   路由重定向
│   ├── lang/                     # 国际化（zh-cn / en）
│   ├── layout/                   #   布局框架
│   │   ├── index.vue             #   布局入口
│   │   ├── main.vue              #   内容区
│   │   └── components/           #   Sidebar / NavBar / TagsView / AppMain / Settings
│   ├── router/index.ts           #   静态路由
│   ├── store/modules/            #   Pinia 状态管理
│   │   ├── app.ts                #   侧栏 / 语言 / 尺寸
│   │   ├── menu.ts               #   全局菜单树
│   │   ├── permission.ts         #   动态路由生成（后端菜单树 → Vue Router）
│   │   ├── settings.ts           #   系统设置
│   │   ├── tagsView.ts           #   多标签页
│   │   └── user.ts               #   登录 / 用户信息 / 登出
│   ├── styles/                   # 全局样式
│   ├── typings/                  # TypeScript 类型声明
│   ├── utils/
│   │   ├── request.ts            #   Axios 封装（JWT Bearer）
│   │   ├── requestHelper.ts      #   HTTP 状态码文案
│   │   └── index.ts              #   通用工具
│   ├── App.vue                   #   根组件
│   ├── main.ts                   #   应用入口
│   ├── permission.ts             #   路由守卫
│   └── settings.ts               #   默认 UI 配置
├── .env.development              # 开发环境变量
├── .env.production               # 生产环境变量
├── index.html                    # HTML 入口
├── package.json
├── vite.config.ts                # Vite 配置（/api 代理 + 自动导入）
├── tsconfig.json
└── uno.config.ts                 # UnoCSS 配置
```

---

## 功能模块

### 1. 认证与登录
- 账号密码登录（JWT Token）
- 登出与会话管理
- 修改密码（右上角头像下拉菜单 → 修改密码，`PUT /api/user/change-password`，需验证旧密码）

### 2. 组织机构管理
- CSS Grid 主从布局（左侧 280px 机构树 + 右侧列表面板）
- 自定义树节点：层级色标徽章（总行红/分行橙/支行蓝/部门紫）、状态指示灯、彩色左边框
- 机构 CRUD、启用/停用（内联确认）
- 右侧无边框现代表格，机构代码等宽字体、层级彩色胶囊、状态指示灯、圆形操作按钮
- 侧滑抽屉表单（按「基本信息 / 组织设置 / 其他设置」分区）
- 机构导入改为侧滑抽屉（拖拽上传区）
- 机构层级通过 `useDict('OrgLevel')` 字典翻译获取标签，样式映射（色标/徽章）保留本地常量
- 通用状态（启用/禁用）通过 `useDict('CommonStatus')` 字典翻译获取标签

### 3. 人员管理
- 人员分页列表，头部集成导出/导入/新增按钮
- 搜索工具栏（姓名、工号、机构树选择、状态筛选）
- 无边框现代表格，姓名加粗、工号/手机号等宽字体、状态指示灯
- 侧滑抽屉表单（按「基本信息 / 任职信息」分区，姓名双列输入）
- 导入改为侧滑抽屉（拖拽上传区）
- 删除内联确认（Popconfirm）
- 人员状态通过 `useDict('PersonStatus')` 字典翻译获取标签

### 4. 用户管理
- 头部统计胶囊 + 搜索工具栏（用户名、昵称、机构树选择、状态筛选）
- 无边框现代表格，用户名加粗 + 状态胶囊合并显示、角色标签、失败次数高亮
- 圆形操作按钮（编辑/分配角色/重置密码/解锁/删除），删除内联确认（Popconfirm）
- 重置密码弹框：支持手动输入或随机生成密码，确认后显示新密码并自动复制到剪贴板
- 侧滑抽屉表单（按「账号信息 / 个人信息」分区，新增时显示密码输入）
- 分配角色改为侧滑抽屉（角色树复选框）
- 用户状态通过 `useDict('UserStatus')` 字典翻译获取标签

### 5. 角色管理
- 角色分页列表，无边框现代表格 + 圆形操作按钮
- 角色 CRUD（侧滑抽屉表单，按「基本信息 / 其他设置」分区）
- 菜单权限分配（600px 侧滑抽屉，菜单树复选框 + 类型色标图标）
- 删除内联确认（Popconfirm）、头部统计胶囊、状态指示灯
- 搜索工具栏集成状态筛选
- 状态通过 `useDict('CommonStatus')` 字典翻译获取标签

### 6. 菜单与权限
- 菜单树形管理（目录 D / 菜单 M / 按钮 B），全宽树 + 侧滑抽屉编辑
- 菜单 CRUD，类型色标（目录紫 / 菜单绿 / 按钮橙）、状态指示灯、悬浮操作按钮
- 头部统计胶囊实时显示各类型数量
- 搜索过滤、删除内联确认（Popconfirm）
- 表单按「基本信息 / 路由配置 / 权限配置 / 显示选项」分区，支持暗色模式
- 状态通过 `useDict('CommonStatus')` 字典翻译获取标签
- 基于后端菜单树动态路由生成
- `v-hasPerm` 按钮级权限控制

### 7. 数据字典
- **主从布局**（index.vue）：左侧字典类型卡片列表 + 右侧字典项表格，CSS Grid 自适应分割
- 类型列表：选中高亮（主题色左边框）、搜索、分页、状态指示灯
- 字典项表格：无边框现代表格、圆形操作按钮（编辑/删除/状态切换）
- 字典类型/字典项独立管理页（type.vue / item.vue）：头部 + 工具栏 + 表格面板 + 侧滑抽屉表单
- 字典项批量排序（侧滑抽屉）、状态切换内联确认
- 字典翻译接口对接
- **`useDict` 组合式函数**：统一管理字典数据的加载、缓存和翻译，各业务页面通过字典类型动态获取下拉选项和标签显示
- 状态通过 `useDict('CommonStatus')` 字典翻译获取标签

### 8. 系统配置
- 系统配置 CRUD、按键查询，无边框现代表格 + 侧滑抽屉表单
- 配置键高亮显示（主题色 code 标签）、配置值等宽字体
- 搜索工具栏双输入框、删除内联确认（Popconfirm）、头部统计胶囊

### 9. 缓存管理
- 仪表盘式缓存统计（4 张彩色边框卡片：字典/菜单/配置/权限，带图标与数值）
- 每张卡片集成 Popconfirm 清理按钮，头部「清理全部缓存」危险操作按钮
- 在线用户管理面板：用户名、昵称、机构、IP、登录时间，强制下线（Popconfirm）
- 在线人数红色胶囊徽章、IP 等字字体标签

### 10. 定时任务管理
- 定时任务分页列表，无边框现代表格 + 圆形操作按钮
- 任务 CRUD（侧滑抽屉表单，按「基本信息 / 调度配置」分区）
- 启用/停用任务（内联确认，Redis pub/sub 多实例同步）
- 手动触发任务（立即异步执行一次）
- 任务名称加粗、服务名称/Cron表达式等宽标签、运行状态指示灯（绿色=运行中/灰色=已停用）
- 搜索工具栏（任务名称、状态筛选）

### 11. 日志管理
- 操作日志：头部标题 + 副标题描述、工具栏（操作人/模块/类型/结果/时间）、无边框表格，操作人加粗、类型胶囊、结果指示灯、错误信息红色等宽字体；操作类型通过 `useDict('OperationType')` 字典翻译
- 登录日志：用户名加粗、登录IP等宽标签、登录类型胶囊、结果指示灯、失败原因红色高亮；登录类型通过 `useDict('LoginType')` 字典翻译
- 系统日志：日志级别彩色徽章（INFO蓝/WARN黄/ERROR红）、服务器IP等宽标签、异常堆栈展开行（红色等宽字体暗色代码区）
- 安全日志：风险等级/处理状态彩色胶囊徽章、操作IP等宽标签、处理改为侧滑抽屉（圆形操作按钮）；风险类型通过 `useDict('RiskType')`、风险等级通过 `useDict('RiskLevel')`、处理状态通过 `useDict('HandleStatus')` 字典翻译

### 12. 布局与通用
- 左侧 / 顶部 / 混合三种布局模式
- 多标签页（TagsView，按用户隔离持久化）
- 主题切换（亮色 / 暗色）
- 全屏、水印
- NProgress 路由进度条

---

## 启动方式

### 环境要求
- **Node.js** ≥ 18
- **pnpm**（推荐）

### 安装依赖

```bash
pnpm install
```

### 开发模式

```bash
pnpm dev
```

访问 `http://localhost:8989`，API 代理到 `http://localhost:8080`。

### 生产构建

```bash
pnpm build:prod
```

---

## 环境变量

| 变量                | 说明                   |
| ------------------- | ---------------------- |
| `VITE_APP_PORT`    | 开发服务器端口         |
| `VITE_APP_BASE_API`| API 请求前缀           |
| `VITE_APP_API_URL` | 后端 API 代理目标地址 |

---

## RBAC 权限流程

```
登录（POST /api/auth/login）
  → 获取 JWT Token & 用户信息
  → 拉取菜单树（GET /api/menu/tree）
  → 菜单树转 Vue Router 路由 → router.addRoute 动态注册
  → 页面渲染时 v-hasPerm 控制按钮级权限
```

---

## 开发说明

### Composables（组合式函数）
项目通过 `src/composables/` 提供通用组合式函数，消除各管理页面的 CRUD 样板代码：
- **`useTable`**: 封装分页表格的数据加载、搜索、重置逻辑，所有页面统一使用 1-based 分页
- **`useFormDialog`**: 封装新增/编辑弹窗的表单校验、提交逻辑
- **`useConfirmDelete`**: 封装删除确认弹窗
- **`useStatusToggle`**: 封装启用/停用状态切换（默认值 `'1'`/`'0'`）
- **`useExportImport`**: 封装 Excel 导入导出
- **`useDict`**: 封装数据字典翻译，提供模块级缓存、请求去重、动态选项列表、标签翻译和标签样式映射

### 字典翻译
所有字典类型使用后端枚举类简单名（PascalCase）作为 dict_type：
- `CommonStatus` — 通用启用/禁用状态
- `OrgLevel` — 机构等级
- `PersonStatus` — 人员状态
- `UserStatus` — 用户账号状态
- `OperationType` — 操作类型
- `LoginType` — 登录类型
- `RiskType` — 安全风险类型
- `RiskLevel` — 安全风险等级
- `HandleStatus` — 安全处理状态

翻译接口返回 `{ code, label, value }` 格式：
- `code` = 后端枚举常量名（如 `ACTIVE`、`HEAD_OFFICE`）
- `value` = 业务表实际存储值（如 `1`、`2`）
- `label` = 显示名（如 `在职`、`总行`）

业务页面通过 `useDict('DictType')` 获取翻译，不硬编码标签文本。

### 共享常量
`src/constants/` 仅保留前端 UI 关注点的映射：
- **`tagTypes.ts`**: 字典项标签样式映射（`COMMON_STATUS_TAG_MAP`、`USER_STATUS_TAG_MAP` 等），标签样式是前端 UI 关注点，不走字典接口
- **`labels.ts`**: 菜单类型、日志级别等非字典枚举的标签映射
- **`status.ts`**: 状态映射工具函数

### 自动导入
项目配置了 `unplugin-auto-import`，以下 API 无需手动导入：
- **Vue**: `ref`、`reactive`、`computed`、`watch`、`onMounted` 等
- **@vueuse/core**: `useSessionStorage`、`useLocalStorage` 等
- **Element Plus**: `ElMessage`、`ElMessageBox` 等（组件也通过 `unplugin-vue-components` 自动注册）

### 工具函数
`src/utils/index.ts` 提供通用工具函数：
- **`formatDate`**: 基于 dayjs 的日期时间格式化，将 ISO 8601 格式（如 `2025-01-01T10:00:00`）转为 `YYYY-MM-DD HH:mm:ss` 显示格式，所有列表页时间列统一使用

### 前后端接口协议
- 后端统一响应格式：`{ code: 200, message: "success", data: ... }`，业务成功码为 `200`
- 前端响应拦截器、登录、获取用户信息、生成路由、加载菜单树均以 `code === 200` 判断成功

### 动态路由与侧边栏
- 登录后仅存储 token，由路由守卫通过 `getUserInfo()` + `generateRoutes()` 完整加载用户信息和动态路由
- 侧边栏 `resolvePath` 对绝对路径（以 `/` 开头）直接使用，不与父路径拼接
