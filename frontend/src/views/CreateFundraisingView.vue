<template>
  <div class="create-fundraising-container">
    <header class="header">
      <div class="logo">
        <h1>🐾 发布募捐活动</h1>
      </div>
      <nav class="nav">
        <button @click="goBack" class="nav-link">返回</button>
      </nav>
    </header>

    <main class="main-content">
      <div class="form-container">
        <form @submit.prevent="handleSubmit" class="fundraising-form">
          <div class="form-group">
            <label for="title">活动标题 *</label>
            <input
              type="text"
              id="title"
              v-model="formData.title"
              required
              placeholder="请输入募捐活动标题"
            />
          </div>

          <div class="form-group">
            <label for="purpose">资金用途 *</label>
            <textarea
              id="purpose"
              v-model="formData.purpose"
              required
              rows="4"
              placeholder="请详细说明资金的具体用途（如：购买猫粮、医疗费用、临时安置等）"
            ></textarea>
          </div>

          <div class="form-group">
            <label for="content">活动详情 *</label>
            <textarea
              id="content"
              v-model="formData.content"
              required
              rows="8"
              placeholder="请详细描述募捐活动的目的、受益对象、活动背景等信息"
            ></textarea>
          </div>

          <div class="form-group">
            <label for="targetAmount">目标金额 (元) *</label>
            <input
              type="number"
              id="targetAmount"
              v-model.number="formData.targetAmount"
              required
              min="1"
              step="1"
              placeholder="请输入目标金额"
            />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="startTime">开始时间 *</label>
              <input
                type="datetime-local"
                id="startTime"
                v-model="formData.startTime"
                required
              />
            </div>

            <div class="form-group">
              <label for="endTime">结束时间 *</label>
              <input
                type="datetime-local"
                id="endTime"
                v-model="formData.endTime"
                required
              />
            </div>
          </div>

          <div class="form-group">
            <label for="activityImg">活动图片URL</label>
            <input
              type="url"
              id="activityImg"
              v-model="formData.activityImg"
              placeholder="请输入活动图片链接（可选）"
            />
          </div>

          <div class="form-actions">
            <button type="button" @click="goBack" class="btn-cancel">取消</button>
            <button type="submit" class="btn-submit">提交审核</button>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fundraisingApi } from '../api'

const router = useRouter()

const formData = ref({
  title: '',
  content: '',
  purpose: '',
  targetAmount: null,
  startTime: '',
  endTime: '',
  activityImg: '',
  creatorId: null
})

onMounted(() => {
  const user = localStorage.getItem('currentUser')
  if (!user) {
    alert('请先登录')
    router.push('/login')
    return
  }
  const userData = JSON.parse(user)
  formData.value.creatorId = userData.userId
})

const handleSubmit = async () => {
  if (new Date(formData.value.endTime) <= new Date(formData.value.startTime)) {
    alert('结束时间必须晚于开始时间')
    return
  }

  try {
    const response = await fundraisingApi.createFundraising(formData.value)
    alert('募捐活动已提交，等待管理员审核')
    router.push('/fundraising')
  } catch (error) {
    console.error('提交失败:', error)
    alert('提交失败，请稍后重试')
  }
}

const goBack = () => {
  router.push('/fundraising')
}
</script>

<style scoped>
.create-fundraising-container {
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
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}

.form-container {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.fundraising-form {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.form-group input,
.form-group textarea {
  padding: 12px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-cancel,
.btn-submit {
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

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .form-container {
    padding: 25px;
  }
}
</style>
