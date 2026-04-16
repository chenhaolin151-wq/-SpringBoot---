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
import { useRouter } from 'vue-router'

const loginForm = ref({ username: '', password: '' })

const router = useRouter()

const handleLogin = async () => {
  const res = await request.post('/api/user/login', loginForm.value)
  if (typeof res !== 'string') {
    ElMessage.success('登录成功！')
    localStorage.setItem('user', JSON.stringify(res))
    if (res.role === 'ADMIN_HR' || res.role === 'ADMIN_NORMAL') {
      router.push('/admin')
    } else {
      router.push('/user')
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #2d3a4b;
}

.login-box {
  width: 400px;
  text-align: center;
}
</style>