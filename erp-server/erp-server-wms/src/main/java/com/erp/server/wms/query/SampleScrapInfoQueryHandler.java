package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BaseStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.UserContext;
import com.erp.model.wms.enums.QcNoticeStatusEnum;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SampleScrapInfoQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)){
            return getQueryAllSql();
        }
        super.buildDefaultDTO("ssi.approve_status", value);
        return super.getSplicingSQL();
    }
}
