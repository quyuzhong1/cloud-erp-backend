package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.server.oms.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author Will
 * @date: 2024/2/28 9:49
 */
@Component
public class SoChangeQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }


    /**
     * @description: tabSql
     * @author Will
     * @date: 2024/2/26 15:55
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        //待我审核
        if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("sc.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
            //需要审核的业务ids
            List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.SO_CHANGE.getCode());
            if (CollectionUtils.isNotEmpty(businessIds)) {
                super.buildDefaultDTO("sc.id", businessIds);
            }else{
                //返回空结果
                return this.getQueryEmptySql();
            }
        }
        //已审核
        if (PageListTypeEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("sc.approve_status", ApproveStatusEnum.APPROVE.getStatus());
        }
        //审核不通过
        if (PageListTypeEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("sc.approve_status", ApproveStatusEnum.REJECT.getStatus());
        }
        return super.getSplicingSQL();
    }
}

