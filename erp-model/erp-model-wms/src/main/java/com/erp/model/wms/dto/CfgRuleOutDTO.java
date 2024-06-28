package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 出库配置规则请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-06-28
*/
@Data
@NoArgsConstructor
public class CfgRuleOutDTO implements Serializable {




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
        * 配置规则类型
        */
        private String type;

        /**
        * 规则内容
        */
        private String ruleContent;


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
        * 配置规则类型
        */
        @NotBlank(message = "配置规则类型不能为空")
        @Size(max = 20,message = "配置规则类型最大长度不能超过20位")
        private String type;

    }


}