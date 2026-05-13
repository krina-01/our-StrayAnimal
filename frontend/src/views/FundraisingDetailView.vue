<template>
  <div class="fundraising-detail-container">
    <header class="header">
      <div class="logo">
        <h1>🐾 募捐详情</h1>
      </div>
      <nav class="nav">
        <button @click="goBack" class="nav-link">返回列表</button>
      </nav>
    </header>

    <main class="main-content" v-if="fundraising">
      <div class="detail-header">
        <div class="detail-image">
          <img :src="fundraising.activityImg || '/default-fundraising.jpg'" :alt="fundraising.title" />
        </div>
        <div class="detail-info">
          <h2>{{ fundraising.title }}</h2>
          <div class="status-badge" :class="fundraising.status">
            {{ getStatusText(fundraising.status) }}
          </div>
          <p class="description">{{ fundraising.content }}</p>

          <div class="progress-section">
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: progressPercent + '%' }"
              ></div>
            </div>
            <div class="progress-stats">
              <div class="stat">
                <span class="label">已筹金额</span>
                <span class="value">¥{{ fundraising.currentAmount || 0 }}</span>
              </div>
              <div class="stat">
                <span class="label">目标金额</span>
                <span class="value">¥{{ fundraising.targetAmount }}</span>
              </div>
              <div class="stat">
                <span class="label">完成进度</span>
                <span class="value">{{ progressPercent.toFixed(1) }}%</span>
              </div>
            </div>
          </div>

          <div class="time-info">
            <span>开始: {{ formatDate(fundraising.startTime) }}</span>
            <span>结束: {{ formatDate(fundraising.endTime) }}</span>
          </div>

          <button
            @click="showDonateModal = true"
            class="btn-donate"
            :disabled="fundraising.status !== 'ongoing'"
            v-if="currentUser"
          >
            {{ fundraising.status === 'ongoing' ? '立即捐赠' : '当前不可捐赠' }}
          </button>
          <p class="login-tip" v-else>请先登录后再进行捐赠</p>
        </div>
      </div>

      <div class="tabs-section">
        <div class="tabs">
          <button
            :class="['tab-btn', { active: activeTab === 'donations' }]"
            @click="activeTab = 'donations'"
          >
            捐款记录
          </button>
          <button
            :class="['tab-btn', { active: activeTab === 'usage' }]"
            @click="activeTab = 'usage'"
          >
            资金用途
          </button>
          <button
            :class="['tab-btn', { active: activeTab === 'myDonations' }]"
            @click="loadMyDonations"
            v-if="currentUser"
          >
            我的捐款
          </button>
        </div>

        <div class="tab-content">
          <div v-if="activeTab === 'donations'" class="donations-list">
            <h3>捐款明细</h3>
            <div class="summary-cards">
              <div class="summary-card">
                <div class="summary-value">{{ summary.totalPeople || 0 }}</div>
                <div class="summary-label">捐款人数</div>
              </div>
              <div class="summary-card">
                <div class="summary-value">¥{{ summary.totalAmount || 0 }}</div>
                <div class="summary-label">捐款总额</div>
              </div>
            </div>
            <div class="donation-records" v-if="donationRecords.length > 0">
              <div v-for="record in donationRecords" :key="record.id" class="donation-item">
                <div class="donor-info">
                  <span class="donor-name">{{ record.isAnonymous ? '匿名爱心人士' : `用户${record.userId}` }}</span>
                  <span class="donate-time">{{ formatDateTime(record.payTime) }}</span>
                </div>
                <div class="donate-amount">¥{{ record.amount }}</div>
                <div class="donate-message" v-if="record.message">{{ record.message }}</div>
              </div>
            </div>
            <div class="empty-state" v-else>
              <p>暂无捐款记录</p>
            </div>
          </div>

          <div v-if="activeTab === 'usage'" class="usage-list">
            <h3>资金用途公示</h3>
            <div class="usage-records" v-if="usageRecords.length > 0">
              <div v-for="usage in usageRecords" :key="usage.id" class="usage-item">
                <div class="usage-header">
                  <h4>{{ usage.title }}</h4>
                  <div class="usage-status" :class="getUsageStatusClass(usage.status)">
                    {{ getUsageStatusText(usage.status) }}
                  </div>
                </div>
                <div class="usage-amount">金额: ¥{{ usage.amount }}</div>
                <div class="usage-content">{{ usage.content }}</div>
                <div class="usage-meta">
                  <span>申请时间: {{ formatDateTime(usage.createTime) }}</span>
                  <span v-if="usage.auditTime">审核时间: {{ formatDateTime(usage.auditTime) }}</span>
                </div>
              </div>
            </div>
            <div class="empty-state" v-else>
              <p>暂无资金用途记录</p>
            </div>
          </div>

          <div v-if="activeTab === 'myDonations'" class="my-donations-list">
            <h3>我的捐款记录</h3>
            <div class="donation-records" v-if="myDonations.length > 0">
              <div v-for="record in myDonations" :key="record.id" class="donation-item">
                <div class="donor-info">
                  <span class="donate-time">{{ formatDateTime(record.payTime) }}</span>
                </div>
                <div class="donate-amount">¥{{ record.amount }}</div>
                <div class="donate-message" v-if="record.message">{{ record.message }}</div>
              </div>
            </div>
            <div class="empty-state" v-else>
              <p>您还没有捐过款哦~</p>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div class="loading" v-if="!fundraising">
      <p>加载中...</p>
    </div>

    <div class="modal-overlay" v-if="showDonateModal" @click="closeDonateModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>爱心捐赠</h3>
          <button @click="closeDonateModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="amount-options">
            <label>选择捐赠金额：</label>
            <div class="amount-buttons">
              <button
                v-for="amount in [10, 20, 50, 100]"
                :key="amount"
                :class="['amount-btn', { selected: selectedAmount === amount }]"
                @click="selectedAmount = amount"
              >
                ¥{{ amount }}
              </button>
            </div>
            <div class="custom-amount">
              <label>或输入自定义金额：</label>
              <input
                type="number"
                v-model.number="customAmount"
                min="1"
                placeholder="请输入金额"
                @focus="selectedAmount = null"
              />
            </div>
          </div>
          <div class="message-input">
            <label>留言祝福（可选）：</label>
            <textarea
              v-model="donateMessage"
              rows="3"
              placeholder="写下您的祝福语..."
            ></textarea>
          </div>
          <div class="anonymous-option">
            <label>
              <input type="checkbox" v-model="isAnonymous" />
              匿名捐赠
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeDonateModal" class="btn-cancel">取消</button>
          <button @click="handleDonate" class="btn-confirm">确认捐赠</button>
        </div>
      </div>
    </div>

    <div class="modal-overlay" v-if="showCertificateModal" @click="closeCertificateModal">
      <div class="certificate-modal" @click.stop>
        <div class="certificate-header">
          <h3>🎉 捐赠证书</h3>
          <button @click="closeCertificateModal" class="close-btn">×</button>
        </div>
        <div class="certificate-body">
          <img :src="certificateImage" alt="捐赠证书" class="certificate-image" />
        </div>
        <div class="certificate-actions">
          <button @click="downloadCertificate" class="btn-download">下载证书</button>
          <button @click="closeCertificateModal" class="btn-close">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fundraisingApi } from '../api'

