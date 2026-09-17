<script setup lang="ts">
import { onMounted, ref, nextTick, onUnmounted } from "vue";

interface LipstickEntry {
  id: string;
  name: string;
  color: string;
  brand: { name: string; series: { name: string; lipsticks: LipstickEntry[] }[] };
  series: { name: string; lipsticks: LipstickEntry[] };
  _hsl?: { h: number; s: number; l: number };
  group?: any;
}

const canvasRef = ref<HTMLCanvasElement | null>(null);
const bgCanvasRef = ref<HTMLCanvasElement | null>(null);
const containerRef = ref<HTMLDivElement | null>(null);
const hoveredLipstick = ref<LipstickEntry | null>(null);
const showUi = ref(false);
const seriesColors = ref<{ color: string; active: boolean }[]>([]);
const loading = ref(true);

const bgDpi = 0.25;
const zrDpi = 1;
let width = 0;
let height = 0;
let lipstickData: LipstickEntry[] = [];
let minMax = { minHue: 0, maxHue: 0, minLight: 0, maxLight: 0 };
let dataPoints: { x: number; y: number; lipstick: LipstickEntry }[] = [];
let animFrameId = 0;

const brandDisplayMap: Record<string, string> = {
  "圣罗兰": "YSL",
  "香奈儿可可小姐": "Chanel",
  "迪奥": "Dior",
  "美宝莲": "Maybelline",
  "纪梵希": "Givenchy",
  "兰蔻": "Lancôme"
};

function getBrandDisplayName(brandName: string): string {
  return brandDisplayMap[brandName] || brandName;
}

function updateLipstickData(rawData: { brands: { name: string; series: { name: string; lipsticks: { id: string; name: string; color: string }[] }[] }[] }) {
  lipstickData = [];
  for (let bid = 0; bid < rawData.brands.length; bid++) {
    const brand = rawData.brands[bid];
    for (let sid = 0; sid < brand.series.length; sid++) {
      const series = brand.series[sid];
      const lipsticks = series.lipsticks as LipstickEntry[];
      lipstickData = lipstickData.concat(lipsticks);
      for (let lid = 0; lid < lipsticks.length; lid++) {
        lipsticks[lid].series = series as unknown as LipstickEntry['series'];
        lipsticks[lid].brand = brand as unknown as LipstickEntry['brand'];
      }
    }
  }
}

function hexToHsl(hex: string): { h: number; s: number; l: number } {
  let r = parseInt(hex.slice(1, 3), 16) / 255;
  let g = parseInt(hex.slice(3, 5), 16) / 255;
  let b = parseInt(hex.slice(5, 7), 16) / 255;
  const max = Math.max(r, g, b);
  const min = Math.min(r, g, b);
  let h = 0, s = 0;
  const l = (max + min) / 2;
  if (max !== min) {
    const d = max - min;
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
    switch (max) {
      case r: h = ((g - b) / d + (g < b ? 6 : 0)) / 6; break;
      case g: h = ((b - r) / d + 2) / 6; break;
      case b: h = ((r - g) / d + 4) / 6; break;
    }
  }
  return { h: h * 360, s: s * 100, l: l * 100 };
}

function encodeHue(hue: number): number {
  if (hue < 180) return 180 - hue;
  else return 540 - hue;
}

function getMinMax(data: LipstickEntry[]) {
  let minHue = Number.MAX_VALUE;
  let maxHue = Number.MIN_VALUE;
  let minLight = Number.MAX_VALUE;
  let maxLight = Number.MIN_VALUE;
  for (let i = 0; i < data.length; i++) {
    const hsl = hexToHsl(data[i].color);
    data[i]._hsl = hsl;
    const hue = encodeHue(hsl.h);
    if (hue < 165 || hue > 220) continue;
    if (hue > maxHue) maxHue = hue;
    if (hue < minHue) minHue = hue;
    if (hsl.l > maxLight) maxLight = hsl.l;
    if (hsl.l < minLight) minLight = hsl.l;
  }
  return {
    minHue: minHue - 2,
    maxHue: maxHue + 2,
    minLight: Math.max(minLight - 10, 0),
    maxLight: Math.min(maxLight + 5, 100)
  };
}

