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
 * 汉化管理规则表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2025-01-17
*/
@Data
@NoArgsConstructor
public class RulePromptWordDTO implements Serializable {




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
        * 名称
        */
        private String name;

        /**
        * 禁用状态false 未禁用
        */
        private Boolean disabled;

        /**
        * 规则描述
        */
        private String desc;

        /**
        * 提示
        */
        private String tips;

        /**
        * 解决方案
        */
        private String solution;

        /**
        * 优先级
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
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 50,message = "名称最大长度不能超过50位")
        private String name;

        /**
        * 禁用状态false 未禁用
        */
        @NotNull(message = "禁用状态false 未禁用不能为空")
        private Boolean disabled;

        /**
        * 规则描述
        */
        @NotBlank(message = "规则描述不能为空")
        @Size(max = 255,message = "规则描述最大长度不能超过255位")
        private String desc;

        /**
        * 提示
        */
        @NotBlank(message = "提示不能为空")
        @Size(max = 255,message = "提示最大长度不能超过255位")
        private String tips;

        /**
        * 解决方案
        */
        @NotBlank(message = "解决方案不能为空")
        @Size(max = 255,message = "解决方案最大长度不能超过255位")
        private String solution;

        /**
        * 优先级
        */
        @NotNull(message = "优先级不能为空")
        private Integer index;


    }


}