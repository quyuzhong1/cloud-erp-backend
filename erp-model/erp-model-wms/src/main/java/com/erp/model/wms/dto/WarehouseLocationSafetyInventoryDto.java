package com.erp.model.wms.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 仓位安全库存
 * @date 2024-06-21
 * @author tanmujin
 */
@Data
public class WarehouseLocationSafetyInventoryDto implements Serializable {

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SearchParamDto extends SortDTO {
        private String skuNo;

        /**
         * 仓位编码
         */
        private String warehouseLocationCode;

        /**
         * 仓库ID
         */
        private String warehouseId;

        /**
         * 库区编码
         */
        private String warehouseAreaCode;

        /**
         * 库区类型
         */
        private String warehouseAreaType;

        /**
         * 不显示0库存
         */
        private String hideZeroInventory;

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class exportParamDto extends SearchParamDto{
        private List<String> ids;
    }

    @Data
    public static class ViewDto{
        private String id;

        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 仓位编码
         */
        private String warehouseLocation;

        /**
         * 仓库ID
         */
        private String warehouseId;

        /**
         * 所属仓库
         */
        private String warehouseName;

        /**
         * 所属库区编码
         */
        private String warehouseArea;

        /**
         * 所属库区名称
         */
        private String warehouseAreaName;

        /**
         * 库区类型
         */
        private String warehouseAreaType;

        /**
         * 安全库存
         */
        private Integer safetyQty;

        /**
         * 补货上限量
         */
        private Integer maxQty;
    }

    @Data
    public static class UpdateParamDto{
        private String id;

        /**
         * 安全库存
         */
        private Integer safetyQty;

        /**
         * 补货上限量
         */
        private Integer maxQty;
    }

    @Data
    public static class importExcelDto implements Serializable{
        @ExcelProperty(value = "*SKU", index = 0)
        @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 64)
        private String skuNo;

        @ExcelProperty(value = "产品名称", index = 1)
        @FieldValid(fieldName = "产品名称", maxLength = 64)
        private String productName;

        @ExcelProperty(value = "*仓位编码", index = 2)
        @FieldValid(fieldName = "仓位编码", isNotBlank = true, maxLength = 64)
        private String warehouseLocation;

        @ExcelProperty(value = "*所属仓库", index = 3)
        @FieldValid(fieldName = "所属仓库", isNotBlank = true, maxLength = 64)
        private String warehouseName;

        @ExcelProperty(value = "所属库区", index = 4)
        @FieldValid(fieldName = "所属库区", maxLength = 64)
        private String warehouseAreaName;

        @ExcelProperty(value = "库区类型", index = 5)
        @FieldValid(fieldName = "库区类型", maxLength = 20)
        private String warehouseAreaType;

        @ExcelProperty(value = "安全库存", index = 6)
        @FieldValid(fieldName = "安全库存", maxLength = 20)
        private Integer safetyQty;

        @ExcelProperty(value = "补货上限量", index = 7)
        @FieldValid(fieldName = "补货上限量", maxLength = 20)
        private Integer maxQty;

        private String errorInfo;
    }

    @Data
    public static class ExportExcelDto implements Serializable{

    }
}
