<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { animalApi, surrenderApi } from '@/api/index.js'

const router = useRouter()
const route = useRoute()
const animal = ref<any>(null)
const currentUser = ref<any>(null)
const surrenderReason = ref('')
const loading = ref(false)
const submitting = ref(false)
const error = ref('')

onMounted(() => {
  const userStr = localStorage.getItem('currentUser')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
  } else {
    router.push('/login')
    return
  }

  const animalId = route.query.animalId
  if (animalId) {
    loadAnimal(Number(animalId))
  } else {
    error.value = '缺少动物信息'
  }
})

const loadAnimal = async (animalId: number) => {
  loading.value = true
  try {
    const response = await animalApi.getAnimalById(animalId)
    animal.value = response.data

    if (animal.value.userId !== currentUser.value.userId) {
      error.value = '无权操作此动物信息'
      animal.value = null
    }
  } catch (err) {
    console.error('加载动物信息失败:', err)
    error.value = '加载动物信息失败'
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!surrenderReason.value.trim()) {
    alert('请填写送养原因')
    return
  }

  if (surrenderReason.value.trim().length < 10) {
    alert('送养原因至少10个字符')
    return
  }

  submitting.value = true
  try {
    await surrenderApi.publishSurrender({
      userId: currentUser.value.userId,
      animalId: animal.value.animalId,
      surrenderReason: surrenderReason.value.trim()
    })

    alert('送养申请提交成功，等待审核')
    router.push('/surrender')
  } catch (err: any) {
    console.error('提交失败:', err)
    alert(err.response?.data?.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.push('/surrender')
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('currentUser')
    router.push('/')
  }
}
</script>

<template>
  <div class="surrender-edit-container">
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
      <div class="edit-card">
        <h1 class="page-title">发布送养信息</h1>

        <div v-if="loading" class="loading">加载中...</div>

        <div v-else-if="error" class="error-message">
          {{ error }}
          <button class="btn-back" @click="router.push('/surrender')">返回</button>
        </div>

        <div v-else-if="animal" class="edit-form">
          <section class="animal-info-section">
            <h2 class="section-title">动物信息</h2>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">姓名:</span>
                <span class="value">{{ animal.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">种类:</span>
                <span class="value">{{ animal.species }}</span>
              </div>
              <div class="info-item">
                <span class="label">年龄:</span>
                <span class="value">{{ animal.age }} 岁</span>
              </div>
              <div class="info-item">
                <span class="label">性别:</span>
                <span class="value">{{ animal.gender === 'male' ? '公' : animal.gender === 'female' ? '母' : animal.gender }}</span>
              </div>
              <div class="info-item">
                <span class="label">健康状况:</span>
                <span class="value">{{ animal.healthStatus }}</span>
              </div>
              <div class="info-item">
                <span class="label">位置:</span>
                <span class="value">{{ animal.location || '未填写' }}</span>
              </div>
              <div class="info-item full-width" v-if="animal.rescueRecord">
                <span class="label">救助记录:</span>
                <span class="value">{{ animal.rescueRecord }}</span>
              </div>
            </div>
          </section>

          <section class="surrender-reason-section">
            <h2 class="section-title">送养原因</h2>
            <textarea
              v-model="surrenderReason"
              placeholder="请详细说明送养原因（至少10个字符）..."
              rows="6"
              class="reason-textarea"
              maxlength="500"
            ></textarea>
            <div class="char-count">{{ surrenderReason.length }}/500</div>
          </section>

          <div class="form-actions">
            <button class="btn-cancel" @click="handleCancel" :disabled="submitting">
              取消
            </button>
            <button class="btn-submit" @click="handleSubmit" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交送养申请' }}
            </button>
          </div>
        </div>
      </div>
    </main>

    <footer class="footer">
      <p>&copy; 2026 流浪动物救助平台 | 用爱心温暖每一个生命</p>
    </footer>
  </div>
</template>

<style scoped>
.surrender-edit-container {
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
  padding: 40px 20px;
}

.edit-card {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin: 0 0 30px 0;
  text-align: center;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 16px;
}

.error-message {
  text-align: center;
  padding: 40px;
  color: #dc3545;
  font-size: 16px;
}

.btn-back {
  margin-top: 20px;
  background: #6c757d;
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.btn-back:hover {
  background: #5a6268;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.section-title {
  font-size: 20px;
  color: #555;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #eee;
}

.animal-info-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-item.full-width {
  grid-column: 1 / -1;
  flex-direction: column;
  align-items: flex-start;
}

.label {
  font-weight: 600;
  color: #333;
  min-width: 80px;
}

.value {
  color: #666;
}

.surrender-reason-section {
  display: flex;
  flex-direction: column;
}

.reason-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.3s ease;
}

.reason-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.btn-cancel,
.btn-submit {
  padding: 12px 32px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 140px;
}

.btn-cancel {
  background: #f8f9fa;
  color: #666;
  border: 1px solid #ddd;
}

.btn-cancel:hover:not(:disabled) {
  background: #e9ecef;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-cancel:disabled,
.btn-submit:disabled {
  opacity: 0.6;
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

  .edit-card {
    padding: 20px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn-cancel,
  .btn-submit {
    width: 100%;
  }
}
</style>


