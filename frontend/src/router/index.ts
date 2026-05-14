import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/my-fundraisings',
      name: 'MyFundraisings',
      component: () => import('../views/MyFundraisingsView.vue')
    },

    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('../views/AdminLoginView.vue'),
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: () => import('../views/AdminDashboardView.vue'),
    },
    // 新增：志愿活动页面
    {
      path: '/volunteer-activities',
      name: 'volunteer-activities',
      component: () => import('../views/VolunteerActivities.vue'),
    },
    {
      path: '/adoption',
      name: 'adoption',
      component: () => import('../views/adoption/AdoptionView.vue'),
    },
    {
      path: '/adoption-records',
      name: 'adoption-records',
      component: () => import('../views/adoption/AdoptionRecordsView.vue'),
    },
    {
      path: '/adoption-visit',
      name: 'adoption-visit',
      component: () => import('../views/adoption/AdoptionVisitView.vue'),
    },
    {
      path: '/surrender',
      name: 'surrender',
      component: () => import('../views/surrender/SurrenderView.vue'),
    },
    {
      path: '/surrender/edit',
      name: 'surrender-edit',
      component: () => import('../views/surrender/SurrenderEditView.vue'),
    },
    {
      path: '/my-animals',
      name: 'my-animals',
      component: () => import('../views/animal/MyAnimalsView.vue'),
    },
    {
      path: '/fundraising',
      name: 'fundraising',
      component: () => import('../views/FundraisingListView.vue'),
    },
    {
      path: '/fundraising/create',
      name: 'fundraising-create',
      component: () => import('../views/CreateFundraisingView.vue'),
    },
    {
      path: '/fundraising/:id',
      name: 'fundraising-detail',
      component: () => import('../views/FundraisingDetailView.vue'),
    },
    {
      path: '/audit-manage',
      name: 'audit-manage',
      component: () => import('../views/AuditManageView.vue'),
    },
  ],
})

export default router
