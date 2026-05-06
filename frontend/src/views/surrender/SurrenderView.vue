<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { animalApi, surrenderApi, adoptionApplicationApi, userApi } from '@/api/index.js'

const router = useRouter()
const currentUser = ref<any>(null)
const userAnimals = ref<any[]>([])
const surrenderList = ref<any[]>([])
const loading = ref(false)
const activeTab = ref<'available' | 'surrendered' | 'adopted'>('available')
const adoptedApplications = ref<any[]>([])

onMounted(() => {
  const userStr = localStorage.getItem('currentUser')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
    loadUserAnimals()
    loadSurrenderList()
    loadAdoptedApplications()
  } else {
    router.push('/login')
  }
})

const loadUserAnimals = async () => {
  if (!currentUser.value) return

  loading.value = true
  try {
    const response = await animalApi.getAnimalsByUserId(currentUser.value.userId)
    userAnimals.value = response.data || []
  } catch (error) {
    console.error('加载动物信息失败:', error)
  } finally {
    loading.value = false
  }
}

const loadSurrenderList = async () => {
  if (!currentUser.value) return

  try {
    const response = await surrenderApi.getSurrenderListByUser(currentUser.value.userId)
    surrenderList.value = response.data || []
  } catch (error) {
    console.error('加载送养列表失败:', error)
  }
}

const loadAdoptedApplications = async () => {
  if (!currentUser.value) return

  try {
    console.log('=== 开始加载领养申请 ===')
    // 获取所有已通过审核的申请
    const response = await adoptionApplicationApi.getApplicationsByStatus('approved')
    console.log('API 返回的申请数据:', response.data)

    const applications = response.data || []
    console.log('申请数量:', applications.length)

    // 过滤出当前用户的送养相关的申请
    const userSurrenderIds = surrenderList.value.map(s => s.surrenderId)
    console.log('当前用户的送养IDs:', userSurrenderIds)

    adoptedApplications.value = applications.filter(app => {
      console.log('检查申请:', app.applicationId, 'surrenderId:', app.surrenderId, 'surrender:', app.surrender)
      return userSurrenderIds.includes(app.surrenderId)
    })

    console.log('过滤后的申请:', adoptedApplications.value)

    // 为每个申请加载领养人信息
    for (const app of adoptedApplications.value) {
      try {
        console.log(`加载申请 ${app.applicationId} 的领养人信息, userId:`, app.userId)
        const userResponse = await userApi.getUserById(app.userId)
        console.log('领养人信息:', userResponse.data)
        app.adopter = userResponse.data
      } catch (error) {
        console.error(`加载申请 ${app.applicationId} 的领养人信息失败:`, error)
      }
    }

    console.log('最终 adoptedApplications:', adoptedApplications.value)
  } catch (error) {
    console.error('加载领养申请失败:', error)
  }
}

const handlePublishSurrender = (animal: any) => {
  router.push({
    path: '/surrender/edit',
    query: { animalId: animal.animalId }
  })
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
    'surrender_pending': '送养审核中',
    'adopted': '已被领养',
    'available': '可送养'
  }
  return statusMap[status] || status
}

const getStatusClass = (status: string) => {
  const classMap: Record<string, string> = {
    'pending': 'status-pending',
    'approved': 'status-approved',
    'rejected': 'status-rejected',
    'surrender_pending': 'status-pending',
    'adopted': 'status-adopted',
    'available': 'status-available'
  }
  return classMap[status] || ''
}

const isAnimalInSurrender = (animalId: number) => {
  return surrenderList.value.some(s => s.animalId === animalId && s.auditStatus !== 'rejected')
}

const availableAnimals = computed(() => {
  return userAnimals.value.filter(animal =>
    !isAnimalInSurrender(animal.animalId) && animal.adoptStatus !== 'adopted'
  )
})

const surrenderedAnimals = computed(() => {
  return userAnimals.value.filter(animal =>
    isAnimalInSurrender(animal.animalId)
  )
})

const adoptedAnimals = computed(() => {
  return userAnimals.value.filter(animal =>
    animal.adoptStatus === 'adopted'
  )
})

const getAdopterForAnimal = (animalId: number) => {
  console.log('查询动物ID:', animalId)

  // 第一步：找到这个动物对应的送养记录
  const surrender = surrenderList.value.find(s => s.animalId === animalId)
  console.log('找到的送养记录:', surrender)

  if (!surrender) {
    console.log('未找到送养记录')
    return null
  }

  // 第二步：通过送养ID找到领养申请
  const application = adoptedApplications.value.find(app =>
    app.surrenderId === surrender.surrenderId
  )

  console.log('找到的领养申请:', application)
  console.log('领养人:', application?.adopter)

  return application?.adopter || null
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('currentUser')
    router.push('/')
  }
}
</script>

