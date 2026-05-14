<template>
  <div class="my-fundraisings-container">
    <header class="header">
      <div class="logo">
        <h1>🐾 我的募捐活动</h1>
      </div>
      <nav class="nav">
        <button @click="goBack" class="nav-link">返回</button>
        <button @click="goToCreate" class="nav-link primary">发布新募捐</button>
      </nav>
    </header>

    <main class="main-content">
      <div class="stats-bar">
        <div class="stat-item">
          <span class="stat-number">{{ totalFundraisings }}</span>
          <span class="stat-label">总活动数</span>
        </div>
        <div class="stat-item">
          <span class="stat-number">{{ ongoingCount }}</span>
          <span class="stat-label">进行中</span>
        </div>
        <div class="stat-item">
          <span class="stat-number">{{ totalRaised }}</span>
          <span class="stat-label">总筹款(元)</span>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <p>加载中...</p>
      </div>

      <div v-else-if="myFundraisings.length === 0" class="empty-state">
        <p> 您还没有发布任何募捐活动</p>
        <button @click="goToCreate" class="btn-create">立即发布</button>
      </div>

      <div v-else class="fundraisings-list">
        <div v-for="item in myFundraisings" :key="item.fundraisingId"
             class="fundraising-card" :class="'status-' + item.status">
          <div class="card-header">
            <h3>{{ item.title }}</h3>
            <span class="status-badge" :class="item.status">
              {{ getStatusText(item.status) }}
            </span>
          </div>

          <div class="card-body">
            <div class="info-row">
              <span class="label">资金用途：</span>
              <span class="value">{{ item.purpose || '未填写' }}</span>
            </div>
            <div class="info-row">
              <span class="label">目标金额：</span>
              <span class="value">¥{{ item.targetAmount }}</span>
            </div>
            <div class="info-row">
              <span class="label">已筹金额：</span>
              <span class="value highlight">¥{{ item.currentAmount || 0 }}</span>
            </div>
            <div class="info-row">
              <span class="label">活动时间：</span>
              <span class="value">{{ formatDate(item.startTime) }} ~ {{ formatDate(item.endTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">活动详情：</span>
              <p class="content">{{ item.content }}</p>
            </div>

            <div v-if="item.currentAmount" class="progress-bar">
              <div class="progress-fill" :style="{ width: getProgressPercent(item) + '%' }"></div>
              <span class="progress-text">{{ getProgressPercent(item) }}%</span>
            </div>
          </div>

          <div v-if="item.status === 'ongoing'" class="card-actions">
            <button @click="completeFundraising(item)" class="btn-complete">
              ✓ 完成活动
            </button>
            <button @click="terminateFundraising(item)" class="btn-terminate">
              ✗ 终止活动
            </button>
          </div>

          <div v-else class="card-footer">
            <span class="end-note">
              {{ item.status === 'completed' ? '✅ 活动已完成' : '⚠️ 活动已终止' }}
            </span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fundraisingApi } from '../api'

const router = useRouter()
const loading = ref(false)
const myFundraisings = ref([])

const totalFundraisings = computed(() => myFundraisings.value.length)
const ongoingCount = computed(() => myFundraisings.value.filter(f => f.status === 'ongoing').length)
const totalRaised = computed(() => {
  return myFundraisings.value.reduce((sum, f) => sum + (f.currentAmount || 0), 0)
})

onMounted(() => {
  loadMyFundraisings()
})

const loadMyFundraisings = async () => {
  loading.value = true
  try {
    const user = JSON.parse(localStorage.getItem('currentUser'))
    if (!user || !user.userId) {
      alert('请先登录')
      router.push('/login')
      return
    }
    const response = await fundraisingApi.getMyFundraisings(user.userId)
    myFundraisings.value = response.data
  } catch (error) {
    console.error('加载失败:', error)
    alert('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  const map = {
    'pending': '待审核',
    'ongoing': '进行中',
    'completed': '已完成',
    'terminated': '已终止',
    'rejected': '已拒绝'
  }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getProgressPercent = (item) => {
  if (!item.targetAmount || item.targetAmount === 0) return 0
  const percent = ((item.currentAmount || 0) / item.targetAmount) * 100
  return Math.min(100, Math.round(percent))
}

const completeFundraising = async (item) => {
  if (!confirm(`确定要完成募捐活动 "${item.title}" 吗？\n完成后将无法继续接受捐赠。`)) {
    return
  }

  try {
    const user = JSON.parse(localStorage.getItem('currentUser'))
    await fundraisingApi.completeMyFundraising(item.fundraisingId, user.userId)
    alert('活动已完成')
    await loadMyFundraisings()
  } catch (error) {
    console.error('操作失败:', error)
    alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
  }
}

const terminateFundraising = async (item) => {
  if (!confirm(`确定要终止募捐活动 "${item.title}" 吗？\n终止后将无法继续接受捐赠。`)) {
    return
  }

  try {
    const user = JSON.parse(localStorage.getItem('currentUser'))
    await fundraisingApi.terminateMyFundraising(item.fundraisingId, user.userId)
    alert('活动已终止')
    await loadMyFundraisings()
  } catch (error) {
    console.error('操作失败:', error)
    alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
  }
}

const goBack = () => {
  router.push('/fundraising')
}

const goToCreate = () => {
  router.push('/fundraising/create')
}
</script>

<style scoped>
.my-fundraisings-container {
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
  gap: 15px;
  align-items: center;
}

.nav-link {
  color: #555;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 5px;
  transition: all 0.3s;
  border: 2px solid #667eea;
  background: transparent;
  cursor: pointer;
}

.nav-link:hover {
  color: #667eea;
  background: #f5f5f5;
}

.nav-link.primary {
  background: #667eea;
  color: white;
}

.nav-link.primary:hover {
  background: #5568d3;
}

.main-content {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
}

.stats-bar {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-item {
  background: white;
  padding: 25px;
  border-radius: 10px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-number {
  display: block;
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.empty-state p {
  font-size: 18px;
  color: #666;
  margin-bottom: 20px;
}

.btn-create {
  display: inline-block;
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s;
  border: none;
  cursor: pointer;
}

.btn-create:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.fundraisings-list {
  display: grid;
  gap: 20px;
}

.fundraising-card {
  background: white;
  border-radius: 10px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.fundraising-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.card-header h3 {
  margin: 0;
  color: #333;
  font-size: 20px;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.status-badge.pending {
  background: #ffeb3b;
  color: #333;
}

.status-badge.ongoing {
  background: #4caf50;
  color: white;
}

.status-badge.completed {
  background: #2196f3;
  color: white;
}

.status-badge.terminated,
.status-badge.rejected {
  background: #f44336;
  color: white;
}

.card-body {
  margin-bottom: 20px;
}

.info-row {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
}

.info-row .label {
  color: #666;
  font-weight: 600;
  min-width: 100px;
}

.info-row .value {
  color: #333;
  flex: 1;
}

.info-row .value.highlight {
  color: #4caf50;
  font-weight: bold;
  font-size: 18px;
}

.content {
  margin: 8px 0 0 0;
  color: #555;
  line-height: 1.6;
  white-space: pre-wrap;
}

.progress-bar {
  position: relative;
  height: 30px;
  background: #e0e0e0;
  border-radius: 15px;
  overflow: hidden;
  margin-top: 20px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s;
}

.progress-text {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
  font-weight: bold;
  color: white;
}

.card-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 2px solid #f0f0f0;
}

.btn-complete,
.btn-terminate {
  padding: 12px 25px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-complete {
  background: #2196f3;
  color: white;
}

.btn-complete:hover {
  background: #1976d2;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(33, 150, 243, 0.3);
}

.btn-terminate {
  background: #f44336;
  color: white;
}

.btn-terminate:hover {
  background: #d32f2f;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(244, 67, 54, 0.3);
}

.card-footer {
  padding-top: 15px;
  border-top: 2px solid #f0f0f0;
  text-align: center;
}

.end-note {
  color: #666;
  font-size: 14px;
}

@media (max-width: 768px) {
  .header {
    padding: 15px 20px;
    flex-direction: column;
    gap: 15px;
  }

  .stats-bar {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .card-actions {
    flex-direction: column;
  }

  .info-row {
    flex-direction: column;
  }

  .info-row .label {
    min-width: auto;
    margin-bottom: 4px;
  }
}
</style>
