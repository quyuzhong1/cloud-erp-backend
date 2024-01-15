package com.erp.server.scm.query;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.query.IQueryHandler;
import com.common.business.utils.QueryUtils;
import com.erp.model.scm.enums.ArrivalStatusEnum;
import com.erp.model.scm.enums.PurchaseListTypeEnum;
import com.erp.server.scm.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class PurchaseOrderQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("so.code".equals(field)){
            value = "'"+value+"'";
            return "case when  subString ( " + value + ",1,2) != 'PL' then po.source_code "+ compareCodeSplicingValueSql +
                    " else EXISTS ( select pa.id from purchase_application pa left join purchase_application_ref_po parp on parp.is_deleted = false and pa.id = parp.purchase_application_id " +
                    " where parp.purchase_order_id = po.id and pa.code "+ compareCodeSplicingValueSql +" ) end";
        }
        if("so.tab".equals(field)){
            //待我审核
            if (PurchaseListTypeEnum.TO_BE_APPROVE.getCode().equals(value)) {
                super.buildDefaultDTO("po.approve_status",Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                //需要审核的业务ids
                List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.PURCHASE_ORDER.getCode());
                if (CollectionUtils.isNotEmpty(businessIds)) {
                    super.buildDefaultDTO("po.id", businessIds);
                }else{
                    //返回空结果
                    return this.getQueryEmptySql();
                }
            }
            // 待提交
            if (PurchaseListTypeEnum.WAIT_SUBMIT.getCode().equals(value)) {
                super.buildDefaultDTO("po.approve_status", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
            }
            //待到货
            if (PurchaseListTypeEnum.TO_BE_CREATE.getCode().equals(value)) {
                super.buildDefaultDTO("po.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
                super.buildDefaultDTO("pod.arrival_status", Arrays.asList(ArrivalStatusEnum.NON_ARRIVAL.getCode(),ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode()));
            }
            //已到货
            if (PurchaseListTypeEnum.CREATED.getCode().equals(value)) {
                super.buildDefaultDTO("po.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
                super.buildDefaultDTO("pod.arrival_status", Arrays.asList(ArrivalStatusEnum.ARRIVED.getCode(),ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode()));
            }
            //不通过
            if (PurchaseListTypeEnum.REJECT.getCode().equals(value)) {
                super.buildDefaultDTO("po.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
            }
        }
        return null;
    }
}

