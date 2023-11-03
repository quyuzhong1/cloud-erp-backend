package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 物流地址表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@NoArgsConstructor
public class LogisticsAddressDTO implements Serializable {




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
        * 名称
        */
        private String name;

        /**
        * 类型
        */
        private String type;

        /**
        * 公司名
        */
        private String companyName;

        /**
        * 联系人
        */
        private String contact;

        /**
        * 邮箱
        */
        private String email;

        /**
        * 电话
        */
        private String telNumber;

        /**
        * 国家
        */
        private String country;

        /**
        * 省
        */
        private String province;

        /**
        * 城市
        */
        private String city;

        /**
        * 区
        */
        private String district ;

        /**
        * 详细地址1
        */
        private String addressFirst;

        /**
        * 详细地址2
        */
        private String addressSecond;

        /**
        * 邮编
        */
        private String zipCode;


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
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 50,message = "名称最大长度不能超过50位")
        private String name;

        /**
        * 类型
        */
        @NotBlank(message = "类型不能为空")
        private String type;

        /**
        * 公司名
        */
        @Size(max = 50,message = "公司名最大长度不能超过50位")
        private String companyName;

        /**
        * 联系人
        */
        @Size(max = 50,message = "联系人最大长度不能超过50位")
        private String contact;

        /**
        * 邮箱
        */
        @Size(max = 50,message = "邮箱最大长度不能超过50位")
        private String email;

        /**
        * 电话
        */
        @Size(max = 20,message = "电话最大长度不能超过20位")
        private String telNumber;

        /**
        * 国家
        */
        @NotBlank(message = "国家不能为空")
        private String country;

        /**
        * 省
        */
        @NotBlank(message = "省不能为空")
        private String province;

        /**
        * 城市
        */
        @NotBlank(message = "城市不能为空")
        private String city;

        /**
        * 区
        */
        @NotBlank(message = "区不能为空")
        private String district ;

        /**
        * 详细地址1
        */
        @NotBlank(message = "详细地址1不能为空")
        @Size(max = 255,message = "详细地址1最大长度不能超过250位")
        private String addressFirst;

        /**
        * 详细地址2
        */
        @Size(max = 255,message = "详细地址2最大长度不能超过250位")
        private String addressSecond;

        /**
        * 邮编
        */
        @Size(max = 20,message = "邮编最大长度不能超过20位")
        private String zipCode;


    }


}