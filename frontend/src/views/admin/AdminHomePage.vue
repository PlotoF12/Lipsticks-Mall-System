<script setup lang="ts">
import { useRouter } from "vue-router";

const router = useRouter();

const entries = [
  { title: "商品管理", desc: "上架、编辑、下架商品", path: "/admin/products" },
  { title: "用户管理", desc: "查看用户、编辑资料、启用/禁用账号", path: "/admin/users" }
];
</script>

<template>
  <div class="wrap">
    <el-page-header @back="router.push('/')">
      <template #content>
        <span class="title">管理后台</span>
      </template>
    </el-page-header>

    <p class="desc">从这里进入运营相关能力。普通用户访问本页会被拦截并回到首页。</p>

    <div class="grid">
      <el-card v-for="item in entries" :key="item.path" class="card" shadow="hover">
        <h3>{{ item.title }}</h3>
        <p>{{ item.desc }}</p>
        <el-button type="primary" @click="router.push(item.path)">进入</el-button>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.wrap {
  max-width: 980px;
  margin: 24px auto;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #512337;
}

.desc {
  color: #4b5563;
  line-height: 1.7;
  margin: 16px 0;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.card h3 {
  margin: 0 0 8px;
  color: #4f2035;
}

.card p {
  margin: 0 0 14px;
  color: #6b7280;
  min-height: 44px;
}
</style>
