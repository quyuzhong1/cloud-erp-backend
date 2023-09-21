package com.erp.model.bi.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/5 10:08
 */
@Data
@NoArgsConstructor
public class BiSalesMonitoringSearchDTO extends PermissionsDTO {

    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class ParamDTO extends PermissionsDTO{

        /**
         * 考核维度（销售额 salesAmount、销量 salesQty）
         */
        @NotBlank(message = "考核维度不能为空")
        private String metrics;

        /**
         * 搜索类型（二级部门 dept、人员 user、店铺 shop,、品类 category、SKU sku、平台 platform、国家 country）
         */
        @NotBlank(message = "搜索类型不能为空")
        private String searchType;
    }

}
