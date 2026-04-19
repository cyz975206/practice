<template>
  <div class="log-manage">
    <!-- 页面头部 -->
    <div class="lm-header">
      <h2 class="lm-header__title">操作日志</h2>
      <span class="lm-header__desc">系统操作的完整审计记录</span>
    </div>

    <!-- 搜索工具栏 -->
    <div class="lm-toolbar">
      <el-input
        v-model="queryParams.operatorName"
        placeholder="搜索操作人..."
        clearable
        :prefix-icon="Search"
        class="lm-toolbar__input"
        @keyup.enter="handleQuery"
      />
      <el-input
        v-model="queryParams.module"
        placeholder="搜索操作模块..."
        clearable
        class="lm-toolbar__input"
        @keyup.enter="handleQuery"
      />
      <el-select
        v-model="queryParams.operationType"
        placeholder="全部类型"
        clearable
        class="lm-toolbar__select"
      >
        <el-option
          v-for="item in operationTypeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-select
        v-model="queryParams.result"
        placeholder="全部结果"
        clearable
        class="lm-toolbar__select"
      >
        <el-option label="成功" value="success" />
        <el-option label="失败" value="fail" />
      </el-select>
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
      <el-table v-loading="tableLoading" :data="tableData">
        <el-table-column prop="operatorName" label="操作人" min-width="120">
          <template #default="{ row }">
            <span class="lm-operator">{{ row.operatorName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="操作模块" min-width="140" show-overflow-tooltip />
        <el-table-column prop="operationType" label="操作类型" width="110" align="center">
          <template #default="{ row }">
            <span class="lm-tag lm-tag--subtle">{{ getLabel('OperationType', row.operationType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operationContent" label="操作内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operationResult" label="结果" width="90" align="center">
          <template #default="{ row }">
            <span
              class="lm-dot-label"
              :class="row.operationResult === 'success' ? 'is-success' : 'is-fail'"
            >
              <span class="lm-dot-label__dot"></span>
              {{ row.operationResult === 'success' ? '成功' : '失败' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="错误信息" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.errorMsg" class="lm-error">{{ row.errorMsg }}</span>
            <span v-else class="lm-empty">—</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="operationTime"
          label="操作时间"
          width="170"
          align="center"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span class="lm-time">{{ formatDate(row.operationTime) }}</span>
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
import { getOperationLogPage } from '@/api/log/operation'
import type { OperationLogVO, OperationLogQuery } from '@/api/log/types'
import Pagination from '@/components/Pagination/index.vue'
import { useTable } from '@/composables/useTable'
import { useDict } from '@/composables/useDict'
import { formatDate } from '@/utils'

const { options: operationTypeOptions, getLabel } = useDict('OperationType')

// ==================== 表格 ====================
const dateRange = ref<string[]>([])

const {
  tableData,
  tableLoading,
  total,
  queryParams,
  loadTableData,
} = useTable<OperationLogQuery, OperationLogVO>({
  apiFn: getOperationLogPage,
  defaultQuery: {
    page: 1,
    size: 20,
    operatorName: undefined,
    module: undefined,
    operationType: undefined,
    result: undefined,
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
    operatorName: undefined,
    module: undefined,
    operationType: undefined,
    result: undefined,
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
    width: 170px;
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
  }

  :deep(.el-pagination) {
    padding: 16px 20px;
    display: flex;
    justify-content: flex-end;
    border-top: 1px solid var(--el-border-color-extra-light);
  }
}

/* ─── 表格单元格 ─── */
.lm-operator {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.lm-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 500;

  &--subtle {
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
  }
}

.lm-dot-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;

  &__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  &.is-success {
    color: #059669;

    .lm-dot-label__dot {
      background: #10b981;
      box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
    }
  }

  &.is-fail {
    color: #dc2626;

    .lm-dot-label__dot {
      background: #ef4444;
      box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.2);
    }
  }
}

.lm-error {
  font-size: 13px;
  color: #dc2626;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
}

.lm-empty {
  color: var(--el-text-color-placeholder);
}

.lm-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ─── 暗色模式 ─── */
html.dark {
  .lm-tag--subtle {
    background: rgba(255, 255, 255, 0.06);
  }

  .lm-error {
    color: #f87171;
  }

  .lm-dot-label {
    &.is-success .lm-dot-label__dot {
      box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.15);
    }

    &.is-fail .lm-dot-label__dot {
      box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.15);
    }
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
