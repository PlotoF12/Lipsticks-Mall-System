<script setup lang="ts">
import { onMounted, ref, computed, nextTick } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import * as faceapi from "face-api.js";
import http from "../api/http";
import type { LipstickProduct } from "../types/models";

const route = useRoute();

const modelsLoaded = ref(false);
const loadingModels = ref(true);
const processing = ref(false);
const uploadedImageUrl = ref<string | null>(null);
const resultImageUrl = ref<string | null>(null);
const uploadedImageEl = ref<HTMLImageElement | null>(null);
const canvasRef = ref<HTMLCanvasElement | null>(null);
const productId = ref<number | null>(null);
const product = ref<LipstickProduct | null>(null);
const detectedFaces = ref(0);
const lipDetected = ref(false);
const errorMsg = ref<string | null>(null);

const colorHex = computed(() => product.value?.colorHex || "#D4597B");

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

async function loadModels() {
  loadingModels.value = true;
  try {
    await faceapi.nets.ssdMobilenetv1.loadFromUri("/models");
    await faceapi.nets.faceLandmark68Net.loadFromUri("/models");
    modelsLoaded.value = true;
  } catch (e) {
    console.error("模型加载失败:", e);
    ElMessage.error("人脸识别模型加载失败，请刷新重试");
  } finally {
    loadingModels.value = false;
  }
}

async function loadProduct(id: number) {
  try {
    const res = await http.get<{ success: boolean; data: LipstickProduct }>(`/mall/products/${id}`);
    product.value = res.data.data;
  } catch {
    product.value = null;
  }
}

function getLipPoints(landmarks: faceapi.FaceLandmarks68): { outer: { x: number; y: number }[]; inner: { x: number; y: number }[] } {
  const positions = landmarks.positions;
  const outerLip = positions.slice(48, 60);
  const innerLip = positions.slice(60, 68);
  return { outer: outerLip, inner: innerLip };
}

function drawLipPath(
  ctx: CanvasRenderingContext2D,
  points: { x: number; y: number }[]
) {
  if (points.length < 3) return;
  ctx.beginPath();
  ctx.moveTo(points[0].x, points[0].y);
  for (let i = 1; i < points.length; i++) {
    const prev = points[i - 1];
    const curr = points[i];
    const cpx = (prev.x + curr.x) / 2;
    const cpy = (prev.y + curr.y) / 2;
    ctx.quadraticCurveTo(prev.x, prev.y, cpx, cpy);
  }
  const last = points[points.length - 1];
  const first = points[0];
  const cpx = (last.x + first.x) / 2;
  const cpy = (last.y + first.y) / 2;
  ctx.quadraticCurveTo(last.x, last.y, cpx, cpy);
  ctx.closePath();
}

function fillLipArea(
  ctx: CanvasRenderingContext2D,
  outerPoints: { x: number; y: number }[],
  innerPoints: { x: number; y: number }[],
  color: string
) {
  ctx.save();
  drawLipPath(ctx, outerPoints);
  drawLipPath(ctx, innerPoints);
  ctx.clip("evenodd");

  const xs = outerPoints.map((p) => p.x);
  const ys = outerPoints.map((p) => p.y);
  const minX = Math.min(...xs);
  const maxX = Math.max(...xs);
  const minY = Math.min(...ys);
  const maxY = Math.max(...ys);

  ctx.fillStyle = color;
  ctx.globalAlpha = 0.55;
  ctx.fillRect(minX - 2, minY - 2, maxX - minX + 4, maxY - minY + 4);

  ctx.globalAlpha = 0.3;
  ctx.filter = "blur(3px)";
  ctx.fillRect(minX - 4, minY - 4, maxX - minX + 8, maxY - minY + 8);

  ctx.restore();
}

