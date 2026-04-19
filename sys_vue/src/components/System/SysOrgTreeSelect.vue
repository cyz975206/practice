<template>
  <div>
    <el-tree-select
      v-model="value"
      :data="orgList"
      :props="{ children: 'children', label: 'orgShortName', value: 'orgCode' }"
      node-key="orgCode"
      check-strictly
      :default-expanded-keys="expendId"
      :render-after-expand="false"
      clearable
      :placeholder="placeholder"
      style="width: 100%"
      @clear="handleClear"
      @node-click="handleNodeClick"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted } from 'vue';
import { getOrgTree } from '@/api/org';
import type { OrgVO } from '@/api/org/types';

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    placeholder?: string;
  }>(),
  {
    modelValue: '',
    placeholder: '请选择机构',
  }
);

const emits = defineEmits<{
  (e: 'update:modelValue', val: string): void;
  (e: 'change', data: OrgVO | null): void;
}>();

defineExpose({
  refresh,
  reset,
});

const value = ref(props.modelValue);
const orgList = ref<OrgVO[]>([]);
const expendId = ref<string[]>([]);

/** 加载机构树 */
async function refresh() {
  try {
    const res = await getOrgTree();
    orgList.value = res.data || [];
    if (orgList.value.length > 0) {
      expendId.value = [orgList.value[0].orgCode];
    }
  } catch (e) {
    console.error('加载机构树失败', e);
  }
}

/** 重置选择 */
function reset() {
  value.value = '';
  emits('update:modelValue', '');
  emits('change', null);
}

/** 清空 */
function handleClear() {
  emits('update:modelValue', '');
  emits('change', null);
}

/** 节点点击 */
function handleNodeClick(data: OrgVO) {
  value.value = data.orgCode;
  emits('update:modelValue', data.orgCode);
  emits('change', data);
}

// 监听外部 v-model 变化
watch(
  () => props.modelValue,
  (val) => {
    value.value = val;
  }
);

onMounted(() => {
  refresh();
});
</script>

<style lang="scss" scoped></style>
