package com.invoice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("t_invoice_item")
public class InvoiceItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("invoice_id")
    private Long invoiceId;

    @TableField("item_name")
    private String itemName;

    @TableField("specification")
    private String specification;

    @TableField("unit")
    private String unit;

    @TableField("quantity")
    private BigDecimal quantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("tax_rate")
    private BigDecimal taxRate;

    @TableField("tax_amount")
    private BigDecimal taxAmount;
}
