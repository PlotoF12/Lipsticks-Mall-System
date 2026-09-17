<script setup lang="ts">
import { onMounted, ref, watch, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import http from "../api/http";
import type { LipstickProduct } from "../types/models";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const product = ref<LipstickProduct | null>(null);

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

const finishMap: Record<string, string> = {
  matte: "哑光",
  gloss: "滋润",
  satin: "缎面"
};

const carouselImages = computed(() => {
  if (!product.value) return [];
  const id = product.value.id;
  const images: string[] = [];
  for (let i = 1; i <= 5; i++) {
    images.push(`/images/products/${id}/${i}.png`);
  }
  return images;
});

async function load(id: string) {
  loading.value = true;
  product.value = null;
  try {
    const res = await http.get<{ success: boolean; data: LipstickProduct }>(`/mall/products/${id}`);
    product.value = res.data.data;
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : "加载失败");
  } finally {
    loading.value = false;
  }
}

function goTryOn() {
  if (product.value) {
    router.push({ path: "/user/tryon", query: { productId: String(product.value.id) } });
  }
}

function handleImageError(e: Event) {
  const img = e.target as HTMLImageElement;
  img.style.display = "none";
}

onMounted(() => load(String(route.params.id)));
watch(
  () => route.params.id,
  (id) => load(String(id))
);
</script>

<template>
  <el-card v-loading="loading">
    <template v-if="product">
      <div class="product-header">
        <div class="product-info">
          <h2>{{ product.title }}</h2>
          <p class="sub">{{ product.brand }} · {{ product.shade }}</p>
          <p class="price">¥ {{ product.price }}</p>
        </div>
        <div class="carousel-section">
          <el-carousel height="280px" indicator-position="outside">
            <el-carousel-item v-for="(img, idx) in carouselImages" :key="idx">
              <div class="carousel-img-wrap">
                <img
                  :src="img"
                  :alt="`商品图片 ${idx + 1}`"
                  class="carousel-img"
                  @error="handleImageError"
                />
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
        <div v-if="product.colorHex" class="color-preview">
          <div class="color-circle" :style="{ background: product.colorHex }" />
          <span class="color-hex">{{ product.colorHex }}</span>
        </div>
      </div>

      <el-divider />

      <el-descriptions :column="2" border>
        <el-descriptions-item label="品牌">{{ product.brand || "-" }}</el-descriptions-item>
        <el-descriptions-item label="色号">{{ product.shade || "-" }}</el-descriptions-item>
        <el-descriptions-item label="色系">{{ categoryMap[product.category || ''] || product.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="质地">{{ finishMap[product.finishType || ''] || product.finishType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ product.price }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ product.stock }}</el-descriptions-item>
        <el-descriptions-item label="适合肤色">{{ product.suitableSkinTone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="适合性别">{{ product.suitableGender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="适用场景" :span="2">{{ product.scene || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <div class="detail-section">
        <h3>商品详情</h3>
        <p class="detail">{{ product.detail || "暂无详情" }}</p>
      </div>

      <div class="action-bar">
        <el-button type="primary" size="large" @click="goTryOn">试妆体验</el-button>
        <el-button size="large" @click="router.push('/user/mall')">返回商城</el-button>
      </div>
    </template>
    <template v-else-if="!loading">
      <el-empty description="未找到商品" />
    </template>
  </el-card>
</template>

<style scoped>
.carousel-section {
  flex: 1;
  min-width: 0;
  max-width: 400px;
  border-radius: 12px;
  overflow: hidden;
  background: #f9fafb;
}

.carousel-img-wrap {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
}

.product-info {
  flex: 0 0 220px;
}

.sub {
  color: #6b7280;
  margin: 0;
}

.price {
  font-size: 24px;
  font-weight: 700;
  color: #7b2146;
  margin: 12px 0 0;
}

.color-preview {
  flex: 0 0 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.color-circle {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  border: 2px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.color-hex {
  font-size: 12px;
  color: #6b7280;
}

.detail-section h3 {
  margin: 0 0 8px;
  color: #374151;
}

.detail {
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
  margin: 0;
}

.action-bar {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
</style>
