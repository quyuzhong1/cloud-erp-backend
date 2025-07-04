package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 流程设置值映射请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class CfgProcessValueMapDTO implements Serializable {




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
        * 对应的字段配置ID
        */
        private String fieldMapId;

        /**
        * 第三方选项（显示文本）
        */
        private String thirdValue;

        /**
        * 数大臣选项值
        */
        private String sysValue;

        private String defaultValue;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;
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
        * 对应的字段配置ID
        */
        @Size(max = 19,message = "对应的字段配置ID最大长度不能超过19位")
        private String fieldMapId;

        /**
        * 第三方选项（显示文本）
        */
        @NotBlank(message = "第三方选项（显示文本）不能为空")
        @Size(max = 255,message = "第三方选项（显示文本）最大长度不能超过30位")
        private String thirdValue;

        /**
        * 数大臣选项值
        */
        @NotBlank(message = "数大臣选项值不能为空")
        @Size(max = 255,message = "数大臣选项值最大长度不能超过30位")
        private String sysValue;


        @Size(max = 255,message = "数大臣选项值最大长度不能超过30位")
        private String defaultValue;
    }

    @Data
    @NoArgsConstructor
    public static class DropDownDTO {

        /**
        * 前段展示
        */
        private String name;

        /**
        * 实际value
        */
        private String value;
    }
}