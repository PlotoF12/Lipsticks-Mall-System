import { onMounted, ref, watch, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import http from "../api/http";
const route = useRoute();
const router = useRouter();
const loading = ref(false);
const product = ref(null);
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
const finishMap = {
    matte: "哑光",
    gloss: "滋润",
    satin: "缎面"
};
const carouselImages = computed(() => {
    if (!product.value)
        return [];
    const id = product.value.id;
    const images = [];
    for (let i = 1; i <= 5; i++) {
        images.push(`/images/products/${id}/${i}.png`);
    }
    return images;
});
async function load(id) {
    loading.value = true;
    product.value = null;
    try {
        const res = await http.get(`/mall/products/${id}`);
        product.value = res.data.data;
    }
    catch (e) {
        ElMessage.error(e instanceof Error ? e.message : "加载失败");
    }
    finally {
        loading.value = false;
    }
}
function goTryOn() {
    if (product.value) {
        router.push({ path: "/user/tryon", query: { productId: String(product.value.id) } });
    }
}
function handleImageError(e) {
    const img = e.target;
    img.style.display = "none";
}
onMounted(() => load(String(route.params.id)));
watch(() => route.params.id, (id) => load(String(id)));
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
// CSS variable injection 
// CSS variable injection end 
const __VLS_0 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({}));
const __VLS_2 = __VLS_1({}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.loading) }, null, null);
var __VLS_4 = {};
__VLS_3.slots.default;
if (__VLS_ctx.product) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "carousel-section" },
    });
    const __VLS_5 = {}.ElCarousel;
    /** @type {[typeof __VLS_components.ElCarousel, typeof __VLS_components.elCarousel, typeof __VLS_components.ElCarousel, typeof __VLS_components.elCarousel, ]} */ ;
    // @ts-ignore
    const __VLS_6 = __VLS_asFunctionalComponent(__VLS_5, new __VLS_5({
        height: "320px",
        indicatorPosition: "outside",
    }));
    const __VLS_7 = __VLS_6({
        height: "320px",
        indicatorPosition: "outside",
    }, ...__VLS_functionalComponentArgsRest(__VLS_6));
    __VLS_8.slots.default;
    for (const [img, idx] of __VLS_getVForSourceType((__VLS_ctx.carouselImages))) {
        const __VLS_9 = {}.ElCarouselItem;
        /** @type {[typeof __VLS_components.ElCarouselItem, typeof __VLS_components.elCarouselItem, typeof __VLS_components.ElCarouselItem, typeof __VLS_components.elCarouselItem, ]} */ ;
        // @ts-ignore
        const __VLS_10 = __VLS_asFunctionalComponent(__VLS_9, new __VLS_9({
            key: (idx),
        }));
        const __VLS_11 = __VLS_10({
            key: (idx),
        }, ...__VLS_functionalComponentArgsRest(__VLS_10));
        __VLS_12.slots.default;
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "carousel-img-wrap" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.img)({
            ...{ onError: (__VLS_ctx.handleImageError) },
            src: (img),
            alt: (`商品图片 ${idx + 1}`),
            ...{ class: "carousel-img" },
        });
        var __VLS_12;
    }
    var __VLS_8;
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "product-header" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "product-info" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
    (__VLS_ctx.product.title);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "sub" },
    });
    (__VLS_ctx.product.brand);
    (__VLS_ctx.product.shade);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "price" },
    });
    (__VLS_ctx.product.price);
    if (__VLS_ctx.product.colorHex) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "color-preview" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div)({
            ...{ class: "color-circle" },
            ...{ style: ({ background: __VLS_ctx.product.colorHex }) },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "color-hex" },
        });
        (__VLS_ctx.product.colorHex);
    }
    const __VLS_13 = {}.ElDivider;
    /** @type {[typeof __VLS_components.ElDivider, typeof __VLS_components.elDivider, ]} */ ;
    // @ts-ignore
    const __VLS_14 = __VLS_asFunctionalComponent(__VLS_13, new __VLS_13({}));
    const __VLS_15 = __VLS_14({}, ...__VLS_functionalComponentArgsRest(__VLS_14));
    const __VLS_17 = {}.ElDescriptions;
    /** @type {[typeof __VLS_components.ElDescriptions, typeof __VLS_components.elDescriptions, typeof __VLS_components.ElDescriptions, typeof __VLS_components.elDescriptions, ]} */ ;
    // @ts-ignore
    const __VLS_18 = __VLS_asFunctionalComponent(__VLS_17, new __VLS_17({
        column: (2),
        border: true,
    }));
    const __VLS_19 = __VLS_18({
        column: (2),
        border: true,
    }, ...__VLS_functionalComponentArgsRest(__VLS_18));
    __VLS_20.slots.default;
    const __VLS_21 = {}.ElDescriptionsItem;
    /** @type {[typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, ]} */ ;
    // @ts-ignore
    const __VLS_22 = __VLS_asFunctionalComponent(__VLS_21, new __VLS_21({
        label: "品牌",
    }));
    const __VLS_23 = __VLS_22({
        label: "品牌",
    }, ...__VLS_functionalComponentArgsRest(__VLS_22));
    __VLS_24.slots.default;
    (__VLS_ctx.product.brand || "-");
    var __VLS_24;
    const __VLS_25 = {}.ElDescriptionsItem;
    /** @type {[typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, ]} */ ;
    // @ts-ignore
    const __VLS_26 = __VLS_asFunctionalComponent(__VLS_25, new __VLS_25({
        label: "色号",
    }));
    const __VLS_27 = __VLS_26({
        label: "色号",
    }, ...__VLS_functionalComponentArgsRest(__VLS_26));
    __VLS_28.slots.default;
    (__VLS_ctx.product.shade || "-");
    var __VLS_28;
    const __VLS_29 = {}.ElDescriptionsItem;
    /** @type {[typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, ]} */ ;
    // @ts-ignore
    const __VLS_30 = __VLS_asFunctionalComponent(__VLS_29, new __VLS_29({
        label: "色系",
    }));
    const __VLS_31 = __VLS_30({
        label: "色系",
    }, ...__VLS_functionalComponentArgsRest(__VLS_30));
    __VLS_32.slots.default;
    (__VLS_ctx.categoryMap[__VLS_ctx.product.category || ''] || __VLS_ctx.product.category || '-');
    var __VLS_32;
    const __VLS_33 = {}.ElDescriptionsItem;
    /** @type {[typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, ]} */ ;
    // @ts-ignore
    const __VLS_34 = __VLS_asFunctionalComponent(__VLS_33, new __VLS_33({
        label: "质地",
    }));
    const __VLS_35 = __VLS_34({
        label: "质地",
    }, ...__VLS_functionalComponentArgsRest(__VLS_34));
    __VLS_36.slots.default;
    (__VLS_ctx.finishMap[__VLS_ctx.product.finishType || ''] || __VLS_ctx.product.finishType || '-');
    var __VLS_36;
    const __VLS_37 = {}.ElDescriptionsItem;
    /** @type {[typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, ]} */ ;
    // @ts-ignore
    const __VLS_38 = __VLS_asFunctionalComponent(__VLS_37, new __VLS_37({
        label: "价格",
    }));
    const __VLS_39 = __VLS_38({
        label: "价格",
    }, ...__VLS_functionalComponentArgsRest(__VLS_38));
    __VLS_40.slots.default;
    (__VLS_ctx.product.price);
    var __VLS_40;
    const __VLS_41 = {}.ElDescriptionsItem;
    /** @type {[typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, ]} */ ;
    // @ts-ignore
    const __VLS_42 = __VLS_asFunctionalComponent(__VLS_41, new __VLS_41({
        label: "库存",
    }));
    const __VLS_43 = __VLS_42({
        label: "库存",
    }, ...__VLS_functionalComponentArgsRest(__VLS_42));
    __VLS_44.slots.default;
    (__VLS_ctx.product.stock);
    var __VLS_44;
    const __VLS_45 = {}.ElDescriptionsItem;
    /** @type {[typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, ]} */ ;
    // @ts-ignore
    const __VLS_46 = __VLS_asFunctionalComponent(__VLS_45, new __VLS_45({
        label: "适合肤色",
    }));
    const __VLS_47 = __VLS_46({
        label: "适合肤色",
    }, ...__VLS_functionalComponentArgsRest(__VLS_46));
    __VLS_48.slots.default;
    (__VLS_ctx.product.suitableSkinTone || '-');
    var __VLS_48;
    const __VLS_49 = {}.ElDescriptionsItem;
    /** @type {[typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, ]} */ ;
    // @ts-ignore
    const __VLS_50 = __VLS_asFunctionalComponent(__VLS_49, new __VLS_49({
        label: "适合性别",
    }));
    const __VLS_51 = __VLS_50({
        label: "适合性别",
    }, ...__VLS_functionalComponentArgsRest(__VLS_50));
    __VLS_52.slots.default;
    (__VLS_ctx.product.suitableGender || '-');
    var __VLS_52;
    const __VLS_53 = {}.ElDescriptionsItem;
    /** @type {[typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, typeof __VLS_components.ElDescriptionsItem, typeof __VLS_components.elDescriptionsItem, ]} */ ;
    // @ts-ignore
    const __VLS_54 = __VLS_asFunctionalComponent(__VLS_53, new __VLS_53({
        label: "适用场景",
        span: (2),
    }));
    const __VLS_55 = __VLS_54({
        label: "适用场景",
        span: (2),
    }, ...__VLS_functionalComponentArgsRest(__VLS_54));
    __VLS_56.slots.default;
    (__VLS_ctx.product.scene || '-');
    var __VLS_56;
    var __VLS_20;
    const __VLS_57 = {}.ElDivider;
    /** @type {[typeof __VLS_components.ElDivider, typeof __VLS_components.elDivider, ]} */ ;
    // @ts-ignore
    const __VLS_58 = __VLS_asFunctionalComponent(__VLS_57, new __VLS_57({}));
    const __VLS_59 = __VLS_58({}, ...__VLS_functionalComponentArgsRest(__VLS_58));
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "detail-section" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "detail" },
    });
    (__VLS_ctx.product.detail || "暂无详情");
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "action-bar" },
    });
    const __VLS_61 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_62 = __VLS_asFunctionalComponent(__VLS_61, new __VLS_61({
        ...{ 'onClick': {} },
        type: "primary",
        size: "large",
    }));
    const __VLS_63 = __VLS_62({
        ...{ 'onClick': {} },
        type: "primary",
        size: "large",
    }, ...__VLS_functionalComponentArgsRest(__VLS_62));
    let __VLS_65;
    let __VLS_66;
    let __VLS_67;
    const __VLS_68 = {
        onClick: (__VLS_ctx.goTryOn)
    };
    __VLS_64.slots.default;
    var __VLS_64;
    const __VLS_69 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_70 = __VLS_asFunctionalComponent(__VLS_69, new __VLS_69({
        ...{ 'onClick': {} },
        size: "large",
    }));
    const __VLS_71 = __VLS_70({
        ...{ 'onClick': {} },
        size: "large",
    }, ...__VLS_functionalComponentArgsRest(__VLS_70));
    let __VLS_73;
    let __VLS_74;
    let __VLS_75;
    const __VLS_76 = {
        onClick: (...[$event]) => {
            if (!(__VLS_ctx.product))
                return;
            __VLS_ctx.router.push('/user/mall');
        }
    };
    __VLS_72.slots.default;
    var __VLS_72;
}
else if (!__VLS_ctx.loading) {
    const __VLS_77 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_78 = __VLS_asFunctionalComponent(__VLS_77, new __VLS_77({
        description: "未找到商品",
    }));
    const __VLS_79 = __VLS_78({
        description: "未找到商品",
    }, ...__VLS_functionalComponentArgsRest(__VLS_78));
}
var __VLS_3;
/** @type {__VLS_StyleScopedClasses['carousel-section']} */ ;
/** @type {__VLS_StyleScopedClasses['carousel-img-wrap']} */ ;
/** @type {__VLS_StyleScopedClasses['carousel-img']} */ ;
/** @type {__VLS_StyleScopedClasses['product-header']} */ ;
/** @type {__VLS_StyleScopedClasses['product-info']} */ ;
/** @type {__VLS_StyleScopedClasses['sub']} */ ;
/** @type {__VLS_StyleScopedClasses['price']} */ ;
/** @type {__VLS_StyleScopedClasses['color-preview']} */ ;
/** @type {__VLS_StyleScopedClasses['color-circle']} */ ;
/** @type {__VLS_StyleScopedClasses['color-hex']} */ ;
/** @type {__VLS_StyleScopedClasses['detail-section']} */ ;
/** @type {__VLS_StyleScopedClasses['detail']} */ ;
/** @type {__VLS_StyleScopedClasses['action-bar']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            router: router,
            loading: loading,
            product: product,
            categoryMap: categoryMap,
            finishMap: finishMap,
            carouselImages: carouselImages,
            goTryOn: goTryOn,
            handleImageError: handleImageError,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
