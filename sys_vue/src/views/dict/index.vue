<template>
  <div class="dict-manage">
    <!-- 页面头部 -->
    <div class="dm-header">
      <div class="dm-header__left">
        <h2 class="dm-header__title">数据字典</h2>
        <span class="dm-header__count">共 <em>{{ typeTotal }}</em> 个类型</span>
      </div>
    </div>

    <!-- 主从布局 -->
    <div class="dm-layout">
      <!-- 左侧：字典类型列表 -->
      <div class="dm-type-panel">
        <div class="dm-type-panel__bar">
          <el-input
            v-model="typeQueryParams.dictName"
            placeholder="搜索类型..."
            clearable
            :prefix-icon="Search"
            size="small"
            class="dm-type-panel__search"
            @keyup.enter="handleQueryType"
          />
          <el-button type="primary" size="small" @click="handleAddType">
            <el-icon><Plus /></el-icon>
          </el-button>
        </div>

        <div v-loading="typeLoading" class="dm-type-list">
          <div
            v-for="item in typeData"
            :key="item.id"
            class="dm-type-item"
            :class="{ 'is-active': selectedDictType === item.dictType }"
            @click="handleTypeRowClick(item)"
          >
            <div class="dm-type-item__main">
              <span class="dm-type-item__name">{{ item.dictName }}</span>
              <code class="dm-type-item__code">{{ item.dictType }}</code>
            </div>
            <div class="dm-type-item__meta">
              <span class="dm-type-item__count">{{ item.itemCount || 0 }} 项</span>
              <span
                class="dm-type-item__dot"
                :class="item.status === '1' ? 'is-on' : 'is-off'"
              ></span>
            </div>
          </div>
          <el-empty
            v-if="!typeLoading && typeData.length === 0"
            description="暂无字典类型"
            :image-size="60"
          />
        </div>

        <el-pagination
          v-if="typeTotal > 0"
          v-model:current-page="typeQueryParams.page"
          v-model:page-size="typeQueryParams.size"
          :total="typeTotal"
          :page-sizes="[10, 20, 50]"
          small
          layout="total, prev, pager, next"
          class="dm-type-panel__pager"
          @size-change="loadTypeData"
          @current-change="loadTypeData"
        />
      </div>

      <!-- 右侧：字典项管理 -->
      <div class="dm-item-panel">
        <div class="dm-item-panel__bar">
          <div class="dm-item-panel__title">
            <span>字典项管理</span>
            <code v-if="selectedDictType" class="dm-item-panel__tag">{{
              selectedDictType
            }}</code>
          </div>
          <template v-if="selectedDictType">
            <el-select
              v-model="itemQueryParams.status"
              placeholder="状态"
              clearable
              size="small"
              style="width: 100px"
            >
              <el-option
                v-for="opt in statusOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <el-button size="small" @click="handleQueryItem">搜索</el-button>
            <el-button size="small" @click="resetQueryItem">重置</el-button>
            <el-button type="primary" size="small" @click="handleAddItem">
              <el-icon><Plus /></el-icon>
              <span>新增</span>
            </el-button>
          </template>
        </div>

        <el-empty
          v-if="!selectedDictType"
          description="请在左侧选择一个字典类型"
          class="dm-item-panel__empty"
        />

        <template v-else>
          <div class="dm-item-panel__table">
            <el-table v-loading="itemLoading" :data="itemData">
              <el-table-column
                prop="dictCode"
                label="字典编码"
                min-width="120"
              >
                <template #default="{ row }">
                  <code class="dm-item-code">{{ row.dictCode }}</code>
                </template>
              </el-table-column>
              <el-table-column
                prop="dictLabel"
                label="字典标签"
                min-width="120"
              >
                <template #default="{ row }">
                  <span class="dm-item-label">{{ row.dictLabel }}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="dictValue"
                label="字典值"
                min-width="80"
              >
                <template #default="{ row }">
                  <code class="dm-item-value">{{ row.dictValue }}</code>
                </template>
              </el-table-column>
              <el-table-column prop="sort" label="排序" width="70" align="center" />
              <el-table-column prop="status" label="状态" width="90" align="center">
                <template #default="{ row }">
                  <span class="dm-item-status">
                    <span
                      class="dm-item-status__dot"
                      :class="row.status === '1' ? 'is-on' : 'is-off'"
                    ></span>
                    <span class="dm-item-status__text">{{
                      getStatusLabel('CommonStatus', row.status)
                    }}</span>
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="130" align="center" fixed="right">
                <template #default="{ row }">
                  <div class="dm-item-actions">
                    <el-tooltip content="编辑" placement="top">
                      <el-button
                        circle
                        size="small"
                        @click="handleEditItem(row)"
                      >
                        <el-icon :size="14"><Edit /></el-icon>
                      </el-button>
                    </el-tooltip>
                    <el-tooltip content="删除" placement="top">
                      <el-popconfirm
                        :title="`确认删除字典项「${row.dictLabel}」？`"
                        confirm-button-text="确定"
                        cancel-button-text="取消"
                        @confirm="handleDeleteItem(row)"
                      >
                        <template #reference>
                          <el-button circle size="small" type="danger">
                            <el-icon :size="14"><Delete /></el-icon>
                          </el-button>
                        </template>
                      </el-popconfirm>
                    </el-tooltip>
                    <el-tooltip
                      :content="row.status === '1' ? '禁用' : '启用'"
                      placement="top"
                    >
                      <el-button
                        circle
                        size="small"
                        :type="row.status === '1' ? 'warning' : 'success'"
                        @click="handleToggleItemStatus(row)"
                      >
                        <el-icon :size="14"><Setting /></el-icon>
                      </el-button>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              v-if="itemTotal > 0"
              v-model:current-page="itemQueryParams.page"
              v-model:page-size="itemQueryParams.size"
              :total="itemTotal"
              :page-sizes="[10, 20, 50]"
              small
              layout="total, sizes, prev, pager, next"
              class="dm-item-panel__pager"
              @size-change="loadItemData"
              @current-change="loadItemData"
            />
          </div>
        </template>
      </div>
    </div>

    <!-- 字典类型抽屉 -->
    <el-drawer
      v-model="typeDialogVisible"
      :title="typeDialogTitle"
      direction="rtl"
      size="440"
      :close-on-click-modal="false"
      append-to-body
      @close="resetTypeForm"
    >
      <el-form
        ref="typeFormRef"
        :model="typeFormData"
        :rules="typeFormRules"
        label-position="top"
        class="dm-form"
      >
        <div class="dm-form__section">
          <div class="dm-form__heading">基本信息</div>
          <el-form-item label="字典类型" prop="dictType">
            <el-input
              v-model="typeFormData.dictType"
              placeholder="唯一标识，如 sys_user_sex"
              :disabled="!!typeFormData.id"
            />
          </el-form-item>
          <el-form-item label="字典名称" prop="dictName">
            <el-input
              v-model="typeFormData.dictName"
              placeholder="显示名称，如 用户性别"
            />
          </el-form-item>
        </div>
        <div class="dm-form__section">
          <div class="dm-form__heading">其他</div>
          <el-form-item label="备注">
            <el-input
              v-model="typeFormData.remark"
              type="textarea"
              :rows="3"
              placeholder="可选备注说明..."
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dm-drawer__footer">
          <el-button @click="typeDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="typeSubmitLoading"
            @click="handleSubmitType"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 字典项抽屉 -->
    <el-drawer
      v-model="itemDialogVisible"
      :title="itemDialogTitle"
      direction="rtl"
      size="480"
      :close-on-click-modal="false"
      append-to-body
      @close="resetItemForm"
    >
      <el-form
        ref="itemFormRef"
        :model="itemFormData"
        :rules="itemFormRules"
        label-position="top"
        class="dm-form"
      >
        <div class="dm-form__section">
          <div class="dm-form__heading">字典项信息</div>
          <el-form-item label="字典编码" prop="dictCode">
            <el-input
              v-model="itemFormData.dictCode"
              placeholder="唯一标识，如 male"
            />
          </el-form-item>
          <el-form-item label="字典标签" prop="dictLabel">
            <el-input
              v-model="itemFormData.dictLabel"
              placeholder="显示文本，如 男"
            />
          </el-form-item>
          <el-form-item label="字典值" prop="dictValue">
            <el-input
              v-model="itemFormData.dictValue"
              placeholder="实际值，如 0"
            />
          </el-form-item>
        </div>
        <div class="dm-form__section">
          <div class="dm-form__heading">其他设置</div>
          <div class="dm-form__inline">
            <el-form-item label="排序号">
              <el-input-number
                v-model="itemFormData.sort"
                :min="0"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="itemFormData.status">
                <el-radio-button value="1">启用</el-radio-button>
                <el-radio-button value="0">禁用</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </div>
          <el-form-item label="备注">
            <el-input
              v-model="itemFormData.remark"
              type="textarea"
              :rows="2"
              placeholder="可选备注说明..."
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dm-drawer__footer">
          <el-button @click="itemDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="itemSubmitLoading"
            @click="handleSubmitItem"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Setting } from '@element-plus/icons-vue'
