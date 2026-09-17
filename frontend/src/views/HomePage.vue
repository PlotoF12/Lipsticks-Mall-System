<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const auth = useAuthStore();

const isAdmin = computed(() => auth.role === "ADMIN");

const modules = [
  {
    key: "mall",
    title: "口红商城",
    caption: "品牌选购",
    iconText: "M",
    desc: "浏览品牌、色号、价格与热门榜单，支持登录后查看更完整的商品信息。",
    path: "/user/mall"
  },
  {
    key: "recommend",
    title: "产品推荐",
    caption: "智能建议",
    iconText: "R",
    desc: "基于性别、肤质与使用场景进行差异化推荐，提供更贴近真实消费场景的建议。",
    path: "/user/recommend"
  },
  {
    key: "visualization",
    title: "颜色可视化",
    caption: "色彩洞察",
    iconText: "V",
    desc: "以可视化方式查看色号分布、色系对比与风格表现，帮助快速锁定合适口红。",
    path: "/user/visualization"
  }
];

function enterModule(key: string, path: string) {
  router.push(path);
}

function enterAdmin() {
  router.push("/admin");
}
</script>

<template>
  <section class="home-wrap">
    <div class="bg-orb orb-left"></div>
    <div class="bg-orb orb-right"></div>

    <div class="intro">
      <div class="intro-badge">Lipstick Experience Platform</div>
      <h1>智能口红商城系统</h1>
      <p>
        本系统聚焦口红选购、智能推荐与颜色可视化展示，为用户提供从了解商品到试妆决策的一站式体验。
        你可以在商城中快速查看品牌与色号信息，也可以通过推荐模块获得更贴合人群与场景的建议，
        并在颜色可视化页面直观感受不同色系风格，提升选购效率与体验。
      </p>
    </div>

    <div class="module-grid" :class="{ 'grid-four': modules.length === 4 }">
      <div
        v-for="item in modules"
        :key="item.key"
        class="module-card"
      >
        <div class="module-head">
          <div class="module-icon">{{ item.iconText }}</div>
          <span class="module-caption">{{ item.caption }}</span>
        </div>
        <h3>{{ item.title }}</h3>
        <p>{{ item.desc }}</p>
        <el-button class="module-btn" type="primary" @click.stop="enterModule(item.key, item.path)">
          进入{{ item.title }}
        </el-button>
      </div>

      <div v-if="isAdmin" class="module-card admin-card">
        <div class="module-head">
          <div class="module-icon admin-icon">A</div>
          <span class="module-caption">运营</span>
        </div>
        <h3>管理后台</h3>
        <p>管理员可在此维护商品与用户：上架/下架、编辑详情与价格库存、启用或禁用账号等。</p>
        <el-button class="module-btn" type="primary" @click.stop="enterAdmin">进入管理后台</el-button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.home-wrap {
  min-height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 30px 24px;
  position: relative;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(10px);
  pointer-events: none;
}

.orb-left {
  width: 280px;
  height: 280px;
  left: -70px;
  top: 40px;
  background: radial-gradient(circle, rgba(215, 110, 161, 0.28), rgba(215, 110, 161, 0));
}

.orb-right {
  width: 340px;
  height: 340px;
  right: -90px;
  bottom: 10px;
  background: radial-gradient(circle, rgba(108, 99, 255, 0.2), rgba(108, 99, 255, 0));
}

.intro {
  max-width: 920px;
  text-align: center;
  margin-bottom: 30px;
  position: relative;
  z-index: 1;
}

.intro-badge {
  display: inline-block;
  padding: 6px 14px;
  margin-bottom: 14px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: #7b2146;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(209, 138, 168, 0.45);
}

.intro h1 {
  margin: 0 0 14px;
  font-size: 38px;
  color: #512337;
  letter-spacing: 0.03em;
}

.intro p {
  margin: 0;
  line-height: 1.8;
  color: #4d525d;
}

.module-grid {
  width: 100%;
  max-width: 1040px;
  display: grid;
  grid-template-columns: repeat(3, minmax(220px, 1fr));
  gap: 18px;
  position: relative;
  z-index: 1;
}

.module-grid.grid-four {
  grid-template-columns: repeat(2, minmax(220px, 1fr));
  max-width: 900px;
}

@media (min-width: 1100px) {
  .module-grid.grid-four {
    grid-template-columns: repeat(4, minmax(200px, 1fr));
    max-width: 1120px;
  }
}

.module-card {
  padding: 24px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(9px);
  cursor: default;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background 0.22s ease;
}

.module-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.module-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(140deg, #8f2d56, #d7739a);
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 18px rgba(143, 45, 86, 0.25);
}

.module-caption {
  font-size: 12px;
  color: #6f7480;
}

.module-card h3 {
  margin: 0 0 10px;
  color: #4f2035;
}

.module-card p {
  margin: 0 0 16px;
  color: #4e5461;
  min-height: 70px;
}

.module-btn {
  background: linear-gradient(130deg, #7d2247, #be5e87);
  border: none;
}

.admin-card {
  border-color: rgba(108, 99, 255, 0.35);
}

.admin-icon {
  background: linear-gradient(140deg, #4f46e5, #7c3aed);
  box-shadow: 0 8px 18px rgba(79, 70, 229, 0.25);
}

.module-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: 0 14px 34px rgba(66, 16, 44, 0.22);
  border-color: rgba(172, 92, 128, 0.72);
  background: rgba(255, 255, 255, 0.45);
}

@media (max-width: 900px) {
  .module-grid {
    grid-template-columns: 1fr;
  }

  .module-grid.grid-four {
    grid-template-columns: 1fr;
  }
}
</style>
