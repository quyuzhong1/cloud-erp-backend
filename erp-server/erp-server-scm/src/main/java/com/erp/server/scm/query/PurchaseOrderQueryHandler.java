package com.erp.server.scm.query;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
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
public class PurchaseOrderQueryHandler implements IQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    public String splicingSQL(String field, String compareCode, Object value, String compareCodeSplicingValueSql) {
        if("so.code".equals(field)){
            value = "'"+value+"'";
            return "case when  subString ( " + value + ",1,2) != 'PL' then po.source_code "+ compareCodeSplicingValueSql +
                    " else EXISTS ( select pa.id from purchase_application pa left join purchase_application_ref_po parp on parp.is_deleted = false and pa.id = parp.purchase_application_id " +
                    " where parp.purchase_order_id = po.id and pa.code "+ compareCodeSplicingValueSql +" ) end";
        }
        if("so.tab".equals(field)){
            //待我审核
            List<AdvanceQueryDTO> dtoList = new ArrayList<>();
            if (PurchaseListTypeEnum.TO_BE_APPROVE.getCode().equals(value)) {
                AdvanceQueryDTO approveQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO("po.approve_status", QueryConditionEnum.IN_LIST, Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()), QueryDataTypeEnum.STRING);
                dtoList.add(approveQueryDTO);
                //需要审核的业务ids
                List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.PURCHASE_ORDER.getCode());
                if (CollectionUtils.isNotEmpty(businessIds)) {
                    AdvanceQueryDTO queryIdDTO = AdvanceQueryDTO.buildSplicingSQLDTO("po.id", QueryConditionEnum.IN_LIST, businessIds, QueryDataTypeEnum.STRING);
                    dtoList.add(queryIdDTO);
                }else{
                    //返回空结果
                    return this.getQueryEmptySql();
                }
            }
            // 待提交
            if (PurchaseListTypeEnum.WAIT_SUBMIT.getCode().equals(value)) {
                AdvanceQueryDTO approveQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO("po.approve_status", QueryConditionEnum.IN_LIST, Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()), QueryDataTypeEnum.STRING);
                dtoList.add(approveQueryDTO);
            }
            //待到货
            if (PurchaseListTypeEnum.TO_BE_CREATE.getCode().equals(value)) {
                AdvanceQueryDTO approveQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO("po.approve_status", QueryConditionEnum.IN_LIST, Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()), QueryDataTypeEnum.STRING);
                dtoList.add(approveQueryDTO);
                AdvanceQueryDTO arrivalStatusDTO = AdvanceQueryDTO.buildSplicingSQLDTO("pod.arrival_status", QueryConditionEnum.IN_LIST, Arrays.asList(ArrivalStatusEnum.NON_ARRIVAL.getCode(),ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode()), QueryDataTypeEnum.STRING);
                dtoList.add(arrivalStatusDTO);
            }
            //已到货
            if (PurchaseListTypeEnum.CREATED.getCode().equals(value)) {
                AdvanceQueryDTO approveQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO("po.approve_status", QueryConditionEnum.IN_LIST, Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()), QueryDataTypeEnum.STRING);
                dtoList.add(approveQueryDTO);
                AdvanceQueryDTO arrivalStatusDTO = AdvanceQueryDTO.buildSplicingSQLDTO("pod.arrival_status", QueryConditionEnum.IN_LIST, Arrays.asList(ArrivalStatusEnum.ARRIVED.getCode(),ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode()), QueryDataTypeEnum.STRING);
                dtoList.add(arrivalStatusDTO);
            }
            //不通过
            if (PurchaseListTypeEnum.REJECT.getCode().equals(value)) {
                AdvanceQueryDTO approveQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO("po.approve_status", QueryConditionEnum.IN_LIST, Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()), QueryDataTypeEnum.STRING);
                dtoList.add(approveQueryDTO);
            }
            return QueryUtils.splicingSQL(dtoList);
        }
        return null;
    }
}

