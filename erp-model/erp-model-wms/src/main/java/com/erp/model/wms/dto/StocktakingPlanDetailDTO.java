package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 盘点计划明细表请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
*/
@Data
@NoArgsConstructor
public class StocktakingPlanDetailDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 主表id
        */
        private String mainId;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
         * 仓库类型
         */
        private String warehouseTypeName;

        /**
        * 仓库区域
        */
        private String warehouseArea;

        /**
        * 仓位
        */
        private String warehouseLocation;
        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
        * skuid
        */
        private String skuId;

        /**
        * sku编码
        */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
        * 组织id
        */
        private String orgId;

        /**
        * 组织名称
        */
        private String orgName;

        /**
         * 可用库存
         */
        private Integer usableQty;

        /**
         * 冻结库存
         */
        private Integer frozenQty;

        /**
         * 库位id
         */
        private String warehouseLocationId;

        /**
         * 库存id
         */
        private String inventoryId;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 200,message = "仓库名称最大长度不能超过200位")
        private String warehouseName;

        /**
        * 仓库区域
        */
        @NotBlank(message = "仓库区域不能为空")
        @Size(max = 32,message = "仓库区域最大长度不能超过32位")
        private String warehouseArea;

        /**
        * 仓位
        */
        @NotBlank(message = "仓位不能为空")
        @Size(max = 32,message = "仓位最大长度不能超过32位")
        private String warehouseLocation;

        /**
        * skuid
        */
        @NotBlank(message = "skuid不能为空")
        @Size(max = 64,message = "skuid最大长度不能超过64位")
        private String skuId;

        /**
        * sku编码
        */
        @NotBlank(message = "sku编码不能为空")
        @Size(max = 64,message = "sku编码最大长度不能超过64位")
        private String skuNo;

        /**
        * 组织id
        */
        @NotBlank(message = "组织id不能为空")
        @Size(max = 19,message = "组织id最大长度不能超过19位")
        private String orgId;

        /**
        * 组织名称
        */
        @NotBlank(message = "组织名称不能为空")
        @Size(max = 100,message = "组织名称最大长度不能超过100位")
        private String orgName;


    }


}