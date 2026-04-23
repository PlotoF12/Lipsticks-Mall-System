import { onMounted, ref, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import http from "../api/http";
const router = useRouter();
const loading = ref(false);
const products = ref([]);
const brands = ref([]);
const categories = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(6);
const keyword = ref("");
const selectedBrand = ref("");
const selectedCategory = ref("");
const categoryMap = {
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
            http.get("/mall/brands"),
            http.get("/mall/categories")
        ]);
        brands.value = brandsRes.data.data;
        categories.value = categoriesRes.data.data;
    }
    catch {
        /* ignore */
    }
}
async function load() {
    loading.value = true;
    try {
        const params = {
            page: currentPage.value,
            size: pageSize.value
        };
        if (keyword.value.trim())
            params.keyword = keyword.value.trim();
        if (selectedBrand.value)
            params.brand = selectedBrand.value;
        if (selectedCategory.value)
            params.category = selectedCategory.value;
        const res = await http.get("/mall/products/page", { params });
        const pageData = res.data.data;
        products.value = pageData.records;
        total.value = pageData.total;
    }
    catch (e) {
        ElMessage.error(e instanceof Error ? e.message : "加载失败");
    }
    finally {
        loading.value = false;
    }
}
function handlePageChange(page) {
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
function goDetail(id) {
    router.push(`/user/product/${id}`);
}
function getCategoryLabel(key) {
    if (!key)
        return "";
    return categoryMap[key] || key;
}
function truncate(text, len) {
    if (!text)
        return "";
    return text.length > len ? text.slice(0, len) + "..." : text;
}
onMounted(() => {
    loadFilters();
    load();
});
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['mall-header']} */ ;
/** @type {__VLS_StyleScopedClasses['mall-header']} */ ;
/** @type {__VLS_StyleScopedClasses['product-card']} */ ;
/** @type {__VLS_StyleScopedClasses['product-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['product-grid']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "mall-wrap" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "bg-orb orb-left" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "bg-orb orb-right" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "mall-header" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "filter-bar" },
});
const __VLS_0 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    ...{ 'onKeyup': {} },
    modelValue: (__VLS_ctx.keyword),
    placeholder: "搜索商品名称/品牌/色号",
    clearable: true,
    ...{ style: {} },
}));
const __VLS_2 = __VLS_1({
    ...{ 'onKeyup': {} },
    modelValue: (__VLS_ctx.keyword),
    placeholder: "搜索商品名称/品牌/色号",
    clearable: true,
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
let __VLS_4;
let __VLS_5;
let __VLS_6;
const __VLS_7 = {
    onKeyup: (__VLS_ctx.handleSearch)
};
var __VLS_3;
const __VLS_8 = {}.ElSelect;
/** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
// @ts-ignore
const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({
    ...{ 'onChange': {} },
    modelValue: (__VLS_ctx.selectedBrand),
    placeholder: "品牌",
    clearable: true,
    ...{ style: {} },
}));
const __VLS_10 = __VLS_9({
    ...{ 'onChange': {} },
    modelValue: (__VLS_ctx.selectedBrand),
    placeholder: "品牌",
    clearable: true,
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_9));
let __VLS_12;
let __VLS_13;
let __VLS_14;
const __VLS_15 = {
    onChange: (__VLS_ctx.handleSearch)
};
__VLS_11.slots.default;
for (const [b] of __VLS_getVForSourceType((__VLS_ctx.brands))) {
    const __VLS_16 = {}.ElOption;
    /** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
    // @ts-ignore
    const __VLS_17 = __VLS_asFunctionalComponent(__VLS_16, new __VLS_16({
        key: (b),
        label: (b),
        value: (b),
    }));
    const __VLS_18 = __VLS_17({
        key: (b),
        label: (b),
        value: (b),
    }, ...__VLS_functionalComponentArgsRest(__VLS_17));
}
var __VLS_11;
const __VLS_20 = {}.ElSelect;
/** @type {[typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, typeof __VLS_components.ElSelect, typeof __VLS_components.elSelect, ]} */ ;
// @ts-ignore
const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({
    ...{ 'onChange': {} },
    modelValue: (__VLS_ctx.selectedCategory),
    placeholder: "色系",
    clearable: true,
    ...{ style: {} },
}));
const __VLS_22 = __VLS_21({
    ...{ 'onChange': {} },
    modelValue: (__VLS_ctx.selectedCategory),
    placeholder: "色系",
    clearable: true,
    ...{ style: {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_21));
let __VLS_24;
let __VLS_25;
let __VLS_26;
const __VLS_27 = {
    onChange: (__VLS_ctx.handleSearch)
};
__VLS_23.slots.default;
for (const [c] of __VLS_getVForSourceType((__VLS_ctx.categories))) {
    const __VLS_28 = {}.ElOption;
    /** @type {[typeof __VLS_components.ElOption, typeof __VLS_components.elOption, ]} */ ;
    // @ts-ignore
    const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
        key: (c),
        label: (__VLS_ctx.getCategoryLabel(c)),
        value: (c),
    }));
    const __VLS_30 = __VLS_29({
        key: (c),
        label: (__VLS_ctx.getCategoryLabel(c)),
        value: (c),
    }, ...__VLS_functionalComponentArgsRest(__VLS_29));
}
var __VLS_23;
const __VLS_32 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({
    ...{ 'onClick': {} },
    type: "primary",
}));
const __VLS_34 = __VLS_33({
    ...{ 'onClick': {} },
    type: "primary",
}, ...__VLS_functionalComponentArgsRest(__VLS_33));
let __VLS_36;
let __VLS_37;
let __VLS_38;
const __VLS_39 = {
    onClick: (__VLS_ctx.handleSearch)
};
__VLS_35.slots.default;
var __VLS_35;
const __VLS_40 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
    ...{ 'onClick': {} },
}));
const __VLS_42 = __VLS_41({
    ...{ 'onClick': {} },
}, ...__VLS_functionalComponentArgsRest(__VLS_41));
let __VLS_44;
let __VLS_45;
let __VLS_46;
const __VLS_47 = {
    onClick: (__VLS_ctx.resetFilters)
};
__VLS_43.slots.default;
var __VLS_43;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "product-grid" },
});
__VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.loading) }, null, null);
for (const [item] of __VLS_getVForSourceType((__VLS_ctx.products))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        key: (item.id),
        ...{ class: "product-card" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-color-area" },
    });
    if (item.colorHex) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span)({
            ...{ class: "color-circle" },
            ...{ style: ({ background: item.colorHex }) },
        });
    }
    else {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span)({
            ...{ class: "color-circle color-circle-empty" },
        });
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-body" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({
        ...{ class: "card-title" },
    });
    (item.title);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-meta" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "meta-brand" },
    });
    (item.brand);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "meta-divider" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "meta-category" },
    });
    (__VLS_ctx.getCategoryLabel(item.category));
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "card-desc" },
    });
    (__VLS_ctx.truncate(item.detail, 50));
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-footer" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "card-price" },
    });
    (item.price);
    const __VLS_48 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({
        ...{ 'onClick': {} },
        ...{ class: "detail-btn" },
        type: "primary",
        size: "small",
    }));
    const __VLS_50 = __VLS_49({
        ...{ 'onClick': {} },
        ...{ class: "detail-btn" },
        type: "primary",
        size: "small",
    }, ...__VLS_functionalComponentArgsRest(__VLS_49));
    let __VLS_52;
    let __VLS_53;
    let __VLS_54;
    const __VLS_55 = {
        onClick: (...[$event]) => {
            __VLS_ctx.goDetail(item.id);
        }
    };
    __VLS_51.slots.default;
    var __VLS_51;
}
if (!__VLS_ctx.loading && __VLS_ctx.products.length === 0) {
    const __VLS_56 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({
        description: "暂无商品",
        ...{ class: "empty-state" },
    }));
    const __VLS_58 = __VLS_57({
        description: "暂无商品",
        ...{ class: "empty-state" },
    }, ...__VLS_functionalComponentArgsRest(__VLS_57));
}
if (__VLS_ctx.total > 0) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "pagination-area" },
    });
    const __VLS_60 = {}.ElPagination;
    /** @type {[typeof __VLS_components.ElPagination, typeof __VLS_components.elPagination, ]} */ ;
    // @ts-ignore
    const __VLS_61 = __VLS_asFunctionalComponent(__VLS_60, new __VLS_60({
        ...{ 'onCurrentChange': {} },
        currentPage: (__VLS_ctx.currentPage),
        pageSize: (__VLS_ctx.pageSize),
        total: (__VLS_ctx.total),
        layout: "prev, pager, next",
    }));
    const __VLS_62 = __VLS_61({
        ...{ 'onCurrentChange': {} },
        currentPage: (__VLS_ctx.currentPage),
        pageSize: (__VLS_ctx.pageSize),
        total: (__VLS_ctx.total),
        layout: "prev, pager, next",
    }, ...__VLS_functionalComponentArgsRest(__VLS_61));
    let __VLS_64;
    let __VLS_65;
    let __VLS_66;
    const __VLS_67 = {
        onCurrentChange: (__VLS_ctx.handlePageChange)
    };
    var __VLS_63;
}
/** @type {__VLS_StyleScopedClasses['mall-wrap']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-orb']} */ ;
/** @type {__VLS_StyleScopedClasses['orb-left']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-orb']} */ ;
/** @type {__VLS_StyleScopedClasses['orb-right']} */ ;
/** @type {__VLS_StyleScopedClasses['mall-header']} */ ;
/** @type {__VLS_StyleScopedClasses['filter-bar']} */ ;
/** @type {__VLS_StyleScopedClasses['product-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['product-card']} */ ;
/** @type {__VLS_StyleScopedClasses['card-color-area']} */ ;
/** @type {__VLS_StyleScopedClasses['color-circle']} */ ;
/** @type {__VLS_StyleScopedClasses['color-circle']} */ ;
/** @type {__VLS_StyleScopedClasses['color-circle-empty']} */ ;
/** @type {__VLS_StyleScopedClasses['card-body']} */ ;
/** @type {__VLS_StyleScopedClasses['card-title']} */ ;
/** @type {__VLS_StyleScopedClasses['card-meta']} */ ;
/** @type {__VLS_StyleScopedClasses['meta-brand']} */ ;
/** @type {__VLS_StyleScopedClasses['meta-divider']} */ ;
/** @type {__VLS_StyleScopedClasses['meta-category']} */ ;
/** @type {__VLS_StyleScopedClasses['card-desc']} */ ;
/** @type {__VLS_StyleScopedClasses['card-footer']} */ ;
/** @type {__VLS_StyleScopedClasses['card-price']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['empty-state']} */ ;
/** @type {__VLS_StyleScopedClasses['pagination-area']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            loading: loading,
            products: products,
            brands: brands,
            categories: categories,
            total: total,
            currentPage: currentPage,
            pageSize: pageSize,
            keyword: keyword,
            selectedBrand: selectedBrand,
            selectedCategory: selectedCategory,
            handlePageChange: handlePageChange,
            handleSearch: handleSearch,
            resetFilters: resetFilters,
            goDetail: goDetail,
            getCategoryLabel: getCategoryLabel,
            truncate: truncate,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
