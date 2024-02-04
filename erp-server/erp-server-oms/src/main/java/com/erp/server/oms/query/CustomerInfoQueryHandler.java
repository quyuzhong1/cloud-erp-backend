package com.erp.server.oms.query;

import com.common.business.constant.SearchType;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.ArrivalStatusEnum;
import com.erp.model.scm.enums.PurchaseListTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class CustomerInfoQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("ci.tab".equals(field)){
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }
            if (SearchType.WAIT_SUBMIT.equals(searchType)) {
                super.buildDefaultDTO("ci.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }
            //待审核
            if (SearchType.WAIT_APPROVE.equals(searchType)) {
                super.buildDefaultDTO("ci.approve_status", ApproveStatusEnum.APPROVE_ING.getStatus());
            }

            //已审核
            if (ApproveStatusEnum.APPROVE.getStatus().equals(searchType)) {
                super.buildDefaultDTO("ci.approve_status", ApproveStatusEnum.APPROVE.getStatus());
            }

            //审核不通过
            if (ApproveStatusEnum.REJECT.getStatus().equals(searchType)) {
                super.buildDefaultDTO("ci.approve_status", ApproveStatusEnum.REJECT.getStatus());
            }
        }
        return null;
    }
}

