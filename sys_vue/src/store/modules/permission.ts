import { RouteRecordRaw } from "vue-router";
import { defineStore } from "pinia";
import { constantRoutes } from "@/router";
import { store } from "@/store";
import { getMenuTree } from "@/api/menu";
import type { R } from "@/api/types";

const viewModules = import.meta.glob("../../views/**/*.vue");
const Layout = () => import("@/layout/index.vue");

/** 后端菜单节点 */
interface MenuNode {
  id: number;
  menuCode: string;
  menuName: string;
  menuType: string;
  parentId: number;
  path: string;
  component: string;
  perms: string;
  icon: string;
  isFrame: number;
  sort: number;
  status: string;
  children?: MenuNode[];
}

/** 将后端菜单树转为 Vue Router 路由 */
function menuToRoute(menu: MenuNode): RouteRecordRaw | null {
  // 跳过禁用的菜单和按钮
  if (menu.status === "0" || menu.menuType === "B") return null;

  const route: RouteRecordRaw = {
    path: menu.path || "",
    name: menu.menuCode,
    meta: {
      title: menu.menuName,
      icon: menu.icon,
      perms: menu.perms,
      keepAlive: true,
    },
  };

  if (menu.children && menu.children.length > 0) {
    const children = menu.children
      .map((child) => menuToRoute(child))
      .filter(Boolean) as RouteRecordRaw[];

    if (children.length > 0) {
      route.children = children;
      route.redirect = children[0].path;
      // 目录类型或有子菜单时使用 Layout
      route.component = Layout;
    } else {
      return null;
    }
  } else if (menu.component) {
    const componentPath = `../../views/${menu.component}.vue`;
    route.component = viewModules[componentPath];
    if (!route.component) {
      console.warn(`组件未找到: ${componentPath}`);
      return null;
    }
  } else {
    return null;
  }

  return route;
}

// setup
export const usePermissionStore = defineStore("permission", () => {
  const routes = ref<RouteRecordRaw[]>([]);

  function setRoutes(newRoutes: RouteRecordRaw[]) {
    routes.value = constantRoutes.concat(newRoutes);
  }

  /** 根据后端菜单树生成动态路由，同时返回原始菜单树 */
  async function generateRoutes() {
    const response = await getMenuTree();
    if (response.code === 200 && response.data) {
      const menuTree = response.data as MenuNode[];
      const asyncRoutes = menuTree
        .map((menu) => menuToRoute(menu))
        .filter(Boolean) as RouteRecordRaw[];
      setRoutes(asyncRoutes);
      return { routes: asyncRoutes, menuTree };
    }
    return { routes: [] as RouteRecordRaw[], menuTree: [] as MenuNode[] };
  }

  /** 混合模式左侧菜单 */
  const mixLeftMenu = ref<RouteRecordRaw[]>([]);
  function getMixLeftMenu(activeTop: string) {
    routes.value.forEach((item) => {
      if (item.path === activeTop) {
        mixLeftMenu.value = item.children || [];
      }
    });
  }

  return { routes, setRoutes, generateRoutes, getMixLeftMenu, mixLeftMenu };
});

// 非setup
export function usePermissionStoreHook() {
  return usePermissionStore(store);
}
