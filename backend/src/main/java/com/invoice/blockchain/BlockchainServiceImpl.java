package com.invoice.blockchain;

import com.invoice.entity.Invoice;
import com.invoice.mapper.InvoiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

/**
 * 区块链服务实现类
 * 真实连接FISCO BCOS区块链
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainServiceImpl implements BlockchainService {

    private final InvoiceMapper invoiceMapper;
    private final BcosSDK bcosSDK;
    private final Client client;
    private final CryptoKeyPair cryptoKeyPair;
    private final InvoiceRegistry invoiceRegistry;

    @Value("${fisco.contract.address}")
    private String contractAddress;

    @Override
    public String issueInvoice(Invoice invoice) throws Exception {
        // 计算发票数据的哈希值
        String dataHash = calculateDataHash(invoice);

        // 调用智能合约发行发票
        // 将金额转换为分，避免小数
        BigInteger amountInCents = invoice.getTotalAmount().multiply(new BigDecimal("100")).toBigInteger();
        
        // 调用智能合约方法
        String txHash = invoiceRegistry.issueInvoice(
                invoice.getInvoiceCode(),
                invoice.getInvoiceNumber(),
                amountInCents,
                dataHash
        );

        // 等待交易确认
        Thread.sleep(2000); // 简单等待，实际应该轮询交易状态

        // 获取交易收据
        org.fisco.bcos.sdk.transaction.model.TransactionReceipt receipt = client.getTransactionReceipt(txHash);
        if (receipt == null || !receipt.getStatus().equals("0x0")) {
            throw new Exception("交易失败: " + receipt.getStatus());
        }

        // 获取区块高度
        BigInteger blockHeight = receipt.getBlockNumber();

        // 更新发票的区块链信息
        invoice.setTxHash(txHash);
        invoice.setBlockHeight(blockHeight.longValue());
        invoice.setChainProofExists(1);
        invoice.setStatus(1); // 已签发
        invoiceMapper.updateById(invoice);

        log.info("发票上链成功: invoiceCode={}, txHash={}, blockHeight={}",
                invoice.getInvoiceCode(), txHash, blockHeight);

        return txHash;
    }

    @Override
    @Async
    public void issueInvoiceAsync(Invoice invoice) {
        CompletableFuture.runAsync(() -> {
            try {
                issueInvoice(invoice);
            } catch (Exception e) {
                log.error("异步上链失败: {}", e.getMessage());
                // 可以在此实现重试逻辑
            }
        });
    }

    @Override
    public boolean verifyInvoice(String invoiceCode, String invoiceNumber, BigDecimal amount) throws Exception {
        // 将金额转换为分
        BigInteger amountInCents = amount.multiply(new BigDecimal("100")).toBigInteger();
        
        // 调用智能合约验证发票
        boolean[] result = invoiceRegistry.verifyInvoice(invoiceCode, invoiceNumber, amountInCents);
        
        log.info("验证发票: invoiceCode={}, invoiceNumber={}, amount={}, result={}",
                invoiceCode, invoiceNumber, amount, result[0]);

        return result[0];
    }

    @Override
    public String getTransactionReceipt(String txHash) throws Exception {
        // 查询区块链交易收据
        org.fisco.bcos.sdk.transaction.model.TransactionReceipt receipt = client.getTransactionReceipt(txHash);
        if (receipt == null) {
            return "{\"status\":\"error\",\"message\":\"Transaction not found\"}";
        }

        return "{\"status\":\"" + receipt.getStatus() + "\",\"blockNumber\":\"" + receipt.getBlockNumber() + "\"}";
    }

    /**
     * 计算发票数据的SHA256哈希值
     */
    private String calculateDataHash(Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append(invoice.getInvoiceCode());
        sb.append(invoice.getInvoiceNumber());
        sb.append(invoice.getTotalAmount().toPlainString());
        sb.append(invoice.getInvoiceDate());
        sb.append(invoice.getBuyerName());
        sb.append(invoice.getSellerName());

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不存在", e);
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