import {
  getDictTypePage,
  createDictType,
  updateDictType,
  deleteDictType,
} from '@/api/dict/type'
import {
  getDictItemPage,
  createDictItem,
  updateDictItem,
  deleteDictItem,
  updateDictItemStatus,
} from '@/api/dict/item'
import type { DictTypeVO, DictTypeForm } from '@/api/dict/types'
import type { DictItemVO, DictItemForm } from '@/api/dict/types'
import { useDict } from '@/composables/useDict'

const { options: statusOptions, getLabel: getStatusLabel } = useDict('CommonStatus')

// ==================== 字典类型 ====================
const typeData = ref<DictTypeVO[]>([])
const typeLoading = ref(false)
const typeTotal = ref(0)
const typeQueryParams = ref({
  page: 1,
  size: 10,
  dictName: undefined as string | undefined,
})

async function loadTypeData() {
  typeLoading.value = true
  try {
    const res = await getDictTypePage(typeQueryParams.value)
    typeData.value = res.data?.content || []
    typeTotal.value = res.data?.totalElements || 0
  } finally {
    typeLoading.value = false
  }
}
function handleQueryType() {
  typeQueryParams.value.page = 1
  loadTypeData()
}
function resetQueryType() {
  typeQueryParams.value = { page: 1, size: 10, dictName: undefined }
  loadTypeData()
}

