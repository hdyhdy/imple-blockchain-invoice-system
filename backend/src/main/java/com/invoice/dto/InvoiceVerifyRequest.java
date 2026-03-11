package com.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceVerifyRequest {

    @NotBlank(message = "发票代码不能为空")
    private String invoiceCode;

    @NotBlank(message = "发票号码不能为空")
    private String invoiceNumber;

    @NotBlank(message = "校验码不能为空")
    private String checkCode;

    @NotNull(message = "总金额不能为空")
    private BigDecimal totalAmount;
}
