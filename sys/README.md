# sys 管理系统 - 后端

面向企业级组织的权限与资源管理核心系统，聚焦人员、机构、用户、角色、菜单、数据字典的全生命周期管理，构建完整的 RBAC 权限体系，支持 JWT 认证、Redis 缓存管理和日志审计。

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 17 |
| SpringBoot | 3.3.13 |
| Spring Data JPA | 3.3.13 |
| Spring Boot Validation | 3.3.13 |
| MySQL | 8 |
| BouncyCastle (SM4) | 1.78 |
| Apache POI | 5.3.0 |
| Pinyin4j | 2.5.0 |
| SpringDoc OpenAPI | 2.6.0 |
| Lombok | 1.18.38 |
| Redisson | 3.27.2 |
| JJWT | 0.12.6 |
| Spring AOP | 3.3.13 |

## 项目结构

```
sys/
├── pom.xml
├── src/main/java/com/cyz/
│   ├── SysApplication.java              # 启动类（@EnableScheduling + @EnableAsync）
│   ├── config/
│   │   ├── Sm4DataSourceConfig.java     # SM4解密数据源配置
│   │   ├── Sm4RedisConfig.java          # SM4解密Redis连接配置（Redisson）
│   │   ├── Sm4Properties.java           # SM4密钥配置属性（@ConfigurationProperties）
│   │   ├── RedisConfig.java             # RedisTemplate序列化配置
│   │   ├── JwtProperties.java           # JWT配置属性（仅secret，过期时间从系统配置动态读取）
│   │   ├── TaskProperties.java          # 定时任务配置属性（task.service_name）
│   │   ├── SchedulerConfig.java         # ThreadPoolTaskScheduler配置
│   │   ├── DynamicTaskScheduler.java    # 动态定时任务调度引擎（反射+分布式锁+任务日志上下文）
│   │   ├── TaskRedisListener.java       # Redis pub/sub任务变更监听（区分refresh/trigger消息）
│   │   ├── JpaConfig.java               # JPA审计配置
│   │   ├── WebMvcConfig.java            # CORS + JWT过滤器注册（白名单从系统配置动态读取）
│   │   └── SystemConfigInitializer.java  # 系统初始化（默认配置+字典+菜单+超级管理员+定时任务）
│   ├── common/
│   │   ├── base/BaseEntity.java         # 基础父表 (@MappedSuperclass)
│   │   ├── constant/                    # 常量 (CacheConstant)
│   │   ├── context/                     # 上下文 (TaskLogContext)
│   │   ├── enums/                       # 枚举 (CommonStatus, OperationType, OrgLevel, OrgStatus, PersonStatus, UserStatus, MenuType, MenuStatus, RoleStatus, LoginType, RiskType, RiskLevel, HandleStatus)
│   │   ├── annotation/                  # 自定义注解 (@OperationLog)
│   │   ├── aspect/                      # AOP切面 (OperationLogAspect)
│   │   ├── exception/                   # 异常处理 (BizException, GlobalExceptionHandler)
│   │   ├── result/R.java                # 统一响应包装 R<T>
│   │   └── util/
│   │       ├── Sm4Util.java             # SM4 ECB/PKCS7Padding 加解密
│   │       ├── Md5Util.java             # MD5 加密
│   │       ├── PinyinUtil.java          # 中文转拼音
│   │       ├── IpUtil.java              # 客户端IP获取
│   │       └── ExcelUtil.java           # 通用Excel读写
│   ├── security/                        # JWT认证
│   │   ├── JwtTokenProvider.java        # JWT token生成/解析/校验（过期时间从session_timeout_minutes动态读取）
│   │   ├── JwtAuthenticationFilter.java # JWT请求过滤器（动态白名单 + Redis缓存）
│   │   ├── LoginUser.java               # 登录用户信息
│   │   └── LoginUserContext.java        # ThreadLocal当前用户
│   ├── entity/
│   │   ├── SysOrg.java                  # 机构实体
│   │   ├── SysPerson.java               # 人员实体
│   │   ├── SysDict.java                 # 数据字典实体
│   │   ├── SysUser.java                 # 用户实体
│   │   ├── SysMenu.java                 # 菜单实体
│   │   ├── SysRole.java                 # 角色实体
│   │   ├── SysUserRole.java             # 用户角色关联实体
│   │   ├── SysRoleMenu.java             # 角色菜单关联实体
│   │   ├── SysConfig.java               # 系统配置实体
│   │   ├── SysOperationLog.java         # 操作日志实体
│   │   ├── SysOperationLogArchive.java  # 操作日志归档实体
│   │   ├── SysLoginLog.java             # 登录日志实体
│   │   ├── SysLoginLogArchive.java      # 登录日志归档实体
│   │   ├── SysSystemLog.java            # 系统日志实体
│   │   ├── SysSystemLogArchive.java     # 系统日志归档实体
│   │   ├── SysSecurityLog.java          # 安全日志实体
│   │   └── SysSecurityLogArchive.java   # 安全日志归档实体
│   │   └── SysTask.java                 # 定时任务实体
│   │   └── SysTaskLog.java              # 定时任务日志实体
│   ├── repository/
│   │   ├── SysOrgRepository.java
│   │   ├── SysPersonRepository.java
│   │   ├── SysDictRepository.java
│   │   ├── SysUserRepository.java
│   │   ├── SysMenuRepository.java
│   │   ├── SysRoleRepository.java
│   │   ├── SysUserRoleRepository.java
│   │   ├── SysRoleMenuRepository.java
│   │   ├── SysConfigRepository.java
│   │   ├── SysOperationLogRepository.java
│   │   ├── SysOperationLogArchiveRepository.java
│   │   ├── SysLoginLogRepository.java
│   │   ├── SysLoginLogArchiveRepository.java
│   │   ├── SysSystemLogRepository.java
│   │   ├── SysSystemLogArchiveRepository.java
│   │   ├── SysSecurityLogRepository.java
│   │   └── SysSecurityLogArchiveRepository.java
│   │   └── SysTaskRepository.java
│   │   └── SysTaskLogRepository.java
│   ├── service/
│   │   ├── SysOrgService.java
│   │   ├── SysPersonService.java
│   │   ├── DictService.java             # 数据字典服务
│   │   ├── SysUserService.java          # 用户服务
│   │   ├── SysMenuService.java          # 菜单服务
│   │   ├── SysRoleService.java          # 角色服务
│   │   ├── AuthService.java             # 认证服务
│   │   ├── CacheService.java            # 缓存管理服务
│   │   ├── ConfigService.java           # 系统配置服务
│   │   ├── OperationLogService.java     # 操作日志服务
│   │   ├── LoginLogService.java         # 登录日志服务
│   │   ├── SystemLogService.java        # 系统日志服务
│   │   ├── SecurityLogService.java      # 安全日志服务
│   │   └── SysTaskService.java          # 定时任务服务
│   │   └── SysTaskLogService.java       # 定时任务日志服务
│   │   └── impl/
│   │       ├── SysOrgServiceImpl.java
│   │       ├── SysPersonServiceImpl.java
│   │       ├── DictServiceImpl.java
│   │       ├── SysUserServiceImpl.java
│   │       ├── SysMenuServiceImpl.java
│   │       ├── SysRoleServiceImpl.java
│   │       ├── AuthServiceImpl.java
│   │       ├── CacheServiceImpl.java
│   │       ├── ConfigServiceImpl.java
│   │       ├── OperationLogServiceImpl.java
│   │       ├── LoginLogServiceImpl.java
│   │       ├── SystemLogServiceImpl.java
│   │       └── SecurityLogServiceImpl.java
│   │       └── SysTaskServiceImpl.java
│   │       └── SysTaskLogServiceImpl.java
│   ├── controller/
│   │   ├── SysOrgController.java
│   │   ├── SysPersonController.java
│   │   ├── DictController.java          # 数据字典接口
│   │   ├── SysUserController.java       # 用户管理接口
│   │   ├── SysMenuController.java       # 菜单管理接口
│   │   ├── SysRoleController.java       # 角色管理接口
│   │   ├── AuthController.java          # 认证接口
│   │   ├── CacheController.java         # 缓存管理接口
│   │   ├── ConfigController.java        # 系统配置接口
│   │   ├── OperationLogController.java  # 操作日志接口
│   │   ├── LoginLogController.java      # 登录日志接口
│   │   ├── SystemLogController.java     # 系统日志接口
│   │   └── SecurityLogController.java   # 安全日志接口
│   │   └── SysTaskController.java       # 定时任务管理接口
│   │   └── SysTaskLogController.java    # 定时任务日志接口
│   ├── dto/
│   │   ├── request/                     # Org*/Person*/Dict*/User*/Menu*/Role*/Config*/Login*Request ...
│   │   └── response/                    # Org*/Person*/Dict*/User*/Menu*/Role*/Config*/Login*/Cache*Response ...
│   ├── task/
│   │   ├── LogArchiveTask.java          # 日志归档定时任务
│   │   ├── helper/TaskLogHelper.java    # 任务日志函数式包装方法
│   │   └── consumer/TaskLogConsumer.java # Redis队列消费者（BRPOP守护线程）
│   └── converter/
│       ├── OrgConverter.java
│       ├── PersonConverter.java
│       ├── DictConverter.java           # 字典转换器
│       ├── UserConverter.java           # 用户转换器（含密码强度校验）
│       ├── MenuConverter.java           # 菜单转换器
│       ├── RoleConverter.java           # 角色转换器
│       ├── ConfigConverter.java         # 系统配置转换器
│       ├── OrgExcelRowMapper.java
│       └── PersonExcelRowMapper.java
└── src/main/resources/
    └── application.yml                  # 配置文件 (DB凭据SM4加密存储)
```

