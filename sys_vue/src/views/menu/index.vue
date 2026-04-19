<template>
  <div class="menu-manage">
    <!-- 页面头部 -->
    <div class="mm-header">
      <div class="mm-header__left">
        <h2 class="mm-header__title">菜单管理</h2>
        <div class="mm-header__stats">
          <span class="mm-stat mm-stat--dir">
            <span class="mm-stat__dot"></span>
            目录 <em>{{ stats.dir }}</em>
          </span>
          <span class="mm-stat mm-stat--menu">
            <span class="mm-stat__dot"></span>
            菜单 <em>{{ stats.menu }}</em>
          </span>
          <span class="mm-stat mm-stat--btn">
            <span class="mm-stat__dot"></span>
            按钮 <em>{{ stats.btn }}</em>
          </span>
        </div>
      </div>
      <div class="mm-header__right">
        <el-input
          v-model="treeFilterText"
          placeholder="搜索菜单名称..."
          clearable
          :prefix-icon="Search"
          class="mm-search"
        />
        <el-button type="primary" @click="handleAdd(null)">
          <el-icon><Plus /></el-icon>
          <span>新增根菜单</span>
        </el-button>
      </div>
    </div>

    <!-- 菜单树 -->
    <div v-loading="treeLoading" class="mm-tree-panel">
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        :props="{ children: 'children', label: 'menuName' }"
        node-key="id"
        highlight-current
        default-expand-all
        :filter-node-method="filterNode"
        :expand-on-click-node="false"
        @node-click="handleNodeClick"
      >
        <template #default="{ data }">
          <div
            class="mm-node"
            :class="[
              `mm-node--${typeClass(data.menuType)}`,
              { 'mm-node--off': data.status === '0' },
            ]"
          >
            <div class="mm-node__body">
              <div class="mm-node__row1">
                <span
                  class="mm-node__icon"
                  :class="`mm-node__icon--${typeClass(data.menuType)}`"
                >
                  <el-icon v-if="data.icon" :size="15">
                    <component :is="data.icon" />
                  </el-icon>
                  <template v-else>{{ typeChar(data.menuType) }}</template>
                </span>
                <span class="mm-node__name">{{ data.menuName }}</span>
                <code class="mm-node__code">{{ data.menuCode }}</code>
              </div>
              <div class="mm-node__row2">
                <code v-if="data.path" class="mm-node__path">{{ data.path }}</code>
                <code v-if="data.perms" class="mm-node__perms">{{ data.perms }}</code>
              </div>
            </div>
            <span
              class="mm-node__dot"
              :class="data.status === '1' ? 'is-on' : 'is-off'"
            ></span>
            <div class="mm-node__ops">
              <el-tooltip content="添加子菜单" placement="top" :show-after="400">
                <el-button
                  circle
                  size="small"
                  @click.stop="handleAdd(data)"
                >
                  <el-icon :size="14"><Plus /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top" :show-after="400">
                <el-button
                  circle
                  size="small"
                  @click.stop="handleEdit(data)"
                >
                  <el-icon :size="14"><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip
                v-if="data.children?.length"
                content="请先删除子菜单"
                placement="top"
              >
                <span class="mm-node__ops-wrap">
                  <el-button circle size="small" type="danger" disabled>
                    <el-icon :size="14"><Delete /></el-icon>
                  </el-button>
                </span>
              </el-tooltip>
              <el-popconfirm
                v-else
                :title="`确认删除「${data.menuName}」？`"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="confirmDelete(data)"
              >
                <template #reference>
                  <el-button circle size="small" type="danger">
                    <el-icon :size="14"><Delete /></el-icon>
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </template>
      </el-tree>
      <el-empty
        v-if="!treeLoading && menuTreeData.length === 0"
        description="暂无菜单数据"
      />
    </div>

    <!-- 编辑抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="formTitle"
      direction="rtl"
      size="520"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        class="mm-form"
      >
        <!-- 基本信息 -->
        <div class="mm-form__section">
          <div class="mm-form__heading">基本信息</div>
          <el-form-item label="菜单编码" prop="menuCode">
            <el-input v-model="formData.menuCode" placeholder="唯一标识，如 system" />
          </el-form-item>
          <el-form-item label="菜单名称" prop="menuName">
            <el-input
              v-model="formData.menuName"
              placeholder="显示名称，如 系统管理"
            />
          </el-form-item>
          <div class="mm-form__inline">
            <el-form-item label="菜单类型" prop="menuType">
              <el-select v-model="formData.menuType" style="width: 100%">
                <el-option label="目录" value="D" />
                <el-option label="菜单" value="M" />
                <el-option label="按钮" value="B" />
              </el-select>
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
          <el-form-item label="上级菜单">
            <el-tree-select
              v-model="formData.parentId"
              :data="menuTreeData"
              :props="{
                children: 'children',
                label: 'menuName',
                value: 'id',
              }"
              node-key="id"
              check-strictly
              :render-after-expand="false"
              placeholder="留空则为根菜单"
              clearable
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <!-- 路由配置（目录/菜单） -->
        <div v-if="formData.menuType !== 'B'" class="mm-form__section">
          <div class="mm-form__heading">路由配置</div>
          <el-form-item label="路由路径" prop="path">
            <el-input v-model="formData.path" placeholder="如 system/user" />
          </el-form-item>
          <el-form-item
            v-if="formData.menuType === 'M'"
            label="组件路径"
            prop="component"
          >
            <el-input v-model="formData.component" placeholder="如 system/user/index" />
          </el-form-item>
          <el-form-item label="是否外链">
            <el-switch
              v-model="isFrameValue"
              active-text="是"
              inactive-text="否"
            />
          </el-form-item>
        </div>

        <!-- 权限配置（按钮） -->
        <div v-if="formData.menuType === 'B'" class="mm-form__section">
          <div class="mm-form__heading">权限配置</div>
          <el-form-item label="权限标识" prop="perms">
            <el-input v-model="formData.perms" placeholder="如 system:user:add" />
          </el-form-item>
        </div>

        <!-- 显示选项 -->
        <div class="mm-form__section">
          <div class="mm-form__heading">显示选项</div>
          <div class="mm-form__inline">
            <el-form-item label="菜单图标">
              <el-input v-model="formData.icon" placeholder="Element Plus 图标名" />
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
        </div>
      </el-form>

      <template #footer>
        <div class="mm-drawer__footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button
            type="primary"
            :loading="submitLoading"
            @click="handleSubmit"
          >
            {{ isEditMode ? '保存修改' : '创建菜单' }}
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getMenuTree,
  createMenu,
  updateMenu,
  deleteMenu,
} from '@/api/menu'
import type { MenuVO, MenuForm } from '@/api/menu/types'
import { useDict } from '@/composables/useDict'

