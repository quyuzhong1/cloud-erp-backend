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

