<template>
  <div class="audit-manage">
    <div class="page-header">
      <h1>📋 审核管理中心</h1>
      <div class="header-actions">
        <button @click="goBack" class="btn-back">返回主页</button>
      </div>
    </div>

    <div class="audit-content">
      <div class="tabs">
        <button
          :class="['tab-btn', { active: activeTab === 'surrender' }]"
          @click="activeTab = 'surrender'"
        >
          送养审核 ({{ pendingSurrenders.length }})
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'adoption' }]"
          @click="activeTab = 'adoption'"
        >
          领养申请审核 ({{ pendingAdoptions.length }})
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'donation' }]"
          @click="activeTab = 'donation'"
        >
          提现审核 ({{ pendingDonations.length }})
        </button>
      </div>

      <div v-if="activeTab === 'surrender'" class="tab-content">
        <h2>待审核送养申请</h2>
        <div v-if="pendingSurrenders.length === 0" class="empty-state">
          <p>暂无待审核的送养申请</p>
        </div>
        <div v-else class="card-list">
          <div v-for="item in pendingSurrenders" :key="item.surrenderId" class="audit-card">
            <div class="card-header">
              <h3>送养申请 #{{ item.surrenderId }}</h3>
              <span class="status-badge pending">待审核</span>
            </div>
            <div class="card-body">
              <div class="info-row">
                <strong>申请人：</strong>{{ item.user?.username || '未知' }}
              </div>
              <div class="info-row">
                <strong>动物名称：</strong>{{ item.animal?.name || '未填写' }}
              </div>
              <div class="info-row">
                <strong>动物类型：</strong>{{ item.animal?.species || '未填写' }}
              </div>
              <div class="info-row">
                <strong>送养原因：</strong>{{ item.surrenderReason || '未填写' }}
              </div>
              <div class="info-row">
                <strong>提交时间：</strong>{{ formatDate(item.submitTime) }}
              </div>
            </div>
            <div class="card-actions">
              <button @click="approveSurrender(item)" class="btn-approve">
                ✓ 通过
              </button>
              <button @click="showRejectDialog('surrender', item)" class="btn-reject">
                ✗ 拒绝
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'adoption'" class="tab-content">
        <h2>待审核领养申请</h2>
        <div v-if="pendingAdoptions.length === 0" class="empty-state">
          <p>暂无待审核的领养申请</p>
        </div>
        <div v-else class="card-list">
          <div v-for="item in pendingAdoptions" :key="item.applicationId" class="audit-card">
            <div class="card-header">
              <h3>领养申请 #{{ item.applicationId }}</h3>
              <span class="status-badge pending">待审核</span>
            </div>
            <div class="card-body">
              <div class="info-row">
                <strong>申请人：</strong>{{ item.user?.username || '未知' }}
              </div>
              <div class="info-row">
                <strong>联系电话：</strong>{{ item.user?.phone || '未填写' }}
              </div>
              <div class="info-row">
                <strong>邮箱：</strong>{{ item.user?.email || '未填写' }}
              </div>
              <div class="info-row">
                <strong>申请时间：</strong>{{ formatDate(item.applicationTime) }}
              </div>
              <div class="info-row" v-if="item.agreementContent">
                <strong>协议内容：</strong>{{ item.agreementContent }}
              </div>
            </div>
            <div class="card-actions">
              <button @click="approveAdoption(item)" class="btn-approve">
                ✓ 通过
              </button>
              <button @click="showRejectDialog('adoption', item)" class="btn-reject">
                ✗ 拒绝
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'donation'" class="tab-content">
        <h2>待审核提现申请</h2>
        <div v-if="pendingDonations.length === 0" class="empty-state">
          <p>暂无待审核的提现申请</p>
        </div>
        <div v-else class="card-list">
          <div v-for="item in pendingDonations" :key="item.id" class="audit-card">
            <div class="card-header">
              <h3>提现申请 #{{ item.id }}</h3>
              <span class="status-badge pending">待审核</span>
            </div>
            <div class="card-body">
              <div class="info-row">
                <strong>申请人：</strong>{{ item.user?.username || '未知' }}
              </div>
              <div class="info-row">
                <strong>提现金额：</strong>¥{{ item.amount || '0.00' }}
              </div>
              <div class="info-row">
                <strong>标题：</strong>{{ item.title || '未填写' }}
              </div>
              <div class="info-row">
                <strong>说明：</strong>{{ item.content || '未填写' }}
              </div>
              <div class="info-row">
                <strong>申请时间：</strong>{{ formatDate(item.createTime) }}
              </div>
            </div>
            <div class="card-actions">
              <button @click="showApproveDonationDialog(item)" class="btn-approve">
                ✓ 通过并发布
              </button>
              <button @click="showRejectDialog('donation', item)" class="btn-reject">
                ✗ 拒绝
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="rejectDialog.visible" class="modal-overlay" @click="closeRejectDialog">
      <div class="modal-content" @click.stop>
        <h3>拒绝原因</h3>
        <textarea
          v-model="rejectDialog.reason"
          placeholder="请输入拒绝原因（可选）"
          rows="4"
        ></textarea>
        <div class="modal-actions">
          <button @click="confirmReject" class="btn-confirm">确认拒绝</button>
          <button @click="closeRejectDialog" class="btn-cancel">取消</button>
        </div>
      </div>
    </div>

    <div v-if="donationApproveDialog.visible" class="modal-overlay" @click="closeDonationApproveDialog">
      <div class="modal-content" @click.stop>
        <h3>发布筹款信息</h3>
        <div class="form-group">
          <label>标题：</label>
          <input
            v-model="donationApproveDialog.title"
            type="text"
            placeholder="请输入筹款标题"
          />
        </div>
        <div class="form-group">
          <label>内容：</label>
          <textarea
            v-model="donationApproveDialog.content"
            placeholder="请输入筹款详细内容"
            rows="6"
          ></textarea>
        </div>
        <div class="modal-actions">
          <button @click="confirmApproveDonation" class="btn-confirm">确认通过</button>
          <button @click="closeDonationApproveDialog" class="btn-cancel">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { auditApi } from '@/api/index.js'

