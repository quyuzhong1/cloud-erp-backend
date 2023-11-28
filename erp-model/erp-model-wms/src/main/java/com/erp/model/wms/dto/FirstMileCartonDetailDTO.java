package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 发货单箱子信息表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class FirstMileCartonDetailDTO implements Serializable {

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
        * first_mile_carton表id
        */
        private String cartonId;

        /**
        * 产品id
        */
        private String skuId;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
        * 装箱数量
        */
        private Integer packQty;
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
        * first_mile_carton表id
        */
        private String cartonId;

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
        * 装箱数量
        */
        @NotNull(message = "装箱数量不能为空")
        private Integer packQty;
    }

    /**
     * 装箱清单产品信息
     */
    @Data
    @NoArgsConstructor
    public static class ListPackingDetailDTO {
        /**
         * 箱号
         */
        private String boxNo;
        /**
         * 箱子包装尺寸
         */
        private String boxSize;
        /**
         * 箱子包装重量
         */
        private String packageWeight;
        /**
         * 装箱SKU
         * 例：（sku*qty+sku*qty+...）
         */
        private String boxDesc;
    }

}