package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 委外退料明细单请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
*/
@Data
@NoArgsConstructor
public class SubcontractReturnDetailDTO implements Serializable {




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
        * bom版本
        */
        private String bomVersion;

        /**
        * 用量
        */
        private Integer quantity;

        /**
        * 父sku用料数量
        */
        private Integer parentSkuUseQty;

        /**
        * 退料数量
        */
        private Integer returnQty;

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
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 仓位
        */
        private String warehouseLocation;

        /**
        * 主表id
        */
        private String mainId;

        /**
        * 对应金蝶详情id
        */
        private String kingdeeDetailId;

        /**
        * 委外订单明细id
        */
        private String subcontractOrderDetailId;


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
        * 父级skuId
        */
        @NotBlank(message = "父级skuId不能为空")
        @Size(max = 19,message = "父级skuId最大长度不能超过19位")
        private String parentSkuId;

        /**
        * 父级skuNo
        */
        @NotBlank(message = "父级skuNo不能为空")
        @Size(max = 64,message = "父级skuNo最大长度不能超过64位")
        private String parentSkuNo;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * bom版本
        */
        @NotBlank(message = "bom版本不能为空")
        @Size(max = 32,message = "bom版本最大长度不能超过32位")
        private String bomVersion;

        /**
        * 用量
        */
        @NotNull(message = "用量不能为空")
        private Integer quantity;

        /**
        * 父sku用料数量
        */
        @NotNull(message = "父sku用料数量不能为空")
        private Integer parentSkuUseQty;

        /**
        * 退料数量
        */
        @NotNull(message = "退料数量不能为空")
        private Integer returnQty;

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
        @Size(max = 64,message = "仓库名称最大长度不能超过64位")
        private String warehouseName;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 仓位
        */
        @NotBlank(message = "仓位不能为空")
        @Size(max = 32,message = "仓位最大长度不能超过32位")
        private String warehouseLocation;

        /**
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 对应金蝶详情id
        */
        @NotBlank(message = "对应金蝶详情id不能为空")
        @Size(max = 30,message = "对应金蝶详情id最大长度不能超过30位")
        private String kingdeeDetailId;

        /**
        * 委外订单明细id
        */
        @NotBlank(message = "委外订单明细id不能为空")
        @Size(max = 19,message = "委外订单明细id最大长度不能超过19位")
        private String subcontractOrderDetailId;


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