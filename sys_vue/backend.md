# sys 后端 API 接口文档（前端对接用）

## 基础信息

- **Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **认证方式**: JWT Token（Header: `Authorization: Bearer <token>`）
- **Content-Type**: `application/json`（文件上传除外）

---

## 统一响应结构

所有接口返回统一格式 `R<T>`：

```json
{
  "code": 200,       // 状态码：200=成功，400=参数校验失败，500=业务异常
  "message": "success", // 提示信息
  "data": {}          // 业务数据，类型取决于具体接口
}
```

**业务异常**: HTTP 200，body 中 `code` 非 200
**参数校验失败**: HTTP 400，`code` 为 400

**分页响应** (`Page<T>`):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [],      // 当前页数据列表
    "totalElements": 0, // 总记录数
    "totalPages": 0,    // 总页数
    "number": 0,        // 当前页码（从0开始）
    "size": 10          // 每页条数
  }
}
```

> **注意**: 分页查询的 `page` 参数前端传 **1** 表示第一页（后端自动转换为 0），`size` 默认 **10**。

---

## 认证管理 `/api/auth`

### POST `/api/auth/login` — 登录

**免认证**

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**Response** `R<LoginResponse>`:

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOi...",
    "userInfo": {
      "userId": 1,
      "username": "admin",
      "nickname": "管理员",
      "orgCode": "ORG001",
      "roles": ["admin"],
      "perms": ["system:user:list", "system:user:add", "..."]
    }
  }
}
```

---

### POST `/api/auth/logout` — 登出

**需认证**

**Response** `R<Void>`

---

### GET `/api/auth/info` — 获取当前登录用户信息

**需认证**

**Response** `R<LoginUserResponse>`:

```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "admin",
    "nickname": "管理员",
    "orgCode": "ORG001",
    "roles": ["admin"],
    "perms": ["system:user:list", "..."]
  }
}
```

---

## 机构管理 `/api/org`

> **字典字段映射**: 本模块返回数据中，以下字段存储的是字典编码值，前端需通过 `GET /api/dict/translation/{dictType}` 获取对应字典列表，用于**列表展示**（编码翻译为显示名）或**表单选择框**：
> - `orgLevel` → 字典类型 `OrgLevel`（机构等级）

### POST `/api/org` — 创建机构

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orgShortName | String | 是 | 机构简称 |
| orgFullName | String | 是 | 机构全称 |
| orgLevel | String | 是 | 机构等级：`HEAD_OFFICE`/`BRANCH`/`SUB_BRANCH`/`DEPARTMENT` |
| parentOrgCode | String | 否 | 上级机构编码 |
| leaderUserId | Long | 否 | 负责人用户ID |
| status | String | 否 | 状态：`1`-启用 `0`-停用 |
| sort | Integer | 否 | 排序号 |

**Response** `R<OrgResponse>`

> **字典字段**: `orgLevel` → `org_level`

---

### PUT `/api/org/{id}` — 更新机构

**Path 参数**: `id` — 机构ID

**Request Body**: 与创建相同，所有字段均可选

**Response** `R<OrgResponse>`

> **字典字段**: `orgLevel` → `org_level`

---

### DELETE `/api/org/{id}` — 删除机构（逻辑删除）

**Path 参数**: `id` — 机构ID

**Response** `R<Void>`

---

### GET `/api/org/{id}` — 获取机构详情

**Path 参数**: `id` — 机构ID

**Response** `R<OrgResponse>`

> **字典字段**: `orgLevel` → `org_level`

---

### GET `/api/org` — 分页查询机构

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| orgShortName | String | 否 | — | 机构简称（模糊匹配） |
| orgLevel | String | 否 | — | 机构等级 |
| status | String | 否 | — | 状态 |
| parentOrgCode | String | 否 | — | 上级机构编码 |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<OrgResponse>>`

> **字典字段**: `orgLevel` → `org_level` — 列表展示时需翻译，查询条件中的 `orgLevel` 可用字典值填充选择框

---

### GET `/api/org/tree` — 获取机构树形结构

**Response** `R<List<OrgTreeResponse>>`

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "orgCode": "ORG001",
      "orgShortName": "总公司",
      "orgFullName": "XX总公司",
      "orgLevel": "HEAD_OFFICE",
      "parentOrgCode": null,
      "leaderUserId": null,
      "status": "1",
      "sort": 0,
      "children": [
        { "...": "子节点，结构相同" }
      ]
    }
  ]
}
```

> **字典字段**: `orgLevel` → `org_level`

---

### PUT `/api/org/{id}/status` — 启用/停用机构

**Path 参数**: `id` — 机构ID

**Query 参数**: `status` — 目标状态（`1`-启用 `0`-停用）

**Response** `R<Void>`

---

### PUT `/api/org/sort` — 批量调整排序

**Query 参数**: `ids` — 机构ID列表（逗号分隔），`sorts` — 排序号列表（逗号分隔）

**Response** `R<Void>`

---

### GET `/api/org/export` — 导出机构Excel

**Response**: 文件流下载（`Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`）

---

### POST `/api/org/import/excel` — 导入机构Excel

**Content-Type**: `multipart/form-data`

**Form 参数**: `file` — Excel 文件

**Response** `R<ImportResultResponse>`:

```json
{
  "code": 200,
  "data": {
    "totalCount": 100,
    "successCount": 98,
    "failCount": 2,
    "errors": ["第3行：机构简称不能为空", "第7行：机构等级无效"]
  }
}
```

---

## 人员管理 `/api/person`

> **字典字段映射**: 本模块返回数据中，以下字段存储的是字典编码值，前端需通过 `GET /api/dict/translation/{dictType}` 获取对应字典列表，用于**列表展示**（编码翻译为显示名）或**表单选择框**：
> - `status` → 字典类型 `PersonStatus`（人员状态）

