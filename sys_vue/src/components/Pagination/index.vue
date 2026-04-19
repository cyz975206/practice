<template>
  <div :class="[{ hidden: hidden }, position]" class="pagination">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :background="background"
      :layout="layout"
      :page-sizes="pageSizes"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { PropType } from "vue";
import { scrollTo } from "@/utils/scroll-to";

const props = defineProps({
  total: {
    required: true,
    type: Number as PropType<number>,
    default: 0,
  },
  page: {
    type: Number,
    default: 1,
  },
  limit: {
    type: Number,
    default: 20,
  },
  pageSizes: {
    type: Array as PropType<number[]>,
    default() {
      return [20, 30, 50, 100, 200];
    },
  },
  layout: {
    type: String,
    default: "total, sizes, prev, pager, next, jumper",
  },
  position: {
    type: String,
    default: "right",
  },
  background: {
    type: Boolean,
    default: true,
  },
  autoScroll: {
    type: Boolean,
    default: true,
  },
  hidden: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["pagination", "update:page", "update:limit"]);

const currentPage = useVModel(props, "page", emit);

const pageSize = useVModel(props, "limit", emit);

function handleSizeChange(val: number) {
  // if (props.total < val) {
  //   return;
  // }
  emit("pagination", { page: currentPage.value, limit: val });
  if (props.autoScroll) {
    scrollTo(0, 800);
  }
}

function handleCurrentChange(val: number) {
  currentPage.value = val;
  emit("pagination", { page: val, limit: props.limit });
  if (props.autoScroll) {
    scrollTo(0, 800);
  }
}
</script>

<style lang="scss" scoped>
.pagination {
  padding: 12px;

  &.hidden {
    display: none;
  }
  &.right {
    display: flex;
    justify-content: flex-end;
  }
  &.center {
    display: flex;
    justify-content: center;
  }
  &.left {
    display: flex;
    justify-content: flex-start;
  }
}
</style>
