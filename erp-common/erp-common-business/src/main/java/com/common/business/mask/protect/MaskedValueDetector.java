package com.common.business.mask.protect;

import com.common.business.mask.cache.CfgMaskFieldSnapshotEntry;
import com.common.business.mask.core.MaskFieldDescriptor;
import com.common.business.mask.handler.RegexSafetyGuard;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 判断提交值是否为脱敏占位。
 *
 * @author cloud-erp
 */
@Component
public class MaskedValueDetector {

    private static final Set<String> DEFAULT_MARKERS = new HashSet<>(Arrays.asList(
            "***", "****", "*****", "******", "已隐藏", "已脱敏"
    ));

    private static final Pattern ALL_STARS = Pattern.compile("^\\*{3,}$");

    public boolean isMaskedValue(Object value, MaskFieldDescriptor fd) {
        if (value instanceof Number && fd != null && fd.getStrategy() == MaskStrategy.AMOUNT) {
            return BigDecimal.ZERO.compareTo(new BigDecimal(value.toString())) == 0;
        }
        String regex = fd == null ? "" : fd.getProtectMaskedValueRegex();
        String replacement = fd == null ? "" : fd.getReplacement();
        return isMaskedValue(value, regex, replacement);
    }

    public boolean isMaskedValue(Object value, CfgMaskFieldSnapshotEntry entry) {
        if (value instanceof Number && entry != null && entry.getStrategy() == MaskStrategy.AMOUNT) {
            return BigDecimal.ZERO.compareTo(new BigDecimal(value.toString())) == 0;
        }
        String regex = entry == null ? "" : entry.getProtectMaskedValueRegex();
        String replacement = entry == null ? "" : entry.getReplacement();
        return isMaskedValue(value, regex, replacement);
    }

    public boolean isMaskedValue(Object value, String customRegex, String replacement) {
        if (value == null) {
            return true;
        }
        if (!(value instanceof CharSequence)) {
            return false;
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return true;
        }
        if (replacement != null && !replacement.trim().isEmpty() && text.equals(replacement.trim())) {
            return true;
        }
        if (DEFAULT_MARKERS.contains(text)) {
            return true;
        }
        if (ALL_STARS.matcher(text).matches()) {
            return true;
        }
        if (customRegex != null && !customRegex.trim().isEmpty()
                && RegexSafetyGuard.checkAndCompile(customRegex) == null) {
            return Pattern.compile(customRegex).matcher(text).matches();
        }
        return false;
    }
}