### POST `/api/person` — 创建人员

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| surname | String | 是 | 姓 |
| givenName | String | 是 | 名 |
| idCard | String | 否 | 身份证号 |
| phone | String | 否 | 手机号 |
| staffNum | String | 是 | 员工工号 |
| orgCode | String | 是 | 所属机构编码 |
| status | String | 否 | 状态：`ACTIVE`/`RESIGNED`/`RETIRED` |

**Response** `R<PersonResponse>`

> **字典字段**: `status` → `person_status`
>
> **orgName**: 所属机构名称（机构简称），通过 orgCode 关联查询

---

### PUT `/api/person/{id}` — 更新人员

**Path 参数**: `id` — 人员ID

**Request Body**: 与创建相同，所有字段均可选

**Response** `R<PersonResponse>`

> **字典字段**: `status` → `person_status`
>
> **orgName**: 所属机构名称（机构简称），通过 orgCode 关联查询

---

### DELETE `/api/person/{id}` — 删除人员（逻辑删除）

**Path 参数**: `id` — 人员ID

**Response** `R<Void>`

---

### GET `/api/person/{id}` — 获取人员详情

**Path 参数**: `id` — 人员ID

**Response** `R<PersonResponse>`

> **字典字段**: `status` → `person_status`
>
> **orgName**: 所属机构名称（机构简称），通过 orgCode 关联查询

---

### GET `/api/person` — 分页查询人员

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| fullName | String | 否 | — | 姓名（模糊匹配） |
| staffNum | String | 否 | — | 员工工号 |
| orgCode | String | 否 | — | 所属机构编码 |
| status | String | 否 | — | 状态 |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<PersonResponse>>`

> **字典字段**: `status` → `person_status` — 列表展示时需翻译，查询条件中的 `status` 可用字典值填充选择框
>
> **orgName**: 所属机构名称（机构简称），通过 orgCode 关联查询，无机构时为 null

---

### GET `/api/person/export` — 导出人员Excel

**Response**: 文件流下载

---

### POST `/api/person/import/excel` — 导入人员Excel

**Content-Type**: `multipart/form-data`

**Form 参数**: `file` — Excel 文件

**Response** `R<ImportResultResponse>`

---

## 用户管理 `/api/user`

> **字典字段映射**: 本模块返回数据中，以下字段存储的是字典编码值，前端需通过 `GET /api/dict/translation/{dictType}` 获取对应字典列表，用于**列表展示**（编码翻译为显示名）或**表单选择框**：
> - `status` → 字典类型 `UserStatus`（用户账号状态）

### POST `/api/user` — 创建用户

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码（需满足密码强度要求） |
| nickname | String | 否 | 用户昵称 |
| personId | Long | 否 | 关联人员ID |
| orgCode | String | 是 | 所属机构编码 |
| status | String | 否 | 账号状态：`1`-正常 `2`-锁定 `3`-禁用 `4`-注销 |

**Response** `R<UserResponse>`

> **字典字段**: `status` → `user_status`
>
> **orgName**: 所属机构名称（机构简称），通过 orgCode 关联查询

---

### PUT `/api/user/{id}` — 更新用户

**Path 参数**: `id` — 用户ID

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | String | 否 | 用户昵称 |
| personId | Long | 否 | 关联人员ID |
| orgCode | String | 否 | 所属机构编码 |
| status | String | 否 | 账号状态 |

**Response** `R<UserResponse>`

> **字典字段**: `status` → `user_status`
>
> **orgName**: 所属机构名称（机构简称），通过 orgCode 关联查询

---

### DELETE `/api/user/{id}` — 删除用户（逻辑删除，级联删除用户角色关联）

**Path 参数**: `id` — 用户ID

**Response** `R<Void>`

---

### GET `/api/user/{id}` — 获取用户详情（不返回密码）

**Path 参数**: `id` — 用户ID

**Response** `R<UserResponse>`

> **字典字段**: `status` → `user_status`
>
> **orgName**: 所属机构名称（机构简称），通过 orgCode 关联查询

---

### GET `/api/user` — 分页查询用户

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| username | String | 否 | — | 用户名 |
| nickname | String | 否 | — | 用户昵称 |
| orgCode | String | 否 | — | 所属机构编码 |
| status | String | 否 | — | 账号状态 |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<UserResponse>>`

