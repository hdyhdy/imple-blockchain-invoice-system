<template>
  <div class="invoice-issue-container">
    <a-card :bordered="false" class="form-card">
      <template #title>
        <a-space>
          <ArrowLeftOutlined @click="handleBack" style="cursor: pointer" />
          <span>发票签发</span>
        </a-space>
      </template>

      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 14 }"
        layout="horizontal"
      >
        <!-- 购买方信息 -->
        <a-divider orientation="left">购买方信息</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="购买方名称" name="buyerName">
              <a-input
                v-model:value="formData.buyerName"
                placeholder="请输入购买方名称"
                :maxlength="100"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="购买方税号" name="buyerTaxId">
              <a-input
                v-model:value="formData.buyerTaxId"
                placeholder="请输入购买方税号"
                :maxlength="50"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="地址电话" name="buyerAddressPhone">
              <a-input
                v-model:value="formData.buyerAddressPhone"
                placeholder="请输入地址和电话"
                :maxlength="200"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="开户行账号" name="buyerBankAccount">
              <a-input
                v-model:value="formData.buyerBankAccount"
                placeholder="请输入开户行及账号"
                :maxlength="100"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 销售方信息 -->
        <a-divider orientation="left">销售方信息</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="销售方名称" name="sellerName">
              <a-input
                v-model:value="formData.sellerName"
                placeholder="请输入销售方名称"
                :maxlength="100"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="销售方税号" name="sellerTaxId">
              <a-input
                v-model:value="formData.sellerTaxId"
                placeholder="请输入销售方税号"
                :maxlength="50"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="地址电话" name="sellerAddressPhone">
              <a-input
                v-model:value="formData.sellerAddressPhone"
                placeholder="请输入地址和电话"
                :maxlength="200"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="开户行账号" name="sellerBankAccount">
              <a-input
                v-model:value="formData.sellerBankAccount"
                placeholder="请输入开户行及账号"
                :maxlength="100"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 发票明细 -->
        <a-divider orientation="left">
          发票明细
          <a-button type="link" @click="handleAddItem">
            <PlusOutlined /> 添加商品
          </a-button>
        </a-divider>

        <a-table
          :columns="itemColumns"
          :data-source="formData.items"
          :pagination="false"
          :scroll="{ x: 1000 }"
          row-key="key"
        >
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.key === 'itemName'">
              <a-form-item
                :name="['items', index, 'itemName']"
                :rules="{ required: true, message: '请输入商品名称' }"
                style="margin: 0"
              >
                <a-input v-model:value="record.itemName" placeholder="商品名称" />
              </a-form-item>
            </template>
            <template v-else-if="column.key === 'specification'">
              <a-form-item style="margin: 0">
                <a-input v-model:value="record.specification" placeholder="规格型号" />
              </a-form-item>
            </template>
            <template v-else-if="column.key === 'unit'">
              <a-form-item style="margin: 0">
                <a-input v-model:value="record.unit" placeholder="单位" style="width: 80px" />
              </a-form-item>
            </template>
            <template v-else-if="column.key === 'quantity'">
              <a-form-item
                :name="['items', index, 'quantity']"
                :rules="{ required: true, message: '请输入数量' }"
                style="margin: 0"
              >
                <a-input-number
                  v-model:value="record.quantity"
                  :min="0.01"
                  :precision="2"
                  placeholder="数量"
                  style="width: 100px"
                  @change="() => calculateItemAmount(index)"
                />
              </a-form-item>
            </template>
            <template v-else-if="column.key === 'unitPrice'">
              <a-form-item
                :name="['items', index, 'unitPrice']"
                :rules="{ required: true, message: '请输入单价' }"
                style="margin: 0"
              >
                <a-input-number
                  v-model:value="record.unitPrice"
                  :min="0"
                  :precision="2"
                  placeholder="单价"
                  style="width: 120px"
                  @change="() => calculateItemAmount(index)"
                />
              </a-form-item>
            </template>
            <template v-else-if="column.key === 'taxRate'">
              <a-form-item
                :name="['items', index, 'taxRate']"
                :rules="{ required: true, message: '请选择税率' }"
                style="margin: 0"
              >
                <a-select
                  v-model:value="record.taxRate"
                  placeholder="税率"
                  style="width: 100px"
                  @change="() => calculateItemAmount(index)"
                >
                  <a-select-option :value="0.03">3%</a-select-option>
                  <a-select-option :value="0.06">6%</a-select-option>
                  <a-select-option :value="0.09">9%</a-select-option>
                  <a-select-option :value="0.13">13%</a-select-option>
                </a-select>
              </a-form-item>
            </template>
            <template v-else-if="column.key === 'amount'">
              <span style="font-weight: 500">
                ¥{{ formatAmount(record.amount || 0) }}
              </span>
            </template>
            <template v-else-if="column.key === 'taxAmount'">
              <span style="color: #f5222d">
                ¥{{ formatAmount(record.taxAmount || 0) }}
              </span>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button
                type="link"
                danger
                size="small"
                @click="handleRemoveItem(index)"
                :disabled="formData.items.length <= 1"
              >
                <DeleteOutlined />
              </a-button>
            </template>
          </template>
        </a-table>

        <!-- 金额汇总 -->
        <a-divider />
        <a-row :gutter="16" justify="end">
          <a-col :span="6">
            <a-form-item :label-col="{ span: 12 }" :wrapper-col="{ span: 12 }" label="不含税金额">
              <span style="font-size: 16px; font-weight: 500">
                ¥{{ formatAmount(totalNetAmount) }}
              </span>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item :label-col="{ span: 12 }" :wrapper-col="{ span: 12 }" label="税额">
              <span style="font-size: 16px; font-weight: 500; color: #f5222d">
                ¥{{ formatAmount(totalTaxAmount) }}
              </span>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item :label-col="{ span: 12 }" :wrapper-col="{ span: 12 }" label="价税合计">
              <span style="font-size: 18px; font-weight: 600; color: #f5222d">
                ¥{{ formatAmount(totalAmount) }}
              </span>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 提交按钮 -->
        <a-divider />
        <a-row justify="center" :gutter="16">
          <a-col>
            <a-space>
              <a-button @click="handleBack">取消</a-button>
              <a-button type="primary" :loading="submitting" @click="handleSubmit">
                提交签发
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { invoiceApi } from '../api/invoice'
import type { InvoiceIssueRequest, InvoiceItem } from '../types/invoice'

