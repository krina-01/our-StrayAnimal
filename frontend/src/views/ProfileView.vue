<template>
  <div class="profile-container">
    <div class="profile-box">
      <h2>个人信息</h2>

      <div v-if="loading" class="loading">加载中...</div>

      <form v-else @submit.prevent="handleUpdate">
        <div class="info-section">
          <h3>基本信息</h3>
          <div class="form-group">
            <label>用户名：</label>
            <input type="text" v-model="userInfo.username" disabled />
          </div>
          <div class="form-group">
            <label>邮箱：</label>
            <input type="email" v-model="userInfo.email" placeholder="请输入邮箱" />
          </div>
          <div class="form-group">
            <label>手机号：</label>
            <input type="tel" v-model="userInfo.phone" placeholder="请输入手机号" />
          </div>
        </div>

        <div class="info-section">
          <h3>详细资料</h3>
          <div class="form-group">
            <label>性别：</label>
            <select v-model="userInfo.gender">
              <option value="male">男</option>
              <option value="female">女</option>
              <option value="unknown">保密</option>
            </select>
          </div>
          <div class="form-group">
            <label>出生年份：</label>
            <input type="number" v-model="userInfo.birthYear" placeholder="例如：1990" min="1900" :max="currentYear" />
          </div>
          <div class="form-group">
            <label>地址：</label>
            <input type="text" v-model="userInfo.address" placeholder="请输入地址" />
          </div>
          <div class="form-group checkbox-group">
            <label>
              <input type="checkbox" v-model="userInfo.hasFixedIncome" />
              有固定收入
            </label>
          </div>
          <div class="form-group checkbox-group">
            <label>
              <input type="checkbox" v-model="userInfo.isPetExperience" />
              有养宠经验
            </label>
          </div>
        </div>

        <div class="info-section">
          <h3>权限信息</h3>
          <div class="permission-info">
            <p>当前角色：{{ getRoleText(userInfo.role) }}</p>
            <p>志愿者：{{ userInfo.isVolunteer ? '是' : '否' }}</p>
            <p v-if="!userInfo.isVolunteer && userInfo.volunteerApplyStatus">
              志愿者申请状态：{{ getVolunteerApplyStatusText(userInfo.volunteerApplyStatus) }}
            </p>
          </div>
          <div class="apply-buttons">
            <button
              type="button"
              @click="handleApplyVolunteer"
              :disabled="userInfo.isVolunteer || userInfo.volunteerApplyStatus === 'pending'"
              class="btn-apply"
            >
              {{ getVolunteerButtonText() }}
            </button>
            <button
              type="button"
              @click="handleApplyAdopter"
              :disabled="userInfo.role === 'adopter'"
              class="btn-apply"
            >
              {{ userInfo.role === 'adopter' ? '已是领养人' : '申请成为领养人' }}
            </button>
          </div>
        </div>

        <div class="button-group">
          <button type="submit" class="btn-save">保存修改</button>
          <button type="button" @click="handleDeleteAccount" class="btn-delete">注销账户</button>
          <router-link to="/" class="btn-cancel">返回首页</router-link>
        </div>

        <div v-if="message" :class="['message', messageType]">
          {{ message }}
        </div>
      </form>

      <!-- 志愿者专区（仅当 isVolunteer 为 true 时显示） -->
      <div class="info-section" v-if="userInfo.isVolunteer">
        <h3>我的志愿</h3>
        <div class="volunteer-tabs">
          <button
            :class="{ 'tab-active': volunteerTab === 'stats' }"
            @click="volunteerTab = 'stats'"
            class="volunteer-tab-btn">
            服务统计
          </button>
          <button
            :class="{ 'tab-active': volunteerTab === 'records' }"
            @click="volunteerTab = 'records'"
            class="volunteer-tab-btn">
            服务记录
          </button>
          <button
            :class="{ 'tab-active': volunteerTab === 'track' }"
            @click="volunteerTab = 'track'"
            class="volunteer-tab-btn">
            报名跟踪
          </button>
        </div>
        <div class="volunteer-content">
          <!-- 统计 -->
          <div v-if="volunteerTab === 'stats'" class="vol-stats">
            <p><strong>累计服务时长：</strong> {{ stats.totalDuration }} 分钟</p>
            <p><strong>参与活动次数：</strong> {{ stats.participationCount }} 次</p>
          </div>
          <!-- 服务记录 -->
          <div v-else-if="volunteerTab === 'records'" class="vol-records">
            <div v-if="serviceRecords.length === 0" class="empty-tip">暂无服务记录</div>
            <div v-for="(record, idx) in serviceRecords" :key="idx" class="record-item">
              <div class="record-title">{{ record.activityName }}</div>
              <div class="record-time">时间：{{ formatDateTime(record.startTime) }} ~ {{ formatDateTime(record.endTime) }}</div>
              <div class="record-duration">服务时长：{{ record.duration }} 分钟</div>
              <div class="record-location">地点：{{ record.location }}</div>
            </div>
          </div>
          <!-- 报名跟踪 -->
          <div v-else-if="volunteerTab === 'track'" class="vol-track">
            <div v-if="registrations.length === 0" class="empty-tip">暂无报名记录</div>
            <div v-for="(reg, idx) in registrations" :key="idx" class="track-item">
              <div class="track-title">{{ reg.activityName }}</div>
              <div class="track-time">活动时间：{{ formatDateTime(reg.activityStartTime) }} ~ {{ formatDateTime(reg.activityEndTime) }}</div>
              <div class="track-status">报名状态：{{ getRegStatusText(reg.status) }}</div>
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
  name: 'ProfileView',
  data() {
    return {
      userInfo: {
        userId: null,
        username: '',
        email: '',
        phone: '',
        gender: 'unknown',
        birthYear: null,
        address: '',
        hasFixedIncome: false,
        isPetExperience: false,
        role: 'user',
        isVolunteer: false,
        volunteerApplyStatus: 'none'   // 新增字段
      },
      loading: true,
      message: '',
      messageType: '',
      volunteerTab: 'stats',
      stats: { totalDuration: 0, participationCount: 0 },
      serviceRecords: [],
      registrations: []
    }
  },
  computed: {
    currentYear() {
      return new Date().getFullYear()
    }
  },
  async created() {
    await this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      const currentUser = localStorage.getItem('currentUser')
      if (!currentUser) {
        alert('请先登录')
        this.$router.push('/login')
        return
      }
      try {
        const user = JSON.parse(currentUser)
        const response = await userApi.getUserById(user.userId)
        this.userInfo = response.data
        this.loading = false
        if (this.userInfo.isVolunteer) {
          await this.loadVolunteerData()
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        this.message = '加载用户信息失败'
        this.messageType = 'error'
        this.loading = false
      }
    },

    async loadVolunteerData() {
      try {
        const userId = this.userInfo.userId
        const statsRes = await volunteerApi.getVolunteerStats(userId)
        this.stats = statsRes.data
        const recordsRes = await volunteerApi.getServiceRecords(userId)
        this.serviceRecords = recordsRes.data
        const regRes = await volunteerApi.getRegistrations(userId)
        this.registrations = regRes.data
      } catch (error) {
        console.error('加载志愿者数据失败:', error)
      }
    },

    async handleUpdate() {
      try {
        this.message = ''
        const response = await userApi.updateUser(this.userInfo.userId, this.userInfo)
        localStorage.setItem('currentUser', JSON.stringify(response.data))
        this.message = '个人信息更新成功！'
        this.messageType = 'success'
        if (this.userInfo.isVolunteer) {
          await this.loadVolunteerData()
        }
      } catch (error) {
        console.error('更新失败:', error)
        this.message = '更新失败，请稍后重试'
        this.messageType = 'error'
      }
    },

    async handleApplyVolunteer() {
      try {
        await userApi.applyVolunteer(this.userInfo.userId)
        this.message = '志愿者申请已提交，等待管理员审核'
        this.messageType = 'success'
        // 刷新用户信息以显示申请状态
        await this.loadUserInfo()
      } catch (error) {
        this.message = error.response?.data?.message || '申请失败，请稍后重试'
        this.messageType = 'error'
      }
    },

    async handleApplyAdopter() {
      try {
        await userApi.applyAdopter(this.userInfo.userId)
        this.message = '领养人申请已提交，等待管理员审核'
        this.messageType = 'success'
      } catch (error) {
        this.message = '申请失败，请稍后重试'
        this.messageType = 'error'
      }
    },

    async handleDeleteAccount() {
      if (!confirm('确定要注销账户吗？此操作需要管理员审核通过后才生效。')) {
        return
      }
      try {
        await userApi.deleteUser(this.userInfo.userId)
        localStorage.removeItem('currentUser')
        alert('注销申请已提交，等待管理员审核')
        this.$router.push('/')
      } catch (error) {
        this.message = '注销申请失败，请稍后重试'
        this.messageType = 'error'
      }
    },

    getRoleText(role) {
      const roleMap = {
        'admin': '管理员',
        'adopter': '领养人',
        'user': '普通用户'
      }
      return roleMap[role] || role
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

    getVolunteerButtonText() {
      if (this.userInfo.isVolunteer) return '已是志愿者'
      if (this.userInfo.volunteerApplyStatus === 'pending') return '申请审核中'
      if (this.userInfo.volunteerApplyStatus === 'rejected') return '申请被拒绝，可重新申请'
      return '申请成为志愿者'
    },

    getRegStatusText(status) {
      const map = { pending: '待审核', approved: '已通过', checked_in: '已签到' }
      return map[status] || status
    },

    formatDateTime(dateStr) {
      if (!dateStr) return ''
      return new Date(dateStr).toLocaleString('zh-CN')
    }
  }
}
</script>
<style scoped>
.profile-container {
  min-height: 100vh;
  padding: 40px 20px;
  background: #f5f5f5;
}

