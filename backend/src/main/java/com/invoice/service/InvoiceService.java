package com.invoice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.invoice.dto.*;
import com.invoice.entity.Invoice;

import java.util.List;

public interface InvoiceService {

    InvoiceIssueResponse issueInvoice(InvoiceIssueRequest request);

    Page<Invoice> listInvoices(Integer page, Integer size, String keyword, Integer status);

    Invoice getInvoiceById(Long id);

    byte[] generatePdf(Long id) throws Exception;

    Result<Void> voidInvoice(Long id);

    InvoiceVerifyResponse verifyInvoice(InvoiceVerifyRequest request);
}
