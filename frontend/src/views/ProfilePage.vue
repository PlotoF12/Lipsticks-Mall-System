<script setup lang="ts">
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
    if (!me) return;
    profile.username = me.username;
    profile.role = me.role;
    profile.gender = me.gender ?? "";
    profile.skinTone = me.skinTone ?? "";
    profile.skinType = me.skinType ?? "";
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : "加载失败");
  } finally {
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
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : "保存失败");
  } finally {
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
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : "修改失败");
  } finally {
    pwdLoading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="wrap">
    <el-card>
      <h2>个人中心</h2>
      <p class="meta">当前用户：<strong>{{ profile.username || "—" }}</strong>（角色：{{ profile.role || "—" }}）</p>

      <h3>个人资料</h3>
      <el-form label-width="100px" :disabled="loading">
        <el-form-item label="性别">
          <el-input v-model="profile.gender" placeholder="例如：female / male" />
        </el-form-item>
        <el-form-item label="肤色">
          <el-input v-model="profile.skinTone" placeholder="例如：neutral / warm / cool" />
        </el-form-item>
        <el-form-item label="肤质">
          <el-input v-model="profile.skinType" placeholder="例如：dry / oily / normal" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="saveProfile">保存资料</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="mt">
      <h3>修改密码</h3>
      <el-form label-width="100px">
        <el-form-item label="原密码">
          <el-input v-model="pwd.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwd.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="pwd.newPassword2" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="pwdLoading" @click="savePassword">更新密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.wrap {
  max-width: 720px;
  margin: 24px auto;
}

.meta {
  color: #4b5563;
  margin: 0 0 16px;
}

.mt {
  margin-top: 16px;
}

h3 {
  margin: 8px 0 12px;
  color: #512337;
}
</style>
