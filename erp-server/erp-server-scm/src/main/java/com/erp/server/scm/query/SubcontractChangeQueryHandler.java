package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class SubcontractChangeQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            String status = value.toString();
            //待我审核
            if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(status)) {
                super.buildDefaultDTO("sc.approve_status",ApproveStatusEnum.APPROVE_ING.getStatus());
            }
            //已审核
            if (PageListTypeEnum.APPROVE.getCode().equals(status)) {
                super.buildDefaultDTO("sc.approve_status",ApproveStatusEnum.APPROVE.getStatus());
            }
            //不通过
            if (PageListTypeEnum.REJECT.getCode().equals(status)) {
                super.buildDefaultDTO("sc.approve_status",ApproveStatusEnum.REJECT.getStatus());
            }
        }
        return null;
    }
}

