/** 字典类型查询参数 */
export interface DictTypeQuery extends PageQuery {
  dictType?: string;
  dictName?: string;
  status?: string;
}

/** 字典类型表单 */
export interface DictTypeForm {
  id?: number;
  dictType: string;
  dictName: string;
  remark?: string;
}

/** 字典类型视图对象 */
export interface DictTypeVO {
  id: number;
  dictType: string;
  dictName: string;
  itemCount?: number;
  status: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

/** 字典项查询参数 */
export interface DictItemQuery extends PageQuery {
  dictType: string;
  status?: string;
}

/** 字典项表单 */
export interface DictItemForm {
  id?: number;
  dictType: string;
  dictCode: string;
  dictLabel: string;
  dictValue: string;
  sort: number;
  remark?: string;
  status: string;
}

/** 字典项视图对象 */
export interface DictItemVO {
  id: number;
  dictType: string;
  dictCode: string;
  dictLabel: string;
  dictValue: string;
  sort: number;
  status: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

/** 字典翻译项 */
export interface DictTranslationItem {
  code: string;
  label: string;
  value: string;
}
