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
 * fba货件装箱信息请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-09-03
*/
@Data
@NoArgsConstructor
public class FbaShipmentPackingDTO implements Serializable {




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
        * 货件箱号
        */
        private String boxNo;

        /**
        * 平台产品id（ASIN）
        */
        private String asin;

        /**
        * 平台sku（msku）
        */
        private String msku;

        /**
        * FNSKU
        */
        private String fnSku;

        /**
        * ERP的SKU
        */
        private String skuNo;

        /**
        * ERP的skuId
        */
        private String skuId;

        /**
        * 装箱数量
        */
        private Integer qty;


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
        * 货件箱号
        */
        @NotBlank(message = "货件箱号不能为空")
        @Size(max = 64,message = "货件箱号最大长度不能超过64位")
        private String boxNo;

        /**
        * 平台产品id（ASIN）
        */
        @NotBlank(message = "平台产品id（ASIN）不能为空")
        @Size(max = 64,message = "平台产品id（ASIN）最大长度不能超过64位")
        private String asin;

        /**
        * 平台sku（msku）
        */
        @NotBlank(message = "平台sku（msku）不能为空")
        @Size(max = 64,message = "平台sku（msku）最大长度不能超过64位")
        private String msku;

        /**
        * FNSKU
        */
        @NotBlank(message = "FNSKU不能为空")
        @Size(max = 64,message = "FNSKU最大长度不能超过64位")
        private String fnSku;

        /**
        * ERP的skuId
        */
        @NotBlank(message = "ERP的skuId不能为空")
        @Size(max = 255,message = "ERP的skuId最大长度不能超过255位")
        private String skuId;

        /**
        * 装箱数量
        */
        @NotNull(message = "装箱数量不能为空")
        private Integer qty;


    }


}