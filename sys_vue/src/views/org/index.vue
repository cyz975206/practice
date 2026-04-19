<template>
  <div class="org-manage">
    <!-- 页面头部 -->
    <div class="om-header">
      <div class="om-header__left">
        <h2 class="om-header__title">机构管理</h2>
        <span class="om-header__count">共 <em>{{ total }}</em> 个机构</span>
      </div>
      <div class="om-header__actions">
        <el-button type="success" @click="handleExport">
          <el-icon><Download /></el-icon>
          <span>导出</span>
        </el-button>
        <el-button type="warning" @click="handleImport">
          <el-icon><Upload /></el-icon>
          <span>导入</span>
        </el-button>
        <el-button v-hasPerm="['org:add']" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>新增机构</span>
        </el-button>
      </div>
    </div>

    <!-- 主从布局 -->
    <div class="om-layout">
      <!-- 左侧：机构树 -->
      <div class="om-tree-panel">
        <div class="om-tree-panel__bar">
          <el-input
            v-model="treeFilterText"
            placeholder="搜索机构..."
            clearable
            :prefix-icon="Search"
            size="small"
            class="om-tree-panel__search"
            @keyup.enter="handleQueryOrg"
          />
        </div>

        <div v-loading="treeLoading" class="om-tree-panel__body">
          <el-tree
            ref="orgTreeRef"
            :data="orgTreeData"
            :props="{ children: 'children', label: 'orgShortName' }"
            node-key="orgCode"
            highlight-current
            :expand-on-click-node="false"
            default-expand-all
            :filter-node-method="filterNode"
            @node-click="handleNodeClick"
          >
            <template #default="{ data }">
              <div class="om-tree-node">
                <div class="om-tree-node__main">
                  <span class="om-tree-node__name">{{ data.orgShortName }}</span>
                </div>
              </div>
            </template>
          </el-tree>
          <el-empty
            v-if="!treeLoading && orgTreeData.length === 0"
            description="暂无机构数据"
            :image-size="60"
          />
        </div>
      </div>

      <!-- 右侧：机构列表 -->
      <div class="om-table-panel">
        <div class="om-table-panel__bar">
          <div class="om-table-panel__left">
            <span class="om-table-panel__title">机构列表</span>
            <code
              v-if="selectedNode"
              class="om-table-panel__tag"
            >{{ selectedNode.orgShortName }}</code>
          </div>
        </div>

        <!-- 搜索工具栏 -->
        <div class="om-toolbar">
          <el-input
            v-model="queryParams.orgShortName"
            placeholder="搜索机构简称..."
            clearable
            :prefix-icon="Search"
            class="om-toolbar__name"
            @keyup.enter="handleQueryOrg"
          />
          <el-select
            v-model="queryParams.orgLevel"
            placeholder="全部层级"
            clearable
            class="om-toolbar__level"
          >
            <el-option
              v-for="item in orgLevelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-select
            v-model="queryParams.status"
            placeholder="全部状态"
            clearable
            class="om-toolbar__status"
          >
            <el-option
              v-for="opt in commonStatusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <el-button @click="handleQueryOrg">搜索</el-button>
          <el-button @click="resetQueryOrg">重置</el-button>
        </div>

        <!-- 表格 -->
        <el-table v-loading="tableLoading" :data="tableData">
          <el-table-column prop="orgCode" label="机构编码" min-width="120">
            <template #default="{ row }">
              <code class="om-org-code">{{ row.orgCode }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="orgShortName" label="机构简称" min-width="130">
            <template #default="{ row }">
              <span class="om-org-name">{{ row.orgShortName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="orgFullName" label="机构全称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="orgLevel" label="机构层级" width="110" align="center">
            <template #default="{ row }">
              <span
                class="om-level-badge"
                :class="`om-level-badge--${levelClass(row.orgLevel)}`"
              >
                {{ levelLabel(row.orgLevel) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <span class="om-status">
                <span
                  class="om-status__dot"
                  :class="row.status === '1' ? 'is-on' : 'is-off'"
                ></span>
                <span class="om-status__text">{{
                  getCommonStatusLabel('CommonStatus', row.status)
                }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="70" align="center" />
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="{ row }">
              <div class="om-actions">
                <el-tooltip content="编辑" placement="top">
                  <el-button circle size="small" @click="handleOrgEdit(row)">
                    <el-icon :size="14"><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip
                  :content="row.status === '1' ? getCommonStatusLabel('CommonStatus', '0') : getCommonStatusLabel('CommonStatus', '1')"
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
                  :title="`确认删除机构「${row.orgShortName}」？`"
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
    </div>

    <!-- 新增/编辑抽屉 -->
    <el-drawer
      v-model="dialogVisible"
      :title="dialogTitle"
      direction="rtl"
      size="520"
      :close-on-click-modal="false"
      append-to-body
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        class="om-form"
      >
        <div class="om-form__section">
          <div class="om-form__heading">基本信息</div>
          <el-form-item label="机构编码" prop="orgCode">
            <el-input
              v-model="formData.orgCode"
              placeholder="唯一标识，如 ORG001"
              :disabled="!!formData.id"
            />
          </el-form-item>
          <div class="om-form__inline">
            <el-form-item label="机构简称" prop="orgShortName">
              <el-input
                v-model="formData.orgShortName"
                placeholder="如 总行"
              />
            </el-form-item>
            <el-form-item label="机构全称" prop="orgFullName">
              <el-input
                v-model="formData.orgFullName"
                placeholder="如 总行营业部"
              />
            </el-form-item>
          </div>
        </div>
        <div class="om-form__section">
          <div class="om-form__heading">组织设置</div>
          <div class="om-form__inline">
            <el-form-item label="机构层级" prop="orgLevel">
              <el-select
                v-model="formData.orgLevel"
                placeholder="请选择层级"
                style="width: 100%"
              >
                <el-option
                  v-for="item in orgLevelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
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
          <el-form-item label="上级机构" prop="parentOrgCode">
            <el-tree-select
              v-model="formData.parentOrgCode"
              :data="orgTreeData"
              :props="{
                children: 'children',
                label: 'orgShortName',
                value: 'orgCode',
              }"
              node-key="orgCode"
              check-strictly
              :render-after-expand="false"
              placeholder="不选则为根机构"
              clearable
              style="width: 100%"
            />
          </el-form-item>
        </div>
        <div class="om-form__section">
          <div class="om-form__heading">其他设置</div>
          <div class="om-form__inline">
            <el-form-item label="负责人ID" prop="leaderUserId">
              <el-input-number
                v-model="formData.leaderUserId"
                :min="0"
                placeholder="用户ID"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio-button
                  v-for="opt in commonStatusOptions"
                  :key="opt.value"
                  :value="opt.value"
                >{{ opt.label }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="om-drawer__footer">
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

    <!-- 导入抽屉 -->
    <el-drawer
      v-model="importDialogVisible"
      title="导入机构"
      direction="rtl"
      size="440"
      :close-on-click-modal="false"
      append-to-body
    >
      <div class="om-import">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-exceed="handleExceed"
          :http-request="() => {}"
          drag
          class="om-import__upload"
        >
          <el-icon class="el-icon--upload"><Upload /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">仅支持 .xlsx / .xls 文件</div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <div class="om-drawer__footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="importLoading"
            @click="submitImport"
          >
            确认导入
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Plus, Edit, Delete, Download, Upload, Search, Setting } from '@element-plus/icons-vue'
import {
  getOrgTree,
  getOrgPage,
  createOrg,
  updateOrg,
  deleteOrg,
  updateOrgStatus,
  exportOrg,
  importOrg,
} from '@/api/org'
import type { OrgVO, OrgForm, OrgQuery } from '@/api/org/types'
import Pagination from '@/components/Pagination/index.vue'
import { useTable } from '@/composables/useTable'
import { useFormDialog } from '@/composables/useFormDialog'
import { useStatusToggle } from '@/composables/useStatusToggle'
import { useExportImport } from '@/composables/useExportImport'
import { useDict } from '@/composables/useDict'

// 机构层级：通过字典获取标签，样式映射保留本地
const ORG_LEVEL_CSS_MAP: Record<string, string> = {
  '1': 'head',
  '2': 'branch',
  '3': 'sub-branch',
  '4': 'dept',
}

const { options: orgLevelOptions, getLabel: getOrgLevelLabel } = useDict('OrgLevel')
const { options: commonStatusOptions, getLabel: getCommonStatusLabel } = useDict('CommonStatus')

function levelLabel(level: string) {
  return getOrgLevelLabel('OrgLevel', level)
}

function levelClass(level: string) {
  return ORG_LEVEL_CSS_MAP[level] ?? 'dept'
}

// ==================== 机构树 ====================
const orgTreeRef = ref<any>(null)
const orgTreeData = ref<OrgVO[]>([])
const treeLoading = ref(false)
const treeFilterText = ref('')
const selectedNode = ref<OrgVO | null>(null)

function filterNode(value: string, data: OrgVO) {
  if (!value) return true
  return data.orgShortName.includes(value)
}

watch(treeFilterText, (val) => {
  orgTreeRef.value?.filter(val)
})

async function loadOrgTree() {
  treeLoading.value = true
  try {
    const res = await getOrgTree()
    orgTreeData.value = res.data || []
  } catch (e) {
    console.error('加载机构树失败', e)
  } finally {
    treeLoading.value = false
  }
}

function handleNodeClick(data: OrgVO) {
  selectedNode.value = data
  queryParams.value.parentOrgCode = data.orgCode
  queryParams.value.page = 1
  loadTableData()
}

// ==================== 表格 ====================
const {
  tableData,
  tableLoading,
  total,
  queryParams,
  loadTableData,
  handleQuery,
  resetQuery,
} = useTable<OrgQuery, OrgVO>({
  apiFn: getOrgPage,
  defaultQuery: {
    page: 1,
    size: 20,
    orgShortName: undefined,
    orgLevel: undefined,
    status: undefined,
    parentOrgCode: undefined,
  },
  immediate: false,
})

function handleQueryOrg() {
  handleQuery()
}

function resetQueryOrg() {
  resetQuery({ parentOrgCode: selectedNode.value?.orgCode })
}

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
} = useFormDialog<OrgForm>({
  defaultForm: {
    orgCode: '',
    orgShortName: '',
    orgFullName: '',
    orgLevel: 'DEPARTMENT',
    parentOrgCode: undefined,
    leaderUserId: undefined,
    status: '1',
    sort: 0,
  },
  createTitle: '新增机构',
  editTitle: '编辑机构',
  createFn: createOrg,
  updateFn: updateOrg,
  formRules: {
    orgCode: [{ required: true, message: '请输入机构编码', trigger: 'blur' }],
    orgShortName: [
      { required: true, message: '请输入机构简称', trigger: 'blur' },
    ],
    orgFullName: [
      { required: true, message: '请输入机构全称', trigger: 'blur' },
    ],
    orgLevel: [
      { required: true, message: '请选择机构层级', trigger: 'change' },
    ],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  },
  afterSubmit: () => {
    loadTableData()
    loadOrgTree()
  },
})

// ==================== 编辑 ====================
function handleOrgEdit(row: OrgVO) {
  handleEdit(row, (row) => ({
    ...row,
    orgLevel: row.orgLevel,
  }))
}

// ==================== 删除 ====================
async function handleDeleteRow(row: OrgVO) {
  try {
    await deleteOrg(row.id)
    loadTableData()
    loadOrgTree()
    if (selectedNode.value?.orgCode === row.orgCode) {
      selectedNode.value = null
    }
  } catch (e) {
    console.error('删除失败', e)
  }
}

// ==================== 状态切换 ====================
const { handleToggleStatus } = useStatusToggle({
  updateStatusFn: updateOrgStatus,
  nameKey: 'orgShortName',
  entityName: '机构',
  enabledValue: '1',
  disabledValue: '0',
  labels: { enable: '启用', disable: '禁用' },
  afterToggle: () => {
    loadTableData()
    loadOrgTree()
  },
})

// ==================== 导出导入 ====================
const {
  importDialogVisible,
  importLoading,
  uploadRef,
  handleExport,
  handleImport,
  handleExceed,
  submitImport,
} = useExportImport<OrgQuery>({
  entityName: '机构',
  exportFn: exportOrg,
  importFn: importOrg,
  queryParams,
  afterImport: () => {
    loadTableData()
    loadOrgTree()
  },
})

// ==================== 初始化 ====================
onMounted(() => {
  loadOrgTree()
  loadTableData()
})
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.org-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* ─── 头部 ─── */
.om-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;

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

  &__actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

/* ─── 主从布局 ─── */
.om-layout {
  display: grid;
  grid-template-columns: 340px 1fr;
  grid-template-rows: 1fr;
  gap: 16px;
  height: calc(100vh - 200px);
  flex-shrink: 0;
}

/* ─── 左侧：机构树面板 ─── */
.om-tree-panel {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &__bar {
    padding: 12px;
    border-bottom: 1px solid var(--el-border-color-extra-light);
    flex-shrink: 0;
  }

  &__search {
    width: 100%;
  }

  &__body {
    flex: 1;
    overflow-y: auto;
    padding: 4px 8px;
    min-height: 0;
  }

  :deep(.el-tree) {
    background: transparent;

    .el-tree-node__content {
      height: auto;
      min-height: 36px;
      padding: 2px 4px;
      border-radius: 6px;
      transition: background 0.15s;
    }

    .el-tree-node__content:hover {
      background: transparent;
    }

    .is-current > .el-tree-node__content {
      background: transparent;
    }

    .el-tree-node__expand-icon {
      padding: 6px 2px;
    }
  }
}

/* 机构树节点 */
.om-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 6px 10px;
  border-radius: 6px;
  border-left: 3px solid var(--el-border-color-light);
  transition: all 0.15s ease;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.is-off {
    opacity: 0.5;
  }

  &--head {
    border-left-color: #ef4444;
  }
  &--branch {
    border-left-color: #f59e0b;
  }
  &--sub-branch {
    border-left-color: #3b82f6;
  }
  &--dept {
    border-left-color: #8b5cf6;
  }

  &__main {
    display: flex;
    align-items: center;
    gap: 6px;
    min-width: 0;
  }

  &__level {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    border-radius: 4px;
    font-size: 9px;
    font-weight: 700;
    flex-shrink: 0;

    &--head {
      background: #fef2f2;
      color: #ef4444;
    }
    &--branch {
      background: #fffbeb;
      color: #f59e0b;
    }
    &--sub-branch {
      background: #eff6ff;
      color: #3b82f6;
    }
    &--dept {
      background: #f5f3ff;
      color: #8b5cf6;
    }
  }

  &__name {
    font-size: 13px;
    color: var(--el-text-color-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;

    &.is-on {
      background: #10b981;
      box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
    }

    &.is-off {
      background: #d1d5db;
    }
  }
}

/* ─── 右侧：表格面板 ─── */
.om-table-panel {
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

  &__tag {
    font-size: 12px;
    font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    padding: 2px 10px;
    border-radius: 4px;
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

  :deep(.el-pagination) {
    padding: 10px 20px;
    display: flex;
    justify-content: flex-end;
    border-top: 1px solid var(--el-border-color-extra-light);
  }
}

/* ─── 工具栏 ─── */
.om-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding: 0 16px 12px;
  border-bottom: 1px solid var(--el-border-color-extra-light);

  &__name {
    width: 180px;
  }

  &__level {
    width: 130px;
  }

  &__status {
    width: 120px;
  }
}

/* ─── 表格单元格 ─── */
.om-org-code {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 4px;
}

.om-org-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

/* 层级标签 */
.om-level-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 500;

  &--head {
    background: #fef2f2;
    color: #ef4444;
  }
  &--branch {
    background: #fffbeb;
    color: #d97706;
  }
  &--sub-branch {
    background: #eff6ff;
    color: #2563eb;
  }
  &--dept {
    background: #f5f3ff;
    color: #7c3aed;
  }
}

/* 状态 */
.om-status {
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
.om-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* ─── 表单 ─── */
.om-form {
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

/* ─── 导入 ─── */
.om-import {
  &__upload {
    :deep(.el-upload-dragger) {
      border-radius: 8px;
      padding: 32px 20px;
    }
  }
}

/* ─── 抽屉底部 ─── */
.om-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ─── 暗色模式 ─── */
html.dark {
  .om-tree-node {
    &--head &__level {
      background: rgba(239, 68, 68, 0.15);
    }
    &--branch &__level {
      background: rgba(245, 158, 11, 0.15);
    }
    &--sub-branch &__level {
      background: rgba(59, 130, 246, 0.15);
    }
    &--dept &__level {
      background: rgba(139, 92, 246, 0.15);
    }
  }

  .om-level-badge {
    &--head {
      background: rgba(239, 68, 68, 0.15);
    }
    &--branch {
      background: rgba(245, 158, 11, 0.15);
    }
    &--sub-branch {
      background: rgba(59, 130, 246, 0.15);
    }
    &--dept {
      background: rgba(139, 92, 246, 0.15);
    }
  }

  .om-org-code {
    background: rgba(255, 255, 255, 0.06);
  }

  .om-table-panel__tag {
    background: rgba(var(--el-color-primary-rgb), 0.12);
  }

  .om-status__dot.is-on {
    box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.15);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 900px) {
  .om-layout {
    grid-template-columns: 1fr;
  }

  .om-tree-panel {
    max-height: 280px;
  }

  .om-form__inline {
    grid-template-columns: 1fr;
  }
}
</style>
