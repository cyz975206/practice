import { ElMessage, ElMessageBox } from 'element-plus'

interface UseConfirmDeleteOptions {
  /** 删除 API 函数 */
  deleteFn: (id: number) => Promise<any>
  /** 用于确认消息的名称字段 */
  nameKey?: string
  /** 实体中文名（如 '用户'、'配置'） */
  entityName: string
  /** 删除成功后的回调 */
  afterDelete?: () => void
}

export function useConfirmDelete(options: UseConfirmDeleteOptions) {
  const { deleteFn, nameKey = 'name', entityName, afterDelete } = options

  async function handleDelete(row: Record<string, any>) {
    try {
      await ElMessageBox.confirm(
        `确认删除${entityName}「${row[nameKey]}」?`,
        '提示',
        { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
      )
      await deleteFn(row.id)
      ElMessage.success('删除成功')
      afterDelete?.()
    } catch {
      // 用户取消
    }
  }

  return { handleDelete }
}