export default {
  name: 'AuditManageView',
  data() {
    return {
      activeTab: 'surrender',
      pendingSurrenders: [],
      pendingAdoptions: [],
      pendingDonations: [],
      rejectDialog: {
        visible: false,
        type: '',
        item: null,
        reason: ''
      },
      donationApproveDialog: {
        visible: false,
        item: null,
        title: '',
        content: ''
      }
    }
  },
  async created() {
    this.checkAdminAuth()
    await this.loadAllPendingData()
  },
  methods: {
    checkAdminAuth() {
      const isAdmin = localStorage.getItem('isAdmin')
      const user = localStorage.getItem('currentUser')

      if (!isAdmin || !user) {
        alert('请先以管理员身份登录')
        this.$router.push('/admin/login')
        return
      }

      const adminUser = JSON.parse(user)
      if (adminUser.role !== 'admin') {
        alert('无权访问此页面')
        this.$router.push('/')
      }
    },

    async loadAllPendingData() {
      await Promise.all([
        this.loadPendingSurrenders(),
        this.loadPendingAdoptions(),
        this.loadPendingDonations()
      ])
    },

    async loadPendingSurrenders() {
      try {
        const response = await auditApi.surrender.getPending()
        this.pendingSurrenders = response.data
      } catch (error) {
        console.error('加载待审核送养申请失败:', error)
        alert('加载送养申请失败')
      }
    },

    async loadPendingAdoptions() {
      try {
        const response = await auditApi.adoption.getPending()
        this.pendingAdoptions = response.data
      } catch (error) {
        console.error('加载待审核领养申请失败:', error)
        alert('加载领养申请失败')
      }
    },

    async loadPendingDonations() {
      try {
        const response = await auditApi.donation.getPending()
        this.pendingDonations = response.data
      } catch (error) {
        console.error('加载待审核提现申请失败:', error)
        alert('加载提现申请失败')
      }
    },

    async approveSurrender(item) {
      if (!confirm(`确定要通过送养申请 #${item.surrenderId} 吗？`)) {
        return
      }

      try {
        await auditApi.surrender.approve(item.surrenderId)
        alert('送养申请审核通过')
        await this.loadPendingSurrenders()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    async approveAdoption(item) {
      if (!confirm(`确定要通过领养申请 #${item.applicationId} 吗？`)) {
        return
      }

      try {
        await auditApi.adoption.approve(item.applicationId)
        alert('领养申请审核通过')
        await this.loadPendingAdoptions()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    showRejectDialog(type, item) {
      this.rejectDialog = {
        visible: true,
        type,
        item,
        reason: ''
      }
    },

    closeRejectDialog() {
      this.rejectDialog = {
        visible: false,
        type: '',
        item: null,
        reason: ''
      }
    },

    async confirmReject() {
      const { type, item, reason } = this.rejectDialog

      try {
        if (type === 'surrender') {
          await auditApi.surrender.reject(item.surrenderId, reason)
          alert('已拒绝送养申请')
          await this.loadPendingSurrenders()
        } else if (type === 'adoption') {
          await auditApi.adoption.reject(item.applicationId, reason)
          alert('已拒绝领养申请')
          await this.loadPendingAdoptions()
        } else if (type === 'donation') {
          await auditApi.donation.reject(item.id, reason)
          alert('已拒绝提现申请')
          await this.loadPendingDonations()
        }
        this.closeRejectDialog()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    showApproveDonationDialog(item) {
      this.donationApproveDialog = {
        visible: true,
        item,
        title: item.title || '',
        content: item.content || ''
      }
    },

    closeDonationApproveDialog() {
      this.donationApproveDialog = {
        visible: false,
        item: null,
        title: '',
        content: ''
      }
    },

    async confirmApproveDonation() {
      const { item, title, content } = this.donationApproveDialog

      if (!title || !content) {
        alert('请填写标题和内容')
        return
      }

      try {
        await auditApi.donation.approve(item.id, { title, content })
        alert('提现申请审核通过，筹款信息已发布')
        await this.loadPendingDonations()
        this.closeDonationApproveDialog()
      } catch (error) {
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'))
      }
    },

    goBack() {
      this.$router.push('/admin/dashboard')
    },

    formatDate(dateString) {
      if (!dateString) return '未知'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.audit-manage {
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  padding: 30px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
}

.btn-back {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2px solid white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-back:hover {
  background: white;
  color: #1e3c72;
}

.audit-content {
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  border-bottom: 2px solid #e0e0e0;
}

.tab-btn {
  padding: 12px 24px;
  background: white;
  border: none;
  border-radius: 8px 8px 0 0;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  background: #f0f0f0;
}

.tab-btn.active {
  background: #1e3c72;
  color: white;
}

.tab-content {
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.tab-content h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
  font-size: 24px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 18px;
}

.card-list {
  display: grid;
  gap: 20px;
}

.audit-card {
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  transition: all 0.3s ease;
}

.audit-card:hover {
  border-color: #1e3c72;
  box-shadow: 0 4px 12px rgba(30, 60, 114, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0e0e0;
}

.card-header h3 {
  margin: 0;
  color: #1e3c72;
  font-size: 20px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.pending {
  background: #ff9800;
  color: white;
}

.card-body {
  margin-bottom: 20px;
}

.info-row {
  margin: 8px 0;
  color: #666;
  line-height: 1.6;
}

.info-row strong {
  color: #333;
  margin-right: 8px;
}

.card-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-approve {
  padding: 10px 20px;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-approve:hover {
  background: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(76, 175, 80, 0.3);
}

.btn-reject {
  padding: 10px 20px;
  background: #f44336;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-reject:hover {
  background: #da190b;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(244, 67, 54, 0.3);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 10px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 600;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #1e3c72;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-confirm {
  padding: 10px 20px;
  background: #1e3c72;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-confirm:hover {
  background: #152a52;
}

.btn-cancel {
  padding: 10px 20px;
  background: #e0e0e0;
  color: #333;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background: #d0d0d0;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    padding: 20px;
  }

  .page-header h1 {
    font-size: 22px;
  }

  .audit-content {
    padding: 20px;
  }

  .card-actions {
    flex-direction: column;
  }

  .btn-approve,
  .btn-reject {
    width: 100%;
  }
}
</style>
