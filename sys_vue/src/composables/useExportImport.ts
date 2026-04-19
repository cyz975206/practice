import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadInstance } from 'element-plus'
import type { R } from '@/api/types'

interface UseExportImportOptions<TQuery> {
  /** 实体中文名 */
  entityName: string
  /** 导出 API */
  exportFn: (params: TQuery) => Promise<any>
  /** 导入 API */
  importFn: (file: File) => Promise<R<any>>
  /** 当前查询参数 */
  queryParams: { value: TQuery }
  /** 导入成功后的回调 */
  afterImport?: () => void
}

export function useExportImport<TQuery>(options: UseExportImportOptions<TQuery>) {
  const { entityName, exportFn, importFn, queryParams, afterImport } = options
  const importDialogVisible = ref(false)
  const importLoading = ref(false)
  const uploadRef = ref<UploadInstance>()

  async function handleExport() {
    try {
      const res = await exportFn(queryParams.value)
      const blob = new Blob([res as any], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${entityName}数据_${new Date().getTime()}.xlsx`
      link.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    } catch (e) {
      console.error('导出失败', e)
    }
  }

  function handleImport() {
    importDialogVisible.value = true
  }

  function handleExceed() {
    ElMessage.warning('只能上传一个文件，请先移除已选文件')
  }

  async function submitImport() {
    const files = uploadRef.value?.uploadFiles
    if (!files || files.length === 0) {
      ElMessage.warning('请选择要导入的文件')
      return
    }
    importLoading.value = true
    try {
      await importFn(files[0].raw as File)
      ElMessage.success('导入成功')
      importDialogVisible.value = false
      afterImport?.()
    } catch (e) {
      console.error('导入失败', e)
    } finally {
      importLoading.value = false
    }
  }

  return { importDialogVisible, importLoading, uploadRef, handleExport, handleImport, handleExceed, submitImport }
}
