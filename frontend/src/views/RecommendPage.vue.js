import { ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import http from "../api/http";
const router = useRouter();
const loading = ref(false);
const items = ref([]);
const target = ref("girlfriend");
const aiLoading = ref(false);
const aiMessages = ref([]);
const aiInput = ref("");
const aiGender = ref("");
const aiSkinTone = ref("");
const aiSkinType = ref("");
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
async function load() {
    loading.value = true;
    try {
        const res = await http.get("/recommend", {
            params: { target: target.value }
        });
        items.value = res.data.data;
    }
    catch (e) {
        ElMessage.error(e instanceof Error ? e.message : "加载失败");
    }
    finally {
        loading.value = false;
    }
}
function goDetail(id) {
    router.push(`/user/product/${id}`);
}
function getCategoryLabel(key) {
    if (!key)
        return "";
    return categoryMap[key] || key;
}
async function sendAiMessage() {
    const userText = aiInput.value.trim();
    if (!userText)
        return;
    aiMessages.value.push({ role: "user", content: userText });
    aiInput.value = "";
    aiLoading.value = true;
    try {
        const res = await http.post("/recommend/ai", {
            message: userText
        });
        aiMessages.value.push({ role: "assistant", content: res.data.data });
    }
    catch (e) {
        aiMessages.value.push({
            role: "assistant",
            content: "抱歉，小红暂时无法回复，请稍后再试～"
        });
    }
    finally {
        aiLoading.value = false;
    }
}
function buildAiPrompt() {
    const parts = [];
    if (aiGender.value)
        parts.push(`我的性别是${aiGender.value}`);
    if (aiSkinTone.value)
        parts.push(`肤色是${aiSkinTone.value}`);
    if (aiSkinType.value)
        parts.push(`肤质是${aiSkinType.value}`);
    if (parts.length > 0) {
        aiInput.value = parts.join("，") + "，请为我推荐合适的口红";
    }
}
load();
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['recommend-header']} */ ;
/** @type {__VLS_StyleScopedClasses['recommend-header']} */ ;
/** @type {__VLS_StyleScopedClasses['two-columns']} */ ;
/** @type {__VLS_StyleScopedClasses['section-head']} */ ;
/** @type {__VLS_StyleScopedClasses['recommend-card']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-msg']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-msg']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-msg']} */ ;
/** @type {__VLS_StyleScopedClasses['user']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-msg']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-msg']} */ ;
/** @type {__VLS_StyleScopedClasses['user']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-bubble']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-msg']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-bubble']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-input-area']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "recommend-wrap" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "bg-orb orb-left" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "bg-orb orb-right" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "recommend-header" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "two-columns" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "column" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "section-card" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "section-head" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "section-icon" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "section-desc" },
});
if (__VLS_ctx.target === 'girlfriend') {
}
else {
}
const __VLS_0 = {}.ElSpace;
/** @type {[typeof __VLS_components.ElSpace, typeof __VLS_components.elSpace, typeof __VLS_components.ElSpace, typeof __VLS_components.elSpace, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    wrap: true,
    ...{ class: "mb-16" },
}));
const __VLS_2 = __VLS_1({
    wrap: true,
    ...{ class: "mb-16" },
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_3.slots.default;
const __VLS_4 = {}.ElRadioGroup;
/** @type {[typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, ]} */ ;
// @ts-ignore
const __VLS_5 = __VLS_asFunctionalComponent(__VLS_4, new __VLS_4({
    ...{ 'onChange': {} },
    modelValue: (__VLS_ctx.target),
}));
const __VLS_6 = __VLS_5({
    ...{ 'onChange': {} },
    modelValue: (__VLS_ctx.target),
}, ...__VLS_functionalComponentArgsRest(__VLS_5));
let __VLS_8;
let __VLS_9;
let __VLS_10;
const __VLS_11 = {
    onChange: (__VLS_ctx.load)
};
__VLS_7.slots.default;
const __VLS_12 = {}.ElRadioButton;
/** @type {[typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, ]} */ ;
// @ts-ignore
const __VLS_13 = __VLS_asFunctionalComponent(__VLS_12, new __VLS_12({
    label: "girlfriend",
}));
const __VLS_14 = __VLS_13({
    label: "girlfriend",
}, ...__VLS_functionalComponentArgsRest(__VLS_13));
__VLS_15.slots.default;
var __VLS_15;
const __VLS_16 = {}.ElRadioButton;
/** @type {[typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, ]} */ ;
// @ts-ignore
const __VLS_17 = __VLS_asFunctionalComponent(__VLS_16, new __VLS_16({
    label: "self",
}));
const __VLS_18 = __VLS_17({
    label: "self",
}, ...__VLS_functionalComponentArgsRest(__VLS_17));
__VLS_19.slots.default;
var __VLS_19;
var __VLS_7;
const __VLS_20 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({
    ...{ 'onClick': {} },
    type: "primary",
    loading: (__VLS_ctx.loading),
}));
const __VLS_22 = __VLS_21({
    ...{ 'onClick': {} },
    type: "primary",
    loading: (__VLS_ctx.loading),
}, ...__VLS_functionalComponentArgsRest(__VLS_21));
let __VLS_24;
let __VLS_25;
let __VLS_26;
const __VLS_27 = {
    onClick: (__VLS_ctx.load)
};
__VLS_23.slots.default;
var __VLS_23;
var __VLS_3;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "recommend-list" },
});
__VLS_asFunctionalDirective(__VLS_directives.vLoading)(null, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.loading) }, null, null);
for (const [item] of __VLS_getVForSourceType((__VLS_ctx.items))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        key: (item.productId),
        ...{ class: "recommend-card" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-left" },
    });
    if (item.colorHex) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span)({
            ...{ class: "color-dot" },
            ...{ style: ({ background: item.colorHex }) },
        });
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-body" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-title" },
    });
    (item.title);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-meta" },
    });
    (item.brand);
    (item.shade);
    (__VLS_ctx.getCategoryLabel(item.category));
    (item.price);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-reason" },
    });
    (item.reason);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "card-action" },
    });
    const __VLS_28 = {}.ElButton;
    /** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
    // @ts-ignore
    const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
        ...{ 'onClick': {} },
        link: true,
        type: "primary",
    }));
    const __VLS_30 = __VLS_29({
        ...{ 'onClick': {} },
        link: true,
        type: "primary",
    }, ...__VLS_functionalComponentArgsRest(__VLS_29));
    let __VLS_32;
    let __VLS_33;
    let __VLS_34;
    const __VLS_35 = {
        onClick: (...[$event]) => {
            __VLS_ctx.goDetail(item.productId);
        }
    };
    __VLS_31.slots.default;
    var __VLS_31;
}
if (!__VLS_ctx.loading && __VLS_ctx.items.length === 0) {
    const __VLS_36 = {}.ElEmpty;
    /** @type {[typeof __VLS_components.ElEmpty, typeof __VLS_components.elEmpty, ]} */ ;
    // @ts-ignore
    const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({
        description: "暂无推荐",
    }));
    const __VLS_38 = __VLS_37({
        description: "暂无推荐",
    }, ...__VLS_functionalComponentArgsRest(__VLS_37));
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "column" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "section-card ai-card" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "section-head" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "section-icon ai-icon" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "section-desc" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "ai-quick-tags" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "tag-label" },
});
const __VLS_40 = {}.ElRadioGroup;
/** @type {[typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, ]} */ ;
// @ts-ignore
const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
    modelValue: (__VLS_ctx.aiGender),
    size: "small",
}));
const __VLS_42 = __VLS_41({
    modelValue: (__VLS_ctx.aiGender),
    size: "small",
}, ...__VLS_functionalComponentArgsRest(__VLS_41));
__VLS_43.slots.default;
const __VLS_44 = {}.ElRadioButton;
/** @type {[typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, ]} */ ;
// @ts-ignore
const __VLS_45 = __VLS_asFunctionalComponent(__VLS_44, new __VLS_44({
    label: "女",
}));
const __VLS_46 = __VLS_45({
    label: "女",
}, ...__VLS_functionalComponentArgsRest(__VLS_45));
const __VLS_48 = {}.ElRadioButton;
/** @type {[typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, ]} */ ;
// @ts-ignore
const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({
    label: "男",
}));
const __VLS_50 = __VLS_49({
    label: "男",
}, ...__VLS_functionalComponentArgsRest(__VLS_49));
var __VLS_43;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "ai-quick-tags" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "tag-label" },
});
const __VLS_52 = {}.ElRadioGroup;
/** @type {[typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, ]} */ ;
// @ts-ignore
const __VLS_53 = __VLS_asFunctionalComponent(__VLS_52, new __VLS_52({
    modelValue: (__VLS_ctx.aiSkinTone),
    size: "small",
}));
const __VLS_54 = __VLS_53({
    modelValue: (__VLS_ctx.aiSkinTone),
    size: "small",
}, ...__VLS_functionalComponentArgsRest(__VLS_53));
__VLS_55.slots.default;
const __VLS_56 = {}.ElRadioButton;
/** @type {[typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, ]} */ ;
// @ts-ignore
const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({
    label: "暖皮",
}));
const __VLS_58 = __VLS_57({
    label: "暖皮",
}, ...__VLS_functionalComponentArgsRest(__VLS_57));
const __VLS_60 = {}.ElRadioButton;
/** @type {[typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, ]} */ ;
// @ts-ignore
const __VLS_61 = __VLS_asFunctionalComponent(__VLS_60, new __VLS_60({
    label: "冷皮",
}));
const __VLS_62 = __VLS_61({
    label: "冷皮",
}, ...__VLS_functionalComponentArgsRest(__VLS_61));
const __VLS_64 = {}.ElRadioButton;
/** @type {[typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, ]} */ ;
// @ts-ignore
const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({
    label: "中性皮",
}));
const __VLS_66 = __VLS_65({
    label: "中性皮",
}, ...__VLS_functionalComponentArgsRest(__VLS_65));
var __VLS_55;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "ai-quick-tags" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "tag-label" },
});
const __VLS_68 = {}.ElRadioGroup;
/** @type {[typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, typeof __VLS_components.ElRadioGroup, typeof __VLS_components.elRadioGroup, ]} */ ;
// @ts-ignore
const __VLS_69 = __VLS_asFunctionalComponent(__VLS_68, new __VLS_68({
    modelValue: (__VLS_ctx.aiSkinType),
    size: "small",
}));
const __VLS_70 = __VLS_69({
    modelValue: (__VLS_ctx.aiSkinType),
    size: "small",
}, ...__VLS_functionalComponentArgsRest(__VLS_69));
__VLS_71.slots.default;
const __VLS_72 = {}.ElRadioButton;
/** @type {[typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, ]} */ ;
// @ts-ignore
const __VLS_73 = __VLS_asFunctionalComponent(__VLS_72, new __VLS_72({
    label: "干性",
}));
const __VLS_74 = __VLS_73({
    label: "干性",
}, ...__VLS_functionalComponentArgsRest(__VLS_73));
const __VLS_76 = {}.ElRadioButton;
/** @type {[typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, ]} */ ;
// @ts-ignore
const __VLS_77 = __VLS_asFunctionalComponent(__VLS_76, new __VLS_76({
    label: "油性",
}));
const __VLS_78 = __VLS_77({
    label: "油性",
}, ...__VLS_functionalComponentArgsRest(__VLS_77));
const __VLS_80 = {}.ElRadioButton;
/** @type {[typeof __VLS_components.ElRadioButton, typeof __VLS_components.elRadioButton, ]} */ ;
// @ts-ignore
const __VLS_81 = __VLS_asFunctionalComponent(__VLS_80, new __VLS_80({
    label: "中性",
}));
const __VLS_82 = __VLS_81({
    label: "中性",
}, ...__VLS_functionalComponentArgsRest(__VLS_81));
var __VLS_71;
const __VLS_84 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_85 = __VLS_asFunctionalComponent(__VLS_84, new __VLS_84({
    ...{ 'onClick': {} },
    size: "small",
    ...{ class: "quick-btn" },
}));
const __VLS_86 = __VLS_85({
    ...{ 'onClick': {} },
    size: "small",
    ...{ class: "quick-btn" },
}, ...__VLS_functionalComponentArgsRest(__VLS_85));
let __VLS_88;
let __VLS_89;
let __VLS_90;
const __VLS_91 = {
    onClick: (__VLS_ctx.buildAiPrompt)
};
__VLS_87.slots.default;
var __VLS_87;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "chat-area" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "chat-messages" },
});
for (const [msg, idx] of __VLS_getVForSourceType((__VLS_ctx.aiMessages))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        key: (idx),
        ...{ class: "chat-msg" },
        ...{ class: (msg.role) },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "msg-avatar" },
    });
    (msg.role === 'user' ? '我' : '红');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "msg-bubble" },
    });
    (msg.content);
}
if (__VLS_ctx.aiMessages.length === 0) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "chat-placeholder" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.br)({});
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "chat-input-area" },
});
const __VLS_92 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_93 = __VLS_asFunctionalComponent(__VLS_92, new __VLS_92({
    ...{ 'onKeyup': {} },
    modelValue: (__VLS_ctx.aiInput),
    placeholder: "输入你的需求，如：我是暖皮干性肤质的女生，推荐日常通勤口红",
}));
const __VLS_94 = __VLS_93({
    ...{ 'onKeyup': {} },
    modelValue: (__VLS_ctx.aiInput),
    placeholder: "输入你的需求，如：我是暖皮干性肤质的女生，推荐日常通勤口红",
}, ...__VLS_functionalComponentArgsRest(__VLS_93));
let __VLS_96;
let __VLS_97;
let __VLS_98;
const __VLS_99 = {
    onKeyup: (__VLS_ctx.sendAiMessage)
};
var __VLS_95;
const __VLS_100 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_101 = __VLS_asFunctionalComponent(__VLS_100, new __VLS_100({
    ...{ 'onClick': {} },
    type: "primary",
    loading: (__VLS_ctx.aiLoading),
}));
const __VLS_102 = __VLS_101({
    ...{ 'onClick': {} },
    type: "primary",
    loading: (__VLS_ctx.aiLoading),
}, ...__VLS_functionalComponentArgsRest(__VLS_101));
let __VLS_104;
let __VLS_105;
let __VLS_106;
const __VLS_107 = {
    onClick: (__VLS_ctx.sendAiMessage)
};
__VLS_103.slots.default;
var __VLS_103;
/** @type {__VLS_StyleScopedClasses['recommend-wrap']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-orb']} */ ;
/** @type {__VLS_StyleScopedClasses['orb-left']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-orb']} */ ;
/** @type {__VLS_StyleScopedClasses['orb-right']} */ ;
/** @type {__VLS_StyleScopedClasses['recommend-header']} */ ;
/** @type {__VLS_StyleScopedClasses['two-columns']} */ ;
/** @type {__VLS_StyleScopedClasses['column']} */ ;
/** @type {__VLS_StyleScopedClasses['section-card']} */ ;
/** @type {__VLS_StyleScopedClasses['section-head']} */ ;
/** @type {__VLS_StyleScopedClasses['section-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['section-desc']} */ ;
/** @type {__VLS_StyleScopedClasses['mb-16']} */ ;
/** @type {__VLS_StyleScopedClasses['recommend-list']} */ ;
/** @type {__VLS_StyleScopedClasses['recommend-card']} */ ;
/** @type {__VLS_StyleScopedClasses['card-left']} */ ;
/** @type {__VLS_StyleScopedClasses['color-dot']} */ ;
/** @type {__VLS_StyleScopedClasses['card-body']} */ ;
/** @type {__VLS_StyleScopedClasses['card-title']} */ ;
/** @type {__VLS_StyleScopedClasses['card-meta']} */ ;
/** @type {__VLS_StyleScopedClasses['card-reason']} */ ;
/** @type {__VLS_StyleScopedClasses['card-action']} */ ;
/** @type {__VLS_StyleScopedClasses['column']} */ ;
/** @type {__VLS_StyleScopedClasses['section-card']} */ ;
/** @type {__VLS_StyleScopedClasses['ai-card']} */ ;
/** @type {__VLS_StyleScopedClasses['section-head']} */ ;
/** @type {__VLS_StyleScopedClasses['section-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['ai-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['section-desc']} */ ;
/** @type {__VLS_StyleScopedClasses['ai-quick-tags']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-label']} */ ;
/** @type {__VLS_StyleScopedClasses['ai-quick-tags']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-label']} */ ;
/** @type {__VLS_StyleScopedClasses['ai-quick-tags']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-label']} */ ;
/** @type {__VLS_StyleScopedClasses['quick-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-area']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-messages']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-msg']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-bubble']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-placeholder']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-input-area']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            loading: loading,
            items: items,
            target: target,
            aiLoading: aiLoading,
            aiMessages: aiMessages,
            aiInput: aiInput,
            aiGender: aiGender,
            aiSkinTone: aiSkinTone,
            aiSkinType: aiSkinType,
            load: load,
            goDetail: goDetail,
            getCategoryLabel: getCategoryLabel,
            sendAiMessage: sendAiMessage,
            buildAiPrompt: buildAiPrompt,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
