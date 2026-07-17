package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

/**
 * 金额脱敏：整体替换为 *** 占位
 *
 * <p>支持类型：</p>
 * <ul>
 *   <li>String → 直接返回 {@code ctx.getReplacement()}</li>
 *   <li>BigDecimal / Number → 数值字段无法写入字符串占位，返回 {@code null} 隐藏金额</li>
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
            return null;
        }
        return ctx.getReplacement();
    }
}
