package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 调拨发货明细请求响应实体
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
*/
@Data
@NoArgsConstructor
public class DmpShipmentDetailDTO implements Serializable {




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
        * 主单id
        */
        private String mainId;

        /**
        * 平台sku
        */
        private String platformSku;

        /**
        * 申报返回数量
        */
        private Integer applyQty;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 签收数量
        */
        private Integer receiveQty;

        /**
        * 产品asin
        */
        private String asin;

        /**
        * 本地库存类型
        */
        private String stockType;

        /**
        * 本地库存sku
        */
        private String stockSku;

        /**
        * SKU图片
        */
        private String pictureUrl;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * FNSKU
        */
        private String fnSku;


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
        * 主单id
        */
        @NotBlank(message = "主单id不能为空")
        @Size(max = 19,message = "主单id最大长度不能超过19位")
        private String mainId;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 64,message = "平台sku最大长度不能超过64位")
        private String platformSku;

        /**
        * 申报返回数量
        */
        @NotNull(message = "申报返回数量不能为空")
        private Integer applyQty;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 签收数量
        */
        @NotNull(message = "签收数量不能为空")
        private Integer receiveQty;

        /**
        * 产品asin
        */
        @NotBlank(message = "产品asin不能为空")
        @Size(max = 100,message = "产品asin最大长度不能超过100位")
        private String asin;

        /**
        * 本地库存类型
        */
        @NotBlank(message = "本地库存类型不能为空")
        @Size(max = 64,message = "本地库存类型最大长度不能超过64位")
        private String stockType;

        /**
        * 本地库存sku
        */
        @NotBlank(message = "本地库存sku不能为空")
        @Size(max = 64,message = "本地库存sku最大长度不能超过64位")
        private String stockSku;

        /**
        * SKU图片
        */
        @NotBlank(message = "SKU图片不能为空")
        @Size(max = 255,message = "SKU图片最大长度不能超过255位")
        private String pictureUrl;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 64,message = "仓库id最大长度不能超过64位")
        private String warehouseId;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 100,message = "仓库名称最大长度不能超过100位")
        private String warehouseName;

        /**
        * FNSKU
        */
        @NotBlank(message = "FNSKU不能为空")
        @Size(max = 100,message = "FNSKU最大长度不能超过100位")
        private String fnSku;


    }


}