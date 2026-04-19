/** 操作日志查询参数 */
export interface OperationLogQuery extends PageQuery {
  operatorName?: string;
  module?: string;
  operationType?: string;
  result?: string;
  startTime?: string;
  endTime?: string;
}

/** 操作日志视图对象 */
export interface OperationLogVO {
  id: number;
  operatorId: number;
  operatorName: string;
  operatorIp: string;
  operatorOrgCode?: string;
  operatorOrg?: string;
  module: string;
  operationType: string;
  operationContent?: string;
  requestParams?: string;
  operationResult: string;
  errorMsg?: string;
  operationTime: string;
}

/** 登录日志查询参数 */
export interface LoginLogQuery extends PageQuery {
  username?: string;
  loginType?: string;
  loginResult?: string;
  startTime?: string;
  endTime?: string;
}

/** 登录日志视图对象 */
export interface LoginLogVO {
  id: number;
  userId?: number;
  username?: string;
  loginIp?: string;
  loginLocation?: string;
  loginDevice?: string;
  loginType: string;
  loginResult: string;
  failReason?: string;
  loginTime: string;
  logoutTime?: string;
  isAbnormal?: boolean;
}

/** 系统日志查询参数 */
export interface SystemLogQuery extends PageQuery {
  logLevel?: string;
  logModule?: string;
  startTime?: string;
  endTime?: string;
}

/** 系统日志视图对象 */
export interface SystemLogVO {
  id: number;
  logLevel: string;
  logModule: string;
  logContent?: string;
  exceptionStack?: string;
  occurTime: string;
  serverIp?: string;
}

/** 安全日志查询参数 */
export interface SecurityLogQuery extends PageQuery {
  riskType?: string;
  riskLevel?: string;
  handleStatus?: string;
  startTime?: string;
  endTime?: string;
}

/** 安全日志处理表单 */
export interface SecurityLogHandleForm {
  handleStatus: string;
  handleNote?: string;
}

/** 安全日志视图对象 */
export interface SecurityLogVO {
  id: number;
  operatorId?: number;
  operatorName?: string;
  operatorIp?: string;
  riskType: string;
  riskContent?: string;
  requestUrl?: string;
  riskLevel: string;
  handleStatus: string;
  handleUserId?: number;
  handleTime?: string;
  handleNote?: string;
  occurTime: string;
}
