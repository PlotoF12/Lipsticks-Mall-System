import { defineStore } from "pinia";
import http from "../api/http";
import type { UserMe } from "../types/models";

const STORAGE_KEY = "lipsticks_auth";

interface PersistedAuth {
  token: string | null;
  username: string | null;
  role: string | null;
}

export const useAuthStore = defineStore("auth", {
  state: (): PersistedAuth => ({
    token: null,
    username: null,
    role: null
  }),
  actions: {
    restore() {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return;
      try {
        const p = JSON.parse(raw) as PersistedAuth;
        this.token = p.token;
        this.username = p.username;
        this.role = p.role;
      } catch {
        localStorage.removeItem(STORAGE_KEY);
      }
    },
    persist() {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          token: this.token,
          username: this.username,
          role: this.role
        } satisfies PersistedAuth)
      );
    },
    clear() {
      this.token = null;
      this.username = null;
      this.role = null;
      localStorage.removeItem(STORAGE_KEY);
    },
    async login(username: string, password: string) {
      const res = await http.post<{
        success: boolean;
        data: { token: string; tokenType: string; username: string; role: string };
      }>("/auth/login", { username, password });
      const d = res.data.data;
      this.token = d.token;
      this.username = d.username;
      this.role = d.role;
      this.persist();
    },
    async register(username: string, password: string) {
      await http.post("/auth/register", { username, password });
    },
    async fetchMe(): Promise<UserMe | null> {
      if (!this.token) return null;
      const res = await http.get<{ success: boolean; data: UserMe }>("/auth/me");
      const me = res.data.data;
      this.username = me.username;
      this.role = me.role;
      this.persist();
      return me;
    },
    async updateProfile(body: { gender?: string; skinTone?: string; skinType?: string }) {
      await http.put("/auth/me", body);
    },
    async changePassword(oldPassword: string, newPassword: string) {
      await http.post("/auth/me/password", { oldPassword, newPassword });
    },
    logout() {
      this.clear();
    }
  }
});
