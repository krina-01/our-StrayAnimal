<template>
  <div class="user-list">
    <h1>用户列表</h1>

    <form @submit.prevent="addUser" class="add-form">
      <h3>添加新用户</h3>

      <div class="form-group">
        <label>用户名：</label>
        <input v-model="newUser.username" placeholder="请输入用户名" required />
      </div>

      <div class="form-group">
        <label>密码：</label>
        <input v-model="newUser.password" type="password" placeholder="请输入密码" required />
      </div>

      <div class="form-group">
        <label>手机号：</label>
        <input v-model="newUser.phone" placeholder="请输入手机号" />
      </div>

      <div class="form-group">
        <label>邮箱：</label>
        <input v-model="newUser.email" type="email" placeholder="请输入邮箱" required />
      </div>

      <div class="form-group">
        <label>角色：</label>
        <select v-model="newUser.role">
          <option value="user">普通用户</option>
          <option value="admin">管理员</option>
        </select>
      </div>

      <div class="form-group">
        <label>是否志愿者：</label>
        <select v-model="newUser.isVolunteer">
          <option :value="false">否</option>
          <option :value="true">是</option>
        </select>
      </div>

      <div class="form-group">
        <label>性别：</label>
        <select v-model="newUser.gender">
          <option value="unknown">未知</option>
          <option value="male">男</option>
          <option value="female">女</option>
        </select>
      </div>

      <div class="form-group">
        <label>有无固定收入：</label>
        <select v-model="newUser.hasFixedIncome">
          <option :value="false">无</option>
          <option :value="true">有</option>
        </select>
      </div>

      <div class="form-group">
        <label>出生年份：</label>
        <input v-model.number="newUser.birthYear" type="number" placeholder="例如：1990" />
      </div>

      <div class="form-group">
        <label>是否有养宠经验：</label>
        <select v-model="newUser.isPetExperience">
          <option :value="false">否</option>
          <option :value="true">是</option>
        </select>
      </div>

      <div class="form-group">
        <label>详细地址：</label>
        <input v-model="newUser.address" placeholder="请输入地址" />
      </div>

      <button type="submit">添加用户</button>
    </form>

    <table>
      <thead>
      <tr>
        <th>ID</th>
        <th>用户名</th>
        <th>手机号</th>
        <th>邮箱</th>
        <th>角色</th>
        <th>志愿者</th>
        <th>性别</th>
        <th>出生年份</th>
        <th>养宠经验</th>
        <th>地址</th>
        <th>操作</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="user in users" :key="user.userId">
        <td>{{ user.userId }}</td>
        <td>{{ user.username }}</td>
        <td>{{ user.phone || '-' }}</td>
        <td>{{ user.email }}</td>
        <td>{{ user.role === 'admin' ? '管理员' : '普通用户' }}</td>
        <td>{{ user.isVolunteer ? '是' : '否' }}</td>
        <td>{{ getGenderText(user.gender) }}</td>
        <td>{{ user.birthYear || '-' }}</td>
        <td>{{ user.isPetExperience ? '有' : '无' }}</td>
        <td>{{ user.address || '-' }}</td>
        <td>
          <button @click="deleteUser(user.userId)" class="btn-delete">删除</button>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import apiClient from '../api'

const users = ref([])
const newUser = ref({
  username: '',
  password: '',
  phone: '',
  email: '',
  role: 'user',
  isVolunteer: false,
  gender: 'unknown',
  hasFixedIncome: false,
  birthYear: null,
  isPetExperience: false,
  address: ''
})

const fetchUsers = async () => {
  try {
    const response = await apiClient.get('/users')
    users.value = response.data
  } catch (error) {
    console.error('获取用户列表失败:', error)
    alert('获取用户列表失败，请检查后端服务是否启动')
  }
}

const addUser = async () => {
  try {
    await apiClient.post('/users', newUser.value)
    resetForm()
    fetchUsers()
    alert('添加成功！')
  } catch (error) {
    console.error('添加用户失败:', error)
    alert('添加用户失败：' + (error.response?.data?.message || error.message))
  }
}

const deleteUser = async (id) => {
  if (!confirm('确定要删除这个用户吗？')) {
    return
  }

  try {
    await apiClient.delete(`/users/${id}`)
    fetchUsers()
    alert('删除成功！')
  } catch (error) {
    console.error('删除用户失败:', error)
    alert('删除用户失败')
  }
}

const resetForm = () => {
  newUser.value = {
    username: '',
    password: '',
    phone: '',
    email: '',
    role: 'user',
    isVolunteer: false,
    gender: 'unknown',
    hasFixedIncome: false,
    birthYear: null,
    isPetExperience: false,
    address: ''
  }
}

const getGenderText = (gender) => {
  const map = {
    'male': '男',
    'female': '女',
    'unknown': '未知'
  }
  return map[gender] || '未知'
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.add-form {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.add-form h3 {
  margin-top: 0;
  color: #333;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #4CAF50;
}

.add-form button {
  width: 100%;
  padding: 12px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 10px;
}

.add-form button:hover {
  background-color: #45a049;
}

table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

th {
  background-color: #4CAF50;
  color: white;
  font-weight: bold;
}

tr:hover {
  background-color: #f5f5f5;
}

.btn-delete {
  padding: 6px 12px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-delete:hover {
  background-color: #da190b;
}
</style>
