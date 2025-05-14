package com.erp.model.workflow.dto;

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
 * fieldList 明细字段映射请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-14
*/
@Data
@NoArgsConstructor
public class CfgProcessFieldSubMapDTO implements Serializable {




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
        * 所属fieldList主字段的id
        */
        private String parentId;

        /**
        * 第三方字段(英文：控件name)
        */
        private String thirdField;

        /**
        * 第三方类型（选项、数值等）：控件type
        */
        private String thirdFieldType;

        /**
        * 第三方是否必填：必填
        */
        private Boolean thirdFieldRequired;

        /**
        * 第三方字段说明：description
        */
        private String thirdFieldDescription;

        /**
        * 数大臣字段
        */
        private String sysField;

        /**
        * 数大臣字段类型
        */
        private String sysFieldType;

        /**
        * 数大臣是否必填
        */
        private Boolean sysFieldRequired;

        /**
        * 默认值
        */
        private String defaultValue;

        /**
        * 是否唯一
        */
        private Boolean isUnique;

        /**
        * 配置类型：sysCfg:系统字段配置、thirdCfg:飞书字段配置
        */
        private String cfgType;

        /**
        * 流程定义code
        */
        private String processDefintionId;


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
        * 所属fieldList主字段的id
        */
        @NotBlank(message = "所属fieldList主字段的id不能为空")
        @Size(max = 19,message = "所属fieldList主字段的id最大长度不能超过19位")
        private String parentId;

        /**
        * 第三方字段(英文：控件name)
        */
        @NotBlank(message = "第三方字段(英文：控件name)不能为空")
        @Size(max = 19,message = "第三方字段(英文：控件name)最大长度不能超过19位")
        private String thirdField;

        /**
        * 第三方类型（选项、数值等）：控件type
        */
        @NotBlank(message = "第三方类型（选项、数值等）：控件type不能为空")
        @Size(max = 30,message = "第三方类型（选项、数值等）：控件type最大长度不能超过30位")
        private String thirdFieldType;

        /**
        * 第三方是否必填：必填
        */
        @NotNull(message = "第三方是否必填：必填不能为空")
        private Boolean thirdFieldRequired;

        /**
        * 第三方字段说明：description
        */
        @NotBlank(message = "第三方字段说明：description不能为空")
        @Size(max = 100,message = "第三方字段说明：description最大长度不能超过100位")
        private String thirdFieldDescription;

        /**
        * 数大臣字段
        */
        @NotBlank(message = "数大臣字段不能为空")
        @Size(max = 30,message = "数大臣字段最大长度不能超过30位")
        private String sysField;

        /**
        * 数大臣字段类型
        */
        @NotBlank(message = "数大臣字段类型不能为空")
        @Size(max = 30,message = "数大臣字段类型最大长度不能超过30位")
        private String sysFieldType;

        /**
        * 数大臣是否必填
        */
        @NotNull(message = "数大臣是否必填不能为空")
        private Boolean sysFieldRequired;

        /**
        * 默认值
        */
        @NotBlank(message = "默认值不能为空")
        @Size(max = 30,message = "默认值最大长度不能超过30位")
        private String defaultValue;

        /**
        * 是否唯一
        */
        @NotNull(message = "是否唯一不能为空")
        private Boolean isUnique;

        /**
        * 配置类型：sysCfg:系统字段配置、thirdCfg:飞书字段配置
        */
        @NotBlank(message = "配置类型：sysCfg:系统字段配置、thirdCfg:飞书字段配置不能为空")
        @Size(max = 30,message = "配置类型：sysCfg:系统字段配置、thirdCfg:飞书字段配置最大长度不能超过30位")
        private String cfgType;

        /**
        * 流程定义code
        */
        @NotBlank(message = "流程定义code不能为空")
        @Size(max = 19,message = "流程定义code最大长度不能超过19位")
        private String processDefintionId;


    }


}