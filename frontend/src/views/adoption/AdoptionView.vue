<template>
  <div class="adoption-container">
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
          <router-link to="/adoption" class="nav-link active">动物领养</router-link>
          <router-link to="/profile" class="nav-link">个人中心</router-link>
          <button @click="handleLogout" class="nav-link btn-logout">退出</button>
        </template>
      </nav>
    </header>

    <main class="main-content">
      <section class="page-header">
        <h2>寻找你的小伙伴</h2>
        <p>给流浪的它们一个温暖的家</p>
      </section>

      <section class="filter-section">
        <div class="filter-container">
          <div class="filter-item">
            <label>动物类型：</label>
            <select v-model="filters.species" @change="filterAnimals">
              <option value="">全部</option>
              <option value="狗">狗狗</option>
              <option value="猫">猫咪</option>
              <option value="其他">其他</option>
            </select>
          </div>
          <div class="filter-item">
            <label>年龄：</label>
            <select v-model="filters.age" @change="filterAnimals">
              <option value="">全部</option>
              <option value="young">幼年 (0-1岁)</option>
              <option value="adult">成年 (1-7岁)</option>
              <option value="senior">老年 (7岁以上)</option>
            </select>
          </div>
          <div class="filter-item">
            <label>性别：</label>
            <select v-model="filters.gender" @change="filterAnimals">
              <option value="">全部</option>
              <option value="male">公</option>
              <option value="female">母</option>
              <option value="unknown">未知</option>
            </select>
          </div>
          <div class="filter-item">
            <label>搜索：</label>
            <input
              type="text"
              v-model="filters.search"
              @input="filterAnimals"
              placeholder="输入名字或特征..."
            />
          </div>
        </div>
      </section>

      <section class="animals-section">
        <div v-if="loading" class="loading">
          <p>加载中... 🐾</p>
        </div>
        <div v-else-if="filteredAnimals.length === 0" class="no-animals">
          <p>暂无符合条件的动物，请稍后再试 🐾</p>
        </div>
        <div v-else class="animals-grid">
          <div
            v-for="animal in filteredAnimals"
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
              <button
                @click="openAdoptionModal(animal)"
                class="btn-adopt"
                :class="{ 'btn-applied': isAnimalApplied(animal.animalId) }"
                :disabled="animal.adoptStatus !== 'available' || isAnimalApplied(animal.animalId)"
              >
                {{ getButtonText(animal) }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <section class="adoption-process">
        <h2>领养流程</h2>
        <div class="process-steps">
          <div class="step">
            <div class="step-number">1</div>
            <h3>浏览选择</h3>
            <p>浏览待领养动物，找到心仪的小伙伴</p>
          </div>
          <div class="step">
            <div class="step-number">2</div>
            <h3>提交申请</h3>
            <p>填写领养申请表，说明您的情况</p>
          </div>
          <div class="step">
            <div class="step-number">3</div>
            <h3>审核评估</h3>
            <p>工作人员审核申请并进行评估</p>
          </div>
          <div class="step">
            <div class="step-number">4</div>
            <h3>见面互动</h3>
            <p>安排与动物的见面，确认适配性</p>
          </div>
          <div class="step">
            <div class="step-number">5</div>
            <h3>完成领养</h3>
            <p>签署协议，带新家人回家</p>
          </div>
        </div>
      </section>
    </main>

    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>申请领养 - {{ selectedAnimal?.name }}</h2>
          <button @click="closeModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitAdoptionApplication">
            <div class="form-group">
              <label for="name">姓名 *</label>
              <input
                type="text"
                id="name"
                v-model="applicationForm.name"
                required
                placeholder="请输入您的姓名"
              />
            </div>
            <div class="form-group">
              <label for="phone">联系电话 *</label>
              <input
                type="tel"
                id="phone"
                v-model="applicationForm.phone"
                required
                placeholder="请输入您的手机号"
              />
            </div>
            <div class="form-group">
              <label for="email">电子邮箱</label>
              <input
                type="email"
                id="email"
                v-model="applicationForm.email"
                placeholder="请输入您的邮箱"
              />
            </div>
            <div class="form-group">
              <label for="address">居住地址 *</label>
              <textarea
                id="address"
                v-model="applicationForm.address"
                required
                placeholder="请输入您的详细居住地址"
                rows="3"
              ></textarea>
            </div>
            <div class="form-group">
              <label for="experience">养宠经验</label>
              <textarea
                id="experience"
                v-model="applicationForm.experience"
                placeholder="请描述您是否有养宠经验"
                rows="3"
              ></textarea>
            </div>
            <div class="form-group">
              <label for="reason">领养理由 *</label>
              <textarea
                id="reason"
                v-model="applicationForm.reason"
                required
                placeholder="请说明您想领养的原因"
                rows="4"
              ></textarea>
            </div>
            <div class="form-group checkbox-group">
              <label>
                <input type="checkbox" v-model="applicationForm.agreement" required />
                我已阅读并同意《领养协议》，承诺科学喂养，不离不弃 *
              </label>
            </div>
            <div class="modal-actions">
              <button type="button" @click="closeModal" class="btn-cancel">取消</button>
              <button type="submit" class="btn-submit" :disabled="submitting">
                {{ submitting ? '提交中...' : '提交申请' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <footer class="footer">
      <p>&copy; 2026 流浪动物救助平台 | 用爱心温暖每一个生命</p>
    </footer>
  </div>
</template>

<script>
import { animalApi, adoptionApplicationApi, surrenderApi } from '@/api'

export default {
  name: 'AdoptionView',
  data() {
    return {
      currentUser: null,
      animals: [],
      filteredAnimals: [],
      filters: {
        species: '',
        age: '',
        gender: '',
        search: ''
      },
      showModal: false,
      selectedAnimal: null,
      selectedSurrender: null,
      applicationForm: {
        name: '',
        phone: '',
        email: '',
        address: '',
        experience: '',
        reason: '',
        agreement: false
      },
      submitting: false,
      loading: true,
      userApplications: [],
      animalSurrenderMap: {}
    }
  },
  async created() {
    const user = localStorage.getItem('currentUser')
    if (user) {
      this.currentUser = JSON.parse(user)
      await this.loadUserApplications()
    }
    await this.loadAnimals()
  },
  methods: {
    async loadAnimals() {
      try {
        this.loading = true

        if (this.currentUser) {
          const response = await animalApi.getAvailableAnimalsExcludingUser(this.currentUser.userId)
          this.animals = response.data
        } else {
          const response = await animalApi.getAvailableAnimals()
          this.animals = response.data
        }

        this.filteredAnimals = [...this.animals]

        await this.loadSurrenderInfo()
      } catch (error) {
        console.error('加载动物数据失败:', error)
        alert('加载数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    async loadSurrenderInfo() {
      for (const animal of this.animals) {
        try {
          const response = await surrenderApi.getSurrenderByAnimalId(animal.animalId)
          if (response.data && response.data.length > 0) {
            this.animalSurrenderMap[animal.animalId] = response.data[0]
          }
        } catch (error) {
          console.error(`加载动物 ${animal.animalId} 的送养信息失败:`, error)
        }
      }
    },

    async loadUserApplications() {
      if (!this.currentUser) return

      try {
        const response = await adoptionApplicationApi.getApplicationsByUserId(this.currentUser.userId)
        this.userApplications = response.data || []
      } catch (error) {
        console.error('加载用户申请记录失败:', error)
        this.userApplications = []
      }
    },

    getUserApplicationForAnimal(animalId) {
      const surrender = this.animalSurrenderMap[animalId]
      if (!surrender) return null

      return this.userApplications.find(app =>
        app.surrenderId === surrender.surrenderId
      )
    },

    isAnimalApplied(animalId) {
      const application = this.getUserApplicationForAnimal(animalId)
      return application && application.auditStatus !== 'rejected'
    },

    getButtonText(animal) {
      if (animal.adoptStatus !== 'available') {
        return '暂不可领养'
      }

      if (!this.currentUser) {
        return '申请领养'
      }

      const application = this.getUserApplicationForAnimal(animal.animalId)

      if (!application) {
        return '申请领养'
      }

      const statusMap = {
        'pending': '已申请，待审核',
        'approved': '已通过',
        'rejected': '已拒绝'
      }

      return statusMap[application.auditStatus] || '已申请'
    },

    handleLogout() {
      if (confirm('确定要退出登录吗？')) {
        localStorage.removeItem('currentUser')
        this.currentUser = null
        this.userApplications = []
        this.$router.push('/')
      }
    },

    filterAnimals() {
      this.filteredAnimals = this.animals.filter(animal => {
        let match = true

        if (this.filters.species && animal.species !== this.filters.species) {
          match = false
        }

        if (this.filters.gender && animal.gender !== this.filters.gender) {
          match = false
        }

        if (this.filters.age) {
          if (this.filters.age === 'young' && animal.age >= 1) {
            match = false
          } else if (this.filters.age === 'adult' && (animal.age < 1 || animal.age >= 7)) {
            match = false
          } else if (this.filters.age === 'senior' && animal.age < 7) {
            match = false
          }
        }

        if (this.filters.search) {
          const searchLower = this.filters.search.toLowerCase()
          const nameMatch = animal.name && animal.name.toLowerCase().includes(searchLower)
          const healthMatch = animal.healthStatus && animal.healthStatus.toLowerCase().includes(searchLower)
          const locationMatch = animal.location && animal.location.toLowerCase().includes(searchLower)
          if (!nameMatch && !healthMatch && !locationMatch) {
            match = false
          }
        }

        return match
      })
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

    async openAdoptionModal(animal) {
      if (!this.currentUser) {
        alert('请先登录后再申请领养')
        this.$router.push('/login')
        return
      }

      if (this.isAnimalApplied(animal.animalId)) {
        const application = this.getUserApplicationForAnimal(animal.animalId)
        const statusMap = {
          'pending': '待审核',
          'approved': '已通过',
          'rejected': '已拒绝'
        }
        alert(`您已经提交过该动物的领养申请\n当前状态：${statusMap[application.auditStatus]}\n\n请等待审核或前往"我的领养申请"页面查看`)
        return
      }

      this.selectedAnimal = animal
      this.selectedSurrender = this.animalSurrenderMap[animal.animalId]

      if (!this.selectedSurrender) {
        alert('该动物暂无送养信息')
        return
      }

      this.applicationForm = {
        name: this.currentUser.username || '',
        phone: this.currentUser.phone || '',
        email: this.currentUser.email || '',
        address: this.currentUser.address || '',
        experience: '',
        reason: '',
        agreement: false
      }
      this.showModal = true
    },

    closeModal() {
      this.showModal = false
      this.selectedAnimal = null
      this.selectedSurrender = null
      this.applicationForm = {
        name: '',
        phone: '',
        email: '',
        address: '',
        experience: '',
        reason: '',
        agreement: false
      }
    },


    async submitAdoptionApplication() {
      if (!this.applicationForm.agreement) {
        alert('请同意领养协议')
        return
      }

      this.submitting = true

      try {
        const applicationData = {
          userId: this.currentUser.userId,
          surrenderId: this.selectedSurrender.surrenderId,
          adoptionReason: this.applicationForm.reason,
          applicationTime: new Date().toISOString(),
          auditStatus: 'pending'
        }

        console.log('=== 准备提交领养申请 ===')
        console.log('请求数据:', JSON.stringify(applicationData, null, 2))

        const response = await adoptionApplicationApi.createApplication(applicationData)

        console.log('=== 提交成功 ===')
        console.log('响应数据:', response.data)

        alert('领养申请提交成功！我们会尽快审核您的申请。')

        await this.loadUserApplications()

        this.closeModal()
      } catch (error) {
        console.error('=== 提交失败 ===')
        console.error('错误对象:', error)
        console.error('错误响应:', error.response)
        console.error('错误状态码:', error.response?.status)
        console.error('错误信息:', error.response?.data)

        if (error.response && error.response.data && error.response.data.message) {
          alert('提交失败: ' + error.response.data.message)
        } else {
          alert('提交失败，请稍后重试')
        }
      } finally {
        this.submitting = false
      }
    }

  }
}
</script>

<style scoped>
.adoption-container {
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

.filter-section {
  background: white;
  padding: 30px 40px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.filter-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-item label {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.filter-item select,
.filter-item input {
  padding: 10px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
}

.filter-item select:focus,
.filter-item input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
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
  padding: 60px 20px;
  color: #999;
  font-size: 18px;
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

.btn-adopt {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 15px;
}

.btn-adopt:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-adopt:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-adopt.btn-applied {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.btn-adopt.btn-applied:hover {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.adoption-process {
  padding: 60px 40px;
  background: white;
  margin-top: 40px;
}

.adoption-process h2 {
  text-align: center;
  font-size: 36px;
  color: #333;
  margin-bottom: 50px;
}

.process-steps {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 30px;
}

.step {
  text-align: center;
  padding: 30px 20px;
  position: relative;
}

.step-number {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  margin: 0 auto 20px;
}

.step h3 {
  color: #333;
  margin-bottom: 10px;
  font-size: 20px;
}

.step p {
  color: #666;
  line-height: 1.6;
  font-size: 14px;
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
}

.modal-content {
  background: white;
  border-radius: 15px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
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

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.checkbox-group label {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  cursor: pointer;
  font-weight: normal;
}

.checkbox-group input[type="checkbox"] {
  width: auto;
  margin-top: 3px;
}

.modal-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 30px;
}

.btn-cancel,
.btn-submit {
  padding: 12px 30px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
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

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  background: #ccc;
  cursor: not-allowed;
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

  .filter-container {
    grid-template-columns: 1fr;
  }

  .animals-grid {
    grid-template-columns: 1fr;
  }

  .process-steps {
    grid-template-columns: 1fr;
  }

  .modal-content {
    margin: 10px;
  }

  .modal-actions {
    flex-direction: column;
  }

  .btn-cancel,
  .btn-submit {
    width: 100%;
  }
}
</style>
