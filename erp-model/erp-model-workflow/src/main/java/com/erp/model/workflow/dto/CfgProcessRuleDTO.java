package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 流程设置执行条件请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-13
*/
@Data
@NoArgsConstructor
public class CfgProcessRuleDTO implements Serializable {




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
        * 流程类型：erpProgress=ERP流程,fsProgress=飞书流程
        */
        private String type;

        /**
        * 流程配置id
        */
        private String cfgProcessId;

        /**
        * 流程定义id
        */
        private String processDefinitionId;

        /**
        * 版本
        */
        private String processDefinitionVersion;

        /**
        * 是否默认
        */
        private Boolean isDefault;

        /**
        * 启用状态
        */
        private Boolean disabled;

        /**
         * 启动条件
         */
        private String ruleDesc;

        private List<CfgProcessExpDTO.ViewDTO> processExpDTOList;

        private List<CfgProcessFieldMapDTO.ViewDTO> processFieldMapDTOList;
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

        /**
         * 审核条件
         */
        @Valid
        private List<CfgProcessExpDTO.AddOrUpdateDTO> processExpDTOList;

        /**
         * 字段配置
         */
        @Valid
        private List<CfgProcessFieldMapDTO.AddOrUpdateDTO> processFieldMapDTOList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 流程类型：erpProgress=ERP流程,fsProgress=飞书流程
        */
        @NotBlank(message = "流程类型：erpProgress=ERP流程,fsProgress=飞书流程不能为空")
        @Size(max = 100,message = "流程类型：erpProgress=ERP流程,fsProgress=飞书流程最大长度不能超过100位")
        private String type;

        /**
        * 流程配置id
        */
        @NotBlank(message = "流程配置id不能为空")
        @Size(max = 30,message = "流程配置id最大长度不能超过30位")
        private String cfgProcessId;

        /**
        * 流程定义id
        */
        @NotBlank(message = "流程定义id不能为空")
        @Size(max = 100,message = "流程定义id最大长度不能超过100位")
        private String processDefinitionId;

        /**
        * 版本
        */
        @NotBlank(message = "版本不能为空")
        @Size(max = 100,message = "版本最大长度不能超过100位")
        private String processDefinitionVersion;

        /**
        * 是否默认
        */
        @NotNull(message = "是否默认不能为空")
        private Boolean isDefault;

        /**
        * 启用状态
        */
        @NotNull(message = "启用状态不能为空")
        private Boolean disabled;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateStateDTO{

        @NotBlank(message = "id不能为空")
        private String id;


        /**
         * true为默认
         * false为非默认
         */
        @NotNull(message = "状态值不能为空")
        private Boolean state;

        @NotBlank(message = "cfgProcessId不能为空")
        private String cfgProcessId;
    }
}