<template>
  <el-dialog
    :model-value="modelValue"
    title="修改密码"
    :width="600"
    destroy-on-close
    @close="closeDialog"
  >
    <el-form
      ref="passwordFormRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input
          v-model="formData.oldPassword"
          show-password
          type="password"
          maxlength="20"
          placeholder="请输入旧密码"
        />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="formData.newPassword"
          show-password
          type="password"
          maxlength="20"
          autocomplete="off"
          placeholder="请输入新密码"
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="formData.confirmPassword"
          maxlength="20"
          autocomplete="off"
          show-password
          type="password"
          placeholder="请再次输入新密码"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { changePassword } from '@/api/user';

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
}>();

const submitLoading = ref(false);
const passwordFormRef = ref<any>(null);

const formData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== formData.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const validateNewPassword = (_rule: any, value: string, callback: Function) => {
  if (value.length < 8 || value.length > 20) {
    callback(new Error('新密码长度必须在8到20个字符'));
  } else if (!/^(?=.*[A-Za-z])(?=.*\d).+$/.test(value)) {
    callback(new Error('密码必须包含字母和数字'));
  } else {
    callback();
  }
};

const rules = reactive({
  oldPassword: [
    { required: true, message: '旧密码不能为空', trigger: 'blur' },
    { min: 3, max: 20, message: '旧密码长度为3到20个字符', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
});

function closeDialog() {
  emit('update:modelValue', false);
  passwordFormRef.value?.clearValidate();
  formData.oldPassword = '';
  formData.newPassword = '';
  formData.confirmPassword = '';
}

async function handleSubmit() {
  const valid = await passwordFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitLoading.value = true;
  try {
    await changePassword({
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword,
    });
    ElMessage.success('密码修改成功');
    closeDialog();
  } catch {
    // error handled by request interceptor
  } finally {
    submitLoading.value = false;
  }
}

defineExpose({
  closeDialog,
});
</script>

<style lang="scss" scoped></style>
