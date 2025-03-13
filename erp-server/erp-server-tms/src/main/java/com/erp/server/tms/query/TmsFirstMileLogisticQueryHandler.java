package com.erp.server.tms.query;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.oms.dto.ListingAdvanceQueryDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.enums.WeightAllocationStatusEnum;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TmsFirstMileLogisticQueryHandler extends AbstractQueryHandler {

    @Resource
    private WmsFirstMileDeliveryFeign firstMileDeliveryFeign;

    @Resource
    private TmsFirstMileLogisticService tmsFirstMileLogisticService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if(field.equals("deliveryWarehouse") || field.equals("toWarehouse")){
            String queryField;
            if(field.equals("deliveryWarehouse")){
                queryField = "fw.id";
            }else{
                queryField = "tw.id";
            }
            List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
            AdvanceQueryDTO preDto = AdvanceQueryDTO.buildDefaultSplicingSQLDTO("fmd.logistics_status","finish");
            advanceQueryDTOList.add(preDto);
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.CONTAINS)
                    || queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH) ||  queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(queryField,queryConditionEnum,value, QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();

                List<FirstMileDeliveryEntity> list = firstMileDeliveryFeign.advanceQuery(advanceQueryContainer);
                List<String> ids = list.stream().map(FirstMileDeliveryEntity::getId).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(ids)) {
                    return getQueryEmptySql();
                }
                super.buildSplicingSQLDTO("lb.outstock_id",QueryConditionEnum.IN_LIST,ids,QueryDataTypeEnum.STRING);

            }

            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(queryField,QueryConditionEnum.IN_LIST,value,QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();
                List<FirstMileDeliveryEntity> list = firstMileDeliveryFeign.advanceQuery(advanceQueryContainer);
                List<String> ids = list.stream().map(FirstMileDeliveryEntity::getId).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(ids)) {
                    return getQueryAllSql();
                }
                super.buildSplicingSQLDTO("lb.outstock_id",QueryConditionEnum.NOT_IN_LIST,ids,QueryDataTypeEnum.STRING);
            }
        }

        if(field.equals("signTime")){
            compareCodeSplicingValueSql = compareCodeSplicingValueSql.replace("signTime","lt.track_time");
            return " exists (SELECT 1 from  logistics_track lt where lt.is_deleted = false and lt.status = 'sign' and lt.track_no = lbd.track_no AND lt.track_time = (select max(track_time) from logistics_track b where b.track_no = lbd.track_no and b.is_deleted = false) and lt.track_time "+compareCodeSplicingValueSql+" ) ";
        }
        if(field.equals("weightAllocationStatus")){
            if (WeightAllocationStatusEnum.TODO.equals(value)){
                return "not exists (select 1 from first_mile_weight_allocation fmwa where fmwa.is_deleted = false and fmwa.logistics_bill_id = lb.id limit 1)";
            }else {
                return "exists (select 1 from first_mile_weight_allocation fmwa where fmwa.is_deleted = false and fmwa.logistics_bill_id = lb.id limit 1)";
            }
        }
        if(field.equals("warn")){
            List<TmsFirstMileLogisticDTO.PagingVO> list = tmsFirstMileLogisticService.hasWarnPaging(new TmsFirstMileLogisticDTO.PagingParamDTO());
            List<String> ids = new ArrayList<>();
            list = list.stream().filter(v-> Objects.nonNull(v.getWarnHour())).collect(Collectors.toList());
            if(value.equals("overdue")){
                ids = list.stream().filter(v->v.getWarnHour() <= 72 && v.getWarnHour() > 0).map(TmsFirstMileLogisticDTO.PagingVO::getId).collect(Collectors.toList());
            }
            if(value.equals("expired")){
                ids = list.stream().filter(v->v.getWarnHour() < 0).map(TmsFirstMileLogisticDTO.PagingVO::getId).collect(Collectors.toList());
            }
            if(value.equals("normal")){
                ids = list.stream().filter(v->v.getWarnHour() > 72).map(TmsFirstMileLogisticDTO.PagingVO::getId).collect(Collectors.toList());
            }
            if(CollectionUtils.isEmpty(ids)){
                return this.getQueryEmptySql();
            }
            super.buildSplicingSQLDTO("lb.id",QueryConditionEnum.IN_LIST,ids,QueryDataTypeEnum.STRING);
        }

        if(field.equals("reconciliationStatus")){
            return " lb.id in  (\n" +
                    "\t\tSELECT\n" +
                    "\t\t\tlogistics_bill_id \n" +
                    "\t\tFROM\n" +
                    "\t\t\t(\n" +
                    "\t\t\tSELECT\n" +
                    "\t\t\t\tbill.logistics_bill_id,\n" +
                    "\t\t\t\tbill.reconciliation_status,\n" +
                    "\t\t\t\tROW_NUMBER ( ) OVER ( PARTITION BY bill.logistics_bill_id ORDER BY bill.create_time ) AS rn \n" +
                    "\t\t\tFROM\n" +
                    "\t\t\t\tlogistics_bill_cost bill\n" +
                    "\t\t\t\tINNER JOIN logistics_bill lb on lb.id = bill.logistics_bill_id and lb.is_deleted = false\n" +
                    "\t\t\t\tLEFT JOIN tms_first_mile_reconciliation_detail tfmrd ON bill.reconciliation_id = tfmrd.main_id \n" +
                    "\t\t\t\tAND tfmrd.is_deleted = FALSE \n" +
                    "\t\t\t\tAND lb.order_type = 'firstMile' \n" +
                    "\t\t\t\tAND tfmrd.\"type\" = 'actual' \n" +
                    "\t\t\t\tAND tfmrd.reconciliation_count = 1 \n" +
                    "\t\t\tWHERE\n" +
                    "\t\t\t\tbill.is_deleted = FALSE \n" +
                    "\t\t\t) AS detail \n" +
                    "\t\tWHERE\n" +
                    "\t\t\trn = 1 \n" +
                    "\t\t\tAND reconciliation_status  "+compareCodeSplicingValueSql+" \n" +
                    "\t\t) ";
        }
        return null;
    }
}

