import { ref, shallowRef } from 'vue'
import { getDictTranslation } from '@/api/dict/translation'
import type { DictTranslationItem } from '@/api/dict/types'

// ---- Module-level cache (shared across all composable instances) ----

const dictCache = new Map<string, DictTranslationItem[]>()
const pendingRequests = new Map<string, Promise<DictTranslationItem[]>>()

// ---- Types ----

export interface DictOption {
  label: string
  value: string
}

export interface UseDictReturn {
  loading: ReturnType<typeof ref<boolean>>
  options: ReturnType<typeof shallowRef<DictOption[]>>
  getLabel: (dictType: string, code: string) => string
  getTagType: (dictType: string, code: string, tagMap?: Record<string, string>) => string
  getCode: (dictType: string, codeOrValue: string) => string
  reload: (dictType?: string) => Promise<void>
}

// ---- Composable ----

export function useDict(dictType: string): UseDictReturn
export function useDict(dictTypes: string[]): UseDictReturn
export function useDict(dictTypes: string | string[]): UseDictReturn {
  const types = Array.isArray(dictTypes) ? dictTypes : [dictTypes]

  const loading = ref(false)
  const options = shallowRef<DictOption[]>([])

  function findItem(dt: string, code: string): DictTranslationItem | undefined {
    const items = dictCache.get(dt)
    if (!items) return undefined
    return items.find(item => item.code === code || item.value === code)
  }

  function getLabel(dt: string, code: string): string {
    return findItem(dt, code)?.label ?? code
  }

  function getTagType(dt: string, code: string, tagMap?: Record<string, string>): string {
    return tagMap?.[code] ?? ''
  }

  function getCode(dt: string, codeOrValue: string): string {
    const item = findItem(dt, codeOrValue)
    return item?.code ?? codeOrValue
  }

  async function fetchDict(type: string): Promise<DictTranslationItem[]> {
    if (dictCache.has(type)) return dictCache.get(type)!
    if (pendingRequests.has(type)) return pendingRequests.get(type)!

    const promise = getDictTranslation(type).then(res => {
      const raw = res.data
      const items: DictTranslationItem[] = Array.isArray(raw) ? raw : (raw?.translations ?? [])
      dictCache.set(type, items)
      pendingRequests.delete(type)
      return items
    }).catch(err => {
      pendingRequests.delete(type)
      console.error(`Failed to load dict: ${type}`, err)
      return [] as DictTranslationItem[]
    })

    pendingRequests.set(type, promise)
    return promise
  }

  function rebuildOptions() {
    if (types.length === 1) {
      const items = dictCache.get(types[0]) ?? []
      options.value = items.map(item => ({ label: item.label, value: item.value }))
    } else {
      const all: DictOption[] = []
      for (const t of types) {
        const items = dictCache.get(t) ?? []
        all.push(...items.map(item => ({ label: item.label, value: item.value })))
      }
      options.value = all
    }
  }

  async function loadAll() {
    loading.value = true
    try {
      await Promise.all(types.map(t => fetchDict(t)))
      rebuildOptions()
    } finally {
      loading.value = false
    }
  }

  async function reload(dt?: string) {
    if (dt) {
      dictCache.delete(dt)
      await fetchDict(dt)
      rebuildOptions()
    } else {
      for (const t of types) dictCache.delete(t)
      await loadAll()
    }
  }

  if (types.some(t => !dictCache.has(t))) {
    loadAll()
  } else {
    rebuildOptions()
  }

  return { loading, options, getLabel, getTagType, getCode, reload }
}
