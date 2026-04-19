<template>
  <div class="cache-manage">
    <!-- 页面头部 -->
    <div class="cm-header">
      <div class="cm-header__left">
        <h2 class="cm-header__title">缓存管理</h2>
      </div>
      <el-popconfirm
        title="确认清理全部缓存？"
        confirm-button-text="确定"
        cancel-button-text="取消"
        @confirm="handleClearCache('all')"
      >
        <template #reference>
          <el-button type="danger" :loading="clearing === 'all'">
            <el-icon><Delete /></el-icon>
            <span>清理全部缓存</span>
          </el-button>
        </template>
      </el-popconfirm>
    </div>

    <!-- 缓存统计 -->
    <div class="cm-stats">
      <div
        v-for="item in statItems"
        :key="item.key"
        class="cm-stat-tile"
        :class="`cm-stat-tile--${item.key}`"
      >
        <span class="cm-stat-tile__icon">
          <el-icon :size="22"><component :is="item.icon" /></el-icon>
        </span>
        <div class="cm-stat-tile__info">
          <span class="cm-stat-tile__count">{{
            cacheStats[item.valueKey]
          }}</span>
          <span class="cm-stat-tile__label">{{ item.label }}</span>
        </div>
        <el-popconfirm
          :title="`确认清理${item.label}？`"
          confirm-button-text="确定"
          cancel-button-text="取消"
          @confirm="handleClearCache(item.key)"
        >
          <template #reference>
            <el-button
              class="cm-stat-tile__action"
              size="small"
              :loading="clearing === item.key"
            >
              清理
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <!-- 在线用户 -->
    <div class="cm-online-panel">
      <div class="cm-online-panel__bar">
        <div class="cm-online-panel__left">
          <span class="cm-online-panel__title">在线用户</span>
          <span class="cm-online-panel__count">{{
            cacheStats.onlineUserCount
          }}</span>
        </div>
        <el-button @click="loadOnlineUsers">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </el-button>
      </div>

      <el-table v-loading="onlineLoading" :data="onlineUsers">
        <el-table-column prop="username" label="用户名" min-width="120">
          <template #default="{ row }">
            <span class="cm-username">{{ row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="120">
          <template #default="{ row }">
            <span class="cm-nickname">{{ row.nickname }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="orgCode"
          label="所属机构"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column prop="loginIp" label="登录IP" min-width="140">
          <template #default="{ row }">
            <code class="cm-ip">{{ row.loginIp }}</code>
          </template>
        </el-table-column>
        <el-table-column
          prop="loginTime"
          label="登录时间"
          width="170"
          align="center"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span class="cm-time">{{ formatDate(row.loginTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              :title="`确认强制用户「${row.nickname || row.username}」下线？`"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleForceOffline(row)"
            >
              <template #reference>
                <el-button circle size="small" type="danger">
                  <el-icon :size="14"><Delete /></el-icon>
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Refresh, Document, Menu, Setting, Lock } from '@element-plus/icons-vue'
import {
  getCacheStats,
  clearCache,
  getOnlineUsers,
  forceOffline,
} from '@/api/cache'
import type { CacheStatsVO, OnlineUserVO } from '@/api/cache/types'
import { formatDate } from '@/utils'

// ==================== 统计配置 ====================
const statItems = [
  {
    key: 'dict',
    label: '字典缓存',
    valueKey: 'dictCacheCount',
    icon: 'Document',
  },
  {
    key: 'menu',
    label: '菜单缓存',
    valueKey: 'menuCacheCount',
    icon: 'Menu',
  },
  {
    key: 'config',
    label: '配置缓存',
    valueKey: 'configCacheCount',
    icon: 'Setting',
  },
  {
    key: 'perms',
    label: '权限缓存',
    valueKey: 'permsCacheCount',
    icon: 'Lock',
  },
]

// ==================== 缓存统计 ====================
const cacheStats = ref<CacheStatsVO>({
  dictCacheCount: 0,
  menuCacheCount: 0,
  configCacheCount: 0,
  permsCacheCount: 0,
  onlineUserCount: 0,
})

async function loadCacheStats() {
  try {
    const res = await getCacheStats()
    cacheStats.value = res.data || cacheStats.value
  } catch (e) {
    console.error('加载缓存统计失败', e)
  }
}

// ==================== 缓存清理 ====================
const clearing = ref('')

async function handleClearCache(type: string) {
  const typeLabels: Record<string, string> = {
    dict: '字典缓存',
    menu: '菜单缓存',
    config: '配置缓存',
    perms: '权限缓存',
    all: '全部缓存',
  }
  const label = typeLabels[type] || type
  clearing.value = type
  try {
    await clearCache(type)
    ElMessage.success(`${label}清理成功`)
    loadCacheStats()
  } catch (e) {
    console.error('清理缓存失败', e)
  } finally {
    clearing.value = ''
  }
}

// ==================== 在线用户 ====================
const onlineUsers = ref<OnlineUserVO[]>([])
const onlineLoading = ref(false)

async function loadOnlineUsers() {
  onlineLoading.value = true
  try {
    const res = await getOnlineUsers()
    onlineUsers.value = res.data || []
  } catch (e) {
    console.error('加载在线用户失败', e)
  } finally {
    onlineLoading.value = false
  }
}

async function handleForceOffline(row: OnlineUserVO) {
  try {
    await forceOffline(row.userId)
    ElMessage.success('已强制下线')
    loadOnlineUsers()
    loadCacheStats()
  } catch (e) {
    console.error('强制下线失败', e)
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadCacheStats()
  loadOnlineUsers()
})
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.cache-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* ─── 头部 ─── */
.cm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    letter-spacing: -0.02em;
  }
}

/* ─── 统计卡片 ─── */
.cm-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.cm-stat-tile {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-left: 3px solid transparent;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: var(--el-box-shadow-light);
  }

  &--dict {
    border-left-color: #3b82f6;
  }
  &--menu {
    border-left-color: #10b981;
  }
  &--config {
    border-left-color: #f59e0b;
  }
  &--perms {
    border-left-color: #8b5cf6;
  }

  &__icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &--dict &__icon {
    background: #eff6ff;
    color: #3b82f6;
  }
  &--menu &__icon {
    background: #ecfdf5;
    color: #10b981;
  }
  &--config &__icon {
    background: #fffbeb;
    color: #f59e0b;
  }
  &--perms &__icon {
    background: #f5f3ff;
    color: #8b5cf6;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__count {
    display: block;
    font-size: 28px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    line-height: 1.2;
    letter-spacing: -0.02em;
  }

  &__label {
    display: block;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin-top: 4px;
  }

  &__action {
    align-self: flex-start;
  }
}

/* ─── 在线用户面板 ─── */
.cm-online-panel {
  flex: 1;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;

  &__bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color-extra-light);
  }

  &__left {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  &__count {
    font-size: 13px;
    font-weight: 600;
    color: #f43f5e;
    background: #fff1f2;
    border: 1px solid #fecdd3;
    padding: 2px 10px;
    border-radius: 100px;
  }

  :deep(.el-table) {
    --el-table-border-color: transparent;
    --el-table-header-border-color: transparent;
    flex: 1;

    th.el-table__cell {
      background: var(--el-fill-color-lighter);
      font-weight: 600;
      font-size: 12px;
      letter-spacing: 0.04em;
      color: var(--el-text-color-secondary);
    }

    td.el-table__cell {
      border-bottom-color: var(--el-border-color-extra-light);
    }

    .el-table__row {
      transition: background 0.15s;
    }

    .el-table__row:hover > td.el-table__cell {
      background: var(--el-fill-color-light) !important;
    }
  }
}

/* ─── 表格单元格 ─── */
.cm-username {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.cm-nickname {
  color: var(--el-text-color-regular);
}

.cm-ip {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 4px;
}

.cm-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ─── 暗色模式 ─── */
html.dark {
  .cm-stat-tile {
    &--dict &__icon {
      background: rgba(59, 130, 246, 0.15);
    }
    &--menu &__icon {
      background: rgba(16, 185, 129, 0.15);
    }
    &--config &__icon {
      background: rgba(245, 158, 11, 0.15);
    }
    &--perms &__icon {
      background: rgba(139, 92, 246, 0.15);
    }
  }

  .cm-online-panel__count {
    background: rgba(244, 63, 94, 0.12);
    border-color: rgba(244, 63, 94, 0.2);
  }

  .cm-ip {
    background: rgba(255, 255, 255, 0.06);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 900px) {
  .cm-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .cm-stats {
    grid-template-columns: 1fr;
  }

  .cm-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
