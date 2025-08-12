package com.erp.server.plm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.utils.QueryUtils;
import com.erp.model.plm.enums.SkuStdCostTabEnum;
import com.erp.model.scm.enums.PurchasePriceTabFlagEnum;
import com.erp.server.plm.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@Component
public class SkuStdCostDetailQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        //选项卡
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if ("ps.sale_state".equals(field)){
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)){
                return "ps.sale_state is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)){
                return "ps.sale_state is not null";
            }
            return " ps.sale_state " + compareCodeSplicingValueSql;
        }
        return null;
    }

    /**
     *
     */
    public String getTabSql(Object value) {
        //待我审核
        if (SkuStdCostTabEnum.TO_BE_APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("sscd.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
            //需要审核的业务ids
//            List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.PURCHASE_PRICE.getCode());
            List<String> businessIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(businessIds)) {
                super.buildDefaultDTO("pp.id", businessIds);
            } else {
                //返回空结果
                return this.getQueryEmptySql();
            }
        }
        // 不通过
        if (PurchasePriceTabFlagEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("sscd.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
        }
        //已审核启用
        if (PurchasePriceTabFlagEnum.APPROVE_ENABLE.getCode().equals(value)) {
            super.buildDefaultDTO("sscd.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
        }
        //已审核停用
        if (PurchasePriceTabFlagEnum.APPROVE_DISABLED.getCode().equals(value)) {
            super.buildDefaultDTO("sscd.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
        }
        return super.getSplicingSQL();
    }

}

