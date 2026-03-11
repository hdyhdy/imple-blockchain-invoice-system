import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'InvoiceList',
    component: () => import('../views/InvoiceList.vue'),
    meta: { title: '发票管理' }
  },
  {
    path: '/invoice/issue',
    name: 'InvoiceIssue',
    component: () => import('../views/InvoiceIssue.vue'),
    meta: { title: '发票签发' }
  },
  {
    path: '/invoice/detail/:id',
    name: 'InvoiceDetail',
    component: () => import('../views/InvoiceDetail.vue'),
    meta: { title: '发票详情' }
  },
  {
    path: '/verify',
    name: 'InvoiceVerify',
    component: () => import('../views/InvoiceVerify.vue'),
    meta: { title: '发票验证', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - 区块链电子发票系统`
  }
  next()
})

export default router
