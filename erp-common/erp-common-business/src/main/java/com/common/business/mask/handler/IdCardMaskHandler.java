package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

/**
 * 身份证号脱敏：18 位 / 15 位 都保留前 3 后 4，中间全 *
 *
 * <p>例：</p>
 * <ul>
 *   <li>18 位：110101199001011234 → 110***********1234</li>
 *   <li>15 位：110101900101123    → 110********0123</li>
 * </ul>
 *
 * <p>不做校验位算法（出参字段语义已表达"这是身份证"，无需自动识别）。</p>
 *
 * @author cloud-erp
 */
@Component
public class IdCardMaskHandler implements MaskHandler {

    @Override
    public MaskStrategy strategy() {
        return MaskStrategy.ID_CARD;
    }

    @Override
    public Object handle(Object value, MaskContext ctx) {
        if (!(value instanceof String)) {
            return value;
        }
        String s = ((String) value).trim();
        int len = s.length();
        if (len != 15 && len != 18) {
            return s;
        }
        StringBuilder sb = new StringBuilder(len);
        sb.append(s, 0, 3);
        for (int i = 0; i < len - 7; i++) {
            sb.append('*');
        }
        sb.append(s, len - 4, len);
        return sb.toString();
    }
}
