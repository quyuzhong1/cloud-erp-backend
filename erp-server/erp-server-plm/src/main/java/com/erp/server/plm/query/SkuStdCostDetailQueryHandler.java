package com.erp.server.plm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
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

        //选项卡
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    /**
     *
     */
    public String getTabSql(Object value) {
        //待我审核
        if (SkuStdCostTabEnum.TO_BE_APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("pp.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
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
            super.buildDefaultDTO("pp.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
        }
        //已审核启用
        if (PurchasePriceTabFlagEnum.APPROVE_ENABLE.getCode().equals(value)) {
            super.buildDefaultDTO("pp.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
            super.buildDefaultDTO("ppd.disabled", Collections.singletonList(Boolean.FALSE));
        }
        //已审核停用
        if (PurchasePriceTabFlagEnum.APPROVE_DISABLED.getCode().equals(value)) {
            super.buildDefaultDTO("pp.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
            super.buildDefaultDTO("ppd.disabled", Collections.singletonList(Boolean.TRUE));
        }
        return super.getSplicingSQL();
    }

}

