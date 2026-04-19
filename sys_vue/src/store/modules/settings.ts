import { defineStore } from "pinia";
import defaultSettings from "@/settings";

export const useSettingsStore = defineStore("setting", () => {
  const title = ref("sys管理系统");
  const version = ref("1.0.0");
  const time = ref("20260412");
  const company = ref("");

  const tagsView = ref(defaultSettings.tagsView);
  const showSettings = ref<boolean>(defaultSettings.showSettings);
  const sidebarLogo = ref<boolean>(defaultSettings.sidebarLogo);
  const fixedHeader = ref(defaultSettings.fixedHeader);
  const layout = ref(defaultSettings.layout);
  const themeColor = ref(defaultSettings.themeColor);
  const theme = ref(defaultSettings.theme);
  const watermark = ref(defaultSettings.watermark);

  const settingsMap: Record<string, Ref<any>> = {
    showSettings,
    fixedHeader,
    tagsView,
    sidebarLogo,
    layout,
    themeColor,
    theme,
  };

  function changeSetting({ key, value }: { key: string; value: any }) {
    const setting = settingsMap[key];
    if (setting !== undefined) {
      setting.value = value;
      if (key === "theme" && value === "dark") {
        document.documentElement.classList.add("dark");
      } else {
        document.documentElement.classList.remove("dark");
      }
    }
  }

  return {
    title,
    version,
    time,
    company,
    showSettings,
    tagsView,
    fixedHeader,
    sidebarLogo,
    layout,
    themeColor,
    changeSetting,
    theme,
    watermark,
  };
});
