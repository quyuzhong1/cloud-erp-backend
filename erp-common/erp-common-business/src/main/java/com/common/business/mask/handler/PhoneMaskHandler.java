package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

/**
 * 手机号脱敏：保留前 3 位、后 4 位，中间 4 位 *
 *
 * <p>例：13812345678 → 138****5678</p>
 *
 * <p>仅处理 String 类型，长度 &lt; 7 时原样返回；不做严格的"中国手机号"格式校验，
 * 因为业务字段语义已经表达了"这是手机号"，避免误伤海外手机号 / 内部分机号。</p>
 *
 * @author cloud-erp
 */
@Component
public class PhoneMaskHandler implements MaskHandler {

    @Override
    public MaskStrategy strategy() {
        return MaskStrategy.PHONE;
    }

    @Override
    public Object handle(Object value, MaskContext ctx) {
        if (!(value instanceof String)) {
            return value;
        }
        String s = (String) value;
        int len = s.length();
        if (len < 7) {
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
