package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.TabFlagEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.ArrivalStatusEnum;
import com.erp.model.scm.enums.PurchaseListTypeEnum;
import com.erp.model.scm.enums.PurchasePriceTabFlagEnum;
import com.erp.server.scm.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class PurchasePriceQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //供应商名称
        if("supplierName".equals(field)){
            return "pp.supplier_id IN (SELECT id FROM supplier  WHERE is_deleted= false "+ compareCodeSplicingValueSql +
                    ")" ;
        }
        //选项卡
        if("tab".equals(field)){
            //待我审核
            if (PurchasePriceTabFlagEnum.APPROVE_ING.getCode().equals(value)) {
                super.buildDefaultDTO("pp.approve_status",Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                //需要审核的业务ids
                List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.PURCHASE_PRICE.getCode());
                if (CollectionUtils.isNotEmpty(businessIds)) {
                    super.buildDefaultDTO("pp.id", businessIds);
                }else{
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
                super.buildDefaultDTO("pp.disabled",Boolean.FALSE);
            }
            //已审核停用
            if (PurchasePriceTabFlagEnum.APPROVE_DISABLED.getCode().equals(value)) {
                super.buildDefaultDTO("pp.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
                super.buildDefaultDTO("pp.disabled", Boolean.TRUE);
            }
        }
        return null;
    }
}

