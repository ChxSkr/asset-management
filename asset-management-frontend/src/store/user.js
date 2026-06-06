import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, getProfile as getProfileApi, updateProfile as updateProfileApi } from '../api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null
  }),

  actions: {
    async login(credentials) {
      const res = await loginApi(credentials)
      this.token = res.data.token
      localStorage.setItem('token', res.data.token)
      return res
    },

    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    },

    async fetchProfile() {
      const res = await getProfileApi()
      this.userInfo = res.data
      return res
    }
  }
})