async function processImage(file: File) {
  processing.value = true;
  errorMsg.value = null;
  detectedFaces.value = 0;
  lipDetected.value = false;
  resultImageUrl.value = null;

  if (uploadedImageUrl.value) {
    URL.revokeObjectURL(uploadedImageUrl.value);
  }
  uploadedImageUrl.value = URL.createObjectURL(file);

  await nextTick();

  const img = uploadedImageEl.value;
  if (!img) {
    processing.value = false;
    return;
  }

  await new Promise<void>((resolve) => {
    if (img.complete) resolve();
    else img.onload = () => resolve();
  });

  if (!modelsLoaded.value) {
    errorMsg.value = "人脸识别模型尚未加载完成，请稍候重试";
    processing.value = false;
    return;
  }

  try {
    const detections = await faceapi
      .detectAllFaces(img)
      .withFaceLandmarks();

    detectedFaces.value = detections.length;

    if (detections.length === 0) {
      errorMsg.value = "未识别到人脸，请上传包含清晰人脸的照片";
      processing.value = false;
      return;
    }

    const detection = detections[0];
    const landmarks = detection.landmarks;
    const lipPoints = getLipPoints(landmarks);

    const outerLip = lipPoints.outer;
    const innerLip = lipPoints.inner;

    if (outerLip.length < 12 || innerLip.length < 8) {
      errorMsg.value = "未识别到唇部，无法试色";
      processing.value = false;
      return;
    }

    const lipWidth = Math.max(...outerLip.map((p) => p.x)) - Math.min(...outerLip.map((p) => p.x));
    if (lipWidth < 10) {
      errorMsg.value = "未识别到唇部，无法试色";
      processing.value = false;
      return;
    }

    lipDetected.value = true;

    if (!productId.value) {
      errorMsg.value = "请先选择口红产品";
      processing.value = false;
      return;
    }

    const scaleX = img.naturalWidth / img.width;
    const scaleY = img.naturalHeight / img.height;

    const scaledOuterLip = outerLip.map((p) => ({ x: Math.round(p.x * scaleX), y: Math.round(p.y * scaleY) }));
    const scaledInnerLip = innerLip.map((p) => ({ x: Math.round(p.x * scaleX), y: Math.round(p.y * scaleY) }));

    const formData = new FormData();
    formData.append("file", file);
    formData.append("productId", String(productId.value));
    formData.append("outerLip", JSON.stringify(scaledOuterLip));
    formData.append("innerLip", JSON.stringify(scaledInnerLip));

    const res = await http.post<{ success: boolean; data: { input: string; output: string } }>("/tryon/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" }
    });

    if (res.data.data?.output) {
      resultImageUrl.value = "data:image/png;base64," + res.data.data.output;
    }
  } catch (e) {
    console.error("处理失败:", e);
    errorMsg.value = "图片处理失败，请重试";
  } finally {
    processing.value = false;
  }
}

function onFileChange(uploadFile: import("element-plus").UploadFile) {
  if (!uploadFile.raw) {
    ElMessage.warning("无法读取文件，请重试");
    return;
  }
  processImage(uploadFile.raw);
}

function downloadResult() {
  if (!resultImageUrl.value) return;
  const a = document.createElement("a");
  a.href = resultImageUrl.value;
  a.download = "tryon-result.png";
  a.click();
}

function resetTryOn() {
  if (uploadedImageUrl.value) {
    URL.revokeObjectURL(uploadedImageUrl.value);
  }
  uploadedImageUrl.value = null;
  resultImageUrl.value = null;
  errorMsg.value = null;
  detectedFaces.value = 0;
  lipDetected.value = false;
}

onMounted(async () => {
  const id = route.query.productId;
  if (id) {
    productId.value = Number(id);
    await loadProduct(productId.value);
  }
  await loadModels();
});
</script>

<template>
  <div class="tryon-page">
    <div class="tryon-header">
      <h1 class="tryon-title">口红试色</h1>
      <p class="tryon-subtitle">上传一张面部照片，AI 智能识别唇部并实时上色</p>
    </div>

    <div class="tryon-body">
      <div class="panel panel-left">
        <h3 class="panel-title">选择口红</h3>
        <div v-if="product" class="product-card">
          <div class="product-color-row">
            <div class="product-swatch" :style="{ background: product.colorHex || '#ccc' }" />
            <div class="product-meta">
              <span class="product-name">{{ product.title }}</span>
              <span class="product-brand">{{ product.brand }} · {{ product.shade }}</span>
              <span class="product-finish">{{ finishMap[product.finishType || ''] || product.finishType || '' }}</span>
            </div>
          </div>
          <div v-if="product.colorHex" class="product-hex">
            色值：<span class="hex-value">{{ product.colorHex }}</span>
          </div>
        </div>
        <div v-else class="no-product">
          <el-icon :size="32" color="#ccc"><i class="el-icon-lipstick" /></el-icon>
          <p>请从商品详情页点击「试妆体验」进入</p>
        </div>

        <el-divider />

        <h3 class="panel-title">上传照片</h3>
        <p class="upload-hint">请上传包含清晰人脸的正面照片，系统将自动识别唇部区域</p>

        <div class="upload-area">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            accept="image/*"
            :disabled="processing || loadingModels"
            :on-change="onFileChange"
            drag
          >
            <div class="upload-inner">
              <el-icon :size="40" color="#D4597B"><i class="el-icon-upload" /></el-icon>
              <p>点击或拖拽上传照片</p>
              <p class="upload-tip">支持 JPG / PNG，建议正面清晰照</p>
            </div>
          </el-upload>
        </div>

        <div v-if="loadingModels" class="model-status">
          <el-icon class="is-loading"><i class="el-icon-loading" /></el-icon>
          <span>人脸识别模型加载中...</span>
        </div>
        <div v-else-if="modelsLoaded" class="model-status ready">
          <span>✓ 人脸识别模型已就绪</span>
        </div>

        <div v-if="errorMsg" class="error-msg">
          <el-icon color="#f56c6c" :size="18"><i class="el-icon-warning" /></el-icon>
          <span>{{ errorMsg }}</span>
        </div>
      </div>

      <div class="panel panel-right">
        <h3 class="panel-title">试色效果</h3>

        <div v-if="processing" class="preview-area processing">
          <el-icon class="is-loading" :size="48" color="#D4597B"><i class="el-icon-loading" /></el-icon>
          <p>AI 正在识别并上色...</p>
        </div>

        <div v-else-if="resultImageUrl" class="preview-area has-result">
          <div class="compare-view">
            <div class="compare-item">
              <span class="compare-label">原图</span>
              <img :src="uploadedImageUrl!" alt="原图" class="compare-img" />
            </div>
            <div class="compare-item">
              <span class="compare-label">试色效果</span>
              <img :src="resultImageUrl" alt="试色效果" class="compare-img" />
            </div>
          </div>
          <div class="result-actions">
            <el-button type="primary" @click="resetTryOn">重新试色</el-button>
            <el-button @click="downloadResult">保存效果图</el-button>
          </div>
        </div>

        <div v-else class="preview-area empty">
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
              <circle cx="32" cy="32" r="30" stroke="#e0d0d8" stroke-width="2" stroke-dasharray="6 4" />
              <path d="M32 18c-2 0-4 1-5 3l-3 6c-1 2 0 4 1 5l7 8 7-8c1-1 2-3 1-5l-3-6c-1-2-3-3-5-3z" fill="#f0d0d8" stroke="#d4a0b0" stroke-width="1.5" />
            </svg>
          </div>
          <p>上传照片后，试色效果将在此展示</p>
        </div>
      </div>
    </div>

    <canvas ref="canvasRef" class="hidden-canvas" />
    <img ref="uploadedImageEl" :src="uploadedImageUrl || ''" class="hidden-img" crossorigin="anonymous" />
  </div>
