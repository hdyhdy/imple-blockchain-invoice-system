import axios from 'axios'
import type {
  Invoice,
  InvoiceIssueRequest,
  InvoiceIssueResponse,
  InvoiceVerifyRequest,
  InvoiceVerifyResponse,
  PageResult,
  ApiResponse
} from '../types/invoice'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      } else if (status === 403) {
        console.error('没有权限访问')
      } else if (status === 500) {
        console.error('服务器错误')
      }
    }
    return Promise.reject(error)
  }
)

// 发票相关API
export const invoiceApi = {
  // 签发发票
  issueInvoice: (data: InvoiceIssueRequest) => {
    return request.post<any, ApiResponse<InvoiceIssueResponse>>('/invoice/issue', data)
  },

  // 获取发票列表
  getInvoiceList: (params: {
    page: number
    size: number
    keyword?: string
    status?: number
  }) => {
    return request.get<any, ApiResponse<PageResult<Invoice>>>('/invoice/list', { params })
  },

  // 获取发票详情
  getInvoiceDetail: (id: number) => {
    return request.get<any, ApiResponse<Invoice>>(`/invoice/${id}`)
  },

  // 获取发票详情（含明细）
  getInvoiceDetailWithItems: (id: number) => {
    return request.get<any, ApiResponse<Invoice>>(`/invoice/${id}/detail`)
  },

  // 下载发票PDF
  downloadPdf: (id: number) => {
    return request.get<any, any>(`/invoice/${id}/pdf`, {
      responseType: 'blob'
    })
  },

  // 作废发票
  voidInvoice: (id: number) => {
    return request.post<any, ApiResponse<null>>(`/invoice/${id}/void`)
  },

  // 验证发票
  verifyInvoice: (data: InvoiceVerifyRequest) => {
    return request.post<any, ApiResponse<InvoiceVerifyResponse>>('/invoice/verify', data)
  }
}

export default invoiceApi
