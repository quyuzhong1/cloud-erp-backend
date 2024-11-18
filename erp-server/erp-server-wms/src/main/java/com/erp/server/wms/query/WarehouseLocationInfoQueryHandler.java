package com.erp.server.wms.query;

import cn.hutool.core.date.CalendarUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.WarehouseLocationStatusEnum;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        if ("wl.status".equals(field) && CharSequenceUtil.isNotBlank((String) value)) {
            return getTabSql(value);
        }
        /*if("wl.update_time".equals(field)) {
            return getDateSql(value, compareCodeSplicingValueSql);
        }*/
        return null;
    }

    private String getDateSql(Object value, String compareCodeSplicingValueSql) {
        if(value instanceof ArrayList) {
            ArrayList<String> dateList = (ArrayList<String>) value;
            super.buildSplicingSQLDTO("wl.update_time", QueryConditionEnum.GE, dateList.get(0), QueryDataTypeEnum.STRING);
            super.buildSplicingSQLDTO("wl.update_time", QueryConditionEnum.LE, dateList.get(1), QueryDataTypeEnum.STRING);
            return super.getSplicingSQL();
        }

        String compare = compareCodeSplicingValueSql.substring(0, 2);
        if(QueryConditionEnum.GE.getCode().equals(compare)) {
            super.buildSplicingSQLDTO("wl.update_time", QueryConditionEnum.GE, value, QueryDataTypeEnum.STRING);
        }
        if(QueryConditionEnum.LE.getCode().equals(compare)) {
            super.buildSplicingSQLDTO("wl.update_time", QueryConditionEnum.LE, value, QueryDataTypeEnum.STRING);
        }

        return super.getSplicingSQL();
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
