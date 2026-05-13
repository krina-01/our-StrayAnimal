<template>
  <div class="home-container">
    <header class="header">
      <div class="logo">
        <h1>🐾 流浪动物救助平台</h1>
      </div>
      <nav class="nav">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/adoption-records" class="nav-link">领养记录</router-link>
        <router-link to="/adoption-visit" class="nav-link">回访记录</router-link>
        <router-link to="/about" class="nav-link">关于我们</router-link>
        <router-link to="/volunteer-activities" class="nav-link">志愿服务</router-link>   <!-- 新增 -->
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
      <section class="hero-section">
        <div class="hero-content">
          <h2>关爱流浪动物，传递温暖爱心</h2>
          <p>每一个生命都值得被温柔以待</p>
          <div class="hero-buttons">
            <router-link to="/register" class="btn-primary" v-if="!currentUser">加入我们</router-link>
            <router-link to="/profile" class="btn-primary" v-else>查看我的信息</router-link>
            <a href="#about" class="btn-secondary">了解更多</a>
          </div>
        </div>
      </section>

      <section id="about" class="features-section">
        <h2>我们的服务</h2>
        <div class="features-grid">
          <router-link to="/adoption" class="feature-card">
            <div class="feature-icon">🏠</div>
            <h3>动物领养</h3>
            <p>为流浪动物寻找温暖的家，让爱不再流浪</p>
          </router-link>
          <router-link to="/surrender" class="feature-card" v-if="currentUser">
            <div class="feature-icon">🤲</div>
            <h3>送养发布</h3>
            <p>为无法继续饲养的宠物寻找新的温暖家庭</p>
          </router-link>
          <div class="feature-card" @click="$router.push('/volunteer-activities')">
            <div class="feature-icon">❤️</div>
            <h3>志愿服务</h3>
            <p>加入志愿者团队，用行动守护小生命</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🏥</div>
            <h3>医疗救助</h3>
            <p>提供专业的医疗救助，保障动物健康</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📢</div>
            <h3>公益宣传</h3>
            <p>传播爱护动物理念，提高公众意识</p>
          </div>
        </div>
      </section>

      <section class="stats-section">
        <div class="stat-item">
          <div class="stat-number">500+</div>
          <div class="stat-label">成功领养</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">200+</div>
          <div class="stat-label">志愿者</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">1000+</div>
          <div class="stat-label">救助动物</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">50+</div>
          <div class="stat-label">合作机构</div>
        </div>
      </section>

      <section class="cta-section">
        <h2>立即行动，改变它们的命运</h2>
        <p>无论是领养、志愿还是捐赠，您的每一份帮助都意义重大</p>
        <router-link to="/register" class="btn-primary" v-if="!currentUser">立即注册</router-link>
        <router-link to="/profile" class="btn-primary" v-else>参与救助</router-link>
      </section>
    </main>

    <footer class="footer">
      <p>&copy; 2026 流浪动物救助平台 | 用爱心温暖每一个生命</p>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'HomeView',
  data() {
    return {
      currentUser: null
    }
  },
  created() {
    const user = localStorage.getItem('currentUser')
    if (user) {
      this.currentUser = JSON.parse(user)
    }
  },
  methods: {
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
.home-container {
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
}

.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 100px 40px;
  text-align: center;
}

.hero-content h2 {
  font-size: 48px;
  margin-bottom: 20px;
}

.hero-content p {
  font-size: 24px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.hero-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.btn-primary, .btn-secondary {
  padding: 15px 40px;
  border-radius: 30px;
  text-decoration: none;
  font-size: 18px;
  transition: all 0.3s;
  display: inline-block;
}

.btn-primary {
  background: white;
  color: #667eea;
  font-weight: bold;
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.btn-secondary {
  background: transparent;
  color: white;
  border: 2px solid white;
}

.btn-secondary:hover {
  background: white;
  color: #667eea;
}

.features-section {
  padding: 80px 40px;
  background: #f9f9f9;
}

.features-section h2 {
  text-align: center;
  font-size: 36px;
  color: #333;
  margin-bottom: 50px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.feature-card {
  background: white;
  padding: 40px 30px;
  border-radius: 10px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-10px);
}

.feature-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.feature-card h3 {
  color: #333;
  margin-bottom: 15px;
  font-size: 24px;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
}

.stats-section {
  padding: 60px 40px;
  background: white;
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 48px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 18px;
  color: #666;
}

.cta-section {
  padding: 80px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
}

.cta-section h2 {
  font-size: 36px;
  margin-bottom: 20px;
}

.cta-section p {
  font-size: 20px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.footer {
  background: #333;
  color: white;
  text-align: center;
  padding: 30px;
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

  .hero-content h2 {
    font-size: 32px;
  }

  .hero-content p {
    font-size: 18px;
  }

  .hero-buttons {
    flex-direction: column;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>
