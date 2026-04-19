import { defineStore } from "pinia";
import { useSessionStorage } from "@vueuse/core";
import defaultSettings from "@/settings";

// setup
export const useAppStore = defineStore("app", () => {
  // state
  const device = ref("desktop");
  const size = ref(defaultSettings.size);

  const sidebarStatus = useSessionStorage("sidebarStatus", "closed");

  const sidebar = reactive({
    opened: sidebarStatus.value !== "closed",
    withoutAnimation: false,
  });
  const activeTopMenu = ref("");

  // actions
  function toggleSidebar() {
    sidebar.opened = !sidebar.opened;
    sidebar.withoutAnimation = false;
    if (sidebar.opened) {
      sidebarStatus.value = "opened";
    } else {
      sidebarStatus.value = "closed";
    }
  }

  function closeSideBar(withoutAnimation: boolean) {
    sidebar.opened = false;
    sidebar.withoutAnimation = withoutAnimation;
    sidebarStatus.value = "closed";
  }

  function openSideBar(withoutAnimation: boolean) {
    sidebar.opened = true;
    sidebar.withoutAnimation = withoutAnimation;
    sidebarStatus.value = "opened";
  }

  function toggleDevice(val: string) {
    device.value = val;
  }

  function changeSize(val: string) {
    size.value = val;
  }

  /**
   * 混合模式顶部切换
   */
  function changeTopActive(val: string) {
    activeTopMenu.value = val;
  }
  return {
    device,
    sidebar,
    size,
    activeTopMenu,
    toggleDevice,
    changeSize,
    toggleSidebar,
    closeSideBar,
    openSideBar,
    changeTopActive,
  };
});
