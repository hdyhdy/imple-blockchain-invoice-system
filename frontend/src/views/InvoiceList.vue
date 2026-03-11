<template>
  <div class="invoice-list-container">
    <!-- 查询区域 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" @finish="handleSearch">
        <a-form-item label="关键词" name="keyword">
          <a-input
            v-model:value="searchParams.keyword"
            placeholder="发票号码/购买方/销售方"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select
            v-model:value="searchParams.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="0">待签发</a-select-option>
            <a-select-option :value="1">已签发</a-select-option>
            <a-select-option :value="2">已作废</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作区域 -->
    <a-card :bordered="false" class="table-card">
      <template #title>
        <a-space>
          <span>发票列表</span>
          <a-tag color="blue">共 {{ pagination.total }} 条</a-tag>
        </a-space>
      </template>
      <template #extra>
        <a-button type="primary" @click="handleIssue">
          <template #icon><PlusOutlined /></template>
          签发发票
        </a-button>
      </template>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1200 }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'invoiceNumber'">
            <a @click="handleViewDetail(record.id)">
              {{ record.invoiceCode }}-{{ record.invoiceNumber }}
            </a>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'totalAmount'">
            <span style="font-weight: 500; color: #f5222d">
              ¥{{ formatAmount(record.totalAmount) }}
            </span>
          </template>
          <template v-else-if="column.key === 'chainProofExists'">
            <a-tag :color="record.chainProofExists === 1 ? 'success' : 'default'">
              <template #icon>
                <CheckCircleOutlined v-if="record.chainProofExists === 1" />
                <CloseCircleOutlined v-else />
              </template>
              {{ record.chainProofExists === 1 ? '已上链' : '未上链' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleViewDetail(record.id)">
                <template #icon><EyeOutlined /></template>
                查看
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleDownloadPdf(record.id)"
                :disabled="record.status === 0"
              >
                <template #icon><DownloadOutlined /></template>
                下载
              </a-button>
              <a-button
                type="link"
                size="small"
                danger
                @click="handleVoid(record)"
                :disabled="record.status !== 1"
              >
                <template #icon><DeleteOutlined /></template>
                作废
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  EyeOutlined,
  DownloadOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue'
import { invoiceApi } from '../api/invoice'
import type { Invoice } from '../types/invoice'

const router = useRouter()

// 表格列定义
const columns = [
  {
    title: '发票号码',
    key: 'invoiceNumber',
    width: 180,
    fixed: 'left'
  },
  {
    title: '开票日期',
    dataIndex: 'invoiceDate',
    key: 'invoiceDate',
    width: 120
  },
  {
    title: '购买方',
    dataIndex: 'buyerName',
    key: 'buyerName',
    ellipsis: true
  },
  {
    title: '销售方',
    dataIndex: 'sellerName',
    key: 'sellerName',
    ellipsis: true
  },
  {
    title: '金额（元）',
    key: 'totalAmount',
    width: 120,
    align: 'right'
  },
  {
    title: '状态',
    key: 'status',
    width: 100
  },
  {
    title: '区块链存证',
    key: 'chainProofExists',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 查询参数
const searchParams = reactive({
  keyword: '',
  status: undefined as number | undefined
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格数据
const tableData = ref<Invoice[]>([])
const loading = ref(false)

// 格式化金额
const formatAmount = (amount: number | string) => {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  return num.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 获取状态颜色
const getStatusColor = (status: number) => {
  const colorMap: Record<number, string> = {
    0: 'orange',
    1: 'green',
    2: 'red'
  }
  return colorMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待签发',
    1: '已签发',
    2: '已作废'
  }
  return textMap[status] || '未知'
}

// 查询发票列表
const fetchInvoiceList = async () => {
  loading.value = true
  try {
    const res = await invoiceApi.getInvoiceList({
      page: pagination.current,
      size: pagination.pageSize,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    } else {
      message.error(res.message || '查询失败')
    }
  } catch (error) {
    message.error('请求失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  pagination.current = 1
  fetchInvoiceList()
}

// 重置
const handleReset = () => {
  searchParams.keyword = ''
  searchParams.status = undefined
  pagination.current = 1
  fetchInvoiceList()
}

// 分页变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchInvoiceList()
}

// 签发发票
const handleIssue = () => {
  router.push('/invoice/issue')
}

// 查看详情
const handleViewDetail = (id: number) => {
  router.push(`/invoice/detail/${id}`)
}

// 下载PDF
const handleDownloadPdf = async (id: number) => {
  try {
    const res = await invoiceApi.downloadPdf(id)
    const blob = new Blob([res as any], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `invoice_${id}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    message.success('下载成功')
  } catch (error) {
    message.error('下载失败')
  }
}

// 作废发票
const handleVoid = (record: Invoice) => {
  Modal.confirm({
    title: '确认作废',
    content: `确定要作废发票 ${record.invoiceCode}-${record.invoiceNumber} 吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await invoiceApi.voidInvoice(record.id)
        if (res.code === 200) {
          message.success('作废成功')
          fetchInvoiceList()
        } else {
          message.error(res.message || '作废失败')
        }
      } catch (error) {
        message.error('作废失败，请稍后重试')
      }
    }
  })
}

// 初始化
onMounted(() => {
  fetchInvoiceList()
})
</script>

<style scoped>
.invoice-list-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card {
  border-radius: 8px;
}

.table-card {
  border-radius: 8px;
}

:deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
}
</style>
