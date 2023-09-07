package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 条件配置表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
*/
@Data
@NoArgsConstructor
public class CfConditionDTO implements Serializable {




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
        * 条件字段 对应dict_rule_condition key
        */
        private String conditionField;

        /**
        * 逻辑关系 对应 dict_rule_condition key 多个逗号分割
        */
        private String logic;

        /**
        * 空间 如时间戳 输入框之类
        */
        private String controls;

        /**
        * 对应api url
        */
        private String apiUrl;


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
        * 条件字段 对应dict_rule_condition key
        */
       // @NotBlank(message = "条件字段 对应dict_rule_condition key不能为空")
        @Size(max = 30,message = "条件字段 对应dict_rule_condition key最大长度不能超过30位")
        private String conditionField;

        /**
         * 字段名
         */
        private String conditionFieldName;

        /**
        * 逻辑关系 对应 dict_rule_condition key 多个逗号分割
        */
        @NotBlank(message = "逻辑关系 对应 dict_rule_condition key 多个逗号分割不能为空")
        @Size(max = 100,message = "逻辑关系 对应 dict_rule_condition key 多个逗号分割最大长度不能超过100位")
        private String logic;

        /**
         * 逻辑关系名
         */
        private String logicName;

        /**
        * 空间 如时间戳 输入框之类
        */
        @NotBlank(message = "空间 如时间戳 输入框之类不能为空")
        @Size(max = 50,message = "空间 如时间戳 输入框之类最大长度不能超过50位")
        private String controls;

        /**
        * 对应api url
        */
        @Size(max = 255,message = "对应api url最大长度不能超过255位")
        private String apiUrl;


    }


}