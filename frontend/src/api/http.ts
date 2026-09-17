import axios from "axios";

const STORAGE_KEY = "lipsticks_auth";

const http = axios.create({
  baseURL: "/api",
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (raw) {
    try {
      const p = JSON.parse(raw) as { token?: string | null };
      if (p.token) {
        config.headers.Authorization = `Bearer ${p.token}`;
      }
    } catch {
      /* ignore */
    }
  }
  return config;
});

http.interceptors.response.use(
  (res) => {
    const body = res.data as { success?: boolean; message?: string };
    if (body && typeof body.success === "boolean" && body.success === false) {
      return Promise.reject(new Error(body.message || "请求失败"));
    }
    return res;
  },
  (err) => Promise.reject(err)
);

export default http;
