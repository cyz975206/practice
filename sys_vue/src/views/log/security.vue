<template>
  <div class="log-manage">
    <!-- 页面头部 -->
    <div class="lm-header">
      <h2 class="lm-header__title">安全日志</h2>
      <span class="lm-header__desc">安全风险事件与处理记录</span>
    </div>

    <!-- 搜索工具栏 -->
    <div class="lm-toolbar">
      <el-input
        v-model="queryParams.riskType"
        placeholder="搜索风险类型..."
        clearable
        :prefix-icon="Search"
        class="lm-toolbar__input"
        @keyup.enter="handleQuery"
      />
      <el-select
        v-model="queryParams.riskLevel"
        placeholder="全部等级"
        clearable
        class="lm-toolbar__select"
      >
        <el-option
          v-for="item in riskLevelOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-select
        v-model="queryParams.handleStatus"
        placeholder="全部状态"
        clearable
        class="lm-toolbar__select"
      >
        <el-option
          v-for="item in handleStatusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
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
        <el-table-column prop="operatorName" label="操作人" min-width="110">
          <template #default="{ row }">
            <span class="lm-operator">{{ row.operatorName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorIp" label="操作IP" min-width="130">
          <template #default="{ row }">
            <code class="lm-mono">{{ row.operatorIp }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="riskType" label="风险类型" min-width="130">
          <template #default="{ row }">
            <span class="lm-tag lm-tag--subtle">{{ getRiskLabel('RiskType', row.riskType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="100" align="center">
          <template #default="{ row }">
            <span
              class="lm-risk-badge"
              :class="`lm-risk-badge--${riskBadgeClass(row.riskLevel)}`"
            >
              {{ getRiskLabel('RiskLevel', row.riskLevel) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="riskContent" label="风险内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="handleStatus" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <span
              class="lm-handle-badge"
              :class="`lm-handle-badge--${handleBadgeClass(row.handleStatus)}`"
            >
              {{ getHandleLabel('HandleStatus', row.handleStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="occurTime"
          label="发生时间"
          width="170"
          align="center"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span class="lm-time">{{ formatDate(row.occurTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <div class="lm-actions">
              <el-tooltip content="处理" placement="top">
                <el-button circle size="small" type="warning" @click="handleSecurityLog(row)">
                  <el-icon :size="14"><Edit /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
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

    <!-- 处理抽屉 -->
    <el-drawer
      v-model="dialogVisible"
      title="处理安全日志"
      direction="rtl"
      size="420"
      :close-on-click-modal="false"
      append-to-body
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        class="lm-form"
      >
        <el-form-item label="处理状态" prop="handleStatus">
          <el-select
            v-model="formData.handleStatus"
            placeholder="请选择处理状态"
            style="width: 100%"
          >
            <el-option
              v-for="item in handleStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注" prop="handleNote">
          <el-input
            v-model="formData.handleNote"
            type="textarea"
            :rows="5"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="lm-drawer__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Edit } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getSecurityLogPage, handleSecurityLog } from '@/api/log/security'
import type { SecurityLogVO, SecurityLogQuery, SecurityLogHandleForm } from '@/api/log/types'
import Pagination from '@/components/Pagination/index.vue'
import { useTable } from '@/composables/useTable'
import { useDict } from '@/composables/useDict'
import { formatDate } from '@/utils'

const { options: riskLevelOptions, getLabel: getRiskLabel, getTagType: getRiskTagType } = useDict('RiskLevel')
const { options: riskTypeOptions } = useDict('RiskType')
const { options: handleStatusOptions, getLabel: getHandleLabel, getTagType: getHandleTagType } = useDict('HandleStatus')

// 风险等级样式映射（dict_value → CSS class）
const RISK_LEVEL_CSS: Record<string, string> = {
  '1': 'info',
  '2': 'warning',
  '3': 'danger',
}

// 处理状态样式映射（dict_value → CSS class）
const HANDLE_STATUS_CSS: Record<string, string> = {
  '1': 'warning',
  '2': 'success',
  '3': 'info',
}

function riskBadgeClass(level: string) {
  return RISK_LEVEL_CSS[level] || 'info'
}

function handleBadgeClass(status: string) {
  return HANDLE_STATUS_CSS[status] || 'info'
}

// ==================== 表格 ====================
const dateRange = ref<string[]>([])

const {
  tableData,
  tableLoading,
  total,
  queryParams,
  loadTableData,
} = useTable<SecurityLogQuery, SecurityLogVO>({
  apiFn: getSecurityLogPage,
  defaultQuery: {
    page: 1,
    size: 20,
    riskType: undefined,
    riskLevel: undefined,
    handleStatus: undefined,
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
    riskType: undefined,
    riskLevel: undefined,
    handleStatus: undefined,
    startTime: undefined,
    endTime: undefined,
  }
  loadTableData()
}

// ==================== 处理抽屉 ====================
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const currentLogId = ref<number>(0)
const formData = ref<SecurityLogHandleForm>({
  handleStatus: 'handled',
  handleNote: '',
})

const formRules: FormRules = {
  handleStatus: [{ required: true, message: '请选择处理状态', trigger: 'change' }],
}

function handleSecurityLog(row: SecurityLogVO) {
  currentLogId.value = row.id
  formData.value = {
    handleStatus: 'handled',
    handleNote: '',
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    await handleSecurityLog(currentLogId.value, formData.value)
    ElMessage.success('处理成功')
    dialogVisible.value = false
    loadTableData()
  } catch (e) {
    console.error('处理失败', e)
  } finally {
    submitLoading.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
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

.lm-mono {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 4px;
}

.lm-risk-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 500;

  &--info {
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
  }

  &--warning {
    background: #fffbeb;
    color: #d97706;
  }

  &--danger {
    background: #fef2f2;
    color: #dc2626;
  }
}

.lm-handle-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 500;

  &--warning {
    background: #fffbeb;
    color: #d97706;
  }

  &--success {
    background: #ecfdf5;
    color: #059669;
  }

  &--info {
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
  }
}

.lm-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
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

.lm-actions {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ─── 表单 ─── */
.lm-form {
  padding: 0;
}

/* ─── 抽屉底部 ─── */
.lm-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ─── 暗色模式 ─── */
html.dark {
  .lm-tag--subtle {
    background: rgba(255, 255, 255, 0.06);
  }

  .lm-mono {
    background: rgba(255, 255, 255, 0.06);
  }

  .lm-risk-badge {
    &--warning {
      background: rgba(217, 119, 6, 0.12);
    }

    &--danger {
      background: rgba(220, 38, 38, 0.12);
    }
  }

  .lm-handle-badge {
    &--warning {
      background: rgba(217, 119, 6, 0.12);
    }

    &--success {
      background: rgba(16, 185, 129, 0.15);
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