const router = useRouter()
const formRef = ref()

// 表单数据
const formData = reactive<InvoiceIssueRequest>({
  buyerName: '',
  buyerTaxId: '',
  buyerAddressPhone: '',
  buyerBankAccount: '',
  sellerName: '',
  sellerTaxId: '',
  sellerAddressPhone: '',
  sellerBankAccount: '',
  totalAmount: 0,
  items: [
    {
      itemName: '',
      specification: '',
      unit: '件',
      quantity: 1,
      unitPrice: 0,
      amount: 0,
      taxRate: 0.13,
      taxAmount: 0
    }
  ]
})

// 表单校验规则
const rules = {
  buyerName: [{ required: true, message: '请输入购买方名称', trigger: 'blur' }],
  sellerName: [{ required: true, message: '请输入销售方名称', trigger: 'blur' }]
}

// 明细表列定义
const itemColumns = [
  {
    title: '商品名称',
    key: 'itemName',
    width: 180
  },
  {
    title: '规格型号',
    key: 'specification',
    width: 120
  },
  {
    title: '单位',
    key: 'unit',
    width: 80
  },
  {
    title: '数量',
    key: 'quantity',
    width: 100
  },
  {
    title: '单价',
    key: 'unitPrice',
    width: 120
  },
  {
    title: '税率',
    key: 'taxRate',
    width: 100
  },
  {
    title: '金额',
    key: 'amount',
    width: 120
  },
  {
    title: '税额',
    key: 'taxAmount',
    width: 120
  },
  {
    title: '操作',
    key: 'action',
    width: 60,
    fixed: 'right'
  }
]

// 提交状态
const submitting = ref(false)

// 计算单条明细金额
const calculateItemAmount = (index: number) => {
  const item = formData.items[index]
  item.amount = item.quantity * item.unitPrice
  item.taxAmount = item.amount * item.taxRate
  formData.totalAmount = totalAmount.value
}

// 计算总不含税金额
const totalNetAmount = computed(() => {
  return formData.items.reduce((sum, item) => sum + (item.amount || 0), 0)
})

// 计算总税额
const totalTaxAmount = computed(() => {
  return formData.items.reduce((sum, item) => sum + (item.taxAmount || 0), 0)
})

// 计算价税合计
const totalAmount = computed(() => {
  return totalNetAmount.value + totalTaxAmount.value
})

// 格式化金额
const formatAmount = (amount: number) => {
  return amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 添加商品明细
const handleAddItem = () => {
  formData.items.push({
    itemName: '',
    specification: '',
    unit: '件',
    quantity: 1,
    unitPrice: 0,
    amount: 0,
    taxRate: 0.13,
    taxAmount: 0
  })
}

// 删除商品明细
const handleRemoveItem = (index: number) => {
  if (formData.items.length > 1) {
    formData.items.splice(index, 1)
  }
}

// 返回列表
const handleBack = () => {
  router.push('/')
}

// 提交签发
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (formData.items.length === 0) {
      message.error('请至少添加一条商品明细')
      return
    }
    const hasEmptyItem = formData.items.some(
      (item) => !item.itemName || item.quantity <= 0 || item.unitPrice <= 0
    )
    if (hasEmptyItem) {
      message.error('请完善商品明细信息')
      return
    }

    submitting.value = true
    const res = await invoiceApi.issueInvoice(formData)
    if (res.code === 200) {
      message.success('发票签发成功')
      router.push('/')
    } else {
      message.error(res.message || '签发失败')
    }
  } catch (error) {
    console.error('校验失败', error)
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  // 可以在这里初始化一些数据
})
</script>

<style scoped>
.invoice-issue-container {
  max-width: 1200px;
  margin: 0 auto;
}

.form-card {
  border-radius: 8px;
}

:deep(.ant-divider-inner-text) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.ant-form-item) {
  margin-bottom: 16px;
}
</style>
