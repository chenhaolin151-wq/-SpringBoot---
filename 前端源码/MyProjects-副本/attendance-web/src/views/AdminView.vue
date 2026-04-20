<template>
    <div class="admin-container" style="padding: 20px;">
        <el-tabs type="border-card" v-model="activeName">

            <el-tab-pane label="考勤记录查询">

                <el-row :gutter="20" type="flex" align="middle"
                    style="margin-bottom: 25px; background: #f8f9fa; padding: 20px; border-radius: 8px; border: 1px solid #ebeef5;">
                    <el-col :span="4">
                        <el-statistic title="今日应到" :value="totalExpected" />
                    </el-col>

                    <el-col :span="4">
                        <el-statistic title="今日缺勤" :value="absentCount"
                            value-style="color: #cf1322; font-weight: bold;" />
                    </el-col>

                    <el-col :span="4">
                        <el-statistic title="今日迟到" :value="lateCount" value-style="color: #faad14" />
                    </el-col>

                    <el-col :span="4">
                        <el-statistic title="今日早退" :value="earlyCount" value-style="color: #faad14" />
                    </el-col>

                    <el-col :span="8" style="text-align: right;">
                        <el-button type="warning" icon="Download" size="large" :loading="loading"
                            @click="downloadExcel">
                            导出报表
                        </el-button>
                    </el-col>
                </el-row>

                <el-table :data="allRecords" border stripe :row-class-name="tableRowClassName">
                    <el-table-column label="头像" width="70">
                        <template #default="scope">
                            <el-image style="width: 40px; height: 40px; border-radius: 50%"
                                :src="scope.row.user?.avatar" :preview-src-list="[scope.row.user?.avatar]" fit="cover">
                                <template #error>
                                    <div
                                        style="background: #f5f7fa; display: flex; align-items: center; justify-content: center; height: 100%;">
                                        <el-icon>
                                            <User />
                                        </el-icon>
                                    </div>
                                </template>
                            </el-image>
                        </template>
                    </el-table-column>

                    <el-table-column label="姓名" width="100">
                        <template #default="scope">
                            {{ scope.row.user?.realName || '未知用户' }}
                        </template>
                    </el-table-column>

                    <el-table-column label="联系方式" width="150">
                        <template #default="scope">
                            {{ scope.row.user?.phone || '未填写' }}
                        </template>
                    </el-table-column>

                    <el-table-column prop="punchDate" label="打卡日期" width="120" />

                    <el-table-column label="打卡时间">
                        <template #default="scope">
                            上：{{ formatTime(scope.row.clockIn) }} / 下：{{ formatTime(scope.row.clockOut) }}
                        </template>
                    </el-table-column>

                    <el-table-column label="考勤状态" width="160">
                        <template #default="scope">
                            <div style="display: flex; gap: 5px; flex-wrap: wrap;">
                                <el-tag v-if="scope.row.status === 0" type="success" size="small">正常</el-tag>

                                <el-tag v-if="scope.row.status === 1 || scope.row.status === 3" type="danger"
                                    size="small">迟到</el-tag>

                                <el-tag v-if="scope.row.status === 2 || scope.row.status === 3" type="warning"
                                    size="small">早退</el-tag>

                                <el-tag v-if="scope.row.clockIn && !scope.row.clockOut" type="info"
                                    size="small">未下班</el-tag>

                                <el-tag v-if="scope.row.status === 4" type="danger" effect="dark">缺勤</el-tag>
                            </div>
                        </template>
                    </el-table-column>


                </el-table>
            </el-tab-pane>

            <el-tab-pane label="考勤月度报表" name="report">
                <div style="margin-bottom: 20px; display: flex; align-items: center; gap: 10px;">
                    <span style="font-weight: bold;">报表月份：</span>
                    <el-date-picker v-model="reportMonth" type="month" value-format="YYYY-MM" :clearable="false"
                        @change="fetchReport" />
                    <el-button type="success" icon="Search" @click="fetchReport">生成报表</el-button>
                </div>

                <el-table :data="reportData" border stripe style="width: 100%">
                    <el-table-column prop="realName" label="员工姓名" width="120" fixed />
                    <el-table-column prop="totalDays" label="应出勤(天)" align="center" />
                    <el-table-column prop="normalDays" label="正常(天)" align="center">
                        <template #default="scope">
                            <span style="color: #67C23A">{{ scope.row.normalDays }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="lateCount" label="迟到(次)" align="center">
                        <template #default="scope">
                            <span :style="{ color: scope.row.lateCount > 0 ? '#F56C6C' : '' }">{{ scope.row.lateCount
                            }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="earlyCount" label="早退(次)" align="center" />
                    <el-table-column prop="absentDays" label="缺勤(天)" align="center">
                        <template #default="scope">
                            <el-tag v-if="scope.row.absentDays > 0" type="danger">{{ scope.row.absentDays }}</el-tag>
                            <span v-else>0</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="overtimeHours" label="加班(小时)" align="center" />
                </el-table>
            </el-tab-pane>

            <el-tab-pane label="数据统计分析" name="statistics">
                <div style="margin-bottom: 20px; display: flex; align-items: center; gap: 10px;">
                    <span style="font-weight: bold; color: #606266;">查询月份：</span>
                    <el-date-picker v-model="statsMonth" type="month" placeholder="选择月份" value-format="YYYY-MM"
                        :clearable="false" @change="fetchStatistics" />
                    <el-button type="primary" icon="Refresh" @click="fetchStatistics">刷新数据</el-button>
                </div>
                <el-row :gutter="20">
                    <el-col :span="16">
                        <el-card shadow="hover" header="📅 本月考勤出勤趋势">
                            <div id="trendChart" style="height: 400px;"></div>
                        </el-card>
                    </el-col>
                    <el-col :span="8">
                        <el-card shadow="hover" header="📊 考勤状态分布">
                            <div id="statusPieChart" style="height: 400px;"></div>
                        </el-card>
                    </el-col>
                </el-row>

                <el-row :gutter="20" style="margin-top: 20px;">
                    <el-col :span="24">
                        <el-card shadow="hover" header="🏆 员工加班时长排行 (Top 5)">
                            <div id="overtimeRankChart" style="height: 350px;"></div>
                        </el-card>
                    </el-col>
                </el-row>
            </el-tab-pane>

            <el-tab-pane label="请假审批" name="leaveAudit">
                <el-table :data="allLeaveList" border style="width: 100%">
                    <el-table-column label="申请人" width="120">
                        <template #default="scope">
                            {{ scope.row.user?.realName || '未知用户' }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="startDate" label="开始日期" width="110" />
                    <el-table-column prop="endDate" label="结束日期" width="110" />
                    <el-table-column prop="reason" label="请假理由" />
                    <el-table-column label="当前状态" width="100">
                        <template #default="scope">
                            <el-tag
                                :type="scope.row.status === 0 ? 'info' : (scope.row.status === 1 ? 'success' : 'danger')">
                                {{ scope.row.status === 0 ? '待审批' : (scope.row.status === 1 ? '已通过' : '已拒绝') }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="附件" width="100">
                        <template #default="scope">
                            <el-image v-if="scope.row.attachment" style="width: 50px; height: 50px"
                                :src="scope.row.attachment" :preview-src-list="[scope.row.attachment]" fit="cover" />
                            <span v-else style="color: #999">无</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="180">
                        <template #default="scope">
                            <div v-if="scope.row.status === 0">
                                <el-button size="small" type="success"
                                    @click="handleAudit(scope.row.id, 1)">通过</el-button>
                                <el-button size="small" type="danger"
                                    @click="handleAudit(scope.row.id, 2)">拒绝</el-button>
                            </div>
                            <span v-else style="color: #999; font-size: 12px;">已处理</span>
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>

            <el-tab-pane label="加班审批" name="ot_audit">
                <el-table :data="allOvertimeList" border stripe>
                    <el-table-column label="申请人" width="120">
                        <template #default="scope">
                            {{ scope.row.user?.realName || '未知用户' }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="overtimeDate" label="加班日期" width="150" />
                    <el-table-column prop="duration" label="时长(小时)" width="100" />
                    <el-table-column prop="reason" label="加班原因" />
                    <el-table-column label="操作" width="200">
                        <template #default="scope">
                            <div v-if="scope.row.status === 0">
                                <el-button type="success" size="small" @click="auditOT(scope.row, 1)">通过</el-button>
                                <el-button type="danger" size="small" @click="auditOT(scope.row, 2)">拒绝</el-button>
                            </div>
                            <el-tag v-else :type="scope.row.status === 1 ? 'success' : 'danger'">
                                {{ scope.row.status === 1 ? '已通过' : '已拒绝' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>

            <el-tab-pane label="补签审批" name="correctionAudit">
                <el-table :data="correctionList" border stripe style="width: 100%">
                    <el-table-column prop="user.realName" label="申请人" width="120" />
                    <el-table-column prop="applyDate" label="补签日期" width="120" />
                    <el-table-column label="补签类型" width="100">
                        <template #default="scope">
                            <el-tag :type="scope.row.type === 'IN' ? '' : 'info'">
                                {{ scope.row.type === 'IN' ? '上班卡' : '下班卡' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="reason" label="原因" />
                    <el-table-column label="操作" width="180">
                        <template #default="scope">
                            <div v-if="scope.row.status === 0">
                                <el-button type="success" size="small"
                                    @click="handleApprove(scope.row.id)">通过</el-button>
                                <el-button type="danger" size="small" @click="handleReject(scope.row.id)">驳回</el-button>
                            </div>

                            <span v-else style="color: #999; font-size: 12px;">已完成处理</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="审批状态" width="120">
                        <template #default="scope">
                            <el-tag v-if="scope.row.status === 0" type="info">待审批</el-tag>
                            <el-tag v-if="scope.row.status === 1" type="success" effect="dark">已通过</el-tag>
                            <el-tag v-if="scope.row.status === 2" type="danger" effect="dark">已驳回</el-tag>
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>

            <el-tab-pane label="排班管理" name="schedule">
                <el-calendar v-model="currentDate">
                    <template #date-cell="{ data }">
                        <div class="calendar-cell" @click="viewDailyDetail(data.day)" style="position: relative;">
                            <div v-if="isHolidayDate(data.day)" class="holiday-badge">休</div>
                            <p :class="[
                                data.isSelected ? 'is-selected-date' : 'date-day',
                                isHolidayDate(data.day) ? 'is-holiday-number' : ''
                            ]">
                                {{ data.day.split('-').slice(2).join() }}
                            </p>
                            <div class="summary-wrapper">
                                <div v-for="stat in getDailySummary(data.day)" :key="stat.name" class="stat-item">
                                    <span class="status-dot" :style="{ backgroundColor: stat.color }"></span>
                                    <span class="stat-text">{{ stat.name }}: {{ stat.count }}人</span>
                                </div>
                            </div>
                        </div>
                    </template>
                </el-calendar>

                <el-dialog v-model="detailVisible" :title="selectedDay + ' 排班详情'" width="450px" destroy-on-close>
                    <div
                        style="background-color: #fdf6f6; border: 1px solid #fde2e2; border-radius: 8px; padding: 15px; margin-bottom: 20px;">
                        <div
                            style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px;">
                            <div style="display: flex; align-items: center;">
                                <span style="font-weight: bold; color: #606266; margin-right: 12px;">节假日状态:</span>
                                <template v-if="isHoliday">
                                    <el-tag type="danger" effect="dark" round>{{ holidayName }}</el-tag>
                                    <el-button type="danger" link @click="removeHoliday"
                                        style="margin-left: 10px;">取消设定</el-button>
                                </template>
                                <template v-else>
                                    <span style="color: #909399; font-size: 14px;">普通日期</span>
                                    <el-button type="primary" link @click="isHoliday = true"
                                        style="margin-left: 10px;">设为节日</el-button>
                                </template>
                            </div>
                        </div>

                        <div v-if="isHoliday" style="display: flex; gap: 10px;">
                            <el-input v-model="holidayName" placeholder="输入节日名称（如：春节）" size="default">
                                <template #prefix>
                                    <el-icon>
                                        <Calendar />
                                    </el-icon>
                                </template>
                            </el-input>
                            <el-button type="danger" @click="saveHoliday">保存</el-button>
                        </div>
                    </div>
                    <div style="margin-bottom: 15px; display: flex; justify-content: flex-end;">
                        <el-popconfirm title="确定要清空这一天所有的排班吗？此操作不可撤销！" confirm-button-text="确定清空"
                            cancel-button-text="取消" confirm-button-type="danger" @confirm="clearDailySchedule">
                            <template #reference>
                                <el-button type="danger" size="small" icon="DeleteFilled">清空当日排班</el-button>
                            </template>
                        </el-popconfirm>
                    </div>

                    <el-table :data="dailyStaffList" stripe>
                        <el-table-column prop="realName" label="姓名" width="120" />
                        <el-table-column prop="workShift.shiftName" label="班次">
                            <template #default="scope">
                                <el-tag size="small">{{ scope.row.workShift.shiftName }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="时间段">
                            <template #default="scope">
                                <span style="font-size: 12px; color: #909399;">
                                    {{ scope.row.workShift.startTime.substring(0, 5) }}-{{
                                        scope.row.workShift.endTime.substring(0, 5) }}
                                </span>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="80">
                            <template #default="scope">
                                <el-popconfirm title="确定要取消此排班吗？" confirm-button-text="确定" cancel-button-text="不用了"
                                    @confirm="confirmDelete(scope.row.id)">
                                    <template #reference>
                                        <el-button type="danger" icon="Delete" circle plain size="small" />
                                    </template>
                                </el-popconfirm>
                            </template>
                        </el-table-column>
                    </el-table>

                    <template #footer>
                        <el-button @click="detailVisible = false">关闭</el-button>
                        <el-button type="primary" :disabled="isHoliday" @click="handleGoToSchedule(selectedDay)">
                            {{ isHoliday ? '节假日无需排班' : '去排班' }}
                        </el-button>
                    </template>

                </el-dialog>

                <el-dialog v-model="dialogVisible" title="分配班次" width="30%">
                    <el-form :model="scheduleForm" label-width="80px">
                        <el-form-item label="日期">
                            <el-input v-model="scheduleForm.workDate" disabled />
                        </el-form-item>
                        <el-form-item label="员工">
                            <el-select v-model="scheduleForm.userId" placeholder="请选择员工">
                                <el-option v-for="u in users" :key="u.id" :label="u.realName" :value="u.id" />
                            </el-select>
                        </el-form-item>
                        <el-form-item label="班次">
                            <el-select v-model="scheduleForm.shiftId" placeholder="请选择班次">
                                <el-option v-for="s in shifts" :key="s.id" :label="s.shiftName" :value="s.id" />
                            </el-select>
                        </el-form-item>
                    </el-form>
                    <template #footer>
                        <el-button @click="dialogVisible = false">取消</el-button>
                        <el-button type="primary" @click="submitSchedule">确认排班</el-button>
                    </template>
                </el-dialog>
            </el-tab-pane>

            <el-tab-pane v-if="currentUser.role === 'ADMIN_HR'" label="班次设置" name="shiftConfig">
                <div style="padding: 20px;">
                    <div style="margin-bottom: 20px;">
                        <el-button type="primary" @click="handleAddShift">新增班次</el-button>
                    </div>

                    <el-table :data="shifts" border style="width: 100%">
                        <el-table-column prop="shiftName" label="班次名称" width="180" />
                        <el-table-column prop="startTime" label="上班时间" width="180" />
                        <el-table-column prop="endTime" label="下班时间" width="180" />
                        <el-table-column label="操作">
                            <template #default="scope">
                                <el-button size="small" @click="handleEditShift(scope.row)">编辑</el-button>
                                <el-button size="small" type="danger"
                                    @click="confirmDeleteShift(scope.row.id)">删除</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>

                <el-dialog v-model="shiftDialogVisible" :title="shiftForm.id ? '编辑班次' : '新增班次'" width="30%">
                    <el-form :model="shiftForm" label-width="100px">
                        <el-form-item label="班次名称">
                            <el-input v-model="shiftForm.shiftName" placeholder="如：早班" />
                        </el-form-item>
                        <el-form-item label="上班时间">
                            <el-time-picker v-model="shiftForm.startTime" value-format="HH:mm:ss"
                                placeholder="选择上班时间" />
                        </el-form-item>
                        <el-form-item label="下班时间">
                            <el-time-picker v-model="shiftForm.endTime" value-format="HH:mm:ss" placeholder="选择下班时间" />
                        </el-form-item>
                    </el-form>
                    <template #footer>
                        <el-button @click="shiftDialogVisible = false">取消</el-button>
                        <el-button type="primary" @click="submitShiftSave">确定</el-button>
                    </template>
                </el-dialog>
            </el-tab-pane>

            <el-tab-pane v-if="currentUser.role === 'ADMIN_HR'" label="员工管理" name="userManagement">
                <div style="margin-bottom: 20px;">
                    <el-button type="primary" icon="Plus" @click="addVisible = true">
                        添加员工
                    </el-button>
                </div>

                <el-table :data="userList" border stripe style="width: 100%">
                    <el-table-column prop="realName" label="姓名" width="120" />
                    <el-table-column prop="username" label="账号" width="150" />
                    <el-table-column prop="phone" label="联系方式" />
                    <el-table-column label="当前状态" width="100">
                        <template #default="scope">
                            <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">
                                {{ scope.row.status === 0 ? '在职' : '离职' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="180">
                        <template #default="scope">
                            <el-popconfirm v-if="scope.row.status === 0" title="确定要将该员工设为离职吗？离职后将无法登录系统。"
                                @confirm="changeStatus(scope.row.id, 1)">
                                <template #reference>
                                    <el-button type="warning" link>办理离职</el-button>
                                </template>
                            </el-popconfirm>

                            <el-button v-else type="success" link @click="changeStatus(scope.row.id, 0)">
                                恢复在职
                            </el-button>
                        </template>
                    </el-table-column>
                    <el-table-column label="权限角色" width="180">
                        <template #default="scope">
                            <el-select v-model="scope.row.role" size="small" @change="handleRoleChange(scope.row)"
                                placeholder="设置角色">
                                <el-option label="人事管理员" value="ADMIN_HR" />
                                <el-option label="普通主管" value="ADMIN_NORMAL" />
                                <el-option label="普通员工" value="EMPLOYEE" />
                            </el-select>
                        </template>
                    </el-table-column>


                </el-table>

                <el-dialog v-model="addVisible" title="录入新员工信息" width="450px">
                    <el-form :model="newUser" label-width="100px">
                        <el-form-item label="姓名">
                            <el-input v-model="newUser.realName" />
                        </el-form-item>
                        <el-form-item label="登录账号">
                            <el-input v-model="newUser.username" />
                        </el-form-item>
                        <el-form-item label="手机号">
                            <el-input v-model="newUser.phone" />
                        </el-form-item>
                    </el-form>
                    <template #footer>
                        <el-button @click="addVisible = false">取消</el-button>
                        <el-button type="primary" @click="submitUser">提交</el-button>
                    </template>
                </el-dialog>
            </el-tab-pane>

            <el-tab-pane v-if="currentUser.role === 'ADMIN_HR'" label="考勤环境" name="attendanceConfig">
                <div style="max-width: 600px; margin: 20px auto;">
                    <el-card shadow="hover">
                        <template #header>
                            <div style="display: flex; align-items: center; justify-content: space-between;">
                                <span style="font-weight: bold;">🏢 办公地点 IP 校验设置</span>
                            </div>
                        </template>

                        <el-form label-width="120px" style="margin-top: 10px;">
                            <el-form-item label="当前网络 IP">
                                <div style="display: flex; align-items: center; gap: 10px;">
                                    <el-tag type="info" effect="plain">{{ adminCurrentIp }}</el-tag>
                                    <el-button type="primary" link icon="Refresh" @click="fetchAdminIp">刷新</el-button>
                                </div>
                            </el-form-item>

                            <el-form-item label="授权打卡 IP">
                                <el-input v-model="officeIp" placeholder="例如: 121.40.xx.xx">
                                    <template #prefix>
                                        <el-icon>
                                            <Location />
                                        </el-icon>
                                    </template>
                                </el-input>
                                <p style="font-size: 12px; color: #909399; margin: 5px 0 0 0;">
                                    * 只有连接此 IP 对应 WiFi 的员工才能完成上下班打卡。
                                </p>
                            </el-form-item>

                            <el-form-item>
                                <el-button type="success" icon="Check" @click="setAsOfficeIp">设为当前 IP</el-button>
                                <el-button type="primary" icon="DocumentChecked" @click="saveConfig">保存配置</el-button>
                            </el-form-item>
                        </el-form>
                    </el-card>
                </div>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script setup>

import { ref, onMounted, computed, watch, nextTick } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus' // 必须补上这行！

import { Location } from '@element-plus/icons-vue'

import * as echarts from 'echarts'; // 导入图表库

const allRecords = ref([])

const allLeaveList = ref([]) // 存储所有人的请假申请

const allOvertimeList = ref([])

const loading = ref(false)

// 1. 定义数据
const currentDate = ref(new Date()) // 当前日历显示的日期
const shifts = ref([]) // 所有的班次选项
const users = ref([])  // 所有的员工选项
const scheduleList = ref([]) // 已经排好的班次数据
const dialogVisible = ref(false) // 控制弹窗

const currentUser = ref({});

const scheduleForm = ref({
    workDate: '',
    userId: '',
    shiftId: ''
})

// 1. 定义班次弹窗相关的变量
const shiftDialogVisible = ref(false)

const shiftForm = ref({
    id: null,
    shiftName: '',
    startTime: '',
    endTime: ''
})

const detailVisible = ref(false)
const selectedDay = ref('')
const dailyStaffList = ref([])

const correctionList = ref([])

const addVisible = ref(false)

const newUser = ref({
    realName: '',
    phone: '',
    username: ''
})

const userList = ref([]) // 用于存放员工列表数据

const isHoliday = ref(false)
const holidayName = ref('')
const holidayList = ref([]); // 存放从后端查到的节假日

const adminCurrentIp = ref('加载中...')
const officeIp = ref('')


const formatTime = (timeStr) => {
    if (!timeStr) return '--:--'
    return timeStr.split('T')[1].split('.')[0]
}


// 定义当前激活的标签页名，默认显示第一个标签页（比如 'records'）
const activeName = ref('records')

const statsMonth = ref(new Date().toISOString().substring(0, 7));

const reportMonth = ref(new Date().toISOString().substring(0, 7));
const reportData = ref([]);

// --------------------------------考勤记录查询---------------------------------------------









// 获取所有人的考勤记录并展示在第一个标签页
const fetchAll = async () => {
    try {
        const res = await request.get('/api/attendance/allRecords')
        allRecords.value = res
    } catch (err) {
        console.error("管理员获取数据失败", err)
    }
}

// 使用 window.location.href 触发后端流下载报表
const downloadExcel = () => {
    loading.value = true
    try {
        // 直接跳转到后端下载接口
        window.location.href = '/api/file/export'
        // 🌟 核心：手动指定后端基地址，确保端口与 request.js 一致
        const BASE_URL = 'http://localhost:8081'
        window.location.href = `${BASE_URL}/api/file/export`
        // 🌟 重点：不要立即关掉 loading，延迟 1.5 秒
        // 给浏览器留出响应时间，也给用户一个“正在生成”的心理反馈
        setTimeout(() => {
            loading.value = false
        }, 1500)
    } catch (err) {
        loading.value = false// 无论成功失败，最后都关掉转圈
        ElMessage.error('导出失败')
    }
}

//计算应打卡人数
const totalExpected = computed(() => {
    // 逻辑：找出打卡日期为今天的排班人数（后端重构后，allRecords里每一行就是一个人）
    const today = new Date().toISOString().split('T')[0]
    return allRecords.value.filter(r => r.punchDate === today).length
})

//计算缺勤人数
const absentCount = computed(() => {
    const today = new Date().toISOString().split('T')[0]
    return allRecords.value.filter(r => r.punchDate === today && r.status === 4).length
})

//计算早退人数
const earlyCount = computed(() => {
    const today = new Date().toISOString().split('T')[0]
    return allRecords.value.filter(r => r.punchDate === today && (r.status === 2 || r.status === 3)).length
})

// 计算今日迟到人数
const lateCount = computed(() => {
    const today = new Date().toISOString().split('T')[0]
    return allRecords.value.filter(r =>
        r.punchDate === today && (r.status === 1 || r.status === 3)
    ).length
})

//实现“缺勤行背景变红”的效果
const tableRowClassName = ({ row }) => {
    // 如果状态是 4 (缺勤)，返回自定义的类名
    if (row.status === 4) {
        return 'absent-row'
    }
    return ''
}










// --------------------------------考勤月度报表---------------------------------------------










const fetchReport = async () => {
    try {
        const res = await request.get(`/api/attendance/report?month=${reportMonth.value}`);
        reportData.value = res;
    } catch (err) {
        ElMessage.error("获取报表失败");
    }
};

// 在 watch(activeName) 中增加触发
watch(activeName, (val) => {
    if (val === 'statistics') fetchStatistics();
    if (val === 'report') fetchReport(); // 🌟 切换到报表页时自动查询
});
































// --------------------------------数据统计分析---------------------------------------------










// 初始化员工加班时长排行图表 (柱状图)
const initOvertimeRankChart = (data) => {
    const chartDom = document.getElementById('overtimeRankChart');
    // 如果找不到容器，直接返回，防止报错
    if (!chartDom) return;

    const myChart = echarts.init(chartDom);
    const option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'value',
            name: '小时',
            boundaryGap: [0, 0.01]
        },
        yAxis: {
            type: 'category',
            // 姓名列表，如 ['张三', '李四', ...]
            data: data.names || []
        },
        series: [
            {
                name: '加班总时长',
                type: 'bar',
                // 时长列表，如 [10.5, 8.0, ...]
                data: data.hours || [],
                itemStyle: {
                    // 使用漂亮的渐变色或 ElementPlus 主题色
                    color: '#67C23A'
                },
                label: {
                    show: true,
                    position: 'right',
                    formatter: '{c} 小时'
                }
            }
        ]
    };
    myChart.setOption(option);
};

// 初始化趋势折线图
const initTrendChart = (data) => {
    const chartDom = document.getElementById('trendChart');
    const myChart = echarts.init(chartDom);
    const option = {
        title: { text: '出勤对比趋势' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['实际到岗', '应当到岗'] }, // 🌟 增加图例
        xAxis: { type: 'category', data: data.dates },
        yAxis: { type: 'value', minInterval: 1 },
        series: [
            {
                name: '实际到岗',
                data: data.counts,
                type: 'line',
                smooth: true,
                itemStyle: { color: '#409EFF' } // 蓝色
            },
            {
                name: '应当到岗',
                data: data.expectedCounts, // 🌟 使用后端新增的字段
                type: 'line',
                smooth: true,
                lineStyle: { type: 'dashed' }, // 🌟 设置为虚线，方便区分
                itemStyle: { color: '#67C23A' } // 绿色
            }
        ]
    };
    myChart.setOption(option);
};

// 初始化状态饼图
const initStatusPieChart = (data) => {
    const chartDom = document.getElementById('statusPieChart');
    const myChart = echarts.init(chartDom);
    const option = {
        tooltip: { trigger: 'item' },
        legend: { bottom: '5%', left: 'center' },
        series: [{
            name: '状态分布',
            type: 'pie',
            radius: ['40%', '70%'],
            data: [
                { value: data.normal, name: '正常' },
                { value: data.late, name: '迟到' },
                { value: data.early, name: '早退' },
                { value: data.absent, name: '缺勤' }
            ]
        }]
    };
    myChart.setOption(option);
};

// 获取统计数据并渲染
const fetchStatistics = async () => {
    try {

        const monthStr = statsMonth.value;


        await request.get('/api/attendance/testAbsence?month=${monthStr}');

        // 2. 发起请求获取后端真实统计数据
        const res = await request.get(`/api/attendance/statistics?month=${monthStr}`);

        await nextTick();

        // 4. 渲染图表
        if (res) {
            initTrendChart({
                dates: res.dates,
                counts: res.counts,
                expectedCounts: res.expectedCounts
            });
            initStatusPieChart({
                normal: res.normalCount || 0,
                late: res.lateCount || 0,
                early: res.earlyCount || 0,
                absent: res.absentCount || 0
            });
            initOvertimeRankChart({
                names: res.employeeNames,
                hours: res.overtimeHours
            });
        }
    } catch (err) {
        console.error("获取统计数据失败:", err);
    }

};

// 监听标签页切换，当切换到“statistics”时重新渲染图表
watch(() => activeName.value, (val) => {
    if (val === 'statistics') {
        fetchStatistics();
    }
});











// --------------------------------请假审批---------------------------------------------






// 处理请假的“通过/拒绝”动作
const handleAudit = async (id, status) => {
    try {
        // 这里的参数要和后端 @RequestParam 对应
        await request.post(`/api/leave/audit?id=${id}&status=${status}`)
        ElMessage.success('处理成功')
        fetchAllLeaves() // 刷新列表看最新状态
    } catch (err) {
        ElMessage.error('处理失败')
    }
}

// 管理员一次性加载所有员工的请假单，用于在“请假审批”页进行处理。
const fetchAllLeaves = async () => {
    const res = await request.get('/api/leave/all')
    allLeaveList.value = res
}








// --------------------------------加班审批---------------------------------------------







// 管理员用来获取全公司所有员工的加班申请列表，以便进行统一审批
const fetchAllOvertime = async () => {
    const res = await request.get('/api/overtime/all')
    allOvertimeList.value = res
}

// 处理加班申请的“通过/拒绝”动作
const auditOT = async (row, status) => {
    await request.post(`/api/overtime/audit?id=${row.id}&status=${status}`)
    ElMessage.success('审批成功')
    fetchAllOvertime()
}









// --------------------------------排班管理---------------------------------------------










// Vue 会为日历的每一个格子调用它，用来统计并显示该天“早班 X 人、晚班 Y 人”的小圆点
const getDailySummary = (day) => {
    // 1. 筛选出当天的所有排班
    const daySchedules = scheduleList.value.filter(s => s.workDate === day)
    if (daySchedules.length === 0) return []

    // 2. 统计各班次人数
    const statsMap = {}
    daySchedules.forEach(s => {
        const shiftName = s.workShift.shiftName
        if (!statsMap[shiftName]) {
            statsMap[shiftName] = 0
        }
        statsMap[shiftName]++
    })

    // 3. 转换为数组并分配精美颜色
    const colors = { '早班': '#67C23A', '晚班': '#E6A23C', '中班': '#409EFF' }
    return Object.keys(statsMap).map(name => ({
        name: name,
        count: statsMap[name],
        color: colors[name] || '#909399' // 如果没匹配到颜色，用灰色
    }))
}

// 根据日历当前的月份获取整月的排班列表
const fetchSchedules = async () => {
    // 🌟 核心改进：使用本地时间获取年和月，避免时区偏差
    const d = currentDate.value
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0') // getMonth() 是 0-11，所以要 +1
    const monthStr = `${year}-${month}` // 结果如 "2026-04"

    const res = await request.get(`/api/schedule/list?month=${monthStr}`)
    scheduleList.value = res
}

// 监听日历绑定的日期变量
watch(currentDate, async () => {
    console.log('月份已切换，当前日期为:', currentDate.value);

    // 🌟 1. 获取排班数据 (旧功能)
    await fetchSchedules();

    // 🌟 2. 获取节假日数据 (新功能预留)
    // 如果你还没定义 fetchHolidays，可以先注释掉下面这一行
    await fetchHolidays();

}, { immediate: true }); // 🌟 关键：确保页面一打开就执行一次

// 点击“去排班”触发，排班详情关闭、分配班次打开
const handleGoToSchedule = (day) => {
    detailVisible.value = false; // 先关掉“详情”
    openSchedule(day);           // 再打开“排班分配”
};

// 在点击日历格子的“去排班”时，记录目标日期并弹出分配班次的窗口
const openSchedule = (day) => {
    scheduleForm.value.workDate = day
    dialogVisible.value = true
}

// 保存当日排班
const submitSchedule = async () => {
    await request.post('/api/schedule/save', scheduleForm.value)
    ElMessage.success('排班成功')
    dialogVisible.value = false
    fetchSchedules() // 刷新日历数据
    initData()
}

//删除单个员工排班
const confirmDelete = async (id) => {

    // 调用后端删除接口
    try {
        const res = await request.delete(`/api/schedule/delete/${id}`)
        if (res === '删除成功') {
            ElMessage.success('删除成功')
            // 🌟 重点：删除后需要重新获取日历数据并刷新当前弹窗列表
            fetchSchedules() // 刷新日历数据
            // 假设当前弹窗数据存在 dayScheduleDetails 里，也要同步剔除
            dailyStaffList.value = dailyStaffList.value.filter(item => item.id !== id)
        }
    } catch (err) {
        ElMessage.error('删除失败')
    }
}

//批量删除排班
const clearDailySchedule = async () => {
    try {
        // selectedDay 就是你在 viewDailyDetail 时记录的那个日期字符串（如 "2026-03-17"）
        const res = await request.post(`/api/schedule/deleteByDate?date=${selectedDay.value}`)

        if (res === '批量删除成功') {
            ElMessage.success(`${selectedDay.value} 的排班已全部清空`)

            // 1. 核心：清空当前弹窗显示的列表，让表格瞬间变空
            dailyStaffList.value = []

            // 2. 核心：重新获取全局排班数据，刷新日历格子的统计数字
            fetchSchedules()

            // 3. 可选：自动关闭弹窗
            detailVisible.value = false;
        }
    } catch (err) {
        console.error(err)
        ElMessage.error('清空失败，请稍后再试')
    }
}

// 初始化所有班次选项和（按日期过滤后的）可选员工名单
const initData = async (workDate = null) => {
    try {
        // 1. 获取班次（维持原样）
        const sRes = await request.get('/api/shift/all')
        shifts.value = sRes

        // 2. 获取员工
        // 🌟 核心：构建带日期参数的 URL
        let url = '/api/schedule/availableUsers'
        if (workDate) {
            // 如果传了日期，URL 变成 .../users?workDate=2026-03-18
            // 后端接收到后，会自动过滤掉那天请假的人
            url += `?workDate=${workDate}`
        }

        const uRes = await request.get(url)
        users.value = uRes // 更新 Vue 响应式变量，下拉框会自动变

        // 3. 获取现有排班（维持原样）
        fetchSchedules()
    } catch (err) {
        console.error('初始化数据失败', err)
    }
}

// 点击日历格子时触发，显示该天具体员工值班信息。
const viewDailyDetail = async (day) => {

    selectedDay.value = day
    await initData(day);
    // 🌟 2. 匹配节假日 (新功能)
    // 依然使用 substring(0, 10) 防止 4 月份匹配失败
    const holidayInfo = holidayList.value.find(h =>
        h.holidayDate && h.holidayDate.substring(0, 10) === day
    );
    if (holidayInfo) {
        isHoliday.value = true;
        holidayName.value = holidayInfo.name;
    } else {
        // 重要：如果不是节日，一定要清空，否则会残留上一次点击的节日名
        isHoliday.value = false;
        holidayName.value = '';
    }
    dailyStaffList.value = scheduleList.value.filter(s => s.workDate === day)
    detailVisible.value = true
}

// 检查某一天是否为节假日，在日历上渲染“休”字标记
const isHolidayDate = (day) => {
    // 1. 安全检查：如果节假日名单还没加载出来，直接返回 false
    if (!holidayList.value || holidayList.value.length === 0) {
        return false;
    }

    // 2. 匹配逻辑：拿着日历传进来的 day (2026-03-19) 去名单里找
    // 使用 some 只要找到一个匹配就返回 true
    return holidayList.value.some(h => {
        // 确保数据库记录里有日期字段
        if (h && h.holidayDate) {
            // 🌟 核心：强制只取前10位对比，无视后端带不带 00:00:00
            return h.holidayDate.substring(0, 10) === day;
        }
        return false;
    });
};

// 节假日配置，设置某天为节假日并清空当日排班
const saveHoliday = async () => {
    try {
        const data = {
            holidayDate: selectedDay.value,
            name: holidayName.value,
            isWorkDay: 0
        };

        // 1. 发送保存请求
        await request.post('/api/holiday/save', data);
        ElMessage.success('设置成功');

        await clearDailySchedule();

        // 🌟 2. 核心：重新拉取本月的节假日列表
        // 这一步执行完，holidayList 变了，日历格子的 isHolidayDate(day) 会自动重新计算
        await fetchHolidays();

    } catch (err) {
        console.error("保存失败:", err);
    }
};

// 节假日配置，使节假日恢复为普通日期
const removeHoliday = async () => {
    // 1. 先尝试在已加载的名单里找 ID
    const holidayInfo = holidayList.value.find(h =>
        h.holidayDate && h.holidayDate.substring(0, 10) === selectedDay.value
    );

    // 2. 如果找到了 ID，说明数据库有记录，需要请求后端删除
    if (holidayInfo) {
        try {
            await request.delete(`/api/holiday/delete?id=${holidayInfo.id}`);
            ElMessage.success('已恢复为普通日期');
            await fetchHolidays(); // 刷新日历标红状态
        } catch (err) {
            ElMessage.error('删除失败');
        }
    } else {
        // 3. 🌟 如果没找到 ID，说明用户只是点开了“设为节日”但没保存
        // 这种情况下直接关掉输入框即可，不需要请求后端
        console.log("仅重置前端显示状态");
    }

    // 无论是否发了请求，最后都要重置弹窗内的显示状态
    isHoliday.value = false;
    holidayName.value = '';
};

// 从数据库获取指定月份的节假日安排
const fetchHolidays = async () => {
    try {
        const d = currentDate.value
        const year = d.getFullYear()
        const month = String(d.getMonth() + 1).padStart(2, '0')
        const monthStr = `${year}-${month}`

        const res = await request.get(`/api/holiday/list?month=${monthStr}`)
        holidayList.value = res
    } catch (err) {
        console.error("获取节假日失败:", err)
    }
}








// --------------------------------班次设置---------------------------------------------






// 班次的提交保存
const submitShiftSave = async () => {
    try {
        await request.post(`/api/shift/save?role=${currentUser.value.role}`, shiftForm.value)
        ElMessage.success('保存成功')
        shiftDialogVisible.value = false
        initData() // 重新拉取数据，刷新表格
        fetchSchedules()
    } catch (error) {
        ElMessage.error('保存失败')
    }
}

// 删除班次
const confirmDeleteShift = async (id) => {
    try {
        await request.delete(`/api/shift/delete/${id}`)
        ElMessage.success('删除成功')
        initData() // 刷新表格
        fetchSchedules()
    } catch (error) {
        ElMessage.error('删除失败，可能该班次正在被使用')
    }
}

// 重置班次表单并打开新增班次的对话框
const handleAddShift = () => {
    shiftForm.value = { id: null, shiftName: '', startTime: '', endTime: '' }
    shiftDialogVisible.value = true
}

// 供管理员进行修改、编辑选中班次的现有信息（名称、时间）
const handleEditShift = (row) => {
    shiftForm.value = { ...row } // 将当前行的数据拷贝到表单里
    shiftDialogVisible.value = true
}





// --------------------------------补签审批---------------------------------------------









// 获取补签申请列表
const fetchCorrectionList = async () => {
    try {
        const res = await request.get('/api/correction/list')
        correctionList.value = res
    } catch (error) {
        ElMessage.error('获取补签列表失败')
    }
}

// 补签申请的审批
const handleApprove = async (id) => {
    try {
        const res = await request.post(`/api/correction/approve?id=${id}`)
        if (res === 'SUCCESS') {
            ElMessage.success('审批通过，考勤记录已自动修复')
            fetchCorrectionList() // 重新刷新列表
        }
    } catch (error) {
        ElMessage.error('操作失败')
    }
}

// 驳回补签申请（简单处理，直接改状态即可）
const handleReject = async (id) => {
    try {
        // 调用刚才写的接口
        const res = await request.post(`/api/correction/reject?applyId=${id}`)
        if (res === 'SUCCESS') {
            ElMessage.warning('申请已驳回') // 驳回用 warning 橙色提醒更合适
            fetchCorrectionList() // 刷新列表，被驳回的申请状态会变红
        }
    } catch (error) {
        ElMessage.error('操作失败')
    }
}









// --------------------------------员工管理---------------------------------------------







// 添加新员工
const submitUser = async () => {
    try {
        const res = await request.post('/api/user/add', newUser.value)
        // 🌟 判定条件与后端返回的 data 保持一致
        if (res === 'SUCCESS') {
            ElMessage.success('员工添加成功，默认密码 123456')
            addVisible.value = false

            // 🌟 使用 await 确保列表刷新请求已发出并更新了响应式变量
            await fetchUserList()

            // 重置表单
            newUser.value = { realName: '', phone: '', username: '' }
        }
    } catch (err) {
        // 拦截器已经弹过错误了，这里可以处理具体的 loading 状态关闭
        console.error("添加员工失败", err)
    }
}

//获取所有员工信息
const fetchUserList = async () => {
    try {
        const res = await request.get('/api/user/all')
        userList.value = res
    } catch (err) {
        ElMessage.error('获取员工列表失败')
    }
}

//更改员工任职状态
const changeStatus = async (id, status) => {
    try {
        // 使用 URLSearchParams 传参，或者直接拼在 URL 后
        await request.put(`/api/user/updateStatus?id=${id}&status=${status}`)
        ElMessage.success('操作成功')
        initData()
        fetchSchedules()
        fetchUserList() // 刷新列表查看状态变化
    } catch (err) {
        ElMessage.error('操作失败')
    }
}

//修改员工权限，如果修改的是自己，会强制刷新页面以应用新权限。
const handleRoleChange = async (row) => {
    try {
        // 1. 调用后端更新接口
        await request.put(`/api/user/updateRole?id=${row.id}&role=${row.role}`)

        ElMessage.success(`${row.realName} 的权限已更新为 ${row.role}`)

        // 🌟 核心改进：主动同步权限状态
        // 从 localStorage 拿到当前登录的用户对象
        const currentUserStr = localStorage.getItem('user')
        if (currentUserStr) {
            const currentUser = JSON.parse(currentUserStr)

            // 如果修改的这个员工 ID 正好是当前登录人的 ID
            if (currentUser.id === row.id) {
                // 更新对象里的 role 字段
                currentUser.role = row.role
                // 重新写回 localStorage
                localStorage.setItem('user', JSON.stringify(currentUser))

                // 💡 关键操作：由于 App.vue 的变量通常在加载时赋值，
                // 最简单的方法是强制刷新页面，让整个系统按新权限重新走一遍。
                ElMessage.warning('检测到您的权限已变动，正在重新初始化...')
                setTimeout(() => {
                    location.reload()
                }, 1000)
            }
        }
    } catch (err) {
        ElMessage.error('权限更新失败')
        fetchUserList() // 失败了刷新列表回退状态
    }
}








// --------------------------------考勤环境---------------------------------------------





// 获取管理员当前的真实 IP（调用我们刚写的后端接口）
const fetchAdminIp = async () => {
    try {
        const res = await request.get('/api/attendance/currentIp')
        adminCurrentIp.value = res
    } catch (err) {
        ElMessage.error('无法获取当前 IP')
    }
}

// 将系统检测到的管理员当前网络 IP 快捷填入配置框，方便将其设为办公地点 IP
const setAsOfficeIp = () => {
    officeIp.value = adminCurrentIp.value
}

// 将管理员修改后的“授权打卡 IP”正式提交并保存到后端数据库配置中
const saveConfig = async () => {
    if (!officeIp.value) return ElMessage.warning('请填写有效的 IP 地址')
    try {
        const res = await request.post(`/api/attendance/updateConfig?ip=${officeIp.value}`)
        if (res === '更新成功') {
            ElMessage.success('考勤地点配置已更新！')
        }
    } catch (err) {
        ElMessage.error('保存失败')
    }
}







onMounted(() => {
    fetchAll()
    fetchAllLeaves()
    fetchAllOvertime()
    initData()
    // 从本地存储获取登录时存下的用户信息
    const userJson = localStorage.getItem('user');
    if (userJson) {
        currentUser.value = JSON.parse(userJson);
    }
    fetchCorrectionList()
    fetchUserList()
    fetchAdminIp()
})
</script>


<style scoped>
.calendar-cell {
    height: 100%;
    padding: 4px;
    transition: background 0.3s;
}

.calendar-cell:hover {
    background-color: #f0f9eb;
    /* 鼠标悬停有个淡淡的绿色反馈 */
}

.date-day {
    margin: 0;
    font-size: 14px;
    color: #303133;
}

.is-selected-date {
    margin: 0;
    font-size: 14px;
    color: #409EFF;
    font-weight: bold;
}

.summary-wrapper {
    margin-top: 8px;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.stat-item {
    display: flex;
    align-items: center;
    line-height: 1;
}

.status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 6px;
    flex-shrink: 0;
}

.stat-text {
    font-size: 11px;
    color: #606266;
    white-space: nowrap;
}

.export-btn {
    padding: 12px 24px;
    font-weight: bold;
    letter-spacing: 1px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 6px rgba(230, 162, 60, 0.2);
    /* 淡淡的橙色投影 */
}

.export-btn:hover {
    transform: translateY(-2px);
    /* 悬停时轻微浮起 */
    box-shadow: 0 6px 12px rgba(230, 162, 60, 0.3);
}

.export-btn:active {
    transform: translateY(0);
}

/* 重点：加上 !important 确保覆盖掉 Element UI 默认的交替行颜色 */
:deep(.el-table .absent-row) {
    background-color: #fff1f0 !important;
    /* 极浅的红色背景 */
}

/* 如果你想让这一行鼠标悬停时颜色稍微加深一点点 */
:deep(.el-table .absent-row:hover > td) {
    background-color: #ffccc7 !important;
}

/* 结合你之前说的“闪烁”想法，我们可以给标签加一个呼吸灯效果，而不是剧烈闪烁 */
.absent-tag-animate {
    animation: breathing 2s infinite ease-in-out;
}

.is-holiday-number {
    color: #f56c6c !important;
    /* ElementPlus 的红色 */
    font-weight: bold;
}

.holiday-badge {
    position: absolute;
    top: 4px;
    right: 4px;
    background-color: #f56c6c;
    /* 红色背景 */
    color: white;
    /* 白色文字 */
    font-size: 10px;
    /* 小字体 */
    line-height: 1;
    padding: 2px 4px;
    border-radius: 3px;
    /* 圆角 */
    font-weight: bold;
    z-index: 10;
    /* 确保在最上层 */
}

@keyframes breathing {
    0% {
        opacity: 1;
    }

    50% {
        opacity: 0.6;
    }

    100% {
        opacity: 1;
    }
}
</style>