function getDataCoord(data: LipstickEntry, mm: typeof minMax) {
  const hue = encodeHue(data._hsl!.h);
  const light = data._hsl!.l;
  return {
    x: (hue - mm.minHue) * width / (mm.maxHue - mm.minHue) / bgDpi,
    y: height / bgDpi - (light - mm.minLight) * height / (mm.maxLight - mm.minLight) / bgDpi
  };
}

function hslToRgb(h: number, s: number, l: number): [number, number, number] {
  h /= 360; s /= 100; l /= 100;
  let r, g, b;
  if (s === 0) {
    r = g = b = l;
  } else {
    const hue2rgb = (p: number, q: number, t: number) => {
      if (t < 0) t += 1;
      if (t > 1) t -= 1;
      if (t < 1/6) return p + (q - p) * 6 * t;
      if (t < 1/2) return q;
      if (t < 2/3) return p + (q - p) * (2/3 - t) * 6;
      return p;
    };
    const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
    const p = 2 * l - q;
    r = hue2rgb(p, q, h + 1/3);
    g = hue2rgb(p, q, h);
    b = hue2rgb(p, q, h - 1/3);
  }
  return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)];
}

function renderBackground(bgDom: HTMLCanvasElement, mm: typeof minMax) {
  const container = containerRef.value;
  if (!container) return;
  const cw = container.clientWidth;
  const ch = container.clientHeight;
  const w = Math.floor(cw * bgDpi);
  const h = Math.floor(ch * bgDpi);
  bgDom.width = w;
  bgDom.height = h;
  bgDom.style.width = cw + 'px';
  bgDom.style.height = ch + 'px';

  const ctx = bgDom.getContext('2d')!;
  const imgData = ctx.createImageData(w, h);
  const d = imgData.data;
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const light = (h - y) / h * (mm.maxLight - mm.minLight) + mm.minLight;
      const hue = x / w * (mm.maxHue - mm.minHue) + mm.minHue;
      const rgb = hslToRgb(encodeHue(hue), 80, light);
      const id = (y * w + x) * 4;
      d[id] = rgb[0];
      d[id + 1] = rgb[1];
      d[id + 2] = rgb[2];
      d[id + 3] = 255;
    }
  }
  ctx.putImageData(imgData, 0, 0);
}

function renderDataPoints(canvas: HTMLCanvasElement, data: LipstickEntry[], mm: typeof minMax) {
  const container = containerRef.value;
  if (!container) return;
  const cw = container.clientWidth;
  const ch = container.clientHeight;
  const w = Math.floor(cw * zrDpi);
  const h = Math.floor(ch * zrDpi);
  canvas.width = w;
  canvas.height = h;
  canvas.style.width = cw + 'px';
  canvas.style.height = ch + 'px';

  const ctx = canvas.getContext('2d')!;
  ctx.clearRect(0, 0, w, h);
  dataPoints = [];
  for (let i = 0; i < data.length; i++) {
    const coord = getDataCoord(data[i], mm);
    const px = coord.x * zrDpi;
    const py = coord.y * zrDpi;
    dataPoints.push({ x: px, y: py, lipstick: data[i] });

    ctx.beginPath();
    ctx.arc(px, py, 5, 0, Math.PI * 2);
    ctx.fillStyle = data[i].color;
    ctx.fill();
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
    ctx.lineWidth = 1;
    ctx.stroke();

    ctx.font = '12px "PingFang SC", "Microsoft YaHei", sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillStyle = 'rgba(255, 255, 255, 0.5)';
    ctx.fillText(data[i].name, px, py + 16);
  }
}

function findClosestPoint(mx: number, my: number): number {
  let closest = -1;
  let minDist = 20;
  for (let i = 0; i < dataPoints.length; i++) {
    const dx = mx * zrDpi - dataPoints[i].x;
    const dy = my * zrDpi - dataPoints[i].y;
    const dist = Math.sqrt(dx * dx + dy * dy);
    if (dist < minDist) {
      minDist = dist;
      closest = i;
    }
  }
  return closest;
}

