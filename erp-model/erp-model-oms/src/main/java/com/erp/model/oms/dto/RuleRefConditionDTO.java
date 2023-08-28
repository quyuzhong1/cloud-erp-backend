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
 * 规则关联条件表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
*/
@Data
@NoArgsConstructor
public class RuleRefConditionDTO implements Serializable {




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
        * 规则id
        */
        private String ruleId;

        /**
        * 条件id
        */
        private String ruleConditionId;


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
        * 规则id
        */
        @NotBlank(message = "规则id不能为空")
        @Size(max = 19,message = "规则id最大长度不能超过19位")
        private String ruleId;

        /**
        * 条件id
        */
        @NotBlank(message = "条件id不能为空")
        @Size(max = 19,message = "条件id最大长度不能超过19位")
        private String ruleConditionId;


    }


}