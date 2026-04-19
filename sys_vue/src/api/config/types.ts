/** 系统配置查询参数 */
export interface ConfigQuery extends PageQuery {
  configKey?: string;
  configName?: string;
}

/** 系统配置表单 */
export interface ConfigForm {
  id?: number;
  configKey: string;
  configName: string;
  configValue: string;
  remark?: string;
  sort: number;
}

/** 系统配置视图对象 */
export interface ConfigVO {
  id: number;
  configKey: string;
  configName: string;
  configValue: string;
  remark?: string;
  sort: number;
  createTime?: string;
  updateTime?: string;
}
