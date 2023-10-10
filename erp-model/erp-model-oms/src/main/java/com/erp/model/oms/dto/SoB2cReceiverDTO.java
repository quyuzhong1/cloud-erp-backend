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
        * 城市名称
        */
        private String cityName;

        /**
        * 国家名称
        */
        private String countryName;
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
        * 买家登录id
        */
        @Size(max = 100,message = "买家登录id最大长度不能超过100位")
        private String loginId;

        /**
        * 买家id
        */
        @NotBlank(message = "买家id不能为空")
        @Size(max = 19,message = "买家id最大长度不能超过19位")
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
         * 国家名称
         */
        @NotBlank(message = "国家名称不能为空")
        @Size(max = 50,message = "国家名称最大长度不能超过50位")
        private String countryName;

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


    }


}