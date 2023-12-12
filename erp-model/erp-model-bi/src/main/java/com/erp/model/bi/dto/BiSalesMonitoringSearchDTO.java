package com.erp.model.bi.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
    public static class ParamDTO extends BiFilterDTO{

        /**
         * 考核维度（销售额 salesAmount、销量 salesQty）
         */
        @NotBlank(message = "考核维度不能为空")
        private String metrics;

        /**
         * 搜索类型（二级部门 dept、人员 user、店铺 shop,、品类 category、SKU sku、平台 platform、国家 country）
         */
        @NotNull(message = "搜索类型不能为空")
        private Integer searchType;

        /**
         * 数据权限
         * 1-自己,2-部门,3-全部
         */
        private Integer dataScope;

        /**
         * 拼接的sql
         */
        private String permissionSql;
    }

}
