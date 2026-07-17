package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 金额脱敏：整体替换为 *** 占位
 *
 * <p>支持类型：</p>
 * <ul>
 *   <li>String → 直接返回 {@code ctx.getReplacement()}</li>
 *   <li>BigDecimal / Number → 由于该字段类型为数值，无法直接写入字符串，
 *       返回 {@link BigDecimal#ZERO} 作为占位（前端展示时由调用方渲染为 ***）</li>
 * </ul>
 *
 * <p>注：业务侧若希望前端看到 *** 字符串，DTO 字段应声明为 String / Object 类型。</p>
 *
 * @author cloud-erp
 */
@Component
public class AmountMaskHandler implements MaskHandler {

    @Override
    public MaskStrategy strategy() {
        return MaskStrategy.AMOUNT;
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
        if (value instanceof Number) {
            return BigDecimal.ZERO;
        }
        return value;
    }
}
