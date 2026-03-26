<template>
  <div v-if="!isLogin">
    <LoginView />
  </div>

  <div v-else class="main-app">
    <el-menu mode="horizontal" default-active="1" @select="handleSelect" background-color="#545c64" text-color="#fff">
      <el-menu-item index="1">员工打卡</el-menu-item>
      <el-menu-item index="2" v-if="userRole && userRole.startsWith('ADMIN')">管理员后台</el-menu-item>
      
      <div class="user-info-tag">
        <span>欢迎您，{{ userName }}</span>
        <el-button type="info" link @click="handleLogout" style="margin-left: 20px;">退出登录</el-button>
      </div>
    </el-menu>

    <div class="content">
      <UserView v-if="activeTab === '1'" />
      <AdminView v-if="activeTab === '2'" />
    </div>
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import LoginView from './views/LoginView.vue'
import UserView from './views/UserView.vue'
import AdminView from './views/AdminView.vue'

// 1. 初始化登录状态
const isLogin = ref(false)
const activeTab = ref('1')
const userName = ref('')
const userRole = ref('')

// 2. 从浏览器缓存里读取用户信息
const savedUser = JSON.parse(localStorage.getItem('user') || 'null')
if (savedUser) {
  isLogin.value = true
  userName.value = savedUser.realName || savedUser.username
  userRole.value = savedUser.role // 假设数据库里有角色字段
}

// 3. 菜单切换逻辑
const handleSelect = (index) => {
  console.log("当前点击的菜单索引是：", index) // F12看这里
  activeTab.value = index
}

// 4. 退出登录逻辑
const handleLogout = () => {
  localStorage.removeItem('user') // 清空缓存
  location.reload() // 刷新页面，回到登录态
}

// 监听登录状态或在挂载时获取
onMounted(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  userRole.value = user.role // 确保这里拿到的是 'ADMIN_HR' 等字符串
})
</script>

<style>
body { margin: 0; font-family: sans-serif; }
.user-info-tag {
  float: right;
  line-height: 60px;
  color: white;
  padding-right: 20px;
}
.content { padding: 20px; }
</style>