const { options: statusOptions } = useDict('CommonStatus')

// ==================== 菜单树 ====================
const menuTreeRef = ref<any>(null)
const menuTreeData = ref<MenuVO[]>([])
const treeLoading = ref(false)
const treeFilterText = ref('')
const selectedNode = ref<MenuVO | null>(null)

function filterNode(value: string, data: MenuVO) {
  if (!value) return true
  return data.menuName.includes(value)
}

watch(treeFilterText, (val) => {
  menuTreeRef.value?.filter(val)
})

async function loadMenuTree() {
  treeLoading.value = true
  try {
    const res = await getMenuTree()
    menuTreeData.value = res.data || []
  } catch (e) {
    console.error('加载菜单树失败', e)
  } finally {
    treeLoading.value = false
  }
}

// 统计
const stats = computed(() => {
  let dir = 0
  let menu = 0
  let btn = 0
  function walk(nodes: MenuVO[]) {
    for (const n of nodes) {
      if (n.menuType === 'D') dir++
      else if (n.menuType === 'M') menu++
      else if (n.menuType === 'B') btn++
      if (n.children?.length) walk(n.children)
    }
  }
  walk(menuTreeData.value)
  return { dir, menu, btn }
})

// 类型工具
function typeClass(type: string) {
  return type === 'D' ? 'dir' : type === 'M' ? 'menu' : 'btn'
}

function typeChar(type: string) {
  return type === 'D' ? 'D' : type === 'M' ? 'M' : 'B'
}

// ==================== 抽屉 & 表单 ====================
const drawerVisible = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const isEditMode = ref(false)
const isFrameValue = ref(false)

const formTitle = computed(() =>
  isEditMode.value ? '编辑菜单' : '新增菜单',
)

const defaultForm = (): MenuForm => ({
  menuCode: '',
  menuName: '',
  menuType: 'M',
  parentId: undefined,
  path: '',
  component: '',
  perms: '',
  icon: '',
  isFrame: 0,
  sort: 0,
  status: '1',
})

const formData = ref<MenuForm>(defaultForm())

watch(isFrameValue, (val) => {
  formData.value.isFrame = val ? 1 : 0
})

const formRules: FormRules = {
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// ==================== 操作 ====================
function handleNodeClick(data: MenuVO) {
  isEditMode.value = true
  selectedNode.value = data
  formData.value = {
    id: data.id,
    menuCode: data.menuCode,
    menuName: data.menuName,
    menuType: data.menuType,
    parentId: data.parentId || undefined,
    path: data.path,
    component: data.component,
    perms: data.perms,
    icon: data.icon,
    isFrame: data.isFrame,
    sort: data.sort,
    status: data.status,
  }
  isFrameValue.value = data.isFrame === 1
  drawerVisible.value = true
}

function handleAdd(parentNode: MenuVO | null) {
  isEditMode.value = false
  selectedNode.value = null
  formData.value = { ...defaultForm(), parentId: parentNode?.id }
  isFrameValue.value = false
  drawerVisible.value = true
}

function handleEdit(data: MenuVO) {
  handleNodeClick(data)
}

function handleCancel() {
  drawerVisible.value = false
  isEditMode.value = false
  selectedNode.value = null
  isFrameValue.value = false
  formData.value = defaultForm()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formData.value.id) {
      await updateMenu(formData.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createMenu(formData.value)
      ElMessage.success('创建成功')
    }
    handleCancel()
    loadMenuTree()
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    submitLoading.value = false
  }
}