const route = useRoute()
const router = useRouter()

const fundraising = ref(null)
const summary = ref({})
const donationRecords = ref([])
const usageRecords = ref([])
const myDonations = ref([])
const activeTab = ref('donations')
const showDonateModal = ref(false)
const selectedAmount = ref(null)
const customAmount = ref(null)
const donateMessage = ref('')
const isAnonymous = ref(false)
const currentUser = ref(null)
const showCertificateModal = ref(false)
const certificateImage = ref('')
const lastDonation = ref(null)

const progressPercent = computed(() => {
  if (!fundraising.value || !fundraising.value.targetAmount || fundraising.value.targetAmount === 0) {
    return 0
  }
  const percent = ((fundraising.value.currentAmount || 0) / fundraising.value.targetAmount) * 100
  return Math.min(percent, 100)
})

const finalAmount = computed(() => {
  return selectedAmount.value || customAmount.value
})

onMounted(() => {
  const user = localStorage.getItem('currentUser')
  if (user) {
    currentUser.value = JSON.parse(user)
  }
  loadDetail()
})

const loadDetail = async () => {
  try {
    const id = route.params.id
    const response = await fundraisingApi.getFundraisingDetail(id)
    fundraising.value = response.data.project

    await loadSummary()
    await loadDonationRecords()
    await loadUsageRecords()
  } catch (error) {
    console.error('加载详情失败:', error)
    alert('加载详情失败')
  }
}