## 功能模块

### 机构管理 (`/api/org`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/org` | 创建机构 |
| PUT | `/api/org/{id}` | 更新机构 |
| DELETE | `/api/org/{id}` | 删除机构（逻辑删除） |
| GET | `/api/org/{id}` | 获取机构详情 |
| GET | `/api/org` | 分页查询机构（支持 orgShortName/orgLevel/status/parentOrgCode 筛选） |
| GET | `/api/org/tree` | 获取机构树形结构 |
| PUT | `/api/org/{id}/status` | 启用/停用机构（参数: `status`） |
| PUT | `/api/org/sort` | 批量调整排序（参数: `ids`, `sorts`） |
| GET | `/api/org/export` | 导出机构Excel |
| POST | `/api/org/import/excel` | 导入机构Excel文件 |

### 人员管理 (`/api/person`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/person` | 创建人员 |
| PUT | `/api/person/{id}` | 更新人员 |
| DELETE | `/api/person/{id}` | 删除人员（逻辑删除） |
| GET | `/api/person/{id}` | 获取人员详情（返回所属机构名称） |
| GET | `/api/person` | 分页查询人员（支持 fullName/staffNum/orgCode/status 筛选，返回所属机构名称） |
| GET | `/api/person/export` | 导出人员Excel |
| POST | `/api/person/import/excel` | 导入人员Excel文件 |

