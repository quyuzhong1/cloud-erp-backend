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
 * 流程设置字段配置请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class CfgProcessFieldMapDTO implements Serializable {




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
        * 第三方字段(中文：控件name)
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
        * 配置id
        */
        private String cfgId;

        /**
        * 默认值
        */
        private String defaultValue;

        /**
        * 是否唯一
        */
        private Boolean isUnique;

        /**
        * 配置类型：sysCfg:系统字段配置、fsCfg:飞书字段配置
        */
        private String cfgType;

        /**
         * 第三方字段id，唯一标识
         */
        private String thirdFieldId;

        /**
         * 第三方字段是否是明细控件
         */
        private Boolean isDetailField;

        /**
         * 第三方字段所属明细控件id
         */
        private String thirdParentId;

        /**
         * 明细字段父id
         */
        private String sysParentId;

        /**
         * 组别类型（0正常级别，1集合父项，2集合子项）
         */
        private String groupType;

        /**
         * 排序字段
         */
        private int index;

        /**
         * thirdFieldTypeName
         */
        private String thirdFieldTypeName;

        /**
         * sysFieldTypeName
         */
        private String sysFieldTypeName;

        /**
         * 数据唯一值，sysParentId + sysField,同fieldBelongsType +conditionField
         */
        private String uniqueCode;

        private List<CfgProcessValueMapDTO.ViewDTO> processValueMapDTOList;
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
         * 选项条件
         */
        @Valid
        private List<CfgProcessValueMapDTO.AddOrUpdateDTO> processValueMapDTOList;
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
        * 第三方字段(英文：控件name)
        */
        //@NotBlank(message = "第三方字段(中文：控件name)不能为空")
        @Size(max = 19,message = "第三方字段(中文：控件name)最大长度不能超过19位")
        private String thirdField;

        /**
        * 第三方类型（选项、数值等）：控件type
        */
        //@NotBlank(message = "第三方类型（选项、数值等）：控件type不能为空")
        @Size(max = 30,message = "第三方类型（选项、数值等）：控件type最大长度不能超过30位")
        private String thirdFieldType;

        /**
        * 第三方是否必填：必填
        */
        //@NotNull(message = "第三方是否必填：必填不能为空")
        private Boolean thirdFieldRequired;

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
        * 配置id
        */
        @Size(max = 19,message = "配置id最大长度不能超过19位")
        private String cfgId;

        /**
        * 默认值
        */
        @Size(max = 255,message = "默认值最大长度不能超过30位")
        private String defaultValue;

        /**
        * 是否唯一
        */
        @NotNull(message = "是否唯一不能为空")
        private Boolean isUnique;

        /**
        * 配置类型：sysCfg:系统字段配置、fsCfg:飞书字段配置
        */
        @Size(max = 30,message = "配置类型：sysCfg:系统字段配置、fsCfg:飞书字段配置最大长度不能超过30位")
        private String cfgType;

        /**
         * 第三方字段id，唯一标识
         */
        @NotBlank(message = "第三方字段id")
        @Size(max = 255,message = "第三方字段id最大长度不能超过255位")
        private String thirdFieldId;

        /**
         * 第三方字段是否是明细控件
         */
        @NotNull(message = "是否是明细控件不能为空")
        private Boolean isDetailField;

        /**
         * 第三方字段父id
         */
        @Size(max = 255,message = "第三方字段父id最大长度不能超过255位")
        private String thirdParentId;

        /**
         * 第三方字段所属明细控件id
         */
        @Size(max = 255,message = "父控件id最大长度不能超过255位")
        private String sysParentId;

        /**
         * 排序
         */
        private int index;
    }


}