const loadSummary = async () => {
  try {
    const id = route.params.id
    const response = await fundraisingApi.getSummary(id)
    summary.value = response.data
  } catch (error) {
    console.error('加载汇总失败:', error)
  }
}

const loadDonationRecords = async () => {
  try {
    const id = route.params.id
    const response = await fundraisingApi.getDetails(id)
    donationRecords.value = response.data
  } catch (error) {
    console.error('加载捐款记录失败:', error)
  }
}

const loadUsageRecords = async () => {
  try {
    const id = route.params.id
    const response = await fundraisingApi.getDetails(id)
    usageRecords.value = response.data
  } catch (error) {
    console.error('加载用途记录失败:', error)
  }
}

const loadMyDonations = async () => {
  if (!currentUser.value) {
    alert('请先登录')
    return
  }
  try {
    const response = await fundraisingApi.getMyDonations(currentUser.value.userId)
    myDonations.value = response.data.filter(
      record => record.fundraisingId === parseInt(route.params.id)
    )
    activeTab.value = 'myDonations'
  } catch (error) {
    console.error('加载我的捐款失败:', error)
    alert('加载失败')
  }
}

const handleDonate = async () => {
  if (!finalAmount.value || finalAmount.value <= 0) {
    alert('请选择或输入捐赠金额')
    return
  }

  try {
    const donationData = {
      fundraisingId: parseInt(route.params.id),
      userId: currentUser.value.userId,
      amount: finalAmount.value,
      message: donateMessage.value,
      isAnonymous: isAnonymous.value
    }

    const response = await fundraisingApi.donate(donationData)
    lastDonation.value = response.data.record

    closeDonateModal()
    await loadDetail()

    generateCertificate()
    showCertificateModal.value = true
  } catch (error) {
    console.error('捐赠失败:', error)
    alert('捐赠失败，请稍后重试')
  }
}

