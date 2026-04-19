<template>
  <div class="login-container">
    <div class="flex">
      <div class="login-left-bg"></div>
      <el-card class="z-1 !border-none w-[445px]">
        <div class="text-center">
          <h2 class="font-bold text-xl" style="padding-top: 30px">
            {{ title }}
          </h2>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginData"
          :rules="loginRules"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              ref="usernameRef"
              v-model="loginData.username"
              class="flex-1 h-[45px] px-3"
              size="large"
              placeholder="请输入用户名"
              name="username"
            />
          </el-form-item>
          <el-tooltip
            :disabled="isCapslock === false"
            content="大写锁定已开启"
            placement="right"
          >
            <el-form-item prop="password">
              <el-input
                v-model="loginData.password"
                class="flex-1 h-[45px] px-3"
                placeholder="请输入密码"
                :type="passwordVisible ? 'input' : 'password'"
                size="large"
                name="password"
                @keyup="checkCapslock"
                @keyup.enter="handleLogin"
              />
              <span class="mr-2" @click="passwordVisible = !passwordVisible">
                <svg-icon
                  :icon-class="passwordVisible ? 'eye-open' : 'eye'"
                  class="cursor-pointer"
                />
              </span>
            </el-form-item>
          </el-tooltip>

          <el-button
            :loading="loading"
            type="primary"
            class="w-full h-[50px] text-base"
            @click.prevent="handleLogin"
          >
            登 录
          </el-button>
        </el-form>
      </el-card>
    </div>

    <div
      class="absolute bottom-1 text-[14px] text-center"
      v-show="useAppStore().device == 'desktop'"
    >
      <p>{{ company }}</p>
    </div>

    <div class="absolute bottom-0 right-2 text-[#909399] text-[11px]">
      {{ `${version}-${time}` }}
    </div>
  </div>
</template>

<script setup lang="ts">
import router from "@/router";
import { useRoute } from "vue-router";
import SvgIcon from "@/components/SvgIcon/index.vue";
import { useSettingsStore } from "@/store/modules/settings";
import { useUserStore } from "@/store/modules/user";
import { useAppStore } from "@/store/modules/app";
import { useDebounceFn } from "@vueuse/core";
import type { LoginData } from "@/api/auth/types";

const settingsStore = useSettingsStore();
const { title, version, time, company } = settingsStore;

const appStore = useAppStore();
const WIDTH = 992;
const { width } = useWindowSize();
watchEffect(() => {
  if (width.value < WIDTH) {
    appStore.toggleDevice("mobile");
  } else {
    appStore.toggleDevice("desktop");
  }
});

const loading = ref(false);
const isCapslock = ref(false);
const passwordVisible = ref(false);
const loginFormRef = ref();

const loginData = ref<LoginData>({
  username: "",
  password: "",
});

const loginRules = computed(() => {
  return {
    username: [
      {
        required: true,
        trigger: "blur",
        message: "请输入用户名",
      },
    ],
    password: [
      {
        required: true,
        trigger: "blur",
        message: "请输入密码",
      },
    ],
  };
});

function checkCapslock(e: KeyboardEvent) {
  const { key } = e;
  isCapslock.value = key && key.length === 1 && key >= "A" && key <= "Z";
}

const route = useRoute();
const userStore = useUserStore();
const debouncedLogin = useDebounceFn(handleLogin, 500);

function handleLogin() {
  loginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true;
      try {
        await userStore.login(loginData.value);
        const redirect = (route.query.redirect as string) ?? "/";
        router.push(redirect);
      } catch (error: any) {
        // 错误已在 request interceptor 中处理
      } finally {
        loading.value = false;
      }
    }
  });
}

onMounted(() => {
  document.documentElement.classList.remove("dark");
});
</script>

<style lang="scss" scoped>
.login-container {
  @apply w-full h-full flex-center;

  overflow-y: auto;
  background: url("@/assets/images/webLogin-1.jpg") no-repeat center center /
    cover;

  .login-left-bg {
    width: 800px;
    height: 450px;
  }

  .login-form {
    padding: 30px 10px;
    padding-top: 50px;

    .el-button {
      margin-top: 40px;
    }
  }
}

.el-form-item {
  background: var(--el-input-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 5px;
}

:deep(.el-input) {
  .el-input__wrapper {
    padding: 0;
    background-color: transparent;
    box-shadow: none !important;

    &.is-focus,
    &:hover {
      box-shadow: none !important;
    }

    input:-webkit-autofill {
      transition: background-color 1000s ease-in-out 0s;
    }
  }
}
</style>
