import { createApp } from 'vue'
import App from './App.vue' // 确保这里指向的是 App.vue

// 1. 引入刚才安装的 Element Plus 漂亮组件库
import ElementPlus from 'element-plus'
// 2. 引入组件库配套的样式文件（没它按钮就不漂亮了）
import 'element-plus/dist/index.css'

const app = createApp(App)

// 3. 告诉 Vue：我们要使用这个组件库
app.use(ElementPlus)

app.mount('#app')