// 字典类型抽屉
const typeDialogVisible = ref(false)
const typeDialogTitle = ref('')
const typeSubmitLoading = ref(false)
const typeFormRef = ref()
const typeFormData = ref<DictTypeForm & { id?: number }>({
  dictType: '',
  dictName: '',
  remark: '',
})
const typeFormRules = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
}
function resetTypeForm() {
  typeFormData.value = { dictType: '', dictName: '', remark: '' }
  typeFormRef.value?.resetFields()
}
function handleAddType() {
  typeDialogTitle.value = '新增字典类型'
  typeFormData.value = { dictType: '', dictName: '', remark: '' }
  typeDialogVisible.value = true
}
function handleEditType(row: DictTypeVO) {
  typeDialogTitle.value = '编辑字典类型'
  typeFormData.value = {
    id: row.id,
    dictType: row.dictType,
    dictName: row.dictName,
    remark: row.remark || '',
  }
  typeDialogVisible.value = true
}
async function handleSubmitType() {
  await typeFormRef.value?.validate()
  typeSubmitLoading.value = true
  try {
    if (typeFormData.value.id) {
      await updateDictType(typeFormData.value.id, typeFormData.value)
      ElMessage.success('更新成功')
    } else {
      await createDictType(typeFormData.value)
      ElMessage.success('创建成功')
    }
    typeDialogVisible.value = false
    loadTypeData()
  } finally {
    typeSubmitLoading.value = false
  }
}

// ==================== 字典项 ====================
const selectedDictType = ref('')
const itemData = ref<DictItemVO[]>([])
const itemLoading = ref(false)
const itemTotal = ref(0)
const itemQueryParams = ref({
  page: 1,
  size: 10,
  dictType: '',
  status: undefined as string | undefined,
})

function handleTypeRowClick(row: DictTypeVO) {
  selectedDictType.value = row.dictType
  itemQueryParams.value = {
    page: 1,
    size: 10,
    dictType: row.dictType,
    status: undefined,
  }
  loadItemData()
}
async function loadItemData() {
  itemLoading.value = true
  try {
    const res = await getDictItemPage(itemQueryParams.value)
    itemData.value = res.data?.content || []
    itemTotal.value = res.data?.totalElements || 0
  } finally {
    itemLoading.value = false
  }
}
function handleQueryItem() {
  itemQueryParams.value.page = 1
  loadItemData()
}
function resetQueryItem() {
  itemQueryParams.value = {
    page: 1,
    size: 10,
    dictType: selectedDictType.value,
    status: undefined,
  }
  loadItemData()
}

// 字典项抽屉
const itemDialogVisible = ref(false)
const itemDialogTitle = ref('')
const itemSubmitLoading = ref(false)
const itemFormRef = ref()
const itemFormData = ref<DictItemForm & { id?: number }>({
  dictType: '',
  dictCode: '',
  dictLabel: '',
  dictValue: '',
  sort: 0,
  remark: '',
  status: '1',
})
const itemFormRules = {
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
}
function resetItemForm() {
  itemFormData.value = {
    dictType: '',
    dictCode: '',
    dictLabel: '',
    dictValue: '',
    sort: 0,
    remark: '',
    status: '1',
  }
  itemFormRef.value?.resetFields()
}
function handleAddItem() {
  itemDialogTitle.value = '新增字典项'
  itemFormData.value = {
    dictType: selectedDictType.value,
    dictCode: '',
    dictLabel: '',
    dictValue: '',
    sort: 0,
    remark: '',
    status: '1',
  }
  itemDialogVisible.value = true
}
function handleEditItem(row: DictItemVO) {
  itemDialogTitle.value = '编辑字典项'
  itemFormData.value = {
    id: row.id,
    dictType: row.dictType,
    dictCode: row.dictCode,
    dictLabel: row.dictLabel,
    dictValue: row.dictValue || '',
    sort: row.sort,
    remark: row.remark || '',
    status: row.status,
  }
  itemDialogVisible.value = true
}
async function handleSubmitItem() {
  await itemFormRef.value?.validate()
  itemSubmitLoading.value = true
  try {
    if (itemFormData.value.id) {
      await updateDictItem(itemFormData.value.id, itemFormData.value)
      ElMessage.success('更新成功')
    } else {
      await createDictItem(itemFormData.value)
      ElMessage.success('创建成功')
    }
    itemDialogVisible.value = false
    loadItemData()
  } finally {
    itemSubmitLoading.value = false
  }
}

