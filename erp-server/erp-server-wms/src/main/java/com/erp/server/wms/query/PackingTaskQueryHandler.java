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
        /**
         * 默认生成为待装箱，根据装箱数量更新，装箱数量=0
         * 装箱中：装箱数量大于0且小于发货数量
         * 已装箱：装箱数量等于发货数量
         */
        if (PackingTaskStatusEnum.UNPACKED.getCode().equals(value)){
            return "pt.packing_status = unpacked";
        }else if (PackingTaskStatusEnum.PACKING.getCode().equals(value)){
            return "pt.packing_status = packing";
        }else if (PackingTaskStatusEnum.PACKED.getCode().equals(value)){
            return "pt.packing_status = packed";
        }
        return super.getSplicingSQL();
    }
}

