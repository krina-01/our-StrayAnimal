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
          待审核用户 ({{ pendingUsers.length }})
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'all' }]"
          @click="activeTab = 'all'"
        >
          所有用户 ({{ allUsers.length }})
        </button>
      </div>

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
              <button @click="approveUser(user)" class="btn-approve">
                ✓ 通过
              </button>
              <button @click="rejectUser(user)" class="btn-reject">
                ✗ 拒绝
              </button>
            </div>
          </div>
        </div>
      </div>

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
                <th>状态</th>
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
                  <span :class="['status-badge', user.registerStatus]">
                    {{ getStatusText(user.registerStatus) }}
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
    </div>
  </div>
</template>

<script>
import { userApi } from '@/api/index.js'

export default {
  name: 'AdminDashboardView',
  data() {
    return {
      adminUser: null,
      pendingUsers: [],
      allUsers: [],
      activeTab: 'pending'
    }
  },
  async created() {
    this.checkAdminAuth()
    await this.loadData()
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

    async approveUser(user) {
      if (!confirm(`确定要通过用户 "${user.username}" 的注册申请吗？`)) {
        return
      }

      try {
        await userApi.approveUser(user.userId)
        alert('审核通过')
        await this.loadData()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    async rejectUser(user) {
      if (!confirm(`确定要拒绝用户 "${user.username}" 的注册申请吗？`)) {
        return
      }

      try {
        await userApi.rejectUser(user.userId)
        alert('已拒绝')
        await this.loadData()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    async assignRole(user, role) {
      if (!role) {
        return
      }

      if (!confirm(`确定要赋予用户 "${user.username}" ${this.getRoleText(role)} 权限吗？`)) {
        return
      }

      try {
        await userApi.assignRole(user.userId, role)
        alert('权限分配成功')
        await this.loadData()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    async removeVolunteer(user) {
      if (!confirm(`确定要取消用户 "${user.username}" 的志愿者身份吗？`)) {
        return
      }

      try {
        await userApi.removeRole(user.userId, 'volunteer')
        alert('已取消志愿者身份')
        await this.loadData()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
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

    getStatusText(status) {
      const statusMap = {
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '已拒绝',
        'pending_delete': '待注销'
      }
      return statusMap[status] || status
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

  .user-table {
    font-size: 14px;
  }

  .user-table th,
  .user-table td {
    padding: 8px;
  }
}
</style>
