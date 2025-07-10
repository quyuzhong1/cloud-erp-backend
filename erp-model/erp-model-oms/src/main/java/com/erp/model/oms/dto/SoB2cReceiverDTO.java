package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * B2C销售订单买家信息表请求响应实体
 *
 * @author Will
 * @since 2023-08-18
*/
@Data
@NoArgsConstructor
public class SoB2cReceiverDTO implements Serializable {


    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO{

        /**
        * 主键id
        */
        private String  id;

        /**
        * 买家全名
        */
        private String name;


        /**
        * 国家名称
        */
        private String countryName;
        /**
         * 销售订单id
         */
        private String mainId;
        /**
         * 销售订单编码
         */
        private String soB2cCode;
        /**
         * 军区id
         */
        private String partitionId;
        /**
         * 军区编码
         */
        private String partitionCode;
        /**
         * 军区名称
         */
        private String partitionName;
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
//        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 买家登录id
        */
        @Size(max = 100,message = "买家登录id最大长度不能超过100位")
        private String loginId;

        /**
        * 买家id
        */
        @NotBlank(message = "买家id不能为空")
        @Size(max = 100,message = "买家id最大长度不能超过100位")
        private String customerId;

        /**
        * 邮箱
        */
        @Size(max = 100,message = "邮箱最大长度不能超过100位")
        private String email;

        /**
        * 买家电话
        */
        @Size(max = 32,message = "买家电话最大长度不能超过32位")
        private String telNumber;

        /**
        * 收货地址1
        */
        @Size(max = 255,message = "收货地址1最大长度不能超过255位")
        private String firstAddress;

        /**
        * 收货地址2
        */
        @Size(max = 255,message = "收货地址2最大长度不能超过255位")
        private String secondAddress;

        /**
         * 城市名称
         */
        @NotBlank(message = "城市名称不能为空")
        @Size(max = 50,message = "城市名称最大长度不能超过50位")
        private String cityName;

        /**
         * 国家 来源 http://172.16.100.11:3002/project/36/interface/api/13390
         */
       // @NotBlank(message = "国家不能为空")
        private String country;

        /**
         *省/州
         */
        private String provinceName;

        /**
         *区
         */
        private String districtName;



        /**
        * 收货人名称
        */
        @NotBlank(message = "收货人名称不能为空")
        @Size(max = 50,message = "收货人名称最大长度不能超过50位")
        private String receiverName;

        /**
        * 收货人电话
        */
        @Size(max = 32,message = "收货人电话最大长度不能超过32位")
        private String receiverTelNumber;

        /**
        * 邮编
        */
        @Size(max = 32,message = "邮编最大长度不能超过32位")
        private String postCode;

        /**
        * 街道详细地址
        */
        private String fullAddress;

        /**
         * 收件人税号
         */
        private String receiverTaxNo;


    }

    @Data
    @NoArgsConstructor
    public static class UpdateBaseDTO {
        /**
         * 收件人信息id
         */
        @NotBlank(message = "收件人信息id不能为空")
        private String id;
        /**
         * 销售订单id
         */
        @NotBlank(message = "销售订单id不能为空")
        private String mainId;
        /**
         * 销售订单编码
         */
        private String soB2cCode;
        //国家城市信息
        /**
         * 国家 来源 http://172.16.100.11:3002/project/36/interface/api/13390
         */
         @NotBlank(message = "国家不能为空")
        private String country;
        /**
         * 国家名称
         */
        private String countryName;
        /**
         *省/州
         */
        private String provinceName;
        /**
         * 城市名称
         */
        @NotBlank(message = "城市名称不能为空")
        @Size(max = 50,message = "城市名称最大长度不能超过50位")
        private String cityName;
        /**
         * 邮编
         */
        @Size(max = 32,message = "邮编最大长度不能超过32位")
        private String postCode;
        //收货人信息
        /**
         * 收货人名称
         */
        @NotBlank(message = "收货人名称不能为空")
        @Size(max = 50,message = "收货人名称最大长度不能超过50位")
        private String receiverName;

        /**
         * 收货人电话
         */
        @Size(max = 32,message = "收货人电话最大长度不能超过32位")
        private String receiverTelNumber;
        /**
         * 收件人税号
         */
        private String receiverTaxNo;
        //地址信息
        /**
         * 收货地址1
         */
        @Size(max = 255,message = "收货地址1最大长度不能超过255位")
        private String firstAddress;

        /**
         * 收货地址2
         */
        @Size(max = 255,message = "收货地址2最大长度不能超过255位")
        private String secondAddress;
        /**
         * 街道详细地址
         */
        private String fullAddress;
    }

    @Data
    @NoArgsConstructor
    public static class AddressDTO {
        /**
         * 销售订单id
         */
        @NotBlank(message = "销售订单id不能为空")
        private String soId;
        /**
         * 销售订单编码
         */
        private String soCode;
        /**
         * 发票地址
         */
        private String invoiceAddress;
    }
}