// 删除字典项
async function handleDeleteItem(row: DictItemVO) {
  try {
    await deleteDictItem(row.id)
    ElMessage.success('删除成功')
    loadItemData()
  } catch (e) {
    console.error('删除失败', e)
  }
}

// 字典项状态切换
async function handleToggleItemStatus(row: DictItemVO) {
  const newStatus = row.status === '1' ? '0' : '1'
  const label = newStatus === '1' ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(
      `确认${label}字典项「${row.dictLabel}」？`,
      '提示',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' },
    )
    await updateDictItemStatus(row.id, newStatus)
    ElMessage.success(`${label}成功`)
    loadItemData()
  } catch {
    // 用户取消
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadTypeData()
})
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.dict-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* ─── 头部 ─── */
.dm-header {
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

/* ─── 主从布局 ─── */
.dm-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

/* ─── 左侧：类型面板 ─── */
.dm-type-panel {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &__bar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    border-bottom: 1px solid var(--el-border-color-extra-light);
  }

  &__search {
    flex: 1;
  }

  &__pager {
    padding: 8px 12px;
    display: flex;
    justify-content: center;
    border-top: 1px solid var(--el-border-color-extra-light);
  }
}

/* 类型列表 */
.dm-type-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px;
}

.dm-type-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin: 2px 0;
  border-radius: 6px;
  border: 1px solid transparent;
  border-left: 3px solid transparent;
  cursor: pointer;
  transition: all 0.15s ease;

  &:hover {
    background: var(--el-fill-color-light);
    border-color: var(--el-border-color-extra-light);
  }

  &.is-active {
    background: var(--el-color-primary-light-9);
    border-left-color: var(--el-color-primary);
  }

  &__main {
    min-width: 0;
  }

  &__name {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__code {
    display: block;
    font-size: 11px;
    font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
    color: var(--el-text-color-placeholder);
    margin-top: 2px;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }

  &__count {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

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
}

/* ─── 右侧：字典项面板 ─── */
.dm-item-panel {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &__bar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    border-bottom: 1px solid var(--el-border-color-extra-light);
    flex-wrap: wrap;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 10px;
    flex: 1;
    min-width: 0;

    span {
      font-size: 15px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      white-space: nowrap;
    }
  }

  &__tag {
    font-size: 12px;
    font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    padding: 2px 10px;
    border-radius: 4px;
    flex-shrink: 0;
  }

  &__empty {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__table {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  &__pager {
    padding: 10px 16px;
    display: flex;
    justify-content: flex-end;
    border-top: 1px solid var(--el-border-color-extra-light);
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
}

/* ─── 字典项单元格 ─── */
.dm-item-code {
  font-size: 12px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 1px 6px;
  border-radius: 3px;
}

.dm-item-label {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.dm-item-value {
  font-size: 12px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color);
  padding: 1px 6px;
  border-radius: 3px;
}

.dm-item-status {
  display: inline-flex;
  align-items: center;
  gap: 5px;

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
    font-size: 12px;
    color: var(--el-text-color-regular);
  }
}

.dm-item-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* ─── 表单 ─── */
.dm-form {
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
.dm-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ─── 暗色模式 ─── */
html.dark {
  .dm-type-item.is-active {
    background: rgba(var(--el-color-primary-rgb), 0.1);
  }

  .dm-item-panel__tag {
    background: rgba(var(--el-color-primary-rgb), 0.12);
  }

  .dm-item-code {
    background: rgba(var(--el-color-primary-rgb), 0.12);
  }

  .dm-item-value {
    background: rgba(255, 255, 255, 0.06);
  }

  .dm-item-status__dot.is-on,
  .dm-type-item__dot.is-on {
    box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.15);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 900px) {
  .dm-layout {
    grid-template-columns: 1fr;
  }

  .dm-type-panel {
    max-height: 280px;
  }

  .dm-form__inline {
    grid-template-columns: 1fr;
  }
}
</style>
