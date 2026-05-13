<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const currentUser = ref<any>(null)
const adoptedAnimals = ref<any[]>([])
const surrenderedAnimals = ref<any[]>([])
const adoptedVisitRecords = ref<any[]>([])
const surrenderedVisitRecords = ref<any[]>([])
const loading = ref(false)
const showVisitModal = ref(false)
const currentAnimal = ref<any>(null)
const activeTab = ref<'adopted' | 'surrendered'>('adopted')
const visitForm = ref({
  visitContent: '',
  animalStatus: ''
})

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

onMounted(() => {
  const userStr = localStorage.getItem('currentUser')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
    console.log('当前用户:', currentUser.value)
    loadData()
  } else {
    router.push('/login')
  }
})

watch(activeTab, (newTab) => {
  console.log('切换到标签页:', newTab)
  console.log('我的领养动物数量:', adoptedAnimals.value.length)
  console.log('我的送养动物数量:', surrenderedAnimals.value.length)
  console.log('显示动物数量:', getDisplayAnimals.value.length)
  console.log('显示动物列表:', getDisplayAnimals.value)
})

const loadData = async () => {
  if (!currentUser.value) return

  loading.value = true
  try {
    await Promise.all([
      loadAdoptedAnimals(),
      loadSurrenderedAnimals(),
      loadAdoptedVisitRecords(),
      loadSurrenderedVisitRecords()
    ])
    console.log('=== 数据加载完成 ===')
    console.log('adoptedAnimals:', adoptedAnimals.value)
    console.log('surrenderedAnimals:', surrenderedAnimals.value)
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadAdoptedAnimals = async () => {
  if (!currentUser.value) return

  try {
    // 查询我的领养申请（审核通过且动物已领养）
    const response = await apiClient.get(`/adoption-applications/user/${currentUser.value.userId}`)
    const applications = response.data || []
    console.log('我的领养申请 - 总数:', applications.length)
    console.log('申请列表:', applications)

    // 过滤出审核通过且动物已领养的申请，提取动物信息
    adoptedAnimals.value = applications
      .filter(app => app.auditStatus === 'approved' && app.surrender?.animal?.adoptStatus === 'adopted')
      .map(app => ({
        ...app.surrender.animal,
        applicationId: app.applicationId,
        surrenderId: app.surrenderId
      }))

    console.log('我的领养动物 (申请approved且动物adopted):', adoptedAnimals.value)
  } catch (error) {
    console.error('加载已领养动物失败:', error)
  }
}

const loadSurrenderedAnimals = async () => {
  if (!currentUser.value) return

  try {
    const response = await apiClient.get(`/surrender/user/${currentUser.value.userId}`)
    const surrenders = response.data || []
    console.log('我的送养记录 - 总数:', surrenders.length)
    console.log('送养记录原始数据:', surrenders)

    // 过滤出审核通过、状态为published、且动物已领养的送养
    surrenderedAnimals.value = surrenders
      .filter(s =>
        s.auditStatus === 'approved' &&
        s.status === 'published' &&
        s.animal?.adoptStatus === 'adopted'
      )
      .map(s => ({
        ...s.animal,
        surrenderId: s.surrenderId,
        surrenderReason: s.surrenderReason
      }))

    console.log('我的送养动物 (审核approved、状态published、动物adopted):', surrenderedAnimals.value)
  } catch (error) {
    console.error('加载已送养动物失败:', error)
  }
}

const loadAdoptedVisitRecords = async () => {
  if (!currentUser.value) return

  try {
    const response = await apiClient.get(`/adoption-visits/user/${currentUser.value.userId}`)
    adoptedVisitRecords.value = response.data || []
    console.log('领养回访记录:', adoptedVisitRecords.value)
  } catch (error) {
    console.error('加载领养回访记录失败:', error)
  }
}

const loadSurrenderedVisitRecords = async () => {
  if (!currentUser.value) return

  try {
    const response = await apiClient.get(`/adoption-visits/surrender-user/${currentUser.value.userId}`)
    surrenderedVisitRecords.value = response.data || []
    console.log('送养回访记录:', surrenderedVisitRecords.value)
  } catch (error) {
    console.error('加载送养回访记录失败:', error)
  }
}

const openVisitModal = (animal: any) => {
  currentAnimal.value = animal
  visitForm.value = {
    visitContent: '',
    animalStatus: ''
  }
  showVisitModal.value = true
}

const closeVisitModal = () => {
  showVisitModal.value = false
  currentAnimal.value = null
  visitForm.value = {
    visitContent: '',
    animalStatus: ''
  }
}

const submitVisit = async () => {
  if (!currentAnimal.value || !visitForm.value.visitContent) {
    alert('请填写回访内容')
    return
  }

  try {
    await apiClient.post('/adoption-visits', {
      animalId: currentAnimal.value.animalId,
      userId: currentUser.value.userId,
      visitContent: visitForm.value.visitContent,
      animalStatus: visitForm.value.animalStatus || '良好'
    })

    alert('回访记录提交成功！')
    closeVisitModal()
    await loadAdoptedVisitRecords()
  } catch (error) {
    console.error('提交回访记录失败:', error)
    alert('提交失败，请稍后重试')
  }
}

const viewVisitDetail = (animalId: number) => {
  const records = activeTab.value === 'adopted'
    ? adoptedVisitRecords.value.filter(v => v.animalId === animalId)
    : surrenderedVisitRecords.value.filter(v => v.animalId === animalId)

  if (records.length === 0) {
    alert('暂无回访记录')
    return
  }

  let detail = '回访记录：\n\n'
  records.forEach((record, index) => {
    detail += `第${index + 1}次回访\n`
    detail += `时间：${new Date(record.visitTime).toLocaleString('zh-CN')}\n`
    detail += `动物状态：${record.animalStatus || '未填写'}\n`
    detail += `回访内容：${record.visitContent}\n\n`
  })

  alert(detail)
}

const getVisitCount = (animalId: number) => {
  const records = activeTab.value === 'adopted'
    ? adoptedVisitRecords.value
    : surrenderedVisitRecords.value
  return records.filter(v => v.animalId === animalId).length
}

const getLastVisitTime = (animalId: number) => {
  const records = activeTab.value === 'adopted'
    ? adoptedVisitRecords.value
    : surrenderedVisitRecords.value

  const filteredRecords = records.filter(v => v.animalId === animalId)
  if (filteredRecords.length === 0) return '暂无回访'

  const lastRecord = filteredRecords.sort((a, b) =>
    new Date(b.visitTime).getTime() - new Date(a.visitTime).getTime()
  )[0]

  return new Date(lastRecord.visitTime).toLocaleDateString('zh-CN')
}

const getDisplayAnimals = computed(() => {
  const result = activeTab.value === 'adopted' ? adoptedAnimals.value : surrenderedAnimals.value
  console.log('getDisplayAnimals 计算:', {
    activeTab: activeTab.value,
    adoptedAnimals: adoptedAnimals.value,
    surrenderedAnimals: surrenderedAnimals.value,
    result: result
  })
  return result
})

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('currentUser')
    router.push('/')
  }
}
</script>

