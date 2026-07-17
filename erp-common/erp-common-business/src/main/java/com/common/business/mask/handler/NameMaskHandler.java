package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskStrategy;

import org.springframework.stereotype.Component;

/**
 * 中文姓名脱敏：保留姓 + 名末字（≥3）/ 末字 *（=2）/ 首字 *（=1）
 *
 * <p>例：</p>
 * <ul>
 *   <li>张三      → 张*</li>
 *   <li>李四光    → 李*光</li>
 *   <li>欧阳娜娜  → 欧**娜</li>
 *   <li>王        → 王*</li>
 * </ul>
 *
 * <p>非中文姓名（含空格的英文名等）按"首字 + ***"处理。</p>
 *
 * @author cloud-erp
 */
@Component
public class NameMaskHandler implements MaskHandler {

    @Override
    public MaskStrategy strategy() {
        return MaskStrategy.NAME;
    }

    @Override
    public Object handle(Object value, MaskContext ctx) {
        if (!(value instanceof String)) {
            return value;
        }
        String s = (String) value;
        int len = s.length();
        if (len == 0) {
            return s;
        }
        if (len == 1) {
            return s + "*";
        }
        if (len == 2) {
            return s.charAt(0) + "*";
        }
        StringBuilder sb = new StringBuilder(len);
        sb.append(s.charAt(0));
        for (int i = 1; i < len - 1; i++) {
            sb.append('*');
        }
        sb.append(s.charAt(len - 1));
        return sb.toString();
    }
}
