package com.erp.server.wms.query;

import cn.hutool.core.date.CalendarUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.WarehouseLocationStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @date 2024-06-03
 * @author tanmujin
 */
@Component
public class WarehouseLocationInfoQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("wl.status".equals(field)) {
            return getTabSql(value);
        }
        if("wl.update_time".equals(field)) {
            DateTime date = DateUtil.parseDate((CharSequence) value);
            Calendar calendar = CalendarUtil.calendar(date);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            String dateString = DateUtil.formatDate(calendar.getTime());
            super.buildSplicingSQLDTO("wl.update_time", QueryConditionEnum.GE, value, QueryDataTypeEnum.STRING);
            super.buildSplicingSQLDTO("wl.update_time", QueryConditionEnum.LE, dateString, QueryDataTypeEnum.STRING);
            return super.getSplicingSQL();
        }
        return null;
    }

    public String getTabSql(Object value) {
        if (WarehouseLocationStatusEnum.IDLE.getCode().equals(value)) {
            super.buildDefaultDTO("wl.status", WarehouseLocationStatusEnum.IDLE.getCode());
        }
        if (WarehouseLocationStatusEnum.RECYCLABLE.getCode().equals(value)) {
            super.buildDefaultDTO("wl.status", WarehouseLocationStatusEnum.RECYCLABLE.getCode());
        }
        if (WarehouseLocationStatusEnum.OCCUPIED.getCode().equals(value)) {
            super.buildDefaultDTO("wl.status", WarehouseLocationStatusEnum.OCCUPIED.getCode());
        }
        return super.getSplicingSQL();
    }
}
