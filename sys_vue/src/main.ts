import { createApp } from "vue";
import App from "./App.vue";
import router from "@/router";
import { setupStore } from "@/store";
import { setupDirective } from "@/directive";
import { setupComponent } from "@/components";
import { setupPermissionGuard } from "@/permission";

// 本地SVG图标
import "virtual:svg-icons-register";

// 样式
import "element-plus/theme-chalk/dark/css-vars.css";
import "@/styles/index.scss";
import "uno.css";
import ElementPlus from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import "element-plus/dist/index.css";

const app = createApp(App);
app.use(ElementPlus, { locale: zhCn });
// 全局注册 自定义指令(directive)
setupDirective(app);
// 全局注册 状态管理(store)
setupStore(app);
// 全局注册 组件
setupComponent(app);
// 路由守卫（必须在 Pinia 之后）
setupPermissionGuard();
app.use(router);
app.mount("#micro-app-demo");
