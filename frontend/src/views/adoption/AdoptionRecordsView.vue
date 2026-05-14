<template>
  <div class="adoption-records-container">
    <header class="header">
      <div class="logo">
        <h1>🐾 流浪动物救助平台</h1>
      </div>
      <nav class="nav">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/about" class="nav-link">关于我们</router-link>
        <template v-if="!currentUser">
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="nav-link btn-register">注册</router-link>
        </template>
        <template v-else>
          <span class="welcome-text">欢迎，{{ currentUser.username }}</span>
          <router-link to="/adoption-records" class="nav-link active">领养记录</router-link>
          <router-link to="/profile" class="nav-link">个人中心</router-link>
          <button @click="handleLogout" class="nav-link btn-logout">退出</button>
        </template>
      </nav>
    </header>

    <main class="main-content">
      <h1 class="page-title">我的领养申请记录</h1>

      <div class="content-wrapper">
        <div class="tabs">
          <button
            :class="['tab-btn', { active: activeTab === 'all' }]"
            @click="activeTab = 'all'"
          >
            全部申请 ({{ allApplications.length }})
          </button>
          <button
            :class="['tab-btn', { active: activeTab === 'pending' }]"
            @click="activeTab = 'pending'"
          >
            待审核 ({{ pendingApplications.length }})
          </button>
          <button
            :class="['tab-btn', { active: activeTab === 'approved' }]"
            @click="activeTab = 'approved'"
          >
            已通过 ({{ approvedApplications.length }})
          </button>
          <button
            :class="['tab-btn', { active: activeTab === 'rejected' }]"
            @click="activeTab = 'rejected'"
          >
            已拒绝 ({{ rejectedApplications.length }})
          </button>
        </div>

        <div v-if="loading" class="loading">加载中...</div>

        <div v-else-if="getCurrentApplications().length === 0" class="empty-state">
          <p>暂无领养申请记录</p>
          <router-link to="/adoption" class="btn-go-adopt">去领养动物</router-link>
        </div>

        <div v-else class="applications-list">
          <div v-for="application in getCurrentApplications()" :key="application.applicationId" class="application-card">
            <div class="application-header">
              <div class="animal-info">
                <h3>{{ getAnimalName(application) }}</h3>
                <p class="animal-species">{{ getAnimalSpecies(application) }}</p>
              </div>
              <span class="status-badge" :class="getStatusClass(application.auditStatus)">
                {{ getStatusText(application.auditStatus) }}
              </span>
            </div>

            <div class="application-body">
              <div class="info-row">
                <span class="label">申请时间：</span>
                <span class="value">{{ formatDate(application.applicationTime) }}</span>
              </div>
              <div class="info-row">
                <span class="label">领养原因：</span>
                <span class="value">{{ application.adoptionReason || '未填写' }}</span>
              </div>
              <div v-if="application.auditTime" class="info-row">
                <span class="label">审核时间：</span>
                <span class="value">{{ formatDate(application.auditTime) }}</span>
              </div>
              <div v-if="application.agreementStatus" class="info-row">
                <span class="label">协议状态：</span>
                <span class="value">
                  <span class="agreement-badge" :class="application.agreementStatus">
                    {{ getAgreementText(application.agreementStatus) }}
                  </span>
                </span>
              </div>
            </div>

            <div class="application-actions">
              <button
                v-if="application.auditStatus === 'approved' && !application.agreementContent"
                @click="openAgreementModal(application)"
                class="btn-sign"
              >
                签署协议
              </button>
              <button
                v-if="application.auditStatus === 'pending'"
                @click="cancelApplication(application)"
                class="btn-cancel"
              >
                取消申请
              </button>
              <button
                @click="viewDetail(application)"
                class="btn-detail"
              >
                查看详情
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div v-if="showAgreementModal" class="modal-overlay" @click="closeAgreementModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>签署领养协议</h2>
          <button @click="closeAgreementModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="agreement-preview" v-html="getAgreementHtml()"></div>
          <div class="form-group">
            <label>
              <input type="checkbox" v-model="agreedToTerms" />
              我已阅读并同意上述协议条款
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeAgreementModal" class="btn-modal-cancel">取消</button>
          <button
            @click="signAgreement"
            class="btn-modal-submit"
            :disabled="!agreedToTerms"
          >
            确认签署
          </button>
        </div>
      </div>
    </div>

    <footer class="footer">
      <p>&copy; 2026 流浪动物救助平台 | 用爱心温暖每一个生命</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { adoptionApplicationApi } from '@/api/index.js'