</template>

<style scoped>
.tryon-page {
  max-width: 1100px;
  margin: 24px auto;
  padding: 0 20px;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.tryon-header {
  text-align: center;
  margin-bottom: 28px;
}

.tryon-title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #D4597B, #8B5CF6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.tryon-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #9b8fab;
}

.tryon-body {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.panel {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.panel-left {
  flex: 0 0 340px;
}

.panel-right {
  flex: 1;
  min-width: 0;
}

.panel-title {
  margin: 0 0 14px;
  font-size: 16px;
  font-weight: 700;
  color: #374151;
}

.product-card {
  background: linear-gradient(135deg, #fdf2f8, #f5f0ff);
  border-radius: 12px;
  padding: 16px;
}

.product-color-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.product-swatch {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  flex-shrink: 0;
}

.product-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.product-name {
  font-size: 15px;
  font-weight: 700;
  color: #374151;
}

.product-brand {
  font-size: 12px;
  color: #6b7280;
}

.product-finish {
  font-size: 11px;
  color: #9ca3af;
}

.product-hex {
  margin-top: 10px;
  font-size: 12px;
  color: #6b7280;
}

.hex-value {
  font-family: monospace;
  font-weight: 600;
  color: #D4597B;
}

.no-product {
  text-align: center;
  padding: 20px;
  color: #9ca3af;
  font-size: 13px;
}

.no-product p {
  margin: 8px 0 0;
}

.upload-hint {
  font-size: 12px;
  color: #9ca3af;
  margin: 0 0 12px;
  line-height: 1.6;
}

.upload-area {
  margin-bottom: 12px;
}

.upload-area :deep(.el-upload-dragger) {
  border-radius: 12px;
  border-color: #e5d0d8;
  background: #fdf8fa;
  padding: 20px;
}

.upload-area :deep(.el-upload-dragger:hover) {
  border-color: #D4597B;
}

.upload-inner {
  text-align: center;
}

.upload-inner p {
  margin: 8px 0 0;
  font-size: 14px;
  color: #6b7280;
}

.upload-tip {
  font-size: 12px !important;
  color: #9ca3af !important;
}

.model-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #9ca3af;
  margin-top: 8px;
}

.model-status.ready {
  color: #67c23a;
}

.error-msg {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 10px 14px;
  background: #fef0f0;
  border-radius: 8px;
  font-size: 13px;
  color: #f56c6c;
  line-height: 1.5;
}

.preview-area {
  min-height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border-radius: 12px;
  border: 2px dashed #e8e0e4;
}

.preview-area.processing {
  gap: 12px;
  color: #9ca3af;
  font-size: 14px;
}

.preview-area.empty {
  gap: 8px;
  color: #9ca3af;
  font-size: 13px;
}

.preview-area.has-result {
  background: transparent;
  border: none;
  min-height: auto;
  padding: 0;
}

.compare-view {
  display: flex;
  gap: 16px;
  width: 100%;
}

.compare-item {
  flex: 1;
  min-width: 0;
  text-align: center;
}

.compare-label {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 8px;
  padding: 2px 10px;
  background: #f3f4f6;
  border-radius: 10px;
}

.compare-img {
  width: 100%;
  max-height: 480px;
  object-fit: contain;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.result-actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
  justify-content: center;
}

.hidden-canvas {
  display: none;
}

.hidden-img {
  display: none;
}
</style>
