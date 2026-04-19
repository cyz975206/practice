package com.cyz.config;

import com.cyz.common.util.Md5Util;
import com.cyz.common.enums.*;
import com.cyz.common.constant.CacheConstant;
import com.cyz.entity.SysConfig;
import com.cyz.entity.SysDict;
import com.cyz.entity.SysMenu;
import com.cyz.entity.SysRole;
import com.cyz.entity.SysRoleMenu;
import com.cyz.entity.SysTask;
import com.cyz.entity.SysUser;
import com.cyz.entity.SysUserRole;
import com.cyz.repository.SysConfigRepository;
import com.cyz.repository.SysDictRepository;
import com.cyz.repository.SysMenuRepository;
import com.cyz.repository.SysRoleMenuRepository;
import com.cyz.repository.SysRoleRepository;
import com.cyz.repository.SysTaskRepository;
import com.cyz.repository.SysUserRepository;
import com.cyz.repository.SysUserRoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemConfigInitializer {

    private final SysConfigRepository configRepository;
    private final SysDictRepository dictRepository;
    private final SysRoleRepository roleRepository;
    private final SysMenuRepository menuRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysTaskRepository taskRepository;
    private final TaskProperties taskProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SUPER_ADMIN_ROLE_CODE = "super_admin";
    private static final String SUPER_ADMIN_ROLE_NAME = "超级管理员";
    private static final String ADMIN_USERNAME = "admin";

    @PostConstruct
    @Transactional
    public void init() {
        initDefaultConfigs();
        initDefaultDicts();
        initDefaultMenus();
        initSuperAdminRole();
        initAdminUser();
        initDefaultTasks();
        loadTasksToRedis();
    }

    // ==================== 系统配置初始化 ====================

    private void initDefaultConfigs() {
        List<ConfigItem> items = new ArrayList<>();
        // 用户安全配置
        items.add(new ConfigItem("default_admin_password", "初始化管理员默认密码", "888888", "系统首次启动时创建admin用户的默认密码", 1));
        items.add(new ConfigItem("login_fail_max_count", "登录失败最大次数", "5", "连续登录失败达到此次数后锁定账号", 2));
        items.add(new ConfigItem("password_expire_days", "密码有效期天数", "0", "密码过期天数，0表示无期限", 3));
        items.add(new ConfigItem("password_strength_level", "密码强度等级", "1", "密码强度等级（1-低 2-中 3-高）", 4));
        items.add(new ConfigItem("session_timeout_minutes", "会话超时时间", "20", "会话超时时间（分钟）", 5));
        // 日志审计配置
        items.add(new ConfigItem("operation_log_retention_days", "操作日志归档天数", "60", "超过此天数的操作日志将被归档", 6));
        items.add(new ConfigItem("login_log_retention_days", "登录日志归档天数", "60", "超过此天数的登录日志将被归档", 7));
        items.add(new ConfigItem("security_log_retention_days", "安全日志归档天数", "60", "超过此天数的安全日志将被归档", 8));
        items.add(new ConfigItem("system_log_retention_days", "系统日志归档天数", "60", "超过此天数的系统日志将被归档", 9));
        // JWT认证配置
        items.add(new ConfigItem("jwt_whitelist", "JWT认证白名单", "/api/auth/login,/swagger-ui/**,/swagger-ui.html,/v3/api-docs/**,/favicon.ico,/error", "免认证路径，多个路径用英文逗号分隔，支持Ant风格通配符", 10));
        // 缓存策略配置
        items.add(new ConfigItem("cache_expire_hour", "权限缓存过期小时数", "1", "权限相关缓存过期时间（小时）", 11));
        items.add(new ConfigItem("null_cache_minute", "空值缓存分钟数", "10", "空值缓存过期时间（分钟）", 12));
        items.add(new ConfigItem("cache_random_offset_minute", "缓存随机偏移分钟", "10", "缓存过期随机偏移时间（分钟）", 13));

        int created = 0;
        for (ConfigItem item : items) {
            if (!configRepository.existsByConfigKeyAndIsDeletedFalse(item.key)) {
                SysConfig config = SysConfig.builder()
                        .configKey(item.key)
                        .configName(item.name)
                        .configValue(item.defaultValue)
                        .remark(item.remark)
                        .sort(item.sort)
                        .build();
                configRepository.save(config);
                created++;
            }
        }
        if (created > 0) {
            log.info("初始化系统默认配置完成，新增 {} 项配置", created);
        }
    }

    // ==================== 数据字典初始化 ====================

    private void initDefaultDicts() {
        List<DictTypeItem> types = new ArrayList<>();

        // 通用状态
        types.add(new DictTypeItem(CommonStatus.class.getSimpleName(), "通用状态",
                new DictItemData(CommonStatus.ENABLED.name(), "启用", "1", 1),
                new DictItemData(CommonStatus.DISABLED.name(), "禁用", "0", 2)));

        // 机构等级
        types.add(new DictTypeItem(OrgLevel.class.getSimpleName(), "机构等级",
                new DictItemData(OrgLevel.HEAD_OFFICE.name(), "总行", "1", 1),
                new DictItemData(OrgLevel.BRANCH.name(), "分行", "2", 2),
                new DictItemData(OrgLevel.SUB_BRANCH.name(), "支行", "3", 3),
                new DictItemData(OrgLevel.DEPARTMENT.name(), "营业部", "4", 4)));

        // 人员状态
        types.add(new DictTypeItem(PersonStatus.class.getSimpleName(), "人员状态",
                new DictItemData(PersonStatus.ACTIVE.name(), "正常", "1", 1),
                new DictItemData(PersonStatus.RESIGNED.name(), "离职", "2", 2),
                new DictItemData(PersonStatus.RETIRED.name(), "退休", "3", 3)));

        // 用户状态
        types.add(new DictTypeItem(UserStatus.class.getSimpleName(), "用户状态",
                new DictItemData(UserStatus.NORMAL.name(), "正常", "1", 1),
                new DictItemData(UserStatus.LOCKED.name(), "锁定", "2", 2),
                new DictItemData(UserStatus.DISABLED.name(), "禁用", "3", 3),
                new DictItemData(UserStatus.CANCELLED.name(), "注销", "4", 4)));

        // 操作类型
        types.add(new DictTypeItem(OperationType.class.getSimpleName(), "操作类型",
                new DictItemData(OperationType.ADD.name(), "新增", "1", 1),
                new DictItemData(OperationType.EDIT.name(), "编辑", "2", 2),
                new DictItemData(OperationType.DELETE.name(), "删除", "3", 3),
                new DictItemData(OperationType.IMPORT.name(), "导入", "4", 4),
                new DictItemData(OperationType.EXPORT.name(), "导出", "5", 5),
                new DictItemData(OperationType.CHANGE_STATUS.name(), "状态变更", "6", 6)));

        // 登录类型
        types.add(new DictTypeItem(LoginType.class.getSimpleName(), "登录类型",
                new DictItemData(LoginType.LOGIN.name(), "登录", "1", 1),
                new DictItemData(LoginType.LOGOUT.name(), "登出", "2", 2),
                new DictItemData(LoginType.RESET_PWD.name(), "重置密码", "3", 3),
                new DictItemData(LoginType.UNLOCK.name(), "解锁", "4", 4)));

        // 安全风险类型
        types.add(new DictTypeItem(RiskType.class.getSimpleName(), "安全风险类型",
                new DictItemData(RiskType.AUTH_FAIL.name(), "认证失败", "1", 1),
                new DictItemData(RiskType.SESSION_EXPIRED.name(), "会话过期", "2", 2),
                new DictItemData(RiskType.SS_CONFLICT.name(), "账号冲突", "3", 3)));

        // 安全风险等级
        types.add(new DictTypeItem(RiskLevel.class.getSimpleName(), "安全风险等级",
                new DictItemData(RiskLevel.LOW.name(), "低", "1", 1),
                new DictItemData(RiskLevel.MEDIUM.name(), "中", "2", 2),
                new DictItemData(RiskLevel.HIGH.name(), "高", "3", 3)));

        // 安全处理状态
        types.add(new DictTypeItem(HandleStatus.class.getSimpleName(), "安全处理状态",
                new DictItemData(HandleStatus.UNHANDLED.name(), "未处理", "1", 1),
                new DictItemData(HandleStatus.HANDLED.name(), "已处理", "2", 2),
                new DictItemData(HandleStatus.IGNORED.name(), "已忽略", "3", 3)));

        int typeCreated = 0;
        int itemCounted = 0;
        for (DictTypeItem type : types) {
            // 检查字典类型是否存在
            if (!dictRepository.existsByDictTypeAndDictCodeIsNullAndIsDeletedFalse(type.dictType)) {
                SysDict typeRow = new SysDict();
                typeRow.setDictType(type.dictType);
                typeRow.setDictName(type.dictName);
                typeRow.setStatus("1");
                dictRepository.save(typeRow);
                typeCreated++;
            }
            // 检查字典项
            for (DictItemData item : type.items) {
                if (!dictRepository.existsByDictTypeAndDictCodeAndIsDeletedFalse(type.dictType, item.code)) {
                    SysDict itemRow = new SysDict();
                    itemRow.setDictType(type.dictType);
                    itemRow.setDictCode(item.code);
                    itemRow.setDictLabel(item.label);
                    itemRow.setDictValue(item.value);
                    itemRow.setSort(item.sort);
                    itemRow.setStatus("1");
                    dictRepository.save(itemRow);
                    itemCounted++;
                }
            }
        }
        if (typeCreated > 0 || itemCounted > 0) {
            log.info("初始化数据字典完成，新增 {} 个字典类型，{} 个字典项", typeCreated, itemCounted);
        }
    }

    // ==================== 菜单数据初始化 ====================

    private void initDefaultMenus() {
        if (menuRepository.countByIsDeletedFalse() > 0) {
            return;
        }

        // 一级菜单（目录）
        SysMenu orgDir = createMenu("org", "组织机构", "D", null, "/org", null, null, false, 1);
        SysMenu systemDir = createMenu("system", "系统管理", "D", null, "/system", null, null, false, 2);

        SysMenu logDir = createMenu("log", "日志管理", "D", null, "/log", null, null, false, 3);

        // 二级菜单
        createMenu("org:list", "机构管理", "M", orgDir.getId(), "/org/list", "org/index", "system:org:list", false, 1);
        createMenu("person:list", "人员管理", "M", orgDir.getId(), "/person/list", "person/index", "system:person:list", false, 2);
        createMenu("user:list", "用户管理", "M", systemDir.getId(), "/user/list", "user/index", "system:user:list", false, 1);
        createMenu("role:list", "角色管理", "M", systemDir.getId(), "/role/list", "role/index", "system:role:list", false, 2);
        createMenu("menu:list", "菜单管理", "M", systemDir.getId(), "/menu/list", "menu/index", "system:menu:list", false, 3);
        createMenu("config:list", "系统配置", "M", systemDir.getId(), "/config/list", "config/index", "system:config:list", false, 4);
        createMenu("dict:list", "数据字典", "M", systemDir.getId(), "/dict/list", "dict/index", "system:dict:list", false, 5);
        createMenu("cache:list", "缓存管理", "M", systemDir.getId(), "/cache/list", "cache/index", "system:cache:list", false, 6);
        createMenu("task:list", "定时任务", "M", systemDir.getId(), "/task/list", "task/index", "system:task:list", false, 7);
        createMenu("log:operation", "操作日志", "M", logDir.getId(), "/log/operation", "log/operation", "system:log:operation", false, 1);
        createMenu("log:login", "登录日志", "M", logDir.getId(), "/log/login", "log/login", "system:log:login", false, 2);
        createMenu("log:system", "系统日志", "M", logDir.getId(), "/log/system", "log/system", "system:log:system", false, 3);
        createMenu("log:security", "安全日志", "M", logDir.getId(), "/log/security", "log/security", "system:log:security", false, 4);

        log.info("初始化默认菜单完成，共 16 个菜单");
    }

    private SysMenu createMenu(String code, String name, String type, Long parentId,
                                String path, String component, String perms,
                                Boolean isFrame, int sort) {
        SysMenu menu = SysMenu.builder()
                .menuCode(code)
                .menuName(name)
                .menuType(type)
                .parentId(parentId)
                .path(path)
                .component(component)
                .perms(perms)
                .isFrame(isFrame)
                .sort(sort)
                .status("1")
                .build();
        return menuRepository.save(menu);
    }

    // ==================== 超级管理员角色初始化 ====================

    private void initSuperAdminRole() {
        if (roleRepository.countByIsDeletedFalse() > 0) {
            return;
        }

        SysRole superAdminRole = SysRole.builder()
                .roleCode(SUPER_ADMIN_ROLE_CODE)
                .roleName(SUPER_ADMIN_ROLE_NAME)
                .roleDesc("拥有系统全部菜单权限的超级管理员角色")
                .status("1")
                .sort(0)
                .build();
        superAdminRole = roleRepository.save(superAdminRole);
        log.info("初始化超级管理员角色: {} ({})", superAdminRole.getRoleName(), superAdminRole.getId());

        List<SysMenu> allMenus = menuRepository.findAllActiveOrderBySort();
        for (SysMenu menu : allMenus) {
            SysRoleMenu roleMenu = SysRoleMenu.builder()
                    .roleId(superAdminRole.getId())
                    .menuId(menu.getId())
                    .build();
            roleMenuRepository.save(roleMenu);
        }
        log.info("超级管理员角色已分配 {} 个菜单权限", allMenus.size());
    }

    // ==================== admin 用户初始化 ====================

    private void initAdminUser() {
        if (userRepository.countByIsDeletedFalse() > 0) {
            return;
        }

        String defaultPassword = configRepository.findByConfigKeyAndIsDeletedFalse("default_admin_password")
                .map(SysConfig::getConfigValue)
                .orElse("888888");

        SysUser adminUser = SysUser.builder()
                .username(ADMIN_USERNAME)
                .password(Md5Util.encrypt(defaultPassword))
                .nickname("超级管理员")
                .status("1")
                .loginFailCount(0)
                .build();
        adminUser = userRepository.save(adminUser);
        log.info("初始化admin用户: {} ({})", adminUser.getUsername(), adminUser.getId());

        SysRole superAdminRole = roleRepository.findByRoleCodeAndIsDeletedFalse(SUPER_ADMIN_ROLE_CODE)
                .orElseThrow(() -> new RuntimeException("超级管理员角色不存在，初始化失败"));
        SysUserRole userRole = SysUserRole.builder()
                .userId(adminUser.getId())
                .roleId(superAdminRole.getId())
                .build();
        userRoleRepository.save(userRole);
        log.info("admin用户已绑定超级管理员角色");
    }

    // ==================== 定时任务初始化 ====================

    private void initDefaultTasks() {
        if (taskRepository.countByIsDeletedFalse() > 0) {
            return;
        }
        SysTask logArchiveTask = SysTask.builder()
                .name("日志归档任务")
                .serviceName(taskProperties.getServiceName())
                .funPath("logArchiveTask.archiveLogs")
                .cron("0 0 2 * * ?")
                .hasStart(true)
                .build();
        taskRepository.save(logArchiveTask);
        log.info("初始化默认定时任务完成");
    }

    private void loadTasksToRedis() {
        List<SysTask> allTasks = taskRepository.findAllByIsDeletedFalse();
        Map<String, List<SysTask>> grouped = allTasks.stream()
                .collect(java.util.stream.Collectors.groupingBy(SysTask::getServiceName));
        for (Map.Entry<String, List<SysTask>> entry : grouped.entrySet()) {
            String rk = entry.getKey();
            List<SysTask> tasks = entry.getValue();
            String listKey = CacheConstant.TASK_LIST_PREFIX + rk;
            redisTemplate.delete(listKey);
            if (!tasks.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(listKey, tasks.toArray());
            }
            String channel = CacheConstant.TASK_CHANNEL_PREFIX + rk;
            redisTemplate.convertAndSend(channel, "refresh");
            log.info("加载定时任务到Redis: redisKey={}, count={}", rk, tasks.size());
        }
    }

    // ==================== 内部记录类 ====================

    private record ConfigItem(String key, String name, String defaultValue, String remark, int sort) {
    }

    private record DictTypeItem(String dictType, String dictName, DictItemData... items) {
    }

    private record DictItemData(String code, String label, String value, int sort) {
    }
}
