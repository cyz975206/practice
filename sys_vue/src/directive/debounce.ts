import type { Directive, DirectiveBinding } from "vue";

interface ElType extends HTMLElement {
  __debounceHandler__: (event: Event) => void;
}

// 默认配置
const DEFAULT_DELAY = 300;

/**
 * 防抖指令
 * @example v-debounce="handleClick" 只传方法，使用默认延迟时间300ms
 * @example v-debounce="[handleClick, 500]" 第一个参数是绑定的方法，第二个参数是延迟时间
 */
export const debounce: Directive = {
  mounted(el: ElType, binding: DirectiveBinding) {
    let fn: Function;
    let delay: number = DEFAULT_DELAY;

    // 解析参数
    if (typeof binding.value === "function") {
      fn = binding.value;
    } else if (Array.isArray(binding.value)) {
      [fn, delay = DEFAULT_DELAY] = binding.value;
      if (typeof fn !== "function") {
        console.error("v-debounce指令第一个参数必须是函数");
        return;
      }
      if (typeof delay !== "number") {
        console.error("v-debounce指令第二个参数必须是数字");
        return;
      }
    } else {
      console.error("v-debounce指令参数格式错误");
      return;
    }

    // 创建防抖处理函数
    let timer: NodeJS.Timeout | null = null;
    el.__debounceHandler__ = function (event: Event) {
      if (timer) {
        clearTimeout(timer);
      }
      timer = setTimeout(() => {
        // 模拟 @click="subit()" 的行为，直接调用函数而不传参
        fn();
      }, delay);
    };

    // 绑定点击事件
    el.addEventListener("click", el.__debounceHandler__);
  },

  beforeUnmount(el: ElType) {
    // 移除事件监听
    el.removeEventListener("click", el.__debounceHandler__);
  },
};
