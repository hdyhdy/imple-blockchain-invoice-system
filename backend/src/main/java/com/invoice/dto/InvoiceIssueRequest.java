package com.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class InvoiceIssueRequest {

    @NotBlank(message = "购买方名称不能为空")
    private String buyerName;

    private String buyerTaxId;

    private String buyerAddressPhone;

    private String buyerBankAccount;

    @NotBlank(message = "销售方名称不能为空")
    private String sellerName;

    private String sellerTaxId;

    private String sellerAddressPhone;

    private String sellerBankAccount;

    @NotEmpty(message = "发票明细不能为空")
    private List<InvoiceItemDTO> items;
}
