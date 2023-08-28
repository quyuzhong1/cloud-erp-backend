package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 规则条件表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
*/
@Data
@NoArgsConstructor
public class RuleConditionDTO implements Serializable {




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
        * 左括号
        */
        private String leftBracket;

        /**
        * 条件的字段
        */
        private String field;

        /**
        * 逻辑关系
        */
        private String dictCompare;

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
        * 逻辑关系
        */
        @NotBlank(message = "逻辑关系不能为空")
        @Size(max = 30,message = "逻辑关系最大长度不能超过30位")
        private String dictCompare;

        /**
        * 对应的值
        */
        @NotBlank(message = "对应的值不能为空")
        @Size(max = 30,message = "对应的值最大长度不能超过30位")
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
        @NotBlank(message = "逻辑关系 or 和 and不能为空")
        @Size(max = 10,message = "逻辑关系 or 和 and最大长度不能超过10位")
        private String logic;


    }


}