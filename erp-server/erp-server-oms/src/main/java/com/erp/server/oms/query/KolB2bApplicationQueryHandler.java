package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.oms.enums.KolB2bApplicationTableEnum;
import com.erp.model.oms.enums.KolB2bRefStatusEnum;
import com.erp.server.oms.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author jack
 * @date 2025-12-03
 */
@Component
public class KolB2bApplicationQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if ("b2bRefStatusName".equals(field)) {
            if (KolB2bRefStatusEnum.WAIT_GENERATE.getCode().equals(value)) {
                return "not exists (SELECT sd.source_detail_id FROM so_info si inner join so_detail sd on si.id = sd.main_id and sd.is_deleted = false WHERE si.is_deleted= false and si.invalid_status = false and kbad.id = sd.source_detail_id)" ;
            }
            if (KolB2bRefStatusEnum.WAIT_APPROVE.getCode().equals(value)) {
                return " exists (SELECT sd.source_detail_id FROM so_info si inner join so_detail sd on si.id = sd.main_id and sd.is_deleted = false WHERE si.is_deleted= false and si.invalid_status = false and si.approve_status != 'approve' and kbad.id = sd.source_detail_id)" ;
            }
            if (KolB2bRefStatusEnum.APPROVED.getCode().equals(value)) {
                return "not exists (SELECT sd.source_detail_id FROM so_info si inner join so_detail sd on si.id = sd.main_id and sd.is_deleted = false WHERE si.is_deleted= false and si.invalid_status = false and si.approve_status = 'approve' and kbad.id = sd.source_detail_id)" ;
            }
        }
        if ("deliveryStatusName".equals(field)) {
            return "kbad.id IN (SELECT sd.source_detail_id FROM so_info si inner join so_detail sd on si.id = sd.main_id and sd.is_deleted = false WHERE si.is_deleted= false and si.invalid_status = false and sd.delivery_status "+ compareCodeSplicingValueSql +
                    ")" ;
        }
        if ("feedbackUrl".equals(field)) {
            return "kbad.id IN (SELECT source_detail_id FROM kol_feedback  WHERE is_deleted= false and url "+ compareCodeSplicingValueSql +
                    ")" ;
        }
        return null;
    }

    /**
     * @description: tabSql拼接
     * @author Will
     * @date: 2025/12/03 19:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {

        //待我审核
        if(KolB2bApplicationTableEnum.TO_BE_APPROVE.getCode().equals(value)){
            super.buildDefaultDTO("kba.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
            //需要审核的业务ids
            List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.KOL_B2B_APPLICATION.getCode());
            if (CollectionUtils.isNotEmpty(businessIds)) {
                super.buildDefaultDTO("kba.id", businessIds);
            }else{
                //返回空结果
                return this.getQueryEmptySql();
            }
        }
        //审核通过
        if(KolB2bApplicationTableEnum.APPROVE.getCode().equals(value)){
            super.buildDefaultDTO("kba.approve_status", ApproveStatusEnum.APPROVE.getCode());
        }
        //审核不通过
        if(KolB2bApplicationTableEnum.REJECT.getCode().equals(value)){
            super.buildDefaultDTO("kba.approve_status", ApproveStatusEnum.REJECT.getCode());
        }
        //待发货
        if(KolB2bApplicationTableEnum.WAIT_SHIPPED.getCode().equals(value)){
            return "not exists (select * from so_detail sd where sd.is_deleted = false and sd.source_detail_id = kbad.id and sd.delivery_status in ('partialShipment','completeShipment'))";
        }
        //已发货
        if(KolB2bApplicationTableEnum.SHIPPED.getCode().equals(value)){
            return "exists (select * from so_detail sd where sd.is_deleted = false and sd.source_detail_id = kbad.id and sd.delivery_status = 'completeShipment')";
        }
        return super.getSplicingSQL();
    }
}

