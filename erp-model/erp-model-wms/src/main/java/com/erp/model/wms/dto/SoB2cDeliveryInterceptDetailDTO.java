package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * b2c发货拦截单详情请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
*/
@Data
@NoArgsConstructor
public class SoB2cDeliveryInterceptDetailDTO implements Serializable {




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
        * 产品id
        */
        private String skuId;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

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
        * 来源详情id
        */
        private String sourceDetailId;


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
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 19,message = "产品id最大长度不能超过19位")
        private String skuId;

        /**
        * 产品编号
        */
        @NotBlank(message = "产品编号不能为空")
        @Size(max = 19,message = "产品编号最大长度不能超过19位")
        private String skuNo;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

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
        @Size(max = 255,message = "仓库名称最大长度不能超过255位")
        private String warehouseName;

        /**
        * 仓位
        */
        private String warehouseLocation;

        /**
        * 来源详情id
        */
        @NotBlank(message = "来源详情id不能为空")
        @Size(max = 19,message = "来源详情id最大长度不能超过19位")
        private String sourceDetailId;


    }


}