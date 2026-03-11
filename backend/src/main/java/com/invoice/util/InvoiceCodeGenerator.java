package com.invoice.util;

import java.util.Random;

/**
 * 发票代码和号码生成器
 */
public class InvoiceCodeGenerator {

    private static final Random random = new Random();

    /**
     * 生成发票号码（8位数字）
     */
    public static String generateInvoiceNumber() {
        return String.format("%08d", Math.abs(random.nextInt(99999999)));
    }

    /**
     * 生成校验码（6位字母数字混合）
     */
    public static String generateCheckCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