### 用户管理 (`/api/user`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user` | 创建用户（密码 MD5 加密存储，校验密码强度） |
| PUT | `/api/user/{id}` | 更新用户 |
| DELETE | `/api/user/{id}` | 删除用户（逻辑删除，级联删除用户角色关联） |
| GET | `/api/user/{id}` | 获取用户详情（不返回密码，返回所属机构名称） |
| GET | `/api/user` | 分页查询用户（支持 username/nickname/orgCode/status 筛选，返回角色名称、在线状态和所属机构名称） |
| PUT | `/api/user/{id}/reset-password` | 重置密码（可选传新密码，不传则自动生成符合密码强度等级的随机密码并返回，同时清零登录失败次数并更新密码修改时间） |
| PUT | `/api/user/change-password` | 修改密码（需认证，验证旧密码，校验新密码强度，同时清零登录失败次数并更新密码修改时间） |
| PUT | `/api/user/{id}/unlock` | 解锁用户（恢复正常状态并清零登录失败次数） |
| PUT | `/api/user/{id}/roles` | 分配角色（全量替换模式） |

### 菜单管理 (`/api/menu`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/menu` | 创建菜单（目录/菜单/按钮），自动清除菜单树缓存 |
| PUT | `/api/menu/{id}` | 更新菜单，自动清除菜单树缓存 |
| DELETE | `/api/menu/{id}` | 删除菜单（逻辑删除，级联删除角色菜单关联），自动清除菜单树缓存 |
| GET | `/api/menu/{id}` | 获取菜单详情 |
| GET | `/api/menu` | 分页查询菜单（支持 menuName/menuType/status 筛选） |
| GET | `/api/menu/tree` | 获取菜单树形结构（带 Redis 缓存） |

### 角色管理 (`/api/role`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/role` | 创建角色 |
| PUT | `/api/role/{id}` | 更新角色 |
| DELETE | `/api/role/{id}` | 删除角色（逻辑删除，级联删除角色菜单和用户角色关联） |
| GET | `/api/role/{id}` | 获取角色详情 |
| GET | `/api/role` | 分页查询角色（支持 roleName/status 筛选） |
| PUT | `/api/role/{id}/menus` | 分配菜单（全量替换模式） |
| GET | `/api/role/{id}/menus` | 查询角色已分配的菜单ID列表 |

### 认证管理 (`/api/auth`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录（返回 JWT token + 用户信息，记录登录IP/设备信息，校验密码过期） |
| POST | `/api/auth/logout` | 登出（清除 Redis 在线会话，记录登出日志） |
| GET | `/api/auth/info` | 获取当前登录用户信息（含角色、权限标识） |

### 缓存管理 (`/api/cache`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/cache/online-users` | 获取在线用户列表 |
| DELETE | `/api/cache/online-users/{userId}` | 强制用户下线 |
| GET | `/api/cache/stats` | 获取缓存统计信息 |
| DELETE | `/api/cache/clear` | 清理指定缓存（参数: `type` = dict/menu/config/perms/all），清理 config 类型时同时刷新 JWT 白名单缓存 |

### 系统配置 (`/api/config`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/config` | 创建系统配置 |
| PUT | `/api/config/{id}` | 更新系统配置 |
| DELETE | `/api/config/{id}` | 删除系统配置（逻辑删除 + 清除 Redis 缓存） |
| GET | `/api/config/{id}` | 查询系统配置详情 |
| GET | `/api/config` | 分页查询系统配置（支持 configKey/configName 筛选） |
| GET | `/api/config/key/{configKey}` | 按配置键查询配置值（带 Redis 缓存 + 动态 TTL） |

### 操作日志 (`/api/log/operation`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/log/operation` | 分页查询操作日志（支持 operatorName/module/operationType/result/startTime/endTime 筛选） |

自动记录：通过 `@OperationLog` 注解 + AOP 切面自动捕获 Controller 层的增删改操作，异步写入数据库。

### 登录日志 (`/api/log/login`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/log/login` | 分页查询登录日志（支持 username/loginType/loginResult/startTime/endTime 筛选） |

自动记录：在 AuthService 中登录成功/失败、登出时自动记录，包含登录IP、设备信息和异常标记。

### 系统日志 (`/api/log/system`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/log/system` | 分页查询系统日志（支持 logLevel/logModule/startTime/endTime 筛选） |

自动记录：全局异常处理器捕获未预期的系统异常时自动写入，也可通过注入 `SystemLogService` 在业务代码中主动调用。

### 安全日志 (`/api/log/security`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/log/security` | 分页查询安全日志（支持 riskType/riskLevel/handleStatus/startTime/endTime 筛选） |
| PUT | `/api/log/security/{id}/handle` | 处理安全日志（标记已处理/忽略，参数: `handleStatus`, `handleNote`） |

自动记录：在 JwtAuthenticationFilter 中认证失败/会话过期/账号冲突时自动记录。

### 定时任务管理 (`/api/task`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/task` | 创建定时任务 |
| PUT | `/api/task/{id}` | 更新定时任务 |
| DELETE | `/api/task/{id}` | 删除定时任务（逻辑删除） |
| GET | `/api/task/{id}` | 查询定时任务详情 |
| GET | `/api/task` | 分页查询定时任务（支持 name/hasStart 筛选） |
| PUT | `/api/task/{id}/start` | 启用定时任务（通过 Redis pub/sub 通知所有实例注册） |
| PUT | `/api/task/{id}/stop` | 停用定时任务（通过 Redis pub/sub 通知所有实例移除） |
| POST | `/api/task/{id}/trigger` | 手动触发定时任务（通过 Redis pub/sub 通知所有实例，分布式锁保证只执行一次） |

