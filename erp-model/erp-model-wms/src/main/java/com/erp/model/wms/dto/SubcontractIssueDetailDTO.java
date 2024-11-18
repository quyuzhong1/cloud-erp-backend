package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 委外发料明细单请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-01-08
*/
@Data
@NoArgsConstructor
public class SubcontractIssueDetailDTO implements Serializable {




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
        * 父级skuId
        */
        private String parentSkuId;

        /**
        * 父级skuNo
        */
        private String parentSkuNo;

        /**
        * skuId
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
        * bom版本
        */
        private String bomVersion;

        /**
        * 用量
        */
        private Integer quantity;

        /**
        * 领料数量
        */
        private Integer receiveQty;

        /**
         * 可用库存
         */
        private Integer curInventoryQty;

        /**
         * 已发料数量
         */
        private Integer hasIssueQty;

        /**
        * 发料数量
        */
        private Integer issueQty;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 备注
        */
        private String remark;

        /**
         * 委外订单明细id
         */
        private String subcontractOrderDetailId;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
         * 来源父级明细id
         */
        private String parentSourceDetailId;

        /**
        * 仓位
        */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;
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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 发料数量
        */
        @NotNull(message = "发料数量不能为空")
        private Integer issueQty;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 来源明细id
        */
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
         * 委外订单明细id
         */
        @NotBlank(message = "委外订单明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String subcontractOrderDetailId;

        /**
        * 仓位
        */
        @Size(max = 32,message = "仓位最大长度不能超过32位")
        private String warehouseLocation;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;
    }


    @Data
    @NoArgsConstructor
    public static class ListSourceDetailDTO {

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 委外订单id
         */
        private String subcontractOrderId;

        /**
         * 父级来源明细id
         */
        private String parentSourceDetailId;

        /**
         * 委外订单明细id
         */
        private String subcontractOrderDetailId;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 父级skuId
         */
        private String parentSkuId;

        /**
         * 父级skuNo
         */
        private String parentSkuNo;

        /**
         * skuId
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
         * bom版本
         */
        private String bomVersion;

        /**
         * bom用量
         */
        private Integer quantity;

        /**
         * 领料数量
         */
        private Integer receiveQty;

        /**
         * 可用库存
         */
        private Integer curInventoryQty;

        /**
         * 已发料数量
         */
        private Integer hasIssueQty;

        /**
         * 发料数量
         */
        private Integer issueQty;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;
    }
}