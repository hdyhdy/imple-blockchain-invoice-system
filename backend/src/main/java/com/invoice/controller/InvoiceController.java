package com.invoice.controller;

import com.invoice.dto.*;
import com.invoice.entity.Invoice;
import com.invoice.service.InvoiceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * 签发发票
     */
    @PostMapping("/issue")
    public Result<InvoiceIssueResponse> issueInvoice(@Valid @RequestBody InvoiceIssueRequest request) {
        try {
            InvoiceIssueResponse response = invoiceService.issueInvoice(request);
            return Result.success("发票签发成功", response);
        } catch (Exception e) {
            return Result.error("签发失败: " + e.getMessage());
        }
    }

    /**
     * 获取发票列表
     */
    @GetMapping("/list")
    public Result<Page<Invoice>> listInvoices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<Invoice> list = invoiceService.listInvoices(page, size, keyword, status);
        return Result.success(list);
    }

    /**
     * 获取发票详情
     */
    @GetMapping("/{id}")
    public Result<Invoice> getInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        if (invoice == null) {
            return Result.error("发票不存在");
        }
        return Result.success(invoice);
    }

    /**
     * 下载发票PDF
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = invoiceService.generatePdf(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 作废发票
     */
    @PostMapping("/{id}/void")
    public Result<Void> voidInvoice(@PathVariable Long id) {
        return invoiceService.voidInvoice(id);
    }

    /**
     * 验证发票
     */
    @PostMapping("/verify")
    public Result<InvoiceVerifyResponse> verifyInvoice(@Valid @RequestBody InvoiceVerifyRequest request) {
        InvoiceVerifyResponse response = invoiceService.verifyInvoice(request);
        if (response.getValid() == null || !response.getValid()) {
            return Result.error("验证失败：发票信息不匹配");
        }
        if (!response.getBlockchainVerified()) {
            return Result.success("发票验证成功，但区块链存证尚未确认", response);
        }
        return Result.success("发票验证成功，已上链存证", response);
    }

    /**
     * 获取发票详情（含明细）
     */
    @GetMapping("/{id}/detail")
    public Result<Invoice> getInvoiceDetail(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        if (invoice == null) {
            return Result.error("发票不存在");
        }
        return Result.success(invoice);
    }
}