.profile-box {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

h3 {
  color: #555;
  margin-bottom: 20px;
  border-bottom: 2px solid #667eea;
  padding-bottom: 10px;
}

.info-section {
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 500;
}

input, select {
  width: 100%;
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 5px;
  font-size: 14px;
}

input:focus, select:focus {
  outline: none;
  border-color: #667eea;
}

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-group input[type="checkbox"] {
  width: auto;
  margin-right: 10px;
}

.permission-info {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 5px;
  margin-bottom: 15px;
}

.permission-info p {
  margin: 8px 0;
  color: #666;
}

.apply-buttons {
  display: flex;
  gap: 10px;
}

.btn-apply {
  flex: 1;
  padding: 10px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-apply:hover:not(:disabled) {
  background: #764ba2;
}

.btn-apply:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.button-group {
  display: flex;
  gap: 10px;
  margin-top: 30px;
}

.btn-save, .btn-delete, .btn-cancel {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  text-align: center;
  text-decoration: none;
  display: inline-block;
}

.btn-save {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-save:hover {
  transform: translateY(-2px);
}

.btn-delete {
  background: #f44336;
  color: white;
}

.btn-delete:hover {
  background: #d32f2f;
}

.btn-cancel {
  background: #e0e0e0;
  color: #333;
}

.btn-cancel:hover {
  background: #bdbdbd;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.message {
  margin-top: 20px;
  padding: 15px;
  border-radius: 5px;
  text-align: center;
}

.message.success {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.message.error {
  background-color: #ffebee;
  color: #c62828;
}

/* 志愿者专区样式 */
.volunteer-tabs {
  display: flex;
  gap: 10px;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 20px;
}
.volunteer-tab-btn {
  padding: 8px 16px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  color: #666;
  transition: all 0.3s;
}
.volunteer-tab-btn:hover {
  color: #667eea;
}
.volunteer-tab-btn.tab-active {
  color: #667eea;
  border-bottom: 2px solid #667eea;
  margin-bottom: -2px;
}
.volunteer-content {
  min-height: 150px;
}
.vol-stats p {
  font-size: 16px;
  margin: 10px 0;
  color: #333;
}
.record-item, .track-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}
.record-title, .track-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}
.record-time, .record-duration, .record-location, .track-time, .track-status {
  font-size: 14px;
  color: #666;
  margin: 4px 0;
}
.empty-tip {
  text-align: center;
  color: #999;
  padding: 30px;
}
</style>
