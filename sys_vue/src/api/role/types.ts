/** 角色查询参数 */
export interface RoleQuery extends PageQuery {
  roleName?: string;
  status?: string;
}

/** 角色表单 */
export interface RoleForm {
  id?: number;
  roleCode: string;
  roleName: string;
  roleDesc?: string;
  status: string;
  sort: number;
}

/** 角色视图对象 */
export interface RoleVO {
  id: number;
  roleCode: string;
  roleName: string;
  roleDesc?: string;
  status: string;
  sort: number;
  createTime?: string;
  updateTime?: string;
}
