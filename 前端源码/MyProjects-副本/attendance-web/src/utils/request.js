import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:8081', // 统一后端基地址
  timeout: 5000
})

// 响应拦截器：这是最神奇的地方
request.interceptors.response.use(
  response => {
    let res = response.data;
    // 如果返回的是 Result 对象，统一在这里处理
    if (res.code === 200) {
      // 这里的 res.data 就是后端 Result 类里的那个 T data 
      return res.data; 
    } else {
      // 如果后端返回 code 500，直接弹窗报错，不需要在每个页面写 alert
      ElMessage.error(res.msg || '系统错误');
      return Promise.reject(res.msg);
    }
  },
  error => {
    ElMessage.error('服务器连接超时，请检查后端是否启动');
    return Promise.reject(error);
  }
)

export default request