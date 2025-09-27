package com.erp.model.workflow.dto;

import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 三方生成查询明细请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-05-27
*/
@Data
@NoArgsConstructor
public class ApproveTaskDetailDTO implements Serializable {




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
        * 三方生成查询id
        */
        private String mianId;

        /**
        * 第三方接口字段(英文)
        */
        private String thirdField;

        /**
        * 第三方类型（选项、数值等）
        */
        private String thirdFieldType;
        /**
         * 第三方类型名称（选项、数值等）,CfgQueryOptionFieldTypeEnum
         */
        private String thirdFieldTypeName;

        /**
        * 第三方字段值
        */
        private String thirdFieldValue;

        /**
        * 第三方字段必填
        */
        private Boolean thirdFieldRequired;

        /**
        * 说明
        */
        private String thirdDesc;

        /**
        * 数大臣字段名称
        */
        private String sysFieldName;

        /**
        * 数大臣接口字段(英文)
        */
        private String sysField;

        /**
        * 数大臣类型
        */
        private String sysFieldType;
        /**
         * 数大臣类型名称
         */
        private String sysFieldTypeName;

        /**
        * 数大臣必填
        */
        private Boolean sysFieldRequired;

        /**
        * 数大臣字段值
        */
        private String sysFieldValue;
        /**
         *排序
         */
        private Integer index;
        /**
         * 实体编码
         */
        private String entityCode;
        /**
         * 实体名称
         */
        private String entityName;

        /**
         * 字段信息
         */
        private CfgQueryOptionEntity cfgQueryOptionEntity;
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
    public static class UpdateDTO  {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 数大臣字段值
         */
        @NotBlank(message = "数大臣字段值不能为空")
        private String sysFieldValue;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 三方生成查询id
        */
        @NotBlank(message = "三方生成查询id不能为空")
        @Size(max = 19,message = "三方生成查询id最大长度不能超过19位")
        private String mianId;

        /**
        * 第三方接口字段(英文)
        */
        @NotBlank(message = "第三方接口字段(英文)不能为空")
        @Size(max = 32,message = "第三方接口字段(英文)最大长度不能超过32位")
        private String thirdField;

        /**
        * 第三方类型（选项、数值等）
        */
        @NotBlank(message = "第三方类型（选项、数值等）不能为空")
        @Size(max = 32,message = "第三方类型（选项、数值等）最大长度不能超过32位")
        private String thirdFieldType;

        /**
        * 第三方字段值
        */
        @NotBlank(message = "第三方字段值不能为空")
        @Size(max = 255,message = "第三方字段值最大长度不能超过255位")
        private String thirdFieldValue;

        /**
        * 第三方字段必填
        */
        @NotNull(message = "第三方字段必填不能为空")
        private Boolean thirdFieldRequired;

        /**
        * 说明
        */
        private String thirdDesc;

        /**
        * 数大臣字段名称
        */
        @NotBlank(message = "数大臣字段名称不能为空")
        @Size(max = 64,message = "数大臣字段名称最大长度不能超过64位")
        private String sysFieldName;

        /**
        * 数大臣接口字段(英文)
        */
        @NotBlank(message = "数大臣接口字段(英文)不能为空")
        @Size(max = 32,message = "数大臣接口字段(英文)最大长度不能超过32位")
        private String sysField;

        /**
        * 数大臣类型
        */
        @NotBlank(message = "数大臣类型不能为空")
        @Size(max = 32,message = "数大臣类型最大长度不能超过32位")
        private String sysFieldType;

        /**
        * 数大臣必填
        */
        @NotNull(message = "数大臣必填不能为空")
        private Boolean sysFieldRequired;

        /**
        * 数大臣字段值
        */
        @NotBlank(message = "数大臣字段值不能为空")
        private String sysFieldValue;

        /**
         * 排序字段
         */
        private Integer index;
        /**
         * 实体名称
         */
        private String entityName;
        /**
         * 实体编码
         */
        private String entityCode;
    }


}