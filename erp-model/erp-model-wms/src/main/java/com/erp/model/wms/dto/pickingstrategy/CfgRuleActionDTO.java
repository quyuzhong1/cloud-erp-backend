package com.erp.model.wms.dto.pickingstrategy;

import com.common.business.annotation.Dict;
import com.erp.model.wms.enums.OutStockModeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class CfgRuleActionDTO {
    private CfgRuleActionDTO() {
        throw new IllegalStateException("Utility CfgRuleActionDTO class");
    }
    @Getter
    @Setter
    public static class View {
        private String id;
        @Dict(queryFieldName = "id", tableName = "warehouse")
        private String warehouseId;
        @Dict(queryFieldName = "id", tableName = "warehouse_location")
        private String warehouseAreaId;
        @Dict(enumClass = OutStockModeEnum.class)
        private String outStockMode;
        private Integer index;
    }
    @Getter
    @Setter
    public static class Add {
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;
        @NotBlank(message = "库区不能为空")
        private String warehouseAreaId;
        @NotBlank(message = "出库方式不能为空")
        private String outStockMode;
    }
    @Getter
    @Setter
    public static class Update {
        private String id;
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;
        @NotBlank(message = "库区不能为空")
        private String warehouseAreaId;
        @NotBlank(message = "出库方式不能为空")
        private String outStockMode;
    }
}
