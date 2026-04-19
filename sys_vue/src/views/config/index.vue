<template>
  <div class="config-manage">
    <!-- 页面头部 -->
    <div class="cm-header">
      <div class="cm-header__left">
        <h2 class="cm-header__title">系统配置</h2>
        <span class="cm-header__count">共 <em>{{ total }}</em> 条配置</span>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span>新增配置</span>
      </el-button>
    </div>

    <!-- 搜索工具栏 -->
    <div class="cm-toolbar">
      <el-input
        v-model="queryParams.configKey"
        placeholder="搜索配置键..."
        clearable
        :prefix-icon="Search"
        class="cm-toolbar__key"
        @keyup.enter="handleQuery"
      />
      <el-input
        v-model="queryParams.configName"
        placeholder="搜索配置名称..."
        clearable
        class="cm-toolbar__name"
        @keyup.enter="handleQuery"
      />
      <el-button @click="handleQuery">搜索</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 表格区域 -->
    <div class="cm-table-panel">
      <el-table v-loading="tableLoading" :data="tableData">
        <el-table-column prop="configKey" label="配置键" min-width="180">
          <template #default="{ row }">
            <code class="cm-config-key">{{ row.configKey }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="configName" label="配置名称" min-width="140">
          <template #default="{ row }">
            <span class="cm-config-name">{{ row.configName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="cm-config-value">{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="cm-time">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <div class="cm-actions">
              <el-tooltip content="编辑" placement="top">
                <el-button circle size="small" @click="handleEdit(row)">
                  <el-icon :size="14"><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-popconfirm
                :title="`确认删除配置「${row.configName}」？`"
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
        class="cm-form"
      >
        <div class="cm-form__section">
          <div class="cm-form__heading">配置信息</div>
          <el-form-item label="配置键" prop="configKey">
            <el-input
              v-model="formData.configKey"
              placeholder="唯一标识，如 sys.upload.maxSize"
              :disabled="!!formData.id"
            />
          </el-form-item>
          <el-form-item label="配置名称" prop="configName">
            <el-input
              v-model="formData.configName"
              placeholder="显示名称，如 最大上传大小"
            />
          </el-form-item>
        </div>
        <div class="cm-form__section">
          <div class="cm-form__heading">配置内容</div>
          <el-form-item label="配置值" prop="configValue">
            <el-input
              v-model="formData.configValue"
              type="textarea"
              :rows="4"
              placeholder="请输入配置值"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="2"
              placeholder="可选备注说明..."
            />
          </el-form-item>
          <el-form-item label="排序号">
            <el-input-number
              v-model="formData.sort"
              :min="0"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="cm-drawer__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="submitLoading"
            @click="handleSubmit"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import {
  getConfigPage,
  createConfig,
  updateConfig,
  deleteConfig,
} from '@/api/config'
import type { ConfigVO, ConfigForm, ConfigQuery } from '@/api/config/types'
import Pagination from '@/components/Pagination/index.vue'
import { useTable } from '@/composables/useTable'
import { useFormDialog } from '@/composables/useFormDialog'
import { ElMessage } from 'element-plus'
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
} = useTable<ConfigQuery, ConfigVO>({
  apiFn: getConfigPage,
  defaultQuery: {
    page: 1,
    size: 20,
    configKey: undefined,
    configName: undefined,
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
} = useFormDialog<ConfigForm>({
  defaultForm: {
    configKey: '',
    configName: '',
    configValue: '',
    remark: '',
    sort: 0,
  },
  createTitle: '新增配置',
  editTitle: '编辑配置',
  createFn: createConfig,
  updateFn: updateConfig,
  formRules: {
    configKey: [
      { required: true, message: '请输入配置键', trigger: 'blur' },
    ],
    configName: [
      { required: true, message: '请输入配置名称', trigger: 'blur' },
    ],
    configValue: [
      { required: true, message: '请输入配置值', trigger: 'blur' },
    ],
  },
  afterSubmit: loadTableData,
})

// ==================== 删除 ====================
async function handleDeleteRow(row: ConfigVO) {
  try {
    await deleteConfig(row.id)
    ElMessage.success('删除成功')
    loadTableData()
  } catch (e) {
    console.error('删除失败', e)
  }
}
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.config-manage {
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
.cm-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  &__key {
    width: 260px;
  }

  &__name {
    width: 200px;
  }
}

/* ─── 表格面板 ─── */
.cm-table-panel {
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
.cm-config-key {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 2px 8px;
  border-radius: 4px;
}

.cm-config-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.cm-config-value {
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.cm-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ─── 操作按钮 ─── */
.cm-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* ─── 表单 ─── */
.cm-form {
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
.cm-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ─── 暗色模式 ─── */
html.dark {
  .cm-config-key {
    background: rgba(var(--el-color-primary-rgb), 0.12);
  }

  .cm-config-value {
    color: var(--el-text-color-regular);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 768px) {
  .cm-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .cm-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .cm-toolbar__key,
  .cm-toolbar__name {
    width: 100%;
  }
}
</style>