async function confirmDelete(data: MenuVO) {
  try {
    await deleteMenu(data.id)
    ElMessage.success('删除成功')
    if (selectedNode.value?.id === data.id) {
      handleCancel()
    }
    loadMenuTree()
  } catch (e) {
    console.error('删除失败', e)
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadMenuTree()
})
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.menu-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* ─── 头部 ─── */
.mm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;

  &__left {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    letter-spacing: -0.02em;
  }

  &__stats {
    display: flex;
    gap: 8px;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

/* 统计胶囊 */
.mm-stat {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 12px;
  border-radius: 100px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-extra-light);

  em {
    font-style: normal;
    font-weight: 600;
  }

  &__dot {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  &--dir {
    .mm-stat__dot {
      background: #6366f1;
    }
    em {
      color: #6366f1;
    }
  }

  &--menu {
    .mm-stat__dot {
      background: #10b981;
    }
    em {
      color: #10b981;
    }
  }

  &--btn {
    .mm-stat__dot {
      background: #f59e0b;
    }
    em {
      color: #f59e0b;
    }
  }
}

.mm-search {
  width: 220px;
}

/* ─── 树面板 ─── */
.mm-tree-panel {
  flex: 1;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 12px 8px;
  overflow-y: auto;
  min-height: 0;

  :deep(.el-tree) {
    background: transparent;

    .el-tree-node__content {
      height: auto;
      min-height: 36px;
      padding: 2px 8px 2px 0;
      border-radius: 6px;
      margin: 1px 0;
      transition: background 0.15s;
    }

    .el-tree-node__content:hover {
      background: transparent;
    }

    .is-current > .el-tree-node__content {
      background: transparent;
    }

    .el-tree-node__expand-icon {
      padding: 6px 4px;
    }
  }
}

/* ─── 树节点 ─── */
.mm-node {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid transparent;
  border-left: 3px solid transparent;
  transition: all 0.15s ease;
  gap: 10px;

  &:hover {
    background: var(--el-fill-color-light);
    border-color: var(--el-border-color-extra-light);

    .mm-node__ops {
      opacity: 1;
    }
  }

  &--dir {
    border-left-color: #6366f1;
  }

  &--menu {
    border-left-color: #10b981;
  }

  &--btn {
    border-left-color: #f59e0b;
  }

  &--off {
    opacity: 0.5;
  }

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__row1 {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 26px;
    height: 26px;
    border-radius: 6px;
    font-size: 11px;
    font-weight: 700;
    flex-shrink: 0;
    letter-spacing: -0.02em;

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
    font-weight: 500;
    color: var(--el-text-color-primary);
    white-space: nowrap;
  }

  &__code {
    font-size: 11px;
    color: var(--el-text-color-placeholder);
    font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
    background: var(--el-fill-color);
    padding: 1px 6px;
    border-radius: 3px;
  }

  &__row2 {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 3px;
    padding-left: 34px;
  }

  &__path,
  &__perms {
    font-size: 11px;
    color: var(--el-text-color-secondary);
    font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
    background: var(--el-fill-color);
    padding: 1px 6px;
    border-radius: 3px;
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

  &__ops {
    display: flex;
    align-items: center;
    gap: 4px;
    opacity: 0.3;
    transition: opacity 0.15s;
    flex-shrink: 0;

    .el-button {
      transition: all 0.15s;
    }

    &-wrap {
      display: inline-flex;
    }
  }
}

/* ─── 表单 ─── */
.mm-form {
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
.mm-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ─── 暗色模式 ─── */
html.dark {
  .mm-node__icon--dir {
    background: rgba(99, 102, 241, 0.15);
  }

  .mm-node__icon--menu {
    background: rgba(16, 185, 129, 0.15);
  }

  .mm-node__icon--btn {
    background: rgba(245, 158, 11, 0.15);
  }

  .mm-node__code,
  .mm-node__path,
  .mm-node__perms {
    background: rgba(255, 255, 255, 0.06);
  }

  .mm-node__dot.is-on {
    box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.15);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 768px) {
  .mm-header {
    flex-direction: column;
    align-items: stretch;
  }

  .mm-header__left {
    flex-wrap: wrap;
  }

  .mm-search {
    width: 100%;
  }

  .mm-form__inline {
    grid-template-columns: 1fr;
  }
}
</style>
