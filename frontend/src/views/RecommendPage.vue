<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import http from "../api/http";
import type { RecommendItem } from "../types/models";

const router = useRouter();
const loading = ref(false);
const items = ref<RecommendItem[]>([]);
const target = ref<"girlfriend" | "self">("girlfriend");

const aiLoading = ref(false);
const aiMessages = ref<{ role: "user" | "assistant"; content: string }[]>([]);
const aiInput = ref("");
const aiGender = ref("");
const aiSkinTone = ref("");
const aiSkinType = ref("");

const categoryMap: Record<string, string> = {
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
    const res = await http.get<{ success: boolean; data: RecommendItem[] }>("/recommend", {
      params: { target: target.value }
    });
    items.value = res.data.data;
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : "加载失败");
  } finally {
    loading.value = false;
  }
}

function goDetail(id: number) {
  router.push(`/user/product/${id}`);
}

function getCategoryLabel(key: string | null): string {
  if (!key) return "";
  return categoryMap[key] || key;
}

async function sendAiMessage() {
  const userText = aiInput.value.trim();
  if (!userText) return;

  aiMessages.value.push({ role: "user", content: userText });
  aiInput.value = "";

  aiLoading.value = true;
  try {
    const res = await http.post<{ success: boolean; data: string }>("/recommend/ai", {
      message: userText
    });
    aiMessages.value.push({ role: "assistant", content: res.data.data });
  } catch (e: unknown) {
    aiMessages.value.push({
      role: "assistant",
      content: "抱歉，小红暂时无法回复，请稍后再试～"
    });
  } finally {
    aiLoading.value = false;
  }
}

function buildAiPrompt() {
  const parts: string[] = [];
  if (aiGender.value) parts.push(`我的性别是${aiGender.value}`);
  if (aiSkinTone.value) parts.push(`肤色是${aiSkinTone.value}`);
  if (aiSkinType.value) parts.push(`肤质是${aiSkinType.value}`);
  if (parts.length > 0) {
    aiInput.value = parts.join("，") + "，请为我推荐合适的口红";
  }
}

load();
</script>

<template>
  <section class="recommend-wrap">
    <div class="bg-orb orb-left"></div>
    <div class="bg-orb orb-right"></div>

    <div class="recommend-header">
      <h2>产品推荐</h2>
      <p>选择推荐场景或与AI助手小红对话，获取个性化推荐</p>
    </div>

    <div class="two-columns">
      <div class="column">
        <div class="section-card">
          <div class="section-head">
            <div class="section-icon">★</div>
            <h3>站主推荐</h3>
          </div>
          <p class="section-desc">
            基于用户画像进行差异化推荐。
            <template v-if="target === 'girlfriend'">为女朋友推荐时，侧重热门色号与经典选择。</template>
            <template v-else>为自己推荐时，侧重肤质匹配与日常百搭。</template>
          </p>

          <el-space wrap class="mb-16">
            <el-radio-group v-model="target" @change="load">
              <el-radio-button label="girlfriend">为 Ta 推荐</el-radio-button>
              <el-radio-button label="self">为自己推荐</el-radio-button>
            </el-radio-group>
            <el-button type="primary" :loading="loading" @click="load">刷新</el-button>
          </el-space>

          <div v-loading="loading" class="recommend-list">
            <div v-for="item in items" :key="item.productId" class="recommend-card">
              <div class="card-left">
                <span v-if="item.colorHex" class="color-dot" :style="{ background: item.colorHex }" />
              </div>
              <div class="card-body">
                <div class="card-title">{{ item.title }}</div>
                <div class="card-meta">
                  {{ item.brand }} · {{ item.shade }} · {{ getCategoryLabel(item.category) }} · ¥{{ item.price }}
                </div>
                <div class="card-reason">{{ item.reason }}</div>
              </div>
              <div class="card-action">
                <el-button link type="primary" @click="goDetail(item.productId)">查看详情</el-button>
              </div>
            </div>
            <el-empty v-if="!loading && items.length === 0" description="暂无推荐" />
          </div>
        </div>
      </div>

      <div class="column">
        <div class="section-card ai-card">
          <div class="section-head">
            <div class="section-icon ai-icon">AI</div>
            <h3>AI 智能推荐</h3>
          </div>
          <p class="section-desc">
            你好，我是小红！告诉我你的性别、肤色和肤质，我来为你推荐最合适的口红～
          </p>

          <div class="ai-quick-tags">
            <span class="tag-label">性别：</span>
            <el-radio-group v-model="aiGender" size="small">
              <el-radio-button label="女" />
              <el-radio-button label="男" />
            </el-radio-group>
          </div>
          <div class="ai-quick-tags">
            <span class="tag-label">肤色：</span>
            <el-radio-group v-model="aiSkinTone" size="small">
              <el-radio-button label="暖皮" />
              <el-radio-button label="冷皮" />
              <el-radio-button label="中性皮" />
            </el-radio-group>
          </div>
          <div class="ai-quick-tags">
            <span class="tag-label">肤质：</span>
            <el-radio-group v-model="aiSkinType" size="small">
              <el-radio-button label="干性" />
              <el-radio-button label="油性" />
              <el-radio-button label="中性" />
            </el-radio-group>
          </div>
          <el-button size="small" class="quick-btn" @click="buildAiPrompt">快速生成提问</el-button>

          <div class="chat-area">
            <div class="chat-messages">
              <div
                v-for="(msg, idx) in aiMessages"
                :key="idx"
                class="chat-msg"
                :class="msg.role"
              >
                <div class="msg-avatar">{{ msg.role === 'user' ? '我' : '红' }}</div>
                <div class="msg-bubble">{{ msg.content }}</div>
              </div>
              <div v-if="aiMessages.length === 0" class="chat-placeholder">
                👋 嗨！我是小红，你的专属口红推荐助手。<br />
                请告诉我你的性别、肤色、肤质，或者直接描述你的需求，我来帮你挑选！
              </div>
            </div>

            <div class="chat-input-area">
              <el-input
                v-model="aiInput"
                placeholder="输入你的需求，如：我是暖皮干性肤质的女生，推荐日常通勤口红"
                @keyup.enter="sendAiMessage"
              />
              <el-button
                type="primary"
                :loading="aiLoading"
                @click="sendAiMessage"
              >
                发送
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.recommend-wrap {
  min-height: calc(100vh - 120px);
  padding: 30px 24px;
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
  width: 280px;
  height: 280px;
  left: -70px;
  top: 40px;
  background: radial-gradient(circle, rgba(215, 110, 161, 0.28), rgba(215, 110, 161, 0));
}

