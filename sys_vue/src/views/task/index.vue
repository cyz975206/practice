<template>
  <div class="task-manage">
    <!-- 页面头部 -->
    <div class="tm-header">
      <div class="tm-header__left">
        <h2 class="tm-header__title">定时任务</h2>
        <span class="tm-header__count">共 <em>{{ total }}</em> 个任务</span>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span>新增任务</span>
      </el-button>
    </div>

    <!-- 搜索工具栏 -->
    <div class="tm-toolbar">
      <el-input
        v-model="queryParams.name"
        placeholder="搜索任务名称..."
        clearable
        :prefix-icon="Search"
        class="tm-toolbar__name"
        @keyup.enter="handleQuery"
      />
      <el-select
        v-model="queryParams.hasStart"
        placeholder="任务状态"
        clearable
        class="tm-toolbar__status"
      >
        <el-option label="已启用" :value="true" />
        <el-option label="已停用" :value="false" />
      </el-select>
      <el-button @click="handleQuery">搜索</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 表格区域 -->
    <div class="tm-table-panel">
      <el-table v-loading="tableLoading" :data="tableData">
        <el-table-column prop="name" label="任务名称" min-width="160">
          <template #default="{ row }">
            <span class="tm-task-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="serviceName" label="服务名称" min-width="140">
          <template #default="{ row }">
            <code class="tm-service-name">{{ row.serviceName }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="funPath" label="方法路径" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="tm-fun-path">{{ row.funPath }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cron" label="Cron表达式" min-width="140">
          <template #default="{ row }">
            <code class="tm-cron">{{ row.cron }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="hasStart" label="状态" width="90" align="center">
          <template #default="{ row }">
            <span class="tm-status" :class="row.hasStart ? 'tm-status--running' : 'tm-status--stopped'">
              <span class="tm-status__dot" />
              {{ row.hasStart ? '运行中' : '已停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="tm-time">{{ formatDate(row.updateTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="tm-actions">
              <el-tooltip content="编辑" placement="top">
                <el-button circle size="small" @click="handleEdit(row)">
                  <el-icon :size="14"><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip :content="row.hasStart ? '停用' : '启用'" placement="top">
                <el-button
                  circle
                  size="small"
                  :type="row.hasStart ? 'warning' : 'success'"
                  :loading="toggleLoading === row.id"
                  @click="handleToggle(row)"
                >
                  <el-icon :size="14">
                    <VideoPause v-if="row.hasStart" />
                    <VideoPlay v-else />
                  </el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="执行一次" placement="top">
                <el-button
                  circle
                  size="small"
                  type="info"
                  :loading="triggerLoading === row.id"
                  @click="handleTrigger(row)"
                >
                  <el-icon :size="14"><CaretRight /></el-icon>
                </el-button>
              </el-tooltip>
              <el-popconfirm
                :title="`确认删除任务「${row.name}」？`"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="handleDeleteRow(row)"
              >
                <template #reference>
                  <el-button circle size="small" type="danger">
                    <el-icon :size="14"><Delete /></el-icon>
                  </el-button>
                </template>
              </el-popconfirm>
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

    <!-- 新增/编辑抽屉 -->
    <el-drawer
      v-model="dialogVisible"
      :title="dialogTitle"
      direction="rtl"
      size="480"
      :close-on-click-modal="false"
      append-to-body
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        class="tm-form"
      >
        <div class="tm-form__section">
          <div class="tm-form__heading">基本信息</div>
          <el-form-item label="任务名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入任务名称" />
          </el-form-item>
          <el-form-item label="服务名称" prop="serviceName">
            <el-input
              v-model="formData.serviceName"
              placeholder="标识所属服务实例"
              :disabled="!!formData.id"
            />
          </el-form-item>
        </div>
        <div class="tm-form__section">
          <div class="tm-form__heading">调度配置</div>
          <el-form-item label="方法路径" prop="funPath">
            <el-input v-model="formData.funPath" placeholder="格式: beanName.methodName" />
          </el-form-item>
          <el-form-item label="Cron表达式" prop="cron">
            <el-input v-model="formData.cron" placeholder="如: 0 0 2 * * ?" />
          </el-form-item>
          <el-form-item label="是否启用">
            <el-switch v-model="formData.hasStart" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="tm-drawer__footer">
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
import {
  Plus, Edit, Delete, Search,
  VideoPlay, VideoPause, CaretRight,
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTaskPage,
  createTask,
  updateTask,
  deleteTask,
  startTask,
  stopTask,
  triggerTask,
} from '@/api/task'
import type { TaskVO, TaskForm, TaskQuery } from '@/api/task/types'
import Pagination from '@/components/Pagination/index.vue'
import { useTable } from '@/composables/useTable'
import { useFormDialog } from '@/composables/useFormDialog'
import { formatDate } from '@/utils'

// ==================== 表格 ====================
const {
  tableData,
  tableLoading,
  total,
  queryParams,
  loadTableData,
  handleQuery,
  resetQuery,
} = useTable<TaskQuery, TaskVO>({
  apiFn: getTaskPage,
  defaultQuery: {
    page: 1,
    size: 20,
    name: undefined,
    hasStart: undefined,
  },
})

// ==================== 表单抽屉 ====================
const {
  dialogVisible,
  dialogTitle,
  submitLoading,
  formRef,
  formData,
  formRules,
  handleAdd,
  handleEdit,
  handleSubmit,
  resetForm,
} = useFormDialog<TaskForm>({
  defaultForm: {
    name: '',
    serviceName: '',
    funPath: '',
    cron: '',
    hasStart: false,
  },
  createTitle: '新增任务',
  editTitle: '编辑任务',
  createFn: createTask,
  updateFn: updateTask,
  formRules: {
    name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
    serviceName: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
    funPath: [{ required: true, message: '请输入方法路径', trigger: 'blur' }],
    cron: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }],
  },
  afterSubmit: loadTableData,
})

// ==================== 启用/停用 ====================
const toggleLoading = ref<number | null>(null)

async function handleToggle(row: TaskVO) {
  const action = row.hasStart ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确认${action}任务「${row.name}」？`,
      '提示',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' },
    )
    toggleLoading.value = row.id
    if (row.hasStart) {
      await stopTask(row.id)
    } else {
      await startTask(row.id)
    }
    ElMessage.success(`${action}成功`)
    loadTableData()
  } catch {
    // 用户取消或请求失败
  } finally {
    toggleLoading.value = null
  }
}

// ==================== 手动触发 ====================
const triggerLoading = ref<number | null>(null)

async function handleTrigger(row: TaskVO) {
  try {
    await ElMessageBox.confirm(
      `确认立即执行任务「${row.name}」？`,
      '提示',
      { type: 'info', confirmButtonText: '确定', cancelButtonText: '取消' },
    )
    triggerLoading.value = row.id
    await triggerTask(row.id)
    ElMessage.success('已触发执行')
  } catch {
    // 用户取消或请求失败
  } finally {
    triggerLoading.value = null
  }
}

// ==================== 删除 ====================
async function handleDeleteRow(row: TaskVO) {
  try {
    await deleteTask(row.id)
    ElMessage.success('删除成功')
    loadTableData()
  } catch (e) {
    console.error('删除失败', e)
  }
}
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.task-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* ─── 头部 ─── */
.tm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  &__left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    letter-spacing: -0.02em;
  }

  &__count {
    font-size: 13px;
    color: var(--el-text-color-regular);
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color-extra-light);
    padding: 3px 12px;
    border-radius: 100px;

    em {
      font-style: normal;
      font-weight: 600;
      color: var(--el-color-primary);
    }
  }
}

/* ─── 工具栏 ─── */
.tm-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  &__name {
    width: 260px;
  }

  &__status {
    width: 140px;
  }
}

/* ─── 表格面板 ─── */
.tm-table-panel {
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
.tm-task-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.tm-service-name {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 2px 8px;
  border-radius: 4px;
}

.tm-fun-path {
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.tm-cron {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 4px;
}

.tm-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ─── 状态指示 ─── */
.tm-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;

  &__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  &--running {
    color: #10b981;

    .tm-status__dot {
      background: #10b981;
      box-shadow: 0 0 6px rgba(16, 185, 129, 0.4);
    }
  }

  &--stopped {
    color: var(--el-text-color-secondary);

    .tm-status__dot {
      background: var(--el-text-color-disabled);
    }
  }
}

/* ─── 操作按钮 ─── */
.tm-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* ─── 表单 ─── */
.tm-form {
  &__section {
    padding-bottom: 24px;
    margin-bottom: 24px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &:last-child {
      border-bottom: none;
      margin-bottom: 0;
    }
  }

  &__heading {
    font-size: 12px;
    font-weight: 600;
    color: var(--el-text-color-secondary);
    text-transform: uppercase;
    letter-spacing: 0.08em;
    margin-bottom: 16px;
  }
}

/* ─── 抽屉底部 ─── */
.tm-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ─── 暗色模式 ─── */
html.dark {
  .tm-service-name {
    background: rgba(var(--el-color-primary-rgb), 0.12);
  }

  .tm-cron {
    background: rgba(255, 255, 255, 0.06);
  }

  .tm-status--running .tm-status__dot {
    box-shadow: 0 0 6px rgba(16, 185, 129, 0.3);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 768px) {
  .tm-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .tm-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .tm-toolbar__name,
  .tm-toolbar__status {
    width: 100%;
  }
}
</style>
