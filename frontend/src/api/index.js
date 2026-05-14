import axios from 'axios'

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true   // 新增：允许携带 Cookie/Session，解决 CORS 认证问题
})
export const fundraisingApi = {
  getAllFundraising() {
    return apiClient.get('/fundraising/list')
  },

  getFundraisingByStatus(status) {
    return apiClient.get(`/fundraising/status/${status}`)
  },

  getFundraisingDetail(id) {
    return apiClient.get(`/fundraising/detail/${id}`)
  },

  createFundraising(fundraisingData) {
    return apiClient.post('/fundraising/create', fundraisingData)
  },

  donate(donationData) {
    return apiClient.post('/fundraising/donate', donationData)
  },

  getMyDonations(userId) {
    return apiClient.get(`/fundraising/my-donations/${userId}`)
  },

  getSummary(fundraisingId) {
    return apiClient.get(`/fundraising/summary/${fundraisingId}`)
  },

  getDetails(fundraisingId) {
    return apiClient.get(`/fundraising/details/${fundraisingId}`)
  },

  getMyFundraisings(userId) {
    return apiClient.get(`/fundraising/my-fundraisings/${userId}`)
  },

  completeMyFundraising(id, userId) {
    return apiClient.put(`/fundraising/my-complete/${id}`, null, {
      params: { userId }
    })
  },

  terminateMyFundraising(id, userId) {
    return apiClient.put(`/fundraising/my-terminate/${id}`, null, {
      params: { userId }
    })
  },

  approve(id) {
    return apiClient.put(`/fundraising/approve/${id}`)
  },

  reject(id) {
    return apiClient.put(`/fundraising/reject/${id}`)
  },

  complete(id) {
    return apiClient.put(`/fundraising/complete/${id}`)
  },

  terminate(id) {
    return apiClient.put(`/fundraising/terminate/${id}`)
  }
}

export const userApi = {
  getAllUsers() {
    return apiClient.get('/users')
  },

  getUserById(id) {
    return apiClient.get(`/users/${id}`)
  },

  getUserByUsername(username) {
    return apiClient.get(`/users/username/${username}`)
  },

  register(userData) {
    return apiClient.post('/users/register', userData)
  },

  login(credentials) {
    return apiClient.post('/users/login', credentials)
  },

  updateUser(id, userData) {
    return apiClient.put(`/users/${id}`, userData)
  },

  deleteUser(id) {
    return apiClient.delete(`/users/${id}`)
  },

  getPendingUsers() {
    return apiClient.get('/users/pending')
  },

  approveUser(id) {
    return apiClient.put(`/users/${id}/approve`)
  },

  rejectUser(id) {
    return apiClient.put(`/users/${id}/reject`)
  },

  assignRole(id, role) {
    return apiClient.put(`/users/${id}/assign-role`, { role })
  },

  removeRole(id, role) {
    return apiClient.put(`/users/${id}/remove-role`, { role })
  },

  // 志愿者申请（普通用户提交）
  applyVolunteer(id) {
    return apiClient.post(`/users/${id}/apply-volunteer`)
  },

  // 管理员：获取所有待审核的志愿者申请
  getVolunteerApplications() {
    return apiClient.get('/users/volunteer-applications')
  },

  // 管理员：获取所有待审核的领养人申请
  getAdopterApplications() {
    return apiClient.get('/users/adopter-applications')
  },

  // 管理员：通过志愿者申请
  approveVolunteerApplication(id) {
    return apiClient.put(`/users/${id}/approve-volunteer`)
  },

  // 管理员：拒绝志愿者申请
  rejectVolunteerApplication(id) {
    return apiClient.put(`/users/${id}/reject-volunteer-application`)
  },

  // 取消志愿者身份（保留）
  cancelVolunteer(id) {
    return apiClient.put(`/users/${id}/cancel-volunteer`)
  },

  applyAdopter(id) {
    return apiClient.post(`/users/${id}/apply-adopter`)
  },

  approveAdopter(id) {
    return apiClient.put(`/users/${id}/approve-adopter`)
  },

  cancelAdopter(id) {
    return apiClient.put(`/users/${id}/cancel-adopter`)
  },

  getVolunteers() {
    return apiClient.get('/users/volunteers')
  },

  getPendingDeleteUsers() {
    return apiClient.get('/users/pending-delete')
  },

  approveDelete(id) {
    return apiClient.put(`/users/${id}/approve-delete`)
  },

  rejectDelete(id) {
    return apiClient.put(`/users/${id}/reject-delete`)
  },

  verifyAdmin(credentials) {
    return apiClient.post('/users/admin/verify', credentials)
  },

  getAllUsersForAdmin() {
    return apiClient.get('/users/admin/all-users')
  }
}