定时任务通过 `ThreadPoolTaskScheduler` + 反射动态调度，变更统一通过 Redis pub/sub 通知所有实例（含本实例）刷新调度器，使用 Redisson 分布式锁保证并发安全。

### 任务日志 (`/api/log/task`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/log/task` | 分页查询任务日志（支持 taskName/funPath/runResult/startTime/endTime 筛选） |

自动记录：定时任务执行时自动记录运行日志。任务方法通过 `TaskLogHelper.execute()` 包裹执行逻辑，自动记录开始时间、执行详情、结束时间和耗时。日志通过 Redis List 队列异步写入数据库，BRPOP 消费者守护线程负责消费和持久化，分布式环境下每条日志仅被一个实例消费。未使用 `TaskLogHelper` 的任务也会自动记录基础日志（执行结果和耗时）。

### 字典类型管理 (`/api/dict/type`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/dict/type` | 创建字典类型 |
| PUT | `/api/dict/type/{id}` | 更新字典类型（自动清除翻译缓存） |
| DELETE | `/api/dict/type/{id}` | 删除字典类型（级联软删除所有字典项，清除翻译缓存） |
| GET | `/api/dict/type/{id}` | 获取字典类型详情（含字典项数量） |
| GET | `/api/dict/type` | 分页查询字典类型（支持 dictType/dictName/status 筛选） |
| PUT | `/api/dict/type/{id}/status` | 启用/禁用字典类型（清除翻译缓存） |

### 字典项管理 (`/api/dict/item`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/dict/item` | 创建字典项（清除翻译缓存） |
| PUT | `/api/dict/item/{id}` | 更新字典项（清除翻译缓存） |
| DELETE | `/api/dict/item/{id}` | 删除字典项（逻辑删除，清除翻译缓存） |
| GET | `/api/dict/item/{id}` | 获取字典项详情 |
| GET | `/api/dict/item` | 分页查询字典项（dictType 必传，支持 status 筛选） |
| PUT | `/api/dict/item/{id}/status` | 启用/禁用字典项（清除翻译缓存） |
| PUT | `/api/dict/item/sort` | 批量调整排序（参数: `ids`, `sorts`，清除翻译缓存） |

### 字典翻译 (`/api/dict/translation`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/dict/translation/{dictType}` | 获取类型下所有 code-label-value 翻译项（带 Redis 缓存） |
| GET | `/api/dict/translation/{dictType}/code/{dictCode}` | 单个编码翻译为显示名 |
| GET | `/api/dict/translation/{dictType}/value/{dictValue}` | 单个值翻译为显示名 |

## 数据库表

### sys_org（机构表）

| 字段 | 类型 | 说明                   |
|------|------|----------------------|
| id | bigint | 主键                   |
| org_code | varchar(64) | 机构号（唯一）              |
| org_short_name | varchar(64) | 机构简称                 |
| org_full_name | varchar(255) | 机构全称                 |
| org_level | varchar(32) | 机构等级（数据字典 OrgLevel） |
| parent_org_code | varchar(64) | 上级机构号                |
| leader_user_id | bigint | 机构负责人用户ID            |
| status | varchar(32) | 状态（1/0） |
| sort | int | 排序                   |
| is_deleted | tinyint(1) | 逻辑删除                 |
| create_by / update_by | bigint | 创建人/修改人              |
| create_time / update_time | datetime | 创建时间/修改时间            |

普通索引：`idx_org_parent_org_code (parent_org_code)`

### sys_person（人员表）

| 字段 | 类型 | 说明           |
|------|------|--------------|
| id | bigint | 主键           |
| surname / given_name | varchar(32) | 姓/名          |
| surname_pinyin / given_name_pinyin | varchar(64) | 姓拼音/名拼音      |
| full_name / full_name_pinyin | varchar(64/128) | 姓名/姓名拼音      |
| id_card | varchar(256) | 身份证号码（SM4加密） |
| phone | varchar(256) | 手机号码（SM4加密）  |
| staff_num | varchar(32) | 员工工号（唯一）     |
| org_code | varchar(64) | 所属机构号        |
| status | varchar(32) | 状态（数据字典 PersonStatus）     |
| is_deleted | tinyint(1) | 逻辑删除         |
| create_by / update_by | bigint | 创建人/修改人      |
| create_time / update_time | datetime | 创建时间/修改时间    |

普通索引：`idx_person_org_code (org_code)`, `idx_person_status (status)`, `idx_person_id_card (id_card)`

### sys_user（用户表）

| 字段 | 类型 | 说明         |
|------|------|------------|
| id | bigint | 主键         |
| username | varchar(64) | 用户名（唯一）    |
| password | varchar(255) | 密码（MD5加密）  |
| nickname | varchar(64) | 用户昵称       |
| person_id | bigint | 关联人员ID     |
| org_code | varchar(64) | 所属机构号      |
| status | varchar(32) | 账号状态（数据字典 UserStatus） |
| login_fail_count | int | 登录失败次数     |
| last_login_time | datetime | 最后登录时间     |
| password_update_time | datetime | 密码最后修改时间   |
| is_deleted | tinyint(1) | 逻辑删除       |
| create_by / update_by | bigint | 创建人/修改人    |
| create_time / update_time | datetime | 创建时间/修改时间  |

普通索引：`idx_user_org_code (org_code)`, `idx_user_status (status)`

