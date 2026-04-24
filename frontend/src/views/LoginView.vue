<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-icon">🐾</div>
        <h2>欢迎回来</h2>
        <p class="subtitle">登录您的账户继续</p>
      </div>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">
            <span class="label-icon">👤</span>
            用户名
          </label>
          <input
            type="text"
            id="username"
            v-model="loginForm.username"
            required
            placeholder="请输入用户名"
            class="form-input"
          />
        </div>
        <div class="form-group">
          <label for="password">
            <span class="label-icon">🔒</span>
            密码
          </label>
          <input
            type="password"
            id="password"
            v-model="loginForm.password"
            required
            placeholder="请输入密码"
            class="form-input"
          />
        </div>
        <button type="submit" class="btn-login">
          <span>立即登录</span>
          <span class="btn-arrow">→</span>
        </button>
        <div class="divider">
          <span>或</span>
        </div>
        <div class="links">
          <router-link to="/register" class="link-primary">
            <span>还没有账号？</span>
            <strong>立即注册</strong>
          </router-link>
          <router-link to="/" class="link-secondary">
            ← 返回首页
          </router-link>
        </div>
        <div v-if="errorMessage" class="error-message">
          <span class="error-icon">⚠️</span>
          {{ errorMessage }}
        </div>
      </form>
    </div>
    <div class="decoration-circles">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script>
import { userApi } from '@/api/index.js'

export default {
  name: 'LoginView',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      errorMessage: ''
    }
  },
  methods: {
    validateUsername(username) {
      if (!username || username.trim() === '') {
        return '用户名不能为空'
      }

      const hasChinese = /[\u4e00-\u9fa5]/.test(username)
      const hasEnglish = /[a-zA-Z]/.test(username)

      if (hasChinese && hasEnglish) {
        return '用户名不能中英文混杂，只能使用纯中文或纯英文'
      }

      if (hasChinese) {
        if (/^[^\u4e00-\u9fa5]+$/.test(username)) {
          return '中文用户名只能包含中文字符，不能包含数字和符号'
        }
        if (username.length < 2 || username.length > 20) {
          return '用户名长度必须在2-20个字符之间'
        }
        return ''
      }

      if (hasEnglish) {
        if (/[^a-zA-Z]/.test(username)) {
          return '英文用户名只能包含字母，不能包含数字和符号'
        }
        if (username.length < 2 || username.length > 20) {
          return '用户名长度必须在2-20个字符之间'
        }
        return ''
      }

      return '用户名只能包含中文或英文字母'
    },

    async handleLogin() {
      try {
        this.errorMessage = ''

        const validationError = this.validateUsername(this.loginForm.username)
        if (validationError) {
          this.errorMessage = validationError
          return
        }

        if (!this.loginForm.password || this.loginForm.password.trim() === '') {
          this.errorMessage = '密码不能为空'
          return
        }

        const response = await userApi.login(this.loginForm)

        localStorage.setItem('currentUser', JSON.stringify(response.data.user))

        alert('登录成功！')
        this.$router.push('/')
      } catch (error) {
        if (error.response && error.response.data && error.response.data.message) {
          this.errorMessage = error.response.data.message
        } else {
          this.errorMessage = '登录失败，请稍后重试'
        }
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.decoration-circles {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  right: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -100px;
  left: -50px;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.login-box {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 50px 40px;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 450px;
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 35px;
}

.logo-icon {
  font-size: 60px;
  margin-bottom: 15px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.login-header h2 {
  color: #333;
  font-size: 32px;
  margin: 0 0 10px 0;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  color: #666;
  font-size: 16px;
  margin: 0;
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 25px;
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

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: white;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.form-input::placeholder {
  color: #aaa;
}

.btn-login {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 10px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-login:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.btn-login:active {
  transform: translateY(-1px);
}

.btn-arrow {
  font-size: 20px;
  transition: transform 0.3s ease;
}

.btn-login:hover .btn-arrow {
  transform: translateX(5px);
}

.divider {
  text-align: center;
  margin: 25px 0;
  position: relative;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, transparent, #ddd, transparent);
}

.divider span {
  background: rgba(255, 255, 255, 0.95);
  padding: 0 15px;
  color: #999;
  font-size: 14px;
  position: relative;
}

.links {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.link-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: #666;
  text-decoration: none;
  padding: 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.link-primary:hover {
  background: #f0f1f2;
  color: #667eea;
  transform: translateX(5px);
}

.link-primary strong {
  color: #667eea;
  font-weight: 600;
}

.link-secondary {
  display: block;
  text-align: center;
  color: #999;
  text-decoration: none;
  padding: 10px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.link-secondary:hover {
  color: #667eea;
  transform: translateX(-5px);
}

.error-message {
  margin-top: 20px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  color: #c62828;
  border-radius: 10px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  animation: shake 0.5s ease-in-out;
  border-left: 4px solid #c62828;
}

.error-icon {
  font-size: 18px;
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-10px);
  }
  75% {
    transform: translateX(10px);
  }
}

@media (max-width: 768px) {
  .login-box {
    padding: 40px 30px;
  }

  .login-header h2 {
    font-size: 28px;
  }

  .logo-icon {
    font-size: 50px;
  }

  .circle-1 {
    width: 200px;
    height: 200px;
  }

  .circle-2 {
    width: 150px;
    height: 150px;
  }

  .circle-3 {
    display: none;
  }
}
</style>
