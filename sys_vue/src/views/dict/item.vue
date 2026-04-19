<template>
  <div class="dict-item-manage">
    <!-- 页面头部 -->
    <div class="dim-header">
      <div class="dim-header__left">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="dim-header__divider"></div>
        <h2 class="dim-header__title">字典项管理</h2>
        <code class="dim-header__tag">{{ currentDictType }}</code>
      </div>
      <el-button type="info" @click="handleBatchSort">
        <el-icon><Sort /></el-icon>
        <span>批量排序</span>
      </el-button>
    </div>

    <!-- 搜索工具栏 -->
    <div class="dim-toolbar">
      <el-select
        v-model="queryParams.status"
        placeholder="全部状态"
        clearable
        class="dim-toolbar__status"
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
      <div style="flex: 1"></div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span>新增字典项</span>
      </el-button>
    </div>

    <!-- 表格区域 -->
    <div class="dim-table-panel">
      <el-table v-loading="tableLoading" :data="tableData">
        <el-table-column prop="dictCode" label="字典编码" min-width="140">
          <template #default="{ row }">
            <code class="dim-code">{{ row.dictCode }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="dictLabel" label="字典标签" min-width="140">
          <template #default="{ row }">
            <span class="dim-label">{{ row.dictLabel }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="dictValue" label="字典值" min-width="100">
          <template #default="{ row }">
            <code class="dim-value">{{ row.dictValue }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="dim-status">
              <span
                class="dim-status__dot"
                :class="row.status === '1' ? 'is-on' : 'is-off'"
              ></span>
              <span class="dim-status__text">{{
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
            <span class="dim-time">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="dim-actions">
              <el-tooltip content="编辑" placement="top">
                <el-button circle size="small" @click="handleEdit(row)">
                  <el-icon :size="14"><Edit /></el-icon>
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
                :title="`确认删除字典项「${row.dictLabel}」？`"
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
        class="dim-form"
      >
        <div class="dim-form__section">
          <div class="dim-form__heading">字典项信息</div>
          <el-form-item label="字典编码" prop="dictCode">
            <el-input
              v-model="formData.dictCode"
              placeholder="唯一标识，如 male"
            />
          </el-form-item>
          <el-form-item label="字典标签" prop="dictLabel">
            <el-input
              v-model="formData.dictLabel"
              placeholder="显示文本，如 男"
            />
          </el-form-item>
          <el-form-item label="字典值" prop="dictValue">
            <el-input
              v-model="formData.dictValue"
              placeholder="实际值，如 0"
            />
          </el-form-item>
        </div>
        <div class="dim-form__section">
          <div class="dim-form__heading">其他设置</div>
          <div class="dim-form__inline">
            <el-form-item label="排序号" prop="sort">
              <el-input-number
                v-model="formData.sort"
                :min="0"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio-button
                  v-for="opt in statusOptions"
                  :key="opt.value"
                  :value="opt.value"
                >{{ opt.label }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </div>
          <el-form-item label="备注">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="2"
              placeholder="可选备注说明..."
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dim-drawer__footer">
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

    <!-- 批量排序抽屉 -->
    <el-drawer
      v-model="sortDialogVisible"
      title="批量排序"
      direction="rtl"
      size="520"
      :close-on-click-modal="false"
      append-to-body
    >
      <div class="dim-sort">
        <el-table :data="sortList" max-height="460">
          <el-table-column prop="dictLabel" label="字典标签" min-width="140" />
          <el-table-column prop="dictCode" label="字典编码" min-width="120">
            <template #default="{ row }">
              <code class="dim-sort__code">{{ row.dictCode }}</code>
            </template>
          </el-table-column>
          <el-table-column label="排序" width="130" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.sort"
                :min="0"
                :max="9999"
                size="small"
                controls-position="right"
                style="width: 100%"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <div class="dim-drawer__footer">
          <el-button @click="sortDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="sortLoading" @click="submitBatchSort">
            保存排序
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Search,
  ArrowLeft,
  Sort,
  Setting,
} from '@element-plus/icons-vue'
import {
  getDictItemPage,
  createDictItem,
  updateDictItem,
  deleteDictItem,
  updateDictItemStatus,
  sortDictItem,
} from '@/api/dict/item'
import type { DictItemVO, DictItemForm, DictItemQuery } from '@/api/dict/types'
import Pagination from '@/components/Pagination/index.vue'
import { useTable } from '@/composables/useTable'
import { useFormDialog } from '@/composables/useFormDialog'
import { useStatusToggle } from '@/composables/useStatusToggle'
import { useDict } from '@/composables/useDict'
import { formatDate } from '@/utils'

const route = useRoute()
const router = useRouter()

const currentDictType = ref((route.query.dictType as string) || '')

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
} = useTable<DictItemQuery, DictItemVO>({
  apiFn: (params) => getDictItemPage(params),
  defaultQuery: () => ({
    page: 1,
    size: 20,
    dictType: currentDictType.value,
    status: undefined,
  }),
  immediate: false,
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
} = useFormDialog<DictItemForm>({
  defaultForm: () => ({
    dictType: currentDictType.value,
    dictCode: '',
    dictLabel: '',
    dictValue: '',
    sort: 0,
    remark: '',
    status: '1',
  }),
  createTitle: '新增字典项',
  editTitle: '编辑字典项',
  createFn: createDictItem,
  updateFn: updateDictItem,
  formRules: {
    dictCode: [
      { required: true, message: '请输入字典编码', trigger: 'blur' },
    ],
    dictLabel: [
      { required: true, message: '请输入字典标签', trigger: 'blur' },
    ],
    dictValue: [
      { required: true, message: '请输入字典值', trigger: 'blur' },
    ],
    sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  },
  afterSubmit: loadTableData,
})

// ==================== 删除 ====================
async function handleDeleteRow(row: DictItemVO) {
  try {
    await deleteDictItem(row.id)
    ElMessage.success('删除成功')
    loadTableData()
  } catch (e) {
    console.error('删除失败', e)
  }
}

// ==================== 状态切换 ====================
const { handleToggleStatus } = useStatusToggle({
  updateStatusFn: updateDictItemStatus,
  nameKey: 'dictLabel',
  entityName: '字典项',
  labels: { enable: '启用', disable: '禁用' },
  afterToggle: loadTableData,
})

// ==================== 批量排序 ====================
const sortDialogVisible = ref(false)
const sortLoading = ref(false)
const sortList = ref<
  { id: number; dictCode: string; dictLabel: string; sort: number }[]
>([])

async function handleBatchSort() {
  try {
    const res = await getDictItemPage({
      page: 1,
      size: 9999,
      dictType: currentDictType.value,
    })
    const items = res.data?.content || []
    sortList.value = items.map((item: DictItemVO) => ({
      id: item.id,
      dictCode: item.dictCode,
      dictLabel: item.dictLabel,
      sort: item.sort,
    }))
    sortDialogVisible.value = true
  } catch (e) {
    console.error('加载字典项失败', e)
  }
}

async function submitBatchSort() {
  sortLoading.value = true
  try {
    const ids = sortList.value.map((item) => item.id)
    const sorts = sortList.value.map((item) => item.sort)
    await sortDictItem({ ids, sorts })
    ElMessage.success('排序保存成功')
    sortDialogVisible.value = false
    loadTableData()
  } catch (e) {
    console.error('保存排序失败', e)
  } finally {
    sortLoading.value = false
  }
}

// ==================== 导航 ====================
function goBack() {
  router.push({ path: '/dict/type' })
}

// ==================== 初始化 ====================
onMounted(() => {
  if (!currentDictType.value) {
    ElMessage.error('缺少字典类型参数')
    router.push({ path: '/dict/type' })
    return
  }
  loadTableData()
})
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.dict-item-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* ─── 头部 ─── */
.dim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  &__left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__divider {
    width: 1px;
    height: 20px;
    background: var(--el-border-color);
  }

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    letter-spacing: -0.02em;
  }

  &__tag {
    font-size: 13px;
    font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    padding: 3px 12px;
    border-radius: 4px;
  }
}

/* ─── 工具栏 ─── */
.dim-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  &__status {
    width: 130px;
  }
}

/* ─── 表格面板 ─── */
.dim-table-panel {
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
.dim-code {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 2px 8px;
  border-radius: 4px;
}

.dim-label {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.dim-value {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 4px;
}

.dim-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ─── 状态 ─── */
.dim-status {
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
.dim-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* ─── 表单 ─── */
.dim-form {
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

  &__inline {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0 16px;
  }
}

/* ─── 排序 ─── */
.dim-sort {
  &__code {
    font-size: 12px;
    font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
    color: var(--el-text-color-secondary);
  }
}

/* ─── 抽屉底部 ─── */
.dim-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ─── 暗色模式 ─── */
html.dark {
  .dim-header__tag {
    background: rgba(var(--el-color-primary-rgb), 0.12);
  }

  .dim-code {
    background: rgba(var(--el-color-primary-rgb), 0.12);
  }

  .dim-value {
    background: rgba(255, 255, 255, 0.06);
  }

  .dim-status__dot.is-on {
    box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.15);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 768px) {
  .dim-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .dim-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .dim-toolbar__status {
    width: 100%;
  }

  .dim-form__inline {
    grid-template-columns: 1fr;
  }
}
</style>
