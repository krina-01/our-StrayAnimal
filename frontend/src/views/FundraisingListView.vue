<template>
  <div class="fundraising-list-container">
    <header class="header">
      <div class="logo">
        <h1>🐾 医疗救助募捐</h1>
      </div>
      <nav class="nav">
        <router-link to="/" class="nav-link">首页</router-link>
        <button @click="goToCreate" class="btn-create" v-if="currentUser">发布募捐</button>
        <template v-if="!currentUser">
          <router-link to="/login" class="nav-link">登录</router-link>
        </template>
        <template v-else>
          <span class="welcome-text">欢迎，{{ currentUser.username }}</span>
          <button @click="handleLogout" class="nav-link btn-logout">退出</button>
        </template>
      </nav>
    </header>

    <main class="main-content">
      <div class="filter-tabs">
        <button
          :class="['tab-btn', { active: currentStatus === 'all' }]"
          @click="filterByStatus('all')"
        >
          全部
        </button>
        <button
          :class="['tab-btn', { active: currentStatus === 'ongoing' }]"
          @click="filterByStatus('ongoing')"
        >
          进行中
        </button>
        <button
          :class="['tab-btn', { active: currentStatus === 'ended' }]"
          @click="filterByStatus('ended')"
        >
          已结束
        </button>
      </div>

      <div class="fundraising-grid" v-if="fundraisingList.length > 0">
        <div
          v-for="item in fundraisingList"
          :key="item.fundraisingId"
          class="fundraising-card"
          @click="goToDetail(item.fundraisingId)"
        >
          <div class="card-image">
            <img :src="item.activityImg || '/default-fundraising.jpg'" :alt="item.title" />
            <div class="status-badge" :class="item.status">
              {{ getStatusText(item.status) }}
            </div>
          </div>
          <div class="card-content">
            <h3>{{ item.title }}</h3>
            <p class="description">{{ item.content.substring(0, 100) }}...</p>
            <div class="progress-info">
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  :style="{ width: getProgressPercent(item) + '%' }"
                ></div>
              </div>
              <div class="progress-text">
                <span>已筹 ¥{{ item.currentAmount || 0 }}</span>
                <span>目标 ¥{{ item.targetAmount }}</span>
              </div>
            </div>
            <div class="card-footer">
              <span class="time">截止: {{ formatDate(item.endTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-else>
        <p>暂无募捐活动</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fundraisingApi } from '../api'

const router = useRouter()

const fundraisingList = ref([])
const currentStatus = ref('all')
const currentUser = ref(null)

onMounted(() => {
  const user = localStorage.getItem('currentUser')
  if (user) {
    currentUser.value = JSON.parse(user)
  }
  loadFundraisingList()
})

const loadFundraisingList = async () => {
  try {
    let response
    if (currentStatus.value === 'all') {
      response = await fundraisingApi.getAllFundraising()
    } else {
      response = await fundraisingApi.getFundraisingByStatus(currentStatus.value)
    }
    fundraisingList.value = response.data
  } catch (error) {
    console.error('加载募捐列表失败:', error)
    alert('加载募捐列表失败')
  }
}

const filterByStatus = (status) => {
  currentStatus.value = status
  loadFundraisingList()
}

const goToDetail = (id) => {
  router.push(`/fundraising/${id}`)
}

const goToCreate = () => {
  router.push('/fundraising/create')
}

const getStatusText = (status) => {
  const statusMap = {
    'pending': '待审核',
    'ongoing': '进行中',
    'ended': '已结束',
    'terminated': '已终止'
  }
  return statusMap[status] || status
}

const getProgressPercent = (item) => {
  if (!item.targetAmount || item.targetAmount === 0) return 0
  const percent = (item.currentAmount / item.targetAmount) * 100
  return Math.min(percent, 100)
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('currentUser')
    currentUser.value = null
    router.push('/')
  }
}
</script>

<style scoped>
.fundraising-list-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo h1 {
  margin: 0;
  color: #667eea;
  font-size: 24px;
}

.nav {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-link {
  color: #555;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 5px;
  transition: all 0.3s;
}

.nav-link:hover {
  color: #667eea;
  background: #f5f5f5;
}

.btn-create {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.btn-create:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-logout {
  background: transparent;
  border: 2px solid #667eea;
  cursor: pointer;
}

.welcome-text {
  color: #667eea;
  font-weight: 500;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.filter-tabs {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.tab-btn {
  padding: 10px 25px;
  border: 2px solid #667eea;
  background: white;
  color: #667eea;
  border-radius: 25px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.tab-btn.active,
.tab-btn:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.fundraising-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 25px;
}

.fundraising-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.fundraising-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  color: white;
}

.status-badge.ongoing {
  background: #52c41a;
}

.status-badge.ended {
  background: #999;
}

.status-badge.pending {
  background: #faad14;
}

.status-badge.terminated {
  background: #ff4d4f;
}

.card-content {
  padding: 20px;
}

.card-content h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 20px;
}

.description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
  min-height: 50px;
}

.progress-info {
  margin-bottom: 15px;
}

.progress-bar {
  height: 10px;
  background: #e0e0e0;
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.time {
  color: #999;
  font-size: 14px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
  font-size: 18px;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 15px;
    padding: 15px 20px;
  }

  .fundraising-grid {
    grid-template-columns: 1fr;
  }
}
</style>