function redrawCanvas(canvas: HTMLCanvasElement, emphasisIdx: number) {
  const ctx = canvas.getContext('2d')!;
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  const emphasisLipstick = emphasisIdx >= 0 ? dataPoints[emphasisIdx].lipstick : null;
  const siblings = emphasisLipstick ? emphasisLipstick.series.lipsticks : [];

  for (let i = 0; i < dataPoints.length; i++) {
    const dp = dataPoints[i];
    const isEmphasis = emphasisLipstick && dp.lipstick === emphasisLipstick;
    const isRelated = emphasisLipstick && siblings.includes(dp.lipstick) && !isEmphasis;
    const isDownplay = emphasisLipstick && !isEmphasis && !isRelated;

    ctx.save();
    ctx.beginPath();

    if (isEmphasis) {
      ctx.arc(dp.x, dp.y, 30, 0, Math.PI * 2);
      ctx.fillStyle = dp.lipstick.color;
      ctx.fill();
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 3;
      ctx.stroke();
      ctx.shadowBlur = 20;
      ctx.shadowColor = 'rgba(0, 0, 0, 0.4)';

      ctx.font = 'bold 16px "PingFang SC", "Microsoft YaHei", sans-serif';
      ctx.textAlign = 'left';
      ctx.textBaseline = 'middle';
      ctx.fillStyle = dp.lipstick.color;
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 3;
      ctx.strokeText('#' + dp.lipstick.id + ' ' + dp.lipstick.name, dp.x + 35, dp.y);
      ctx.fillText('#' + dp.lipstick.id + ' ' + dp.lipstick.name, dp.x + 35, dp.y);
    } else if (isRelated) {
      ctx.arc(dp.x, dp.y, 10, 0, Math.PI * 2);
      ctx.fillStyle = dp.lipstick.color;
      ctx.fill();
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 2;
      ctx.stroke();
      ctx.shadowBlur = 8;
      ctx.shadowColor = 'rgba(0, 0, 0, 0.2)';

      ctx.font = '13px "PingFang SC", "Microsoft YaHei", sans-serif';
      ctx.textAlign = 'left';
      ctx.textBaseline = 'middle';
      ctx.fillStyle = dp.lipstick.color;
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.75)';
      ctx.lineWidth = 2;
      ctx.strokeText('#' + dp.lipstick.id + ' ' + dp.lipstick.name, dp.x + 14, dp.y);
      ctx.fillText('#' + dp.lipstick.id + ' ' + dp.lipstick.name, dp.x + 14, dp.y);
    } else if (isDownplay) {
      ctx.globalAlpha = 0.6;
      ctx.arc(dp.x, dp.y, 5, 0, Math.PI * 2);
      ctx.fillStyle = dp.lipstick.color;
      ctx.fill();
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
      ctx.lineWidth = 1;
      ctx.stroke();
    } else {
      ctx.arc(dp.x, dp.y, 5, 0, Math.PI * 2);
      ctx.fillStyle = dp.lipstick.color;
      ctx.fill();
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
      ctx.lineWidth = 1;
      ctx.stroke();

      ctx.font = '12px "PingFang SC", "Microsoft YaHei", sans-serif';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillStyle = 'rgba(255, 255, 255, 0.5)';
      ctx.fillText(dp.lipstick.name, dp.x, dp.y + 16);
    }
    ctx.restore();
  }
}

function updateUi(lipstick: LipstickEntry) {
  hoveredLipstick.value = lipstick;
  seriesColors.value = lipstick.series.lipsticks.map((l: LipstickEntry) => ({
    color: l.color,
    active: l === lipstick
  }));
}

