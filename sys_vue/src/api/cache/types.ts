/** 在线用户 */
export interface OnlineUserVO {
  userId: number;
  username: string;
  nickname: string;
  orgCode?: string;
  loginIp?: string;
  loginTime?: string;
  lastAccessTime?: string;
}

/** 缓存统计 */
export interface CacheStatsVO {
  dictCacheCount: number;
  menuCacheCount: number;
  configCacheCount: number;
  permsCacheCount: number;
  onlineUserCount: number;
}