const router = useRouter()
const currentUser = ref<any>(null)
const applications = ref<any[]>([])
const loading = ref(false)
const activeTab = ref<'all' | 'pending' | 'approved' | 'rejected'>('all')
const showAgreementModal = ref(false)
const currentApplication = ref<any>(null)
const agreedToTerms = ref(false)

onMounted(() => {
  const userStr = localStorage.getItem('currentUser')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
    loadApplications()
  } else {
    router.push('/login')
  }
})

const loadApplications = async () => {
  if (!currentUser.value) return

  loading.value = true
  try {
    const response = await adoptionApplicationApi.getApplicationsByUserId(currentUser.value.userId)
    applications.value = response.data || []
  } catch (error) {
    console.error('加载领养申请失败:', error)
  } finally {
    loading.value = false
  }
}

const allApplications = computed(() => applications.value)

const pendingApplications = computed(() => {
  return applications.value.filter(app => app.auditStatus === 'pending')
})

const approvedApplications = computed(() => {
  return applications.value.filter(app => app.auditStatus === 'approved')
})

const rejectedApplications = computed(() => {
  return applications.value.filter(app => app.auditStatus === 'rejected')
})

const getCurrentApplications = () => {
  switch (activeTab.value) {
    case 'pending':
      return pendingApplications.value
    case 'approved':
      return approvedApplications.value
    case 'rejected':
      return rejectedApplications.value
    default:
      return allApplications.value
  }
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
    'completed': '已完成'
  }
  return statusMap[status] || status
}

const getStatusClass = (status: string) => {
  const classMap: Record<string, string> = {
    'pending': 'status-pending',
    'approved': 'status-approved',
    'rejected': 'status-rejected',
    'completed': 'status-completed'
  }
  return classMap[status] || ''
}

const getAgreementText = (status: string) => {
  const textMap: Record<string, string> = {
    'unsigned': '未签署',
    'signed': '已签署',
    'pending': '待签署'
  }
  return textMap[status] || status
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('currentUser')
    router.push('/')
  }
}

const openAgreementModal = (application: any) => {
  currentApplication.value = application
  agreedToTerms.value = false
  showAgreementModal.value = true
}

const closeAgreementModal = () => {
  showAgreementModal.value = false
  currentApplication.value = null
  agreedToTerms.value = false
}

const signAgreement = async () => {
  if (!currentApplication.value) return

  try {
    const agreementContent = `领养人 ${currentUser.value.username} 于 ${new Date().toLocaleString('zh-CN')} 签署领养协议`
    await adoptionApplicationApi.signAgreement(currentApplication.value.applicationId, agreementContent)
    alert('协议签署成功！')
    closeAgreementModal()
    await loadApplications()
  } catch (error) {
    console.error('签署协议失败:', error)
    alert('签署协议失败，请稍后重试')
  }
}

const cancelApplication = async (application: any) => {
  if (!confirm('确定要取消此领养申请吗？')) return

  try {
    await adoptionApplicationApi.deleteApplication(application.applicationId)
    alert('申请已取消')
    await loadApplications()
  } catch (error) {
    console.error('取消申请失败:', error)
    alert('取消申请失败，请稍后重试')
  }
}

