package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

/**
 * 密码脱敏：固定 6 个 *，不暴露原长度
 *
 * <p>例：任何非空字符串 → ******</p>
 *
 * @author cloud-erp
 */
@Component
public class PasswordMaskHandler implements MaskHandler {

    private static final String FIXED = "******";

    @Override
    public MaskStrategy strategy() {
        return MaskStrategy.PASSWORD;
    }

    @Override
    public Object handle(Object value, MaskContext ctx) {
        if (value == null) {
            return ctx.isKeepEmpty() ? null : FIXED;
        }
        if (value instanceof String) {
            String s = (String) value;
            if (s.isEmpty() && ctx.isKeepEmpty()) {
                return s;
            }
            return FIXED;
        }
        return value;
    }
}
