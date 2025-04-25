package com.erp.server.scm.query;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.ArrivalStatusEnum;
import com.erp.model.scm.enums.PurchaseTableFlagEnum;
import com.erp.server.scm.service.CommonService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Component
public class SubcontractOrderQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("so.approve_status".equals(field)) {
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }
            super.buildDefaultDTO("so.approve_status", value.toString());
        }

        if ("poCode".equals(field)) {
            return "EXISTS (select source_id from purchase_order where is_deleted = false and source_id = so.id and code "+ compareCodeSplicingValueSql +" )";
        }

        if ("tab".equals(field)) {
            List<String> approveStatusList = new ArrayList<>(1);
            List<String> arrivalStatusList = new ArrayList<>(2);
            if (PurchaseTableFlagEnum.TO_BE_APPROVE.getCode().equals(value)) {
                approveStatusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
                //需要审核的业务ids
                List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
                if (CollectionUtils.isNotEmpty(businessIds)) {
                    super.buildDefaultDTO("so.id", businessIds);
                }
            }
            // 待提交
            if (PurchaseTableFlagEnum.WAIT_SUBMIT.getCode().equals(value)) {
                approveStatusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }
            //待到货
            if (PurchaseTableFlagEnum.TO_BE_CREATE.getCode().equals(value)) {
                approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
                arrivalStatusList.add(ArrivalStatusEnum.NON_ARRIVAL.getCode());
                arrivalStatusList.add(ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode());
            }
            //已到货
            if (PurchaseTableFlagEnum.CREATED.getCode().equals(value)) {
                approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
                arrivalStatusList.add(ArrivalStatusEnum.ARRIVED.getCode());
            }
            //不通过
            if (PurchaseTableFlagEnum.REJECT.getCode().equals(value)) {
                approveStatusList.add(ApproveStatusEnum.REJECT.getStatus());
            }
            if (CollectionUtils.isNotEmpty(approveStatusList)) {
                super.buildDefaultDTO("so.approve_status", approveStatusList);
            }
            if (CollectionUtils.isNotEmpty(arrivalStatusList)) {
                super.buildDefaultDTO("sod.arrival_status", arrivalStatusList);
            }
            return super.getSplicingSQL();
        }
        return null;
    }
}

