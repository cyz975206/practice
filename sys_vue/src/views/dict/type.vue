<template>
  <div class="dict-type-manage">
    <!-- 页面头部 -->
    <div class="dtm-header">
      <div class="dtm-header__left">
        <h2 class="dtm-header__title">字典类型</h2>
        <span class="dtm-header__count">共 <em>{{ total }}</em> 个类型</span>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span>新增类型</span>
      </el-button>
    </div>

    <!-- 搜索工具栏 -->
    <div class="dtm-toolbar">
      <el-input
        v-model="queryParams.dictType"
        placeholder="搜索字典类型..."
        clearable
        :prefix-icon="Search"
        class="dtm-toolbar__type"
        @keyup.enter="handleQuery"
      />
      <el-input
        v-model="queryParams.dictName"
        placeholder="搜索字典名称..."
        clearable
        class="dtm-toolbar__name"
        @keyup.enter="handleQuery"
      />
      <el-select
        v-model="queryParams.status"
        placeholder="全部状态"
        clearable
        class="dtm-toolbar__status"
      >
        <el-option
          v-for="opt in statusOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-button @click="handleQuery">搜索</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 表格区域 -->
    <div class="dtm-table-panel">
      <el-table v-loading="tableLoading" :data="tableData">
        <el-table-column prop="dictType" label="字典类型" min-width="160">
          <template #default="{ row }">
            <code class="dtm-dict-type">{{ row.dictType }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="dictName" label="字典名称" min-width="140">
          <template #default="{ row }">
            <span class="dtm-dict-name">{{ row.dictName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="itemCount" label="字典项数" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="dtm-status">
              <span
                class="dtm-status__dot"
                :class="row.status === '1' ? 'is-on' : 'is-off'"
              ></span>
              <span class="dtm-status__text">{{
                getStatusLabel('CommonStatus', row.status)
              }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="remark"
          label="备注"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="170"
          align="center"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span class="dtm-time">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="dtm-actions">
              <el-tooltip content="编辑" placement="top">
                <el-button circle size="small" @click="handleEdit(row)">
                  <el-icon :size="14"><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="字典项" placement="top">
                <el-button circle size="small" @click="goToItems(row)">
                  <el-icon :size="14"><List /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip
                :content="row.status === '1' ? getStatusLabel('CommonStatus', '0') : getStatusLabel('CommonStatus', '1')"
                placement="top"
              >
                <el-button
                  circle
                  size="small"
                  :type="row.status === '1' ? 'warning' : 'success'"
                  @click="handleToggleStatus(row)"
                >
                  <el-icon :size="14"><Setting /></el-icon>
                </el-button>
              </el-tooltip>
              <el-popconfirm
                :title="`确认删除类型「${row.dictName}」？`"
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
        class="dtm-form"
      >
        <div class="dtm-form__section">
          <div class="dtm-form__heading">基本信息</div>
          <el-form-item label="字典类型" prop="dictType">
            <el-input
              v-model="formData.dictType"
              placeholder="唯一标识，如 sys_user_sex"
              :disabled="!!formData.id"
            />
          </el-form-item>
          <el-form-item label="字典名称" prop="dictName">
            <el-input
              v-model="formData.dictName"
              placeholder="显示名称，如 用户性别"
            />
          </el-form-item>
        </div>
        <div class="dtm-form__section">
          <div class="dtm-form__heading">其他</div>
          <el-form-item label="备注">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              placeholder="可选备注说明..."
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dtm-drawer__footer">
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
import { useRouter } from 'vue-router'
import { Plus, Edit, Delete, Search, List, Setting } from '@element-plus/icons-vue'
import {
  getDictTypePage,
  createDictType,
  updateDictType,
  deleteDictType,
  updateDictTypeStatus,
} from '@/api/dict/type'
import type { DictTypeVO, DictTypeForm, DictTypeQuery } from '@/api/dict/types'
import Pagination from '@/components/Pagination/index.vue'
import { useTable } from '@/composables/useTable'
import { useFormDialog } from '@/composables/useFormDialog'
import { useStatusToggle } from '@/composables/useStatusToggle'
import { useDict } from '@/composables/useDict'
import { ElMessage } from 'element-plus'
import { formatDate } from '@/utils'

const router = useRouter()

const { options: statusOptions, getLabel: getStatusLabel } = useDict('CommonStatus')

// ==================== 表格 ====================
const {
  tableData,
  tableLoading,
  total,
  queryParams,
  loadTableData,
  handleQuery,
  resetQuery,
} = useTable<DictTypeQuery, DictTypeVO>({
  apiFn: getDictTypePage,
  defaultQuery: {
    page: 1,
    size: 20,
    dictType: undefined,
    dictName: undefined,
    status: undefined,
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
} = useFormDialog<DictTypeForm>({
  defaultForm: { dictType: '', dictName: '', remark: '' },
  createTitle: '新增字典类型',
  editTitle: '编辑字典类型',
  createFn: createDictType,
  updateFn: updateDictType,
  formRules: {
    dictType: [
      { required: true, message: '请输入字典类型', trigger: 'blur' },
    ],
    dictName: [
      { required: true, message: '请输入字典名称', trigger: 'blur' },
    ],
  },
  afterSubmit: loadTableData,
})

// ==================== 删除 ====================
async function handleDeleteRow(row: DictTypeVO) {
  try {
    await deleteDictType(row.id)
    ElMessage.success('删除成功')
    loadTableData()
  } catch (e) {
    console.error('删除失败', e)
  }
}

// ==================== 状态切换 ====================
const { handleToggleStatus } = useStatusToggle({
  updateStatusFn: updateDictTypeStatus,
  nameKey: 'dictName',
  entityName: '字典类型',
  labels: { enable: '启用', disable: '禁用' },
  afterToggle: loadTableData,
})

// ==================== 跳转字典项 ====================
function goToItems(row: DictTypeVO) {
  router.push({ path: '/dict/item', query: { dictType: row.dictType } })
}
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.dict-type-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* ─── 头部 ─── */
.dtm-header {
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
.dtm-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  &__type {
    width: 220px;
  }

  &__name {
    width: 200px;
  }

  &__status {
    width: 130px;
  }
}

/* ─── 表格面板 ─── */
.dtm-table-panel {
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
.dtm-dict-type {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 2px 8px;
  border-radius: 4px;
}

.dtm-dict-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.dtm-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ─── 状态 ─── */
.dtm-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;

  &__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;

    &.is-on {
      background: #10b981;
      box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
    }

    &.is-off {
      background: #d1d5db;
    }
  }

  &__text {
    font-size: 13px;
    color: var(--el-text-color-regular);
  }
}

/* ─── 操作按钮 ─── */
.dtm-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* ─── 表单 ─── */
.dtm-form {
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
.dtm-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ─── 暗色模式 ─── */
html.dark {
  .dtm-dict-type {
    background: rgba(var(--el-color-primary-rgb), 0.12);
  }

  .dtm-status__dot.is-on {
    box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.15);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 768px) {
  .dtm-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .dtm-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .dtm-toolbar__type,
  .dtm-toolbar__name,
  .dtm-toolbar__status {
    width: 100%;
  }
}
</style>
