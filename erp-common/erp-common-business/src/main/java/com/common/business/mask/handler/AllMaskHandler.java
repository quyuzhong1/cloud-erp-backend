package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

/**
 * 全部遮蔽：任何非空值都替换为 {@code ctx.getReplacement()}（默认 ***）
 *
 * <p>非 String / Number / Boolean 的复杂对象返回 null（避免序列化时返回原对象明文）。</p>
 *
 * @author cloud-erp
 */
@Component
public class AllMaskHandler implements MaskHandler {

    @Override
    public MaskStrategy strategy() {
        return MaskStrategy.ALL;
    }

    @Override
    public Object handle(Object value, MaskContext ctx) {
        if (value == null) {
            return ctx.isKeepEmpty() ? null : ctx.getReplacement();
        }
        if (value instanceof String) {
            String s = (String) value;
            if (s.isEmpty() && ctx.isKeepEmpty()) {
                return s;
            }
            return ctx.getReplacement();
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof Character) {
            return ctx.getReplacement();
        }
        return null;
    }
}
