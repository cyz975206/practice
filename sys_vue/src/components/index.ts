import { App } from "vue";
// 阿里巴巴图标
import AliIcon from "./Icons/AliIcon.vue";

export function setupComponent(app: App<Element>) {
  app.component("AliIcon", AliIcon);
}