function handleMouseMove(e: MouseEvent) {
  const canvas = canvasRef.value;
  if (!canvas) return;
  const rect = canvas.getBoundingClientRect();
  const mx = e.clientX - rect.left;
  const my = e.clientY - rect.top;
  const idx = findClosestPoint(mx, my);
  if (idx >= 0) {
    const lipstick = dataPoints[idx].lipstick;
    redrawCanvas(canvas, idx);
    updateUi(lipstick);
    showUi.value = true;
    canvas.style.cursor = 'pointer';
  } else {
    if (hoveredLipstick.value) {
      hoveredLipstick.value = null;
      redrawCanvas(canvas, -1);
      showUi.value = false;
    }
    canvas.style.cursor = 'default';
  }
}

function handleClick() {
  if (!hoveredLipstick.value) {
    redrawCanvas(canvasRef.value!, -1);
    showUi.value = false;
  }
}

async function init() {
  loading.value = true;
  try {
    const response = await fetch('/lipstick.json');
    const rawData = await response.json();
    const bgCanvas = bgCanvasRef.value!;
    const zrCanvas = canvasRef.value!;
    width = Math.floor(containerRef.value!.clientWidth * bgDpi);
    height = Math.floor(containerRef.value!.clientHeight * bgDpi);
    updateLipstickData(rawData);
    minMax = getMinMax(lipstickData);
    renderBackground(bgCanvas, minMax);
    renderDataPoints(zrCanvas, lipstickData, minMax);
    updateUi(lipstickData[0]);
    showUi.value = true;
  } catch (e) {
    console.error('加载数据失败:', e);
  } finally {
    loading.value = false;
  }
}

function handleResize() {
  cancelAnimationFrame(animFrameId);
  animFrameId = requestAnimationFrame(() => {
    if (lipstickData.length > 0) {
      const bgCanvas = bgCanvasRef.value!;
      const zrCanvas = canvasRef.value!;
      width = Math.floor(containerRef.value!.clientWidth * bgDpi);
      height = Math.floor(containerRef.value!.clientHeight * bgDpi);
      minMax = getMinMax(lipstickData);
      renderBackground(bgCanvas, minMax);
      renderDataPoints(zrCanvas, lipstickData, minMax);
    }
  });
}

