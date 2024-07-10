package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.PackingWeightStatusEnum;
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
//                return "(select COALESCE(SUM(wcd.pack_qty), 0) from wms_carton wc left join wms_carton_detail wcd on wcd.main_id = wc.id where wc.packing_task_id = pt.id and wc.is_deleted = false ) = 0";
                return "EXISTS ( SELECT 1 FROM wms_carton wc LEFT JOIN wms_carton_detail wcd ON wcd.main_id = wc.id WHERE wc.packing_task_id = pt.id AND wc.is_deleted = FALSE GROUP BY wc.packing_task_id HAVING COALESCE(SUM(wcd.pack_qty), 0) = 0)";
            }else if (PackingTaskStatusEnum.PACKING.getCode().equals(value)){
//                return "(select COALESCE(SUM(wcd.pack_qty), 0) from wms_carton wc left join wms_carton_detail wcd on wcd.main_id = wc.id where wc.packing_task_id = pt.id and wc.is_deleted = false ) > 0" +
//                        " and " +
//                        " ((select COALESCE(SUM(wcd.pack_qty), 0) from wms_carton wc left join wms_carton_detail wcd on wcd.main_id = wc.id where wc.packing_task_id = pt.id and wc.is_deleted = false ) <" +
//                        " (select COALESCE(SUM(wcd.delivery_qty), 0) from packing_task_detail ptd where ptd.main_id = pt.id and ptd.is_deleted = false))";
                return "EXISTS (SELECT 1 FROM wms_carton wc LEFT JOIN wms_carton_detail wcd ON wcd.main_id = wc.id WHERE wc.packing_task_id = pt.id AND wc.is_deleted = FALSE GROUP BY wc.packing_task_id HAVING COALESCE(SUM(wcd.pack_qty), 0) > 0)" +
                        " AND " +
                        " EXISTS (SELECT 1 FROM (SELECT ptd.main_id, COALESCE(SUM(ptd.delivery_qty), 0) AS total_delivery_qty FROM packing_task_detail ptd WHERE ptd.is_deleted = FALSE GROUP BY ptd.main_id) AS ptd_agg WHERE ptd_agg.main_id = pt.id AND ptd_agg.total_delivery_qty > " +
                        " COALESCE((SELECT SUM(wcd.pack_qty) FROM wms_carton wc LEFT JOIN wms_carton_detail wcd ON wcd.main_id = wc.id WHERE wc.packing_task_id = pt.id AND wc.is_deleted = FALSE), 0))";
            }else if (PackingTaskStatusEnum.PACKED.getCode().equals(value)){
//                return "(select COALESCE(SUM(wcd.pack_qty), 0) from wms_carton wc left join wms_carton_detail wcd on wcd.main_id = wc.id where wc.packing_task_id = pt.id and wc.is_deleted = false ) =" +
//                        " (select COALESCE(SUM(wcd.delivery_qty), 0) from packing_task_detail ptd where ptd.main_id = pt.id and ptd.is_deleted = false)";
                return " EXISTS (SELECT 1 FROM (SELECT ptd.main_id, COALESCE(SUM(ptd.delivery_qty), 0) AS total_delivery_qty FROM packing_task_detail ptd WHERE ptd.is_deleted = FALSE GROUP BY ptd.main_id) AS ptd_agg WHERE ptd_agg.main_id = pt.id AND ptd_agg.total_delivery_qty = " +
                        " COALESCE((SELECT SUM(wcd.pack_qty) FROM wms_carton wc LEFT JOIN wms_carton_detail wcd ON wcd.main_id = wc.id WHERE wc.packing_task_id = pt.id AND wc.is_deleted = FALSE), 0)";
            }

        }
        if ("weightingStatus".equals(field)){
            /**
             * 全部称重：装箱数量等于拣货数量且称重状态（单箱）全部称重成功
             * 部分称重：装箱数量小于拣货数量或称重状态（单箱）部分称重成功
             * 未称重：装箱数量等于0且称重状态（单箱）未称重
             */
            if (PackingWeightStatusEnum.WEIGHTED.getCode().equals(value)){
                return "(SELECT SUM(wcd.pack_qty) FROM wms_carton wc LEFT JOIN wms_carton_detail wcd ON wcd.main_id = wc.id and wcd.is_deleted = false WHERE wc.packing_task_id = pt.ID AND wc.is_deleted = FALSE and wc.weighting_status = 'success') =" +
                        " (SELECT COALESCE(SUM(ptd.delivery_qty), 0) FROM packing_task_detail ptd WHERE ptd.main_id = pt.ID AND ptd.is_deleted = FALSE) ";
            }else if (PackingWeightStatusEnum.WEIGHTING.getCode().equals(value)){
                return "(SELECT SUM(wcd.pack_qty) FROM wms_carton wc LEFT JOIN wms_carton_detail wcd ON wcd.main_id = wc.id and wcd.is_deleted = false WHERE wc.packing_task_id = pt.ID AND wc.is_deleted = FALSE and wc.weighting_status = 'success') <" +
                        " (SELECT COALESCE(SUM(ptd.delivery_qty), 0) FROM packing_task_detail ptd WHERE ptd.main_id = pt.ID AND ptd.is_deleted = FALSE)" +
                        " and (SELECT SUM(wcd.pack_qty) FROM wms_carton wc LEFT JOIN wms_carton_detail wcd ON wcd.main_id = wc.id and wcd.is_deleted = false WHERE wc.packing_task_id = pt.ID AND wc.is_deleted = FALSE and wc.weighting_status = 'success') > 0";
            }else if (PackingWeightStatusEnum.UNWEIGHED.getCode().equals(value)){
                return "(SELECT SUM(wcd.pack_qty) FROM wms_carton wc LEFT JOIN wms_carton_detail wcd ON wcd.main_id = wc.id and wcd.is_deleted = false WHERE wc.packing_task_id = pt.ID AND wc.is_deleted = FALSE and wc.weighting_status = 'success') = 0";
            }
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

