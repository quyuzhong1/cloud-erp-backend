package com.erp.model.tms.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 运费模板请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-11-03
*/
@Data
@NoArgsConstructor
public class ShippingTemplateDTO implements Serializable {




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
        * 模板名称
        */
        private String name;

        /**
        * 计费方式
        */
        private String billingMethod;

        /**
        * 币别
        */
        private String currency;

        /**
        * 重量单位
        */
        private String weightUnit;

        /**
        * 价格进制
        */
        private String priceBinary;

        /**
        * 材积设置
        */
        private String volumeSetting;

        /**
        * 生效日期
        */
        private LocalDate effectiveDate;

        /**
        * 失效日期
        */
        private LocalDate expireDate;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 模板类型
        */
        private String type;


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
        * 模板名称
        */
        @NotBlank(message = "模板名称不能为空")
        @Size(max = 64,message = "模板名称最大长度不能超过64位")
        private String name;

        /**
        * 计费方式
        */
        @NotBlank(message = "计费方式不能为空")
        @Size(max = 32,message = "计费方式最大长度不能超过32位")
        private String billingMethod;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 16,message = "币别最大长度不能超过16位")
        private String currency;

        /**
        * 重量单位
        */
        @NotBlank(message = "重量单位不能为空")
        @Size(max = 16,message = "重量单位最大长度不能超过16位")
        private String weightUnit;

        /**
        * 价格进制
        */
        @NotBlank(message = "价格进制不能为空")
        @Size(max = 32,message = "价格进制最大长度不能超过32位")
        private String priceBinary;

        /**
        * 材积设置
        */
        @NotBlank(message = "材积设置不能为空")
        @Size(max = 64,message = "材积设置最大长度不能超过64位")
        private String volumeSetting;

        /**
        * 生效日期
        */
        private LocalDate effectiveDate;

        /**
        * 失效日期
        */
        private LocalDate expireDate;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 模板类型
        */
        @NotBlank(message = "模板类型不能为空")
        @Size(max = 32,message = "模板类型最大长度不能超过32位")
        private String type;


    }


}