import type { App } from "vue";

import { hasPerm } from "./permission";
import { debounce } from "./debounce";

// 全局注册 directive
export function setupDirective(app: App<Element>) {
  // 使 v-hasPerm 在所有组件中都可用
  app.directive("hasPerm", hasPerm);
  // 注册防抖指令
  app.directive("debounce", debounce);
}
