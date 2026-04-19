<template>
  <div>
    <el-tree
      ref="menuTreeRef"
      :data="menuList"
      :props="{ children: 'children', label: 'menuName' }"
      node-key="id"
      :expand-on-click-node="false"
      :show-checkbox="showCheckbox"
      :default-expand-all="true"
      :default-checked-keys="checkedMenuIds"
      @check="handleCheck"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, nextTick } from 'vue';
import { getMenuTree } from '@/api/menu';
import type { MenuVO } from '@/api/menu/types';
import { getRoleMenuIds, assignRoleMenus } from '@/api/role';

const props = withDefaults(
  defineProps<{
    roleId?: number;
    showCheckbox?: boolean;
  }>(),
  {
    roleId: undefined,
    showCheckbox: true,
  }
);

const emits = defineEmits<{
  (e: 'update:checkedMenuIds', ids: number[]): void;
}>();

defineExpose({
  getCheckedMenuIds,
  saveMenuIds,
});

const menuTreeRef = ref<any>(null);
const menuList = ref<MenuVO[]>([]);
const checkedMenuIds = ref<number[]>([]);
const loading = ref(false);

/** 加载菜单树 */
async function loadMenuTree() {
  loading.value = true;
  try {
    const res = await getMenuTree();
    menuList.value = res.data || [];
  } catch (e) {
    console.error('加载菜单树失败', e);
  } finally {
    loading.value = false;
  }
}

/** 加载角色已分配的菜单ID */
async function loadRoleMenuIds(roleId: number) {
  if (!roleId) return;
  try {
    const res = await getRoleMenuIds(roleId);
    checkedMenuIds.value = res.data || [];
    await nextTick();
    menuTreeRef.value?.setCheckedKeys(checkedMenuIds.value);
  } catch (e) {
    console.error('加载角色菜单ID失败', e);
  }
}

/** 勾选事件 */
function handleCheck() {
  const checked = menuTreeRef.value?.getCheckedKeys(false) || [];
  const halfChecked = menuTreeRef.value?.getHalfCheckedKeys() || [];
  const allIds = [...checked, ...halfChecked];
  checkedMenuIds.value = checked;
  emits('update:checkedMenuIds', allIds);
}

/** 获取当前勾选的菜单ID（含半选） */
function getCheckedMenuIds(): number[] {
  const checked = menuTreeRef.value?.getCheckedKeys(false) || [];
  const halfChecked = menuTreeRef.value?.getHalfCheckedKeys() || [];
  return [...checked, ...halfChecked];
}

/** 保存菜单分配 */
async function saveMenuIds(roleId: number): Promise<void> {
  const ids = getCheckedMenuIds();
  await assignRoleMenus(roleId, ids);
}

/** 监听 roleId 变化 */
watch(
  () => props.roleId,
  (newVal) => {
    if (newVal) {
      loadRoleMenuIds(newVal);
    } else {
      checkedMenuIds.value = [];
      menuTreeRef.value?.setCheckedKeys([]);
    }
  },
  { immediate: true }
);

// 初始化加载菜单树
loadMenuTree();
</script>

<style lang="scss" scoped></style>
