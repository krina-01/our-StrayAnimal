<template>
  <div class="add-animal-container">
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
      <div class="form-wrapper">
        <h1 class="page-title">📝 发布动物信息</h1>
        <p class="page-subtitle">填写动物的基本信息，帮助它找到温暖的家</p>

        <form @submit.prevent="handleSubmit" class="animal-form">
          <div class="form-section">
            <h2 class="section-title">基本信息</h2>

            <div class="form-row">
              <div class="form-group">
                <label for="name">
                  <span class="label-icon">🐾</span>
                  动物名称 <span class="required">*</span>
                </label>
                <input
                  type="text"
                  id="name"
                  v-model="animalForm.name"
                  required
                  placeholder="请输入动物名称"
                  class="form-input"
                />
              </div>

              <div class="form-group">
                <label for="species">
                  <span class="label-icon">🐕</span>
                  动物种类 <span class="required">*</span>
                </label>
                <select
                  id="species"
                  v-model="animalForm.species"
                  required
                  class="form-input"
                >
                  <option value="">请选择种类</option>
                  <option value="狗">狗</option>
                  <option value="猫">猫</option>
                  <option value="兔子">兔子</option>
                  <option value="鸟">鸟</option>
                  <option value="其他">其他</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="age">
                  <span class="label-icon">🎂</span>
                  年龄（岁）<span class="required">*</span>
                </label>
                <input
                  type="number"
                  id="age"
                  v-model.number="animalForm.age"
                  required
                  min="0"
                  max="50"
                  placeholder="请输入年龄"
                  class="form-input"
                />
              </div>

              <div class="form-group">
                <label for="gender">
                  <span class="label-icon">⚧</span>
                  性别 <span class="required">*</span>
                </label>
                <select
                  id="gender"
                  v-model="animalForm.gender"
                  required
                  class="form-input"
                >
                  <option value="">请选择性别</option>
                  <option value="male">公</option>
                  <option value="female">母</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="healthStatus">
                  <span class="label-icon">💊</span>
                  健康状况 <span class="required">*</span>
                </label>
                <select
                  id="healthStatus"
                  v-model="animalForm.healthStatus"
                  required
                  class="form-input"
                >
                  <option value="">请选择健康状况</option>
                  <option value="健康">健康</option>
                  <option value="轻微疾病">轻微疾病</option>
                  <option value="需要治疗">需要治疗</option>
                  <option value="残疾">残疾</option>
                </select>
              </div>

              <div class="form-group">
                <label for="location">
                  <span class="label-icon">📍</span>
                  所在位置
                </label>
                <input
                  type="text"
                  id="location"
                  v-model="animalForm.location"
                  placeholder="例如：北京市朝阳区XX路"
                  class="form-input"
                />
              </div>
            </div>
          </div>

          <div class="form-section">
            <h2 class="section-title">详细信息</h2>

            <div class="form-group">
              <label for="rescueRecord">
                <span class="label-icon">📖</span>
                救助记录
              </label>
              <textarea
                id="rescueRecord"
                v-model="animalForm.rescueRecord"
                rows="4"
                placeholder="请描述动物的救助经过、性格特点等信息..."
                class="form-textarea"
              ></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" @click="handleCancel" class="btn-cancel">
              取消
            </button>
            <button type="submit" class="btn-submit" :disabled="loading">
              <span v-if="loading">提交中...</span>
              <span v-else>✓ 发布动物信息</span>
            </button>
          </div>
        </form>
      </div>
    </main>

    <footer class="footer">
      <p>&copy; 2026 流浪动物救助平台 | 用爱心温暖每一个生命</p>
    </footer>
  </div>
</template>

<script>
import { animalApi } from '@/api/index.js'

export default {
  name: 'AddAnimalView',
  data() {
    return {
      currentUser: null,
      loading: false,
      animalForm: {
        name: '',
        species: '',
        age: null,
        gender: '',
        healthStatus: '',
        location: '',
        rescueRecord: ''
      }
    }
  },
  created() {
    const user = localStorage.getItem('currentUser')
    if (user) {
      this.currentUser = JSON.parse(user)
    } else {
      this.$router.push('/login')
    }
  },
  methods: {
    // AddAnimalView.vue 中的 handleSubmit 方法
    async handleSubmit() {
      try {
        this.loading = true

        const formData = {
          ...this.animalForm,
          userId: this.currentUser.userId  // 必须传递 userId
        }

        const response = await animalApi.addAnimal(formData)

        alert('动物信息发布成功！')
        this.$router.push('/my-animals')
      } catch (error) {
        console.error('发布失败:', error)
        if (error.response && error.response.data) {
          alert('发布失败：' + (error.response.data.message || error.response.data))
        } else {
          alert('发布失败，请稍后重试')
        }
      } finally {
        this.loading = false
      }
    },
    //

    handleCancel() {
      if (confirm('确定要取消吗？已填写的内容将不会保存。')) {
        this.$router.back()
      }
    },

    handleLogout() {
      if (confirm('确定要退出登录吗？')) {
        localStorage.removeItem('currentUser')
        this.currentUser = null
        this.$router.push('/')
      }
    }
  }
}
</script>

<style scoped>
.add-animal-container {
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

.form-wrapper {
  max-width: 900px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.page-title {
  font-size: 32px;
  color: #333;
  margin: 0 0 10px 0;
  text-align: center;
}

.page-subtitle {
  text-align: center;
  color: #666;
  font-size: 16px;
  margin: 0 0 40px 0;
}

.form-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 22px;
  color: #667eea;
  margin: 0 0 25px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #555;
  font-weight: 600;
  font-size: 15px;
}

.label-icon {
  font-size: 18px;
}

.required {
  color: #f5576c;
  font-weight: bold;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: white;
  font-family: inherit;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid #f0f0f0;
}

.btn-cancel,
.btn-submit {
  padding: 14px 40px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
  transform: translateY(-2px);
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.btn-submit:active:not(:disabled) {
  transform: translateY(-1px);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.footer {
  background: white;
  padding: 20px;
  text-align: center;
  color: #666;
  margin-top: auto;
  border-top: 1px solid #eee;
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

  .form-wrapper {
    padding: 30px 20px;
  }

  .page-title {
    font-size: 26px;
  }

  .form-row {
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
