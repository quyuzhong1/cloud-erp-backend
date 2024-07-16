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
 * 中台销售订单收货人表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
*/
@Data
@NoArgsConstructor
public class DmpSoReceiverDTO implements Serializable {




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
        * 订单主表id
        */
        private String mainId;

        /**
        * 国家二字码
        */
        private String country;

        /**
        * 买家账号
        */
        private String buyerId;

        /**
        * 买家姓名
        */
        private String buyerName;

        /**
        * 收货人名称
        */
        private String receiverName;

        /**
        * 收货人电话
        */
        private String receiverTelNumber;

        /**
        * 邮编
        */
        private String postCode;

        /**
        * 省份
        */
        private String province;

        /**
        * 城市
        */
        private String city;

        /**
        * 所属区域
        */
        private String district;

        /**
        * 街道详细地址
        */
        private String fullAddress;

        /**
        * 买家地址1
        */
        private String mainStreet;

        /**
        * 买家地址2
        */
        private String secondStreet;

        /**
        * 买家电话1
        */
        private String mainPhone;

        /**
        * 买家电话2
        */
        private String secondPhone;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
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
        * 订单主表id
        */
        @NotBlank(message = "订单主表id不能为空")
        @Size(max = 19,message = "订单主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 25,message = "国家二字码最大长度不能超过25位")
        private String country;

        /**
        * 买家账号
        */
        @NotBlank(message = "买家账号不能为空")
        @Size(max = 64,message = "买家账号最大长度不能超过64位")
        private String buyerId;

        /**
        * 买家姓名
        */
        @NotBlank(message = "买家姓名不能为空")
        @Size(max = 25,message = "买家姓名最大长度不能超过25位")
        private String buyerName;

        /**
        * 收货人名称
        */
        @NotBlank(message = "收货人名称不能为空")
        @Size(max = 25,message = "收货人名称最大长度不能超过25位")
        private String receiverName;

        /**
        * 收货人电话
        */
        @NotBlank(message = "收货人电话不能为空")
        @Size(max = 64,message = "收货人电话最大长度不能超过64位")
        private String receiverTelNumber;

        /**
        * 邮编
        */
        @NotBlank(message = "邮编不能为空")
        @Size(max = 25,message = "邮编最大长度不能超过25位")
        private String postCode;

        /**
        * 省份
        */
        @NotBlank(message = "省份不能为空")
        @Size(max = 64,message = "省份最大长度不能超过64位")
        private String province;

        /**
        * 城市
        */
        @NotBlank(message = "城市不能为空")
        @Size(max = 64,message = "城市最大长度不能超过64位")
        private String city;

        /**
        * 所属区域
        */
        @NotBlank(message = "所属区域不能为空")
        @Size(max = 64,message = "所属区域最大长度不能超过64位")
        private String district;

        /**
        * 街道详细地址
        */
        @NotBlank(message = "街道详细地址不能为空")
        @Size(max = 255,message = "街道详细地址最大长度不能超过255位")
        private String fullAddress;

        /**
        * 买家地址1
        */
        @NotBlank(message = "买家地址1不能为空")
        @Size(max = 255,message = "买家地址1最大长度不能超过255位")
        private String mainStreet;

        /**
        * 买家地址2
        */
        @NotBlank(message = "买家地址2不能为空")
        @Size(max = 255,message = "买家地址2最大长度不能超过255位")
        private String secondStreet;

        /**
        * 买家电话1
        */
        @NotBlank(message = "买家电话1不能为空")
        @Size(max = 64,message = "买家电话1最大长度不能超过64位")
        private String mainPhone;

        /**
        * 买家电话2
        */
        @NotBlank(message = "买家电话2不能为空")
        @Size(max = 64,message = "买家电话2最大长度不能超过64位")
        private String secondPhone;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


    }


}