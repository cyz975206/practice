import { ElMessage, ElMessageBox } from 'element-plus'
import type { R } from '@/api/types'

interface UseStatusToggleOptions {
  /** 更新状态 API 函数 */
  updateStatusFn: (id: number, status: string) => Promise<R<any>>
  /** 用于确认消息的名称字段 */
  nameKey?: string
  /** 实体中文名 */
  entityName: string
  /** 启用值（默认 '1'） */
  enabledValue?: string
  /** 停用值（默认 '0'） */
  disabledValue?: string
  /** 状态标签 */
  labels?: { enable: string; disable: string }
  /** 切换成功后的回调 */
  afterToggle?: () => void
}

export function useStatusToggle(options: UseStatusToggleOptions) {
  const {
    updateStatusFn,
    nameKey = 'name',
    entityName,
    enabledValue = '1',
    disabledValue = '0',
    labels = { enable: '启用', disable: '停用' },
    afterToggle,
  } = options

  async function handleToggleStatus(row: Record<string, any>) {
    const isEnabled = row.status === enabledValue
    const newStatus = isEnabled ? disabledValue : enabledValue
    const action = isEnabled ? labels.disable : labels.enable
    try {
      await ElMessageBox.confirm(
        `确认${action}${entityName}「${row[nameKey]}」?`,
        '提示',
        { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
      )
      await updateStatusFn(row.id, newStatus)
      ElMessage.success(`${action}成功`)
      afterToggle?.()
    } catch {
      // 用户取消
    }
  }

  return { handleToggleStatus }
}
