package com.erp.model.wms.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * FBA货件签收信息请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
*/
@Data
@NoArgsConstructor
public class FbaShipmentReceiveDTO implements Serializable {




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
        * FBA拣货明细表id
        */
        private String detailId;

        /**
        * 亚马逊FBA货件单号
        */
        private String fbaShipmentId;

        /**
        * 平台sku
        */
        private String asin;

        /**
        * 卖家sku
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
        * 产品名称
        */
        private String productName;

        /**
        * 申报数量
        */
        private Integer declareQty;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 收发差异
        */
        private Integer diffQty;

        /**
        * 是否组合品
        */
        private Boolean isCombo;

        /**
        * 收货数量
        */
        private Integer receiveQty;

        /**
        * 最新签收日期
        */
        private LocalDateTime receiveDate;


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
        * FBA拣货明细表id
        */
        @NotBlank(message = "FBA拣货明细表id不能为空")
        @Size(max = 19,message = "FBA拣货明细表id最大长度不能超过19位")
        private String detailId;

        /**
        * 亚马逊FBA货件单号
        */
        @NotBlank(message = "亚马逊FBA货件单号不能为空")
        @Size(max = 19,message = "亚马逊FBA货件单号最大长度不能超过19位")
        private String fbaShipmentId;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 64,message = "平台sku最大长度不能超过64位")
        private String asin;

        /**
        * 卖家sku
        */
        @NotBlank(message = "卖家sku不能为空")
        @Size(max = 64,message = "卖家sku最大长度不能超过64位")
        private String msku;

        /**
        * FNSKU
        */
        @NotBlank(message = "FNSKU不能为空")
        @Size(max = 64,message = "FNSKU最大长度不能超过64位")
        private String fnSku;

        /**
        * ERP的SKU
        */
        @NotBlank(message = "ERP的SKU不能为空")
        @Size(max = 64,message = "ERP的SKU最大长度不能超过64位")
        private String skuNo;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 500,message = "产品名称最大长度不能超过500位")
        private String productName;

        /**
        * 申报数量
        */
        @NotNull(message = "申报数量不能为空")
        private Integer declareQty;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 收发差异
        */
        @NotNull(message = "收发差异不能为空")
        private Integer diffQty;

        /**
        * 是否组合品
        */
        @NotNull(message = "是否组合品不能为空")
        private Boolean isCombo;

        /**
        * 收货数量
        */
        @NotNull(message = "收货数量不能为空")
        private Integer receiveQty;

        /**
        * 最新签收日期
        */
        private LocalDateTime receiveDate;


    }


}