package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

/**
 * 邮箱脱敏：本地部分仅保留首字符 + ***，域名原样
 *
 * <p>例：</p>
 * <ul>
 *   <li>jack@xxx.com → j***@xxx.com</li>
 *   <li>j@xxx.com    → j***@xxx.com</li>
 * </ul>
 *
 * <p>无 @ 或 @ 在首位时原样返回（不算合法邮箱，避免误改）。</p>
 *
 * @author cloud-erp
 */
@Component
public class EmailMaskHandler implements MaskHandler {

    @Override
    public MaskStrategy strategy() {
        return MaskStrategy.EMAIL;
    }

    @Override
    public Object handle(Object value, MaskContext ctx) {
        if (!(value instanceof String)) {
            return value;
        }
        String s = (String) value;
        int at = s.indexOf('@');
        if (at <= 0 || at == s.length() - 1) {
            return s;
        }
        return s.charAt(0) + "***" + s.substring(at);
    }
}
