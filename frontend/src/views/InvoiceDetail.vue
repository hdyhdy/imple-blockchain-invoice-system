<template>
  <div class="invoice-detail-container">
    <!-- 加载状态 -->
    <a-spin :spinning="loading">
      <a-card :bordered="false" class="detail-card">
        <template #title>
          <a-space>
            <ArrowLeftOutlined @click="handleBack" style="cursor: pointer" />
            <span>发票详情</span>
            <a-tag :color="getStatusColor(invoice?.status)">
              {{ getStatusText(invoice?.status) }}
            </a-tag>
          </a-space>
        </template>
        <template #extra>
          <a-space>
            <a-button type="primary" @click="handlePrint">
              <PrinterOutlined /> 打印
            </a-button>
            <a-button @click="handleDownloadPdf">
              <DownloadOutlined /> 下载PDF
            </a-button>
          </a-space>
        </template>

        <div v-if="invoice" class="invoice-paper">
          <!-- 发票头部 -->
          <div class="invoice-header">
            <div class="invoice-title">电子发票</div>
            <div class="invoice-code">
              <span>发票代码：</span>
              <strong>{{ invoice.invoiceCode }}</strong>
            </div>
          </div>

          <!-- 发票信息 -->
          <a-descriptions :column="2" bordered size="small" class="info-table">
            <a-descriptions-item label="发票号码" :span="2">
              <strong>{{ invoice.invoiceNumber }}</strong>
            </a-descriptions-item>
            <a-descriptions-item label="开票日期">
              {{ invoice.invoiceDate }}
            </a-descriptions-item>
            <a-descriptions-item label="校验码">
              {{ invoice.checkCode }}
            </a-descriptions-item>

            <!-- 购买方 -->
            <a-descriptions-item label="购买方名称" :span="2">
              {{ invoice.buyerName }}
            </a-descriptions-item>
            <a-descriptions-item label="购买方税号" :span="2">
              {{ invoice.buyerTaxId || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="购买方地址电话" :span="2">
              {{ invoice.buyerAddressPhone || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="购买方开户行账号" :span="2">
              {{ invoice.buyerBankAccount || '-' }}
            </a-descriptions-item>

            <!-- 销售方 -->
            <a-descriptions-item label="销售方名称" :span="2">
              {{ invoice.sellerName }}
            </a-descriptions-item>
            <a-descriptions-item label="销售方税号" :span="2">
              {{ invoice.sellerTaxId || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="销售方地址电话" :span="2">
              {{ invoice.sellerAddressPhone || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="销售方开户行账号" :span="2">
              {{ invoice.sellerBankAccount || '-' }}
            </a-descriptions-item>
          </a-descriptions>

          <!-- 发票明细 -->
          <a-table
            :columns="itemColumns"
            :data-source="invoice.items || []"
            :pagination="false"
            size="small"
            class="items-table"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'amount'">
                <span>¥{{ formatAmount(record.amount) }}</span>
              </template>
              <template v-else-if="column.key === 'taxRate'">
                {{ (record.taxRate * 100).toFixed(0) }}%
              </template>
              <template v-else-if="column.key === 'taxAmount'">
                <span style="color: #f5222d">¥{{ formatAmount(record.taxAmount) }}</span>
              </template>
            </template>
          </a-table>

          <!-- 金额汇总 -->
          <div class="amount-summary">
            <div class="summary-item">
              <span>不含税金额：</span>
              <strong>¥{{ formatAmount(invoice.netAmount) }}</strong>
            </div>
            <div class="summary-item">
              <span>税额：</span>
              <strong style="color: #f5222d">¥{{ formatAmount(invoice.taxAmount) }}</strong>
            </div>
            <div class="summary-item total">
              <span>价税合计：</span>
              <strong>¥{{ formatAmount(invoice.totalAmount) }}</strong>
            </div>
          </div>

          <!-- 区块链存证信息 -->
          <a-divider>区块链存证信息</a-divider>
          <a-descriptions :column="2" bordered size="small">
            <a-descriptions-item label="区块链存证">
              <a-tag :color="invoice.chainProofExists === 1 ? 'success' : 'default'">
                {{ invoice.chainProofExists === 1 ? '已上链' : '未上链' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="区块高度">
              {{ invoice.blockHeight || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="交易哈希" :span="2">
              <a-typography-paragraph
                :copyable="true"
                :ellipsis="true"
                style="max-width: 600px; margin-bottom: 0"
              >
                {{ invoice.txHash || '-' }}
              </a-typography-paragraph>
            </a-descriptions-item>
          </a-descriptions>

          <!-- 底部信息 -->
          <div class="invoice-footer">
            <div>开票日期：{{ invoice.invoiceDate }}</div>
            <div>此发票仅作为交易凭证，请妥善保管</div>
          </div>
        </div>
      </a-card>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  PrinterOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'
import { invoiceApi } from '../api/invoice'
import type { Invoice } from '../types/invoice'

const router = useRouter()
const route = useRoute()

// 发票数据
const invoice = ref<Invoice | null>(null)
const loading = ref(false)

// 明细表列定义
const itemColumns = [
  {
    title: '商品名称',
    dataIndex: 'itemName',
    key: 'itemName'
  },
  {
    title: '规格型号',
    dataIndex: 'specification',
    key: 'specification'
  },
  {
    title: '单位',
    dataIndex: 'unit',
    key: 'unit',
    width: 60
  },
  {
    title: '数量',
    dataIndex: 'quantity',
    key: 'quantity',
    width: 80,
    align: 'right'
  },
  {
    title: '单价',
    dataIndex: 'unitPrice',
    key: 'unitPrice',
    width: 100,
    align: 'right'
  },
  {
    title: '金额',
    key: 'amount',
    width: 120,
    align: 'right'
  },
  {
    title: '税率',
    key: 'taxRate',
    width: 80,
    align: 'center'
  },
  {
    title: '税额',
    key: 'taxAmount',
    width: 120,
    align: 'right'
  }
]

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

// 获取发票详情
const fetchInvoiceDetail = async () => {
  const id = Number(route.params.id)
  if (!id) {
    message.error('参数错误')
    router.push('/')
    return
  }

  loading.value = true
  try {
    const res = await invoiceApi.getInvoiceDetailWithItems(id)
    if (res.code === 200) {
      invoice.value = res.data
    } else {
      message.error(res.message || '获取详情失败')
    }
  } catch (error) {
    message.error('请求失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 返回列表
const handleBack = () => {
  router.push('/')
}

// 打印
const handlePrint = () => {
  const printContent = document.querySelector('.invoice-paper')
  if (!printContent) return

  const printWindow = window.open('', '_blank')
  if (!printWindow) {
    message.error('无法打开打印窗口，请检查浏览器设置')
    return
  }

  printWindow.document.write(`
    <html>
      <head>
        <title>发票打印 - ${invoice.value?.invoiceCode}-${invoice.value?.invoiceNumber}</title>
        <style>
          body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            padding: 20px;
          }
          .invoice-paper {
            max-width: 800px;
            margin: 0 auto;
            background: #fff;
          }
          .invoice-header {
            text-align: center;
            padding: 20px 0;
            border-bottom: 2px solid #333;
          }
          .invoice-title {
            font-size: 28px;
            font-weight: bold;
            margin-bottom: 10px;
          }
          .info-table {
            margin: 20px 0;
          }
          .items-table {
            margin: 20px 0;
          }
          .amount-summary {
            display: flex;
            justify-content: flex-end;
            gap: 40px;
            padding: 20px 0;
            border-top: 1px solid #ddd;
          }
          .summary-item {
            font-size: 14px;
          }
          .summary-item.total {
            font-size: 16px;
            color: #f5222d;
          }
          .invoice-footer {
            text-align: center;
            padding: 20px 0;
            color: #999;
            font-size: 12px;
          }
          @media print {
            body { padding: 0; }
            .invoice-paper { max-width: 100%; }
          }
        </style>
      </head>
      <body>
        ${printContent.innerHTML}
      </body>
    </html>
  `)
  printWindow.document.close()
  printWindow.focus()
  setTimeout(() => {
    printWindow.print()
    printWindow.close()
  }, 250)
}

// 下载PDF
const handleDownloadPdf = async () => {
  const id = Number(route.params.id)
  if (!id) return

  try {
    const res = await invoiceApi.downloadPdf(id)
    const blob = new Blob([res as any], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `invoice_${invoice.value?.invoiceCode}_${invoice.value?.invoiceNumber}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    message.success('下载成功')
  } catch (error) {
    message.error('下载失败')
  }
}

// 初始化
onMounted(() => {
  fetchInvoiceDetail()
})
</script>

<style scoped>
.invoice-detail-container {
  max-width: 1000px;
  margin: 0 auto;
}

.detail-card {
  border-radius: 8px;
}

.invoice-paper {
  background: #fff;
  padding: 20px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.invoice-header {
  text-align: center;
  padding: 20px 0;
  border-bottom: 2px solid #333;
  margin-bottom: 20px;
}

.invoice-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 10px;
}

.invoice-code {
  font-size: 14px;
  color: #666;
}

.invoice-code strong {
  color: #333;
}

.info-table {
  margin: 20px 0;
}

.items-table {
  margin: 20px 0;
}

.amount-summary {
  display: flex;
  justify-content: flex-end;
  gap: 40px;
  padding: 20px 0;
  border-top: 1px solid #ddd;
}

.summary-item {
  font-size: 14px;
}

.summary-item.total {
  font-size: 18px;
  color: #f5222d;
}

.invoice-footer {
  text-align: center;
  padding: 20px 0;
  color: #999;
  font-size: 12px;
  border-top: 1px solid #e8e8e8;
  margin-top: 20px;
}

:deep(.ant-descriptions-bordered .ant-descriptions-item-label) {
  background: #fafafa;
  font-weight: 500;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
}
</style>
