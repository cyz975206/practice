/** 后端统一响应包装 R<T> */
export interface R<T = any> {
  /** 状态码，200 表示成功 */
  code: number;
  /** 提示信息 */
  message: string;
  /** 响应数据 */
  data: T;
}

/** 分页查询参数（0-based） */
export interface PageQuery {
  /** 页码（从 0 开始） */
  page: number;
  /** 每页条数 */
  size: number;
}

/** 分页响应（Spring Data JPA Page 格式） */
export interface PageResult<T> {
  /** 数据列表 */
  content: T[];
  /** 总记录数 */
  totalElements: number;
  /** 总页数 */
  totalPages: number;
  /** 每页条数 */
  size: number;
  /** 当前页码（0-based） */
  number: number;
}