系统首次启动且用户表为空时自动初始化 admin 超级管理员用户：

| 字段 | 初始值 |
|------|--------|
| username | admin |
| password | MD5(default_admin_password 配置值，默认 888888) |
| nickname | 超级管理员 |
| status | 1（正常） |
| login_fail_count | 0 |

初始化时自动绑定超级管理员角色（super_admin）。

### sys_menu（菜单表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| menu_code | varchar(64) | 菜单编码（唯一） |
| menu_name | varchar(64) | 菜单名称 |
| menu_type | varchar(32) | 菜单类型（D-目录 M-菜单 B-按钮） |
| parent_id | bigint | 父菜单ID |
| path | varchar(255) | 路由地址 |
| component | varchar(255) | 组件路径 |
| perms | varchar(255) | 权限标识 |
| icon | varchar(128) | 图标 |
| is_frame | tinyint(1) | 是否外链（0-否 1-是） |
| sort | int | 排序 |
| status | varchar(32) | 菜单状态（1-启用 0-禁用） |
| is_deleted | tinyint(1) | 逻辑删除 |
| create_by / update_by | bigint | 创建人/修改人 |
| create_time / update_time | datetime | 创建时间/修改时间 |

普通索引：`idx_menu_parent_id (parent_id)`

系统首次启动且菜单表为空时自动初始化的默认菜单（17 个）：

| menu_code | menu_name | menu_type | path | component | perms | sort |
|-----------|-----------|-----------|------|-----------|-------|------|
| org | 组织机构 | D | /org | | | 1 |
| org:list | 机构管理 | M | /org/list | org/index | system:org:list | 1 |
| person:list | 人员管理 | M | /person/list | person/index | system:person:list | 2 |
| system | 系统管理 | D | /system | | | 2 |
| user:list | 用户管理 | M | /user/list | user/index | system:user:list | 1 |
| role:list | 角色管理 | M | /role/list | role/index | system:role:list | 2 |
| menu:list | 菜单管理 | M | /menu/list | menu/index | system:menu:list | 3 |
| config:list | 系统配置 | M | /config/list | config/index | system:config:list | 4 |
| dict:list | 数据字典 | M | /dict/list | dict/index | system:dict:list | 5 |
| cache:list | 缓存管理 | M | /cache/list | cache/index | system:cache:list | 6 |
| task:list | 定时任务 | M | /task/list | task/index | system:task:list | 7 |
| log | 日志管理 | D | /log | | | 3 |
| log:operation | 操作日志 | M | /log/operation | log/operation | system:log:operation | 1 |
| log:login | 登录日志 | M | /log/login | log/login | system:log:login | 2 |
| log:system | 系统日志 | M | /log/system | log/system | system:log:system | 3 |
| log:security | 安全日志 | M | /log/security | log/security | system:log:security | 4 |
| log:task | 任务日志 | M | /log/task | log/task | system:log:task | 5 |

### sys_role（角色表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| role_code | varchar(64) | 角色编码（唯一） |
| role_name | varchar(64) | 角色名称 |
| role_desc | varchar(255) | 角色描述 |
| status | varchar(32) | 角色状态（1-启用 0-禁用） |
| sort | int | 排序 |
| is_deleted | tinyint(1) | 逻辑删除 |
| create_by / update_by | bigint | 创建人/修改人 |
| create_time / update_time | datetime | 创建时间/修改时间 |

系统首次启动且角色表为空时自动初始化超级管理员角色：

| 字段 | 初始值 |
|------|--------|
| role_code | super_admin |
| role_name | 超级管理员 |
| role_desc | 拥有系统全部菜单权限的超级管理员角色 |
| status | 1（启用） |
| sort | 0 |

初始化时自动分配全部菜单权限。

### sys_user_role（用户角色关联表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| role_id | bigint | 角色ID |
| is_deleted | tinyint(1) | 逻辑删除 |
| create_by / update_by | bigint | 创建人/修改人 |
| create_time / update_time | datetime | 创建时间/修改时间 |

唯一约束：`uk_user_role (user_id, role_id)`

普通索引：`idx_user_role_role_id (role_id)`

### sys_role_menu（角色菜单权限表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| role_id | bigint | 角色ID |
| menu_id | bigint | 菜单ID |
| is_deleted | tinyint(1) | 逻辑删除 |
| create_by / update_by | bigint | 创建人/修改人 |
| create_time / update_time | datetime | 创建时间/修改时间 |

唯一约束：`uk_role_menu (role_id, menu_id)`

普通索引：`idx_role_menu_menu_id (menu_id)`

### sys_config（系统配置表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| config_key | varchar(64) | 配置键（唯一） |
| config_name | varchar(64) | 配置名称 |
| config_value | varchar(255) | 配置值 |
| remark | varchar(255) | 备注说明 |
| sort | int | 排序 |
| is_deleted | tinyint(1) | 逻辑删除 |
| create_by / update_by | bigint | 创建人/修改人 |
| create_time / update_time | datetime | 创建时间/修改时间 |

系统启动时自动初始化的默认配置项：

