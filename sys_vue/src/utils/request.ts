import axios, { AxiosResponse, InternalAxiosRequestConfig, type AxiosRequestConfig } from "axios";
import { ContentType } from "@/enums/http";
import { showErrorStatus } from "./requestHelper";

export const BASE_URL: string = import.meta.env.VITE_APP_BASE_API as string;

const config = {
  baseURL: BASE_URL,
  timeout: 50000,
  headers: {
    "Content-Type": ContentType.JSON,
  },
};

// 创建 axios 实例
const service = axios.create(config);

// 防止多个 401 弹窗同时出现
let isRefreshing = false;

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem("access_token");
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  (error: any) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 响应数据为二进制流处理(文件下载/导出)
    if (response.data instanceof ArrayBuffer || response.data instanceof Blob) {
      return response;
    }

    const { code, message } = response.data;

    if (response.status === 200) {
      if (code === 200) {
        return response.data;
      }

      ElMessage.error(message || "请求失败");
      return Promise.reject(new Error(message || "请求失败"));
    }

    ElMessage.error(message || "请求失败");
    return Promise.reject(new Error(message || "请求失败"));
  },
  (error: any) => {
    if (error.response) {
      if (error.response.status === 401 && !isRefreshing) {
        isRefreshing = true;
        localStorage.removeItem("access_token");
        ElMessage.warning("登录已过期，请重新登录");
        window.location.href = import.meta.env.BASE_URL + "#/login";
      } else {
        ElMessage.error(showErrorStatus(error.response.status));
      }
    }
    return Promise.reject(error);
  }
);

export function uploadFile<T = any>(
  config: Omit<AxiosRequestConfig, "method" | "data" | "headers">,
  data: FormData | Record<string, any>
): Promise<T> {
  return service.request({
    ...config,
    method: "POST",
    data,
    headers: { "Content-Type": ContentType.FORM_DATA },
  });
}

export function downloadFile<T = any>(
  config: Omit<AxiosRequestConfig, "method" | "responseType" | "headers">
): Promise<T> {
  return service.request({
    ...config,
    method: "POST",
    responseType: "arraybuffer",
    headers: {
      accept: "*/*",
      "Content-Type": "application/json",
      "Accept-Encoding": "gzip, deflate",
    },
  });
}

// 导出 axios 实例
export default service;
