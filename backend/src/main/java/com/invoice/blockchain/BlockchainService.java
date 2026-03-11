package com.invoice.blockchain;

import com.invoice.entity.Invoice;

public interface BlockchainService {

    /**
     * 发行发票到区块链（同步）
     */
    String issueInvoice(Invoice invoice) throws Exception;

    /**
     * 异步发行发票到区块链
     */
    void issueInvoiceAsync(Invoice invoice);

    /**
     * 验证发票是否在区块链上存证
     */
    boolean verifyInvoice(String invoiceCode, String invoiceNumber, java.math.BigDecimal amount) throws Exception;

    /**
     * 获取区块链交易详情
     */
    String getTransactionReceipt(String txHash) throws Exception;
}
