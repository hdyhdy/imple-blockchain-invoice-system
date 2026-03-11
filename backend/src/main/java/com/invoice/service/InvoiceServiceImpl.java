package com.invoice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.invoice.blockchain.BlockchainService;
import com.invoice.dto.*;
import com.invoice.entity.Invoice;
import com.invoice.entity.InvoiceItem;
import com.invoice.mapper.InvoiceItemMapper;
import com.invoice.mapper.InvoiceMapper;
import com.invoice.util.InvoiceCodeGenerator;
import com.invoice.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceMapper invoiceMapper;
    private final InvoiceItemMapper invoiceItemMapper;
    private final BlockchainService blockchainService;
    private final PdfGenerator pdfGenerator;

    @Value("${invoice.code-prefix}")
    private String codePrefix;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceIssueResponse issueInvoice(InvoiceIssueRequest request) {
        // 生成发票UUID
        String invoiceUuid = UUID.randomUUID().toString().replace("-", "");

        // 生成发票代码和号码
        String invoiceCode = codePrefix;
        String invoiceNumber = InvoiceCodeGenerator.generateInvoiceNumber();

        // 生成校验码
        String checkCode = InvoiceCodeGenerator.generateCheckCode();

        // 计算金额
        BigDecimal netAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (InvoiceItemDTO itemDTO : request.getItems()) {
            BigDecimal amount = itemDTO.getQuantity().multiply(itemDTO.getUnitPrice())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal tax = amount.multiply(itemDTO.getTaxRate())
                    .setScale(2, RoundingMode.HALF_UP);

            netAmount = netAmount.add(amount);
            taxAmount = taxAmount.add(tax);
            totalAmount = totalAmount.add(amount.add(tax));
        }

        // 创建发票主记录
        Invoice invoice = new Invoice();
        invoice.setInvoiceUuid(invoiceUuid);
        invoice.setInvoiceCode(invoiceCode);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCheckCode(checkCode);
        invoice.setBuyerName(request.getBuyerName());
        invoice.setBuyerTaxId(request.getBuyerTaxId());
        invoice.setBuyerAddressPhone(request.getBuyerAddressPhone());
        invoice.setBuyerBankAccount(request.getBuyerBankAccount());
        invoice.setSellerName(request.getSellerName());
        invoice.setSellerTaxId(request.getSellerTaxId());
        invoice.setSellerAddressPhone(request.getSellerAddressPhone());
        invoice.setSellerBankAccount(request.getSellerBankAccount());
        invoice.setNetAmount(netAmount);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus(0); // 待签发
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setChainProofExists(0);

        invoiceMapper.insert(invoice);

        // 创建发票明细记录
        for (InvoiceItemDTO itemDTO : request.getItems()) {
            InvoiceItem item = new InvoiceItem();
            item.setInvoiceId(invoice.getId());
            item.setItemName(itemDTO.getItemName());
            item.setSpecification(itemDTO.getSpecification());
            item.setUnit(itemDTO.getUnit());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setTaxRate(itemDTO.getTaxRate());

            BigDecimal amount = itemDTO.getQuantity().multiply(itemDTO.getUnitPrice())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal tax = amount.multiply(itemDTO.getTaxRate())
                    .setScale(2, RoundingMode.HALF_UP);

            item.setAmount(amount);
            item.setTaxAmount(tax);

            invoiceItemMapper.insert(item);
        }

        // 异步上链存证
        try {
            blockchainService.issueInvoiceAsync(invoice);
        } catch (Exception e) {
            log.error("区块链存证失败，将在后台重试: {}", e.getMessage());
        }

        return new InvoiceIssueResponse(
                invoice.getId(),
                invoice.getInvoiceCode(),
                invoice.getInvoiceNumber(),
                invoice.getCheckCode(),
                invoice.getTotalAmount()
        );
    }

    @Override
    public Page<Invoice> listInvoices(Integer page, Integer size, String keyword, Integer status) {
        Page<Invoice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Invoice::getInvoiceCode, keyword)
                    .or().like(Invoice::getInvoiceNumber, keyword)
                    .or().like(Invoice::getBuyerName, keyword)
                    .or().like(Invoice::getSellerName, keyword)
            );
        }

        if (status != null) {
            wrapper.eq(Invoice::getStatus, status);
        }

        wrapper.orderByDesc(Invoice::getCreatedAt);
        return invoiceMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Invoice getInvoiceById(Long id) {
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice != null) {
            LambdaQueryWrapper<InvoiceItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InvoiceItem::getInvoiceId, id);
            List<InvoiceItem> items = invoiceItemMapper.selectList(wrapper);
            invoice.setItems(items);
        }
        return invoice;
    }

    @Override
    public byte[] generatePdf(Long id) throws Exception {
        Invoice invoice = getInvoiceById(id);
        if (invoice == null) {
            throw new Exception("发票不存在");
        }
        return pdfGenerator.generatePdf(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> voidInvoice(Long id) {
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            return Result.error("发票不存在");
        }

        if (invoice.getStatus() == 2) {
            return Result.error("发票已作废");
        }

        invoice.setStatus(2); // 已作废
        invoiceMapper.updateById(invoice);

        return Result.success("发票作废成功", null);
    }

    @Override
    public InvoiceVerifyResponse verifyInvoice(InvoiceVerifyRequest request) {
        // 查询数据库
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getInvoiceCode, request.getInvoiceCode())
                .eq(Invoice::getInvoiceNumber, request.getInvoiceNumber())
                .eq(Invoice::getCheckCode, request.getCheckCode())
                .eq(Invoice::getTotalAmount, request.getTotalAmount());

        Invoice invoice = invoiceMapper.selectOne(wrapper);

        if (invoice == null) {
            return new InvoiceVerifyResponse(false, false, null, null, null);
        }

        // 检查状态
        if (invoice.getStatus() == 2) {
            return new InvoiceVerifyResponse(false, invoice.getChainProofExists() == 1,
                    invoice.getTxHash(), invoice.getBlockHeight(), null);
        }

        // 验证区块链存证
        boolean blockchainVerified = false;
        if (invoice.getChainProofExists() == 1 && invoice.getTxHash() != null) {
            try {
                blockchainVerified = blockchainService.verifyInvoice(
                        invoice.getInvoiceCode(),
                        invoice.getInvoiceNumber(),
                        invoice.getTotalAmount()
                );
            } catch (Exception e) {
                log.error("区块链验证失败: {}", e.getMessage());
            }
        }

        InvoiceVerifyResponse.InvoiceInfo info = new InvoiceVerifyResponse.InvoiceInfo(
                invoice.getBuyerName(),
                invoice.getSellerName(),
                invoice.getTotalAmount(),
                invoice.getInvoiceDate()
        );

        return new InvoiceVerifyResponse(true, blockchainVerified,
                invoice.getTxHash(), invoice.getBlockHeight(), info);
    }
}
