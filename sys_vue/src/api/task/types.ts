/** 定时任务查询参数 */
export interface TaskQuery extends PageQuery {
  name?: string;
  hasStart?: boolean;
}

/** 定时任务表单 */
export interface TaskForm {
  id?: number;
  name: string;
  serviceName: string;
  funPath: string;
  cron: string;
  hasStart: boolean;
}

/** 定时任务视图对象 */
export interface TaskVO {
  id: number;
  name: string;
  serviceName: string;
  funPath: string;
  cron: string;
  hasStart: boolean;
  createTime?: string;
  updateTime?: string;
}
