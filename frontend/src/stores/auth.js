import { defineStore } from "pinia";
import http from "../api/http";
const STORAGE_KEY = "lipsticks_auth";
export const useAuthStore = defineStore("auth", {
    state: () => ({
        token: null,
        username: null,
        role: null
    }),
    actions: {
        restore() {
            const raw = localStorage.getItem(STORAGE_KEY);
            if (!raw)
                return;
            try {
                const p = JSON.parse(raw);
                this.token = p.token;
                this.username = p.username;
                this.role = p.role;
            }
            catch {
                localStorage.removeItem(STORAGE_KEY);
            }
        },
        persist() {
            localStorage.setItem(STORAGE_KEY, JSON.stringify({
                token: this.token,
                username: this.username,
                role: this.role
            }));
        },
        clear() {
            this.token = null;
            this.username = null;
            this.role = null;
            localStorage.removeItem(STORAGE_KEY);
        },
        async login(username, password) {
            const res = await http.post("/auth/login", { username, password });
            const d = res.data.data;
            this.token = d.token;
            this.username = d.username;
            this.role = d.role;
            this.persist();
        },
        async register(username, password) {
            await http.post("/auth/register", { username, password });
        },
        async fetchMe() {
            if (!this.token)
                return null;
            const res = await http.get("/auth/me");
            const me = res.data.data;
            this.username = me.username;
            this.role = me.role;
            this.persist();
            return me;
        },
        async updateProfile(body) {
            await http.put("/auth/me", body);
        },
        async changePassword(oldPassword, newPassword) {
            await http.post("/auth/me/password", { oldPassword, newPassword });
        },
        logout() {
            this.clear();
        }
    }
});
