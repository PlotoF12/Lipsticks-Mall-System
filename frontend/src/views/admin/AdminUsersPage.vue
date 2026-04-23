<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import http from "../../api/http";
import type { AdminUserRow } from "../../types/models";

const router = useRouter();
const loading = ref(false);
const saving = ref(false);
const users = ref<AdminUserRow[]>([]);

const dialogVisible = ref(false);
const editing = ref<AdminUserRow | null>(null);

const form = reactive({
  enabled: true,
  role: "USER",
  gender: "",
  skinTone: "",
  skinType: ""
});

async function load() {
  loading.value = true;
  try {
    const res = await http.get<{ success: boolean; data: AdminUserRow[] }>("/admin/users");
    users.value = res.data.data;
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : "加载失败");
  } finally {
    loading.value = false;
  }
}

function openEdit(row: AdminUserRow) {
  editing.value = row;
  form.enabled = !!row.enabled;
  form.role = row.role;
  form.gender = row.gender ?? "";
  form.skinTone = row.skinTone ?? "";
  form.skinType = row.skinType ?? "";
  dialogVisible.value = true;
}

async function save() {
  if (!editing.value) return;
  saving.value = true;
  try {
    await http.put(`/admin/users/${editing.value.id}`, {
      enabled: form.enabled,
      role: form.role,
      gender: form.gender.trim() || null,
      skinTone: form.skinTone.trim() || null,
      skinType: form.skinType.trim() || null
    });
    ElMessage.success("已保存");
    dialogVisible.value = false;
    await load();
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : "保存失败");
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="wrap">
    <el-page-header @back="router.push('/admin')">
      <template #content>
        <span class="title">用户管理</span>
      </template>
    </el-page-header>

    <div class="toolbar">
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="users" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'danger'">{{ row.enabled ? "启用" : "禁用" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="gender" label="性别" width="100" />
      <el-table-column prop="skinTone" label="肤色" width="120" />
      <el-table-column prop="skinType" label="肤质" width="120" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="编辑用户" width="560px">
      <template v-if="editing">
        <p class="hint">用户：<strong>{{ editing.username }}</strong></p>
        <el-form label-width="100px">
          <el-form-item label="启用">
            <el-switch v-model="form.enabled" />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="form.role" style="width: 100%">
              <el-option label="USER" value="USER" />
              <el-option label="ADMIN" value="ADMIN" />
            </el-select>
          </el-form-item>
          <el-form-item label="性别">
            <el-input v-model="form.gender" />
          </el-form-item>
          <el-form-item label="肤色">
            <el-input v-model="form.skinTone" />
          </el-form-item>
          <el-form-item label="肤质">
            <el-input v-model="form.skinType" />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.wrap {
  max-width: 1100px;
  margin: 24px auto;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #512337;
}

.toolbar {
  margin: 16px 0;
}

.hint {
  margin: 0 0 12px;
  color: #4b5563;
}
</style>
