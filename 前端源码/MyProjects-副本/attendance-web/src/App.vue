<template>
  <div id="app">
    <el-container v-if="isLoggedIn" class="layout-container">
      <el-header class="header">
        <div class="header-left">
          <span class="logo">考勤管理系统</span>
          <el-menu mode="horizontal" :default-active="activePath" background-color="#409EFF" text-color="#fff"
            active-text-color="#ffd04b" router :ellipsis="false" style="border: none; margin-left: 40px;">
            <el-menu-item index="/user">个人打卡</el-menu-item>

            <el-menu-item v-if="currentUser.role === 'ADMIN_HR' || currentUser.role === 'ADMIN_NORMAL'" index="/admin">
              管理面板
            </el-menu-item>
          </el-menu>
        </div>

        <div class="user-info">
          <span>{{ currentUser.realName }} ({{ roleName }})</span>
          <el-button type="danger" link @click="handleLogout" style="margin-left: 20px; color: white;">退出</el-button>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>

    <router-view v-else />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const currentUser = ref({})

const activePath = computed(() => route.path)
const isLoggedIn = computed(() => !!currentUser.value.id)

// 角色显示转换
const roleName = computed(() => {
  const roles = {
    'ADMIN_HR': '人事主管',
    'ADMIN_NORMAL': '普通主管',
    'EMPLOYEE': '普通员工'
  }
  return roles[currentUser.value.role] || '未知角色'
})



const loadUser = () => {
  const user = localStorage.getItem('user')
  if (user) currentUser.value = JSON.parse(user)
}

// 每当路由地址 (route.path) 改变时，重新执行一次 loadUser
watch(
  () => route.path,
  () => {
    loadUser()
  }
)

const handleLogout = () => {
  localStorage.removeItem('user')
  currentUser.value = {}
  router.push('/login')
}

onMounted(() => {
  loadUser()
})
</script>

<style scoped>
.header {
  background-color: #409EFF;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  font-size: 18px;
  font-weight: bold;
}

.layout-container {
  height: 100vh;
}
</style>