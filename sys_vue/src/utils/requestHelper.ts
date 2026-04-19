export function showErrorStatus(status: number | string) {
  let message = "";
  switch (status) {
    case 400:
      message = "请求错误（400）";
      break;
    // case 401:
    //   message = "未授权，请重新登录（401）";
    //   break;
    case 404:
      message = "请求出错（404）";
      break;
    case 500:
      message = "哎呀，出了一点小差错，请稍后再试~";
      break;
    case 502:
      message = "网络错误（502）";
      break;
    case 504:
      message = "网络超时（504）";
      break;
    case "1000":
      message = "您暂时没有权限浏览当前功能项";
      break;
    default:
      message = `连接出错（${status}）`;
      break;
  }
  return message;
}
