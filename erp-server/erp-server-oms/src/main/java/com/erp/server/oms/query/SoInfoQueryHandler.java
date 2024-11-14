package com.erp.server.oms.query;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.oms.constant.OmsConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class SoInfoQueryHandler extends AbstractQueryHandler {

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("trackNo".equals(field)){
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.CONTAINS)
                    || queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH) ||  queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)){
                List<SoOutstockEntity> soOutstockList = soOutstockFeign.listByAdvanceQuery(AdvanceQueryContainer.builder()
                        .advanceQueryDTOList(Arrays.asList(AdvanceQueryDTO.buildSplicingSQLDTO("track_no", AdvanceQueryContext.getCompareCode(),value, QueryDataTypeEnum.STRING)
                                ,AdvanceQueryDTO.buildSplicingSQLDTO("invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN)))
                        .build());
                List<String> soIdList = soOutstockList.stream().map(SoOutstockEntity::getSoId).distinct().collect(Collectors.toList());
                if(CollectionUtils.isEmpty(soIdList)){
                    return this.getQueryEmptySql();
                }
                this.buildDefaultDTO("si.id",soIdList);
            }

            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
                QueryConditionEnum queryCond = queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)?QueryConditionEnum.CONTAINS:QueryConditionEnum.IN_LIST;
                List<SoOutstockEntity> soOutstockList = soOutstockFeign.listByAdvanceQuery(AdvanceQueryContainer.builder()
                        .advanceQueryDTOList(Arrays.asList(AdvanceQueryDTO.buildSplicingSQLDTO("track_no", queryCond,value, QueryDataTypeEnum.STRING)
                                ,AdvanceQueryDTO.buildSplicingSQLDTO("invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN)))
                        .build());
                List<String> soIdList = soOutstockList.stream().map(SoOutstockEntity::getSoId).distinct().collect(Collectors.toList());
                if(CollectionUtils.isEmpty(soIdList)){
                    return this.getQueryAllSql();
                }
                this.buildSplicingSQLDTO("si.id",QueryConditionEnum.NOT_IN_LIST,soIdList,QueryDataTypeEnum.STRING);
            }
        }
        if("remark".equals(field)){
            return " (si.remark "+compareCodeSplicingValueSql+" or sod.remark "+ compareCodeSplicingValueSql +") ";
        }
        /**
         * 虚拟仓是否缺货
         */
        if("isVirtualScarce".equals(field)){
            String sql = "COALESCE(sdnd.deliveryQty,0) - COALESCE(vi.virtualQty,0)";
            if ((Boolean) value) {
                return sql + "< 0";
            } else {
                return sql + ">= 0";
            }
        }

        if("tab".equals(field)){
            switch (value.toString()) {
                case OmsConstant.WAIT_SUBMIT:
                    // 待提交
                    super.buildDefaultDTO("si.approve_status",ApproveStatusEnum.WAIT_SUBMIT.getStatus());
                    break;
                case OmsConstant.WAIT_APPROVE:
                    //待审核
                    super.buildDefaultDTO("si.approve_status",ApproveStatusEnum.APPROVE_ING.getStatus());
                    break;
                case OmsConstant.REJECT:
                    //审核不通过
                    super.buildDefaultDTO("si.approve_status",ApproveStatusEnum.REJECT.getStatus());
                    break;
                //未发货
                case OmsConstant.WAIT_DELIVERY:
                    super.buildDefaultDTO("si.approve_status",ApproveStatusEnum.APPROVE.getStatus());
                    super.buildDefaultDTO("sod.delivery_status",Arrays.asList(DeliveryStatusEnum.UN_SHIPPED.getCode(),DeliveryStatusEnum.PARTIAL_SHIPMENT.getCode()));
                break;
                //已发货
                case OmsConstant.DELIVERY:
                    super.buildDefaultDTO("si.approve_status",ApproveStatusEnum.APPROVE.getStatus());
                    super.buildDefaultDTO("sod.delivery_status",Arrays.asList(DeliveryStatusEnum.COMPLETE_SHIPMENT.getCode()));
                break;

            }
        }
        return null;
    }
}

