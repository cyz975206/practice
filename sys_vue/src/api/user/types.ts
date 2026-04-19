/** 用户查询参数 */
export interface UserQuery extends PageQuery {
  username?: string;
  nickname?: string;
  orgCode?: string;
  status?: string;
}

/** 用户表单 */
export interface UserForm {
  id?: number;
  username: string;
  password?: string;
  nickname: string;
  personId?: number;
  orgCode: string;
  status: string;
}

/** 用户视图对象 */
export interface UserVO {
  id: number;
  username: string;
  nickname: string;
  personId?: number;
  orgCode: string;
  status: string;
  loginFailCount: number;
  lastLoginTime?: string;
  passwordUpdateTime?: string;
  roleNames?: string[];
  createTime?: string;
  updateTime?: string;
}
