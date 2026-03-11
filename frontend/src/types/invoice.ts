// 发票类型定义

export interface InvoiceItem {
  id?: number
  invoiceId?: number
  itemName: string
  specification?: string
  unit?: string
  quantity: number
  unitPrice: number
  amount: number
  taxRate: number
  taxAmount: number
}

export interface Invoice {
  id: number
  invoiceUuid: string
  invoiceCode: string
  invoiceNumber: string
  checkCode: string
  buyerName: string
  buyerTaxId?: string
  buyerAddressPhone?: string
  buyerBankAccount?: string
  sellerName: string
  sellerTaxId?: string
  sellerAddressPhone?: string
  sellerBankAccount?: string
  totalAmount: number
  taxAmount: number
  netAmount: number
  status: number
  pdfUrl?: string
  txHash?: string
  blockHeight?: number
  chainProofExists: number
  invoiceDate: string
  createdAt: string
  updatedAt: string
  items?: InvoiceItem[]
}

export interface InvoiceIssueRequest {
  buyerName: string
  buyerTaxId?: string
  buyerAddressPhone?: string
  buyerBankAccount?: string
  sellerName: string
  sellerTaxId?: string
  sellerAddressPhone?: string
  sellerBankAccount?: string
  totalAmount: number
  items: InvoiceItem[]
}

export interface InvoiceIssueResponse {
  id: number
  invoiceCode: string
  invoiceNumber: string
  checkCode: string
  txHash?: string
  pdfUrl?: string
}

export interface InvoiceVerifyRequest {
  invoiceCode: string
  invoiceNumber: string
  checkCode: string
  totalAmount: number
}

export interface InvoiceVerifyResponse {
  valid: boolean
  blockchainVerified: boolean
  invoiceInfo?: Invoice
  verifyTime?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
