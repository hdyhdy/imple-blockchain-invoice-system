<template>
  <a-config-provider :theme="themeConfig">
    <a-layout class="layout-container">
      <a-layout-header class="header">
        <div class="logo">
          <BlockOutlined class="logo-icon" />
          <span>区块链电子发票系统</span>
        </div>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          theme="dark"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        ></a-menu>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        区块链电子发票系统 ©2024 - 基于FISCO BCOS区块链存证
      </a-layout-footer>
    </a-layout>
  </a-config-provider>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { BlockOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()

const selectedKeys = ref<string[]>(['invoice'])

const menuItems = [
  { key: 'invoice', label: '发票管理' },
  { key: 'verify', label: '发票验证' }
]

const themeConfig = {
  token: {
    colorPrimary: '#1890ff'
  }
}

const handleMenuClick = ({ key }: { key: string }) => {
  if (key === 'invoice') {
    router.push('/')
  } else if (key === 'verify') {
    router.push('/verify')
  }
}

// 监听路由变化，更新菜单选中状态
watch(
  () => route.path,
  (path) => {
    if (path === '/verify') {
      selectedKeys.value = ['verify']
    } else {
      selectedKeys.value = ['invoice']
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.logo {
  color: white;
  font-size: 18px;
  font-weight: bold;
  margin-right: 40px;
  display: flex;
  align-items: center;
}

.logo-icon {
  font-size: 24px;
  margin-right: 8px;
  color: #1890ff;
}

.content {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px - 69px);
}

.footer {
  text-align: center;
  background: #001529;
  color: rgba(255, 255, 255, 0.65);
}
</style>
