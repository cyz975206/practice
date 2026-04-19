/** 机构查询参数 */
export interface OrgQuery extends PageQuery {
  orgShortName?: string;
  orgLevel?: string;
  status?: string;
  parentOrgCode?: string;
}

/** 机构表单 */
export interface OrgForm {
  id?: number;
  orgCode?: string;
  orgShortName: string;
  orgFullName: string;
  orgLevel: string;
  parentOrgCode?: string;
  leaderUserId?: number;
  status: string;
  sort: number;
}

/** 机构视图对象 */
export interface OrgVO {
  id: number;
  orgCode: string;
  orgShortName: string;
  orgFullName: string;
  orgLevel: string;
  parentOrgCode: string;
  leaderUserId?: number;
  status: string;
  sort: number;
  children?: OrgVO[];
  createTime?: string;
  updateTime?: string;
}

/** 排序表单 */
export interface OrgSortForm {
  ids: number[];
  sorts: number[];
}
