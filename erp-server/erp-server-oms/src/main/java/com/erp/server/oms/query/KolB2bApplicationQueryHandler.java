package com.erp.server.oms.query;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.oms.enums.KolB2bApplicationTableEnum;
import com.erp.model.oms.enums.KolB2bRefStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.wms.enums.DeliveryStatusEnum;
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
                return " not exists (SELECT sd.source_detail_id FROM so_info si inner join so_detail sd on si.id = sd.main_id and sd.is_deleted = false WHERE si.is_deleted= false and si.invalid_status = false and kbad.id = sd.source_detail_id)" ;
            }
            if (KolB2bRefStatusEnum.WAIT_APPROVE.getCode().equals(value)) {
                return " exists (SELECT sd.source_detail_id FROM so_info si inner join so_detail sd on si.id = sd.main_id and sd.is_deleted = false WHERE si.is_deleted= false and si.invalid_status = false and si.approve_status != 'approve' and kbad.id = sd.source_detail_id)" ;
            }
            if (KolB2bRefStatusEnum.APPROVED.getCode().equals(value)) {
                return " exists (SELECT sd.source_detail_id FROM so_info si inner join so_detail sd on si.id = sd.main_id and sd.is_deleted = false WHERE si.is_deleted= false and si.invalid_status = false and si.approve_status = 'approve' and kbad.id = sd.source_detail_id)" ;
            }
        }
        if ("projectTag".equals(field)) {
            return " exists (select 1 from jsonb_array_elements_text(kbad.project_tag) AS elem where elem "+ compareCodeSplicingValueSql +")" ;
        }
        if ("deliveryStatusName".equals(field)) {
            if (CharSequenceUtil.equals(DeliveryStatusEnum.UN_SHIPPED.getCode(), value.toString())) {
                return " not exists (SELECT sd.source_detail_id FROM so_info si inner join so_detail sd on si.id = sd.main_id and sd.is_deleted = false WHERE si.is_deleted= false and si.invalid_status = false and sd.delivery_status in ('partialShipment','completeShipment') and kbad.id = sd.source_detail_id)" ;
            }
            return "exists (SELECT sd.source_detail_id FROM so_info si inner join so_detail sd on si.id = sd.main_id and sd.is_deleted = false WHERE si.is_deleted= false and si.invalid_status = false and kbad.id = sd.source_detail_id and sd.delivery_status "+ compareCodeSplicingValueSql +
                    ")" ;
        }
        if ("feedbackUrl".equals(field)) {
            return " exists (SELECT source_detail_id FROM kol_sample_cost_ref_feedback_url WHERE is_deleted = false and source_type = '" + SourceTypeEnum.KOL_B2B_APPLICATION.getCode() + "' and source_detail_id = kbad.id and url "+ compareCodeSplicingValueSql +
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

        //待提交
        if(KolB2bApplicationTableEnum.WAIT_SUBMIT.getCode().equals(value)){
            super.buildDefaultDTO("kba.invalid_status", Collections.singletonList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
            super.buildDefaultDTO("kba.approve_status", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getCode()));
        }

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
        //审核不通过
        if(KolB2bApplicationTableEnum.REJECT.getCode().equals(value)){
            super.buildDefaultDTO("kba.invalid_status", Collections.singletonList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
            super.buildDefaultDTO("kba.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getCode()));
        }
        //待发货
        if(KolB2bApplicationTableEnum.WAIT_SHIPPED.getCode().equals(value)){
            return " kba.approve_status = 'approve' and not exists (select * from so_detail sd where sd.is_deleted = false and sd.source_detail_id = kbad.id and sd.delivery_status in ('partialShipment','completeShipment'))";
        }
        //已发货
        if(KolB2bApplicationTableEnum.SHIPPED.getCode().equals(value)){
            return " kba.approve_status = 'approve' and exists (select * from so_detail sd where sd.is_deleted = false and sd.source_detail_id = kbad.id and sd.delivery_status = 'completeShipment')";
        }
        return super.getSplicingSQL();
    }
}