<template>
  <div class="surrender-container">
    <header class="header">
      <div class="logo">
        <h1>🐾 流浪动物救助平台</h1>
      </div>
      <nav class="nav">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/adoption" class="nav-link">动物领养</router-link>
        <router-link to="/surrender" class="nav-link">送养发布</router-link>
        <router-link to="/about" class="nav-link">关于我们</router-link>
        <template v-if="!currentUser">
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="nav-link btn-register">注册</router-link>
        </template>
        <template v-else>
          <span class="welcome-text">欢迎，{{ currentUser.username }}</span>
          <router-link to="/my-animals" class="nav-link">我的动物</router-link>
          <router-link to="/profile" class="nav-link">个人中心</router-link>
          <button @click="handleLogout" class="nav-link btn-logout">退出</button>
        </template>
      </nav>
    </header>

    <main class="main-content">
      <h1 class="page-title">送养信息发布</h1>

      <div class="content-wrapper">
        <div class="tabs">
          <button
            :class="['tab-btn', { active: activeTab === 'available' }]"
            @click="activeTab = 'available'"
          >
            可送养动物 ({{ availableAnimals.length }})
          </button>
          <button
            :class="['tab-btn', { active: activeTab === 'surrendered' }]"
            @click="activeTab = 'surrendered'"
          >
            已发布送养 ({{ surrenderedAnimals.length }})
          </button>
          <button
            :class="['tab-btn', { active: activeTab === 'adopted' }]"
            @click="activeTab = 'adopted'"
          >
            已被领养 ({{ adoptedAnimals.length }})
          </button>
        </div>

        <section v-if="activeTab === 'available'" class="animals-section">
          <h2 class="section-title">可送养的动物</h2>

          <div v-if="loading" class="loading">加载中...</div>

          <div v-else-if="availableAnimals.length === 0" class="empty-state">
            <p>暂无可送养的动物</p>
          </div>

          <div v-else class="animal-list">
            <div v-for="animal in availableAnimals" :key="animal.animalId" class="animal-card">
              <div class="animal-info">
                <h3 class="animal-name">{{ animal.name }}</h3>
                <div class="animal-details">
                  <p><span class="label">种类:</span> {{ animal.species }}</p>
                  <p><span class="label">年龄:</span> {{ animal.age }} 岁</p>
                  <p><span class="label">性别:</span> {{ animal.gender === 'male' ? '公' : animal.gender === 'female' ? '母' : animal.gender }}</p>
                  <p><span class="label">健康状况:</span> {{ animal.healthStatus }}</p>
                  <p><span class="label">位置:</span> {{ animal.location || '未填写' }}</p>
                  <p v-if="animal.rescueRecord"><span class="label">救助记录:</span> {{ animal.rescueRecord }}</p>
                </div>
              </div>

              <div class="animal-actions">
                <button
                  class="btn-publish"
                  @click="handlePublishSurrender(animal)"
                >
                  发布送养
                </button>
              </div>
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'surrendered'" class="surrender-history">
          <h2 class="section-title">已发布送养的申请</h2>

          <div v-if="surrenderList.length === 0" class="empty-state">
            <p>暂无已发布的送养申请</p>
          </div>

          <div v-else class="surrender-list">
            <div v-for="surrender in surrenderList" :key="surrender.surrenderId" class="surrender-card">
              <div class="surrender-header">
                <h3>{{ surrender.animal?.name || '未知动物' }}</h3>
                <span class="status-badge" :class="getStatusClass(surrender.auditStatus)">
                  {{ getStatusText(surrender.auditStatus) }}
                </span>
              </div>
              <div class="surrender-content">
                <p><span class="label">送养原因:</span> {{ surrender.surrenderReason }}</p>
                <p><span class="label">种类:</span> {{ surrender.animal?.species || '-' }}</p>
                <p><span class="label">年龄:</span> {{ surrender.animal?.age || '-' }} 岁</p>
                <p><span class="label">性别:</span> {{ surrender.animal?.gender === 'male' ? '公' : surrender.animal?.gender === 'female' ? '母' : surrender.animal?.gender || '-' }}</p>
                <p><span class="label">健康状况:</span> {{ surrender.animal?.healthStatus || '-' }}</p>
                <p><span class="label">位置:</span> {{ surrender.animal?.location || '未填写' }}</p>
                <p><span class="label">提交时间:</span> {{ new Date(surrender.submitTime).toLocaleString('zh-CN') }}</p>
                <p v-if="surrender.auditTime"><span class="label">审核时间:</span> {{ new Date(surrender.auditTime).toLocaleString('zh-CN') }}</p>
                <p v-if="surrender.auditRemark"><span class="label">审核备注:</span> {{ surrender.auditRemark }}</p>
              </div>
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'adopted'" class="animals-section">
          <h2 class="section-title">已被领养的动物</h2>

          <div v-if="adoptedAnimals.length === 0" class="empty-state">
            <p>暂无已被领养的动物</p>
          </div>

          <div v-else class="animal-list">
            <div v-for="animal in adoptedAnimals" :key="animal.animalId" class="animal-card adopted-card">
              <div class="animal-info">
                <h3 class="animal-name">{{ animal.name }}</h3>
                <div class="animal-details">
                  <p><span class="label">种类:</span> {{ animal.species }}</p>
                  <p><span class="label">年龄:</span> {{ animal.age }} 岁</p>
                  <p><span class="label">性别:</span> {{ animal.gender === 'male' ? '公' : animal.gender === 'female' ? '母' : animal.gender }}</p>
                  <p><span class="label">健康状况:</span> {{ animal.healthStatus }}</p>
                  <p><span class="label">位置:</span> {{ animal.location || '未填写' }}</p>
                </div>

                <!-- 领养人信息 -->
                <div class="adopter-section">
                  <h4 class="adopter-title">👤 领养人信息</h4>
                  <div class="adopter-card">
                    <div class="adopter-avatar">
                      {{ getAdopterForAnimal(animal.animalId)?.username?.charAt(0) || '?' }}
                    </div>
                    <div class="adopter-content">
                      <p class="adopter-name">{{ getAdopterForAnimal(animal.animalId)?.username || '未知' }}</p>
                      <p v-if="getAdopterForAnimal(animal.animalId)?.phone" class="adopter-contact">
                        📞 {{ getAdopterForAnimal(animal.animalId)?.phone }}
                      </p>
                      <p v-if="getAdopterForAnimal(animal.animalId)?.email" class="adopter-contact">
                        ✉️ {{ getAdopterForAnimal(animal.animalId)?.email }}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <div class="animal-actions">
                <span class="status-badge status-adopted">
                  已被领养
                </span>
              </div>
            </div>
          </div>
        </section>

        <section class="surrender-history" v-if="surrenderList.length > 0 && activeTab === 'available'">

        </section>
      </div>
    </main>

    <footer class="footer">
      <p>&copy; 2026 流浪动物救助平台 | 用爱心温暖每一个生命</p>
    </footer>
  </div>
