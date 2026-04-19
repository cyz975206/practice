/** 菜单查询参数 */
export interface MenuQuery extends PageQuery {
  menuName?: string;
  menuType?: string;
  status?: string;
}

/** 菜单表单 */
export interface MenuForm {
  id?: number;
  menuCode: string;
  menuName: string;
  menuType: string;
  parentId?: number;
  path?: string;
  component?: string;
  perms?: string;
  icon?: string;
  isFrame?: number;
  sort: number;
  status: string;
}

/** 菜单视图对象 */
export interface MenuVO {
  id: number;
  menuCode: string;
  menuName: string;
  menuType: string;
  parentId: number;
  path: string;
  component: string;
  perms: string;
  icon: string;
  isFrame: number;
  sort: number;
  status: string;
  children?: MenuVO[];
  createTime?: string;
  updateTime?: string;
}
