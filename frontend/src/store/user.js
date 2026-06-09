import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const employeeInfo = ref(JSON.parse(localStorage.getItem('employeeInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const roleType = computed(() => userInfo.value?.roleType || employeeInfo.value?.roleType || '')
  const isGuest = computed(() => roleType.value === 'GUEST')
  const isFrontDesk = computed(() => roleType.value === 'FRONT_DESK')
  const isAdmin = computed(() => roleType.value === 'ADMIN')

  function setAuth(tokenVal, user, isEmployee) {
    token.value = tokenVal
    localStorage.setItem('token', tokenVal)
    if (isEmployee) {
      employeeInfo.value = user
      userInfo.value = null
      localStorage.setItem('employeeInfo', JSON.stringify(user))
      localStorage.removeItem('userInfo')
    } else {
      userInfo.value = user
      employeeInfo.value = null
      localStorage.setItem('userInfo', JSON.stringify(user))
      localStorage.removeItem('employeeInfo')
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    employeeInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('employeeInfo')
  }

  return { token, userInfo, employeeInfo, isLoggedIn, roleType, isGuest, isFrontDesk, isAdmin, setAuth, logout }
})
