import request from './request'

export const authAPI = {
  login(data) {
    return request.post('/auth/login', data)
  },
  register(data) {
    return request.post('/auth/register', data)
  }
}

export const guestAPI = {
  getProfile() {
    return request.get('/guest/profile')
  },
  updateProfile(data) {
    return request.put('/guest/profile', data)
  },
  createBooking(data) {
    return request.post('/guest/bookings', data)
  },
  cancelBooking(id) {
    return request.put(`/guest/bookings/${id}/cancel`)
  },
  getUserOrders(params) {
    return request.get('/guest/bookings', { params })
  },
  getOrderDetail(id) {
    return request.get(`/guest/bookings/${id}`)
  }
}

export const roomAPI = {
  queryRooms(params) {
    return request.get('/rooms', { params })
  },
  getRoomDetail(id) {
    return request.get(`/rooms/${id}`)
  },
  getRoomTypes() {
    return request.get('/room-types')
  }
}

export const frontDeskAPI = {
  getAllRooms(params) {
    return request.get('/front-desk/rooms', { params })
  },
  updateRoomStatus(id, status) {
    return request.put(`/front-desk/rooms/${id}/status?status=${status}`)
  },
  getOrders(params) {
    return request.get('/front-desk/orders', { params })
  },
  checkIn(data) {
    return request.post('/front-desk/check-in', data)
  },
  checkOut(orderId, data) {
    return request.post(`/front-desk/check-out/${orderId}`, data)
  },
  getTodayArrivals() {
    return request.get('/front-desk/today-arrivals')
  },
  getTodayDepartures() {
    return request.get('/front-desk/today-departures')
  },
  getOrderDetail(id) {
    return request.get(`/front-desk/orders/${id}`)
  }
}

export const adminAPI = {
  getRoomTypes() {
    return request.get('/admin/room-types')
  },
  createRoomType(data) {
    return request.post('/admin/room-types', data)
  },
  updateRoomType(id, data) {
    return request.put(`/admin/room-types/${id}`, data)
  },
  deleteRoomType(id) {
    return request.delete(`/admin/room-types/${id}`)
  },
  createRoom(data) {
    return request.post('/admin/rooms', data)
  },
  updateRoom(id, data) {
    return request.put(`/admin/rooms/${id}`, data)
  },
  deleteRoom(id) {
    return request.delete(`/admin/rooms/${id}`)
  },
  getAllRooms(params) {
    return request.get('/admin/rooms', { params })
  },
  getEmployees() {
    return request.get('/admin/employees')
  },
  createEmployee(data) {
    return request.post('/admin/employees', data)
  },
  updateEmployee(id, data) {
    return request.put(`/admin/employees/${id}`, data)
  },
  deleteEmployee(id) {
    return request.delete(`/admin/employees/${id}`)
  },
  getStatistics(params) {
    return request.get('/admin/statistics', { params })
  },
  getAllOrders(params) {
    return request.get('/admin/orders', { params })
  }
}
