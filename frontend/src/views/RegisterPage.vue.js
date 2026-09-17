import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useAuthStore } from "../stores/auth";
const router = useRouter();
const auth = useAuthStore();
const loading = ref(false);
const form = reactive({
    username: "",
    password: "",
    password2: ""
});
async function submit() {
    if (!form.username || !form.password) {
        ElMessage.warning("请填写用户名和密码");
        return;
    }
    if (form.password.length < 6) {
        ElMessage.warning("密码至少 6 位");
        return;
    }
    if (form.password !== form.password2) {
        ElMessage.warning("两次输入的密码不一致");
        return;
    }
    loading.value = true;
    try {
        await auth.register(form.username, form.password);
        ElMessage.success("注册成功，请登录");
        await router.replace("/user/login");
    }
    catch (e) {
        ElMessage.error(e instanceof Error ? e.message : "注册失败");
    }
    finally {
        loading.value = false;
    }
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['auth-card']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-head']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-form']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-form']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-form']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-form']} */ ;
/** @type {__VLS_StyleScopedClasses['el-input__wrapper']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['primary-btn']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "auth-wrap" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "bg-orb orb-left" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "bg-orb orb-right" },
});
const __VLS_0 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    ...{ class: "auth-card" },
    shadow: "never",
}));
const __VLS_2 = __VLS_1({
    ...{ class: "auth-card" },
    shadow: "never",
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_3.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "auth-head" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "auth-badge" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "sub" },
});
const __VLS_4 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_5 = __VLS_asFunctionalComponent(__VLS_4, new __VLS_4({
    ...{ 'onSubmit': {} },
    ...{ class: "auth-form" },
    labelWidth: "90px",
}));
const __VLS_6 = __VLS_5({
    ...{ 'onSubmit': {} },
    ...{ class: "auth-form" },
    labelWidth: "90px",
}, ...__VLS_functionalComponentArgsRest(__VLS_5));
let __VLS_8;
let __VLS_9;
let __VLS_10;
const __VLS_11 = {
    onSubmit: () => { }
};
__VLS_7.slots.default;
const __VLS_12 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_13 = __VLS_asFunctionalComponent(__VLS_12, new __VLS_12({
    label: "用户名",
}));
const __VLS_14 = __VLS_13({
    label: "用户名",
}, ...__VLS_functionalComponentArgsRest(__VLS_13));
__VLS_15.slots.default;
const __VLS_16 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_17 = __VLS_asFunctionalComponent(__VLS_16, new __VLS_16({
    modelValue: (__VLS_ctx.form.username),
    autocomplete: "username",
    placeholder: "请输入用户名",
}));
const __VLS_18 = __VLS_17({
    modelValue: (__VLS_ctx.form.username),
    autocomplete: "username",
    placeholder: "请输入用户名",
}, ...__VLS_functionalComponentArgsRest(__VLS_17));
var __VLS_15;
const __VLS_20 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({
    label: "密码",
}));
const __VLS_22 = __VLS_21({
    label: "密码",
}, ...__VLS_functionalComponentArgsRest(__VLS_21));
__VLS_23.slots.default;
const __VLS_24 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_25 = __VLS_asFunctionalComponent(__VLS_24, new __VLS_24({
    modelValue: (__VLS_ctx.form.password),
    type: "password",
    autocomplete: "new-password",
    placeholder: "至少 6 位",
    showPassword: true,
}));
const __VLS_26 = __VLS_25({
    modelValue: (__VLS_ctx.form.password),
    type: "password",
    autocomplete: "new-password",
    placeholder: "至少 6 位",
    showPassword: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_25));
var __VLS_23;
const __VLS_28 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
    label: "确认密码",
}));
const __VLS_30 = __VLS_29({
    label: "确认密码",
}, ...__VLS_functionalComponentArgsRest(__VLS_29));
__VLS_31.slots.default;
const __VLS_32 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({
    modelValue: (__VLS_ctx.form.password2),
    type: "password",
    autocomplete: "new-password",
    placeholder: "再次输入密码",
    showPassword: true,
}));
const __VLS_34 = __VLS_33({
    modelValue: (__VLS_ctx.form.password2),
    type: "password",
    autocomplete: "new-password",
    placeholder: "再次输入密码",
    showPassword: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_33));
var __VLS_31;
const __VLS_36 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({
    ...{ class: "auth-actions" },
}));
const __VLS_38 = __VLS_37({
    ...{ class: "auth-actions" },
}, ...__VLS_functionalComponentArgsRest(__VLS_37));
__VLS_39.slots.default;
const __VLS_40 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_41 = __VLS_asFunctionalComponent(__VLS_40, new __VLS_40({
    ...{ 'onClick': {} },
    ...{ class: "primary-btn" },
    type: "primary",
    loading: (__VLS_ctx.loading),
}));
const __VLS_42 = __VLS_41({
    ...{ 'onClick': {} },
    ...{ class: "primary-btn" },
    type: "primary",
    loading: (__VLS_ctx.loading),
}, ...__VLS_functionalComponentArgsRest(__VLS_41));
let __VLS_44;
let __VLS_45;
let __VLS_46;
const __VLS_47 = {
    onClick: (__VLS_ctx.submit)
};
__VLS_43.slots.default;
var __VLS_43;
const __VLS_48 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({
    ...{ 'onClick': {} },
    link: true,
    type: "primary",
    ...{ class: "link-btn" },
}));
const __VLS_50 = __VLS_49({
    ...{ 'onClick': {} },
    link: true,
    type: "primary",
    ...{ class: "link-btn" },
}, ...__VLS_functionalComponentArgsRest(__VLS_49));
let __VLS_52;
let __VLS_53;
let __VLS_54;
const __VLS_55 = {
    onClick: (...[$event]) => {
        __VLS_ctx.router.push('/user/login');
    }
};
__VLS_51.slots.default;
var __VLS_51;
var __VLS_39;
var __VLS_7;
var __VLS_3;
/** @type {__VLS_StyleScopedClasses['auth-wrap']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-orb']} */ ;
/** @type {__VLS_StyleScopedClasses['orb-left']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-orb']} */ ;
/** @type {__VLS_StyleScopedClasses['orb-right']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-card']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-head']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-badge']} */ ;
/** @type {__VLS_StyleScopedClasses['sub']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-form']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['primary-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['link-btn']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            router: router,
            loading: loading,
            form: form,
            submit: submit,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
