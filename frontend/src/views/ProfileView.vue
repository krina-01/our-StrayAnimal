<template>
  <div class="profile-container">
    <div class="profile-box">
      <h2>个人信息</h2>

      <div v-if="loading" class="loading">加载中...</div>

      <form v-else @submit.prevent="handleUpdate">
        <div class="info-section">
          <h3>基本信息</h3>
          <div class="form-group">
            <label>用户名：</label>
            <input type="text" v-model="userInfo.username" disabled />
          </div>
          <div class="form-group">
            <label>邮箱：</label>
            <input type="email" v-model="userInfo.email" placeholder="请输入邮箱" />
          </div>
          <div class="form-group">
            <label>手机号：</label>
            <input type="tel" v-model="userInfo.phone" placeholder="请输入手机号" />
          </div>
        </div>

        <div class="info-section">
          <h3>详细资料</h3>
          <div class="form-group">
            <label>性别：</label>
            <select v-model="userInfo.gender">
              <option value="male">男</option>
              <option value="female">女</option>
              <option value="unknown">保密</option>
            </select>
          </div>
          <div class="form-group">
            <label>出生年份：</label>
            <input type="number" v-model="userInfo.birthYear" placeholder="例如：1990" min="1900" :max="currentYear" />
          </div>
          <div class="form-group">
            <label>地址：</label>
            <input type="text" v-model="userInfo.address" placeholder="请输入地址" />
          </div>
          <div class="form-group checkbox-group">
            <label>
              <input type="checkbox" v-model="userInfo.hasFixedIncome" />
              有固定收入
            </label>
          </div>
          <div class="form-group checkbox-group">
            <label>
              <input type="checkbox" v-model="userInfo.isPetExperience" />
              有养宠经验
            </label>
          </div>
        </div>

        <div class="info-section">
          <h3>权限信息</h3>
          <div class="permission-info">
            <p>当前角色：{{ getRoleText(userInfo.role) }}</p>
            <p>志愿者：{{ userInfo.isVolunteer ? '是' : '否' }}</p>
          </div>
          <div class="apply-buttons">
            <button
              type="button"
              @click="handleApplyVolunteer"
              :disabled="userInfo.isVolunteer"
              class="btn-apply"
            >
              {{ userInfo.isVolunteer ? '已是志愿者' : '申请成为志愿者' }}
            </button>
            <button
              type="button"
              @click="handleApplyAdopter"
              :disabled="userInfo.role === 'adopter'"
              class="btn-apply"
            >
              {{ userInfo.role === 'adopter' ? '已是领养人' : '申请成为领养人' }}
            </button>
          </div>
        </div>

        <div class="button-group">
          <button type="submit" class="btn-save">保存修改</button>
          <button type="button" @click="handleDeleteAccount" class="btn-delete">注销账户</button>
          <router-link to="/" class="btn-cancel">返回首页</router-link>
        </div>

        <div v-if="message" :class="['message', messageType]">
          {{ message }}
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { userApi } from '@/api/index.js'

