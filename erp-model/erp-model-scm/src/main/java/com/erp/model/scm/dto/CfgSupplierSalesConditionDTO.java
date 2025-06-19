package com.erp.model.scm.dto;

import com.common.business.annotation.Dict;
import com.common.business.enums.ServiceCodeNameEnum;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 销量设置条件明细请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-06-13
*/
@Data
@NoArgsConstructor
public class CfgSupplierSalesConditionDTO implements Serializable {




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
        * sales_setting_id
        */
        private String salesSettingId;

        /**
        * 仓库类型：virtualWarehouse=虚拟仓,physicalWarehouse=实体仓
        */
        private String warehouseType;

        /**
        * 左括号
        */
        private String leftBracket;

        /**
        * 条件的字段
        */
        private String field;

        /**
        * 比较符
        */
        private String compare;

        /**
        * 对应的值
        */
        private String value;

        /**
        * 右括号
        */
        private String rightBracket;

        /**
        * 逻辑关系 or 和 and
        */
        private String logic;

        /**
        * 顺序
        */
        private Integer index;

        /**
        * 值对应名称
        */
        private String name;

        /**
        * 条件所属规则来源
        */
        private String sourceType;

        /**
        * 值类型
        */
        private String valueType;


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
        * sales_setting_id
        */
        @NotBlank(message = "sales_setting_id不能为空")
        @Size(max = 19,message = "sales_setting_id最大长度不能超过19位")
        private String salesSettingId;

        /**
        * 仓库类型：virtualWarehouse=虚拟仓,physicalWarehouse=实体仓
        */
        @NotBlank(message = "仓库类型：virtualWarehouse=虚拟仓,physicalWarehouse=实体仓不能为空")
        @Size(max = 32,message = "仓库类型：virtualWarehouse=虚拟仓,physicalWarehouse=实体仓最大长度不能超过32位")
        private String warehouseType;

        /**
        * 左括号
        */
        @NotBlank(message = "左括号不能为空")
        @Size(max = 10,message = "左括号最大长度不能超过10位")
        private String leftBracket;

        /**
        * 条件的字段
        */
        @NotBlank(message = "条件的字段不能为空")
        @Size(max = 30,message = "条件的字段最大长度不能超过30位")
        private String field;

        /**
        * 比较符
        */
        @NotBlank(message = "比较符不能为空")
        @Size(max = 30,message = "比较符最大长度不能超过30位")
        private String compare;

        /**
        * 对应的值
        */
        @NotBlank(message = "对应的值不能为空")
        private String value;

        /**
        * 右括号
        */
        @NotBlank(message = "右括号不能为空")
        @Size(max = 10,message = "右括号最大长度不能超过10位")
        private String rightBracket;

        /**
        * 逻辑关系 or 和 and
        */
        private String logic;

        /**
        * 顺序
        */
        @NotNull(message = "顺序不能为空")
        private Integer index;

        /**
        * 值对应名称
        */
        @NotBlank(message = "值对应名称不能为空")
        private String name;

        /**
        * 条件所属规则来源
        */
        @NotBlank(message = "条件所属规则来源不能为空")
        @Size(max = 30,message = "条件所属规则来源最大长度不能超过30位")
        private String sourceType;

        /**
        * 值类型
        */
        private String valueType;


    }

    @Getter
    @Setter
    public static class View {
        private String id;
        private String leftBracket;
        private String field;
        @Dict(serviceCode = ServiceCodeNameEnum.OMS, tableName = "dict_rule_condition", queryFieldName = "key", returnFieldName = "value")
        private String compare;
        private String value;
        private String rightBracket;
        @Dict(serviceCode = ServiceCodeNameEnum.OMS, tableName = "dict_rule_condition", queryFieldName = "key", returnFieldName = "value")
        private String logic;
        private String name;
        private Integer index;
        private String valueType;

    }

    @Data
    @NoArgsConstructor
    public static class ConditionDTO {

        private String id;

        /**
         * sales_setting_id
         */
        private String salesSettingId;

        /**
         * 仓库类型：virtualWarehouse=虚拟仓,physicalWarehouse=实体仓
         */
        private String warehouseType;

        /**
         * 左括号
         */
        private String leftBracket;

        /**
         * 条件的字段
         */
        @NotBlank(message = "条件的字段不能为空")
        private String field;

        /**
         * 比较符
         */
        @NotBlank(message = "比较符不能为空")
        private String compare;

        /**
         * 对应的值
         */
        private String value;

        /**
         * 右括号
         */
        private String rightBracket;

        /**
         * 逻辑关系 or 和 and
         */
        private String logic;

        /**
         * 顺序
         */
        private Integer index;

        /**
         * 值对应名称
         */
        private String name;

        /**
         * 条件所属规则来源
         */
        private String sourceType;

        /**
         * 值类型
         */
        private String valueType;

        /**
         * 字段名称
         */
        private String fieldName;


    }



}