const getAnimalName = (application: any) => {
  if (application.surrender?.animal?.name) {
    return application.surrender.animal.name
  }
  if (application.animalName) {
    return application.animalName
  }
  return '未知动物'
}

const getAnimalSpecies = (application: any) => {
  if (application.surrender?.animal?.species) {
    return application.surrender.animal.species
  }
  if (application.animalSpecies) {
    return application.animalSpecies
  }
  return '-'
}

const getSurrenderUserName = () => {
  if (currentApplication.value?.surrender?.user?.username) {
    return currentApplication.value.surrender.user.username
  }
  return '送养人'
}


const getAgreementHtml = () => {
  const surrenderUser = getSurrenderUserName()
  const adopter = currentUser.value?.username || '领养人'
  const animal = currentApplication.value?.surrender?.animal?.name || '未知动物'

  return `
    <div style="font-family: 'Microsoft YaHei', 'PingFang SC', Arial, sans-serif; line-height: 1.8; color: #2c3e50;">
      <h3 style="text-align: center; font-size: 26px; color: #667eea; margin: 0 0 25px 0; padding-bottom: 15px; border-bottom: 3px solid #667eea; font-weight: bold; letter-spacing: 2px;">
        🐾 宠物领养协议 🐾
      </h3>

      <div style="background: white; padding: 20px; border-radius: 8px; margin-bottom: 25px; box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1); border-left: 4px solid #667eea;">
        <div style="display: flex; align-items: center; margin-bottom: 12px; font-size: 16px;">
          <span style="font-weight: 600; color: #555; min-width: 120px;">📌 甲方（送养方）：</span>
          <span style="color: #2c3e50; font-weight: 500; font-size: 17px;">${surrenderUser}</span>
        </div>
        <div style="display: flex; align-items: center; margin-bottom: 12px; font-size: 16px;">
          <span style="font-weight: 600; color: #555; min-width: 120px;">📌 乙方（领养方）：</span>
          <span style="color: #2c3e50; font-weight: 500; font-size: 17px;">${adopter}</span>
        </div>
        <div style="display: flex; align-items: center; font-size: 16px;">
          <span style="font-weight: 600; color: #555; min-width: 120px;">🐾 领养动物：</span>
          <span style="color: #667eea; font-size: 18px; font-weight: 700;">${animal}</span>
        </div>
      </div>

      <div style="margin-bottom: 25px;">
        <div style="display: flex; align-items: flex-start; margin-bottom: 18px; padding: 15px; background: white; border-radius: 8px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);">
          <span style="display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 5px 12px; border-radius: 15px; font-size: 13px; font-weight: 600; margin-right: 12px; white-space: nowrap; min-width: 70px; text-align: center;">第一条</span>
          <span style="flex: 1; color: #555; font-size: 15px; line-height: 1.8; text-align: justify;">乙方承诺为领养动物提供安全、健康的生活环境，确保动物有足够的活动空间和良好的居住条件。</span>
        </div>

        <div style="display: flex; align-items: flex-start; margin-bottom: 18px; padding: 15px; background: white; border-radius: 8px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);">
          <span style="display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 5px 12px; border-radius: 15px; font-size: 13px; font-weight: 600; margin-right: 12px; white-space: nowrap; min-width: 70px; text-align: center;">第二条</span>
          <span style="flex: 1; color: #555; font-size: 15px; line-height: 1.8; text-align: justify;">乙方承诺定期为领养动物进行健康检查和疫苗接种，关注动物的身体状况，及时就医治疗。</span>
        </div>

        <div style="display: flex; align-items: flex-start; margin-bottom: 18px; padding: 15px; background: white; border-radius: 8px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);">
          <span style="display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 5px 12px; border-radius: 15px; font-size: 13px; font-weight: 600; margin-right: 12px; white-space: nowrap; min-width: 70px; text-align: center;">第三条</span>
          <span style="flex: 1; color: #555; font-size: 15px; line-height: 1.8; text-align: justify;">乙方不得虐待、遗弃领养动物，不得将其用于商业目的或实验用途，应给予动物充分的关爱和照顾。</span>
        </div>

        <div style="display: flex; align-items: flex-start; margin-bottom: 18px; padding: 15px; background: white; border-radius: 8px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);">
          <span style="display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 5px 12px; border-radius: 15px; font-size: 13px; font-weight: 600; margin-right: 12px; white-space: nowrap; min-width: 70px; text-align: center;">第四条</span>
          <span style="flex: 1; color: #555; font-size: 15px; line-height: 1.8; text-align: justify;">如乙方无法继续饲养，应将动物归还甲方或经甲方同意后转送给合适的领养人，不得随意处置。</span>
        </div>

        <div style="display: flex; align-items: flex-start; margin-bottom: 18px; padding: 15px; background: white; border-radius: 8px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);">
          <span style="display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 5px 12px; border-radius: 15px; font-size: 13px; font-weight: 600; margin-right: 12px; white-space: nowrap; min-width: 70px; text-align: center;">第五条</span>
          <span style="flex: 1; color: #555; font-size: 15px; line-height: 1.8; text-align: justify;">甲方有权在合理范围内对乙方饲养情况进行回访，了解动物的生活状况，乙方应予以配合。</span>
        </div>

        <div style="display: flex; align-items: flex-start; margin-bottom: 18px; padding: 15px; background: white; border-radius: 8px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);">
          <span style="display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 5px 12px; border-radius: 15px; font-size: 13px; font-weight: 600; margin-right: 12px; white-space: nowrap; min-width: 70px; text-align: center;">第六条</span>
          <span style="flex: 1; color: #555; font-size: 15px; line-height: 1.8; text-align: justify;">本协议自双方签署之日起生效，双方应严格遵守协议条款，共同保障动物的福利和权益。</span>
        </div>
      </div>

      <div style="background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%); padding: 18px; border-radius: 8px; border-left: 4px solid #ffc107; margin-top: 20px;">
        <p style="margin: 0 0 10px 0; color: #856404; font-size: 14px; font-style: italic;">
          💡 温馨提示：领养是一份责任，请慎重考虑后再签署协议。
        </p>
        <p style="margin: 0; color: #2c3e50; font-size: 15px; font-weight: 600; text-align: center; padding-top: 10px; border-top: 1px dashed #ffc107;">
          本人已仔细阅读并理解以上所有条款，自愿签署本协议。
        </p>
      </div>
    </div>
  `
}

