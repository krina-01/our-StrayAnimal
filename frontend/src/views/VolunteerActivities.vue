<template>
  <div class="volunteer-container">
    <h1>🐾 志愿活动中心</h1>
    <div class="filters">
      <button
        v-for="opt in filterOptions"
        :key="opt.value"
        :class="{ active: currentFilter === opt.value }"
        @click="currentFilter = opt.value">
        {{ opt.label }}
      </button>
    </div>
    <div class="activity-list">
      <div v-for="act in paginatedActivities" :key="act.activityId" class="activity-card">
        <h3>{{ act.activityName }}</h3>
        <p><strong>时间：</strong>{{ formatDateTime(act.startTime) }} ~ {{ formatDateTime(act.endTime) }}</p>
        <p><strong>地点：</strong>{{ act.location }}</p>
        <p><strong>描述：</strong>{{ act.description }}</p>
        <p><strong>招募状态：</strong>{{ act.recruitStatus === 'recruiting' ? '招募中' : '已关闭' }}</p>
        <p><strong>活动状态：</strong>{{ getActivityStatus(act) }}</p>
        <div class="actions">
          <button
            v-if="canRegister(act)"
            @click="register(act.activityId)"
            class="btn-register">
            报名
          </button>
          <button
            v-else-if="!currentUser.isVolunteer && act.recruitStatus === 'recruiting' && getActivityStatus(act) === '未开始'"
            @click="alertNotVolunteer"
            class="btn-register btn-disabled">
            成为志愿者后可报名
          </button>
          <button v-if="canCancel(act)" @click="cancel(act.activityId)" class="btn-cancel">取消报名</button>
          <button v-if="canCheckin(act)" @click="doCheckin(act.activityId)" class="btn-checkin">签到</button>
          <span v-if="registrationStatus(act.activityId)" class="status-badge">
            报名状态: {{ getStatusText(registrationStatus(act.activityId)) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 分页组件 -->
    <div v-if="totalActivities > 0" class="pagination">
      <div class="pagination-info">
        共 {{ totalActivities }} 条活动，第 {{ currentPage }} / {{ totalPages }} 页
      </div>
      <div class="pagination-controls">
        <button @click="prevPage" :disabled="currentPage === 1" class="page-btn">上一页</button>
        <span class="page-numbers">
          <button
            v-for="page in visiblePages"
            :key="page"
            @click="goToPage(page)"
            :class="['page-number', { active: page === currentPage }]">
            {{ page }}
          </button>
        </span>
        <button @click="nextPage" :disabled="currentPage === totalPages" class="page-btn">下一页</button>
        <select v-model="pageSize" @change="resetPage" class="page-size-select">
          <option :value="3">3条/页</option>
          <option :value="5">5条/页</option>
          <option :value="10">10条/页</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script>
import { volunteerApi } from '@/api/index.js'

export default {
  name: 'VolunteerActivities',
  data() {
    return {
      currentFilter: 'all',
      filterOptions: [
        { value: 'all', label: '全部' },
        { value: 'recruiting', label: '招募中' },
        { value: 'ongoing', label: '进行中' },
        { value: 'ended', label: '已结束' }
      ],
      activities: [],
      registrations: [],
      // 分页参数
      currentPage: 1,
      pageSize: 10,
    }
  },
  computed: {
    currentUser() {
      return JSON.parse(localStorage.getItem('currentUser') || '{}')
    },
    filteredActivities() {
      let filtered = this.activities
      if (this.currentFilter === 'recruiting') {
        filtered = this.activities.filter(a => a.recruitStatus === 'recruiting')
      } else if (this.currentFilter === 'ongoing') {
        const now = new Date()
        filtered = this.activities.filter(a => new Date(a.startTime) <= now && new Date(a.endTime) >= now)
      } else if (this.currentFilter === 'ended') {
        const now = new Date()
        filtered = this.activities.filter(a => new Date(a.endTime) < now)
      }
      return filtered
    },
    totalActivities() {
      return this.filteredActivities.length
    },
    totalPages() {
      return Math.ceil(this.totalActivities / this.pageSize) || 1
    },
    paginatedActivities() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredActivities.slice(start, end)
    },
    visiblePages() {
      const total = this.totalPages
      const current = this.currentPage
      const delta = 1 // 当前页前后显示页码数
      let range = []
      for (let i = Math.max(2, current - delta); i <= Math.min(total - 1, current + delta); i++) {
        range.push(i)
      }
      if (current - delta > 2) range.unshift('...')
      if (current + delta < total - 1) range.push('...')
      range.unshift(1)
      if (total > 1) range.push(total)
      return range
    }
  },
  watch: {
    currentFilter() {
      this.resetPage()
    },
    pageSize() {
      this.resetPage()
    }
  },
  async created() {
    await this.loadActivities()
    if (this.currentUser.userId) {
      await this.loadMyRegistrations()
    }
  },
  methods: {
    async loadActivities() {
      try {
        const res = await volunteerApi.getActivities()
        this.activities = res.data
      } catch (err) {
        console.error('加载活动失败', err)
      }
    },
    async loadMyRegistrations() {
      try {
        const res = await volunteerApi.getRegistrations(this.currentUser.userId)
        this.registrations = res.data
      } catch (err) {
        console.error('加载报名信息失败', err)
      }
    },
    registrationStatus(activityId) {
      const reg = this.registrations.find(r => r.activityId === activityId)
      return reg ? reg.status : null
    },
    getStatusText(status) {
      const map = { pending: '待审核', approved: '已通过', checked_in: '已签到' }
      return map[status] || status
    },
    getActivityStatus(act) {
      const now = new Date()
      const start = new Date(act.startTime)
      const end = new Date(act.endTime)
      if (now < start) return '未开始'
      if (now >= start && now <= end) return '进行中'
      return '已结束'
    },
    canRegister(act) {
      if (!this.currentUser.isVolunteer) return false
      if (act.recruitStatus !== 'recruiting') return false
      const status = this.registrationStatus(act.activityId)
      return !status && this.getActivityStatus(act) === '未开始'
    },
    canCancel(act) {
      const status = this.registrationStatus(act.activityId)
      return (status === 'pending' || status === 'approved') && this.getActivityStatus(act) === '未开始'
    },
    canCheckin(act) {
      const status = this.registrationStatus(act.activityId)
      return status === 'approved' && this.getActivityStatus(act) === '进行中'
    },
    alertNotVolunteer() {
      alert('您还不是志愿者，请先在个人中心申请成为志愿者，等待管理员审核通过后再报名活动')
    },
    async register(activityId) {
      if (!this.currentUser.userId) {
        alert('请先登录')
        return
      }
      if (!this.currentUser.isVolunteer) {
        alert('您不是志愿者，无法报名')
        return
      }
      try {
        await volunteerApi.registerActivity(activityId, this.currentUser.userId)
        alert('报名成功，等待管理员审核，审核通过后可直接在活动当天签到')
        await this.loadMyRegistrations()
      } catch (err) {
        alert(err.response?.data?.message || '报名失败')
      }
    },
    async cancel(activityId) {
      if (!confirm('确定取消报名吗？')) return
      try {
        await volunteerApi.cancelRegistration(activityId, this.currentUser.userId)
        alert('取消成功')
        await this.loadMyRegistrations()
      } catch (err) {
        alert(err.response?.data?.message || '取消失败')
      }
    },
    async doCheckin(activityId) {
      if (!confirm('请确认您已到达活动现场，签到后将记录您的参与。')) return
      try {
        await volunteerApi.checkin(activityId, this.currentUser.userId)
        alert('签到成功')
        await this.loadMyRegistrations()
      } catch (err) {
        alert(err.response?.data?.message || '签到失败')
      }
    },
    formatDateTime(dateStr) {
      if (!dateStr) return ''
      return new Date(dateStr).toLocaleString('zh-CN')
    },
    // 分页方法
    resetPage() {
      this.currentPage = 1
    },
    prevPage() {
      if (this.currentPage > 1) this.currentPage--
    },
    nextPage() {
      if (this.currentPage < this.totalPages) this.currentPage++
    },
    goToPage(page) {
      if (typeof page === 'number') this.currentPage = page
    }
  }
}
</script>

<style scoped>
.volunteer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
}
.filters button {
  padding: 8px 20px;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  border-radius: 20px;
}
.filters button.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}
.activity-list {
  display: grid;
  gap: 20px;
}
.activity-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.btn-register, .btn-cancel, .btn-checkin {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
.btn-register {
  background: #4caf50;
  color: white;
}
.btn-register.btn-disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 0.8;
}
.btn-cancel {
  background: #f44336;
  color: white;
}
.btn-checkin {
  background: #ff9800;
  color: white;
}
.status-badge {
  background: #f0f0f0;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
}
/* 分页样式 */
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}
.pagination-info {
  color: #666;
  font-size: 14px;
}
.pagination-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.page-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.page-btn:hover:not(:disabled) {
  background: #667eea;
  color: white;
  border-color: #667eea;
}
.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.page-numbers {
  display: flex;
  gap: 5px;
}
.page-number {
  min-width: 32px;
  padding: 6px 0;
  text-align: center;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}
.page-number.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}
.page-size-select {
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
}
@media (max-width: 768px) {
  .pagination {
    flex-direction: column;
    align-items: stretch;
  }
  .pagination-controls {
    justify-content: center;
  }
}
</style>
