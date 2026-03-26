<template>
  <div class="main-layout">
    <el-tabs type="border-card" class="info-tabs">

      <el-tab-pane label="考勤打卡">
        <el-card shadow="never" style="border: none;">
          <el-input v-model="userId" disabled placeholder="当前登录员工ID" style="margin-bottom: 20px;" />
          <div style="display: flex; gap: 10px; margin-bottom: 20px;">
            <el-button type="primary" @click="doPunchIn" style="flex: 1;">上班打卡</el-button>

            <el-button type="success" @click="doPunchOut" style="flex: 1;"
              :disabled="!hasClockedInToday">下班打卡</el-button>
            <el-button type="warning" plain @click="correctionVisible = true">申请补签</el-button>
          </div>
          <el-table :data="recordList" border stripe size="small">
            <el-table-column label="姓名" width="100">
              <template #default="scope">
                {{ scope.row.user?.realName || '加载中...' }}
              </template>
            </el-table-column>
            <el-table-column prop="punchDate" label="日期" width="100" />
            <el-table-column label="标准班次" width="180">
              <template #default="scope">
                <div v-if="scope.row.workShift">
                  <el-tag size="small" effect="plain">{{ scope.row.workShift.shiftName }}</el-tag>
                  <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                    ({{ scope.row.workShift.startTime }} - {{ scope.row.workShift.endTime }})
                  </div>
                </div>
                <span v-else style="color: #C0C4CC; font-size: 12px;">未排班</span>
              </template>
            </el-table-column>
            <el-table-column prop="clockIn" label="上班时间">
              <template #default="scope">
                <div style="display: flex; align-items: center; gap: 5px;">
                  <span>{{ formatTime(scope.row.clockIn) }}</span>

                  <el-tag v-if="scope.row.isCorrection && scope.row.clockIn" size="small" type="warning" effect="dark"
                    style="transform: scale(0.8);">
                    补
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="clockOut" label="下班时间">

              <template #default="scope">
                <div style="display: flex; align-items: center; gap: 5px;">
                  <span>{{ formatTime(scope.row.clockOut) }}</span>

                  <el-tag v-if="scope.row.isCorrection && scope.row.clockOut" size="small" type="primary" effect="dark"
                    style="transform: scale(0.8);">
                    补
                  </el-tag>
                </div>
              </template>


            </el-table-column>
            <el-table-column label="状态" width="150">
              <template #default="scope">
                <div style="display: flex; gap: 4px; flex-wrap: wrap;">
                  <el-tag v-if="scope.row.status === 0" type="success" size="small">正常</el-tag>

                  <el-tag v-if="scope.row.status === 1 || scope.row.status === 3" type="danger" size="small">迟到</el-tag>

                  <el-tag v-if="scope.row.status === 2 || scope.row.status === 3" type="warning"
                    size="small">早退</el-tag>

                  <el-tag v-if="scope.row.status === null" type="info" size="small">待打卡</el-tag>

                  <el-tag v-if="scope.row.status === 4" type="danger" effect="dark" size="small">缺勤</el-tag>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-dialog v-model="correctionVisible" title="发起补签申请" width="400px" append-to-body>
          <el-form :model="correctionForm" label-width="80px">
            <el-form-item label="补签日期">
              <el-date-picker v-model="correctionForm.applyDate" type="date" placeholder="补签哪一天的？"
                value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
            <el-form-item label="补签类型">
              <el-select v-model="correctionForm.type" placeholder="请选择" style="width: 100%">
                <el-option label="补上班卡" value="IN" />
                <el-option label="补下班卡" value="OUT" />
              </el-select>
            </el-form-item>
            <el-form-item label="原因">
              <el-input v-model="correctionForm.reason" type="textarea" :rows="3" placeholder="写明忘记打卡的原因" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="correctionVisible = false">取消</el-button>
            <el-button type="primary" @click="submitCorrection">提交申请</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <el-tab-pane label="我的排班">
        <el-alert title="温馨提示：请按照排班时间准时打卡，迟到或早退将影响考勤结果。" type="info" show-icon :closable="false"
          style="margin-bottom: 15px;" />

        <el-table :data="myScheduleList" border stripe>
          <el-table-column prop="workDate" label="工作日期" width="150" />
          <el-table-column label="班次名称">
            <template #default="scope">
              <el-tag>{{ scope.row.workShift?.shiftName || '未设班次' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上班时间段">
            <template #default="scope">
              <b style="color: #409EFF;">{{ scope.row.workShift?.startTime }}</b>
              至
              <b style="color: #67C23A;">{{ scope.row.workShift?.endTime }}</b>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="个人中心">
        <el-form label-width="80px" style="padding: 20px;">
          <el-form-item label="我的头像">
            <el-upload class="avatar-uploader" action="/api/file/upload/avatar"
              :data="{ userId: userId }" :show-file-list="false" :on-success="handleAvatarSuccess">
              <img v-if="loginUser.avatar" :src="loginUser.avatar" class="avatar"
                style="width: 100px; height: 100px; border-radius: 50%;" />
              <el-icon v-else class="avatar-uploader-icon"
                style="font-size: 28px; border: 1px dashed #d9d9d9; padding: 30px; border-radius: 50%;">
                <Plus />
              </el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="userInfo.realName" disabled />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="userInfo.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="备注信息">
            <el-input v-model="userInfo.info" type="textarea" placeholder="简介" />
          </el-form-item>
          <el-button type="primary" @click="saveUserInfo" style="width: 100%">提交修改</el-button>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="请假申请" name="leave">
        <div style="max-width: 600px; margin: 20px auto;">
          <el-card shadow="hover">
            <template #header>
              <b style="font-size: 16px;">填写请假单</b>
            </template>

            <el-form :model="leaveForm" label-width="80px">
              <el-form-item label="请假日期">
                <el-date-picker v-model="leaveDateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
                  end-placeholder="结束日期" value-format="YYYY-MM-DD" />
              </el-form-item>

              <el-form-item label="请假原因">
                <el-input v-model="leaveForm.reason" type="textarea" placeholder="请输入请假原因" />
              </el-form-item>

              <el-form-item label="证明附件">
                <el-upload ref="uploadRef" action="/api/file/upload/only" name="file"
                  list-type="picture-card" :limit="1" :on-success="handleUploadSuccess" :on-remove="handleRemove">
                  <el-icon>
                    <Plus />
                  </el-icon>
                </el-upload>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="submitLeave">提交申请</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <el-table :data="myLeaveList" style="margin-top: 30px;" border>
            <template #empty>
              <el-empty description="暂无请假记录" />
            </template>
            <el-table-column prop="startDate" label="开始日期" width="120" />
            <el-table-column prop="endDate" label="结束日期" width="120" />
            <el-table-column prop="reason" label="理由" />
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 0 ? 'info' : (scope.row.status === 1 ? 'success' : 'danger')">
                  {{ scope.row.status === 0 ? '待审批' : (scope.row.status === 1 ? '已通过' : '已拒绝') }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="加班申请" name="overtime">
        <div style="max-width: 600px; margin: 20px auto;">
          <el-card shadow="hover">
            <el-form :model="otForm" label-width="80px">
              <el-form-item label="加班日期">
                <el-date-picker v-model="otForm.overtimeDate" type="date" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item label="加班时长">
                <el-input-number v-model="otForm.duration" :precision="1" :step="0.5" :min="0.5" /> 小时
              </el-form-item>
              <el-form-item label="加班事由">
                <el-input v-model="otForm.reason" type="textarea" />
              </el-form-item>
              <el-form-item>
                <el-button type="warning" @click="submitOvertime">提交申请</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <el-table :data="myOvertimeList" border stripe>
            <el-table-column prop="overtimeDate" label="加班日期" width="150" />
            <el-table-column prop="duration" label="时长(小时)" width="120" />
            <el-table-column prop="reason" label="原因" />
            <el-table-column label="状态" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.status === 0 ? 'info' : (scope.row.status === 1 ? 'success' : 'danger')">
                  {{ scope.row.status === 0 ? '待审批' : (scope.row.status === 1 ? '已通过' : '已拒绝') }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>


<script setup>
import { ref, onMounted, watch, computed, reactive } from 'vue'
import request from '@/utils/request'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// --- 1. 数据定义区 ---
const recordList = ref([]) // 考勤记录列表
const loginUser = JSON.parse(localStorage.getItem('user') || '{}') // 从缓存拿登录信息
const userId = ref(loginUser.id || '') // 绑定登录人ID

const leaveDateRange = ref([]) // 存储日期选择器的 [开始, 结束]
const leaveForm = ref({
  reason: '',
  attachment: '' // 存储附件URL
})
const myLeaveList = ref([]) // 我的请假记录列表

const uploadRef = ref(null) // 声明一个和上面 ref 名字一样的变量

const myScheduleList = ref([])

// 1. 补签控制弹窗显示
const correctionVisible = ref(false)

const otForm = ref({ overtimeDate: '', duration: 1.0, reason: '' })
const myOvertimeList = ref([])

// 2. 补签表单数据对象
const correctionForm = reactive({
  applyDate: '',
  type: '',
  reason: '',
  userId: userId.value // 自动绑定当前用户ID
})

// 动态获取个人中心信息
const userInfo = ref({
  realName: loginUser.realName || '未知用户', // 从登录信息里拿真实姓名
  phone: loginUser.phone || '',             // 从登录信息里拿手机号
  info: loginUser.info || ''                // 从登录信息里拿备注
})

// --- 2. 工具函数区 ---
// 时间格式化：把 2026-03-14T12:00:00 变成 12:00:00
const formatTime = (timeStr) => {
  if (!timeStr) return '--:--'
  // 增加一个判断，防止后端传来的格式不含 T 导致报错
  return timeStr.includes('T') ? timeStr.split('T')[1].split('.')[0] : timeStr
}

// --- 3. 核心业务逻辑区 ---
// 查询当前员工的记录
const fetchRecords = async () => {
  if (!userId.value) return
  try {
    const res = await request.get(`/api/attendance/myRecords?userId=${userId.value}`)
    recordList.value = res
  } catch (err) {
    console.error("加载记录失败", err)
    ElMessage.error('无法获取考勤记录，请检查网络或后端服务')
  }
}

// 保存个人信息（后续可以对接后端接口）
const saveUserInfo = async () => {

  userInfo.value.id = userId.value;

  try {

    // 🌟 核心：发送 post 请求到后端接口
    const res = await request.post('/api/user/update', userInfo.value)
    ElMessage.success('个人信息更新成功！')
    // 可选：重新拉取最新信息以确保同步
    // await fetchUserInfo() 
  } catch (err) {
    console.error(err)
    ElMessage.error('网络错误，保存失败')
  }
}

// 下班打卡
const doPunchOut = async () => {
  if (!userId.value) return ElMessage.warning('ID不能为空')

  // --- 新增校验逻辑 ---
  // 在当前页面的记录列表里找今天的记录
  const today = new Date().toISOString().split('T')[0]; // 获取今天日期 "2026-03-15"
  const todayRecord = recordList.value.find(r => r.punchDate === today);

  if (!todayRecord || !todayRecord.clockIn) {
    ElMessage.error('您今天尚未办理上班打卡，请先点击上班打卡！');
    return; // 拦截，不执行后续请求
  }
  // ------------------

  try {
    const res = await request.post(`/api/attendance/punchOut?userId=${userId.value}`)

    // 🌟 同样拦截 IP 限制
    if (typeof res.data === 'string' && res.data.startsWith('IP_LIMIT:')) {
      const currentIp = res.data.split(':')[1];
      ElMessageBox.alert(
        `下班打卡失败！您当前不在公司网络环境（IP: ${currentIp}）。<br/>请返回办公室连接 WiFi 后再打卡。`,
        '位置异常',
        { dangerouslyUseHTMLString: true, type: 'warning', center: true }
      );
      return;
    }

    ElMessage.success(res.data)
    await fetchRecords()
  } catch (err) {
    ElMessage.error('下班打卡失败')
  }
}

// 上班打卡
const doPunchIn = async () => {
  if (!userId.value) return ElMessage.warning('ID不能为空')

  try {
    // 1. 发送打卡请求
    const res = await request.post(`/api/attendance/punchIn?userId=${userId.value}`)

    // 🌟 2. 核心拦截：判断是否触碰了 IP 限制
    // 后端返回的格式是 "IP_LIMIT:123.x.x.x"
    if (typeof res.data === 'string' && res.data.startsWith('IP_LIMIT:')) {
      const currentIp = res.data.split(':')[1]; // 拿到冒号后面的真实 IP

      // 弹出更醒目的警告框（比普通 Message 更适合毕设演示）
      ElMessageBox.alert(
        `打卡失败！系统检测到您当前处于非办公网络环境。 <br/><b>您的当前 IP：</b> <span style="color: #f56c6c">${currentIp}</span><br/><br/>请连接公司授权的 WiFi 后再试。`,
        '地理围栏异常',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '确定',
          type: 'warning',
          center: true
        }
      );
      return; // 拦截，不执行后面的刷新逻辑
    }

    // 🌟 3. 原有成功逻辑
    // 如果没有被 IP 拦截，说明打卡成功，执行你原来的操作
    ElMessage.success(res.data)
    await fetchRecords()

  } catch (err) {
    // 如果是网络报错（如 404, 500）走这里
    ElMessage.error('上班打卡失败，请检查网络连接')
  }
}


// 修改 handleAvatarSuccess 函数
const handleAvatarSuccess = (res) => {
  // 注意：因为 el-upload 不走拦截器，所以 res 是 Result 对象
  if (res.code === 200) {
    const avatarUrl = res.data; // 这里的 data 才是真正的图片地址
    ElMessage.success('头像上传成功！');

    loginUser.avatar = avatarUrl;
    localStorage.setItem('user', JSON.stringify(loginUser));
    userInfo.value.avatar = avatarUrl;
  } else {
    ElMessage.error(res.msg || '上传失败');
  }
}

// 提交请假
const submitLeave = async () => {
  if (!leaveDateRange.value || leaveDateRange.value.length < 2) {
    ElMessage.warning('请选择日期范围！')
    return
  }

  const data = {
    userId: userId.value,
    startDate: leaveDateRange.value[0],
    endDate: leaveDateRange.value[1],
    reason: leaveForm.value.reason,
    attachment: leaveForm.value.attachment, // 把附件地址传给后端
    status: 0
  }

  try {
    // 这里使用我们刚在后端写的接口
    const res = await request.post('/api/leave/apply', data)
    ElMessage.success(res.data)

    // 提交成功后清空表单
    leaveForm.value.reason = ''
    leaveDateRange.value = []
    leaveForm.value.attachment = ''

    // 2. 清空视觉：调用 Element Plus 提供的内置方法 clearFiles()
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }

    // 刷新列表（稍后实现获取列表的函数）
    fetchMyLeaves()
  } catch (err) {
    ElMessage.error('提交失败')
  }
}


// 获取我的请假历史（记得在 onMounted 里也调用一下它）
const fetchMyLeaves = async () => {
  // 注意：这个接口你后端也要顺手写一个 getByUserId 的版本哦！
  const res = await request.get(`/api/leave/my?userId=${userId.value}`)
  myLeaveList.value = res.data
}

const hasClockedInToday = computed(() => {
  const today = new Date().toISOString().split('T')[0];
  return recordList.value.some(r => r.punchDate === today && r.clockIn);
})

// 2. 上传成功的回调
const handleUploadSuccess = (res) => {
  console.log("上传成功，后端返回的全量数据：", res) // 重点看这里！
  // 此时你在控制台就能看到 res 的结构了
  if (res) {
    // 根据你之前头像上传的经验，看看地址是在 res 还是 res.data 
    leaveForm.value.attachment = typeof res === 'string' ? res : (res.data || res.url)
    ElMessage.success('附件上传成功')
  }
}

// 3. 移除图片
const handleRemove = () => {
  leaveForm.value.attachment = ''
}

// 2. 提交加班申请
const submitOvertime = async () => {
  if (!otForm.value.overtimeDate) return ElMessage.warning('请选择日期')
  try {
    const data = {
      ...otForm.value,
      userId: userId.value,
      status: 0 // 默认为待审批
    }
    await request.post('/api/overtime/apply', data)
    ElMessage.success('加班申请提交成功')
    otForm.value = { overtimeDate: '', duration: 1.0, reason: '' } // 清空表单
    fetchMyOvertime() // 刷新列表
  } catch (err) {
    ElMessage.error('提交失败')
  }
}

// 3. 获取我的加班记录
const fetchMyOvertime = async () => {
  try {
    const res = await request.get(`/api/overtime/my?userId=${userId.value}`)
    myOvertimeList.value = res.data
  } catch (err) {
    console.error('获取加班记录失败')
  }
}

// 3. 补签提交逻辑
const submitCorrection = async () => {
  if (!correctionForm.applyDate || !correctionForm.type || !correctionForm.reason) {
    ElMessage.error('请填写完整的申请信息')
    return
  }

  try {
    // 这里的路径根据你的后端 Controller 确定
    const res = await request.post('/api/correction/apply', correctionForm)

    ElMessage.success('申请提交成功，请等待管理员审批')
    correctionVisible.value = false
    // 重置表单
    correctionForm.applyDate = ''
    correctionForm.type = ''
    correctionForm.reason = ''

    if (res.data === 'EXISTED') {
      ElMessage.warning('您已提交过该日期的补签申请，请勿重复提交')
    }
  } catch (error) {
    ElMessage.error('提交失败，请检查网络')
  }
}

const fetchMySchedule = async () => {
  try {
    const res = await request.get(`/api/schedule/mySchedule?userId=${userId.value}`)
    myScheduleList.value = res.data
  } catch (error) {
    ElMessage.error('无法获取排班信息')
  }
}

// --- 4. 生命周期与监听 ---
// 只要 userId 变了，就查记录
watch(userId, (newId) => {
  if (newId) fetchRecords()
})

// 页面初始化加载
onMounted(() => {
  if (userId.value) {
    fetchRecords()   // 获取打卡记录
    fetchMyLeaves()  // 加上这一行：获取请假记录
    fetchMyOvertime() //加班记录
    fetchMySchedule()
  }
})
</script>

<style scoped>
/* style 标签里写的是 CSS，负责页面的美化 */
.main-layout {
  display: flex;
  justify-content: center;
  /* 水平居中 */
  align-items: center;
  /* 垂直居中 */
  height: 100vh;
  /* 撑满整个屏幕高度 */
  background-color: #f5f7fa;
}

.box-card {
  width: 350px;
}

.card-header {
  font-weight: bold;
  text-align: center;
}
</style>