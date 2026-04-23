<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import http from "../api/http";
import type { LipstickProduct } from "../types/models";

const router = useRouter();
const loading = ref(false);
const products = ref<LipstickProduct[]>([]);
const brands = ref<string[]>([]);
const categories = ref<string[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(6);

const keyword = ref("");
const selectedBrand = ref("");
const selectedCategory = ref("");

const categoryMap: Record<string, string> = {
  red: "正红",
  red_brown: "红棕",
  tomato: "番茄红",
  bean_paste: "豆沙",
  pink: "粉色",
  rose: "玫瑰",
  rose_brown: "玫瑰棕",
  nude: "裸色"
};

const totalPages = computed(() => Math.ceil(total.value / pageSize.value));

async function loadFilters() {
  try {
    const [brandsRes, categoriesRes] = await Promise.all([
      http.get<{ success: boolean; data: string[] }>("/mall/brands"),
      http.get<{ success: boolean; data: string[] }>("/mall/categories")
    ]);
    brands.value = brandsRes.data.data;
    categories.value = categoriesRes.data.data;
  } catch {
    /* ignore */
  }
}

async function load() {
  loading.value = true;
  try {
    const params: Record<string, string | number> = {
      page: currentPage.value,
      size: pageSize.value
    };
    if (keyword.value.trim()) params.keyword = keyword.value.trim();
    if (selectedBrand.value) params.brand = selectedBrand.value;
    if (selectedCategory.value) params.category = selectedCategory.value;

    const res = await http.get("/mall/products/page", { params });
    const pageData = res.data.data;
    products.value = pageData.records;
    total.value = pageData.total;
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : "加载失败");
  } finally {
    loading.value = false;
  }
}

function handlePageChange(page: number) {
  currentPage.value = page;
  load();
}

function handleSearch() {
  currentPage.value = 1;
  load();
}

function resetFilters() {
  keyword.value = "";
  selectedBrand.value = "";
  selectedCategory.value = "";
  currentPage.value = 1;
  load();
}

function goDetail(id: number) {
  router.push(`/user/product/${id}`);
}

function getCategoryLabel(key: string | null): string {
  if (!key) return "";
  return categoryMap[key] || key;
}

function truncate(text: string | null, len: number): string {
  if (!text) return "";
  return text.length > len ? text.slice(0, len) + "..." : text;
}

onMounted(() => {
  loadFilters();
  load();
});
</script>

<template>
  <section class="mall-wrap">
    <div class="bg-orb orb-left"></div>
    <div class="bg-orb orb-right"></div>

    <div class="mall-header">
      <h2>口红商城</h2>
      <p>浏览品牌、色号与价格，支持按关键词、品牌和色系筛选</p>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品名称/品牌/色号"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="selectedBrand" placeholder="品牌" clearable style="width: 140px" @change="handleSearch">
        <el-option v-for="b in brands" :key="b" :label="b" :value="b" />
      </el-select>
      <el-select v-model="selectedCategory" placeholder="色系" clearable style="width: 140px" @change="handleSearch">
        <el-option v-for="c in categories" :key="c" :label="getCategoryLabel(c)" :value="c" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div v-loading="loading" class="product-grid">
      <div v-for="item in products" :key="item.id" class="product-card">
        <div class="card-color-area">
          <span v-if="item.colorHex" class="color-circle" :style="{ background: item.colorHex }" />
          <span v-else class="color-circle color-circle-empty" />
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ item.title }}</h3>
          <div class="card-meta">
            <span class="meta-brand">{{ item.brand }}</span>
            <span class="meta-divider">·</span>
            <span class="meta-category">{{ getCategoryLabel(item.category) }}</span>
          </div>
          <p class="card-desc">{{ truncate(item.detail, 50) }}</p>
          <div class="card-footer">
            <span class="card-price">¥{{ item.price }}</span>
            <el-button class="detail-btn" type="primary" size="small" @click="goDetail(item.id)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && products.length === 0" description="暂无商品" class="empty-state" />
    </div>

    <div v-if="total > 0" class="pagination-area">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </section>
</template>

<style scoped>
.mall-wrap {
  min-height: calc(100vh - 120px);
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

.mall-header {
  text-align: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.mall-header h2 {
  margin: 0 0 8px;
  font-size: 32px;
  color: #512337;
}

.mall-header p {
  margin: 0;
  color: #4d525d;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  max-width: 1040px;
  margin: 0 auto;
  min-height: 200px;
  position: relative;
  z-index: 1;
}

.product-card {
  padding: 24px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(9px);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background 0.22s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.product-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: 0 14px 34px rgba(66, 16, 44, 0.22);
  border-color: rgba(172, 92, 128, 0.72);
  background: rgba(255, 255, 255, 0.45);
}

.card-color-area {
  margin-bottom: 14px;
}

.color-circle {
  display: inline-block;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
}

.color-circle-empty {
  background: #e5e7eb;
}

.card-body {
  width: 100%;
  text-align: center;
}

.card-title {
  margin: 0 0 8px;
  font-size: 16px;
  color: #4f2035;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  font-size: 13px;
  color: #6f7480;
  margin-bottom: 8px;
}

.meta-divider {
  margin: 0 4px;
}

.card-desc {
  font-size: 13px;
  color: #4e5461;
  margin: 0 0 14px;
  min-height: 40px;
  line-height: 1.5;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.card-price {
  font-size: 20px;
  font-weight: 700;
  color: #c0392b;
}

.detail-btn {
  background: linear-gradient(130deg, #7d2247, #be5e87);
  border: none;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 28px;
  position: relative;
  z-index: 1;
}

.empty-state {
  grid-column: 1 / -1;
}

@media (max-width: 900px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