const viewDetail = (application: any) => {
  const animalName = getAnimalName(application)
  alert(`申请详情：\n\n动物：${animalName}\n申请时间：${formatDate(application.applicationTime)}\n审核状态：${getStatusText(application.auditStatus)}\n领养原因：${application.adoptionReason || '未填写'}`)
}
</script>

<style scoped>
.adoption-records-container {
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

.nav-link:hover,
.nav-link.active {
  color: #667eea;
  background: #f0f2ff;
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

.loading {
  text-align: center;
  padding: 40px;
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

.applications-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.application-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.application-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.application-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.animal-info h3 {
  margin: 0 0 5px 0;
  font-size: 20px;
  color: #2c3e50;
}

.animal-species {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
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

.status-completed {
  background: #d1ecf1;
  color: #0c5460;
}

.application-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 14px;
}

.label {
  font-weight: 600;
  color: #333;
  min-width: 90px;
}

.value {
  color: #666;
  flex: 1;
}

.agreement-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.agreement-badge.unsigned {
  background: #fff3cd;
  color: #856404;
}

.agreement-badge.signed {
  background: #d4edda;
  color: #155724;
}

.application-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-sign,
.btn-cancel,
.btn-detail {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  flex: 1;
  min-width: 100px;
}

.btn-sign {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-sign:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-cancel {
  background: #f8f9fa;
  color: #666;
  border: 1px solid #ddd;
}

.btn-cancel:hover {
  background: #e9ecef;
}

.btn-detail {
  background: #e3f2fd;
  color: #1976d2;
}

.btn-detail:hover {
  background: #bbdefb;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 700px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  font-size: 22px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 32px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 40px;
  height: 40px;
  line-height: 1;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 30px;
}

.agreement-preview {
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 20px;
  max-height: 500px;
  overflow-y: auto;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.06);
}

.agreement-preview::-webkit-scrollbar {
  width: 8px;
}

.agreement-preview::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.agreement-preview::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
}

.agreement-preview::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5568d3 0%, #654a8f 100%);
}

.agreement-rich-text {
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.8;
  color: #2c3e50;
}

.agreement-title {
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 28px;
  margin: 0 0 30px 0;
  padding-bottom: 20px;
  border-bottom: 3px solid transparent;
  border-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%) 1;
  font-weight: bold;
  letter-spacing: 2px;
}

