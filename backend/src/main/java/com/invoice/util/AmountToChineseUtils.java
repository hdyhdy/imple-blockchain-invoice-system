package com.invoice.util;

import java.math.BigDecimal;

/**
 * 金额转中文大写工具类
 */
public class AmountToChineseUtils {

    private static final String[] NUMBERS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    private static final String[] UNITS = {"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟"};
    private static final String[] DECIMAL_UNITS = {"角", "分"};

    public static String convert(BigDecimal amount) {
        if (amount == null) {
            return "零元整";
        }

        // 转换为分，避免浮点数精度问题
        long cents = amount.multiply(new BigDecimal("100")).longValue();

        if (cents == 0) {
            return "零元整";
        }

        StringBuilder result = new StringBuilder();
        boolean isNegative = cents < 0;
        cents = Math.abs(cents);

        // 处理整数部分
        long integerPart = cents / 100;
        if (integerPart > 0) {
            result.append(convertIntegerPart(integerPart));
            result.append("元");
        }

        // 处理小数部分
        int decimalPart = (int) (cents % 100);
        if (decimalPart == 0) {
            result.append("整");
        } else {
            int jiao = decimalPart / 10;
            int fen = decimalPart % 10;

            if (jiao > 0) {
                result.append(NUMBERS[jiao]).append("角");
            }
            if (fen > 0) {
                result.append(NUMBERS[fen]).append("分");
            }
        }

        if (isNegative) {
            result.insert(0, "负");
        }

        return result.toString();
    }

    private static String convertIntegerPart(long number) {
        if (number == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int unitIndex = 0;

        while (number > 0) {
            int digit = (int) (number % 10);
            if (digit != 0) {
                result.insert(0, UNITS[unitIndex]);
                result.insert(0, NUMBERS[digit]);
            } else {
                // 处理连续的零
                if (!result.toString().endsWith("零") && !result.toString().endsWith("万") && !result.toString().endsWith("亿")) {
                    if (result.length() > 0) {
                        result.insert(0, "零");
                    }
                }
            }

            // 处理万和亿的进位
            if (unitIndex == 4 || unitIndex == 8) {
                if (!result.toString().startsWith("万") && !result.toString().startsWith("亿")) {
                    if (result.length() > 0 && !result.toString().endsWith("零")) {
                        // 检查是否需要添加万或亿
                    }
                }
            }

            number /= 10;
            unitIndex++;
        }

        // 清理末尾的零
        String str = result.toString();
        if (str.endsWith("零")) {
            str = str.substring(0, str.length() - 1);
        }

        return str;
    }
}
