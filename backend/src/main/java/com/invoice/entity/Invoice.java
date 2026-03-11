package com.invoice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_invoice")
public class Invoice implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("invoice_uuid")
    private String invoiceUuid;

    @TableField("invoice_code")
    private String invoiceCode;

    @TableField("invoice_number")
    private String invoiceNumber;

    @TableField("check_code")
    private String checkCode;

    @TableField("buyer_name")
    private String buyerName;

    @TableField("buyer_tax_id")
    private String buyerTaxId;

    @TableField("buyer_address_phone")
    private String buyerAddressPhone;

    @TableField("buyer_bank_account")
    private String buyerBankAccount;

    @TableField("seller_name")
    private String sellerName;

    @TableField("seller_tax_id")
    private String sellerTaxId;

    @TableField("seller_address_phone")
    private String sellerAddressPhone;

    @TableField("seller_bank_account")
    private String sellerBankAccount;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("tax_amount")
    private BigDecimal taxAmount;

    @TableField("net_amount")
    private BigDecimal netAmount;

    @TableField("status")
    private Integer status;

    @TableField("pdf_url")
    private String pdfUrl;

    @TableField("tx_hash")
    private String txHash;

    @TableField("block_height")
    private Long blockHeight;

    @TableField("chain_proof_exists")
    private Integer chainProofExists;

    @TableField("invoice_date")
    private LocalDate invoiceDate;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
