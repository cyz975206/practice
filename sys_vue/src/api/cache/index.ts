import request from "@/utils/request";

/** 获取在线用户列表 */
export function getOnlineUsers() {
  return request({ url: "/api/cache/online-users", method: "get" });
}

/** 强制用户下线 */
export function forceOffline(userId: number) {
  return request({ url: `/api/cache/online-users/${userId}`, method: "delete" });
}

/** 获取缓存统计 */
export function getCacheStats() {
  return request({ url: "/api/cache/stats", method: "get" });
}

/** 清理指定缓存 */
export function clearCache(type: string) {
  return request({ url: "/api/cache/clear", method: "delete", params: { type } });
}
