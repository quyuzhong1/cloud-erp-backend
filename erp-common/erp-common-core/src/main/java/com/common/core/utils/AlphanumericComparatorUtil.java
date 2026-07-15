package com.common.core.utils;

/**
 * 字母数字自然序比较（如 A2 &lt; A10）。
 */
public final class AlphanumericComparatorUtil {

    private AlphanumericComparatorUtil() {
    }

    public static int compare(String left, String right) {
        String a = left == null ? "" : left;
        String b = right == null ? "" : right;
        int i = 0;
        int j = 0;
        int lenA = a.length();
        int lenB = b.length();
        while (i < lenA && j < lenB) {
            char ca = a.charAt(i);
            char cb = b.charAt(j);
            if (Character.isDigit(ca) && Character.isDigit(cb)) {
                int startI = i;
                int startJ = j;
                while (i < lenA && Character.isDigit(a.charAt(i))) {
                    i++;
                }
                while (j < lenB && Character.isDigit(b.charAt(j))) {
                    j++;
                }
                String numA = a.substring(startI, i);
                String numB = b.substring(startJ, j);
                // 去掉前导零后再比长度与字典序，保证 02 < 10
                String trimA = trimLeadingZeros(numA);
                String trimB = trimLeadingZeros(numB);
                if (trimA.length() != trimB.length()) {
                    return trimA.length() - trimB.length();
                }
                int cmp = trimA.compareTo(trimB);
                if (cmp != 0) {
                    return cmp;
                }
                // 数值相等时保留前导零差异：位数更长的更大（可选稳定）
                if (numA.length() != numB.length()) {
                    return numA.length() - numB.length();
                }
            } else {
                char ua = Character.toUpperCase(ca);
                char ub = Character.toUpperCase(cb);
                if (ua != ub) {
                    return ua - ub;
                }
                i++;
                j++;
            }
        }
        return lenA - lenB;
    }

    private static String trimLeadingZeros(String num) {
        int k = 0;
        while (k < num.length() - 1 && num.charAt(k) == '0') {
            k++;
        }
        return num.substring(k);
    }
}