| config_key | 默认值 | 说明 |
|-----------|--------|------|
| default_admin_password | 888888 | 初始化管理员默认密码 |
| login_fail_max_count | 5 | 登录失败最大次数（达到后自动锁定账号） |
| password_expire_days | 0 | 密码有效期天数（0=无期限，登录时校验） |
| password_strength_level | 1 | 密码强度等级（1-低≥6位 2-中≥8位含字母数字 3-高≥10位含大小写数字特殊字符） |
| session_timeout_minutes | 20 | JWT token与会话统一过期时间（分钟，0或未配置=默认20分钟） |
| jwt_whitelist | /api/auth/login,/swagger-ui/**,/swagger-ui.html,/v3/api-docs/**,/favicon.ico,/error | JWT认证白名单，免认证路径，逗号分隔，支持Ant通配符 |
| operation_log_retention_days | 60 | 操作日志归档天数 |
| login_log_retention_days | 60 | 登录日志归档天数 |
| security_log_retention_days | 60 | 安全日志归档天数 |
| system_log_retention_days | 60 | 系统日志归档天数 |
| cache_expire_hour | 1 | 权限缓存过期小时数 |
| null_cache_minute | 10 | 空值缓存分钟数 |
| cache_random_offset_minute | 10 | 缓存随机偏移分钟（防缓存雪崩） |

### sys_dict（数据字典表）

单表设计，`dict_code IS NULL` 为字典类型行，`dict_code IS NOT NULL` 为字典项行。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| dict_type | varchar(64) | 字典类型编码 |
| dict_name | varchar(64) | 字典类型名称 |
| dict_code | varchar(64) | 字典项编码（类型行为 NULL） |
| dict_label | varchar(64) | 字典项显示名（类型行为 NULL） |
| dict_value | varchar(255) | 字典项值（类型行为 NULL） |
| sort | int | 排序 |
| status | varchar(32) | 状态（1-启用 0-禁用） |
| remark | varchar(255) | 备注 |
| is_deleted | tinyint(1) | 逻辑删除 |
| create_by / update_by | bigint | 创建人/修改人 |
| create_time / update_time | datetime | 创建时间/修改时间 |

唯一约束：`uk_dict_type_code (dict_type, dict_code)`

普通索引：`idx_dict_dict_type (dict_type)`

系统启动时自动初始化的默认数据字典（9 个字典类型及字典项）：

| dict_type | dict_code | dict_label | dict_value | 说明 |
|-----------|-----------|------------|------------|------|
| CommonStatus | ENABLED | 启用 | 1 | 通用状态 |
| CommonStatus | DISABLED | 禁用 | 0 | |
| org_level | HEAD_OFFICE | 总行 | 1 | 机构等级 |
| org_level | BRANCH | 分行 | 2 | |
| org_level | SUB_BRANCH | 支行 | 3 | |
| org_level | DEPARTMENT | 营业部 | 4 | |
| PersonStatus | ACTIVE | 正常 | 1 | 人员状态 |
| PersonStatus | RESIGNED | 离职 | 2 | |
| PersonStatus | RETIRED | 退休 | 3 | |
| UserStatus | NORMAL | 正常 | 1 | 用户状态 |
| UserStatus | LOCKED | 锁定 | 2 | |
| UserStatus | DISABLED | 禁用 | 3 | |
| UserStatus | CANCELLED | 注销 | 4 | |
| OperationType | ADD | 新增 | 1 | 操作类型 |
| OperationType | EDIT | 编辑 | 2 | |
| OperationType | DELETE | 删除 | 3 | |
| OperationType | IMPORT | 导入 | 4 | |
| OperationType | EXPORT | 导出 | 5 | |
| OperationType | CHANGE_STATUS | 状态变更 | 6 | |
| LoginType | LOGIN | 登录 | 1 | 登录类型 |
| LoginType | LOGOUT | 登出 | 2 | |
| LoginType | RESET_PWD | 重置密码 | 3 | |
| LoginType | UNLOCK | 解锁 | 4 | |
| RiskType | AUTH_FAIL | 认证失败 | 1 | 安全风险类型 |
| RiskType | SESSION_EXPIRED | 会话过期 | 2 | |
| RiskType | SS_CONFLICT | 账号冲突 | 3 | |
| RiskLevel | LOW | 低 | 1 | 安全风险等级 |
| RiskLevel | MEDIUM | 中 | 2 | |
| RiskLevel | HIGH | 高 | 3 | |
| HandleStatus | UNHANDLED | 未处理 | 1 | 安全处理状态 |
| HandleStatus | HANDLED | 已处理 | 2 | |
| HandleStatus | IGNORED | 已忽略 | 3 | |

### sys_operation_log（操作日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| operator_id | bigint | 操作人ID |
| operator_name | varchar(64) | 操作人姓名 |
| operator_ip | varchar(64) | 操作IP |
| operator_org_code | varchar(64) | 操作人机构号 |
| operator_org | varchar(128) | 操作人机构名 |
| module | varchar(64) | 操作模块 |
| operation_type | varchar(32) | 操作类型（1/2/3/4/5/6，通过字典服务从枚举获取） |
| operation_content | varchar(512) | 操作内容 |
| request_params | text | 请求参数 |
| operation_result | varchar(32) | 操作结果（success/fail） |
| error_msg | text | 异常信息 |
| operation_time | datetime | 操作时间 |
| is_deleted | tinyint(1) | 逻辑删除 |
| create_time / update_time | datetime | 创建时间/修改时间 |

普通索引：`idx_operation_log_operation_time (operation_time)`, `idx_operation_log_operator_name (operator_name)`, `idx_operation_log_module (module)`

### sys_login_log（登录日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| username | varchar(64) | 用户名 |
| login_ip | varchar(64) | 登录IP |
| login_device | varchar(128) | 登录设备（User-Agent） |
| login_type | varchar(32) | 登录类型（1/2/3/4） |
| login_result | varchar(32) | 登录结果（success/fail） |
| fail_reason | varchar(255) | 失败原因 |
| login_time | datetime | 登录时间 |
| logout_time | datetime | 登出时间 |
| is_abnormal | tinyint(1) | 是否异常登录（密码过期/账号被锁定时标记） |
| is_deleted | tinyint(1) | 逻辑删除 |
| create_time / update_time | datetime | 创建时间/修改时间 |

普通索引：`idx_login_log_login_time (login_time)`, `idx_login_log_username (username)`

### sys_system_log（系统日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| log_level | varchar(32) | 日志级别（INFO/WARN/ERROR） |
| log_module | varchar(64) | 日志模块 |
| log_content | text | 日志内容 |
| exception_stack | text | 异常堆栈 |
| occur_time | datetime | 发生时间 |
| server_ip | varchar(64) | 服务器IP |
| is_deleted | tinyint(1) | 逻辑删除 |
| create_time / update_time | datetime | 创建时间/修改时间 |

普通索引：`idx_system_log_occur_time (occur_time)`

### sys_security_log（安全日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| operator_id | bigint | 操作人ID |
| operator_name | varchar(64) | 操作人姓名 |
| operator_ip | varchar(64) | 操作IP |
| risk_type | varchar(64) | 风险类型（1/2/3，通过字典服务从枚举获取） |
| risk_content | varchar(512) | 风险描述 |
| request_url | varchar(255) | 请求URL |
| risk_level | varchar(32) | 风险等级（1/2/3，通过字典服务从枚举获取） |
| handle_status | varchar(32) | 处理状态（1/2/3，通过字典服务从枚举获取） |
| handle_user_id | bigint | 处理人ID |
| handle_time | datetime | 处理时间 |
| handle_note | varchar(512) | 处理备注 |
| occur_time | datetime | 发生时间 |
| is_deleted | tinyint(1) | 逻辑删除 |
| create_time / update_time | datetime | 创建时间/修改时间 |

普通索引：`idx_security_log_occur_time (occur_time)`, `idx_security_log_handle_status (handle_status)`

### 日志归档表

`sys_operation_log_archive`、`sys_login_log_archive`、`sys_system_log_archive`、`sys_security_log_archive` 结构与对应主表完全一致，用于存储归档的历史日志数据。

### sys_task（定时任务表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar(64) | 任务名称 |
| service_name | varchar(64) | 服务名称 |
| fun_path | varchar(255) | 方法路径（格式: beanName.methodName） |
| cron | varchar(32) | Cron表达式 |
| has_start | tinyint(1) | 是否启用 |
| is_deleted | tinyint(1) | 逻辑删除 |
| create_by / update_by | bigint | 创建人/修改人 |
| create_time / update_time | datetime | 创建时间/修改时间 |

唯一约束：`uk_task_service_fun (service_name, fun_path)`

### sys_task_log（定时任务日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| task_id | bigint | 定时任务ID |
| task_name | varchar(64) | 任务名称 |
| service_name | varchar(64) | 服务名称 |
| fun_path | varchar(255) | 方法路径 |
| cron | varchar(32) | 触发时的Cron表达式 |
| run_result | int | 运行结果（1=成功 0=失败） |
| run_log | text | 运行日志（开始时间、执行详情、结束时间、耗时） |
| duration_ms | bigint | 执行耗时（毫秒） |
| create_time | datetime | 创建时间 |

普通索引：`idx_task_log_task_id (task_id)`, `idx_task_log_fun_path (fun_path)`, `idx_task_log_create_time (create_time)`

系统首次启动且任务表为空时自动初始化默认任务：

| name | service_name | fun_path | cron | has_start |
|------|------------|----------|------|-----------|
| 日志归档任务 | (配置值 task.service_name) | logArchiveTask.archiveLogs | 0 0 2 * * ? | true |

## 关键设计

- **SM4加密**: 数据库连接凭据、Redis 连接信息和人员敏感字段（id_card、phone）均使用 SM4 ECB/PKCS7Padding 加密，密钥通过 `sm4.key` 配置并由 `@ConfigurationProperties` 绑定。配置支持明文兼容——若使用明文，系统可正常运行并在启动日志（WARN级别）中输出对应的密文，方便运维替换为加密配置
- **基础父表**: 抽离通用字段为 `BaseEntity`（@MappedSuperclass），业务表通过继承复用
- **树形结构**: 机构数据单根节点，一次查询全部数据后在内存中构建树
- **逻辑删除**: 所有删除操作为逻辑删除，查询自动过滤已删除数据，关联数据批量逻辑删除（使用 saveAll 批量操作）
- **统一响应**: 所有接口返回 `R<T>` 格式，包含 code/message/data
- **全局异常处理**: `GlobalExceptionHandler` 统一处理业务异常、参数校验异常和系统异常，未预期异常自动写入系统日志
- **JPA审计**: `BaseEntity` 和日志实体通过 `@CreatedDate`/`@LastModifiedDate` + `@EntityListeners(AuditingEntityListener.class)` 自动维护创建/修改时间
- **数据字典**: 单表存储字典类型与字典项，提供编码/值翻译能力，支持级联删除，翻译结果通过 Redis 缓存加速。枚举类统一为裸常量（仅常量名，无 value/code/desc 字段），`dict_type` = 枚举类简单名（`getSimpleName`），`dict_code` = 枚举常量名（`name()`），`dict_value` 和 `dict_label` 在字典初始化时写入。业务代码通过 `DictService.getDictValue(dictType, dictCode)` 获取实际存储值，不硬编码 dict_value/dict_label
- **RBAC权限**: 用户-角色-菜单三层关联，支持角色分配菜单权限、用户绑定多个角色，级联逻辑删除关联数据，用户权限通过批量查询优化（消除 N+1 查询）
- **密码安全**: 用户密码 MD5 加密存储，创建用户和重置密码时根据 `password_strength_level` 配置校验密码强度（三级策略），支持密码有效期配置（`password_expire_days`），登录时检查密码是否过期
- **登录安全**: 登录失败次数达到 `login_fail_max_count` 后自动锁定账号（状态变更为"锁定"），需管理员手动解锁
- **菜单树**: 菜单支持目录/菜单/按钮三级类型，通过 parentId 构建树形导航结构，菜单树查询结果缓存至 Redis
- **JWT认证**: 基于 JWT 的无状态认证，`jwt.secret` 通过 `application.yml` 配置，token 过期时间从系统配置表 `session_timeout_minutes` 动态读取（默认20分钟），与 Redis 会话 TTL 保持一致。白名单通过系统配置表 `jwt_whitelist` 动态管理（逗号分隔，支持 Ant 风格通配符如 `/api/auth/**`），配置值缓存至 Redis（30分钟 TTL），修改配置后通过清理 config 缓存即时生效
- **单点登录**: Redis 存储在线用户会话，同一账号仅允许一个活跃会话，重复登录自动踢出旧会话
- **Redis缓存**: 在线用户会话、系统配置、字典翻译、菜单树等数据缓存至 Redis，缓存 TTL 从 `cache_expire_hour` 配置读取并加入随机偏移（防缓存雪崩），支持按类型清理缓存和查看缓存统计
- **异步日志**: 操作日志、登录日志、系统日志、安全日志的保存操作均通过 `@Async` 异步执行（`@EnableAsync` 已启用），避免阻塞主业务流程
- **操作日志**: 基于 `@OperationLog` 注解 + Spring AOP 切面自动捕获 Controller 增删改操作，异步写入数据库
- **登录日志**: 在认证服务中登录成功/失败/登出时自动记录，包含登录IP、设备信息（User-Agent）、异常登录标记
- **安全日志**: 在 JWT 过滤器中认证失败/会话过期/账号冲突时自动记录，支持安全事件处理（标记已处理/忽略）
- **系统日志**: 全局异常处理器自动捕获未预期系统异常写入，也可通过注入 `SystemLogService` 在业务代码中手动记录系统事件
- **日志归档**: 定时任务每天凌晨2点执行，分别读取操作日志/登录日志/安全日志/系统日志的归档天数配置（默认60天），将超期日志从主表迁移到归档表
- **动态定时任务**: 定时任务持久化到 `sys_task` 表，通过 `DynamicTaskScheduler` + `ThreadPoolTaskScheduler` + 反射（`beanName.methodName` 格式）动态注册和调度。`SystemConfigInitializer` 启动时读取全部任务按 `redisKey` 分组写入 Redis 缓存并发布通知，各服务实例通过 `TaskRedisListener` 订阅通知后从缓存加载并初始化自己的定时任务。运行时变更（CRUD/启停）统一通过 Redis pub/sub 通知刷新，手动触发通过 `trigger:{funPath}` 消息通知各实例执行。任务执行使用 Redisson 分布式锁（`sys:task:lock:{redisKey}:exec:{funPath}`）保证同一任务只在一个实例执行
- **任务日志**: 定时任务执行时自动记录运行日志到 `sys_task_log` 表。`DynamicTaskScheduler` 在任务执行前通过 `TaskLogContext`（ThreadLocal）设置任务元数据上下文，任务方法通过 `TaskLogHelper.execute(Supplier<String>)` 包裹执行逻辑，自动记录开始时间、执行详情（任务方法返回的自定义内容）、结束时间和耗时。日志通过 Redis List 队列（`sys:task:log:queue`，LPUSH/BRPOP）异步写入数据库，`TaskLogConsumer` 守护线程负责消费和持久化。未使用 `TaskLogHelper` 的任务也会自动记录基础日志。分布式安全：任务执行由 Redisson 锁保护、ThreadLocal 线程隔离、Redis LPUSH/BRPOP 原子操作保证每条日志仅被一个消费者实例处理
- **系统初始化**: `SystemConfigInitializer` 在 `@PostConstruct` 阶段自动检查并初始化以下数据（已存在则跳过）：
  - 默认配置（13 项）：用户安全、JWT认证、日志审计、缓存策略
  - 默认数据字典（9 个字典类型 + 字典项）：通用状态、机构等级（总行/分行/支行/营业部）、人员状态、用户状态、操作类型、登录类型、安全风险类型、安全风险等级、安全处理状态
  - 默认菜单（17 个）：组织机构（机构管理、人员管理）、系统管理（用户管理、角色管理、菜单管理、系统配置、数据字典、缓存管理、定时任务）、日志管理（操作日志、登录日志、系统日志、安全日志、任务日志），仅菜单表为空时初始化
  - 超级管理员角色（super_admin）：分配全部菜单权限，仅角色表为空时初始化
  - admin 用户：默认密码取自 `default_admin_password` 配置（默认 888888），绑定超级管理员角色，仅用户表为空时初始化
  - 默认定时任务：日志归档任务（cron: `0 0 2 * * ?`，启用），仅任务表为空时初始化
- **API文档**: 集成 SpringDoc OpenAPI，启动后访问 Swagger UI 查看接口文档

## 启动方式

```bash
cd sys
mvn spring-boot:run
```

服务启动后访问 `http://localhost:8080`，Swagger UI 地址：`http://localhost:8080/swagger-ui.html`。

