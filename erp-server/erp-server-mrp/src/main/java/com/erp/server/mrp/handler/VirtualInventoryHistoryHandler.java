package com.erp.server.mrp.handler;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.rpc.wms.feign.WmsVirtualWarehouseFeign;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class VirtualInventoryHistoryHandler extends AbstractQueryHandler {
    @Resource
    private WmsVirtualWarehouseFeign wmsVirtualWarehouseFeign;
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("virtual_warehouse_code".equals(field)) {
            // 查询关联仓库ID
            List<String> codeList = wmsVirtualWarehouseFeign.listWarehouseBySql(compareCodeSplicingValueSql);
            if (CollectionUtils.isEmpty(codeList)) {
                return getQueryEmptySql();
            }
            super.buildSplicingSQLDTO("virtual_warehouse_id", QueryConditionEnum.IN_LIST, codeList, QueryDataTypeEnum.STRING);
        }
        return null;
    }
}
