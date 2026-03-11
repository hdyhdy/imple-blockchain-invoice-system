package com.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceIssueResponse {
    private Long invoiceId;
    private String invoiceCode;
    private String invoiceNumber;
    private String checkCode;
    private BigDecimal totalAmount;
}