```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "管理员",
        "personId": null,
        "orgCode": "ORG001",
        "orgName": "技术部",
        "status": "1",
        "loginFailCount": 0,
        "lastLoginTime": "2025-01-01T10:00:00",
        "createTime": "2025-01-01T00:00:00",
        "updateTime": "2025-01-01T00:00:00",
        "roleNames": ["超级管理员"],
        "online": true
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

> **字典字段**: `status` → `user_status` — 列表展示时需翻译，查询条件中的 `status` 可用字典值填充选择框
>
> **roleNames**: 用户关联的角色名称列表，无角色时返回空数组
>
> **online**: 是否在线（基于 Redis 会话判断）
>
> **orgName**: 所属机构名称（机构简称），通过 orgCode 关联查询，无机构时为 null

---

### PUT `/api/user/{id}/reset-password` — 重置密码

**Path 参数**: `id` — 用户ID

**Request Body**（可选）:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| newPassword | String | 否 | 新密码（不传则自动生成符合密码强度等级的随机密码） |

**Response** `R<String>`

```json
{
  "code": 200,
  "data": "aB3#xY9zLp"
}
```

> 传了新密码则校验强度后使用，不传则自动生成符合当前密码强度等级要求的随机密码并返回。同时清零登录失败次数并更新密码修改时间

---

### PUT `/api/user/change-password` — 修改密码

**需认证**

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码（需满足密码强度要求） |

**Response** `R<Void>`

> 验证旧密码正确后更新密码，同时清零登录失败次数并更新密码修改时间

---

### PUT `/api/user/{id}/unlock` — 解锁用户

**Path 参数**: `id` — 用户ID

**Response** `R<Void>`

> 恢复正常状态并清零登录失败次数

---

### PUT `/api/user/{id}/roles` — 分配角色（全量替换）

**Path 参数**: `id` — 用户ID

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleIds | Long[] | 是 | 角色ID列表（全量替换，传空数组会清除所有角色） |

**Response** `R<Void>`

---

## 菜单管理 `/api/menu`

### POST `/api/menu` — 创建菜单

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| menuCode | String | 是 | 菜单编码（唯一） |
| menuName | String | 是 | 菜单名称 |
| menuType | String | 是 | 菜单类型：`D`-目录 `M`-菜单 `B`-按钮 |
| parentId | Long | 否 | 父菜单ID |
| path | String | 否 | 路由地址 |
| component | String | 否 | 组件路径 |
| perms | String | 否 | 权限标识（如 `system:user:list`） |
| icon | String | 否 | 图标 |
| isFrame | Boolean | 否 | 是否外链：`true`/`false` |
| sort | Integer | 否 | 排序号 |
| status | String | 否 | 菜单状态：`1`-启用 `0`-禁用 |

**Response** `R<MenuResponse>`

---

### PUT `/api/menu/{id}` — 更新菜单

**Path 参数**: `id` — 菜单ID

**Request Body**: 与创建相同，所有字段均可选

**Response** `R<MenuResponse>`

---

### DELETE `/api/menu/{id}` — 删除菜单（逻辑删除，级联删除角色菜单关联）

**Path 参数**: `id` — 菜单ID

**Response** `R<Void>`

---

### GET `/api/menu/{id}` — 获取菜单详情

**Path 参数**: `id` — 菜单ID

**Response** `R<MenuResponse>`

---

### GET `/api/menu` — 分页查询菜单

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| menuName | String | 否 | — | 菜单名称 |
| menuType | String | 否 | — | 菜单类型：`D`/`M`/`B` |
| status | String | 否 | — | 菜单状态：`1`/`0` |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<MenuResponse>>`

---

### GET `/api/menu/tree` — 获取菜单树形结构

