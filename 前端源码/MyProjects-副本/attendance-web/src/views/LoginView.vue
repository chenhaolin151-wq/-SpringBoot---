<template>
  <div class="login-container">
    <el-card class="login-box">
      <h2>系统登录</h2>
      <el-form :model="loginForm">
        <el-form-item>
          <el-input v-model="loginForm.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-button type="primary" @click="handleLogin" style="width: 100%">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loginForm = ref({ username: '', password: '' })

const handleLogin = async () => {
  try {
    const res = await request.post('/api/user/login', loginForm.value)
    if (typeof res === 'string' && res.startsWith('FAILED')) {
      ElMessage.error(res)
    } else {
      ElMessage.success('登录成功！')
      // 关键：把用户信息存入本地浏览器，这样刷新页面也不会丢
      localStorage.setItem('user', JSON.stringify(res))
      // 跳转到主页（这里可以控制 App.vue 切换状态）
      location.reload() 
    }
  } catch (err) {
    ElMessage.error('服务器连接失败')
  }
}
</script>

<style scoped>
.login-container { height: 100vh; display: flex; justify-content: center; align-items: center; background: #2d3a4b; }
.login-box { width: 400px; text-align: center; }
</style>