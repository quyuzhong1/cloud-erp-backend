package com.erp.model.wms.dto.pickingstrategy;

import com.common.business.annotation.Dict;
import com.erp.model.wms.enums.OutStockModeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CfgRuleActionDTO {
    @Dict(queryFieldName = "id", tableName = "warehouse")
    private String warehouseId;
    @Dict(queryFieldName = "id", tableName = "warehouse_location")
    private String warehouseAreaId;
    @Dict(enumClass = OutStockModeEnum.class)
    private String outStockMode;
}
