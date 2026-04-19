<template>
  <div class="role-manage">
    <!-- 页面头部 -->
    <div class="rm-header">
      <div class="rm-header__left">
        <h2 class="rm-header__title">角色管理</h2>
        <span class="rm-header__count">共 <em>{{ total }}</em> 个角色</span>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span>新增角色</span>
      </el-button>
    </div>

    <!-- 搜索工具栏 -->
    <div class="rm-toolbar">
      <el-input
        v-model="queryParams.roleName"
        placeholder="搜索角色名称..."
        clearable
        :prefix-icon="Search"
        class="rm-toolbar__search"
        @keyup.enter="handleQuery"
      />
      <el-select
        v-model="queryParams.status"
        placeholder="全部状态"
        clearable
        class="rm-toolbar__status"
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
    <div class="rm-table-panel">
      <el-table v-loading="tableLoading" :data="tableData">
        <el-table-column prop="roleCode" label="角色编码" min-width="120">
          <template #default="{ row }">
            <code class="rm-role-code">{{ row.roleCode }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="roleName" label="角色名称" min-width="140">
          <template #default="{ row }">
            <span class="rm-role-name">{{ row.roleName }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="roleDesc"
          label="角色描述"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="rm-status">
              <span
                class="rm-status__dot"
                :class="row.status === '1' ? 'is-on' : 'is-off'"
              ></span>
              <span class="rm-status__text">{{
                getStatusLabel('CommonStatus', row.status)
              }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="rm-actions">
              <el-tooltip content="编辑" placement="top">
                <el-button circle size="small" @click="handleEdit(row)">
                  <el-icon :size="14"><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="分配菜单" placement="top">
                <el-button circle size="small" @click="handleAssignMenus(row)">
                  <el-icon :size="14"><Setting /></el-icon>
                </el-button>
              </el-tooltip>
              <el-popconfirm
                :title="`确认删除角色「${row.roleName}」？`"
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
        class="rm-form"
      >
        <div class="rm-form__section">
          <div class="rm-form__heading">基本信息</div>
          <el-form-item label="角色编码" prop="roleCode">
            <el-input
              v-model="formData.roleCode"
              placeholder="唯一标识，如 admin"
              :disabled="!!formData.id"
            />
          </el-form-item>
          <el-form-item label="角色名称" prop="roleName">
            <el-input
              v-model="formData.roleName"
              placeholder="显示名称，如 管理员"
            />
          </el-form-item>
          <el-form-item label="角色描述">
            <el-input
              v-model="formData.roleDesc"
              type="textarea"
              :rows="3"
              placeholder="角色职责描述..."
            />
          </el-form-item>
        </div>
        <div class="rm-form__section">
          <div class="rm-form__heading">其他设置</div>
          <div class="rm-form__inline">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio-button
                  v-for="opt in statusOptions"
                  :key="opt.value"
                  :value="opt.value"
                >{{ opt.label }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="排序号" prop="sort">
              <el-input-number
                v-model="formData.sort"
                :min="0"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="rm-drawer__footer">
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

    <!-- 分配菜单抽屉 -->
    <el-drawer
      v-model="menuDialogVisible"
      title="分配菜单权限"
      direction="rtl"
      size="600"
      :close-on-click-modal="false"
      append-to-body
    >
      <div v-loading="menuDialogLoading" class="rm-menu-assign">
        <p class="rm-menu-assign__hint">
          勾选菜单节点分配给该角色，父节点将自动半选
        </p>
        <el-tree
          ref="menuTreeRef"
          :data="menuTreeData"
          :props="{ children: 'children', label: 'menuName' }"
          node-key="id"
          show-checkbox
          default-expand-all
          :default-checked-keys="checkedMenuIds"
        >
          <template #default="{ data }">
            <span class="rm-menu-node">
              <span
                class="rm-menu-node__icon"
                :class="`rm-menu-node__icon--${typeClass(data.menuType)}`"
              >
                <el-icon v-if="data.icon" :size="14">
                  <component :is="data.icon" />
                </el-icon>
                <template v-else>{{ typeChar(data.menuType) }}</template>
              </span>
              <span class="rm-menu-node__name">{{ data.menuName }}</span>
              <code class="rm-menu-node__code">{{ data.menuCode }}</code>
            </span>
          </template>
        </el-tree>
      </div>
      <template #footer>
        <div class="rm-drawer__footer">
          <el-button @click="menuDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="menuSubmitLoading"
            @click="handleSaveMenus"
          >
            保存分配
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Setting, Search } from '@element-plus/icons-vue'
import {
  getRolePage,
  createRole,
  updateRole,
  deleteRole,
  assignRoleMenus,
  getRoleMenuIds,
} from '@/api/role'
import type { RoleVO, RoleForm, RoleQuery } from '@/api/role/types'
import { getMenuTree } from '@/api/menu'
import type { MenuVO } from '@/api/menu/types'
import Pagination from '@/components/Pagination/index.vue'
import { useTable } from '@/composables/useTable'
import { useFormDialog } from '@/composables/useFormDialog'
import { useDict } from '@/composables/useDict'

const { options: statusOptions, getLabel: getStatusLabel } = useDict('CommonStatus')

// 类型工具
function typeClass(type: string) {
  return type === 'D' ? 'dir' : type === 'M' ? 'menu' : 'btn'
}

function typeChar(type: string) {
  return type === 'D' ? 'D' : type === 'M' ? 'M' : 'B'
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
} = useTable<RoleQuery, RoleVO>({
  apiFn: getRolePage,
  defaultQuery: {
    page: 1,
    size: 20,
    roleName: undefined,
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
} = useFormDialog<RoleForm>({
  defaultForm: { roleCode: '', roleName: '', roleDesc: '', status: '1', sort: 0 },
  createTitle: '新增角色',
  editTitle: '编辑角色',
  createFn: createRole,
  updateFn: updateRole,
  formRules: {
    roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
    roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  },
  afterSubmit: loadTableData,
})

// ==================== 删除 ====================
async function handleDeleteRow(row: RoleVO) {
  try {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadTableData()
  } catch (e) {
    console.error('删除失败', e)
  }
}

// ==================== 分配菜单 ====================
const menuDialogVisible = ref(false)
const menuDialogLoading = ref(false)
const menuSubmitLoading = ref(false)
const menuTreeRef = ref<any>(null)
const menuTreeData = ref<MenuVO[]>([])
const checkedMenuIds = ref<number[]>([])
const currentAssignRoleId = ref<number>(0)

async function handleAssignMenus(row: RoleVO) {
  currentAssignRoleId.value = row.id
  menuDialogLoading.value = true
  menuDialogVisible.value = true
  try {
    const [menuRes, menuIdsRes] = await Promise.all([
      getMenuTree(),
      getRoleMenuIds(row.id),
    ])
    menuTreeData.value = menuRes.data || []
    checkedMenuIds.value = menuIdsRes.data || []
    await nextTick()
  } catch (e) {
    console.error('加载菜单数据失败', e)
  } finally {
    menuDialogLoading.value = false
  }
}

async function handleSaveMenus() {
  menuSubmitLoading.value = true
  try {
    const checkedKeys = menuTreeRef.value?.getCheckedKeys(false) || []
    const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
    const allKeys = [...checkedKeys, ...halfCheckedKeys]
    await assignRoleMenus(currentAssignRoleId.value, allKeys)
    ElMessage.success('菜单分配成功')
    menuDialogVisible.value = false
  } catch (e) {
    console.error('菜单分配失败', e)
  } finally {
    menuSubmitLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.role-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* ─── 头部 ─── */
.rm-header {
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
.rm-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  &__search {
    width: 240px;
  }

  &__status {
    width: 130px;
  }
}

/* ─── 表格面板 ─── */
.rm-table-panel {
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
.rm-role-code {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 4px;
}

.rm-role-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

/* ─── 状态 ─── */
.rm-status {
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
.rm-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* ─── 表单 ─── */
.rm-form {
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

/* ─── 抽屉底部 ─── */
.rm-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ─── 菜单分配 ─── */
.rm-menu-assign {
  &__hint {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin: 0 0 16px;
    padding: 10px 14px;
    background: var(--el-fill-color-light);
    border-radius: 6px;
    border-left: 3px solid var(--el-color-primary);
  }

  :deep(.el-tree) {
    .el-tree-node__content {
      height: auto;
      min-height: 36px;
    }
  }
}

.rm-menu-node {
  display: flex;
  align-items: center;
  gap: 8px;

  &__icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: 5px;
    font-size: 10px;
    font-weight: 700;
    flex-shrink: 0;

    &--dir {
      background: #eef2ff;
      color: #6366f1;
    }

    &--menu {
      background: #ecfdf5;
      color: #059669;
    }

    &--btn {
      background: #fffbeb;
      color: #d97706;
    }
  }

  &__name {
    font-size: 14px;
    color: var(--el-text-color-primary);
  }

  &__code {
    font-size: 11px;
    color: var(--el-text-color-placeholder);
    font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  }
}

/* ─── 暗色模式 ─── */
html.dark {
  .rm-role-code {
    background: rgba(255, 255, 255, 0.06);
  }

  .rm-menu-node__icon--dir {
    background: rgba(99, 102, 241, 0.15);
  }

  .rm-menu-node__icon--menu {
    background: rgba(16, 185, 129, 0.15);
  }

  .rm-menu-node__icon--btn {
    background: rgba(245, 158, 11, 0.15);
  }

  .rm-status__dot.is-on {
    box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.15);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 768px) {
  .rm-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .rm-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .rm-toolbar__search,
  .rm-toolbar__status {
    width: 100%;
  }

  .rm-form__inline {
    grid-template-columns: 1fr;
  }
}
</style>