<template>
  <div class="visit-container">
    <header class="header">
      <div class="logo">
        <h1>🐾 流浪动物救助平台</h1>
      </div>
      <nav class="nav">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/adoption" class="nav-link">动物领养</router-link>
        <router-link to="/surrender" class="nav-link">送养发布</router-link>
        <router-link to="/adoption-visit" class="nav-link active">领养回访</router-link>
        <template v-if="!currentUser">
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="nav-link btn-register">注册</router-link>
        </template>
        <template v-else>
          <span class="welcome-text">欢迎，{{ currentUser.username }}</span>
          <button @click="handleLogout" class="nav-link btn-logout">退出</button>
        </template>
      </nav>
    </header>

    <main class="main-content">
      <h1 class="page-title">📋 领养回访</h1>

      <div class="tabs">
        <button
          :class="['tab-btn', { active: activeTab === 'adopted' }]"
          @click="activeTab = 'adopted'"
        >
          我的领养 ({{ adoptedAnimals.length }})
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'surrendered' }]"
          @click="activeTab = 'surrendered'"
        >
          我的送养 ({{ surrenderedAnimals.length }})
        </button>
      </div>

      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="getDisplayAnimals.length === 0" class="empty-state">
        <p v-if="activeTab === 'adopted'">您还没有领养的动物</p>
        <p v-else>您还没有送养的动物被领养</p>
        <router-link v-if="activeTab === 'adopted'" to="/adoption" class="btn-go-adopt">去领养动物</router-link>
      </div>

      <div v-else class="animals-grid">
        <div v-for="animal in getDisplayAnimals" :key="animal.animalId" class="animal-card">
          <div class="animal-header">
            <h3 class="animal-name">{{ animal.name }}</h3>
            <span class="visit-count">{{ getVisitCount(animal.animalId) }} 次回访</span>
          </div>

          <div class="animal-info">
            <p><span class="label">种类:</span> {{ animal.species }}</p>
            <p><span class="label">年龄:</span> {{ animal.age }} 岁</p>
            <p><span class="label">性别:</span> {{ animal.gender === 'male' ? '公' : animal.gender === 'female' ? '母' : animal.gender }}</p>
            <p><span class="label">健康状况:</span> {{ animal.healthStatus }}</p>
            <p><span class="label">上次回访:</span> {{ getLastVisitTime(animal.animalId) }}</p>
          </div>

          <div class="animal-actions">
            <button @click="viewVisitDetail(animal.animalId)" class="btn-view">
              查看回访记录
            </button>
            <button
              v-if="activeTab === 'adopted'"
              @click="openVisitModal(animal)"
              class="btn-visit"
            >
              填写回访
            </button>
            <span v-else class="view-only-badge">送养方仅可查看</span>
          </div>
        </div>
      </div>
    </main>

    <!-- 填写回访弹窗 -->
    <div v-if="showVisitModal" class="modal-overlay" @click="closeVisitModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>填写回访记录 - {{ currentAnimal?.name }}</h2>
          <button @click="closeVisitModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>动物当前状态</label>
            <select v-model="visitForm.animalStatus" class="form-select">
              <option value="良好">良好</option>
              <option value="健康">健康</option>
              <option value="需要关注">需要关注</option>
              <option value="生病">生病</option>
              <option value="其他">其他</option>
            </select>
          </div>

          <div class="form-group">
            <label>回访内容 *</label>
            <textarea
              v-model="visitForm.visitContent"
              class="form-textarea"
              placeholder="请详细描述动物的生活状况、健康情况、行为表现等..."
              rows="6"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeVisitModal" class="btn-modal-cancel">取消</button>
          <button @click="submitVisit" class="btn-modal-submit">提交回访</button>
        </div>
      </div>
    </div>

    <footer class="footer">
      <p>&copy; 2026 流浪动物救助平台 | 用爱心温暖每一个生命</p>
    </footer>
  </div>
