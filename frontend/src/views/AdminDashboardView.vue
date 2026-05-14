<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1>👑 管理员控制面板</h1>
      <div class="header-actions">
        <span class="admin-info">欢迎，{{ adminUser?.username }}</span>
        <button @click="handleLogout" class="btn-logout">退出登录</button>
      </div>
    </div>

    <!-- 数据统计面板 -->
    <div class="statistics-panel">
      <div class="stats-header">
        <h2>数据统计概览</h2>
        <button @click="downloadAllCharts" class="btn-download-all">
          📥 下载所有图表
        </button>
      </div>

      <!-- 基础数据统计 -->
      <div class="stats-section">
        <h3>基础数据</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">🐾</div>
            <div class="stat-content">
              <div class="stat-value">{{ basicStats.totalAnimals || 0 }}</div>
              <div class="stat-label">动物总数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">👥</div>
            <div class="stat-content">
              <div class="stat-value">{{ basicStats.totalUsers || 0 }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">🏠</div>
            <div class="stat-content">
              <div class="stat-value">{{ basicStats.totalRescueStations || 0 }}</div>
              <div class="stat-label">救助站总数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">📝</div>
            <div class="stat-content">
              <div class="stat-value">{{ basicStats.totalPublications || 0 }}</div>
              <div class="stat-label">信息发布总数</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 救助与领养统计 -->
      <div class="stats-section">
        <h3>救助与领养</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">💝</div>
            <div class="stat-content">
              <div class="stat-value">{{ adoptionStats.rescueCount || 0 }}</div>
              <div class="stat-label">救助数量</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">📋</div>
            <div class="stat-content">
              <div class="stat-value">{{ adoptionStats.adoptionApplicationCount || 0 }}</div>
              <div class="stat-label">领养申请量</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">✅</div>
            <div class="stat-content">
              <div class="stat-value">{{ adoptionStats.successfulAdoptions || 0 }}</div>
              <div class="stat-label">领养成功</div>
            </div>
          </div>
          <div class="stat-card highlight">
            <div class="stat-icon">📈</div>
            <div class="stat-content">
              <div class="stat-value">{{ adoptionStats.successRate || '0%' }}</div>
              <div class="stat-label">领养成功率</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 募捐与财务统计 -->
      <div class="stats-section">
        <h3>募捐与财务</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">💰</div>
            <div class="stat-content">
              <div class="stat-value">¥{{ fundraisingStats.totalAmount || 0 }}</div>
              <div class="stat-label">募捐总金额</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">🎯</div>
            <div class="stat-content">
              <div class="stat-value">{{ fundraisingStats.totalFundraisings || 0 }}</div>
              <div class="stat-label">募捐项目数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">❤️</div>
            <div class="stat-content">
              <div class="stat-value">{{ fundraisingStats.donationCount || 0 }}</div>
              <div class="stat-label">捐款人次</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">📊</div>
            <div class="stat-content">
              <div class="stat-value">¥{{ fundraisingStats.usedAmount || 0 }}</div>
              <div class="stat-label">资金使用</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-section" ref="chartsContainer">
        <!-- 宠物种类分布 - 扇形图 -->
        <div class="chart-card" ref="speciesChartRef">
          <div class="chart-header">
            <h3>宠物种类分布</h3>
            <button @click="downloadChart('species')" class="btn-download">
              📥 下载
            </button>
          </div>
          <div ref="speciesChartDom" class="chart-container"></div>
        </div>

        <!-- 动物状态分布 - 直方图 -->
        <div class="chart-card" ref="statusChartRef">
          <div class="chart-header">
            <h3>动物状态分布</h3>
            <button @click="downloadChart('status')" class="btn-download">
              📥 下载
            </button>
          </div>
          <div ref="statusChartDom" class="chart-container"></div>
        </div>

        <!-- 区域分布 - 柱状图 -->
        <div class="chart-card" ref="locationChartRef">
          <div class="chart-header">
            <h3>区域分布</h3>
            <button @click="downloadChart('location')" class="btn-download">
              📥 下载
            </button>
          </div>
          <div ref="locationChartDom" class="chart-container"></div>
        </div>

        <!-- 募捐趋势 - 折线图 -->
        <div class="chart-card full-width" ref="fundraisingTrendRef">
          <div class="chart-header">
            <h3>募捐项目状态分布</h3>
            <button @click="downloadChart('fundraising')" class="btn-download">
              📥 下载
            </button>
          </div>
          <div ref="fundraisingTrendDom" class="chart-container"></div>
        </div>

        <!-- 综合统计对比 - 组合图 -->
        <div class="chart-card full-width" ref="comparisonChartRef">
          <div class="chart-header">
            <h3>审核状态统计</h3>
            <button @click="downloadChart('comparison')" class="btn-download">
              📥 下载
            </button>
          </div>
          <div ref="comparisonChartDom" class="chart-container"></div>
        </div>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="dropdown-menu">
        <div class="dropdown-item">
          <div
            class="dropdown-header"
            :class="{ 'active-dropdown': openDropdown === 'audit' }"
            @click="toggleDropdown('audit')"
          >
            <span>审核管理</span>
            <span class="dropdown-arrow" :class="{ 'arrow-up': openDropdown === 'audit' }">▼</span>
          </div>
          <div v-if="openDropdown === 'audit'" class="dropdown-content">
            <button
              :class="['dropdown-btn', { active: activeTab === 'pending' }]"
              @click="activeTab = 'pending'; openDropdown = null"
            >
              用户注册审核 ({{ pendingUsers.length }})
            </button>
            <button
              :class="['dropdown-btn', { active: activeTab === 'volunteerApps' }]"
              @click="activeTab = 'volunteerApps'; openDropdown = null"
            >
              志愿者申请审核 ({{ volunteerApplications.length }})
            </button>
            <button
              :class="['dropdown-btn', { active: activeTab === 'surrenderAudit' }]"
              @click="activeTab = 'surrenderAudit'; openDropdown = null"
            >
              送养审核 ({{ pendingSurrenders.length }})
            </button>
            <button
              :class="['dropdown-btn', { active: activeTab === 'adoptionAudit' }]"
              @click="activeTab = 'adoptionAudit'; openDropdown = null"
            >
              领养审核 ({{ pendingAdoptions.length }})
            </button>
            <button
              :class="['dropdown-btn', { active: activeTab === 'registrationApps' }]"
              @click="activeTab = 'registrationApps'; openDropdown = null"
            >
              活动报名审核 ({{ pendingRegistrations.length }})
            </button>
            <button
              :class="['dropdown-btn', { active: activeTab === 'fundraising' }]"
              @click="activeTab = 'fundraising'; openDropdown = null"
            >
              募捐审核 ({{ pendingFundraising.length }})
            </button>
          </div>
        </div>

        <button
          :class="['tab-btn', { active: activeTab === 'all' }]"
          @click="activeTab = 'all'"
        >
          用户管理
        </button>

        <button
          :class="['tab-btn', { active: activeTab === 'activityManage' }]"
          @click="activeTab = 'activityManage'"
        >
          志愿活动管理
        </button>
      </div>

      <!-- 1. 用户注册审核 -->
      <div v-if="activeTab === 'pending'" class="tab-content">
        <h2>待审核用户注册</h2>
        <div v-if="pendingUsers.length === 0" class="empty-state">
          <p>暂无待审核用户</p>
        </div>
        <div v-else class="user-list">
          <div v-for="user in pendingUsers" :key="user.userId" class="user-card">
            <div class="user-info">
              <h3>{{ user.username }}</h3>
              <p><strong>邮箱：</strong>{{ user.email || '未填写' }}</p>
              <p><strong>电话：</strong>{{ user.phone || '未填写' }}</p>
              <p><strong>注册时间：</strong>{{ formatDate(user.registerTime) }}</p>
            </div>
            <div class="user-actions">
              <button @click="approveUser(user)" class="btn-approve">✓ 通过</button>
              <button @click="rejectUser(user)" class="btn-reject">✗ 拒绝</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. 志愿者申请审核 -->
      <div v-if="activeTab === 'volunteerApps'" class="tab-content">
        <h2>待审核志愿者申请</h2>
        <div v-if="volunteerApplications.length === 0" class="empty-state">
          <p>暂无志愿者申请</p>
        </div>
        <div v-else class="user-list">
          <div v-for="user in volunteerApplications" :key="user.userId" class="user-card">
            <div class="user-info">
              <h3>{{ user.username }}</h3>
              <p><strong>邮箱：</strong>{{ user.email || '未填写' }}</p>
              <p><strong>电话：</strong>{{ user.phone || '未填写' }}</p>
              <p><strong>申请状态：</strong>{{ getVolunteerApplyStatusText(user.volunteerApplyStatus) }}</p>
            </div>
            <div class="user-actions">
              <button @click="approveVolunteerApp(user)" class="btn-approve">✓ 通过</button>
              <button @click="rejectVolunteerApp(user)" class="btn-reject">✗ 拒绝</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. 送养审核 -->
      <div v-if="activeTab === 'surrenderAudit'" class="tab-content">
        <h2>待审核送养信息申请</h2>
        <div v-if="!pendingSurrenders || pendingSurrenders.length === 0" class="empty-state">
          <p>暂无待审核送养信息申请</p>
        </div>
        <div v-else class="user-list">
          <div v-for="item in pendingSurrenders" :key="item.surrenderId" class="user-card">
            <div class="user-info">
              <h3>送养申请 #{{ item.surrenderId }}</h3>
              <p><strong>送养人：</strong>{{ item.user?.username || 'ID:' + item.userId }}</p>
              <p><strong>联系电话：</strong>{{ item.user?.phone || '未填写' }}</p>
              <p><strong>动物名称：</strong>{{ item.animal?.name || 'ID:' + item.animalId }}</p>
              <p v-if="item.animal"><strong>动物信息：</strong>{{ item.animal.species }} / {{ item.animal.gender }} / {{ item.animal.age }}岁</p>
              <p v-if="item.animal && item.animal.healthStatus"><strong>健康状况：</strong>{{ item.animal.healthStatus }}</p>
              <p><strong>送养原因：</strong>{{ item.surrenderReason }}</p>
              <p><strong>提交时间：</strong>{{ formatDate(item.submitTime) }}</p>
            </div>
            <div class="user-actions">
              <button @click="approveSurrender(item)" class="btn-approve">✓ 通过</button>
              <button @click="rejectSurrender(item)" class="btn-reject">✗ 拒绝</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 4. 领养审核 -->
      <div v-if="activeTab === 'adoptionAudit'" class="tab-content">
        <h2>待审核领养申请</h2>
        <div v-if="!pendingAdoptions || pendingAdoptions.length === 0" class="empty-state">
          <p>暂无待审核领养申请</p>
        </div>
        <div v-else class="user-list">
          <div v-for="item in pendingAdoptions" :key="item.applicationId" class="user-card">
            <div class="user-info">
              <h3>领养申请 #{{ item.applicationId }}</h3>
              <p><strong>申请人：</strong>{{ item.user?.username || 'ID:' + item.userId }}</p>
              <p><strong>联系电话：</strong>{{ item.user?.phone || '未填写' }}</p>
              <p v-if="item.surrenderInfo"><strong>动物名称：</strong>{{ item.surrenderInfo.animal?.name || 'ID:' + item.surrenderInfo.animalId }}</p>
              <p v-if="item.surrenderInfo && item.surrenderInfo.animal"><strong>动物信息：</strong>{{ item.surrenderInfo.animal.species }} / {{ item.surrenderInfo.animal.gender }} / {{ item.surrenderInfo.animal.age }}岁</p>
              <p v-if="item.surrenderInfo && item.surrenderInfo.animal && item.surrenderInfo.animal.healthStatus"><strong>健康状况：</strong>{{ item.surrenderInfo.animal.healthStatus }}</p>
              <p><strong>领养原因：</strong>{{ item.adoptionReason }}</p>
              <p><strong>申请时间：</strong>{{ formatDate(item.applicationTime) }}</p>
            </div>
            <div class="user-actions">
              <button @click="approveAdoption(item)" class="btn-approve">✓ 通过</button>
              <button @click="rejectAdoption(item)" class="btn-reject">✗ 拒绝</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 5. 报名审核 -->
      <div v-if="activeTab === 'registrationApps'" class="tab-content">
        <h2>待审核的活动报名</h2>
        <div v-if="pendingRegistrations.length === 0" class="empty-state">
          <p>暂无待审核报名</p>
        </div>
        <div v-else class="user-list">
          <div v-for="reg in pendingRegistrations" :key="reg.activity_id + '-' + reg.user_id" class="user-card">
            <div class="user-info">
              <h3>{{ reg.activity_name }}</h3>
              <p><strong>报名者：</strong>{{ reg.username }}（{{ reg.phone || '未填写电话' }}）</p>
              <p><strong>报名时间：</strong>{{ formatDate(reg.apply_time) }}</p>
              <p><strong>活动时间：</strong>{{ formatDateTime(reg.start_time) }} ~ {{ formatDateTime(reg.end_time) }}</p>
              <p><strong>活动地点：</strong>{{ reg.location }}</p>
            </div>
            <div class="user-actions">
              <button @click="approveRegistration(reg)" class="btn-approve">✓ 通过</button>
              <button @click="rejectRegistration(reg)" class="btn-reject">✗ 拒绝</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 6. 所有用户管理 -->
      <div v-if="activeTab === 'all'" class="tab-content">
        <h2>用户管理与权限分配</h2>
        <div v-if="loadingUsers" class="loading-state">
          <p>加载中...</p>
        </div>
        <div v-else-if="allUsers.length === 0" class="empty-state">
          <p>暂无用户数据</p>
        </div>
        <div v-else class="user-table-container">
          <table class="user-table">
            <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>邮箱</th>
              <th>角色</th>
              <th>志愿者</th>
              <th>申请状态</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="user in allUsers" :key="user.userId">
              <td>{{ user.userId }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.email || '-' }}</td>
              <td>
                <span :class="['role-badge', user.role]">{{ user.role }}</span>
              </td>
              <td>
                <span :class="['volunteer-badge', user.isVolunteer ? 'yes' : 'no']">
                  {{ user.isVolunteer ? '是' : '否' }}
                </span>
              </td>
              <td>
                <span :class="['status-badge', user.volunteerApplyStatus || 'none']">
                  {{ getVolunteerApplyStatusText(user.volunteerApplyStatus) }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <select
                    v-if="user.role !== 'admin'"
                    @change="assignRole(user, $event.target.value)"
                    class="role-select"
                  >
                    <option value="">分配角色</option>
                    <option value="admin">管理员</option>
                    <option value="volunteer">志愿者</option>
                    <option value="adopter">领养人</option>
                  </select>
                  <button
                    v-if="user.isVolunteer"
                    @click="removeVolunteer(user)"
                    class="btn-remove-volunteer"
                  >
                    取消志愿者
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 7. 活动管理 -->
      <div v-if="activeTab === 'activityManage'" class="tab-content">
        <h2>志愿活动管理</h2>
        <button @click="openCreateActivityForm" class="btn-add">+ 新建活动</button>

        <!-- 新增/编辑活动表单 -->
        <div v-if="showActivityForm" class="activity-form">
          <h3>{{ editingActivity ? '编辑活动' : '新建活动' }}</h3>
          <div class="form-row">
            <label>活动名称：</label>
            <input v-model="activityForm.activityName" placeholder="请输入活动名称" />
          </div>
          <div class="form-row">
            <label>开始时间：</label>
            <input type="datetime-local" v-model="activityForm.startTime" />
          </div>
          <div class="form-row">
            <label>结束时间：</label>
            <input type="datetime-local" v-model="activityForm.endTime" />
          </div>
          <div class="form-row">
            <label>地点：</label>
            <input v-model="activityForm.location" placeholder="活动地点" />
          </div>
          <div class="form-row">
            <label>描述：</label>
            <textarea v-model="activityForm.description" rows="3" placeholder="活动详情"></textarea>
          </div>
          <div class="form-row">
            <label>招募状态：</label>
            <select v-model="activityForm.recruitStatus">
              <option value="recruiting">招募中</option>
              <option value="closed">已关闭</option>
            </select>
          </div>
          <div class="form-buttons">
            <button @click="saveActivity" class="btn-save">保存</button>
            <button @click="cancelActivityForm" class="btn-cancel">取消</button>
          </div>
        </div>

        <!-- 活动列表 -->
        <div v-if="activities.length === 0 && !showActivityForm" class="empty-state">
          <p>暂无活动数据</p>
          <button @click="openCreateActivityForm" class="btn-create-first">创建第一个活动</button>
        </div>
        <div v-else-if="!showActivityForm" class="activity-list">
          <div v-for="act in activities" :key="act.activityId" class="activity-card">
            <div class="activity-info">
              <h3>{{ act.activityName }}</h3>
              <p><strong>时间：</strong>{{ formatDateTime(act.startTime) }} ~ {{ formatDateTime(act.endTime) }}</p>
              <p><strong>地点：</strong>{{ act.location }}</p>
              <p><strong>描述：</strong>{{ act.description }}</p>
              <p><strong>招募状态：</strong>
                <span :class="act.recruitStatus === 'recruiting' ? 'status-recruiting' : 'status-closed'">
                  {{ act.recruitStatus === 'recruiting' ? '招募中' : '已关闭' }}
                </span>
              </p>
            </div>
            <div class="activity-actions">
              <button @click="editActivity(act)" class="btn-edit">编辑</button>
              <button @click="deleteActivity(act)" class="btn-delete">删除</button>
              <button @click="toggleRecruitStatus(act)" class="btn-toggle">
                {{ act.recruitStatus === 'recruiting' ? '关闭招募' : '开启招募' }}
              </button>
              <button @click="calcDuration(act.activityId)" class="btn-calc">计算服务时长</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 募捐管理 -->
      <div v-if="activeTab === 'fundraising'" class="tab-content">
        <h2>募捐活动管理</h2>

        <!-- 待审核募捐 -->
        <h3>待审核募捐</h3>
        <div v-if="pendingFundraising.length === 0" class="empty-state">
          <p>暂无待审核募捐活动</p>
        </div>
        <div v-else class="fundraising-list">
          <div v-for="item in pendingFundraising" :key="item.fundraisingId" class="fundraising-card">
            <div class="fundraising-info">
              <h3>{{ item.title }}</h3>
              <p><strong>发起人：</strong>{{ item.creator?.username || 'ID:' + item.creatorId }}</p>
              <p><strong>目标金额：</strong>¥{{ item.targetAmount }}</p>
              <p><strong>资金用途：</strong>{{ item.purpose }}</p>
              <p><strong>活动时间：</strong>{{ formatDate(item.startTime) }} ~ {{ formatDate(item.endTime) }}</p>
              <p class="content-preview"><strong>活动详情：</strong>{{ item.content }}</p>
            </div>
            <div class="fundraising-actions">
              <button @click="approveFundraising(item)" class="btn-approve">✓ 通过</button>
              <button @click="rejectFundraising(item)" class="btn-reject">✗ 拒绝</button>
            </div>
          </div>
        </div>

        <!-- 进行中募捐 -->
        <h3 style="margin-top: 40px;">进行中的募捐</h3>
        <div v-if="ongoingFundraising.length === 0" class="empty-state">
          <p>暂无进行中的募捐活动</p>
        </div>
        <div v-else class="fundraising-list">
          <div v-for="item in ongoingFundraising" :key="item.fundraisingId" class="fundraising-card">
            <div class="fundraising-info">
              <h3>{{ item.title }}</h3>
              <p><strong>发起人：</strong>{{ item.creator?.username || 'ID:' + item.creatorId }}</p>
              <p><strong>目标金额：</strong>¥{{ item.targetAmount }}</p>
              <p><strong>已筹金额：</strong>¥{{ item.currentAmount || 0 }}</p>
              <p><strong>资金用途：</strong>{{ item.purpose }}</p>
              <p><strong>活动时间：</strong>{{ formatDate(item.startTime) }} ~ {{ formatDate(item.endTime) }}</p>
              <p class="content-preview"><strong>活动详情：</strong>{{ item.content }}</p>
            </div>
            <div class="fundraising-actions">
              <button @click="completeFundraising(item)" class="btn-complete">✓ 完成</button>
              <button @click="terminateFundraising(item)" class="btn-reject">✗ 终止</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import html2canvas from 'html2canvas'
import { userApi, volunteerApi, auditApi, fundraisingApi, adminApi } from '@/api/index.js'

export default {
  name: 'AdminDashboardView',
  data() {
    return {
      adminUser: null,
      pendingUsers: [],
      volunteerApplications: [],
      pendingSurrenders: [],
      pendingAdoptions: [],
      pendingDonations: [],
      pendingRegistrations: [],
      allUsers: [],
      activities: [],
      pendingFundraising: [],
      ongoingFundraising: [],
      activeTab: 'pending',
      openDropdown: null,
      showActivityForm: false,
      editingActivity: false,
      loadingUsers: false,
      activityForm: {
        activityId: null,
        activityName: '',
        startTime: '',
        endTime: '',
        location: '',
        description: '',
        recruitStatus: 'recruiting'
      },
      basicStats: {},
      adoptionStats: {},
      fundraisingStats: {},
      animalDistribution: {
        speciesList: [],
        statusList: [],
        areaList: []
      },
      speciesChart: null,
      statusChart: null,
      locationChart: null,
      fundraisingTrendChart: null,
      comparisonChart: null
    }
  },
  async mounted() {
    this.checkAdminAuth()
    await this.loadAllData()
    // 等待 DOM 完全渲染后创建图表实例（仅一次）
    this.$nextTick(() => {
      this.initAllCharts()
    })
  },
  beforeDestroy() {
    // 销毁所有图表实例
    const charts = [this.speciesChart, this.statusChart, this.locationChart, this.fundraisingTrendChart, this.comparisonChart]
    charts.forEach(chart => {
      if (chart && !chart.isDisposed()) chart.dispose()
    })
    window.removeEventListener('resize', this.resizeAllChart)
  },
  methods: {
    toggleDropdown(dropdown) {
      this.openDropdown = this.openDropdown === dropdown ? null : dropdown
    },

    checkAdminAuth() {
      const isAdmin = localStorage.getItem('isAdmin')
      const userStr = localStorage.getItem('currentUser')
      if (!isAdmin || !userStr) {
        alert('请先以管理员身份登录')
        this.$router.push('/admin/login')
        return false
      }
      try {
        this.adminUser = JSON.parse(userStr)
        if (this.adminUser.role !== 'admin') {
          alert('无权访问此页面')
          this.$router.push('/')
          return false
        }
        return true
      } catch (e) {
        alert('登录信息异常，请重新登录')
        this.$router.push('/admin/login')
        return false
      }
    },

    async loadAllData() {
      if (!this.adminUser) return
      await Promise.all([
        this.loadPendingUsers(),
        this.loadVolunteerApplications(),
        this.loadPendingSurrenders(),
        this.loadPendingAdoptions(),
        this.loadPendingRegistrations(),
        this.loadAllUsers(),
        this.loadActivities(),
        this.loadPendingFundraising(),
        this.loadOngoingFundraising(),
        this.loadStatistics()
      ])
    },

    async loadStatistics() {
      try {
        const [basicRes, adoptionRes, fundraisingRes, distributionRes] = await Promise.all([
          adminApi.getBasicStatistics(),
          adminApi.getAdoptionStatistics(),
          adminApi.getFundraisingStatistics(),
          adminApi.getAnimalDistribution()
        ])
        this.basicStats = basicRes.data || {}
        this.adoptionStats = adoptionRes.data || {}
        this.fundraisingStats = fundraisingRes.data || {}

        // 兼容后端返回的不同字段名
        const dist = distributionRes.data || {}
        this.animalDistribution = {
          speciesList: dist.speciesList || dist.species || [],
          statusList: dist.statusList || dist.status || [],
          areaList: dist.areaList || dist.areas || []
        }

        // 数据加载完成后刷新图表（仅更新数据，不重建实例）
        this.$nextTick(() => {
          this.refreshAllCharts()
        })
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },

    async loadPendingUsers() {
      try {
        const res = await userApi.getPendingUsers()
        this.pendingUsers = Array.isArray(res.data) ? res.data : []
      } catch (error) {
        this.pendingUsers = []
      }
    },

    async loadPendingFundraising() {
      try {
        const res = await fundraisingApi.getFundraisingByStatus('pending')
        this.pendingFundraising = res.data || []
      } catch (error) {
        this.pendingFundraising = []
      }
    },

    async loadOngoingFundraising() {
      try {
        const res = await fundraisingApi.getFundraisingByStatus('ongoing')
        this.ongoingFundraising = res.data || []
      } catch (error) {
        this.ongoingFundraising = []
      }
    },

    async loadVolunteerApplications() {
      try {
        const res = await userApi.getVolunteerApplications()
        this.volunteerApplications = res.data || []
      } catch (error) {
        this.volunteerApplications = []
      }
    },

    async loadPendingSurrenders() {
      try {
        const res = await auditApi.surrender.getPending()
        this.pendingSurrenders = res.data || []
      } catch (error) {
        this.pendingSurrenders = []
      }
    },

    async loadPendingAdoptions() {
      try {
        const res = await auditApi.adoption.getPending()
        this.pendingAdoptions = res.data || []
      } catch (error) {
        this.pendingAdoptions = []
      }
    },

    async loadPendingRegistrations() {
      try {
        const res = await auditApi.registration.getPending()
        this.pendingRegistrations = res.data || []
      } catch (error) {
        this.pendingRegistrations = []
      }
    },

    async loadAllUsers() {
      this.loadingUsers = true
      try {
        const res = await userApi.getAllUsersForAdmin()
        this.allUsers = res.data || []
      } catch (error) {
        this.allUsers = []
      } finally {
        this.loadingUsers = false
      }
    },

    async loadActivities() {
      try {
        const res = await volunteerApi.getActivities()
        this.activities = res.data || []
      } catch (error) {
        this.activities = []
      }
    },

    // 用户审核
    async approveUser(user) {
      if (!confirm(`确定通过用户 ${user.username} 注册？`)) return
      try {
        await userApi.approveUser(user.userId)
        alert('审核通过')
        await this.loadPendingUsers()
        await this.loadStatistics()          // 刷新统计数据和图表
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async rejectUser(user) {
      if (!confirm(`确定拒绝用户 ${user.username} 注册？`)) return
      try {
        await userApi.rejectUser(user.userId)
        alert('已拒绝')
        await this.loadPendingUsers()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },

// 志愿者审核
    async approveVolunteerApp(user) {
      const userId = user.userId || user.id || user.user_id
      if (!userId) return
      if (!confirm(`确定通过 ${user.username} 志愿者申请？`)) return
      try {
        await userApi.approveVolunteerApplication(userId)
        alert('已通过')
        await Promise.all([this.loadVolunteerApplications(), this.loadAllUsers()])
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async rejectVolunteerApp(user) {
      const userId = user.userId || user.id || user.user_id
      if (!confirm(`确定拒绝 ${user.username} 志愿者申请？`)) return
      try {
        await userApi.rejectVolunteerApplication(userId)
        alert('已拒绝')
        await Promise.all([this.loadVolunteerApplications(), this.loadAllUsers()])
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },

// 送养审核
    async approveSurrender(item) {
      if (!confirm(`通过送养审核申请 #${item.surrenderId}？`)) return
      try {
        await auditApi.surrender.approve(item.surrenderId)
        alert('审核通过')
        await this.loadPendingSurrenders()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async rejectSurrender(item) {
      if (!confirm(`拒绝送养审核申请 #${item.surrenderId}？`)) return
      try {
        await auditApi.surrender.reject(item.surrenderId, '')
        alert('已拒绝')
        await this.loadPendingSurrenders()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },

// 领养审核
    async approveAdoption(item) {
      if (!confirm(`通过领养申请 #${item.applicationId}？`)) return
      try {
        await auditApi.adoption.approve(item.applicationId)
        alert('审核通过')
        await this.loadPendingAdoptions()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async rejectAdoption(item) {
      if (!confirm(`拒绝领养申请 #${item.applicationId}？`)) return
      try {
        await auditApi.adoption.reject(item.applicationId, '')
        alert('已拒绝')
        await this.loadPendingAdoptions()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },

// 活动报名审核
    async approveRegistration(reg) {
      if (!confirm(`通过 ${reg.username} 报名 ${reg.activity_name}？`)) return
      try {
        await auditApi.registration.approve(reg.activity_id, reg.user_id)
        alert('已通过')
        await this.loadPendingRegistrations()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async rejectRegistration(reg) {
      if (!confirm(`拒绝 ${reg.username} 报名 ${reg.activity_name}？`)) return
      try {
        await auditApi.registration.reject(reg.activity_id, reg.user_id)
        alert('已拒绝')
        await this.loadPendingRegistrations()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },

// 募捐审核
    async approveFundraising(item) {
      if (!confirm(`通过募捐活动 ${item.title}？`)) return
      try {
        await fundraisingApi.approve(item.fundraisingId)
        alert('审核通过')
        await Promise.all([this.loadPendingFundraising(), this.loadOngoingFundraising()])
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async rejectFundraising(item) {
      if (!confirm(`拒绝募捐活动 ${item.title}？`)) return
      try {
        await fundraisingApi.reject(item.fundraisingId)
        alert('已拒绝')
        await this.loadPendingFundraising()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async completeFundraising(item) {
      if (!confirm(`完成募捐 ${item.title}？`)) return
      try {
        await fundraisingApi.complete(item.fundraisingId)
        alert('已完成')
        await this.loadOngoingFundraising()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async terminateFundraising(item) {
      if (!confirm(`终止募捐 ${item.title}？`)) return
      try {
        await fundraisingApi.terminate(item.fundraisingId)
        alert('已终止')
        await this.loadOngoingFundraising()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },

// 用户权限分配
    async assignRole(user, role) {
      if (!role) return
      if (!confirm(`赋予 ${user.username} ${this.getRoleText(role)} 权限？`)) return
      try {
        await userApi.assignRole(user.userId, role)
        alert('分配成功')
        await this.loadAllUsers()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async removeVolunteer(user) {
      if (!confirm(`取消 ${user.username} 志愿者身份？`)) return
      try {
        await userApi.removeRole(user.userId, 'volunteer')
        alert('已取消')
        await this.loadAllUsers()
        await this.loadStatistics()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },

    // 活动管理
    openCreateActivityForm() {
      this.editingActivity = false
      this.activityForm = {
        activityId: null, activityName: '', startTime: '', endTime: '',
        location: '', description: '', recruitStatus: 'recruiting'
      }
      this.showActivityForm = true
    },
    editActivity(act) {
      this.editingActivity = true
      this.activityForm = {
        activityId: act.activityId,
        activityName: act.activityName,
        startTime: act.startTime?.slice(0, 16),
        endTime: act.endTime?.slice(0, 16),
        location: act.location,
        description: act.description,
        recruitStatus: act.recruitStatus
      }
      this.showActivityForm = true
    },
    cancelActivityForm() {
      this.showActivityForm = false
    },
    async saveActivity() {
      if (!this.activityForm.activityName) {
        alert('请填写活动名称')
        return
      }
      try {
        if (this.editingActivity) {
          await volunteerApi.updateActivity(this.activityForm.activityId, this.activityForm)
          alert('更新成功')
        } else {
          await volunteerApi.createActivity(this.activityForm, this.adminUser.userId)
          alert('创建成功')
        }
        this.showActivityForm = false
        await this.loadActivities()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async deleteActivity(act) {
      if (!confirm(`删除活动 ${act.activityName}？`)) return
      try {
        await volunteerApi.deleteActivity(act.activityId)
        alert('删除成功')
        await this.loadActivities()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async toggleRecruitStatus(act) {
      const newStatus = act.recruitStatus === 'recruiting' ? 'closed' : 'recruiting'
      try {
        await volunteerApi.updateRecruitStatus(act.activityId, newStatus)
        await this.loadActivities()
      } catch (error) {
        alert('操作失败：' + this.getErrMsg(error))
      }
    },
    async calcDuration(activityId) {
      try {
        const res = await volunteerApi.calculateDuration(activityId)
        alert(res.data?.message || '计算完成')
      } catch (error) {
        alert('计算失败：' + this.getErrMsg(error))
      }
    },

    // 图表方法
    initAllCharts() {
      this.initSpeciesChart()
      this.initStatusChart()
      this.initLocationChart()
      this.initFundraisingTrendChart()
      this.initComparisonChart()
      window.addEventListener('resize', this.resizeAllChart)
    },
    resizeAllChart() {
      this.speciesChart?.resize()
      this.statusChart?.resize()
      this.locationChart?.resize()
      this.fundraisingTrendChart?.resize()
      this.comparisonChart?.resize()
    },
    refreshAllCharts() {
      // 只更新数据，不重建实例
      this.updateSpeciesChart()
      this.updateStatusChart()
      this.updateLocationChart()
      this.updateFundraisingTrendChart()
      this.updateComparisonChart()
    },
    // 以下为图表初始化（带dispose）和数据更新方法
    initSpeciesChart() {
      const el = this.$refs.speciesChartDom
      if (!el) return
      if (this.speciesChart && !this.speciesChart.isDisposed()) {
        this.speciesChart.dispose()
      }
      this.speciesChart = echarts.init(el)
      this.updateSpeciesChart()
    },
    updateSpeciesChart() {
      if (!this.speciesChart) return
      const data = this.animalDistribution.speciesList || []
      if (data.length === 0) {
        this.speciesChart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#999' } },
          tooltip: { trigger: 'item' },
          legend: { orient: 'vertical', left: 'left' },
          series: [{ name: '动物种类', type: 'pie', radius: '70%', data: [] }]
        })
        return
      }
      const option = {
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          name: '动物种类',
          type: 'pie',
          radius: '70%',
          data,
          itemStyle: {
            color: (params) => {
              const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']
              return colors[params.dataIndex % colors.length]
            }
          }
        }]
      }
      this.speciesChart.setOption(option)
    },
    initStatusChart() {
      const el = this.$refs.statusChartDom
      if (!el) return
      if (this.statusChart && !this.statusChart.isDisposed()) {
        this.statusChart.dispose()
      }
      this.statusChart = echarts.init(el)
      this.updateStatusChart()
    },
    updateStatusChart() {
      if (!this.statusChart) return
      const data = this.animalDistribution.statusList || []
      const names = data.map(item => item.name)
      const values = data.map(item => item.value)
      const option = {
        title: names.length === 0 ? { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#999' } } : undefined,
        xAxis: { type: 'category', data: names },
        yAxis: { type: 'value' },
        series: [{ type: 'bar', data: values, barWidth: '40%' }]
      }
      this.statusChart.setOption(option)
    },
    initLocationChart() {
      const el = this.$refs.locationChartDom
      if (!el) return
      if (this.locationChart && !this.locationChart.isDisposed()) {
        this.locationChart.dispose()
      }
      this.locationChart = echarts.init(el)
      this.updateLocationChart()
    },
    updateLocationChart() {
      if (!this.locationChart) return
      const data = this.animalDistribution.areaList || []
      const names = data.map(item => item.name)
      const values = data.map(item => item.value)
      if (names.length === 0) {
        this.locationChart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#999' } },
          xAxis: { type: 'category', data: [] },
          yAxis: { type: 'value' },
          series: [{ type: 'bar', data: [] }]
        })
        return
      }
      const option = {
        xAxis: { type: 'category', data: names, axisLabel: { rotate: 30 } },
        yAxis: { type: 'value' },
        series: [{ type: 'bar', data: values, itemStyle: { color: '#91cc75' } }]
      }
      this.locationChart.setOption(option)
    },
    initFundraisingTrendChart() {
      const el = this.$refs.fundraisingTrendDom
      if (!el) return
      if (this.fundraisingTrendChart && !this.fundraisingTrendChart.isDisposed()) {
        this.fundraisingTrendChart.dispose()
      }
      this.fundraisingTrendChart = echarts.init(el)
      this.updateFundraisingTrendChart()
    },
    updateFundraisingTrendChart() {
      if (!this.fundraisingTrendChart) return
      const d = this.fundraisingStats.fundraisingStatus || {}
      const data = [d.pending || 0, d.ongoing || 0, d.finished || 0, d.terminated || 0]
      const hasData = data.some(v => v > 0)
      if (!hasData) {
        this.fundraisingTrendChart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#999' } },
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: ['待审核', '进行中', '已完成', '已终止'] },
          yAxis: { type: 'value' },
          series: [{ name: '数量', type: 'line', smooth: true, data: [0, 0, 0, 0] }]
        })
        return
      }
      const option = {
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: ['待审核', '进行中', '已完成', '已终止'] },
        yAxis: { type: 'value' },
        series: [{
          name: '数量',
          type: 'line',
          smooth: true,
          data,
          itemStyle: { color: '#fac858' },
          areaStyle: { color: 'rgba(250, 200, 88, 0.2)' }
        }]
      }
      this.fundraisingTrendChart.setOption(option)
    },
    initComparisonChart() {
      const el = this.$refs.comparisonChartDom
      if (!el) return
      if (this.comparisonChart && !this.comparisonChart.isDisposed()) {
        this.comparisonChart.dispose()
      }
      this.comparisonChart = echarts.init(el)
      this.updateComparisonChart()
    },
    updateComparisonChart() {
      if (!this.comparisonChart) return
      const audit = this.basicStats.auditCount || {}
      const pending = [audit.userPending || 0, audit.volPending || 0, audit.adoptPending || 0, audit.surrPending || 0, audit.activityPending || 0, audit.fundraisingPending || 0]
      const passed = [audit.userPass || 0, audit.volPass || 0, audit.adoptPass || 0, audit.surrPass || 0, audit.activityPass || 0, audit.fundraisingPass || 0]
      const rejected = [audit.userReject || 0, audit.volReject || 0, audit.adoptReject || 0, audit.surrReject || 0, audit.activityReject || 0, audit.fundraisingReject || 0]
      const hasData = [...pending, ...passed, ...rejected].some(v => v > 0)
      if (!hasData) {
        this.comparisonChart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#999' } },
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          legend: { data: ['待审核', '已通过', '已拒绝'] },
          xAxis: { type: 'category', data: ['用户', '志愿者', '领养', '送养', '活动报名', '募捐活动'] },
          yAxis: { type: 'value' },
          series: [
            { name: '待审核', type: 'bar', data: [0, 0, 0, 0, 0, 0] },
            { name: '已通过', type: 'bar', data: [0, 0, 0, 0, 0, 0] },
            { name: '已拒绝', type: 'bar', data: [0, 0, 0, 0, 0, 0] }
          ]
        })
        return
      }
      const option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['待审核', '已通过', '已拒绝'] },
        xAxis: { type: 'category', data: ['用户', '志愿者', '领养', '送养', '活动报名', '募捐活动'] },
        yAxis: { type: 'value' },
        series: [
          { name: '待审核', type: 'bar', data: pending, itemStyle: { color: '#fac858' } },
          { name: '已通过', type: 'bar', data: passed, itemStyle: { color: '#91cc75' } },
          { name: '已拒绝', type: 'bar', data: rejected, itemStyle: { color: '#ee6666' } }
        ]
      }
      this.comparisonChart.setOption(option)
    },

    async downloadChart(type) {
      let chartDom = null, name = ''
      switch (type) {
        case 'species': chartDom = this.$refs.speciesChartDom; name = '宠物种类分布'; break
        case 'status': chartDom = this.$refs.statusChartDom; name = '动物状态分布'; break
        case 'location': chartDom = this.$refs.locationChartDom; name = '区域分布'; break
        case 'fundraising': chartDom = this.$refs.fundraisingTrendDom; name = '募捐状态统计'; break
        case 'comparison': chartDom = this.$refs.comparisonChartDom; name = '审核状态统计'; break
      }
      if (!chartDom) return
      const canvas = await html2canvas(chartDom)
      const a = document.createElement('a')
      a.href = canvas.toDataURL('image/png')
      a.download = `${name}_${new Date().getTime()}.png`
      a.click()
    },
    async downloadAllCharts() {
      alert('开始批量导出图表图片，稍等...')
      const list = ['species', 'status', 'location', 'fundraising', 'comparison']
      for (let item of list) {
        await this.downloadChart(item)
        await new Promise(res => setTimeout(res, 600))
      }
    },
    handleLogout() {
      localStorage.clear()
      alert('已退出登录')
      this.$router.push('/admin/login')
    },
    formatDate(date) {
      if (!date) return '未知'
      return new Date(date).toLocaleString('zh-CN')
    },
    formatDateTime(date) {
      return this.formatDate(date)
    },
    getErrMsg(error) {
      return error.response?.data?.message || error.message || '服务器异常'
    },
    getVolunteerApplyStatusText(status) {
      const map = { none: '未申请', pending: '审核中', approved: '已通过', rejected: '已拒绝' }
      return map[status] || status
    },
    getRoleText(role) {
      const map = { admin: '管理员', volunteer: '志愿者', adopter: '领养人', user: '普通用户' }
      return map[role] || role
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background: #f5f7fa;
}
.dashboard-header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #fff;
  padding: 30px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}
.dashboard-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}
.admin-info {
  font-size: 16px;
}
.btn-logout {
  padding: 10px 20px;
  background: #fff;
  color: #1e3c72;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: 0.3s;
}
.btn-logout:hover {
  background: #e8f0ff;
}
.statistics-panel {
  padding: 30px 40px;
}
.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.stats-header h2 {
  font-size: 22px;
  color: #333;
  margin: 0;
}
.btn-download-all {
  padding: 8px 16px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.stats-section {
  margin-bottom: 30px;
}
.stats-section h3 {
  font-size: 18px;
  color: #444;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.stat-card {
  background: #fff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  gap: 15px;
  transition: 0.3s;
}
.stat-card:hover {
  transform: translateY(-5px);
}
.stat-card.highlight {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}
.stat-icon {
  font-size: 36px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f0f5ff;
}
.stat-card.highlight .stat-icon {
  background: rgba(255,255,255,0.2);
}
.stat-content {
  flex: 1;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}
.stat-card.highlight .stat-label {
  color: rgba(255,255,255,0.8);
}
.charts-section {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 20px;
}
.chart-card {
  width: calc(50% - 10px);
  background: #fff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.chart-card.full-width {
  width: 100%;
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.chart-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}
.btn-download {
  padding: 5px 12px;
  font-size: 13px;
  background: #67c23a;
  color: #fff;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}
.chart-container {
  width: 100%;
  height: 300px;
}
.dashboard-content {
  padding: 0 40px 40px;
}
.dropdown-menu {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}
.tab-btn {
  padding: 10px 20px;
  border: 1px solid #409eff;
  background: #fff;
  color: #409eff;
  border-radius: 6px;
  cursor: pointer;
  transition: 0.2s;
}
.tab-btn.active {
  background: #409eff;
  color: #fff;
}
.dropdown-item {
  position: relative;
}
.dropdown-header {
  padding: 10px 20px;
  border: 1px solid #409eff;
  background: #fff;
  color: #409eff;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}
.active-dropdown {
  background: #409eff;
  color: #fff;
}
.dropdown-arrow {
  transition: transform 0.3s;
}
.arrow-up {
  transform: rotate(180deg);
}
.dropdown-content {
  position: absolute;
  top: 110%;
  left: 0;
  background: #fff;
  box-shadow: 0 3px 10px rgba(0,0,0,0.15);
  border-radius: 6px;
  padding: 10px;
  z-index: 99;
  min-width: 220px;
}
.dropdown-btn {
  display: block;
  width: 100%;
  text-align: left;
  padding: 8px 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 4px;
  margin: 3px 0;
}
.dropdown-btn.active {
  background: #ecf5ff;
  color: #409eff;
}
.tab-content {
  background: #fff;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.tab-content h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
  font-size: 20px;
}
.empty-state {
  text-align: center;
  padding: 50px 0;
  color: #999;
  font-size: 15px;
}
.loading-state {
  text-align: center;
  padding: 30px 0;
  color: #666;
}
.user-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.user-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 18px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}
.user-info h3 {
  margin: 0 0 10px;
  color: #333;
}
.user-info p {
  margin: 6px 0;
  color: #666;
  font-size: 14px;
}
.user-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.btn-approve {
  padding: 6px 15px;
  background: #67c23a;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-reject {
  padding: 6px 15px;
  background: #f56c6c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-complete {
  padding: 6px 15px;
  background: #e6a23c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.user-table-container {
  overflow-x: auto;
}
.user-table {
  width: 100%;
  border-collapse: collapse;
}
.user-table th,
.user-table td {
  border: 1px solid #ebeef5;
  padding: 12px 10px;
  text-align: center;
  font-size: 14px;
}
.user-table th {
  background: #f5f7fa;
}
.role-badge {
  padding: 4px 8px;
  border-radius: 3px;
  font-size: 12px;
}
.role-badge.admin {
  background: #fef0f0;
  color: #f56c6c;
}
.role-badge.volunteer {
  background: #f0f9ff;
  color: #409eff;
}
.role-badge.adopter {
  background: #f0fdf4;
  color: #67c23a;
}
.volunteer-badge.yes {
  color: #67c23a;
}
.volunteer-badge.no {
  color: #999;
}
.status-badge {
  font-size: 12px;
}
.role-select {
  padding: 4px;
  border: 1px solid #ccc;
  border-radius: 3px;
}
.btn-remove-volunteer {
  font-size: 12px;
  padding: 3px 6px;
  background: #eee;
  border: none;
  cursor: pointer;
}
.btn-add {
  padding: 8px 16px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 20px;
}
.activity-form {
  border: 1px solid #ebeef5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.form-row {
  margin-bottom: 15px;
}
.form-row label {
  display: block;
  margin-bottom: 5px;
  color: #333;
}
.form-row input,
.form-row textarea,
.form-row select {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}
.form-buttons {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}
.btn-save {
  padding: 8px 20px;
  background: #67c23a;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-cancel {
  padding: 8px 20px;
  background: #999;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.activity-card {
  border: 1px solid #ebeef5;
  padding: 18px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
}
.activity-info h3 {
  margin: 0 0 10px;
}
.activity-info p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}
.activity-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.btn-edit {
  padding: 5px 12px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}
.btn-delete {
  padding: 5px 12px;
  background: #f56c6c;
  color: #fff;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}
.btn-toggle {
  padding: 5px 12px;
  background: #e6a23c;
  color: #fff;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}
.btn-calc {
  padding: 5px 12px;
  background: #9c88ff;
  color: #fff;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}
.status-recruiting {
  color: #67c23a;
}
.status-closed {
  color: #999;
}
.fundraising-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.fundraising-card {
  border: 1px solid #ebeef5;
  padding: 18px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
}
.fundraising-info h3 {
  margin: 0 0 10px;
}
.fundraising-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}
.content-preview {
  max-width: 600px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
