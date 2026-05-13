<template>
  <div class="my-animals-container">
    <header class="header">
      <div class="logo">
        <h1>🐾 流浪动物救助平台</h1>
      </div>
      <nav class="nav">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/adoption" class="nav-link">动物领养</router-link>
        <router-link to="/about" class="nav-link">关于我们</router-link>
        <template v-if="!currentUser">
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="nav-link btn-register">注册</router-link>
        </template>
        <template v-else>
          <span class="welcome-text">欢迎，{{ currentUser.username }}</span>
          <router-link to="/my-animals" class="nav-link active">我的动物</router-link>
          <router-link to="/profile" class="nav-link">个人中心</router-link>
          <button @click="handleLogout" class="nav-link btn-logout">退出</button>
        </template>
      </nav>
    </header>

    <main class="main-content">
      <section class="page-header">
        <h2>我的动物</h2>
        <p>查看您上传的动物信息</p>
      </section>

      <section class="animals-section">
        <div v-if="loading" class="loading">
          <p>加载中... 🐾</p>
        </div>
        <div v-else-if="animals.length === 0" class="no-animals">
          <div class="empty-icon">📭</div>
          <p>您还没有上传任何动物信息</p>
          <router-link to="/surrender" class="btn-add-animal">
            <span>立即发布</span>
            <span class="arrow">→</span>
          </router-link>
        </div>
        <div v-else class="animals-grid">
          <div
            v-for="animal in animals"
            :key="animal.animalId"
            class="animal-card"
          >
            <div class="animal-info">
              <h3>{{ animal.name }}</h3>
              <div class="animal-details">
                <span class="detail-item">
                  <span class="icon">{{ getTypeIcon(animal.species) }}</span>
                  {{ animal.species }}
                </span>
                <span class="detail-item">
                  <span class="icon">⚧</span>
                  {{ getGenderText(animal.gender) }}
                </span>
                <span class="detail-item">
                  <span class="icon">📅</span>
                  {{ animal.age }}岁
                </span>
              </div>
              <p class="animal-description">{{ animal.healthStatus || '健康状况良好' }}</p>
              <p class="animal-location" v-if="animal.location">
                <span class="icon">📍</span> {{ animal.location }}
              </p>
              <div class="animal-actions">
                <button @click="viewAnimalDetail(animal)" class="btn-view">
                  查看详情
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>动物详情 - {{ selectedAnimal?.name }}</h2>
          <button @click="closeDetailModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-section">
            <div class="detail-info">
              <div class="info-row">
                <span class="info-label">🏷️ 名称：</span>
                <span class="info-value">{{ selectedAnimal?.name }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">{{ getTypeIcon(selectedAnimal?.species) }} 种类：</span>
                <span class="info-value">{{ selectedAnimal?.species }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">⚧ 性别：</span>
                <span class="info-value">{{ getGenderText(selectedAnimal?.gender) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">📅 年龄：</span>
                <span class="info-value">{{ selectedAnimal?.age }} 岁</span>
              </div>
              <div class="info-row">
                <span class="info-label">💊 健康状况：</span>
                <span class="info-value">{{ selectedAnimal?.healthStatus || '健康状况良好' }}</span>
              </div>
              <div class="info-row" v-if="selectedAnimal?.location">
                <span class="info-label">📍 位置：</span>
                <span class="info-value">{{ selectedAnimal?.location }}</span>
              </div>
              <div class="info-row" v-if="selectedAnimal?.rescueRecord">
                <span class="info-label">📝 救助记录：</span>
                <span class="info-value">{{ selectedAnimal?.rescueRecord }}</span>
              </div>
              <div class="info-row" v-if="selectedAnimal?.entryTime">
                <span class="info-label">🕒 录入时间：</span>
                <span class="info-value">{{ formatDate(selectedAnimal?.entryTime) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeDetailModal" class="btn-close">关闭</button>
        </div>
      </div>
    </div>

    <footer class="footer">
      <p>&copy; 2026 流浪动物救助平台 | 用爱心温暖每一个生命</p>
    </footer>
  </div>
</template>

<script>
import { animalApi } from '@/api'

export default {
  name: 'MyAnimalsView',
  data() {
    return {
      currentUser: null,
      animals: [],
      loading: true,
      showDetailModal: false,
      selectedAnimal: null
    }
  },
  async created() {
    const user = localStorage.getItem('currentUser')
    if (user) {
      this.currentUser = JSON.parse(user)
      await this.loadUserAnimals()
    } else {
      this.$router.push('/login')
    }
  },
  methods: {
    async loadUserAnimals() {
      try {
        this.loading = true
        const response = await animalApi.getAnimalsByUserId(this.currentUser.userId)
        this.animals = response.data
      } catch (error) {
        console.error('加载动物数据失败:', error)
        alert('加载数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    handleLogout() {
      if (confirm('确定要退出登录吗？')) {
        localStorage.removeItem('currentUser')
        this.currentUser = null
        this.$router.push('/')
      }
    },
    getStatusText(status) {
      const statusMap = {
        available: '可领养',
        pending: '审核中',
        adopted: '已领养'
      }
      return statusMap[status] || status
    },
    getGenderText(gender) {
      const genderMap = {
        male: '公',
        female: '母',
        unknown: '未知'
      }
      return genderMap[gender] || gender
    },
    getTypeIcon(species) {
      if (species && species.includes('狗')) {
        return '🐕'
      } else if (species && species.includes('猫')) {
        return '🐱'
      }
      return '🐾'
    },
    formatDate(dateString) {
      if (!dateString) return '未知'
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    viewAnimalDetail(animal) {
      this.selectedAnimal = animal
      this.showDetailModal = true
    },
    closeDetailModal() {
      this.showDetailModal = false
      this.selectedAnimal = null
    }
  }
}
</script>

<style scoped>
.my-animals-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
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
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 40px;
  text-align: center;
}

.page-header h2 {
  font-size: 42px;
  margin-bottom: 15px;
}

.page-header p {
  font-size: 20px;
  opacity: 0.9;
}

.animals-section {
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.loading,
.no-animals {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.no-animals p {
  font-size: 18px;
  margin-bottom: 30px;
}

.btn-add-animal {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-add-animal:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.arrow {
  font-size: 20px;
  transition: transform 0.3s;
}

.btn-add-animal:hover .arrow {
  transform: translateX(5px);
}

.animals-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 30px;
}

.animal-card {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.animal-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}


.status-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.status-badge.available {
  background: #4caf50;
}

.status-badge.pending {
  background: #ff9800;
}

.status-badge.adopted {
  background: #9e9e9e;
}

.animal-info {
  padding: 25px;
}

.animal-info h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 24px;
}

.animal-details {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
  font-size: 14px;
}

.detail-item .icon {
  font-size: 16px;
}

.animal-description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
  font-size: 14px;
}

.animal-location {
  color: #667eea;
  font-size: 14px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.animal-actions {
  margin-top: 15px;
}

.btn-view {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-view:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: white;
  border-radius: 15px;
  max-width: 700px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  padding: 25px 30px;
  border-bottom: 2px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
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
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  color: #333;
  background: #f5f5f5;
  border-radius: 50%;
}

.modal-body {
  padding: 30px;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 25px;
}



.status-badge-large {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  color: white;
}

.status-badge-large.available {
  background: #4caf50;
}

.status-badge-large.pending {
  background: #ff9800;
}

.status-badge-large.adopted {
  background: #9e9e9e;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  padding: 12px 15px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.info-row:hover {
  background: #f0f2ff;
  transform: translateX(5px);
}

.info-label {
  font-weight: 600;
  color: #555;
  min-width: 120px;
  font-size: 15px;
}

.info-value {
  color: #333;
  flex: 1;
  font-size: 15px;
  line-height: 1.6;
}

.modal-footer {
  padding: 20px 30px;
  border-top: 2px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
}

.btn-close {
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-close:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.footer {
  background: #333;
  color: white;
  text-align: center;
  padding: 30px;
  margin-top: auto;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }

  .nav {
    flex-wrap: wrap;
    justify-content: center;
  }

  .page-header h2 {
    font-size: 32px;
  }

  .animals-grid {
    grid-template-columns: 1fr;
  }

  .modal-content {
    margin: 10px;
  }

  .detail-image {
    height: 200px;
  }

  .placeholder-large {
    font-size: 100px;
  }

  .info-row {
    flex-direction: column;
    gap: 5px;
  }

  .info-label {
    min-width: auto;
  }
}
</style>