.orb-right {
  width: 340px;
  height: 340px;
  right: -90px;
  bottom: 10px;
  background: radial-gradient(circle, rgba(108, 99, 255, 0.2), rgba(108, 99, 255, 0));
}

.recommend-header {
  text-align: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.recommend-header h2 {
  margin: 0 0 8px;
  font-size: 32px;
  color: #512337;
}

.recommend-header p {
  margin: 0;
  color: #4d525d;
}

.two-columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

@media (max-width: 900px) {
  .two-columns {
    grid-template-columns: 1fr;
  }
}

.section-card {
  padding: 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(9px);
}

.section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.section-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(140deg, #8f2d56, #d7739a);
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-icon {
  background: linear-gradient(140deg, #4f46e5, #7c3aed);
}

.section-head h3 {
  margin: 0;
  color: #4f2035;
}

.section-desc {
  color: #4d525d;
  font-size: 14px;
  margin: 0 0 16px;
  line-height: 1.6;
}

.mb-16 {
  margin-bottom: 16px;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recommend-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  transition: box-shadow 0.2s;
}

.recommend-card:hover {
  box-shadow: 0 4px 12px rgba(125, 34, 71, 0.08);
}

.card-left {
  flex-shrink: 0;
  padding-top: 2px;
}

.color-dot {
  display: inline-block;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
}

.card-meta {
  color: #6b7280;
  font-size: 12px;
  margin-top: 3px;
}

.card-reason {
  color: #4b5563;
  font-size: 12px;
  margin-top: 4px;
  line-height: 1.5;
}

.card-action {
  flex-shrink: 0;
  padding-top: 2px;
}

.ai-quick-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.tag-label {
  font-size: 13px;
  color: #6f7480;
  white-space: nowrap;
}

.quick-btn {
  margin-bottom: 16px;
  background: linear-gradient(130deg, #7d2247, #be5e87);
  border: none;
  color: #fff;
}

.chat-area {
  display: flex;
  flex-direction: column;
  height: 360px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
}

.chat-placeholder {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.8;
  text-align: center;
  padding: 40px 20px;
}

.chat-msg {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  align-items: flex-start;
}

.chat-msg.assistant {
  flex-direction: row;
}

.chat-msg.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.chat-msg.user .msg-avatar {
  background: #e0e7ff;
  color: #4338ca;
}

.chat-msg.assistant .msg-avatar {
  background: linear-gradient(140deg, #8f2d56, #d7739a);
  color: #fff;
}

.msg-bubble {
  max-width: 75%;
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-msg.user .msg-bubble {
  background: #7d2247;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.chat-msg.assistant .msg-bubble {
  background: #f3f4f6;
  color: #1f2937;
  border-bottom-left-radius: 4px;
}

.chat-input-area {
  display: flex;
  gap: 8px;
  padding: 10px 14px;
  border-top: 1px solid #e5e7eb;
  background: #fafafa;
}

.chat-input-area .el-button {
  flex-shrink: 0;
  background: linear-gradient(130deg, #7d2247, #be5e87);
  border: none;
}
</style>
