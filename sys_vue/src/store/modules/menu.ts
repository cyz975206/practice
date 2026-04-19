import { defineStore } from "pinia";
import { store } from "@/store";

/** 菜单节点（用于侧边栏渲染） */
interface MenuItem {
  id: number;
  menuCode: string;
  menuName: string;
  menuType: string;
  path: string;
  component: string;
  perms: string;
  icon: string;
  isFrame: number;
  sort: number;
  status: string;
  children?: MenuItem[];
}

export const useMenuStore = defineStore("menu", () => {
  const menuTree = ref<MenuItem[]>([]);

  function setMenuTree(tree: MenuItem[]) {
    menuTree.value = tree;
  }

  return {
    menuTree,
    setMenuTree,
  };
});

// 非setup
export function useMenuStoreHook() {
  return useMenuStore(store);
}
