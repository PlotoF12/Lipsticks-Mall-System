<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "./stores/auth";

const auth = useAuthStore();
const router = useRouter();

const isLoggedIn = computed(() => !!auth.token);
const isAdmin = computed(() => auth.role === "ADMIN");

function logout() {
  auth.logout();
  router.push("/");
}
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-inner">
        <router-link class="brand" to="/">Lipstick Mall System</router-link>
        <div class="nav">
          <router-link to="/user/mall">商城</router-link>
          <router-link to="/user/recommend">推荐</router-link>
          <router-link to="/user/visualization">可视化</router-link>
          <template v-if="!isLoggedIn">
            <router-link to="/user/login">登录</router-link>
            <router-link to="/user/register">注册</router-link>
          </template>
          <template v-else>
            <router-link to="/user/profile">个人中心</router-link>
            <router-link v-if="isAdmin" to="/admin">管理后台</router-link>
            <el-button link type="primary" class="logout-btn" @click="logout">退出</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  color: #fff;
  background: linear-gradient(120deg, #7b2146, #d46b8d);
  font-weight: 600;
  font-size: 18px;
  line-height: 60px;
  padding: 0 20px;
  border-radius: 10px 10px 10px 10px;
  margin: 5px
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.brand {
  color: #fff;
  text-decoration: none;
  letter-spacing: 0.04em;
}

.nav {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.nav a {
  color: rgba(255, 255, 255, 0.92);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.nav a.router-link-active {
  text-decoration: underline;
  text-underline-offset: 4px;
}

.logout-btn {
  color: #fff !important;
  font-weight: 600;
}
</style>
