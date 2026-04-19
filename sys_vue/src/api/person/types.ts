/** 人员查询参数 */
export interface PersonQuery extends PageQuery {
  fullName?: string;
  staffNum?: string;
  orgCode?: string;
  status?: string;
}

/** 人员表单 */
export interface PersonForm {
  id?: number;
  surname: string;
  givenName: string;
  idCard?: string;
  phone?: string;
  staffNum: string;
  orgCode: string;
  status: string;
}

/** 人员视图对象 */
export interface PersonVO {
  id: number;
  surname: string;
  givenName: string;
  fullName: string;
  idCard?: string;
  phone?: string;
  staffNum: string;
  orgCode: string;
  status: string;
  createTime?: string;
  updateTime?: string;
}
