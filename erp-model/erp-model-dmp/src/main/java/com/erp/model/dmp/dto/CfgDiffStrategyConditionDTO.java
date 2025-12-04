package com.erp.model.dmp.dto;

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
 * 差异策略配置条件请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
*/
@Data
@NoArgsConstructor
public class CfgDiffStrategyConditionDTO implements Serializable {




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
        * 明细id
        */
        private String detailId;

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
        * 明细id
        */
        @NotBlank(message = "明细id不能为空")
        @Size(max = 50,message = "明细id最大长度不能超过50位")
        private String detailId;

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
        @NotBlank(message = "逻辑关系 or 和 and不能为空")
        @Size(max = 10,message = "逻辑关系 or 和 and最大长度不能超过10位")
        private String logic;

        /**
        * 顺序
        */
        @NotNull(message = "顺序不能为空")
        private Integer index;


    }


}