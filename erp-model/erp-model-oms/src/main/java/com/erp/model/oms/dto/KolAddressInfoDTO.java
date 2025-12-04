package com.erp.model.oms.dto;

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
 * 达人地址信息请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-12-02
*/
@Data
@NoArgsConstructor
public class KolAddressInfoDTO implements Serializable {




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
        * main_id
        */
        private String mainId;

        /**
        * 国家id
        */
        private String countryId;

        /**
        * 国家
        */
        private String countryName;

        /**
        * 省/州
        */
        private String province;
        /**
        * 城市
        */
        private String city;

        /**
        * 区域
        */
        private String district;

        /**
        * 详细地址
        */
        private String detailAddress;

        /**
        * 联系人
        */
        private String contactPerson;

        /**
        * 邮箱
        */
        private String email;

        /**
        * 联系电话
        */
        private String phone;

        /**
        * 邮编
        */
        private String zipCode;

        /**
        * 是否默认地址
        */
        private Boolean isDefault;

        /**
        * 地址备注
        */
        private String remark;

        /**
        * 是否启用
        */
        private Boolean disabled;


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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * main_id
        */
        private String mainId;

        /**
        * 国家id
        */
        @NotBlank(message = "国家id不能为空")
        @Size(max = 19,message = "国家id最大长度不能超过19位")
            private String countryId;

        /**
        * 国家
        */
        private String countryName;

        /**
        * 省/州id
        */
        private String province;

        /**
        * 城市
        */
        @NotBlank(message = "城市不能为空")
        @Size(max = 100,message = "城市最大长度不能超过100位")
        private String city;


        /**
        * 区域
        */
        private String district;

        /**
        * 详细地址
        */
        @NotBlank(message = "详细地址不能为空")
        @Size(max = 500,message = "详细地址最大长度不能超过500位")
        private String detailAddress;

        /**
        * 联系人
        */
        @NotBlank(message = "联系人不能为空")
        @Size(max = 50,message = "联系人最大长度不能超过50位")
        private String contactPerson;

        /**
        * 邮箱
        */
        @NotBlank(message = "邮箱不能为空")
        @Size(max = 100,message = "邮箱最大长度不能超过100位")
        private String email;

        /**
        * 联系电话
        */
        @NotBlank(message = "联系电话不能为空")
        @Size(max = 20,message = "联系电话最大长度不能超过20位")
        private String phone;

        /**
        * 邮编
        */
        @Size(max = 20,message = "邮编最大长度不能超过20位")
        private String zipCode;

        /**
        * 是否默认地址
        */
        private Boolean isDefault;

        /**
        * 地址备注
        */
        @Size(max = 500,message = "地址备注最大长度不能超过500位")
        private String remark;

        /**
        * 是否启用
        */
        private Boolean disabled;


    }


}