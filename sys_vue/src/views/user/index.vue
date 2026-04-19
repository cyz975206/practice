<template>
  <div class="user-manage">
    <!-- 页面头部 -->
    <div class="um-header">
      <div class="um-header__left">
        <h2 class="um-header__title">用户管理</h2>
        <span class="um-header__count">共 <em>{{ total }}</em> 个用户</span>
      </div>
      <div class="um-header__actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>新增用户</span>
        </el-button>
      </div>
    </div>

    <!-- 搜索工具栏 -->
    <div class="um-toolbar">
      <el-input
        v-model="queryParams.username"
        placeholder="搜索用户名..."
        clearable
        :prefix-icon="Search"
        class="um-toolbar__username"
        @keyup.enter="handleQuery"
      />
      <el-input
        v-model="queryParams.nickname"
        placeholder="搜索昵称..."
        clearable
        class="um-toolbar__nickname"
        @keyup.enter="handleQuery"
      />
      <el-tree-select
        v-model="queryParams.orgCode"
        :data="orgTreeData"
        :props="{
          children: 'children',
          label: 'orgShortName',
          value: 'orgCode',
        }"
        node-key="orgCode"
        check-strictly
        :render-after-expand="false"
        placeholder="全部机构"
        clearable
        class="um-toolbar__org"
      />
      <el-select
        v-model="queryParams.status"
        placeholder="全部状态"
        clearable
        class="um-toolbar__status"
      >
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button @click="handleQuery">搜索</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 表格区域 -->
    <div class="um-table-panel">
      <el-table v-loading="tableLoading" :data="tableData">
        <el-table-column prop="username" label="用户名" min-width="130">
          <template #default="{ row }">
            <div class="um-user-cell">
              <span class="um-user-name">{{ row.username }}</span>
              <span
                class="um-status-badge"
                :class="`um-status-badge--${userStatusTagType(row.status)}`"
              >
                {{ userStatusLabel(row.status) }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
        <el-table-column
          prop="orgCode"
          label="所属机构"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column prop="roleNames" label="角色" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="um-roles">
              <span
                v-for="name in (row.roleNames || []).slice(0, 2)"
                :key="name"
                class="um-role-tag"
              >{{ name }}</span>
              <span
                v-if="(row.roleNames || []).length > 2"
                class="um-role-more"
              >+{{ row.roleNames.length - 2 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="lastLoginTime"
          label="最后登录"
          width="170"
          align="center"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span class="um-time">{{ formatDate(row.lastLoginTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="loginFailCount" label="失败次数" width="90" align="center">
          <template #default="{ row }">
            <span
              class="um-fail-count"
              :class="{ 'is-fail': row.loginFailCount > 0 }"
            >{{ row.loginFailCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="um-actions">
              <el-tooltip content="编辑" placement="top">
                <el-button circle size="small" @click="handleEdit(row)">
                  <el-icon :size="14"><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="分配角色" placement="top">
                <el-button circle size="small" type="primary" @click="handleAssignRoles(row)">
                  <el-icon :size="14"><UserFilled /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="重置密码" placement="top">
                <el-button circle size="small" type="warning" @click="handleResetPassword(row)">
                  <el-icon :size="14"><Key /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip
                v-if="row.status === '2'"
                content="解锁"
                placement="top"
              >
                <el-button circle size="small" type="success" @click="handleUnlock(row)">
                  <el-icon :size="14"><Unlock /></el-icon>
                </el-button>
              </el-tooltip>
              <el-popconfirm
                :title="`确认删除用户「${row.username}」？`"
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
        class="um-form"
      >
        <div class="um-form__section">
          <div class="um-form__heading">账号信息</div>
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="formData.username"
              placeholder="请输入用户名"
              :disabled="!!formData.id"
            />
          </el-form-item>
          <el-form-item v-if="!formData.id" label="密码" prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              show-password
              placeholder="请输入密码"
            />
          </el-form-item>
        </div>
        <div class="um-form__section">
          <div class="um-form__heading">个人信息</div>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="formData.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="所属机构" prop="orgCode">
            <el-tree-select
              v-model="formData.orgCode"
              :data="orgTreeData"
              :props="{
                children: 'children',
                label: 'orgShortName',
                value: 'orgCode',
              }"
              node-key="orgCode"
              check-strictly
              :render-after-expand="false"
              placeholder="请选择所属机构"
              clearable
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select
              v-model="formData.status"
              placeholder="请选择状态"
              style="width: 100%"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="um-drawer__footer">
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

    <!-- 重置密码弹窗 -->
    <el-dialog
      v-model="resetPwdVisible"
      :title="`重置密码 — ${resetPwdForm.username}`"
      width="440"
      :close-on-click-modal="false"
      destroy-on-close
      @close="closeResetPassword"
    >
      <el-form label-position="top" class="um-reset-pwd-form">
        <el-form-item label="新密码">
          <el-input
            v-model="resetPwdForm.password"
            :type="resetPwdForm.isReset ? 'text' : 'password'"
            show-password
            :disabled="resetPwdForm.isReset"
            :readonly="resetPwdForm.isReset"
            maxlength="20"
            placeholder="请输入新密码"
          />
          <div v-if="resetPwdForm.isReset" class="um-reset-pwd-tip">
            密码已重置，新密码已自动复制到剪贴板
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="um-drawer__footer">
          <el-button @click="closeResetPassword">{{ resetPwdForm.isReset ? '确定' : '取消' }}</el-button>
          <el-button
            v-if="!resetPwdForm.isReset"
            type="warning"
            :loading="resetPwdLoading"
            @click="handleRandomPassword"
          >
            随机生成密码
          </el-button>
          <el-button
            v-if="!resetPwdForm.isReset"
            type="primary"
            :loading="resetPwdLoading"
            @click="handleResetConfirm"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分配角色抽屉 -->
    <el-drawer
      v-model="roleDialogVisible"
      title="分配角色"
      direction="rtl"
      size="420"
      :close-on-click-modal="false"
      append-to-body
    >
      <div v-loading="roleDialogLoading" class="um-role-assign">
        <el-tree
          ref="roleTreeRef"
          :data="roleTreeData"
          :props="{ children: 'children', label: 'roleName' }"
          node-key="id"
          show-checkbox
          default-expand-all
          :default-checked-keys="checkedRoleIds"
        />
      </div>
      <template #footer>
        <div class="um-drawer__footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="roleSubmitLoading"
            @click="handleSaveRoles"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Search,
  UserFilled,
  Key,
  Unlock,
} from '@element-plus/icons-vue'
import {
  getUserPage,
  createUser,
  updateUser,
  deleteUser,
  resetPassword,
  unlockUser,
  assignUserRoles,
} from '@/api/user'
import type { UserVO, UserForm, UserQuery } from '@/api/user/types'
import { getRolePage } from '@/api/role'
import type { RoleVO } from '@/api/role/types'
import { getOrgTree } from '@/api/org'
import type { OrgVO } from '@/api/org/types'
import Pagination from '@/components/Pagination/index.vue'
import { useTable } from '@/composables/useTable'
import { useFormDialog } from '@/composables/useFormDialog'
import { useDict } from '@/composables/useDict'
import { USER_STATUS_TAG_MAP } from '@/constants/tagTypes'
import { formatDate } from '@/utils'

const { options: statusOptions, getLabel: getDictLabel } = useDict('UserStatus')

// 用户状态标签与样式
function userStatusLabel(status: string) {
  return getDictLabel('UserStatus', status)
}

function userStatusTagType(status: string) {
  return USER_STATUS_TAG_MAP[status] ?? 'info'
}

// ==================== 机构树 ====================
const orgTreeData = ref<OrgVO[]>([])

async function loadOrgTree() {
  try {
    const res = await getOrgTree()
    orgTreeData.value = res.data || []
  } catch (e) {
    console.error('加载机构树失败', e)
  }
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
} = useTable<UserQuery, UserVO>({
  apiFn: getUserPage,
  defaultQuery: {
    page: 1,
    size: 20,
    username: undefined,
    nickname: undefined,
    orgCode: undefined,
    status: undefined,
  },
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
} = useFormDialog<UserForm>({
  defaultForm: {
    username: '',
    password: '',
    nickname: '',
    orgCode: '',
    status: '1',
  },
  createTitle: '新增用户',
  editTitle: '编辑用户',
  createFn: createUser,
  updateFn: updateUser,
  formRules: {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
    orgCode: [{ required: true, message: '请选择所属机构', trigger: 'change' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  },
  afterSubmit: loadTableData,
})

// ==================== 删除 ====================
async function handleDeleteRow(row: UserVO) {
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadTableData()
  } catch (e) {
    console.error('删除失败', e)
  }
}

// ==================== 重置密码 ====================
const resetPwdVisible = ref(false)
const resetPwdLoading = ref(false)
const resetPwdForm = reactive({
  userId: 0,
  username: '',
  password: '',
  isReset: false,
})

function openResetPassword(row: UserVO) {
  resetPwdForm.userId = row.id
  resetPwdForm.username = row.username
  resetPwdForm.password = ''
  resetPwdForm.isReset = false
  resetPwdVisible.value = true
}

function closeResetPassword() {
  resetPwdVisible.value = false
}

async function copyToClipboard(text: string) {
  try {
    await navigator.clipboard.writeText(text)
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }
}

async function confirmAndReset(data?: { newPassword: string }) {
  try {
    await ElMessageBox.confirm(
      `将重置用户「${resetPwdForm.username}」的密码，是否确认？`,
      '重置密码',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  resetPwdLoading.value = true
  try {
    const res = await resetPassword(resetPwdForm.userId, data)
    const newPwd = res.data as string
    resetPwdForm.password = newPwd
    resetPwdForm.isReset = true
    await copyToClipboard(newPwd)
    ElMessage.success('重置成功，已复制新密码')
  } catch {
    // error handled by request interceptor
  } finally {
    resetPwdLoading.value = false
  }
}

function handleRandomPassword() {
  confirmAndReset()
}

function handleResetConfirm() {
  if (!resetPwdForm.password) {
    ElMessage.warning('请输入新密码')
    return
  }
  confirmAndReset({ newPassword: resetPwdForm.password })
}

function handleResetPassword(row: UserVO) {
  openResetPassword(row)
}

// ==================== 解锁 ====================
async function handleUnlock(row: UserVO) {
  try {
    await unlockUser(row.id)
    ElMessage.success('解锁成功')
    loadTableData()
  } catch (e) {
    console.error('解锁失败', e)
  }
}

// ==================== 分配角色 ====================
const roleDialogVisible = ref(false)
const roleDialogLoading = ref(false)
const roleSubmitLoading = ref(false)
const roleTreeRef = ref<any>(null)
const roleTreeData = ref<RoleVO[]>([])
const checkedRoleIds = ref<number[]>([])
const currentAssignUserId = ref<number>(0)

async function handleAssignRoles(row: UserVO) {
  currentAssignUserId.value = row.id
  roleDialogLoading.value = true
  roleDialogVisible.value = true
  try {
    const roleRes = await getRolePage({ page: 1, size: 1000 })
    roleTreeData.value = roleRes.data?.content || []
    checkedRoleIds.value = []
    await nextTick()
  } catch (e) {
    console.error('加载角色列表失败', e)
  } finally {
    roleDialogLoading.value = false
  }
}

async function handleSaveRoles() {
  roleSubmitLoading.value = true
  try {
    const checkedKeys = roleTreeRef.value?.getCheckedKeys(false) || []
    await assignUserRoles(currentAssignUserId.value, checkedKeys)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    loadTableData()
  } catch (e) {
    console.error('角色分配失败', e)
  } finally {
    roleSubmitLoading.value = false
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadOrgTree()
  loadTableData()
})
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.user-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* ─── 头部 ─── */
.um-header {
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

/* ─── 工具栏 ─── */
.um-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  &__username {
    width: 180px;
  }

  &__nickname {
    width: 160px;
  }

  &__org {
    width: 180px;
  }

  &__status {
    width: 130px;
  }
}

/* ─── 表格面板 ─── */
.um-table-panel {
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
.um-user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.um-user-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.um-roles {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.um-role-tag {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.um-role-more {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.um-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.um-fail-count {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-text-color-secondary);

  &.is-fail {
    color: #dc2626;
    font-weight: 600;
  }
}

/* ─── 状态标签 ─── */
.um-status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 500;

  &--success {
    background: #ecfdf5;
    color: #059669;
  }

  &--danger {
    background: #fef2f2;
    color: #dc2626;
  }

  &--info {
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
  }

  &--warning {
    background: #fffbeb;
    color: #d97706;
  }
}

/* ─── 操作按钮 ─── */
.um-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* ─── 表单 ─── */
.um-form {
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

/* ─── 角色分配 ─── */
.um-role-assign {
  min-height: 200px;
}

/* ─── 重置密码 ─── */
.um-reset-pwd-form {
  :deep(.el-input.is-disabled .el-input__inner) {
    font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
    color: var(--el-color-primary);
    -webkit-text-fill-color: var(--el-color-primary);
    cursor: text;
  }
}

.um-reset-pwd-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-color-success);
}

/* ─── 抽屉底部 ─── */
.um-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ─── 暗色模式 ─── */
html.dark {
  .um-role-tag {
    background: rgba(var(--el-color-primary-rgb), 0.15);
  }

  .um-status-badge--success {
    background: rgba(16, 185, 129, 0.15);
  }

  .um-status-badge--danger {
    background: rgba(220, 38, 38, 0.12);
  }

  .um-status-badge--warning {
    background: rgba(217, 119, 6, 0.12);
  }

  .um-fail-count.is-fail {
    color: #f87171;
  }

  .um-reset-pwd-tip {
    color: var(--el-color-success-light-3);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 768px) {
  .um-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .um-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .um-toolbar__username,
  .um-toolbar__nickname,
  .um-toolbar__org,
  .um-toolbar__status {
    width: 100%;
  }
}
</style>
