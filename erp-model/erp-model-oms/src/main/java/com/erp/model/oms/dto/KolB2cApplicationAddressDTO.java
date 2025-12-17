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
 * B2C寄样申请单地址信息请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-12-04
*/
@Data
@NoArgsConstructor
public class KolB2cApplicationAddressDTO implements Serializable {




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
        * 主表ID
        */
        private String mainId;

        /**
        * 达人ID
        */
        private String partnerId;

        /**
        * 达人昵称
        */
        private String nickname;

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
        private String provinceId;
        private String province;

        /**
        * 城市
        */
        private String city;
        private String cityId;

        /**
        * 区域
        */
        private String district;
        private String districtId;

        /**
        * 详细地址
        */
        private String detailAddress;

        /**
        * 收货人
        */
        private String receiverName;

        /**
        * 收货电话
        */
        private String receiverPhone;

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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 主表ID
        */
        private String mainId;

        /**
        * 达人ID
        */
        @NotBlank(message = "达人不能为空")
        @Size(max = 19,message = "达人最大长度不能超过19位")
        private String partnerId;

        /**
        * 达人昵称
        */
        private String nickname;

        /**
        * 国家id
        */
        @NotBlank(message = "国家不能为空")
        @Size(max = 19,message = "国家最大长度不能超过19位")
        private String countryId;

        /**
        * 国家
        */
        private String countryName;

        /**
        * 省/州
        */
        @NotBlank(message = "省/州不能为空")
        @Size(max = 100,message = "省/州最大长度不能超过19位")
        private String provinceId;
        private String province;

        /**
        * 城市
        */
        @NotBlank(message = "城市不能为空")
        @Size(max = 100,message = "城市最大长度不能超过19位")
        private String cityId;
        private String city;

        /**
        * 区域
        */
        @Size(max = 100,message = "区域最大长度不能超过19位")
        private String districtId;
        private String district;

        /**
        * 详细地址
        */
        @NotBlank(message = "详细地址不能为空")
        @Size(max = 500,message = "详细地址最大长度不能超过500位")
        private String detailAddress;

        /**
        * 收货人
        */
        @NotBlank(message = "收货人不能为空")
        @Size(max = 100,message = "收货人最大长度不能超过100位")
        private String receiverName;

        /**
        * 收货电话
        */
        @NotBlank(message = "收货电话不能为空")
        @Size(max = 20,message = "收货电话最大长度不能超过20位")
        private String receiverPhone;

        /**
        * 邮编
        */
        @Size(max = 20,message = "邮编最大长度不能超过20位")
        private String zipCode;


    }


}