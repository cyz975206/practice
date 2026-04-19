import router from "@/router";
import { useUserStoreHook } from "@/store/modules/user";
import { usePermissionStoreHook } from "@/store/modules/permission";
import { useMenuStoreHook } from "@/store/modules/menu";

import NProgress from "nprogress";
import "nprogress/nprogress.css";

NProgress.configure({ showSpinner: false });

const whiteList = ["/login"];

export function setupPermissionGuard() {
  router.beforeEach(async (to, from, next) => {
    NProgress.start();
    const token = localStorage.getItem("access_token");

    if (token) {
      if (to.path === "/login") {
        next({ path: "/" });
        NProgress.done();
      } else {
        const userStore = useUserStoreHook();
        const hasRoles = userStore.user.roles && userStore.user.roles.length > 0;

        if (hasRoles) {
          if (to.path === '/' || to.matched.length === 0) {
            // 已有角色时访问根路径或未匹配路由，重定向到第一个动态路由
            const permissionStore = usePermissionStoreHook();
            const first = permissionStore.asyncRoutes[0];
            if (first) {
              const redirect = first.redirect || first.path;
              const targetPath = redirect.startsWith('/') ? redirect : first.path + '/' + redirect;
              next({ path: targetPath, replace: true });
            } else {
              next('/404');
            }
          } else {
            next();
          }
        } else {
          try {
            await userStore.getUserInfo();
            const permissionStore = usePermissionStoreHook();
            const { routes: accessRoutes, menuTree } = await permissionStore.generateRoutes();

            if (menuTree.length > 0) {
              const menuStore = useMenuStoreHook();
              menuStore.setMenuTree(menuTree);
            }

            accessRoutes.forEach((route) => {
              router.addRoute(route);
            });
            // 根路径重定向到第一个动态路由
            if (to.path === '/') {
              const first = accessRoutes[0];
              next(first ? { path: first.redirect || first.path, replace: true } : { ...to, replace: true });
            } else {
              next({ ...to, replace: true });
            }
          } catch (error) {
            await userStore.resetToken();
            next(`/login?redirect=${to.path}`);
            NProgress.done();
          }
        }
      }
    } else {
      if (whiteList.indexOf(to.path) !== -1) {
        next();
      } else {
        next(`/login?redirect=${to.path}`);
        NProgress.done();
      }
    }
  });

  router.afterEach(() => {
    NProgress.done();
  });
}