**Response** `R<List<MenuTreeResponse>>`

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "menuCode": "system",
      "menuName": "系统管理",
      "menuType": "D",
      "parentId": null,
      "path": "/system",
      "component": null,
      "perms": null,
      "icon": "system",
      "isFrame": false,
      "sort": 0,
      "status": "1",
      "children": [
        {
          "id": 2,
          "menuCode": "user",
          "menuName": "用户管理",
          "menuType": "M",
          "parentId": 1,
          "path": "/system/user",
          "component": "system/user/index",
          "perms": "system:user:list",
          "icon": "user",
          "isFrame": false,
          "sort": 1,
          "status": "1",
          "children": [
            {
              "id": 3,
              "menuCode": "user:add",
              "menuName": "新增用户",
              "menuType": "B",
              "parentId": 2,
              "path": null,
              "component": null,
              "perms": "system:user:add",
              "icon": null,
              "isFrame": false,
              "sort": 1,
              "status": "1",
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

---

## 角色管理 `/api/role`

### POST `/api/role` — 创建角色

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleCode | String | 是 | 角色编码（唯一） |
| roleName | String | 是 | 角色名称 |
| roleDesc | String | 否 | 角色描述 |
| status | String | 否 | 角色状态：`1`-启用 `0`-禁用 |
| sort | Integer | 否 | 排序号 |

**Response** `R<RoleResponse>`

---

### PUT `/api/role/{id}` — 更新角色

**Path 参数**: `id` — 角色ID

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleName | String | 否 | 角色名称 |
| roleDesc | String | 否 | 角色描述 |
| status | String | 否 | 角色状态 |
| sort | Integer | 否 | 排序号 |

**Response** `R<RoleResponse>`

---

### DELETE `/api/role/{id}` — 删除角色（逻辑删除，级联删除角色菜单和用户角色关联）

**Path 参数**: `id` — 角色ID

**Response** `R<Void>`

---

### GET `/api/role/{id}` — 获取角色详情

**Path 参数**: `id` — 角色ID

**Response** `R<RoleResponse>`

---

### GET `/api/role` — 分页查询角色

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| roleName | String | 否 | — | 角色名称 |
| status | String | 否 | — | 角色状态：`1`/`0` |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<RoleResponse>>`

---

### PUT `/api/role/{id}/menus` — 分配菜单（全量替换）

**Path 参数**: `id` — 角色ID

**Request Body**: `Long[]`（菜单ID数组，全量替换）

```json
[1, 2, 3, 5, 8]
```

**Response** `R<Void>`

---

### GET `/api/role/{id}/menus` — 查询角色已分配的菜单ID列表

**Path 参数**: `id` — 角色ID

**Response** `R<List<Long>>`

```json
{
  "code": 200,
  "data": [1, 2, 3, 5, 8]
}
```

---

## 缓存管理 `/api/cache`

### GET `/api/cache/online-users` — 获取在线用户列表

**Response** `R<List<OnlineUserResponse>>`

```json
{
  "code": 200,
  "data": [
    {
      "userId": 1,
      "username": "admin",
      "nickname": "管理员",
      "orgCode": "ORG001",
      "loginTime": 1713000000000,
      "clientIp": "192.168.1.100"
    }
  ]
}
```

---

### DELETE `/api/cache/online-users/{userId}` — 强制用户下线

**Path 参数**: `userId` — 用户ID

**Response** `R<Void>`

---

### GET `/api/cache/stats` — 获取缓存统计信息

**Response** `R<CacheStatsResponse>`

```json
{
  "code": 200,
  "data": {
    "keyCount": 50,
    "onlineUserCount": 3
  }
}
```

---

### DELETE `/api/cache/clear` — 清理指定缓存

**Query 参数**: `type` — 缓存类型：`dict`/`menu`/`config`/`perms`/`all`

**Response** `R<Void>`

---

## 系统配置 `/api/config`

### POST `/api/config` — 创建系统配置

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| configKey | String | 是 | 配置键（唯一） |
| configName | String | 否 | 配置名称 |
| configValue | String | 否 | 配置值 |
| remark | String | 否 | 备注说明 |
| sort | Integer | 否 | 排序号 |

**Response** `R<ConfigResponse>`

---

### PUT `/api/config/{id}` — 更新系统配置

**Path 参数**: `id` — 配置ID

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| configName | String | 否 | 配置名称 |
| configValue | String | 否 | 配置值 |
| remark | String | 否 | 备注说明 |
| sort | Integer | 否 | 排序号 |

**Response** `R<ConfigResponse>`

---

### DELETE `/api/config/{id}` — 删除系统配置（逻辑删除 + 清除缓存）

**Path 参数**: `id` — 配置ID

**Response** `R<Void>`

---

### GET `/api/config/{id}` — 查询系统配置详情

**Path 参数**: `id` — 配置ID

**Response** `R<ConfigResponse>`

---

### GET `/api/config` — 分页查询系统配置

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| configKey | String | 否 | — | 配置键 |
| configName | String | 否 | — | 配置名称 |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<ConfigResponse>>`

---

### GET `/api/config/key/{configKey}` — 按配置键查询配置值

**Path 参数**: `configKey` — 配置键

**Response** `R<String>`

```json
{
  "code": 200,
  "data": "20"
}
```

---

## 数据字典管理 `/api/dict`

### 字典类型管理

#### POST `/api/dict/type` — 创建字典类型

**Request Body**:

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| dictType | String | 是 | 最大64字符 | 字典类型编码 |
| dictName | String | 是 | 最大64字符 | 字典类型名称 |
| status | String | 否 | — | 状态：`1`-启用 `0`-禁用 |
| remark | String | 否 | 最大255字符 | 备注 |

**Response** `R<DictTypeResponse>`

---

#### PUT `/api/dict/type/{id}` — 更新字典类型

**Path 参数**: `id` — 字典类型ID

**Request Body**:

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| dictName | String | 否 | 最大64字符 | 字典类型名称 |
| status | String | 否 | — | 状态 |
| remark | String | 否 | 最大255字符 | 备注 |

**Response** `R<DictTypeResponse>`

---

#### DELETE `/api/dict/type/{id}` — 删除字典类型（级联软删除所有字典项）

**Path 参数**: `id` — 字典类型ID

**Response** `R<Void>`

---

#### GET `/api/dict/type/{id}` — 查询字典类型详情（含字典项数量）

**Path 参数**: `id` — 字典类型ID

**Response** `R<DictTypeResponse>`

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "dictType": "PersonStatus",
    "dictName": "人员状态",
    "status": "1",
    "remark": null,
    "itemCount": 3,
    "createTime": "2025-01-01T00:00:00",
    "updateTime": "2025-01-01T00:00:00"
  }
}
```

---

#### GET `/api/dict/type` — 分页查询字典类型

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| dictType | String | 否 | — | 字典类型编码（模糊匹配） |
| dictName | String | 否 | — | 字典类型名称（模糊匹配） |
| status | String | 否 | — | 状态 |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<DictTypeResponse>>`

---

#### PUT `/api/dict/type/{id}/status` — 启用/禁用字典类型

**Path 参数**: `id` — 字典类型ID

**Query 参数**: `status` — 目标状态（`1`-启用 `0`-禁用）

**Response** `R<Void>`

---

### 字典项管理

#### POST `/api/dict/item` — 创建字典项

**Request Body**:

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| dictType | String | 是 | — | 字典类型编码 |
| dictCode | String | 是 | 最大64字符 | 字典项编码 |
| dictLabel | String | 是 | 最大64字符 | 字典项显示名 |
| dictValue | String | 否 | 最大255字符 | 字典项值 |
| sort | Integer | 否 | — | 排序号 |
| status | String | 否 | — | 状态 |
| remark | String | 否 | 最大255字符 | 备注 |

**Response** `R<DictItemResponse>`

---

#### PUT `/api/dict/item/{id}` — 更新字典项

**Path 参数**: `id` — 字典项ID

**Request Body**:

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| dictCode | String | 否 | 最大64字符 | 字典项编码 |
| dictLabel | String | 否 | 最大64字符 | 字典项显示名 |
| dictValue | String | 否 | 最大255字符 | 字典项值 |
| sort | Integer | 否 | — | 排序号 |
| status | String | 否 | — | 状态 |
| remark | String | 否 | 最大255字符 | 备注 |

**Response** `R<DictItemResponse>`

---

#### DELETE `/api/dict/item/{id}` — 删除字典项（逻辑删除）

**Path 参数**: `id` — 字典项ID

**Response** `R<Void>`

---

#### GET `/api/dict/item/{id}` — 查询字典项详情

**Path 参数**: `id` — 字典项ID

**Response** `R<DictItemResponse>`

```json
{
  "code": 200,
  "data": {
    "id": 10,
    "dictType": "PersonStatus",
    "dictCode": "ACTIVE",
    "dictLabel": "在职",
    "dictValue": "1",
    "sort": 1,
    "status": "1",
    "remark": null,
    "createTime": "2025-01-01T00:00:00",
    "updateTime": "2025-01-01T00:00:00"
  }
}
```

---

#### GET `/api/dict/item` — 分页查询字典项

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| dictType | String | **是** | — | 字典类型编码 |
| status | String | 否 | — | 状态 |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<DictItemResponse>>`

---

#### PUT `/api/dict/item/{id}/status` — 启用/禁用字典项

**Path 参数**: `id` — 字典项ID

**Query 参数**: `status` — 目标状态（`1`-启用 `0`-禁用）

**Response** `R<Void>`

---

#### PUT `/api/dict/item/sort` — 批量调整字典项排序

**Query 参数**: `ids` — 字典项ID列表（逗号分隔），`sorts` — 排序号列表（逗号分隔）

**Response** `R<Void>`

---

### 字典翻译

#### GET `/api/dict/translation/{dictType}` — 获取类型下所有翻译项

**Path 参数**: `dictType` — 字典类型编码

**Response** `R<DictTranslationResponse>`

```json
{
  "code": 200,
  "data": {
    "dictType": "PersonStatus",
    "translations": [
      { "code": "ACTIVE", "label": "在职", "value": "1" },
      { "code": "RESIGNED", "label": "离职", "value": "2" },
      { "code": "RETIRED", "label": "退休", "value": "3" }
    ]
  }
}
```

---

#### GET `/api/dict/translation/{dictType}/code/{dictCode}` — 编码翻译为显示名

**Path 参数**: `dictType` — 字典类型编码，`dictCode` — 字典项编码

**Response** `R<String>`

```json
{
  "code": 200,
  "data": "在职"
}
```

---

#### GET `/api/dict/translation/{dictType}/value/{dictValue}` — 值翻译为显示名

**Path 参数**: `dictType` — 字典类型编码，`dictValue` — 字典项值

**Response** `R<String>`

---

## 操作日志 `/api/log/operation`

> **字典字段映射**: 本模块返回数据中，以下字段存储的是字典编码值，前端需通过 `GET /api/dict/translation/{dictType}` 获取对应字典列表，用于**列表展示**（编码翻译为显示名）或**表单选择框**：
> - `operationType` → 字典类型 `OperationType`（操作类型）

### GET `/api/log/operation` — 分页查询操作日志

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| operatorName | String | 否 | — | 操作人姓名 |
| module | String | 否 | — | 操作模块 |
| operationType | String | 否 | — | 操作类型：`1`/`2`/`3`/`4`/`5`/`6` |
| result | String | 否 | — | 操作结果：`success`/`fail` |
| startTime | String | 否 | — | 开始时间（格式：`yyyy-MM-dd HH:mm:ss`） |
| endTime | String | 否 | — | 结束时间（格式：`yyyy-MM-dd HH:mm:ss`） |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<OperationLogResponse>>`

> **字典字段**: `operationType` → `operation_type` — 列表展示时需翻译，查询条件中的 `operationType` 可用字典值填充选择框

```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "operatorId": 1,
        "operatorName": "admin",
        "operatorIp": "192.168.1.100",
        "operatorOrgCode": "ORG001",
        "operatorOrg": "总公司",
        "module": "用户管理",
        "operationType": "1",
        "operationContent": "创建用户",
        "requestParams": "{\"username\":\"zhangsan\"}",
        "operationResult": "success",
        "errorMsg": null,
        "operationTime": "2025-01-01T10:00:00",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

---

## 登录日志 `/api/log/login`

> **字典字段映射**: 本模块返回数据中，以下字段存储的是字典编码值，前端需通过 `GET /api/dict/translation/{dictType}` 获取对应字典列表，用于**列表展示**（编码翻译为显示名）或**表单选择框**：
> - `loginType` → 字典类型 `LoginType`（登录类型）

### GET `/api/log/login` — 分页查询登录日志

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| username | String | 否 | — | 用户名 |
| loginType | String | 否 | — | 登录类型：`1`/`2`/`3`/`4` |
| loginResult | String | 否 | — | 登录结果：`success`/`fail` |
| startTime | String | 否 | — | 开始时间（格式：`yyyy-MM-dd HH:mm:ss`） |
| endTime | String | 否 | — | 结束时间（格式：`yyyy-MM-dd HH:mm:ss`） |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<LoginLogResponse>>`

> **字典字段**: `loginType` → `login_type` — 列表展示时需翻译，查询条件中的 `loginType` 可用字典值填充选择框

```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 1,
        "username": "admin",
        "loginIp": "192.168.1.100",
        "loginDevice": "Mozilla/5.0...",
        "loginType": "1",
        "loginResult": "success",
        "failReason": null,
        "loginTime": "2025-01-01T10:00:00",
        "logoutTime": null,
        "isAbnormal": false,
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

---

## 系统日志 `/api/log/system`

### GET `/api/log/system` — 分页查询系统日志

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| logLevel | String | 否 | — | 日志级别：`info`/`warn`/`error` |
| logModule | String | 否 | — | 日志模块 |
| startTime | String | 否 | — | 开始时间（格式：`yyyy-MM-dd HH:mm:ss`） |
| endTime | String | 否 | — | 结束时间（格式：`yyyy-MM-dd HH:mm:ss`） |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<SystemLogResponse>>`

```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "logLevel": "error",
        "logModule": "AuthService",
        "logContent": "用户登录异常",
        "exceptionStack": "java.lang.RuntimeException: ...",
        "occurTime": "2025-01-01T10:00:00",
        "serverIp": "10.0.0.1",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

---

## 安全日志 `/api/log/security`

> **字典字段映射**: 本模块返回数据中，以下字段存储的是字典编码值，前端需通过 `GET /api/dict/translation/{dictType}` 获取对应字典列表，用于**列表展示**（编码翻译为显示名）或**表单选择框**：
> - `riskType` → 字典类型 `RiskType`（安全风险类型）
> - `riskLevel` → 字典类型 `RiskLevel`（安全风险等级）
> - `handleStatus` → 字典类型 `HandleStatus`（安全处理状态）

### GET `/api/log/security` — 分页查询安全日志

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| riskType | String | 否 | — | 风险类型：`1`/`2`/`3` |
| riskLevel | String | 否 | — | 风险等级：`1`/`2`/`3` |
| handleStatus | String | 否 | — | 处理状态：`1`/`2`/`3` |
| startTime | String | 否 | — | 开始时间（格式：`yyyy-MM-dd HH:mm:ss`） |
| endTime | String | 否 | — | 结束时间（格式：`yyyy-MM-dd HH:mm:ss`） |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<SecurityLogResponse>>`

> **字典字段**: `riskType` → `risk_type`，`riskLevel` → `risk_level`，`handleStatus` → `handle_status` — 列表展示时需翻译，查询条件中的 `riskType`、`riskLevel`、`handleStatus` 可用字典值填充选择框

```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "operatorId": null,
        "operatorName": "unknown",
        "operatorIp": "192.168.1.200",
        "riskType": "1",
        "riskContent": "认证失败：无效Token",
        "requestUrl": "/api/user",
        "riskLevel": "2",
        "handleStatus": "1",
        "handleUserId": null,
        "handleTime": null,
        "handleNote": null,
        "occurTime": "2025-01-01T10:00:00",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

---

### PUT `/api/log/security/{id}/handle` — 处理安全日志

**Path 参数**: `id` — 安全日志ID

**Query 参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| handleStatus | String | 是 | 处理状态：`2`/`3` |
| handleNote | String | 否 | 处理备注 |

**Response** `R<Void>`

---

## 定时任务管理 `/api/task`

### POST `/api/task` — 创建定时任务

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 任务名称 |
| serviceName | String | 是 | 服务名称（标识所属服务实例） |
| funPath | String | 是 | 方法路径（格式: beanName.methodName） |
| cron | String | 是 | Cron表达式（如: `0 0 2 * * ?`） |
| hasStart | Boolean | 否 | 是否启用，默认 `false` |

**Response** `R<TaskResponse>`

---

### PUT `/api/task/{id}` — 更新定时任务

**Path 参数**: `id` — 任务ID

**Request Body**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 任务名称 |
| funPath | String | 否 | 方法路径 |
| cron | String | 否 | Cron表达式 |
| hasStart | Boolean | 否 | 是否启用 |

**Response** `R<TaskResponse>`

> 更新后通过 Redis pub/sub 通知所有实例刷新调度器（含本实例）

---

### DELETE `/api/task/{id}` — 删除定时任务（逻辑删除）

**Path 参数**: `id` — 任务ID

**Response** `R<Void>`

---

### GET `/api/task/{id}` — 查询定时任务详情

**Path 参数**: `id` — 任务ID

**Response** `R<TaskResponse>`

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "日志归档任务",
    "serviceName": "sys-management",
    "funPath": "logArchiveTask.archiveLogs",
    "cron": "0 0 2 * * ?",
    "hasStart": true,
    "createTime": "2025-01-01T00:00:00",
    "updateTime": "2025-01-01T00:00:00"
  }
}
```

---

### GET `/api/task` — 分页查询定时任务

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| name | String | 否 | — | 任务名称（模糊匹配） |
| hasStart | Boolean | 否 | — | 是否启用 |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<TaskResponse>>`

---

### PUT `/api/task/{id}/start` — 启用定时任务

**Path 参数**: `id` — 任务ID

**Response** `R<Void>`

> 启用后通过 Redis pub/sub 通知所有实例注册该任务

---

### PUT `/api/task/{id}/stop` — 停用定时任务

**Path 参数**: `id` — 任务ID

**Response** `R<Void>`

> 停止后通过 Redis pub/sub 通知所有实例移除该任务

---

### POST `/api/task/{id}/trigger` — 手动触发定时任务

**Path 参数**: `id` — 任务ID

**Response** `R<Void>`

> 通过 Redis pub/sub 发布 `trigger:{funPath}` 消息通知所有服务实例，各实例收到后从缓存查找任务并执行（分布式锁保证只执行一次），不等待结果返回

---

## 任务日志 `/api/log/task`

### GET `/api/log/task` — 分页查询任务日志

**Query 参数**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| taskName | String | 否 | — | 任务名称（模糊匹配） |
| funPath | String | 否 | — | 方法路径 |
| runResult | Integer | 否 | — | 运行结果：`1`-成功 `0`-失败 |
| startTime | String | 否 | — | 开始时间（格式：`yyyy-MM-dd HH:mm:ss`） |
| endTime | String | 否 | — | 结束时间（格式：`yyyy-MM-dd HH:mm:ss`） |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**Response** `R<Page<TaskLogResponse>>`

```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "taskId": 1,
        "taskName": "日志归档任务",
        "serviceName": "sys",
        "funPath": "logArchiveTask.archiveLogs",
        "cron": "0 0 2 * * ?",
        "runResult": 1,
        "runLog": "开始时间: 2025-01-01 02:00:00\n执行详情: 归档天数: 操作=60, 登录=60, 安全=60, 系统=60 | 归档数量: 操作=10, 登录=5, 系统=3, 安全=1\n结束时间: 2025-01-01 02:00:05\n执行耗时: 5123ms",
        "durationMs": 5123,
        "createTime": "2025-01-01T02:00:05"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

> 任务日志为定时任务执行时自动记录，包含开始时间、执行详情（任务方法返回的自定义内容）、结束时间和执行耗时。通过 Redis 队列异步写入数据库。

---

## 系统初始化数据

系统首次启动时，`SystemConfigInitializer` 自动初始化以下数据（已存在则跳过）。

### 默认系统配置（13 项）

| config_key | 默认值 | 说明 |
|-----------|--------|------|
| `default_admin_password` | `888888` | 初始化管理员默认密码 |
| `login_fail_max_count` | `5` | 登录失败最大次数（达到后自动锁定账号） |
| `password_expire_days` | `0` | 密码有效期天数（0=无期限，登录时校验） |
| `password_strength_level` | `1` | 密码强度等级（1-低≥6位 2-中≥8位含字母数字 3-高≥10位含大小写数字特殊字符） |
| `session_timeout_minutes` | `20` | JWT token与会话统一过期时间（分钟，0或未配置=默认20分钟） |
| `operation_log_retention_days` | `60` | 操作日志归档天数 |
| `login_log_retention_days` | `60` | 登录日志归档天数 |
| `security_log_retention_days` | `60` | 安全日志归档天数 |
| `system_log_retention_days` | `60` | 系统日志归档天数 |
| `jwt_whitelist` | `/api/auth/login,/swagger-ui/**,/swagger-ui.html,/v3/api-docs/**,/favicon.ico,/error` | JWT认证白名单（免认证路径，逗号分隔，支持Ant通配符） |
| `cache_expire_hour` | `1` | 权限缓存过期小时数 |
| `null_cache_minute` | `10` | 空值缓存分钟数 |
| `cache_random_offset_minute` | `10` | 缓存随机偏移分钟（防缓存雪崩） |

### 默认数据字典（9 个字典类型 + 字典项）

> `dict_code` 对应后端枚举常量名（`EnumConstant.name()`），`dict_value` 是业务表中实际存储的值，`dict_type` = 枚举类简单名（`EnumClass.getSimpleName()`）。所有枚举类均为裸常量（仅常量名，无 value/code/desc 字段），业务代码通过 `DictService.getDictValue(dictType, dictCode)` 获取 dict_value，不硬编码。

#### 通用状态 `CommonStatus`

| dict_code | dict_label | dict_value |
|-----------|------------|------------|
| `ENABLED` | 启用 | `1` |
| `DISABLED` | 禁用 | `0` |

#### 机构等级 `OrgLevel`

| dict_code | dict_label | dict_value |
|-----------|------------|------------|
| `HEAD_OFFICE` | 总行 | `1` |
| `BRANCH` | 分行 | `2` |
| `SUB_BRANCH` | 支行 | `3` |
| `DEPARTMENT` | 营业部 | `4` |

#### 人员状态 `PersonStatus`

| dict_code | dict_label | dict_value |
|-----------|------------|------------|
| `ACTIVE` | 正常 | `1` |
| `RESIGNED` | 离职 | `2` |
| `RETIRED` | 退休 | `3` |

#### 用户状态 `UserStatus`

| dict_code | dict_label | dict_value |
|-----------|------------|------------|
| `NORMAL` | 正常 | `1` |
| `LOCKED` | 锁定 | `2` |
| `DISABLED` | 禁用 | `3` |
| `CANCELLED` | 注销 | `4` |

#### 操作类型 `OperationType`

| dict_code | dict_label | dict_value |
|-----------|------------|------------|
| `ADD` | 新增 | `1` |
| `EDIT` | 编辑 | `2` |
| `DELETE` | 删除 | `3` |
| `IMPORT` | 导入 | `4` |
| `EXPORT` | 导出 | `5` |
| `CHANGE_STATUS` | 状态变更 | `6` |

#### 登录类型 `LoginType`

| dict_code | dict_label | dict_value |
|-----------|------------|------------|
| `LOGIN` | 登录 | `1` |
| `LOGOUT` | 登出 | `2` |
| `RESET_PWD` | 重置密码 | `3` |
| `UNLOCK` | 解锁 | `4` |

#### 安全风险类型 `RiskType`

| dict_code | dict_label | dict_value |
|-----------|------------|------------|
| `AUTH_FAIL` | 认证失败 | `1` |
| `SESSION_EXPIRED` | 会话过期 | `2` |
| `SS_CONFLICT` | 账号冲突 | `3` |

#### 安全风险等级 `RiskLevel`

| dict_code | dict_label | dict_value |
|-----------|------------|------------|
| `LOW` | 低 | `1` |
| `MEDIUM` | 中 | `2` |
| `HIGH` | 高 | `3` |

#### 安全处理状态 `HandleStatus`

| dict_code | dict_label | dict_value |
|-----------|------------|------------|
| `UNHANDLED` | 未处理 | `1` |
| `HANDLED` | 已处理 | `2` |
| `IGNORED` | 已忽略 | `3` |

### 默认菜单（17 个）

系统首次启动且菜单表为空时初始化，菜单树结构如下：

```
组织机构 (D) /org
├── 机构管理 (M) /org/list         → component: org/index         perms: system:org:list
└── 人员管理 (M) /person/list      → component: person/index      perms: system:person:list

系统管理 (D) /system
├── 用户管理 (M) /user/list         → component: user/index         perms: system:user:list
├── 角色管理 (M) /role/list         → component: role/index         perms: system:role:list
├── 菜单管理 (M) /menu/list         → component: menu/index         perms: system:menu:list
├── 系统配置 (M) /config/list       → component: config/index       perms: system:config:list
├── 数据字典 (M) /dict/list         → component: dict/index         perms: system:dict:list
├── 缓存管理 (M) /cache/list        → component: cache/index        perms: system:cache:list
└── 定时任务 (M) /task/list         → component: task/index         perms: system:task:list

日志管理 (D) /log
├── 操作日志 (M) /log/operation     → component: log/operation      perms: system:log:operation
├── 登录日志 (M) /log/login         → component: log/login          perms: system:log:login
├── 系统日志 (M) /log/system        → component: log/system         perms: system:log:system
├── 安全日志 (M) /log/security      → component: log/security       perms: system:log:security
└── 任务日志 (M) /log/task          → component: log/task            perms: system:log:task
```

> 前端路由组件路径（`component` 字段）需与实际前端组件文件路径对应。

### 超级管理员角色与 admin 用户

| 项目 | 值 |
|------|------|
| 角色编码 | `super_admin` |
| 角色名称 | 超级管理员 |
| 角色描述 | 拥有系统全部菜单权限的超级管理员角色 |
| 管理员用户名 | `admin` |
| 默认密码 | `888888`（取自配置 `default_admin_password`） |
| 权限范围 | 自动分配所有菜单权限 |

> 初始化逻辑：仅在对应表为空时执行，已有数据不会覆盖。

---

## 枚举值速查

> 以下枚举类均为裸常量（仅常量名，无 value/code/desc 字段）。枚举常量名 = 字典 `dict_code`，业务表实际存储的是 `dict_value`。业务代码通过 `DictService.getDictValue(枚举类.getSimpleName(), 枚举常量.name())` 获取 dict_value，不硬编码。前端建议通过字典翻译接口获取显示名。

### 机构等级 `OrgLevel`

| 枚举值 | 说明 | 业务表存储值 (dict_value) |
|--------|------|--------------------------|
| `HEAD_OFFICE` | 总行 | `1` |
| `BRANCH` | 分行 | `2` |
| `SUB_BRANCH` | 支行 | `3` |
| `DEPARTMENT` | 营业部 | `4` |

### 通用状态 `CommonStatus`

| 枚举值 | 说明 | 业务表存储值 |
|--------|------|-------------|
| `ENABLED` | 启用 | `1` |
| `DISABLED` | 禁用 | `0` |

> 通用状态枚举，适用于机构、字典、菜单、角色等模块的启用/禁用状态，各业务表 status 字段统一使用 `1`/`0`。

### 机构状态 `OrgStatus`

| 枚举值 | 说明 | 业务表存储值 |
|--------|------|-------------|
| `ENABLED` | 启用 | `1` |
| `DISABLED` | 停用 | `0` |

### 人员状态 `PersonStatus`

| 枚举值 | 说明 | 业务表存储值 |
|--------|------|-------------|
| `ACTIVE` | 正常 | `1` |
| `RESIGNED` | 离职 | `2` |
| `RETIRED` | 退休 | `3` |

### 用户账号状态 `UserStatus`

| 枚举值 | 说明 | 业务表存储值 |
|--------|------|-------------|
| `NORMAL` | 正常 | `1` |
| `LOCKED` | 锁定 | `2` |
| `DISABLED` | 禁用 | `3` |
| `CANCELLED` | 注销 | `4` |

### 菜单类型 `MenuType`

| 枚举值 | 说明 | 业务表存储值 |
|--------|------|-------------|
| `DIRECTORY` | 目录 | `D` |
| `MENU` | 菜单 | `M` |
| `BUTTON` | 按钮 | `B` |

### 菜单状态 `MenuStatus` / 角色状态 `RoleStatus`

| 枚举值 | 说明 | 业务表存储值 |
|--------|------|-------------|
| `ENABLED` | 启用 | `1` |
| `DISABLED` | 禁用 | `0` |

### 操作类型 `OperationType`

| 枚举值 | 说明 | 业务表存储值 |
|--------|------|-------------|
| `ADD` | 新增 | `1` |
| `EDIT` | 编辑 | `2` |
| `DELETE` | 删除 | `3` |
| `IMPORT` | 导入 | `4` |
| `EXPORT` | 导出 | `5` |
| `CHANGE_STATUS` | 状态变更 | `6` |

### 登录类型 `LoginType`

| 枚举值 | 说明 | 业务表存储值 |
|--------|------|-------------|
| `LOGIN` | 登录 | `1` |
| `LOGOUT` | 登出 | `2` |
| `RESET_PWD` | 重置密码 | `3` |
| `UNLOCK` | 解锁 | `4` |

### 安全风险类型 `RiskType`

| 枚举值 | 说明 | 业务表存储值 |
|--------|------|-------------|
| `AUTH_FAIL` | 认证失败 | `1` |
| `SESSION_EXPIRED` | 会话过期 | `2` |
| `SS_CONFLICT` | 账号冲突（同账号异地登录） | `3` |

### 安全风险等级 `RiskLevel`

| 枚举值 | 说明 | 业务表存储值 |
|--------|------|-------------|
| `LOW` | 低 | `1` |
| `MEDIUM` | 中 | `2` |
| `HIGH` | 高 | `3` |

### 安全处理状态 `HandleStatus`

| 枚举值 | 说明 | 业务表存储值 |
|--------|------|-------------|
| `UNHANDLED` | 未处理 | `1` |
| `HANDLED` | 已处理 | `2` |
| `IGNORED` | 已忽略 | `3` |

---

## 前端开发注意事项

1. **JWT Token**: 登录成功后将 `token` 存储到 localStorage 或 sessionStorage，后续所有请求在 Header 中携带 `Authorization: Bearer <token>`
2. **Token 过期**: 默认 20 分钟过期，过期后需重新登录（会返回认证失败响应）
3. **单点登录**: 同一账号仅允许一个活跃会话，新登录会踢出旧会话
4. **分页**: 所有分页接口 `page` 从 **1** 开始，响应中 `number` 从 **0** 开始（Spring Data 规范）
5. **时间格式**: 日期时间字段统一使用 `yyyy-MM-ddTHH:mm:ss` 格式（ISO 8601）
6. **删除操作**: 所有删除均为逻辑删除，不会物理删除数据
7. **角色/菜单分配**: `PUT /api/user/{id}/roles` 和 `PUT /api/role/{id}/menus` 均为**全量替换**模式
8. **字典翻译**: 建议前端在应用启动时加载常用字典翻译并缓存，通过 `GET /api/dict/translation/{dictType}` 获取
9. **密码强度**: 创建用户、重置密码和修改密码时后端会校验密码强度（级别由系统配置 `password_strength_level` 决定），前端可提前校验以提升体验
10. **文件上传**: 导入接口使用 `multipart/form-data`，字段名为 `file`
11. **字典字段处理**: 各模块返回数据中标注了 `> **字典字段**` 的接口，其对应字段存储的是字典编码值（非显示名），前端应：
    - **列表展示**: 通过 `GET /api/dict/translation/{dictType}` 获取字典列表，将编码翻译为显示名
    - **表单选择框**: 通过同一接口获取字典列表填充下拉选项，提交时传字典编码值
    - **查询条件**: 筛选条件中的字典字段同样使用字典值填充选择框
