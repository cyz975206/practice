/** 登录请求参数 */
export interface LoginData {
  username: string;
  password: string;
}

/** 登录响应数据 */
export interface LoginResult {
  /** JWT Token */
  token: string;
  /** 用户信息 */
  userInfo: UserInfo;
}

/** 当前登录用户信息 */
export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  avatar?: string;
  roles: string[];
  perms: string[];
  orgCode?: string;
}