export const animalApi = {
  getAllAnimals() {
    return apiClient.get('/animals')
  },

  getAvailableAnimals() {
    return apiClient.get('/animals/available')
  },

  getAvailableAnimalsExcludingUser(userId) {
    return apiClient.get(`/animals/available/exclude-user/${userId}`)
  },

  getAnimalById(id) {
    return apiClient.get(`/animals/${id}`)
  },

  getAnimalsByUserId(userId) {
    return apiClient.get(`/animals/user/${userId}`)
  }
}

export const adoptionApplicationApi = {
  getAllApplications() {
    return apiClient.get('/adoption-applications')
  },

  getApplicationById(id) {
    return apiClient.get(`/adoption-applications/${id}`)
  },

  getApplicationsByStatus(status) {
    return apiClient.get(`/adoption-applications/status/${status}`)
  },

  getApplicationsByUserId(userId) {
    return apiClient.get(`/adoption-applications/user/${userId}`)
  },

  getApplicationsBySurrenderId(surrenderId) {
    return apiClient.get(`/adoption-applications/surrender/${surrenderId}`)
  },

  createApplication(applicationData) {
    return apiClient.post('/adoption-applications', applicationData)
  },

  approveApplication(id) {
    return apiClient.put(`/adoption-applications/${id}/approve`)
  },

  rejectApplication(id) {
    return apiClient.put(`/adoption-applications/${id}/reject`)
  },

  signAgreement(id, agreementContent) {
    return apiClient.put(`/adoption-applications/${id}/sign-agreement`, { agreementContent })
  },

  deleteApplication(id) {
    return apiClient.delete(`/adoption-applications/${id}`)
  }
}

export const surrenderApi = {
  getSurrenderListByUser(userId) {
    return apiClient.get(`/surrender/user/${userId}`)
  },

  getSurrenderById(id) {
    return apiClient.get(`/surrender/${id}`)
  },

  getSurrenderByAnimalId(animalId) {
    return apiClient.get(`/surrender/animal/${animalId}`)
  },

  publishSurrender(data) {
    return apiClient.post('/surrender/publish', data)
  },

  approveSurrender(id) {
    return apiClient.put(`/surrender/${id}/approve`)
  },

  rejectSurrender(id) {
    return apiClient.put(`/surrender/${id}/reject`)
  }
}

// ====================== 志愿活动相关 API ======================
export const volunteerApi = {
  // 志愿者端
  getActivities(status) {
    return apiClient.get('/volunteer/activities', { params: { status } })
  },

  getActivityDetail(id) {
    return apiClient.get(`/volunteer/activities/${id}`)
  },

  registerActivity(activityId, userId) {
    return apiClient.post(`/volunteer/activities/${activityId}/register`, null, {
      params: { userId }
    })
  },

  cancelRegistration(activityId, userId) {
    return apiClient.delete(`/volunteer/activities/${activityId}/register`, {
      params: { userId }
    })
  },

  checkin(activityId, userId) {
    return apiClient.post(`/volunteer/activities/${activityId}/checkin`, null, {
      params: { userId }
    })
  },

  getVolunteerStats(userId) {
    return apiClient.get(`/users/${userId}/volunteer-stats`)
  },

  getServiceRecords(userId) {
    return apiClient.get(`/users/${userId}/service-records`)
  },

  getRegistrations(userId) {
    return apiClient.get(`/users/${userId}/registrations`)
  },

  // 管理员端（活动管理）
  createActivity(activityData, adminId) {
    return apiClient.post('/volunteer/admin/activities', activityData, {
      params: { adminId }
    })
  },

  updateActivity(id, activityData) {
    return apiClient.put(`/volunteer/admin/activities/${id}`, activityData)
  },

  deleteActivity(id) {
    return apiClient.delete(`/volunteer/admin/activities/${id}`)
  },

  updateRecruitStatus(id, status) {
    return apiClient.put(`/volunteer/admin/activities/${id}/recruit-status`, null, {
      params: { status }
    })
  },

  calculateDuration(activityId) {
    return apiClient.post(`/volunteer/activities/${activityId}/calculate-duration`)
  },
  // 新增：报名审核
  getPendingRegistrations() {
    return apiClient.get('/volunteer/admin/pending-registrations')
  },
  approveRegistration(activityId, userId) {
    return apiClient.put(`/volunteer/admin/registrations/${activityId}/approve`, null, {
      params: { userId }
    })
  },
  rejectRegistration(activityId, userId) {
    return apiClient.delete(`/volunteer/admin/registrations/${activityId}/reject`, {
      params: { userId }
    })
  }
}

