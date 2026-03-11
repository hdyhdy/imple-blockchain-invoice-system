<template>
  <div class="invoice-verify-container">
    <div class="verify-card">
      <!-- 头部 -->
      <div class="verify-header">
        <BlockOutlined class="header-icon" />
        <h1>发票验证</h1>
        <p class="header-desc">
          基于区块链存证的电子发票真伪验证系统
        </p>
      </div>

      <!-- 验证表单 -->
      <div class="verify-form">
        <a-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 16 }"
          layout="horizontal"
          @finish="handleVerify"
        >
          <a-form-item label="发票代码" name="invoiceCode">
            <a-input
              v-model:value="formData.invoiceCode"
              placeholder="请输入发票代码"
              :maxlength="20"
              size="large"
            />
          </a-form-item>
          <a-form-item label="发票号码" name="invoiceNumber">
            <a-input
              v-model:value="formData.invoiceNumber"
              placeholder="请输入发票号码"
              :maxlength="20"
              size="large"
            />
          </a-form-item>
          <a-form-item label="校验码" name="checkCode">
            <a-input
              v-model:value="formData.checkCode"
              placeholder="请输入校验码（后6位）"
              :maxlength="6"
              size="large"
            />
          </a-form-item>
          <a-form-item label="金额" name="totalAmount">
            <a-input-number
              v-model:value="formData.totalAmount"
              placeholder="请输入开票金额"
              :min="0"
              :precision="2"
              :formatter="(value: number) => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
              :parser="(value: string) => value.replace(/¥\s?|(,*)/g, '')"
              size="large"
              style="width: 100%"
            />
          </a-form-item>
          <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              :loading="verifying"
              block
            >
              <template #icon><SafetyCertificateOutlined /></template>
              验证发票
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <!-- 验证结果 -->
      <div v-if="verifyResult" class="verify-result">
        <!-- 验证成功 -->
        <a-result
          v-if="verifyResult.valid"
          status="success"
          :title="verifyResult.blockchainVerified ? '验证成功 - 已上链存证' : '验证成功'"
          :sub-title="verifyResult.blockchainVerified
            ? '该发票已通过区块链存证验证，信息真实有效'
            : '该发票信息验证通过，但区块链存证尚未确认'"
        >
          <template #icon>
            <CheckCircleOutlined style="color: #52c41a; font-size: 72px" />
          </template>
          <template #extra>
            <a-card v-if="verifyResult.invoiceInfo" :bordered="false" class="result-card">
              <a-descriptions :column="2" bordered size="small">
                <a-descriptions-item label="发票代码" :span="2">
                  {{ verifyResult.invoiceInfo.invoiceCode }}
                </a-descriptions-item>
                <a-descriptions-item label="发票号码" :span="2">
                  {{ verifyResult.invoiceInfo.invoiceNumber }}
                </a-descriptions-item>
                <a-descriptions-item label="购买方">
                  {{ verifyResult.invoiceInfo.buyerName }}
                </a-descriptions-item>
                <a-descriptions-item label="销售方">
                  {{ verifyResult.invoiceInfo.sellerName }}
                </a-descriptions-item>
                <a-descriptions-item label="开票日期">
                  {{ verifyResult.invoiceInfo.invoiceDate }}
                </a-descriptions-item>
                <a-descriptions-item label="金额">
                  <span style="color: #f5222d; font-weight: 500">
                    ¥{{ formatAmount(verifyResult.invoiceInfo.totalAmount) }}
                  </span>
                </a-descriptions-item>
                <a-descriptions-item label="状态" :span="2">
                  <a-tag :color="getStatusColor(verifyResult.invoiceInfo.status)">
                    {{ getStatusText(verifyResult.invoiceInfo.status) }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item
                  v-if="verifyResult.blockchainVerified"
                  label="区块链存证"
                  :span="2"
                >
                  <a-tag color="success">
                    <CheckCircleOutlined /> 已上链
                  </a-tag>
                </a-descriptions-item>
              </a-descriptions>
            </a-card>
          </template>
        </a-result>

        <!-- 验证失败 -->
        <a-result
          v-else
          status="error"
          title="验证失败"
          sub-title="发票信息不匹配，请检查输入的信息是否正确"
        >
          <template #icon>
            <CloseCircleOutlined style="color: #ff4d4f; font-size: 72px" />
          </template>
          <template #extra>
            <a-button type="primary" @click="handleReset">
              重新验证
            </a-button>
          </template>
        </a-result>
      </div>

      <!-- 提示信息 -->
      <div class="verify-tips">
        <a-alert
          type="info"
          show-icon
          message="验证说明"
          description="请输入发票代码、发票号码、校验码（后6位）和开票金额进行验证。验证结果仅供参考，具体以税务机关查询为准。"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  BlockOutlined,
  SafetyCertificateOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue'
import { invoiceApi } from '../api/invoice'
import type { InvoiceVerifyRequest, InvoiceVerifyResponse } from '../types/invoice'

// 表单数据
const formData = reactive<InvoiceVerifyRequest>({
  invoiceCode: '',
  invoiceNumber: '',
  checkCode: '',
  totalAmount: 0
})

// 表单校验规则
const rules = {
  invoiceCode: [{ required: true, message: '请输入发票代码', trigger: 'blur' }],
  invoiceNumber: [{ required: true, message: '请输入发票号码', trigger: 'blur' }],
  checkCode: [
    { required: true, message: '请输入校验码', trigger: 'blur' },
    { len: 6, message: '校验码为6位', trigger: 'blur' }
  ],
  totalAmount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ]
}

// 验证状态
const verifying = ref(false)
const verifyResult = ref<InvoiceVerifyResponse | null>(null)
const formRef = ref()

// 格式化金额
const formatAmount = (amount: number | string) => {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  return num.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 获取状态颜色
const getStatusColor = (status: number | undefined) => {
  const colorMap: Record<number, string> = {
    0: 'orange',
    1: 'green',
    2: 'red'
  }
  return colorMap[status as number] || 'default'
}

// 获取状态文本
const getStatusText = (status: number | undefined) => {
  const textMap: Record<number, string> = {
    0: '待签发',
    1: '已签发',
    2: '已作废'
  }
  return textMap[status as number] || '未知'
}

// 验证发票
const handleVerify = async () => {
  verifying.value = true
  verifyResult.value = null

  try {
    const res = await invoiceApi.verifyInvoice(formData)
    if (res.code === 200) {
      verifyResult.value = res.data
    } else {
      verifyResult.value = {
        valid: false,
        blockchainVerified: false
      }
    }
  } catch (error) {
    verifyResult.value = {
      valid: false,
      blockchainVerified: false
    }
    message.error('验证请求失败，请稍后重试')
  } finally {
    verifying.value = false
  }
}

// 重置
const handleReset = () => {
  formRef.value?.resetFields()
  verifyResult.value = null
}
</script>

<style scoped>
.invoice-verify-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.verify-card {
  width: 100%;
  max-width: 600px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.verify-header {
  text-align: center;
  margin-bottom: 40px;
}

.header-icon {
  font-size: 48px;
  color: #1890ff;
  margin-bottom: 16px;
}

.verify-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.header-desc {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.verify-form {
  margin-bottom: 24px;
}

.verify-result {
  margin: 24px 0;
}

.result-card {
  max-width: 500px;
  margin: 0 auto;
}

.verify-tips {
  margin-top: 24px;
}

:deep(.ant-result-title) {
  color: #333;
}

:deep(.ant-result-sub-title) {
  color: #666;
}
</style>
