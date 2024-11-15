package com.erp.server.wms.query;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.PoReturnConfirmStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SoReturnReceiveQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            if(CharSequenceUtil.isBlank(value.toString()) || "all".equals(value.toString())){
                return this.getQueryAllSql();
            }
            if("toBeApprove".equals(value.toString())){
                super.buildDefaultDTO("srr.approve_status", ApproveStatusEnum.APPROVE_ING.getStatus());
            }else{
                super.buildDefaultDTO("srr.approve_status",value);
            }
            super.buildSplicingSQLDTO("srr.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        return null;
    }
}
