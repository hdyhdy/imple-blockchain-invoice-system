package com.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceVerifyResponse {
    private Boolean valid;
    private Boolean blockchainVerified;
    private String txHash;
    private Long blockHeight;
    private InvoiceInfo invoiceInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceInfo {
        private String buyerName;
        private String sellerName;
        private BigDecimal totalAmount;
        private LocalDate invoiceDate;
    }
}
