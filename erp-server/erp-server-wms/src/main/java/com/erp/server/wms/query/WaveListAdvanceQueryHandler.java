package com.erp.server.wms.query;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.query.AbstractQueryHandler;
import com.common.core.constant.EnumMessage;
import com.erp.model.wms.enums.WaveStatusEnum;
import org.springframework.stereotype.Component;

/**
 * 波次列表高级查询
 * @date 2024-06-20
 * @author tanmujin
 */
@Component
public class WaveListAdvanceQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("wl.status".equals(field) && CharSequenceUtil.isNotBlank((String) value)) {
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        if (WaveStatusEnum.AWAIT_PICK.getCode().equals(value)) {
            super.buildDefaultDTO("wl.status", WaveStatusEnum.AWAIT_PICK.getCode());
        }
        if (WaveStatusEnum.PICK_ING.getCode().equals(value)) {
            super.buildDefaultDTO("wl.status", WaveStatusEnum.PICK_ING.getCode());
        }
        if (WaveStatusEnum.HANG_UP.getCode().equals(value)) {
            super.buildDefaultDTO("wl.status", WaveStatusEnum.HANG_UP.getCode());
        }
        if (WaveStatusEnum.FINISH.getCode().equals(value)) {
            super.buildDefaultDTO("wl.status", WaveStatusEnum.FINISH.getCode());
        }
        return super.getSplicingSQL();
    }
}
