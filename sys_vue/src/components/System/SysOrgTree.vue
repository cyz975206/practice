<template>
  <div>
    <el-tree
      ref="orgTreeRef"
      :data="orgList"
      :default-expanded-keys="expendId"
      :props="{ children: 'children', label: 'orgShortName' }"
      :expand-on-click-node="false"
      node-key="orgCode"
      highlight-current
      :show-checkbox="showCheckbox"
      :default-expand-all="defaultExpandAll"
      @check-change="handleCheckChange"
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <span class="flex justify-between flex-1 pr-2">
          <span>{{ node.label }}</span>
          <span v-if="isEdit">
            <el-icon class="text-[#409eff] cursor-pointer mr-2" @click.stop="handleManageType('add', data)">
              <Plus />
            </el-icon>
            <el-icon
              v-if="data.parentOrgCode"
              class="text-[#409eff] cursor-pointer mr-2"
              @click.stop="handleManageType('edit', data)"
            >
              <Edit />
            </el-icon>
            <el-icon
              v-if="data.parentOrgCode"
              class="text-[#409eff] cursor-pointer"
              @click.stop="handleManageType('delete', data)"
            >
              <Delete />
            </el-icon>
          </span>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted } from 'vue';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';
import { getOrgTree } from '@/api/org';
import type { OrgVO } from '@/api/org/types';

const props = withDefaults(
  defineProps<{
    defaultExpandAll?: boolean;
    showCheckbox?: boolean;
    isEdit?: boolean;
  }>(),
  {
    defaultExpandAll: false,
    showCheckbox: true,
    isEdit: false,
  }
);

const emits = defineEmits<{
  (e: 'nodeClick', data: OrgVO): void;
  (e: 'manage', type: string, data: OrgVO): void;
}>();

defineExpose({
  refresh,
});

const orgTreeRef = ref<any>(null);
const orgList = ref<OrgVO[]>([]);
const expendId = ref<string[]>([]);

/** 加载机构树 */
async function refresh() {
  try {
    const res = await getOrgTree();
    orgList.value = res.data || [];
    // 设置默认展开第一个节点
    if (orgList.value.length > 0) {
      expendId.value = [orgList.value[0].orgCode];
    }
  } catch (e) {
    console.error('加载机构树失败', e);
  }
}

/** 勾选变更 */
function handleCheckChange(data: OrgVO, checked: boolean) {
  if (checked) {
    orgTreeRef.value?.setCheckedKeys([], false);
    orgTreeRef.value?.setChecked(data.orgCode, true, false);
  }
}

/** 节点点击 */
function handleNodeClick(data: OrgVO) {
  emits('nodeClick', data);
}

/** 管理操作 */
function handleManageType(type: string, data: OrgVO) {
  emits('manage', type, data);
}

onMounted(() => {
  refresh();
});
</script>

<style lang="scss" scoped></style>
