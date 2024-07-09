package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import org.springframework.stereotype.Component;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class PackingTaskQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        if ("packingStatus".equals(field)){
            /**
             * 默认生成为待装箱，根据装箱数量更新，装箱数量=0
             * 装箱中：装箱数量大于0且小于发货数量
             * 已装箱：装箱数量等于发货数量
             */
            if (PackingTaskStatusEnum.UNPACKED.getCode().equals(value)){
                return "(select COALESCE(SUM(wcd.pack_qty), 0) from wms_carton wc left join wms_carton_detail wcd on wcd.main_id = wc.id where wc.packing_task_id = pt.id and wc.is_deleted = false ) = 0";

//                return "exists (select 1 from wms_carton wc left join wms_carton_detail wcd on wcd.main_id = wc.id" +
//                        " where wc.packing_task_id = pt.id and wc.is_deleted = false AND (wc.packing_status = 'incomplete' or wc.packing_status is null )" +
//                        " and (wcd.pack_qty = 0 or wcd.pack_qty is null)";
            }else if (PackingTaskStatusEnum.PACKING.getCode().equals(value)){
                return "(select COALESCE(SUM(wcd.pack_qty), 0) from wms_carton wc left join wms_carton_detail wcd on wcd.main_id = wc.id where wc.packing_task_id = pt.id and wc.is_deleted = false ) > 0";
            }

        }
        if ("weightingStatus".equals(field)){
            /**
             * 全部称重：装箱数量等于拣货数量且称重状态（单箱）全部称重成功
             * 部分称重：装箱数量小于拣货数量或称重状态（单箱）部分称重成功
             * 未称重：装箱数量等于0且称重状态（单箱）未称重
             */

        }
        return null;
    }

    /**
     * @description: tabSql
     * @author Will
     * @date: 2024/2/26 15:55
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        // 待装箱
        if (PackingTaskStatusEnum.UNPACKED.getCode().equals(value)) {
            return "exists (select 1 from wms_carton wc where wc.packing_task_id = pt.id AND wc.packing_status = 'incomplete' and wc.is_deleted = false )";
        }
        // 装箱中
        if (PackingTaskStatusEnum.PACKING.getCode().equals(value)) {
            return "exists (select 1 from wms_carton wc where wc.packing_task_id = pt.id AND wc.packing_status IN ('completed', 'incomplete') and wc.is_deleted = false )";
        }
        //已装箱
        if (PackingTaskStatusEnum.PACKED.getCode().equals(value)) {
            return "exists (select 1 from wms_carton wc where wc.packing_task_id = pt.id AND wc.packing_status = 'completed' and wc.is_deleted = false )";
        }
        return super.getSplicingSQL();
    }
}

