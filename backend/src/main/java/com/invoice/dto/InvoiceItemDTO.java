package com.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceItemDTO {

    @NotBlank(message = "商品名称不能为空")
    private String itemName;

    private String specification;

    private String unit;

    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    @NotNull(message = "税率不能为空")
    private BigDecimal taxRate;
}
