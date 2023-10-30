package com.erp.model.wms.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * FBI货件表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@NoArgsConstructor
public class FbaShipmentDTO implements Serializable {




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
        * 单据编号
        */
        private String code;

        /**
        * FBA货件名称
        */
        private String name;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 国家二字码
        */
        private String countryId;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 平台物流中心
        */
        private String fulfillmentCenter;

        /**
        * 发货状态
        */
        private String deliveryStatus;

        /**
        * 平台货件状态
        */
        private String platformShipmentStatus;

        /**
        * 创建时间（拉取数据的日期）
        */
        private LocalDateTime shipmentCreateTime;

        /**
        * 签收时间（拉取签收数据的日期）
        */
        private LocalDateTime shipmentReceiveTime;

        /**
        * 标签类型（NO_LABEL、SELLER_LABEL、AMAZON_LABEL）
        */
        private String labelType;

        /**
        * 包装类型（混装商品、原厂包装商品）
        */
        private String packType;

        /**
        * 发货地址
        */
        private String deliveryFromAddress;

        /**
        * 配送地址
        */
        private String deliveryToAddress;


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
        * FBA货件名称
        */
        @NotBlank(message = "FBA货件名称不能为空")
        @Size(max = 255,message = "FBA货件名称最大长度不能超过255位")
        private String name;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 64,message = "店铺id最大长度不能超过64位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255,message = "店铺名称最大长度不能超过255位")
        private String shopName;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 10,message = "国家二字码最大长度不能超过10位")
        private String countryId;

        /**
        * 国家名称
        */
        @NotBlank(message = "国家名称不能为空")
        @Size(max = 64,message = "国家名称最大长度不能超过64位")
        private String countryName;

        /**
        * 平台物流中心
        */
        @NotBlank(message = "平台物流中心不能为空")
        @Size(max = 64,message = "平台物流中心最大长度不能超过64位")
        private String fulfillmentCenter;

        /**
        * 发货状态
        */
        @NotBlank(message = "发货状态不能为空")
        @Size(max = 255,message = "发货状态最大长度不能超过255位")
        private String deliveryStatus;

        /**
        * 平台货件状态
        */
        @NotBlank(message = "平台货件状态不能为空")
        @Size(max = 255,message = "平台货件状态最大长度不能超过255位")
        private String platformShipmentStatus;

        /**
        * 创建时间（拉取数据的日期）
        */
        private LocalDateTime shipmentCreateTime;

        /**
        * 签收时间（拉取签收数据的日期）
        */
        private LocalDateTime shipmentReceiveTime;

        /**
        * 标签类型（NO_LABEL、SELLER_LABEL、AMAZON_LABEL）
        */
        @NotBlank(message = "标签类型（NO_LABEL、SELLER_LABEL、AMAZON_LABEL）不能为空")
        @Size(max = 64,message = "标签类型（NO_LABEL、SELLER_LABEL、AMAZON_LABEL）最大长度不能超过64位")
        private String labelType;

        /**
        * 包装类型（混装商品、原厂包装商品）
        */
        @NotBlank(message = "包装类型（混装商品、原厂包装商品）不能为空")
        @Size(max = 64,message = "包装类型（混装商品、原厂包装商品）最大长度不能超过64位")
        private String packType;

        /**
        * 发货地址
        */
        @NotBlank(message = "发货地址不能为空")
        private String deliveryFromAddress;

        /**
        * 配送地址
        */
        @NotBlank(message = "配送地址不能为空")
        private String deliveryToAddress;


    }

    /**
     * 拉取货件信息DTO
     */
    @Data
    @NoArgsConstructor
    public static class pullShipmentDTO {
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 货件单号集合
         */
        private List<String> shipmentCodeList;
    }

    /**
     * 列表查询参数
     */
    @Data
    @NoArgsConstructor
    public class PagingParamDTO {
        /**
         * 单据编号
         */
        private String code;

        /**
         * sku编号
         */
        private List<String> skuNoList;

        /**
         * 店铺id
         */
        private List<String> shopIdList;

        /**
         * 国家二字码
         */
        private List<String> countryIdList;

        /**
         * 平台物流中心
         */
        private String fulfillmentCenter;

        /**
         * 发货状态
         */
        private List<String> deliveryStatusList;

        /**
         * 平台货件状态
         */
        private List<String> platformShipmentStatusList;

        /**
         * 平台sku
         */
        private String asin;

        /**
         * FNSKU
         */
        private String fnSku;

        /**
         * 卖家sku
         */
        private String mSku;

        /**
         * 收发差异（大于0，小于0，等于0，不等于0）
         */
        private String diffRule;

        /**
         * 创建时间（拉取数据的日期）
         */
        private List<String> shipmentCreateTimeList;

        /**
         * 签收时间（拉取签收数据的日期）
         */
        private List<String> shipmentReceiveTimeList;

    }
}