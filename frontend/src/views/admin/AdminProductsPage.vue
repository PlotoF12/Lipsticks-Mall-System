<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import http from "../../api/http";
import type { LipstickProduct } from "../../types/models";

const router = useRouter();
const loading = ref(false);
const saving = ref(false);
const products = ref<LipstickProduct[]>([]);

const dialogVisible = ref(false);
const editingId = ref<number | null>(null);

const form = reactive({
  title: "",
  brand: "",
  shade: "",
  colorHex: "",
  category: "",
  finishType: "",
  detail: "",
  price: 0,
  stock: 0,
  onSale: true,
  suitableSkinTone: "",
  suitableGender: "",
  scene: "",
  imageUrl: ""
});

const categoryOptions = [
  { label: "正红", value: "red" },
  { label: "红棕", value: "red_brown" },
  { label: "番茄红", value: "tomato" },
  { label: "豆沙", value: "bean_paste" },
  { label: "粉色", value: "pink" },
  { label: "玫瑰", value: "rose" },
  { label: "玫瑰棕", value: "rose_brown" },
  { label: "裸色", value: "nude" }
];

const finishOptions = [
  { label: "哑光", value: "matte" },
  { label: "滋润", value: "gloss" },
  { label: "缎面", value: "satin" }
];

const categoryMap: Record<string, string> = Object.fromEntries(
  categoryOptions.map((o) => [o.value, o.label])
);

async function load() {
  loading.value = true;
  try {
    const res = await http.get<{ success: boolean; data: LipstickProduct[] }>("/admin/products");
    products.value = res.data.data;
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : "加载失败");
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  form.title = "";
  form.brand = "";
  form.shade = "";
  form.colorHex = "";
  form.category = "";
  form.finishType = "";
  form.detail = "";
  form.price = 0;
  form.stock = 0;
  form.onSale = true;
  form.suitableSkinTone = "";
  form.suitableGender = "";
  form.scene = "";
  form.imageUrl = "";
}

function openCreate() {
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: LipstickProduct) {
  editingId.value = row.id;
  form.title = row.title;
  form.brand = row.brand ?? "";
  form.shade = row.shade ?? "";
  form.colorHex = row.colorHex ?? "";
  form.category = row.category ?? "";
  form.finishType = row.finishType ?? "";
  form.detail = row.detail ?? "";
  form.price = row.price;
  form.stock = row.stock;
  form.onSale = !!row.onSale;
  form.suitableSkinTone = row.suitableSkinTone ?? "";
  form.suitableGender = row.suitableGender ?? "";
  form.scene = row.scene ?? "";
  form.imageUrl = row.imageUrl ?? "";
  dialogVisible.value = true;
}

async function save() {
  if (!form.title.trim()) {
    ElMessage.warning("请填写标题");
    return;
  }
  saving.value = true;
  try {
    const body = {
      title: form.title.trim(),
      brand: form.brand.trim() || null,
      shade: form.shade.trim() || null,
      colorHex: form.colorHex.trim() || null,
      category: form.category || null,
      finishType: form.finishType || null,
      detail: form.detail.trim() || null,
      price: form.price,
      stock: form.stock,
      onSale: form.onSale,
      suitableSkinTone: form.suitableSkinTone.trim() || null,
      suitableGender: form.suitableGender.trim() || null,
      scene: form.scene.trim() || null,
      imageUrl: form.imageUrl.trim() || null
    };
    if (editingId.value == null) {
      await http.post("/admin/products", body);
      ElMessage.success("已创建");
    } else {
      await http.put(`/admin/products/${editingId.value}`, body);
      ElMessage.success("已保存");
    }
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
        <span class="title">商品管理</span>
      </template>
    </el-page-header>

    <div class="toolbar">
      <el-button type="primary" @click="openCreate">上架新商品</el-button>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="products" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="brand" label="品牌" width="100" />
      <el-table-column prop="shade" label="色号" width="90" />
      <el-table-column label="颜色" width="70" align="center">
        <template #default="{ row }">
          <span v-if="row.colorHex" class="color-dot" :style="{ background: row.colorHex }" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="色系" width="80">
        <template #default="{ row }">{{ categoryMap[row.category] || row.category || "-" }}</template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="80" />
      <el-table-column prop="stock" label="库存" width="70" />
      <el-table-column label="在售" width="70">
        <template #default="{ row }">
          <el-tag :type="row.onSale ? 'success' : 'info'" size="small">{{ row.onSale ? "是" : "否" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '上架商品' : '编辑商品'" width="720px">
      <el-form label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="标题" required>
              <el-input v-model="form.title" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="色号">
              <el-input v-model="form.shade" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="颜色值">
              <el-input v-model="form.colorHex" placeholder="#B3123F" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="色系分类">
              <el-select v-model="form.category" placeholder="选择色系" clearable style="width: 100%">
                <el-option v-for="o in categoryOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="质地">
              <el-select v-model="form.finishType" placeholder="选择质地" clearable style="width: 100%">
                <el-option v-for="o in finishOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详情">
          <el-input v-model="form.detail" type="textarea" :rows="3" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="价格">
              <el-input-number v-model="form.price" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="在售">
              <el-switch v-model="form.onSale" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="适合肤色">
              <el-input v-model="form.suitableSkinTone" placeholder="warm,neutral,cool" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适合性别">
              <el-input v-model="form.suitableGender" placeholder="male,female" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="适用场景">
              <el-input v-model="form.scene" placeholder="日常,约会,职场" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图片路径">
              <el-input v-model="form.imageUrl" placeholder="/images/xxx.jpg" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.wrap {
  max-width: 1200px;
  margin: 24px auto;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #512337;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin: 16px 0;
}

.color-dot {
  display: inline-block;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  border: 1px solid rgba(0, 0, 0, 0.08);
}
</style>
