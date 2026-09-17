<script setup lang="ts">
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
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : "注册失败");
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="auth-wrap">
    <div class="bg-orb orb-left"></div>
    <div class="bg-orb orb-right"></div>

    <el-card class="auth-card" shadow="never">
      <div class="auth-head">
        <div class="auth-badge">Create account</div>
        <h2>注册</h2>
        <p class="sub">创建账号后即可登录，进入口红商城与推荐模块。</p>
      </div>

      <el-form class="auth-form" label-width="90px" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            autocomplete="new-password"
            placeholder="至少 6 位"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="form.password2"
            type="password"
            autocomplete="new-password"
            placeholder="再次输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item class="auth-actions">
          <el-button class="primary-btn" type="primary" :loading="loading" @click="submit">注册</el-button>
          <el-button link type="primary" class="link-btn" @click="router.push('/user/login')">已有账号？去登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<style scoped>
.auth-wrap {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
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
  width: 420px;
  height: 420px;
  left: -120px;
  top: 20px;
  background: radial-gradient(circle, rgba(215, 110, 161, 0.35), rgba(215, 110, 161, 0));
}

.orb-right {
  width: 480px;
  height: 480px;
  right: -140px;
  bottom: -20px;
  background: radial-gradient(circle, rgba(108, 99, 255, 0.22), rgba(108, 99, 255, 0));
}

.auth-card {
  width: min(520px, 100%);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(24px) saturate(1.4);
  -webkit-backdrop-filter: blur(24px) saturate(1.4);
  box-shadow:
    0 8px 32px rgba(125, 34, 71, 0.10),
    0 2px 8px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
  position: relative;
  z-index: 1;
  padding: 48px 44px 40px;
}

.auth-card :deep(.el-card__body) {
  padding: 0;
}

.auth-head {
  text-align: center;
  margin-bottom: 32px;
}

.auth-badge {
  display: inline-block;
  padding: 8px 20px;
  margin-bottom: 16px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.1em;
  color: #7b2146;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(209, 138, 168, 0.4);
  box-shadow: 0 2px 8px rgba(125, 34, 71, 0.08);
}

.auth-head h2 {
  margin: 0 0 14px;
  font-size: 32px;
  font-weight: 700;
  color: #3d1528;
  letter-spacing: 0.04em;
}

.sub {
  margin: 0;
  color: #4d525d;
  line-height: 1.7;
  font-size: 14px;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.auth-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #4a2030;
  font-size: 14px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(209, 138, 168, 0.2);
  padding: 4px 12px;
  transition: all 0.25s ease;
}

.auth-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(190, 94, 135, 0.45);
  box-shadow: 0 2px 8px rgba(125, 34, 71, 0.08);
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(125, 34, 71, 0.5);
  box-shadow: 0 2px 12px rgba(125, 34, 71, 0.12);
}

.auth-actions {
  margin-top: 28px;
}

.auth-actions :deep(.el-form-item__content) {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.primary-btn {
  background: linear-gradient(135deg, #7d2247, #c4688e);
  border: none;
  border-radius: 12px;
  padding: 10px 32px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.04em;
  box-shadow: 0 4px 16px rgba(125, 34, 71, 0.25);
  transition: all 0.3s ease;
}

.primary-btn:hover {
  background: linear-gradient(135deg, #922854, #d47a9f);
  box-shadow: 0 6px 24px rgba(125, 34, 71, 0.35);
  transform: translateY(-1px);
}

.link-btn {
  font-weight: 600;
  font-size: 14px;
}
</style>
