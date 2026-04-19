import { ref, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { R } from '@/api/types'

export interface UseFormDialogOptions<TForm> {
  /** 新增时的默认表单值 */
  defaultForm: TForm | (() => TForm)
  /** 新增弹窗标题 */
  createTitle: string
  /** 编辑弹窗标题 */
  editTitle: string
  /** 创建 API */
  createFn: (data: TForm) => Promise<R<any>>
  /** 更新 API */
  updateFn: (id: number, data: TForm) => Promise<R<any>>
  /** 表单校验规则 */
  formRules?: FormRules
  /** 提交成功后的回调（如刷新列表） */
  afterSubmit?: () => void
}

export interface UseFormDialogReturn<TForm extends { id?: number }> {
  dialogVisible: Ref<boolean>
  dialogTitle: Ref<string>
  submitLoading: Ref<boolean>
  formRef: Ref<FormInstance | undefined>
  formData: Ref<TForm>
  formRules: FormRules
  handleAdd: (extraDefaults?: Partial<TForm>) => void
  handleEdit: (row: TForm & { id: number }, formMapping?: (row: any) => TForm) => void
  handleSubmit: () => Promise<void>
  resetForm: () => void
}

export function useFormDialog<TForm extends { id?: number }>(
  options: UseFormDialogOptions<TForm>
): UseFormDialogReturn<TForm> {
  const {
    defaultForm,
    createTitle,
    editTitle,
    createFn,
    updateFn,
    formRules = {},
    afterSubmit,
  } = options

  const getDefaults = (): TForm =>
    typeof defaultForm === 'function' ? defaultForm() : { ...defaultForm }

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const submitLoading = ref(false)
  const formRef = ref<FormInstance>()
  const formData = ref<TForm>(getDefaults()) as Ref<TForm>

  function handleAdd(extraDefaults?: Partial<TForm>) {
    dialogTitle.value = createTitle
    formData.value = { ...getDefaults(), ...extraDefaults } as TForm
    dialogVisible.value = true
  }

  function handleEdit(
    row: TForm & { id: number },
    formMapping?: (row: any) => TForm
  ) {
    dialogTitle.value = editTitle
    formData.value = formMapping
      ? formMapping(row)
      : ({ ...row } as TForm)
    dialogVisible.value = true
  }

  async function handleSubmit() {
    await formRef.value?.validate()
    submitLoading.value = true
    try {
      if (formData.value.id) {
        await updateFn(formData.value.id, formData.value)
        ElMessage.success('更新成功')
      } else {
        await createFn(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      afterSubmit?.()
    } catch (e) {
      console.error('提交失败', e)
    } finally {
      submitLoading.value = false
    }
  }

  function resetForm() {
    formRef.value?.resetFields()
  }

  return {
    dialogVisible,
    dialogTitle,
    submitLoading,
    formRef,
    formData,
    formRules,
    handleAdd,
    handleEdit,
    handleSubmit,
    resetForm,
  }
}
