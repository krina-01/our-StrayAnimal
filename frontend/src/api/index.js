import axios from 'axios'

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

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

  applyVolunteer(id) {
    return apiClient.post(`/users/${id}/apply-volunteer`)
  },

  approveVolunteer(id) {
    return apiClient.put(`/users/${id}/approve-volunteer`)
  },

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

export default apiClient