export default {
  name: 'ProfileView',
  data() {
    return {
      userInfo: {
        userId: null,
        username: '',
        email: '',
        phone: '',
        gender: 'unknown',
        birthYear: null,
        address: '',
        hasFixedIncome: false,
        isPetExperience: false,
        role: 'user',
        isVolunteer: false
      },
      loading: true,
      message: '',
      messageType: ''
    }
  },
  computed: {
    currentYear() {
      return new Date().getFullYear()
    }
  },
  async created() {
    await this.loadUserInfo()
  },
  methods: {
    validatePhone(phone) {
      if (!phone || phone.trim() === '') {
        return ''
      }

      const phoneRegex = /^1[3-9]\d{9}$/
      if (!phoneRegex.test(phone)) {
        return '手机号格式不正确，必须是11位数字且以1开头'
      }

      return ''
    },

    validateEmail(email) {
      if (!email || email.trim() === '') {
        return ''
      }

      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
      if (!emailRegex.test(email)) {
        return '邮箱格式不正确，例如：example@email.com'
      }

      return ''
    },

    async loadUserInfo() {
      const currentUser = localStorage.getItem('currentUser')
      if (!currentUser) {
        alert('请先登录')
        this.$router.push('/login')
        return
      }

      try {
        const user = JSON.parse(currentUser)
        const response = await userApi.getUserById(user.userId)
        this.userInfo = response.data
        this.loading = false
      } catch (error) {
        console.error('加载用户信息失败:', error)
        this.message = '加载用户信息失败'
        this.messageType = 'error'
        this.loading = false
      }
    },

    async handleUpdate() {
      try {
        this.message = ''

        const emailError = this.validateEmail(this.userInfo.email)
        if (emailError) {
          this.message = emailError
          this.messageType = 'error'
          return
        }

        const phoneError = this.validatePhone(this.userInfo.phone)
        if (phoneError) {
          this.message = phoneError
          this.messageType = 'error'
          return
        }

        if (this.userInfo.birthYear) {
          const currentYear = new Date().getFullYear()
          if (this.userInfo.birthYear < 1900 || this.userInfo.birthYear > currentYear) {
            this.message = `出生年份必须在1900到${currentYear}之间`
            this.messageType = 'error'
            return
          }
        }

        const response = await userApi.updateUser(this.userInfo.userId, this.userInfo)

        localStorage.setItem('currentUser', JSON.stringify(response.data))

        this.message = '个人信息更新成功！'
        this.messageType = 'success'
      } catch (error) {
        console.error('更新失败:', error)
        this.message = '更新失败，请稍后重试'
        this.messageType = 'error'
      }
    },

    async handleApplyVolunteer() {
      try {
        await userApi.applyVolunteer(this.userInfo.userId)
        this.message = '志愿者申请已提交，等待管理员审核'
        this.messageType = 'success'
      } catch (error) {
        this.message = '申请失败，请稍后重试'
        this.messageType = 'error'
      }
    },

    async handleApplyAdopter() {
      try {
        await userApi.applyAdopter(this.userInfo.userId)
        this.message = '领养人申请已提交，等待管理员审核'
        this.messageType = 'success'
      } catch (error) {
        this.message = '申请失败，请稍后重试'
        this.messageType = 'error'
      }
    },

    async handleDeleteAccount() {
      if (!confirm('确定要注销账户吗？此操作需要管理员审核通过后才生效。')) {
        return
      }

      try {
        await userApi.deleteUser(this.userInfo.userId)
        localStorage.removeItem('currentUser')
        alert('注销申请已提交，等待管理员审核')
        this.$router.push('/')
      } catch (error) {
        this.message = '注销申请失败，请稍后重试'
        this.messageType = 'error'
      }
    },

    getRoleText(role) {
      const roleMap = {
        'admin': '管理员',
        'adopter': '领养人',
        'user': '普通用户'
      }
      return roleMap[role] || role
    }
  }
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  padding: 40px 20px;
  background: #f5f5f5;
}

.profile-box {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

h3 {
  color: #555;
  margin-bottom: 20px;
  border-bottom: 2px solid #667eea;
  padding-bottom: 10px;
}

.info-section {
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 500;
}

input, select {
  width: 100%;
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 5px;
  font-size: 14px;
}

input:focus, select:focus {
  outline: none;
  border-color: #667eea;
}

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-group input[type="checkbox"] {
  width: auto;
  margin-right: 10px;
}

.permission-info {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 5px;
  margin-bottom: 15px;
}

.permission-info p {
  margin: 8px 0;
  color: #666;
}

.apply-buttons {
  display: flex;
  gap: 10px;
}

.btn-apply {
  flex: 1;
  padding: 10px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-apply:hover:not(:disabled) {
  background: #764ba2;
}

.btn-apply:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.button-group {
  display: flex;
  gap: 10px;
  margin-top: 30px;
}

.btn-save, .btn-delete, .btn-cancel {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  text-align: center;
  text-decoration: none;
  display: inline-block;
}

.btn-save {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-save:hover {
  transform: translateY(-2px);
}

.btn-delete {
  background: #f44336;
  color: white;
}

.btn-delete:hover {
  background: #d32f2f;
}

.btn-cancel {
  background: #e0e0e0;
  color: #333;
}

.btn-cancel:hover {
  background: #bdbdbd;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.message {
  margin-top: 20px;
  padding: 15px;
  border-radius: 5px;
  text-align: center;
}

.message.success {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.message.error {
  background-color: #ffebee;
  color: #c62828;
}
</style>
