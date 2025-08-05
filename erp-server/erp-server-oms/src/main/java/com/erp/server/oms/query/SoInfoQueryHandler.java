package com.erp.server.oms.query;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.threadlocal.UserContext;
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
                return "exists ( SELECT 1 from so_outstock sout where sout.is_deleted = false and sout.so_id = si.id and sout.invalid_status = false and sout.track_no " + compareCodeSplicingValueSql +")";
            }

            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
                return " not exists ( SELECT 1 from so_outstock sout where sout.is_deleted = false and sout.so_id = si.id and sout.invalid_status = false and sout.track_no " + compareCodeSplicingValueSql +")";

            }
        }
        if("remark".equals(field)){
            return " (si.remark "+compareCodeSplicingValueSql+" or sod.remark "+ compareCodeSplicingValueSql +") ";
        }
        /**
         * 虚拟仓是否缺货
         */
        if("isVirtualScarce".equals(field)){
            return getQueryAllSql();
        }

        /**
         * 虚拟仓是否缺货
         */
        if("isVirtualOutStock".equals(field)){
//            String sql = "COALESCE(sdnd.deliveryQty,0) - COALESCE(vi.virtualQty,0)";
//            if ((Boolean) value) {
//                return sql + "< 0";
//            } else {
//                return sql + ">= 0";
//            }
            return getQueryAllSql();
        }

        if("tab".equals(field)){
            switch (value.toString()) {
                case OmsConstant.WAIT_SUBMIT:
                    // 待提交
                    super.buildDefaultDTO("si.approve_status",ApproveStatusEnum.WAIT_SUBMIT.getStatus());
                    break;
                case OmsConstant.WAIT_APPROVE:
                    //待审核
                    return "si.approve_status = 'approveIng' and  exists (\n" +
                            "\t\t\t\tSELECT 1  \n" +
                            "\t\t\t\tFROM foreign_process_management pm\n" +
                            "        inner JOIN foreign_process_task_management ptm ON pm.process_instance_id = ptm.process_instance_id AND ptm.is_deleted = false AND ptm.task_status = 'approveIng'\n" +
                            "\t\t\t\twhere ptm.cur_approve_id = '"+ UserContext.getDefaultLoginUser().getUid()+"' and pm.business_key = 'soInfo'\n" +
                            "\t\t\t\tand pm.business_id = si.id\n" +
                            "\t\t\t\t)";
                case OmsConstant.REJECT:
                    //审核不通过
                    super.buildDefaultDTO("si.approve_status",ApproveStatusEnum.REJECT.getStatus());
                    break;
                //待发货：审核通过 + 发货明细部分未发货 AND 发货明细未关闭
                case OmsConstant.WAIT_DELIVERY:
                    return "si.approve_status = 'approve' AND EXISTS (SELECT 1 FROM so_detail sd WHERE sd.main_id = si.id AND sd.is_deleted = FALSE " +
                           "AND (sd.delivery_status = 'unShipped' OR sd.delivery_status = 'partialShipment') " +
                           "AND sd.is_close = FALSE)";
                //已发货：审核通过 + 所有明细已发货，或审核通过 + 发货明细部分未发货并且发货明细已关闭
                case OmsConstant.DELIVERY:
                    return "si.approve_status = 'approve' AND ((NOT EXISTS (SELECT 1 FROM so_detail sd WHERE sd.main_id = si.id AND sd.is_deleted = FALSE " +
                           "AND sd.delivery_status != 'completeShipment')) " +
                           "OR (EXISTS (SELECT 1 FROM so_detail sd WHERE sd.main_id = si.id AND sd.is_deleted = FALSE " +
                           "AND (sd.delivery_status = 'unShipped' OR sd.delivery_status = 'partialShipment') " +
                           "AND sd.is_close = TRUE)))";

            }
        }
        return null;
    }
}

