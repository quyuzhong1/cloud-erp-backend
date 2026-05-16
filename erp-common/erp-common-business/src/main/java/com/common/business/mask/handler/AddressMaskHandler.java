package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

/**
 * 地址脱敏：保留前 6 个字符（一般是省 + 市 + 区），其余替换为 ****
 *
 * <p>例：北京市朝阳区建国路100号 → 北京市朝阳****</p>
 *
 * <p>长度 ≤ 6 时原样返回（信息量本来就不大，遮了反而损失语义）。</p>
 *
 * @author cloud-erp
 */
@Component
public class AddressMaskHandler implements MaskHandler {

    /** 默认保留长度（按"省+市"+区前缀粗略估算 6 字符） */
    private static final int KEEP_PREFIX_LEN = 6;

    @Override
    public MaskStrategy strategy() {
        return MaskStrategy.ADDRESS;
    }

    @Override
    public Object handle(Object value, MaskContext ctx) {
        if (!(value instanceof String)) {
            return value;
        }
        String s = (String) value;
        int len = s.length();
        if (len <= KEEP_PREFIX_LEN) {
            return s;
        }
        return s.substring(0, KEEP_PREFIX_LEN) + "****";
    }
}
