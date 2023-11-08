package com.erp.model.tms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-11-03
*/
@Data
@NoArgsConstructor
public class ShippingTemplateOtherCostDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 费用编码
        */
        private String dictCode;

        /**
        * 费用名称
        */
        private String dictName;

        /**
        * 计算方式
        */
        private String calculationMethod;

        /**
        * 计算方式单位
        */
        private String calculationUnit;

        /**
        * 费用设置值
        */
        private BigDecimal costSettingValue;

        /**
        * 数值设置json
        */
        private String extendJson;

        /**
        * 备注
        */
        private String remark;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 费用编码
        */
        @NotBlank(message = "费用编码不能为空")
        @Size(max = 32,message = "费用编码最大长度不能超过32位")
        private String dictCode;

        /**
        * 费用名称
        */
        @NotBlank(message = "费用名称不能为空")
        @Size(max = 32,message = "费用名称最大长度不能超过32位")
        private String dictName;

        /**
        * 计算方式
        */
        @NotBlank(message = "计算方式不能为空")
        @Size(max = 32,message = "计算方式最大长度不能超过32位")
        private String calculationMethod;

        /**
        * 计算方式单位
        */
        @NotBlank(message = "计算方式单位不能为空")
        @Size(max = 32,message = "计算方式单位最大长度不能超过32位")
        private String calculationUnit;

        /**
        * 费用设置值
        */
        @NotNull(message = "费用设置值不能为空")
        @Digits(integer = 12, fraction = 4, message = "费用设置值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal costSettingValue;

        /**
        * 数值设置json
        */
        @NotBlank(message = "数值设置json不能为空")
        @Size(max = 255,message = "数值设置json最大长度不能超过255位")
        private String extendJson;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


}