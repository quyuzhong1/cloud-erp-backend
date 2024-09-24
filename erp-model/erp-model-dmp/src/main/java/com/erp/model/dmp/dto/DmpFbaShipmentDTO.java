package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
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
 * FBA货件表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-09-07
*/
@Data
@NoArgsConstructor
public class DmpFbaShipmentDTO implements Serializable {




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

        /**
        * 第三方唯一编码
        */
        private String fbaShipmentId;

        /**
        * 输入任务ID
        */
        private String inputTaskId;

        /**
        * 亚马逊账号代号
        */
        private String platformShopCode;

        /**
        * 任务转换ID
        */
        private String convertId;

        /**
        * 店铺ID
        */
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        private String dataEncrypt;


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

        /**
        * 第三方唯一编码
        */
        @NotBlank(message = "第三方唯一编码不能为空")
        @Size(max = 255,message = "第三方唯一编码最大长度不能超过255位")
        private String fbaShipmentId;

        /**
        * 输入任务ID
        */
        @NotBlank(message = "输入任务ID不能为空")
        @Size(max = 19,message = "输入任务ID最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 亚马逊账号代号
        */
        @NotBlank(message = "亚马逊账号代号不能为空")
        @Size(max = 100,message = "亚马逊账号代号最大长度不能超过100位")
        private String platformShopCode;

        /**
        * 任务转换ID
        */
        @NotBlank(message = "任务转换ID不能为空")
        @Size(max = 19,message = "任务转换ID最大长度不能超过19位")
        private String convertId;

        /**
        * 店铺ID
        */
        @NotBlank(message = "店铺ID不能为空")
        @Size(max = 19,message = "店铺ID最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        @NotBlank(message = "任务来源唯一加密代号不能为空")
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        @NotBlank(message = "任务数据加密代号不能为空")
        private String dataEncrypt;


    }


}