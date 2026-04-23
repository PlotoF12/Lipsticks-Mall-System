import { onMounted, ref, nextTick, onUnmounted } from "vue";
const canvasRef = ref(null);
const bgCanvasRef = ref(null);
const containerRef = ref(null);
const hoveredLipstick = ref(null);
const showUi = ref(false);
const seriesColors = ref([]);
const loading = ref(true);
const bgDpi = 0.25;
const zrDpi = 1;
let width = 0;
let height = 0;
let lipstickData = [];
let minMax = { minHue: 0, maxHue: 0, minLight: 0, maxLight: 0 };
let dataPoints = [];
let animFrameId = 0;
const brandDisplayMap = {
    "圣罗兰": "YSL",
    "香奈儿可可小姐": "Chanel",
    "迪奥": "Dior",
    "美宝莲": "Maybelline",
    "纪梵希": "Givenchy",
    "兰蔻": "Lancôme"
};
function getBrandDisplayName(brandName) {
    return brandDisplayMap[brandName] || brandName;
}
function updateLipstickData(rawData) {
    lipstickData = [];
    for (let bid = 0; bid < rawData.brands.length; bid++) {
        const brand = rawData.brands[bid];
        for (let sid = 0; sid < brand.series.length; sid++) {
            const series = brand.series[sid];
            const lipsticks = series.lipsticks;
            lipstickData = lipstickData.concat(lipsticks);
            for (let lid = 0; lid < lipsticks.length; lid++) {
                lipsticks[lid].series = series;
                lipsticks[lid].brand = brand;
            }
        }
    }
}
function hexToHsl(hex) {
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
            case r:
                h = ((g - b) / d + (g < b ? 6 : 0)) / 6;
                break;
            case g:
                h = ((b - r) / d + 2) / 6;
                break;
            case b:
                h = ((r - g) / d + 4) / 6;
                break;
        }
    }
    return { h: h * 360, s: s * 100, l: l * 100 };
}
function encodeHue(hue) {
    if (hue < 180)
        return 180 - hue;
    else
        return 540 - hue;
}
function getMinMax(data) {
    let minHue = Number.MAX_VALUE;
    let maxHue = Number.MIN_VALUE;
    let minLight = Number.MAX_VALUE;
    let maxLight = Number.MIN_VALUE;
    for (let i = 0; i < data.length; i++) {
        const hsl = hexToHsl(data[i].color);
        data[i]._hsl = hsl;
        const hue = encodeHue(hsl.h);
        if (hue < 165 || hue > 220)
            continue;
        if (hue > maxHue)
            maxHue = hue;
        if (hue < minHue)
            minHue = hue;
        if (hsl.l > maxLight)
            maxLight = hsl.l;
        if (hsl.l < minLight)
            minLight = hsl.l;
    }
    return {
        minHue: minHue - 2,
        maxHue: maxHue + 2,
        minLight: Math.max(minLight - 10, 0),
        maxLight: Math.min(maxLight + 5, 100)
    };
}
function getDataCoord(data, mm) {
    const hue = encodeHue(data._hsl.h);
    const light = data._hsl.l;
    return {
        x: (hue - mm.minHue) * width / (mm.maxHue - mm.minHue) / bgDpi,
        y: height / bgDpi - (light - mm.minLight) * height / (mm.maxLight - mm.minLight) / bgDpi
    };
}
function hslToRgb(h, s, l) {
    h /= 360;
    s /= 100;
    l /= 100;
    let r, g, b;
    if (s === 0) {
        r = g = b = l;
    }
    else {
        const hue2rgb = (p, q, t) => {
            if (t < 0)
                t += 1;
            if (t > 1)
                t -= 1;
            if (t < 1 / 6)
                return p + (q - p) * 6 * t;
            if (t < 1 / 2)
                return q;
            if (t < 2 / 3)
                return p + (q - p) * (2 / 3 - t) * 6;
            return p;
        };
        const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        const p = 2 * l - q;
        r = hue2rgb(p, q, h + 1 / 3);
        g = hue2rgb(p, q, h);
        b = hue2rgb(p, q, h - 1 / 3);
    }
    return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)];
}
function renderBackground(bgDom, mm) {
    const container = containerRef.value;
    if (!container)
        return;
    const cw = container.clientWidth;
    const ch = container.clientHeight;
    const w = Math.floor(cw * bgDpi);
    const h = Math.floor(ch * bgDpi);
    bgDom.width = w;
    bgDom.height = h;
    bgDom.style.width = cw + 'px';
    bgDom.style.height = ch + 'px';
    const ctx = bgDom.getContext('2d');
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
function renderDataPoints(canvas, data, mm) {
    const container = containerRef.value;
    if (!container)
        return;
    const cw = container.clientWidth;
    const ch = container.clientHeight;
    const w = Math.floor(cw * zrDpi);
    const h = Math.floor(ch * zrDpi);
    canvas.width = w;
    canvas.height = h;
    canvas.style.width = cw + 'px';
    canvas.style.height = ch + 'px';
    const ctx = canvas.getContext('2d');
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
function findClosestPoint(mx, my) {
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
function redrawCanvas(canvas, emphasisIdx) {
    const ctx = canvas.getContext('2d');
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
        }
        else if (isRelated) {
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
        }
        else if (isDownplay) {
            ctx.globalAlpha = 0.6;
            ctx.arc(dp.x, dp.y, 5, 0, Math.PI * 2);
            ctx.fillStyle = dp.lipstick.color;
            ctx.fill();
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
            ctx.lineWidth = 1;
            ctx.stroke();
        }
        else {
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
function updateUi(lipstick) {
    hoveredLipstick.value = lipstick;
    seriesColors.value = lipstick.series.lipsticks.map((l) => ({
        color: l.color,
        active: l === lipstick
    }));
}
function handleMouseMove(e) {
    const canvas = canvasRef.value;
    if (!canvas)
        return;
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
    }
    else {
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
        redrawCanvas(canvasRef.value, -1);
        showUi.value = false;
    }
}
async function init() {
    loading.value = true;
    try {
        const response = await fetch('/lipstick.json');
        const rawData = await response.json();
        const bgCanvas = bgCanvasRef.value;
        const zrCanvas = canvasRef.value;
        width = Math.floor(containerRef.value.clientWidth * bgDpi);
        height = Math.floor(containerRef.value.clientHeight * bgDpi);
        updateLipstickData(rawData);
        minMax = getMinMax(lipstickData);
        renderBackground(bgCanvas, minMax);
        renderDataPoints(zrCanvas, lipstickData, minMax);
        updateUi(lipstickData[0]);
        showUi.value = true;
    }
    catch (e) {
        console.error('加载数据失败:', e);
    }
    finally {
        loading.value = false;
    }
}
function handleResize() {
    cancelAnimationFrame(animFrameId);
    animFrameId = requestAnimationFrame(() => {
        if (lipstickData.length > 0) {
            const bgCanvas = bgCanvasRef.value;
            const zrCanvas = canvasRef.value;
            width = Math.floor(containerRef.value.clientWidth * bgDpi);
            height = Math.floor(containerRef.value.clientHeight * bgDpi);
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
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['vis-ui']} */ ;
/** @type {__VLS_StyleScopedClasses['series-color']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "vis-page" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ref: "containerRef",
    ...{ class: "vis-container" },
});
/** @type {typeof __VLS_ctx.containerRef} */ ;
if (__VLS_ctx.loading) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "vis-loading" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div)({
        ...{ class: "loading-spinner" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.canvas)({
    ref: "bgCanvasRef",
    id: "bg",
    ...{ class: "vis-canvas bg-canvas" },
});
/** @type {typeof __VLS_ctx.bgCanvasRef} */ ;
__VLS_asFunctionalElement(__VLS_intrinsicElements.canvas)({
    ...{ onMousemove: (__VLS_ctx.handleMouseMove) },
    ...{ onClick: (__VLS_ctx.handleClick) },
    ref: "canvasRef",
    id: "zr",
    ...{ class: "vis-canvas zr-canvas" },
});
/** @type {typeof __VLS_ctx.canvasRef} */ ;
if (__VLS_ctx.showUi && __VLS_ctx.hoveredLipstick) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        id: "ui",
        ...{ class: "vis-ui" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "ui-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "ui-brand" },
    });
    (__VLS_ctx.getBrandDisplayName(__VLS_ctx.hoveredLipstick.brand.name));
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "ui-series" },
    });
    (__VLS_ctx.hoveredLipstick.series.name);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "ui-colors" },
    });
    for (const [sc, idx] of __VLS_getVForSourceType((__VLS_ctx.seriesColors))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div)({
            key: (idx),
            ...{ class: "series-color" },
            ...{ class: ({ active: sc.active }) },
            ...{ style: ({ backgroundColor: sc.color }) },
        });
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "ui-lipstick-info" },
        ...{ style: ({ color: __VLS_ctx.hoveredLipstick.color }) },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "ui-id" },
    });
    (__VLS_ctx.hoveredLipstick.id);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "ui-name" },
    });
    (__VLS_ctx.hoveredLipstick.name);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "vis-info" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({
    ...{ class: "vis-title" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "vis-subtitle" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "vis-axis-hint" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "axis-label" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "axis-divider" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "axis-label" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "vis-axis-vhint" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "axis-label" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "axis-divider" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "axis-label" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "vis-brand-legend" },
});
for (const [brandName] of __VLS_getVForSourceType((Object.keys(__VLS_ctx.brandDisplayMap)))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        key: (brandName),
        ...{ class: "legend-item" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "legend-brand" },
    });
    (__VLS_ctx.brandDisplayMap[brandName]);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "legend-cn" },
    });
    (brandName);
}
/** @type {__VLS_StyleScopedClasses['vis-page']} */ ;
/** @type {__VLS_StyleScopedClasses['vis-container']} */ ;
/** @type {__VLS_StyleScopedClasses['vis-loading']} */ ;
/** @type {__VLS_StyleScopedClasses['loading-spinner']} */ ;
/** @type {__VLS_StyleScopedClasses['vis-canvas']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-canvas']} */ ;
/** @type {__VLS_StyleScopedClasses['vis-canvas']} */ ;
/** @type {__VLS_StyleScopedClasses['zr-canvas']} */ ;
/** @type {__VLS_StyleScopedClasses['vis-ui']} */ ;
/** @type {__VLS_StyleScopedClasses['ui-header']} */ ;
/** @type {__VLS_StyleScopedClasses['ui-brand']} */ ;
/** @type {__VLS_StyleScopedClasses['ui-series']} */ ;
/** @type {__VLS_StyleScopedClasses['ui-colors']} */ ;
/** @type {__VLS_StyleScopedClasses['series-color']} */ ;
/** @type {__VLS_StyleScopedClasses['ui-lipstick-info']} */ ;
/** @type {__VLS_StyleScopedClasses['ui-id']} */ ;
/** @type {__VLS_StyleScopedClasses['ui-name']} */ ;
/** @type {__VLS_StyleScopedClasses['vis-info']} */ ;
/** @type {__VLS_StyleScopedClasses['vis-title']} */ ;
/** @type {__VLS_StyleScopedClasses['vis-subtitle']} */ ;
/** @type {__VLS_StyleScopedClasses['vis-axis-hint']} */ ;
/** @type {__VLS_StyleScopedClasses['axis-label']} */ ;
/** @type {__VLS_StyleScopedClasses['axis-divider']} */ ;
/** @type {__VLS_StyleScopedClasses['axis-label']} */ ;
/** @type {__VLS_StyleScopedClasses['vis-axis-vhint']} */ ;
/** @type {__VLS_StyleScopedClasses['axis-label']} */ ;
/** @type {__VLS_StyleScopedClasses['axis-divider']} */ ;
/** @type {__VLS_StyleScopedClasses['axis-label']} */ ;
/** @type {__VLS_StyleScopedClasses['vis-brand-legend']} */ ;
/** @type {__VLS_StyleScopedClasses['legend-item']} */ ;
/** @type {__VLS_StyleScopedClasses['legend-brand']} */ ;
/** @type {__VLS_StyleScopedClasses['legend-cn']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            canvasRef: canvasRef,
            bgCanvasRef: bgCanvasRef,
            containerRef: containerRef,
            hoveredLipstick: hoveredLipstick,
            showUi: showUi,
            seriesColors: seriesColors,
            loading: loading,
            brandDisplayMap: brandDisplayMap,
            getBrandDisplayName: getBrandDisplayName,
            handleMouseMove: handleMouseMove,
            handleClick: handleClick,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