export const auditApi = {
  surrender: {
    getPending: () => apiClient.get('/audit/surrender/pending'),
    getAll: () => apiClient.get('/audit/surrender/all'),
    getByUser: (userId) => apiClient.get(`/audit/surrender/user/${userId}`),
    submit: (data) => apiClient.post('/audit/surrender/submit', data),
    approve: (id) => apiClient.put(`/audit/surrender/${id}/approve`),
    reject: (id, reason) => apiClient.put(`/audit/surrender/${id}/reject`, { reason })
  },
  adoption: {
    getPending: () => apiClient.get('/audit/adoption/pending'),
    getAll: () => apiClient.get('/audit/adoption/all'),
    getByUser: (userId) => apiClient.get(`/audit/adoption/user/${userId}`),
    submit: (data) => apiClient.post('/audit/adoption/submit', data),
    approve: (id) => apiClient.put(`/audit/adoption/${id}/approve`),
    reject: (id, reason) => apiClient.put(`/audit/adoption/${id}/reject`, { reason })
  },
  fundraising: {
    getPending: () => apiClient.get('/audit/fundraising/pending'),
    getAll: () => apiClient.get('/audit/fundraising/all'),
    getByUser: (userId) => apiClient.get(`/audit/fundraising/user/${userId}`),
    approve: (id, remark) => apiClient.put(`/audit/fundraising/${id}/approve`, { remark }),
    reject: (id, remark) => apiClient.put(`/audit/fundraising/${id}/reject`, { remark })
  },
  adopter: {
    getPending: () => apiClient.get('/audit/adopter/pending'),
    approve: (id) => apiClient.put(`/audit/adopter/${id}/approve`),
    reject: (id) => apiClient.put(`/audit/adopter/${id}/reject`)
  },
  donation: {
    getPending: () => apiClient.get('/audit/donation/pending'),
    getAll: () => apiClient.get('/audit/donation/all'),
    submit: (data) => apiClient.post('/audit/donation/withdraw', data),
    approve: (id, publishData) => apiClient.put(`/audit/donation/${id}/approve`, publishData),
    markPaid: (id) => apiClient.put(`/audit/donation/${id}/mark-paid`),
    reject: (id, reason) => apiClient.put(`/audit/donation/${id}/reject`, { reason })
  },
  registration: {
    getPending: () => apiClient.get('/audit/registration/pending'),
    approve: (activityId, userId) => apiClient.put(`/audit/registration/${activityId}/approve`, null, {
      params: { userId }
    }),
    reject: (activityId, userId) => apiClient.delete(`/audit/registration/${activityId}/reject`, {
      params: { userId }
    })
  },
  getStatistics: () => apiClient.get('/audit/statistics')
}

export const adminApi = {
  getBasicStatistics() {
    return apiClient.get('/admin/statistics/basic')
  },

  getAdoptionStatistics() {
    return apiClient.get('/admin/statistics/adoption')
  },

  getFundraisingStatistics() {
    return apiClient.get('/admin/statistics/fundraising')
  },

  getAnimalDistribution() {
    return apiClient.get('/admin/statistics/animal-distribution')
  }
}

export default apiClient
