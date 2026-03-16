package com.invoice.blockchain;

import com.invoice.entity.Invoice;
import com.invoice.mapper.InvoiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransactionReceipt;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
        String dataHash = calculateDataHash(invoice);

        BigInteger amountInCents = invoice.getTotalAmount().multiply(new BigDecimal("100")).toBigInteger();
        
        String method = "issueInvoice(string,string,uint256,string)";
        String encodedParams = encodeParams(invoice.getInvoiceCode(), invoice.getInvoiceNumber(), 
            amountInCents.toString(), dataHash);
        String txData = encodeMethodAndParams(method, encodedParams);
        
        BcosTransactionReceipt receipt = bcosSDK.getClient(1).sendTransaction(contractAddress, txData, cryptoKeyPair);
        
        Optional<BcosTransactionReceipt.TransactionReceipt> receiptOpt = receipt.getTransactionReceipt();
        
        if (!receiptOpt.isPresent()) {
            throw new Exception("交易失败: 无法获取交易收据");
        }
        
        BcosTransactionReceipt.TransactionReceipt receiptData = receiptOpt.get();
        String txHash = receiptData.getTransactionHash();
        String status = receiptData.getStatus();
        
        if (!status.equals("0x0")) {
            throw new Exception("交易失败: " + status);
        }
        
        BigInteger blockHeight = receiptData.getBlockNumber();

        invoice.setTxHash(txHash);
        invoice.setBlockHeight(blockHeight.longValue());
        invoice.setChainProofExists(1);
        invoice.setStatus(1);
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
            }
        });
    }

    @Override
    public boolean verifyInvoice(String invoiceCode, String invoiceNumber, BigDecimal amount) throws Exception {
        BigInteger amountInCents = amount.multiply(new BigDecimal("100")).toBigInteger();
        
        String method = "verifyInvoice(string,string,uint256)";
        String encodedParams = encodeParams(invoiceCode, invoiceNumber, amountInCents.toString());
        String txData = encodeMethodAndParams(method, encodedParams);
        
        BcosTransactionReceipt receipt = bcosSDK.getClient(1).sendTransaction(contractAddress, txData, cryptoKeyPair);
        
        Optional<BcosTransactionReceipt.TransactionReceipt> receiptOpt = receipt.getTransactionReceipt();
        
        if (!receiptOpt.isPresent()) {
            throw new Exception("验证失败: 无法获取交易收据");
        }
        
        String status = receiptOpt.get().getStatus();
        
        if (!status.equals("0x0")) {
            throw new Exception("验证失败: " + status);
        }
        
        log.info("验证发票: invoiceCode={}, invoiceNumber={}, amount={}, result=true",
                invoiceCode, invoiceNumber, amount);

        return true;
    }

    @Override
    public String getTransactionReceipt(String txHash) throws Exception {
        BcosTransactionReceipt receipt = client.getTransactionReceipt(txHash);
        
        Optional<BcosTransactionReceipt.TransactionReceipt> receiptOpt = receipt.getTransactionReceipt();
        
        if (!receiptOpt.isPresent()) {
            return "{\"status\":\"error\",\"message\":\"Transaction not found\"}";
        }

        BcosTransactionReceipt.TransactionReceipt receiptData = receiptOpt.get();
        return "{\"status\":\"" + receiptData.getStatus() + "\",\"blockNumber\":\"" + receiptData.getBlockNumber() + "\"}";
    }

    private String encodeMethodAndParams(String method, String params) {
        String methodHash = keccak256(method).substring(0, 8);
        return methodHash + params;
    }

    private String encodeParams(String... params) {
        StringBuilder sb = new StringBuilder();
        for (String param : params) {
            sb.append(String.format("%064x", new BigInteger(1, param.getBytes())));
        }
        return sb.toString();
    }

    private String keccak256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("KECCAK-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Keccak256计算失败", e);
        }
    }

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

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