.agreement-parties {
  background: white;
  padding: 25px;
  border-radius: 10px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
  border: 2px solid #e8ecf1;
  position: relative;
  overflow: hidden;
}

.agreement-parties::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.party-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 16px;
  padding: 10px 15px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.party-item:hover {
  background: #f0f2ff;
  transform: translateX(5px);
}

.party-item:last-child {
  margin-bottom: 0;
}

.party-label {
  font-weight: 600;
  color: #555;
  min-width: 130px;
  display: flex;
  align-items: center;
}

.party-label::before {
  content: '📌';
  margin-right: 8px;
  font-size: 14px;
}

.party-value {
  color: #2c3e50;
  font-weight: 500;
  flex: 1;
}

.party-value.highlight {
  color: #667eea;
  font-size: 18px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(102, 126, 234, 0.2);
}

.agreement-content {
  margin-bottom: 30px;
}

.clause {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 18px;
  background: white;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-left: 4px solid transparent;
  position: relative;
}

.clause::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px 0 0 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.clause:hover {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  transform: translateX(8px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
}

.clause:hover::before {
  opacity: 1;
}

.clause-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  margin-right: 15px;
  white-space: nowrap;
  min-width: 75px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  flex-shrink: 0;
}

.clause-text {
  flex: 1;
  color: #555;
  font-size: 15px;
  line-height: 1.9;
  text-align: justify;
  font-weight: 400;
}

.agreement-footer {
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
  padding: 22px;
  border-radius: 10px;
  border: 2px solid #ffc107;
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.15);
  position: relative;
}

.agreement-footer::before {
  content: '⚠️';
  position: absolute;
  top: -15px;
  right: 20px;
  font-size: 30px;
  background: white;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.agreement-note {
  margin: 0 0 12px 0;
  color: #856404;
  font-size: 14px;
  font-style: italic;
  line-height: 1.6;
  padding-left: 5px;
}

.agreement-confirm {
  margin: 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
  padding-top: 15px;
  border-top: 2px dashed #ffc107;
  line-height: 1.6;
}

.form-group {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px solid #e8ecf1;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  color: #2c3e50;
  cursor: pointer;
  font-weight: 500;
}

.form-group input[type="checkbox"] {
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: #667eea;
}

.form-group label:hover {
  color: #667eea;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 20px 30px;
  border-top: 1px solid #eee;
}

.btn-modal-cancel,
.btn-modal-submit {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-modal-cancel {
  background: #f8f9fa;
  color: #666;
  border: 1px solid #ddd;
}

.btn-modal-cancel:hover {
  background: #e9ecef;
}

.btn-modal-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-modal-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-modal-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

  .applications-list {
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

  .application-actions {
    flex-direction: column;
  }

  .btn-sign,
  .btn-cancel,
  .btn-detail {
    width: 100%;
  }

  .modal-content {
    width: 95%;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 20px;
  }
}
</style>
