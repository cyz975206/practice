export enum Request {
  GET = "get",
  POST = "post",
  PUT = "put",
  DELETE = "delete",
}

export enum Result {
  SUCCESS = 1,
  FAIL = 0,
  TIMEOUT = 401,
  TYPE = "success",
}

export enum ContentType {
  JSON = "application/json;charset=UTF-8",
  FORM_URLENCODED = "application/x-www-form-urlencoded;charset=UTF-8",
  FORM_DATA = "multipart/form-data",
  EXCEL = "application/vnd.ms-excel;charset=UTF-8",
}
