package com.erp.model.oms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * sku对照表匹配规则请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2023-12-21
*/
@Data
@NoArgsConstructor
public class SkuMappingRuleDTO implements Serializable {


    /**
     * 视图DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewDTO {
        /**
         * id
         */
        private String id;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 规则类型
         */
        private String ruleType;

        /**
         * 状态
         */
        private Boolean disabled;

        /**
         * 操作人
         */
        private String updateUserName;

        /**
         * 操作时间
         */
        private LocalDateTime updateTime;
    }

    /**
     * 规则条件
//     */
//    @Data
//    @NoArgsConstructor
//    public static class ruleConditionsDTO extends CommonDTO {
//
//        /**
//         * 忽略前缀
//         */
//        private String ignorePrefix;
//
//        /**
//         * 忽略前缀
//         */
//        private String ignorePrefix;
//
//        /**
//         * 忽略前缀
//         */
//        private String ignorePrefix;
//
//        /**
//         * 忽略前缀
//         */
//        private String ignorePrefix;
//
//        /**
//         * 忽略前缀
//         */
//        private String ignorePrefix;
//
//        /**
//         * 忽略前缀
//         */
//        private String ignorePrefix;
//
//        /**
//         * 忽略前缀
//         */
//        private String ignorePrefix;
//
//        /**
//         * 忽略前缀
//         */
//        private String ignorePrefix;
//    }

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
        * 优先级:1-5
        */
        @NotNull(message = "优先级:1不能为空")
        @Digits(integer = 2, fraction = 0, message = "优先级:1最大长度不能超过2位")
        private BigDecimal priority;

        /**
        * 规则类型:
        */
        @NotBlank(message = "规则类型:不能为空")
        @Size(max = 50,message = "规则类型:最大长度不能超过50位")
        private String ruleType;

        /**
        * 规则正则
        */
        private String ruleRegular;

        /**
        * 扩展规则
        */
        private String extendRuleType;

        /**
        * 扩展规则正则
        */
        private String extendRuleRegular;

        /**
        * 是否禁用
        */
        private Boolean disabled;


    }


}