</template>

<style scoped>
.visit-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 1000;
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

.nav-link.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-register {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white !important;
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
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  width: 100%;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 30px;
  text-align: center;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  border-bottom: 2px solid #eee;
  padding-bottom: 10px;
}

.tab-btn {
  padding: 12px 24px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px 8px 0 0;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  background: #e8e8e8;
  color: #333;
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #999;
  font-size: 16px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
  background: #f9f9f9;
  border-radius: 8px;
}

.empty-state p {
  margin-bottom: 20px;
  font-size: 16px;
}

.btn-go-adopt {
  display: inline-block;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 12px 32px;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-go-adopt:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.animals-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.animal-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.animal-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.animal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 2px solid #eee;
}

.animal-name {
  margin: 0;
  color: #333;
  font-size: 20px;
}

.visit-count {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 13px;
  font-weight: 600;
}

.animal-info {
  margin-bottom: 20px;
}

.animal-info p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
}

.label {
  font-weight: 600;
  color: #555;
  margin-right: 5px;
}

.animal-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.btn-view,
.btn-visit {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-view {
  background: #f5f5f5;
  color: #666;
}

.btn-view:hover {
  background: #e8e8e8;
}

.btn-visit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-visit:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.view-only-badge {
  flex: 1;
  text-align: center;
  padding: 10px;
  background: #fff3cd;
  color: #856404;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid #ffc107;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 2px solid #eee;
}

.modal-header h2 {
  margin: 0;
  color: #333;
  font-size: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 600;
  font-size: 14px;
}

.form-select,
.form-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
}

.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 120px;
}

.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 20px;
  border-top: 2px solid #eee;
}

.btn-modal-cancel,
.btn-modal-submit {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-modal-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-modal-cancel:hover {
  background: #e8e8e8;
}

.btn-modal-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-modal-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.footer {
  background: white;
  padding: 20px;
  text-align: center;
  color: #999;
  border-top: 1px solid #eee;
}
</style>