onMounted(() => {
  nextTick(() => {
    init();
    window.addEventListener('resize', handleResize);
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  cancelAnimationFrame(animFrameId);
});
</script>

<template>
  <div class="vis-page">
    <div class="vis-intro">
      <h1 class="intro-title">口红颜色可视化</h1>
      <p class="intro-desc">
        将六大品牌的数百款口红色号映射到色相-亮度二维空间中，每一颗圆点代表一支口红。
        横轴表示色相（从暖色到冷色），纵轴表示亮度（从明亮到暗沉）。
        悬停任意色号可查看品牌、系列及同系列色板，探索属于你的那一抹色彩。
      </p>
    </div>
    <div ref="containerRef" class="vis-container">
      <div v-if="loading" class="vis-loading">
        <div class="loading-spinner" />
        <span>加载口红色号数据中...</span>
      </div>
      <canvas ref="bgCanvasRef" id="bg" class="vis-canvas bg-canvas" />
      <canvas
        ref="canvasRef"
        id="zr"
        class="vis-canvas zr-canvas"
        @mousemove="handleMouseMove"
        @click="handleClick"
      />

      <div v-if="showUi && hoveredLipstick" id="ui" class="vis-ui">
        <div class="ui-header">
          <span class="ui-brand">{{ getBrandDisplayName(hoveredLipstick.brand.name) }}</span>
          <span class="ui-series">{{ hoveredLipstick.series.name }}</span>
        </div>
        <div class="ui-colors">
          <div
            v-for="(sc, idx) in seriesColors"
            :key="idx"
            class="series-color"
            :class="{ active: sc.active }"
            :style="{ backgroundColor: sc.color }"
          />
        </div>
        <div class="ui-lipstick-info" :style="{ color: hoveredLipstick.color }">
          <span class="ui-id">#{{ hoveredLipstick.id }}</span>
          <span class="ui-name">{{ hoveredLipstick.name }}</span>
        </div>
      </div>

      <div class="vis-info">
        <div class="vis-axis-hint">
          <span class="axis-label">← 暖色</span>
          <span class="axis-divider">色相</span>
          <span class="axis-label">冷色 →</span>
        </div>
        <div class="vis-axis-vhint">
          <span class="axis-label">亮</span>
          <span class="axis-divider">亮度</span>
          <span class="axis-label">暗</span>
        </div>
      </div>

      <div class="vis-brand-legend">
        <div
          v-for="brandName in Object.keys(brandDisplayMap)"
          :key="brandName"
          class="legend-item"
        >
          <span class="legend-brand">{{ brandDisplayMap[brandName] }}</span>
          <span class="legend-cn">{{ brandName }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.vis-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 32px;
  min-height: calc(100vh - 120px);
  background: linear-gradient(135deg, #fdf2f8 0%, #f5f0ff 50%, #ede9fe 100%);
}

.vis-intro {
  max-width: 1400px;
  width: 100%;
  text-align: center;
  margin-bottom: 20px;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.intro-title {
  margin: 0;
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #D4597B 0%, #8B5CF6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 3px;
}

.intro-desc {
  margin: 12px auto 0;
  max-width: 720px;
  font-size: 14px;
  line-height: 1.8;
  color: #9b8fab;
}

.vis-container {
  position: relative;
  width: 100%;
  max-width: 1400px;
  height: calc(100vh - 240px);
  min-height: 520px;
  overflow: hidden;
  border-radius: 24px;
  box-shadow:
    0 20px 60px rgba(139, 92, 246, 0.12),
    0 8px 24px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: #1a1a2e;
}

.vis-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 16px;
  z-index: 20;
  background: rgba(26, 26, 46, 0.9);
  color: rgba(255, 255, 255, 0.7);
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: 14px;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(255, 255, 255, 0.15);
  border-top-color: #D4597B;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.vis-canvas {
  position: absolute;
  top: 0;
  left: 0;
  border-radius: 24px;
}

.bg-canvas {
  z-index: 0;
}

.zr-canvas {
  z-index: 1;
}

.vis-ui {
  position: absolute;
  top: 24px;
  left: 24px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 16px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 10;
  min-width: 200px;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.vis-ui:hover {
  transform: translateY(-1px);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.15),
    0 4px 12px rgba(0, 0, 0, 0.08);
}

.ui-header {
  margin-bottom: 12px;
  display: flex;
  align-items: baseline;
  gap: 8px;
  flex-wrap: wrap;
}

.ui-brand {
  font-size: 22px;
  font-weight: 700;
  color: #D4597B;
  letter-spacing: 0.5px;
}

.ui-series {
  font-size: 14px;
  color: #888;
  font-weight: 400;
}

.ui-colors {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
  margin-bottom: 14px;
  max-width: 280px;
}

.series-color {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1), border-color 0.2s;
  cursor: pointer;
}

.series-color.active {
  transform: scale(1.35);
  border-color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.25);
}

.ui-lipstick-info {
  font-size: 25px;
  font-weight: 700;
  line-height: 1.3;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.ui-id {
  margin-right: 6px;
  font-weight: 600;
}

.ui-name {
  font-weight: 700;
}

.vis-info {
  position: absolute;
  right: 24px;
  bottom: 24px;
  text-align: right;
  z-index: 10;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  pointer-events: none;
}

.vis-axis-hint {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
}

.vis-axis-vhint {
  position: absolute;
  right: -20px;
  top: 50%;
  transform: translateY(-50%) rotate(90deg);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  white-space: nowrap;
}

.axis-label {
  font-weight: 500;
}

.axis-divider {
  color: rgba(255, 255, 255, 0.25);
  font-size: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  padding: 1px 6px;
}

.vis-brand-legend {
  position: absolute;
  left: 24px;
  bottom: 24px;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 4px;
  pointer-events: none;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.legend-item {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.legend-brand {
  font-size: 12px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.65);
}

.legend-cn {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.35);
}
</style>