</template>

<style scoped>
.surrender-container {
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

.btn-register {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white !important;
}

.btn-register:hover {
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

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.tabs {
  display: flex;
  gap: 10px;
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

.section-title {
  font-size: 22px;
  color: #555;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #eee;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  background: #f9f9f9;
  border-radius: 8px;
}

.animal-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.animal-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: all 0.3s ease;
}

.animal-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.animal-card.adopted-card {
  border-left: 4px solid #4caf50;
  background: linear-gradient(135deg, #f8fff8 0%, #ffffff 100%);
}

.animal-info {
  flex: 1;
}

.animal-name {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 20px;
}

.animal-details {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}

.animal-details p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.label {
  font-weight: 600;
  color: #555;
  margin-right: 5px;
}

.adopter-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px dashed #e0e0e0;
}

.adopter-title {
  margin: 0 0 15px 0;
  color: #2196f3;
  font-size: 16px;
  font-weight: 600;
}

.adopter-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: linear-gradient(135deg, #e3f2fd 0%, #f5f5f5 100%);
  border-radius: 10px;
  border-left: 4px solid #2196f3;
}

.adopter-avatar {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  flex-shrink: 0;
}

.adopter-content {
  flex: 1;
}

.adopter-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.adopter-contact {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.animal-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.btn-publish {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-publish:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-publish:active {
  transform: translateY(0);
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-approved {
  background: #d4edda;
  color: #155724;
}

.status-rejected {
  background: #f8d7da;
  color: #721c24;
}

.status-adopted {
  background: #d1ecf1;
  color: #0c5460;
}

.status-available {
  background: #d4edda;
  color: #155724;
}

.surrender-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.surrender-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
}

.surrender-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.surrender-header h3 {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
}

.surrender-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.surrender-content p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.footer {
  background: #f8f9fa;
  padding: 20px;
  text-align: center;
  color: #666;
  margin-top: auto;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 15px;
    padding: 15px 20px;
  }

  .nav {
    flex-wrap: wrap;
    justify-content: center;
  }

  .animal-list {
    grid-template-columns: 1fr;
  }

  .main-content {
    padding: 20px 15px;
  }

  .page-title {
    font-size: 24px;
  }

  .tabs {
    flex-direction: column;
  }

  .tab-btn {
    border-radius: 8px;
  }
}
</style>
