<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1>👑 管理员控制面板</h1>
      <div class="header-actions">
        <router-link to="/adoption-review" class="btn-review">领养审核</router-link>
        <span class="admin-info">欢迎，{{ adminUser?.username }}</span>
        <button @click="handleLogout" class="btn-logout">退出登录</button>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="tabs">
        <button
          :class="['tab-btn', { active: activeTab === 'pending' }]"
          @click="activeTab = 'pending'"
        >
          用户注册审核 ({{ pendingUsers.length }})
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'volunteerApps' }]"
          @click="activeTab = 'volunteerApps'"
        >
          志愿者申请审核 ({{ volunteerApplications.length }})
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'registrationApps' }]"
          @click="activeTab = 'registrationApps'"
        >
          报名审核 ({{ pendingRegistrations.length }})
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'all' }]"
          @click="activeTab = 'all'"
        >
          所有用户管理
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'activityManage' }]"
          @click="activeTab = 'activityManage'"
        >
          活动管理
        </button>
      </div>

      <!-- 1. 用户注册审核 -->
      <div v-if="activeTab === 'pending'" class="tab-content">
        <h2>待审核用户注册</h2>
        <div v-if="pendingUsers.length === 0" class="empty-state">
          <p>暂无待审核用户</p>
        </div>
        <div v-else class="user-list">
          <div v-for="user in pendingUsers" :key="user.userId" class="user-card">
            <div class="user-info">
              <h3>{{ user.username }}</h3>
              <p><strong>邮箱：</strong>{{ user.email || '未填写' }}</p>
              <p><strong>电话：</strong>{{ user.phone || '未填写' }}</p>
              <p><strong>注册时间：</strong>{{ formatDate(user.registerTime) }}</p>
            </div>
            <div class="user-actions">
              <button @click="approveUser(user)" class="btn-approve">✓ 通过</button>
              <button @click="rejectUser(user)" class="btn-reject">✗ 拒绝</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. 志愿者申请审核 -->
      <div v-if="activeTab === 'volunteerApps'" class="tab-content">
        <h2>待审核志愿者申请</h2>
        <div v-if="volunteerApplications.length === 0" class="empty-state">
          <p>暂无志愿者申请</p>
        </div>
        <div v-else class="user-list">
          <div v-for="user in volunteerApplications" :key="user.userId" class="user-card">
            <div class="user-info">
              <h3>{{ user.username }}</h3>
              <p><strong>邮箱：</strong>{{ user.email || '未填写' }}</p>
              <p><strong>电话：</strong>{{ user.phone || '未填写' }}</p>
              <p><strong>申请时间：</strong>{{ formatDate(user.applyTime) || '未知' }}</p>
            </div>
            <div class="user-actions">
              <button @click="approveVolunteerApp(user)" class="btn-approve">✓ 通过</button>
              <button @click="rejectVolunteerApp(user)" class="btn-reject">✗ 拒绝</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. 报名审核（新增） -->
      <div v-if="activeTab === 'registrationApps'" class="tab-content">
        <h2>待审核的活动报名</h2>
        <div v-if="pendingRegistrations.length === 0" class="empty-state">
          <p>暂无待审核报名</p>
        </div>
        <div v-else class="user-list">
          <div v-for="reg in pendingRegistrations" :key="reg.activity_id + '-' + reg.user_id" class="user-card">
            <div class="user-info">
              <h3>{{ reg.activity_name }}</h3>
              <p><strong>报名者：</strong>{{ reg.username }}（{{ reg.phone || '未填写电话' }}）</p>
              <p><strong>报名时间：</strong>{{ formatDate(reg.apply_time) }}</p>
              <p><strong>活动时间：</strong>{{ formatDateTime(reg.start_time) }} ~ {{ formatDateTime(reg.end_time) }}</p>
              <p><strong>活动地点：</strong>{{ reg.location }}</p>
            </div>
            <div class="user-actions">
              <button @click="approveRegistration(reg)" class="btn-approve">✓ 通过</button>
              <button @click="rejectRegistration(reg)" class="btn-reject">✗ 拒绝</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 4. 所有用户管理 -->
      <div v-if="activeTab === 'all'" class="tab-content">
        <h2>用户管理与权限分配</h2>
        <div v-if="allUsers.length === 0" class="empty-state">
          <p>暂无用户</p>
        </div>
        <div v-else class="user-table-container">
          <table class="user-table">
            <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>邮箱</th>
              <th>角色</th>
              <th>志愿者</th>
              <th>申请状态</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="user in allUsers" :key="user.userId">
              <td>{{ user.userId }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.email || '-' }}</td>
              <td>
                <span :class="['role-badge', user.role]">{{ user.role }}</span>
              </td>
              <td>
                <span :class="['volunteer-badge', user.isVolunteer ? 'yes' : 'no']">
                  {{ user.isVolunteer ? '是' : '否' }}
                </span>
              </td>
              <td>
                <span :class="['status-badge', user.volunteerApplyStatus || 'none']">
                  {{ getVolunteerApplyStatusText(user.volunteerApplyStatus) }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <select
                    v-if="user.role !== 'admin'"
                    @change="assignRole(user, $event.target.value)"
                    class="role-select"
                  >
                    <option value="">分配角色</option>
                    <option value="admin">管理员</option>
                    <option value="volunteer">志愿者</option>
                    <option value="adopter">领养人</option>
                  </select>
                  <button
                    v-if="user.isVolunteer"
                    @click="removeVolunteer(user)"
                    class="btn-remove-volunteer"
                  >
                    取消志愿者
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 5. 活动管理 -->
      <div v-if="activeTab === 'activityManage'" class="tab-content">
        <h2>志愿活动管理</h2>
        <button @click="openCreateActivityForm" class="btn-add">+ 新建活动</button>

        <!-- 新增/编辑活动表单 -->
        <div v-if="showActivityForm" class="activity-form">
          <h3>{{ editingActivity ? '编辑活动' : '新建活动' }}</h3>
          <div class="form-row">
            <label>活动名称：</label>
            <input v-model="activityForm.activityName" placeholder="请输入活动名称" />
          </div>
          <div class="form-row">
            <label>开始时间：</label>
            <input type="datetime-local" v-model="activityForm.startTime" />
          </div>
          <div class="form-row">
            <label>结束时间：</label>
            <input type="datetime-local" v-model="activityForm.endTime" />
          </div>
          <div class="form-row">
            <label>地点：</label>
            <input v-model="activityForm.location" placeholder="活动地点" />
          </div>
          <div class="form-row">
            <label>描述：</label>
            <textarea v-model="activityForm.description" rows="3" placeholder="活动详情"></textarea>
          </div>
          <div class="form-row">
            <label>招募状态：</label>
            <select v-model="activityForm.recruitStatus">
              <option value="recruiting">招募中</option>
              <option value="closed">已关闭</option>
            </select>
          </div>
          <div class="form-buttons">
            <button @click="saveActivity" class="btn-save">保存</button>
            <button @click="cancelActivityForm" class="btn-cancel">取消</button>
          </div>
        </div>

        <!-- 活动列表 -->
        <div v-if="activities.length === 0" class="empty-state">
          <p>暂无活动数据</p>
        </div>
        <div v-else class="activity-list">
          <div v-for="act in activities" :key="act.activityId" class="activity-card">
            <div class="activity-info">
              <h3>{{ act.activityName }}</h3>
              <p><strong>时间：</strong>{{ formatDateTime(act.startTime) }} ~ {{ formatDateTime(act.endTime) }}</p>
              <p><strong>地点：</strong>{{ act.location }}</p>
              <p><strong>描述：</strong>{{ act.description }}</p>
              <p><strong>招募状态：</strong>
                <span :class="act.recruitStatus === 'recruiting' ? 'status-recruiting' : 'status-closed'">
                  {{ act.recruitStatus === 'recruiting' ? '招募中' : '已关闭' }}
                </span>
              </p>
            </div>
            <div class="activity-actions">
              <button @click="editActivity(act)" class="btn-edit">编辑</button>
              <button @click="deleteActivity(act)" class="btn-delete">删除</button>
              <button @click="toggleRecruitStatus(act)" class="btn-toggle">
                {{ act.recruitStatus === 'recruiting' ? '关闭招募' : '开启招募' }}
              </button>
              <button @click="calcDuration(act.activityId)" class="btn-calc">计算服务时长</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { userApi, volunteerApi } from '@/api/index.js'

export default {
  name: 'AdminDashboardView',
  data() {
    return {
      adminUser: null,
      pendingUsers: [],
      volunteerApplications: [],     // 待审核的志愿者申请
      pendingRegistrations: [],      // 新增：待审核的活动报名
      allUsers: [],
      activities: [],                // 所有活动
      activeTab: 'pending',
      showActivityForm: false,
      editingActivity: false,
      activityForm: {
        activityId: null,
        activityName: '',
        startTime: '',
        endTime: '',
        location: '',
        description: '',
        recruitStatus: 'recruiting'
      }
    }
  },
  async created() {
    this.checkAdminAuth()
    await this.loadData()
    await this.loadVolunteerApplications()
    await this.loadPendingRegistrations()   // 新增：加载待审核报名
    await this.loadActivities()
  },
  methods: {
    checkAdminAuth() {
      const isAdmin = localStorage.getItem('isAdmin')
      const user = localStorage.getItem('currentUser')
      if (!isAdmin || !user) {
        alert('请先以管理员身份登录')
        this.$router.push('/admin/login')
        return
      }
      this.adminUser = JSON.parse(user)
      if (this.adminUser.role !== 'admin') {
        alert('无权访问此页面')
        this.$router.push('/')
      }
    },

    async loadData() {
      await this.loadPendingUsers()
      await this.loadAllUsers()
    },

    async loadPendingUsers() {
      try {
        const response = await userApi.getPendingUsers()
        this.pendingUsers = response.data
      } catch (error) {
        console.error('加载待审核用户失败:', error)
      }
    },

    async loadAllUsers() {
      try {
        const response = await userApi.getAllUsersForAdmin()
        this.allUsers = response.data
      } catch (error) {
        console.error('加载所有用户失败:', error)
      }
    },

    async loadVolunteerApplications() {
      try {
        const response = await userApi.getVolunteerApplications()
        this.volunteerApplications = response.data
      } catch (error) {
        console.error('加载志愿者申请失败:', error)
      }
    },

    // 新增：加载待审核的报名记录
    async loadPendingRegistrations() {
      try {
        const response = await volunteerApi.getPendingRegistrations()
        this.pendingRegistrations = response.data
      } catch (error) {
        console.error('加载待审核报名失败:', error)
      }
    },

    async loadActivities() {
      try {
        const response = await volunteerApi.getActivities()
        this.activities = response.data
      } catch (error) {
        console.error('加载活动列表失败:', error)
      }
    },

    // 注册审核
    async approveUser(user) {
      if (!confirm(`确定要通过用户 "${user.username}" 的注册申请吗？`)) return
      try {
        await userApi.approveUser(user.userId)
        alert('审核通过')
        await this.loadData()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    async rejectUser(user) {
      if (!confirm(`确定要拒绝用户 "${user.username}" 的注册申请吗？`)) return
      try {
        await userApi.rejectUser(user.userId)
        alert('已拒绝')
        await this.loadData()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    // 志愿者申请审核
    async approveVolunteerApp(user) {
      if (!confirm(`确定要通过用户 "${user.username}" 的志愿者申请吗？`)) return
      try {
        await userApi.approveVolunteerApplication(user.userId)
        alert('已通过志愿者申请')
        await this.loadVolunteerApplications()
        await this.loadAllUsers()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    async rejectVolunteerApp(user) {
      if (!confirm(`确定要拒绝用户 "${user.username}" 的志愿者申请吗？`)) return
      try {
        await userApi.rejectVolunteerApplication(user.userId)
        alert('已拒绝志愿者申请')
        await this.loadVolunteerApplications()
        await this.loadAllUsers()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    // 新增：报名审核
    async approveRegistration(reg) {
      if (!confirm(`通过用户 ${reg.username} 对活动“${reg.activity_name}”的报名吗？`)) return
      try {
        await volunteerApi.approveRegistration(reg.activity_id, reg.user_id)
        alert('报名已通过')
        await this.loadPendingRegistrations()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    async rejectRegistration(reg) {
      if (!confirm(`拒绝用户 ${reg.username} 对活动“${reg.activity_name}”的报名吗？`)) return
      try {
        await volunteerApi.rejectRegistration(reg.activity_id, reg.user_id)
        alert('已拒绝该报名')
        await this.loadPendingRegistrations()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    // 角色分配
    async assignRole(user, role) {
      if (!role) return
      if (!confirm(`确定要赋予用户 "${user.username}" ${this.getRoleText(role)} 权限吗？`)) return
      try {
        await userApi.assignRole(user.userId, role)
        alert('权限分配成功')
        await this.loadAllUsers()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    async removeVolunteer(user) {
      if (!confirm(`确定要取消用户 "${user.username}" 的志愿者身份吗？`)) return
      try {
        await userApi.removeRole(user.userId, 'volunteer')
        alert('已取消志愿者身份')
        await this.loadAllUsers()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    // 活动管理
    openCreateActivityForm() {
      this.editingActivity = false
      this.activityForm = {
        activityId: null,
        activityName: '',
        startTime: '',
        endTime: '',
        location: '',
        description: '',
        recruitStatus: 'recruiting'
      }
      this.showActivityForm = true
    },

    editActivity(activity) {
      this.editingActivity = true
      this.activityForm = {
        activityId: activity.activityId,
        activityName: activity.activityName,
        startTime: activity.startTime?.slice(0, 16),
        endTime: activity.endTime?.slice(0, 16),
        location: activity.location,
        description: activity.description,
        recruitStatus: activity.recruitStatus
      }
      this.showActivityForm = true
    },

    cancelActivityForm() {
      this.showActivityForm = false
      this.editingActivity = false
    },

    async saveActivity() {
      if (!this.activityForm.activityName) {
        alert('活动名称不能为空')
        return
      }
      try {
        if (this.editingActivity) {
          await volunteerApi.updateActivity(this.activityForm.activityId, this.activityForm)
          alert('活动更新成功')
        } else {
          await volunteerApi.createActivity(this.activityForm, this.adminUser.userId)
          alert('活动创建成功')
        }
        this.showActivityForm = false
        await this.loadActivities()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    async deleteActivity(activity) {
      if (!confirm(`确定要删除活动 "${activity.activityName}" 吗？删除后将同时删除所有报名记录。`)) return
      try {
        await volunteerApi.deleteActivity(activity.activityId)
        alert('活动已删除')
        await this.loadActivities()
      } catch (error) {
        alert('删除失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    async toggleRecruitStatus(activity) {
      const newStatus = activity.recruitStatus === 'recruiting' ? 'closed' : 'recruiting'
      try {
        await volunteerApi.updateRecruitStatus(activity.activityId, newStatus)
        alert(`已${newStatus === 'recruiting' ? '开启' : '关闭'}招募`)
        await this.loadActivities()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    async calcDuration(activityId) {
      try {
        const response = await volunteerApi.calculateDuration(activityId)
        alert(response.data.message || '服务时长计算完成')
        await this.loadActivities()
      } catch (error) {
        alert('计算失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    handleLogout() {
      localStorage.removeItem('currentUser')
      localStorage.removeItem('isAdmin')
      alert('已退出登录')
      this.$router.push('/admin/login')
    },

    formatDate(dateString) {
      if (!dateString) return '未知'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },

    formatDateTime(dateStr) {
      if (!dateStr) return ''
      return new Date(dateStr).toLocaleString('zh-CN')
    },

    getStatusText(status) {
      const statusMap = {
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '已拒绝',
        'pending_delete': '待注销'
      }
      return statusMap[status] || status
    },

    getVolunteerApplyStatusText(status) {
      const map = {
        'none': '未申请',
        'pending': '审核中',
        'approved': '已通过',
        'rejected': '已拒绝'
      }
      return map[status] || status
    },

    getRoleText(role) {
      const roleMap = {
        'admin': '管理员',
        'volunteer': '志愿者',
        'adopter': '领养人',
        'user': '普通用户'
      }
      return roleMap[role] || role
    }
  }
}
</script>

<style scoped>
/* 原有样式保留，添加活动管理相关样式 */
.admin-dashboard {
  min-height: 100vh;
  background: #f5f7fa;
}
.dashboard-header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  padding: 30px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}
.dashboard-header h1 {
  margin: 0;
  font-size: 28px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}
.admin-info {
  font-size: 16px;
}
.btn-logout {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2px solid white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}
.btn-logout:hover {
  background: white;
  color: #1e3c72;
}
.dashboard-content {
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;
}
.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  border-bottom: 2px solid #e0e0e0;
  flex-wrap: wrap;
}
.tab-btn {
  padding: 12px 24px;
  background: white;
  border: none;
  border-radius: 8px 8px 0 0;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s ease;
}
.tab-btn:hover {
  background: #f0f0f0;
}
.tab-btn.active {
  background: #1e3c72;
  color: white;
}
.tab-content {
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}
.tab-content h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
  font-size: 24px;
}
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 18px;
}
.user-list {
  display: grid;
  gap: 20px;
}
.user-card {
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}
.user-card:hover {
  border-color: #1e3c72;
  box-shadow: 0 4px 12px rgba(30, 60, 114, 0.1);
}
.user-info h3 {
  margin: 0 0 10px 0;
  color: #1e3c72;
  font-size: 20px;
}
.user-info p {
  margin: 5px 0;
  color: #666;
}
.user-actions {
  display: flex;
  gap: 10px;
}
.btn-approve {
  padding: 10px 20px;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}
.btn-approve:hover {
  background: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(76, 175, 80, 0.3);
}
.btn-reject {
  padding: 10px 20px;
  background: #f44336;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}
.btn-reject:hover {
  background: #da190b;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(244, 67, 54, 0.3);
}
.user-table-container {
  overflow-x: auto;
}
.user-table {
  width: 100%;
  border-collapse: collapse;
}
.user-table th,
.user-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}
.user-table th {
  background: #f5f7fa;
  font-weight: 600;
  color: #333;
}
.user-table tbody tr:hover {
  background: #f9f9f9;
}
.role-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}
.role-badge.admin {
  background: #ffeb3b;
  color: #333;
}
.role-badge.volunteer {
  background: #4caf50;
  color: white;
}
.role-badge.adopter {
  background: #2196f3;
  color: white;
}
.role-badge.user {
  background: #e0e0e0;
  color: #666;
}
.volunteer-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}
.volunteer-badge.yes {
  background: #4caf50;
  color: white;
}
.volunteer-badge.no {
  background: #e0e0e0;
  color: #666;
}
.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}
.status-badge.pending {
  background: #ff9800;
  color: white;
}
.status-badge.approved {
  background: #4caf50;
  color: white;
}
.status-badge.rejected {
  background: #f44336;
  color: white;
}
.status-badge.none {
  background: #e0e0e0;
  color: #666;
}
.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.role-select {
  padding: 6px 12px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}
.role-select:focus {
  outline: none;
  border-color: #1e3c72;
}
.btn-remove-volunteer {
  padding: 6px 12px;
  background: #ff9800;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s ease;
}
.btn-remove-volunteer:hover {
  background: #f57c00;
}
.btn-add {
  margin-bottom: 20px;
  padding: 10px 20px;
  background: #1e3c72;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.activity-form {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 30px;
  border: 1px solid #e0e0e0;
}
.form-row {
  margin-bottom: 15px;
}
.form-row label {
  display: inline-block;
  width: 100px;
  font-weight: 600;
}
.form-row input, .form-row select, .form-row textarea {
  width: 80%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.form-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.btn-save, .btn-cancel {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-save {
  background: #4caf50;
  color: white;
}
.btn-cancel {
  background: #ccc;
  color: #333;
}
.activity-list {
  display: grid;
  gap: 20px;
}
.activity-card {
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.activity-info h3 {
  margin: 0 0 10px 0;
  color: #1e3c72;
}
.activity-info p {
  margin: 5px 0;
  color: #666;
}
.status-recruiting {
  color: #4caf50;
  font-weight: bold;
}
.status-closed {
  color: #f44336;
  font-weight: bold;
}
.activity-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.activity-actions button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-edit {
  background: #2196f3;
  color: white;
}
.btn-delete {
  background: #f44336;
  color: white;
}
.btn-toggle {
  background: #ff9800;
  color: white;
}
.btn-calc {
  background: #9c27b0;
  color: white;
}
@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    gap: 15px;
    padding: 20px;
  }
  .dashboard-header h1 {
    font-size: 22px;
  }
  .dashboard-content {
    padding: 20px;
  }
  .user-card {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  .user-actions {
    width: 100%;
    justify-content: flex-end;
  }
  .activity-card {
    flex-direction: column;
  }
  .activity-actions {
    margin-top: 15px;
    flex-direction: row;
  }
}
</style>