const generateCertificate = () => {
  const canvas = document.createElement('canvas')
  canvas.width = 800
  canvas.height = 600
  const ctx = canvas.getContext('2d')

  const gradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height)
  gradient.addColorStop(0, '#fff9e6')
  gradient.addColorStop(1, '#ffe6f0')
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, canvas.width, canvas.height)

  ctx.strokeStyle = '#d4af37'
  ctx.lineWidth = 8
  ctx.strokeRect(20, 20, canvas.width - 40, canvas.height - 40)

  ctx.strokeStyle = '#d4af37'
  ctx.lineWidth = 2
  ctx.strokeRect(30, 30, canvas.width - 60, canvas.height - 60)

  ctx.fillStyle = '#d4af37'
  ctx.font = 'bold 48px "Microsoft YaHei", Arial'
  ctx.textAlign = 'center'
  ctx.fillText('捐赠证书', canvas.width / 2, 120)

  const certificateNo = 'CERT' + Date.now()
  ctx.fillStyle = '#666'
  ctx.font = '16px "Microsoft YaHei", Arial'
  ctx.fillText(`证书编号：${certificateNo}`, canvas.width / 2, 160)

  ctx.fillStyle = '#333'
  ctx.font = '20px "Microsoft YaHei", Arial'
  ctx.fillText('兹证明', canvas.width / 2, 220)

  const donorName = currentUser.value?.username || '爱心人士'
  ctx.fillStyle = '#667eea'
  ctx.font = 'bold 32px "Microsoft YaHei", Arial'
  ctx.fillText(donorName, canvas.width / 2, 270)

  ctx.fillStyle = '#333'
  ctx.font = '18px "Microsoft YaHei", Arial'
  const donateTime = formatDateTime(new Date())
  ctx.fillText(`于 ${donateTime}`, canvas.width / 2, 320)

  const fundraisingTitle = fundraising.value?.title || '爱心募捐'
  ctx.fillText(`为 "${fundraisingTitle}" 项目`, canvas.width / 2, 360)

  ctx.fillStyle = '#ff6b6b'
  ctx.font = 'bold 36px "Microsoft YaHei", Arial'
  ctx.fillText(`捐赠人民币 ¥${lastDonation.value?.amount || 0}`, canvas.width / 2, 420)

  ctx.fillStyle = '#333'
  ctx.font = '20px "Microsoft YaHei", Arial'
  ctx.fillText('特发此证，以资鼓励', canvas.width / 2, 470)

  const issueDate = formatDateTime(new Date())
  ctx.fillStyle = '#666'
  ctx.font = '16px "Microsoft YaHei", Arial'
  ctx.fillText(`发证日期：${issueDate}`, canvas.width / 2, 530)

  ctx.fillStyle = '#667eea'
  ctx.font = 'bold 20px "Microsoft YaHei", Arial'
  ctx.fillText('流浪动物救助平台', canvas.width / 2, 560)

  certificateImage.value = canvas.toDataURL('image/png')
}

const downloadCertificate = () => {
  const link = document.createElement('a')
  link.download = `捐赠证书_${Date.now()}.png`
  link.href = certificateImage.value
  link.click()
}

const closeDonateModal = () => {
  showDonateModal.value = false
  selectedAmount.value = null
  customAmount.value = null
  donateMessage.value = ''
  isAnonymous.value = false
}

const closeCertificateModal = () => {
  showCertificateModal.value = false
  certificateImage.value = ''
  lastDonation.value = null
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

const getUsageStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已打款'
  }
  return statusMap[status] || '未知'
}

