import { ref, onMounted, type Ref } from 'vue'
import type { PageQuery, PageResult, R } from '@/api/types'

export interface UseTableOptions<TQuery extends PageQuery, TData> {
  /** 分页查询 API 函数 */
  apiFn: (params: TQuery) => Promise<R<PageResult<TData>>>
  /** 默认查询参数（page 从 1 开始） */
  defaultQuery: TQuery | (() => TQuery)
  /** 数据加载成功后的回调 */
  onSuccess?: (data: TData[]) => void
  /** 是否在 onMounted 时自动加载（默认 true） */
  immediate?: boolean
}

export interface UseTableReturn<TQuery extends PageQuery, TData> {
  tableData: Ref<TData[]>
  tableLoading: Ref<boolean>
  total: Ref<number>
  queryParams: Ref<TQuery>
  loadTableData: () => Promise<void>
  handleQuery: () => void
  resetQuery: (extraParams?: Partial<TQuery>) => void
}

export function useTable<TQuery extends PageQuery, TData>(
  options: UseTableOptions<TQuery, TData>
): UseTableReturn<TQuery, TData> {
  const { apiFn, defaultQuery, onSuccess, immediate = true } = options

  const tableData = ref<TData[]>([]) as Ref<TData[]>
  const tableLoading = ref(false)
  const total = ref(0)

  const getDefaults = (): TQuery =>
    typeof defaultQuery === 'function' ? defaultQuery() : { ...defaultQuery }

  const queryParams = ref<TQuery>(getDefaults()) as Ref<TQuery>

  async function loadTableData() {
    tableLoading.value = true
    try {
      const res = await apiFn(queryParams.value)
      tableData.value = res.data?.content || []
      total.value = res.data?.totalElements || 0
      onSuccess?.(tableData.value)
    } catch (e) {
      console.error('加载数据失败', e)
    } finally {
      tableLoading.value = false
    }
  }

  function handleQuery() {
    queryParams.value = { ...getDefaults(), ...queryParams.value, page: 1 as any }
    loadTableData()
  }

  function resetQuery(extraParams?: Partial<TQuery>) {
    queryParams.value = { ...getDefaults(), ...extraParams } as TQuery
    loadTableData()
  }

  if (immediate) {
    onMounted(() => {
      loadTableData()
    })
  }

  return {
    tableData,
    tableLoading,
    total,
    queryParams,
    loadTableData,
    handleQuery,
    resetQuery,
  }
}
