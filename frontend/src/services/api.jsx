import axios from "axios";

const API_URL = "http://localhost:8080";

const api = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Automatically add token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// AUTH
export const loginUser = (data) => api.post("/auth/login", data);
export const registerUser = (data) => api.post("/auth/register", data);

// USERS
export const getUsers = (page = 0, size = 5) =>
  api.get(`/users?page=${page}&size=${size}`);
export const getUserById = (id) => api.get(`/users/${id}`);
export const addUser = (data) => api.post("/users", data);
export const updateUser = (id, data) => api.put(`/users/${id}`, data);
export const deleteUser = (id) => api.delete(`/users/${id}`);

// ✅ NEW — Self Profile
export const getMyProfile = () => api.get("/users/me");
export const updateMyProfile = (data) => api.put("/users/me", data);

// ✅ NEW — Admin Requests
export const requestAdminAccess = (reason) =>
  api.post("/admin/request", { reason });
export const getPendingRequests = () => api.get("/admin/requests");
export const approveRequest = (id) => api.put(`/admin/approve/${id}`);
export const rejectRequest = (id) => api.put(`/admin/reject/${id}`);

export const agentChat = (message) => api.post("/ai/agent", { message });

export const requestAgentChat = (message) =>
  api.post("/ai/agent/requests", { message });

export const orchestratorChat = (message) =>
  api.post("/ai/agent/orchestrator", { message });

export default api;
