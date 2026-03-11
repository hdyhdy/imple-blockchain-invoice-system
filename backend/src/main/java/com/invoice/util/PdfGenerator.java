package com.invoice.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.invoice.entity.Invoice;
import com.invoice.entity.InvoiceItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF发票生成器
 */
@Slf4j
@Component
public class PdfGenerator {

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(45, 85, 145);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    public byte[] generatePdf(Invoice invoice) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // 标题
            Paragraph title = new Paragraph("增值税电子普通发票")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(HEADER_COLOR);
            document.add(title);

            document.add(new Paragraph("\n"));

            // 发票信息表格
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 3}));
            infoTable.setWidth(UnitValue.createPercentValue(100));

            addInfoRow(infoTable, "发票代码:", invoice.getInvoiceCode());
            addInfoRow(infoTable, "发票号码:", invoice.getInvoiceNumber());
            addInfoRow(infoTable, "校验码:", invoice.getCheckCode());
            addInfoRow(infoTable, "开票日期:", invoice.getInvoiceDate().format(DATE_FORMATTER));

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // 购买方和销售方信息
            Table partyTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            partyTable.setWidth(UnitValue.createPercentValue(100));

            // 购买方信息
            Cell buyerCell = new Cell()
                    .add(new Paragraph("购买方信息").setBold().setFontColor(HEADER_COLOR))
                    .add(new Paragraph("\n"))
                    .add(new Paragraph("名称: " + invoice.getBuyerName()))
                    .add(new Paragraph("税号: " + (invoice.getBuyerTaxId() != null ? invoice.getBuyerTaxId() : "")))
                    .add(new Paragraph("地址电话: " + (invoice.getBuyerAddressPhone() != null ? invoice.getBuyerAddressPhone() : "")))
                    .add(new Paragraph("开户行账号: " + (invoice.getBuyerBankAccount() != null ? invoice.getBuyerBankAccount() : "")))
                    .setBorder(null);
            partyTable.addCell(buyerCell);

            // 销售方信息
            Cell sellerCell = new Cell()
                    .add(new Paragraph("销售方信息").setBold().setFontColor(HEADER_COLOR))
                    .add(new Paragraph("\n"))
                    .add(new Paragraph("名称: " + invoice.getSellerName()))
                    .add(new Paragraph("税号: " + (invoice.getSellerTaxId() != null ? invoice.getSellerTaxId() : "")))
                    .add(new Paragraph("地址电话: " + (invoice.getSellerAddressPhone() != null ? invoice.getSellerAddressPhone() : "")))
                    .add(new Paragraph("开户行账号: " + (invoice.getSellerBankAccount() != null ? invoice.getSellerBankAccount() : "")))
                    .setBorder(null);
            partyTable.addCell(sellerCell);

            document.add(partyTable);
            document.add(new Paragraph("\n"));

            // 商品明细表
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 1, 1, 1, 1, 1}));
            itemsTable.setWidth(UnitValue.createPercentValue(100));

            // 表头
            String[] headers = {"商品名称", "规格型号", "单位", "数量", "单价", "金额", "税率", "税额"};
            for (String header : headers) {
                itemsTable.addHeaderCell(createHeaderCell(header));
            }

            // 数据行
            List<InvoiceItem> items = invoice.getItems();
            if (items != null) {
                for (InvoiceItem item : items) {
                    itemsTable.addCell(createDataCell(item.getItemName()));
                    itemsTable.addCell(createDataCell(item.getSpecification()));
                    itemsTable.addCell(createDataCell(item.getUnit()));
                    itemsTable.addCell(createDataCell(item.getQuantity().toString()));
                    itemsTable.addCell(createDataCell(item.getUnitPrice().toString()));
                    itemsTable.addCell(createDataCell(item.getAmount().toString()));
                    itemsTable.addCell(createDataCell((item.getTaxRate().multiply(new java.math.BigDecimal("100"))).toString() + "%"));
                    itemsTable.addCell(createDataCell(item.getTaxAmount().toString()));
                }
            }

            document.add(itemsTable);
            document.add(new Paragraph("\n"));

            // 合计信息
            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{3, 2}));
            totalTable.setWidth(UnitValue.createPercentValue(100));

            totalTable.addCell(new Cell().add(new Paragraph("合计金额（大写）").setBold()))
                    .addCell(new Cell().add(new Paragraph(AmountToChineseUtils.convert(invoice.getTotalAmount()))));
            totalTable.addCell(new Cell().add(new Paragraph("合计税额").setBold()))
                    .addCell(new Cell().add(new Paragraph(invoice.getTaxAmount().toString())));
            totalTable.addCell(new Cell().add(new Paragraph("价税合计").setBold().setFontColor(HEADER_COLOR)))
                    .addCell(new Cell().add(new Paragraph(invoice.getTotalAmount().toString()).setBold().setFontColor(HEADER_COLOR)));

            document.add(totalTable);
            document.add(new Paragraph("\n"));

            // 区块链存证信息
            if (invoice.getTxHash() != null) {
                Paragraph chainInfo = new Paragraph()
                        .add("区块链存证信息:\n")
                        .add("交易哈希: " + invoice.getTxHash() + "\n")
                        .add("区块高度: " + invoice.getBlockHeight() + "\n")
                        .add("存证状态: " + (invoice.getChainProofExists() == 1 ? "已存证" : "待存证"))
                        .setFontSize(9)
                        .setFontColor(ColorConstants.GRAY);
                document.add(chainInfo);
            }
        }

        return baos.toByteArray();
    }

    private void addInfoRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()).setBorder(null));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "")).setBorder(null));
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell createDataCell(String text) {
        return new Cell()
                .add(new Paragraph(text != null ? text : ""))
                .setTextAlignment(TextAlignment.CENTER);
    }
}
