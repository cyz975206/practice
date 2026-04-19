<template>
  <div class="log-manage">
    <!-- 页面头部 -->
    <div class="lm-header">
      <h2 class="lm-header__title">系统日志</h2>
      <span class="lm-header__desc">服务端运行日志及异常堆栈</span>
    </div>

    <!-- 搜索工具栏 -->
    <div class="lm-toolbar">
      <el-select
        v-model="queryParams.logLevel"
        placeholder="全部级别"
        clearable
        class="lm-toolbar__select"
      >
        <el-option label="INFO" value="INFO" />
        <el-option label="WARN" value="WARN" />
        <el-option label="ERROR" value="ERROR" />
      </el-select>
      <el-input
        v-model="queryParams.logModule"
        placeholder="搜索日志模块..."
        clearable
        :prefix-icon="Search"
        class="lm-toolbar__input"
        @keyup.enter="handleQuery"
      />
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="—"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD HH:mm:ss"
        :default-time="[new Date(0, 0, 0, 0, 0, 0), new Date(0, 0, 0, 23, 59, 59)]"
        class="lm-toolbar__date"
      />
      <el-button @click="handleQuery">搜索</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 表格区域 -->
    <div class="lm-table-panel">
      <el-table v-loading="tableLoading" :data="tableData" row-key="id">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="lm-expand">
              <div v-if="row.exceptionStack" class="lm-expand__stack">
                <pre>{{ row.exceptionStack }}</pre>
              </div>
              <div v-else class="lm-expand__empty">无异常堆栈信息</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="logLevel" label="级别" width="90" align="center">
          <template #default="{ row }">
            <span
              class="lm-level-badge"
              :class="`lm-level-badge--${(row.logLevel || '').toLowerCase()}`"
            >
              {{ row.logLevel }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="logModule" label="日志模块" min-width="140" show-overflow-tooltip />
        <el-table-column prop="logContent" label="日志内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="occurTime" label="发生时间" width="170" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="lm-time">{{ formatDate(row.occurTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="serverIp" label="服务器IP" min-width="140">
          <template #default="{ row }">
            <code class="lm-mono">{{ row.serverIp }}</code>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-if="total > 0"
        v-model:page="queryParams.page"
        v-model:limit="queryParams.size"
        :total="total"
        @pagination="loadTableData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getSystemLogPage } from '@/api/log/system'
import type { SystemLogVO, SystemLogQuery } from '@/api/log/types'
import Pagination from '@/components/Pagination/index.vue'
import { useTable } from '@/composables/useTable'
import { formatDate } from '@/utils'

// ==================== 表格 ====================
const dateRange = ref<string[]>([])

const {
  tableData,
  tableLoading,
  total,
  queryParams,
  loadTableData,
} = useTable<SystemLogQuery, SystemLogVO>({
  apiFn: getSystemLogPage,
  defaultQuery: {
    page: 1,
    size: 20,
    logLevel: undefined,
    logModule: undefined,
    startTime: undefined,
    endTime: undefined,
  },
})

function handleQuery() {
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.value.startTime = dateRange.value[0]
    queryParams.value.endTime = dateRange.value[1]
  } else {
    queryParams.value.startTime = undefined
    queryParams.value.endTime = undefined
  }
  queryParams.value.page = 1
  loadTableData()
}

function resetQuery() {
  dateRange.value = []
  queryParams.value = {
    page: 1,
    size: 20,
    logLevel: undefined,
    logModule: undefined,
    startTime: undefined,
    endTime: undefined,
  }
  loadTableData()
}
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.log-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* ─── 头部 ─── */
.lm-header {
  display: flex;
  align-items: baseline;
  gap: 12px;

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    letter-spacing: -0.02em;
  }

  &__desc {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

/* ─── 工具栏 ─── */
.lm-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  &__input {
    width: 180px;
  }

  &__select {
    width: 130px;
  }

  &__date {
    width: 260px;
  }
}

/* ─── 表格面板 ─── */
.lm-table-panel {
  flex: 1;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
  min-height: 0;

  :deep(.el-table) {
    --el-table-border-color: transparent;
    --el-table-header-border-color: transparent;

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

    .el-table__expanded-cell {
      padding: 0 20px 16px 60px;
    }
  }

  :deep(.el-pagination) {
    padding: 16px 20px;
    display: flex;
    justify-content: flex-end;
    border-top: 1px solid var(--el-border-color-extra-light);
  }
}

/* ─── 展开行 ─── */
.lm-expand {
  &__stack {
    pre {
      margin: 0;
      padding: 16px;
      background: var(--el-fill-color);
      border-radius: 6px;
      font-size: 12px;
      font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
      line-height: 1.7;
      white-space: pre-wrap;
      word-break: break-all;
      max-height: 360px;
      overflow-y: auto;
      color: #dc2626;
    }
  }

  &__empty {
    padding: 12px 0;
    font-size: 13px;
    color: var(--el-text-color-placeholder);
  }
}

/* ─── 表格单元格 ─── */
.lm-level-badge {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;

  &--info {
    background: #eff6ff;
    color: #3b82f6;
  }

  &--warn {
    background: #fffbeb;
    color: #d97706;
  }

  &--error {
    background: #fef2f2;
    color: #dc2626;
  }
}

.lm-mono {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 4px;
}

.lm-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ─── 暗色模式 ─── */
html.dark {
  .lm-level-badge {
    &--info {
      background: rgba(59, 130, 246, 0.15);
    }

    &--warn {
      background: rgba(217, 119, 6, 0.12);
    }

    &--error {
      background: rgba(220, 38, 38, 0.12);
    }
  }

  .lm-expand__stack pre {
    color: #f87171;
    background: rgba(255, 255, 255, 0.04);
  }

  .lm-mono {
    background: rgba(255, 255, 255, 0.06);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 768px) {
  .lm-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .lm-toolbar__input,
  .lm-toolbar__select,
  .lm-toolbar__date {
    width: 100%;
  }
}
</style>
