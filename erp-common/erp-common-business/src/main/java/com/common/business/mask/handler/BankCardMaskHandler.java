package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

/**
 * 银行卡号脱敏：保留前 4 位、后 4 位，中间全 *
 *
 * <p>例：6222024200012345678 → 6222***********5678</p>
 *
 * <p>会先剔除空白和短横线（兼容前端展示格式 "6222 0242 0001 2345 678"），
 * 然后判断长度 13~19 才做脱敏，否则原样返回。</p>
 *
 * @author cloud-erp
 */
@Component
public class BankCardMaskHandler implements MaskHandler {

    @Override
    public MaskStrategy strategy() {
        return MaskStrategy.BANK_CARD;
    }

    @Override
    public Object handle(Object value, MaskContext ctx) {
        if (!(value instanceof String)) {
            return value;
        }
        String origin = (String) value;
        String compact = origin.replaceAll("[\\s\\-]", "");
        int len = compact.length();
        if (len < 13 || len > 19) {
            return origin;
        }
        StringBuilder sb = new StringBuilder(len);
        sb.append(compact, 0, 4);
        for (int i = 0; i < len - 8; i++) {
            sb.append('*');
        }
        sb.append(compact, len - 4, len);
        return sb.toString();
    }
}
