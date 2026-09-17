import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useAuthStore } from "../stores/auth";
const auth = useAuthStore();
const loading = ref(false);
const pwdLoading = ref(false);
const profile = reactive({
    username: "",
    role: "",
    gender: "",
    skinTone: "",
    skinType: ""
});
const pwd = reactive({
    oldPassword: "",
    newPassword: "",
    newPassword2: ""
});
async function load() {
    loading.value = true;
    try {
        const me = await auth.fetchMe();
        if (!me)
            return;
        profile.username = me.username;
        profile.role = me.role;
        profile.gender = me.gender ?? "";
        profile.skinTone = me.skinTone ?? "";
        profile.skinType = me.skinType ?? "";
    }
    catch (e) {
        ElMessage.error(e instanceof Error ? e.message : "加载失败");
    }
    finally {
        loading.value = false;
    }
}
async function saveProfile() {
    loading.value = true;
    try {
        await auth.updateProfile({
            gender: profile.gender || undefined,
            skinTone: profile.skinTone || undefined,
            skinType: profile.skinType || undefined
        });
        ElMessage.success("资料已保存");
        await load();
    }
    catch (e) {
        ElMessage.error(e instanceof Error ? e.message : "保存失败");
    }
    finally {
        loading.value = false;
    }
}
async function savePassword() {
    if (!pwd.oldPassword || !pwd.newPassword) {
        ElMessage.warning("请填写原密码与新密码");
        return;
    }
    if (pwd.newPassword.length < 6) {
        ElMessage.warning("新密码至少 6 位");
        return;
    }
    if (pwd.newPassword !== pwd.newPassword2) {
        ElMessage.warning("两次输入的新密码不一致");
        return;
    }
    pwdLoading.value = true;
    try {
        await auth.changePassword(pwd.oldPassword, pwd.newPassword);
        ElMessage.success("密码已更新");
        pwd.oldPassword = "";
        pwd.newPassword = "";
        pwd.newPassword2 = "";
    }
    catch (e) {
        ElMessage.error(e instanceof Error ? e.message : "修改失败");
    }
    finally {
        pwdLoading.value = false;
    }
}
onMounted(load);
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "wrap" },
});
const __VLS_0 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({}));
const __VLS_2 = __VLS_1({}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_3.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "meta" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
(__VLS_ctx.profile.username || "—");
(__VLS_ctx.profile.role || "—");
__VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
const __VLS_4 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_5 = __VLS_asFunctionalComponent(__VLS_4, new __VLS_4({
    labelWidth: "100px",
    disabled: (__VLS_ctx.loading),
}));
const __VLS_6 = __VLS_5({
    labelWidth: "100px",
    disabled: (__VLS_ctx.loading),
}, ...__VLS_functionalComponentArgsRest(__VLS_5));
__VLS_7.slots.default;
const __VLS_8 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({
    label: "性别",
}));
const __VLS_10 = __VLS_9({
    label: "性别",
}, ...__VLS_functionalComponentArgsRest(__VLS_9));
__VLS_11.slots.default;
const __VLS_12 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_13 = __VLS_asFunctionalComponent(__VLS_12, new __VLS_12({
    modelValue: (__VLS_ctx.profile.gender),
    placeholder: "例如：female / male",
}));
const __VLS_14 = __VLS_13({
    modelValue: (__VLS_ctx.profile.gender),
    placeholder: "例如：female / male",
}, ...__VLS_functionalComponentArgsRest(__VLS_13));
var __VLS_11;
const __VLS_16 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_17 = __VLS_asFunctionalComponent(__VLS_16, new __VLS_16({
    label: "肤色",
}));
const __VLS_18 = __VLS_17({
    label: "肤色",
}, ...__VLS_functionalComponentArgsRest(__VLS_17));
__VLS_19.slots.default;
const __VLS_20 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_21 = __VLS_asFunctionalComponent(__VLS_20, new __VLS_20({
    modelValue: (__VLS_ctx.profile.skinTone),
    placeholder: "例如：neutral / warm / cool",
}));
const __VLS_22 = __VLS_21({
    modelValue: (__VLS_ctx.profile.skinTone),
    placeholder: "例如：neutral / warm / cool",
}, ...__VLS_functionalComponentArgsRest(__VLS_21));
var __VLS_19;
const __VLS_24 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_25 = __VLS_asFunctionalComponent(__VLS_24, new __VLS_24({
    label: "肤质",
}));
const __VLS_26 = __VLS_25({
    label: "肤质",
}, ...__VLS_functionalComponentArgsRest(__VLS_25));
__VLS_27.slots.default;
const __VLS_28 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_29 = __VLS_asFunctionalComponent(__VLS_28, new __VLS_28({
    modelValue: (__VLS_ctx.profile.skinType),
    placeholder: "例如：dry / oily / normal",
}));
const __VLS_30 = __VLS_29({
    modelValue: (__VLS_ctx.profile.skinType),
    placeholder: "例如：dry / oily / normal",
}, ...__VLS_functionalComponentArgsRest(__VLS_29));
var __VLS_27;
const __VLS_32 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_33 = __VLS_asFunctionalComponent(__VLS_32, new __VLS_32({}));
const __VLS_34 = __VLS_33({}, ...__VLS_functionalComponentArgsRest(__VLS_33));
__VLS_35.slots.default;
const __VLS_36 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_37 = __VLS_asFunctionalComponent(__VLS_36, new __VLS_36({
    ...{ 'onClick': {} },
    type: "primary",
    loading: (__VLS_ctx.loading),
}));
const __VLS_38 = __VLS_37({
    ...{ 'onClick': {} },
    type: "primary",
    loading: (__VLS_ctx.loading),
}, ...__VLS_functionalComponentArgsRest(__VLS_37));
let __VLS_40;
let __VLS_41;
let __VLS_42;
const __VLS_43 = {
    onClick: (__VLS_ctx.saveProfile)
};
__VLS_39.slots.default;
var __VLS_39;
var __VLS_35;
var __VLS_7;
var __VLS_3;
const __VLS_44 = {}.ElCard;
/** @type {[typeof __VLS_components.ElCard, typeof __VLS_components.elCard, typeof __VLS_components.ElCard, typeof __VLS_components.elCard, ]} */ ;
// @ts-ignore
const __VLS_45 = __VLS_asFunctionalComponent(__VLS_44, new __VLS_44({
    ...{ class: "mt" },
}));
const __VLS_46 = __VLS_45({
    ...{ class: "mt" },
}, ...__VLS_functionalComponentArgsRest(__VLS_45));
__VLS_47.slots.default;
__VLS_asFunctionalElement(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
const __VLS_48 = {}.ElForm;
/** @type {[typeof __VLS_components.ElForm, typeof __VLS_components.elForm, typeof __VLS_components.ElForm, typeof __VLS_components.elForm, ]} */ ;
// @ts-ignore
const __VLS_49 = __VLS_asFunctionalComponent(__VLS_48, new __VLS_48({
    labelWidth: "100px",
}));
const __VLS_50 = __VLS_49({
    labelWidth: "100px",
}, ...__VLS_functionalComponentArgsRest(__VLS_49));
__VLS_51.slots.default;
const __VLS_52 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_53 = __VLS_asFunctionalComponent(__VLS_52, new __VLS_52({
    label: "原密码",
}));
const __VLS_54 = __VLS_53({
    label: "原密码",
}, ...__VLS_functionalComponentArgsRest(__VLS_53));
__VLS_55.slots.default;
const __VLS_56 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_57 = __VLS_asFunctionalComponent(__VLS_56, new __VLS_56({
    modelValue: (__VLS_ctx.pwd.oldPassword),
    type: "password",
    showPassword: true,
}));
const __VLS_58 = __VLS_57({
    modelValue: (__VLS_ctx.pwd.oldPassword),
    type: "password",
    showPassword: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_57));
var __VLS_55;
const __VLS_60 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_61 = __VLS_asFunctionalComponent(__VLS_60, new __VLS_60({
    label: "新密码",
}));
const __VLS_62 = __VLS_61({
    label: "新密码",
}, ...__VLS_functionalComponentArgsRest(__VLS_61));
__VLS_63.slots.default;
const __VLS_64 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_65 = __VLS_asFunctionalComponent(__VLS_64, new __VLS_64({
    modelValue: (__VLS_ctx.pwd.newPassword),
    type: "password",
    showPassword: true,
}));
const __VLS_66 = __VLS_65({
    modelValue: (__VLS_ctx.pwd.newPassword),
    type: "password",
    showPassword: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_65));
var __VLS_63;
const __VLS_68 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_69 = __VLS_asFunctionalComponent(__VLS_68, new __VLS_68({
    label: "确认新密码",
}));
const __VLS_70 = __VLS_69({
    label: "确认新密码",
}, ...__VLS_functionalComponentArgsRest(__VLS_69));
__VLS_71.slots.default;
const __VLS_72 = {}.ElInput;
/** @type {[typeof __VLS_components.ElInput, typeof __VLS_components.elInput, ]} */ ;
// @ts-ignore
const __VLS_73 = __VLS_asFunctionalComponent(__VLS_72, new __VLS_72({
    modelValue: (__VLS_ctx.pwd.newPassword2),
    type: "password",
    showPassword: true,
}));
const __VLS_74 = __VLS_73({
    modelValue: (__VLS_ctx.pwd.newPassword2),
    type: "password",
    showPassword: true,
}, ...__VLS_functionalComponentArgsRest(__VLS_73));
var __VLS_71;
const __VLS_76 = {}.ElFormItem;
/** @type {[typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, typeof __VLS_components.ElFormItem, typeof __VLS_components.elFormItem, ]} */ ;
// @ts-ignore
const __VLS_77 = __VLS_asFunctionalComponent(__VLS_76, new __VLS_76({}));
const __VLS_78 = __VLS_77({}, ...__VLS_functionalComponentArgsRest(__VLS_77));
__VLS_79.slots.default;
const __VLS_80 = {}.ElButton;
/** @type {[typeof __VLS_components.ElButton, typeof __VLS_components.elButton, typeof __VLS_components.ElButton, typeof __VLS_components.elButton, ]} */ ;
// @ts-ignore
const __VLS_81 = __VLS_asFunctionalComponent(__VLS_80, new __VLS_80({
    ...{ 'onClick': {} },
    type: "primary",
    loading: (__VLS_ctx.pwdLoading),
}));
const __VLS_82 = __VLS_81({
    ...{ 'onClick': {} },
    type: "primary",
    loading: (__VLS_ctx.pwdLoading),
}, ...__VLS_functionalComponentArgsRest(__VLS_81));
let __VLS_84;
let __VLS_85;
let __VLS_86;
const __VLS_87 = {
    onClick: (__VLS_ctx.savePassword)
};
__VLS_83.slots.default;
var __VLS_83;
var __VLS_79;
var __VLS_51;
var __VLS_47;
/** @type {__VLS_StyleScopedClasses['wrap']} */ ;
/** @type {__VLS_StyleScopedClasses['meta']} */ ;
/** @type {__VLS_StyleScopedClasses['mt']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            loading: loading,
            pwdLoading: pwdLoading,
            profile: profile,
            pwd: pwd,
            saveProfile: saveProfile,
            savePassword: savePassword,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