const getUsageStatusClass = (status) => {
  const classMap = {
    0: 'pending',
    1: 'approved',
    2: 'rejected',
    3: 'paid'
  }
  return classMap[status] || ''
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

const formatDateTime = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const goBack = () => {
  router.push('/fundraising')
}
</script>

<style scoped>
.fundraising-detail-container {
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
  background: transparent;
  border: 2px solid #667eea;
  cursor: pointer;
}

.nav-link:hover {
  color: #667eea;
  background: #f5f5f5;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.detail-header {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.detail-image {
  height: 100%;
  min-height: 400px;
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  padding: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-info h2 {
  margin: 0;
  color: #333;
  font-size: 28px;
}

.status-badge {
  display: inline-block;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  color: white;
  width: fit-content;
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

.description {
  color: #666;
  line-height: 1.8;
  font-size: 16px;
}

.progress-section {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.progress-bar {
  height: 15px;
  background: #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 15px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s;
}

.progress-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.stat {
  text-align: center;
}

.stat .label {
  display: block;
  font-size: 14px;
  color: #999;
  margin-bottom: 5px;
}

.stat .value {
  display: block;
  font-size: 20px;
  font-weight: bold;
  color: #667eea;
}

.time-info {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 14px;
}

.btn-donate {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 15px 40px;
  border-radius: 30px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  width: 100%;
}

.btn-donate:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-donate:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-tip {
  text-align: center;
  color: #999;
  font-size: 16px;
}

.tabs-section {
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.tabs {
  display: flex;
  border-bottom: 2px solid #f0f0f0;
}

.tab-btn {
  flex: 1;
  padding: 20px;
  background: white;
  border: none;
  font-size: 16px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 3px solid transparent;
}

.tab-btn.active {
  color: #667eea;
  border-bottom-color: #667eea;
  font-weight: 600;
}

.tab-btn:hover {
  background: #f9f9f9;
}

.tab-content {
  padding: 30px;
}

.donations-list h3,
.usage-list h3,
.my-donations-list h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 22px;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.summary-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 25px;
  border-radius: 10px;
  text-align: center;
}

.summary-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
}

.summary-label {
  font-size: 16px;
  opacity: 0.9;
}

.donation-records,
.usage-records {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.donation-item,
.usage-item {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.donor-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.donor-name {
  font-weight: 600;
  color: #333;
}

.donate-time {
  color: #999;
  font-size: 14px;
}

.donate-amount {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10px;
}

.donate-message {
  color: #666;
  font-style: italic;
  padding-top: 10px;
  border-top: 1px dashed #ddd;
}

.usage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.usage-header h4 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.usage-status {
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 13px;
  font-weight: 600;
  color: white;
}

.usage-status.pending {
  background: #faad14;
}

.usage-status.approved {
  background: #52c41a;
}

.usage-status.rejected {
  background: #ff4d4f;
}

.usage-status.paid {
  background: #1890ff;
}

.usage-amount {
  font-size: 20px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10px;
}

.usage-content {
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.usage-meta {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 13px;
  padding-top: 10px;
  border-top: 1px dashed #ddd;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

.loading {
  text-align: center;
  padding: 80px 20px;
  color: #999;
  font-size: 18px;
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
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 10px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px 25px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 22px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 32px;
  color: #999;
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 25px;
}

.amount-options {
  margin-bottom: 20px;
}

.amount-options label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.amount-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 15px;
}

.amount-btn {
  padding: 12px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s;
}

.amount-btn:hover {
  border-color: #667eea;
  background: #f5f5ff;
}

.amount-btn.selected {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.custom-amount {
  margin-top: 15px;
}

.custom-amount label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.custom-amount input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
}

.message-input {
  margin-bottom: 20px;
}

.message-input label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.message-input textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  font-family: inherit;
  resize: vertical;
}

.anonymous-option {
  padding: 10px 0;
}

.anonymous-option label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #666;
}

.anonymous-option input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.modal-footer {
  padding: 20px 25px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}

.btn-cancel,
.btn-confirm {
  padding: 12px 30px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-confirm {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.certificate-modal {
  background: white;
  border-radius: 15px;
  width: 90%;
  max-width: 900px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.certificate-header {
  padding: 25px 30px;
  border-bottom: 2px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 15px 15px 0 0;
}

.certificate-header h3 {
  margin: 0;
  font-size: 24px;
}

.certificate-body {
  padding: 30px;
  background: #fafafa;
  text-align: center;
}

.certificate-image {
  max-width: 100%;
  height: auto;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.certificate-actions {
  padding: 25px 30px;
  border-top: 2px solid #f0f0f0;
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  background: #fafafa;
  border-radius: 0 0 15px 15px;
}

.btn-download,
.btn-close {
  padding: 12px 30px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  font-weight: 600;
}

.btn-download {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-download:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-close {
  background: #f5f5f5;
  color: #666;
}

.btn-close:hover {
  background: #e0e0e0;
}

@media (max-width: 768px) {
  .detail-header {
    grid-template-columns: 1fr;
  }

  .detail-image {
    min-height: 250px;
  }

  .progress-stats {
    grid-template-columns: 1fr;
  }

  .amount-buttons {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
