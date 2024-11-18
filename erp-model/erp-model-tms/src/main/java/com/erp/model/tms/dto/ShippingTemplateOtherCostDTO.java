package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
        * 计算方式 （字典 discountRate折扣费率,fuelSurchargeRate燃油附加费率,side边长,vote票）
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
         * 计算方式选值
         */
        private List<ShippingTemplateCostSettingDTO.ViewDTO> costSettingList;

        /**
         * 数值设置json实体
         */
        private ExtendJsonDTO.CommonDTO extendJsonDto;

        /**
         * 数值设置json
         */
        private String extendJson;

        /**
        * 备注
        */
        private String remark;

        /**
         * 计算方式选值
         */
        private List<String> settingList;

        /**
         * 计算方式名称选值
         */
        private List<String> settingNameList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 计算方式选值
         */
        private List<String> settingList;
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

        /**
         * 计算方式选值
         */
        private List<String> settingList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 费用编码
        */
        @Size(max = 32,message = "费用编码最大长度不能超过32位")
        private String dictCode;

        /**
        * 计算方式
        */
        @Size(max = 32,message = "计算方式最大长度不能超过32位")
        private String calculationMethod;

        /**
        * 计算方式单位
        */
        @Size(max = 32,message = "计算方式单位最大长度不能超过32位")
        private String calculationUnit;

        /**
        * 费用设置值
        */
        @Digits(integer = 12, fraction = 4, message = "费用设置值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal costSettingValue;

        /**
        * 数值设置json
        */
        private ExtendJsonDTO.CommonDTO extendJsonDto;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


}