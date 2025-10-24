package com.erp.model.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 远程查询配置请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-10-17
*/
@Data
@NoArgsConstructor
public class CfgSystemFieldMappingDTO implements Serializable {




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
        * cfg_query_option表id
        */
        private String cfgQueryOptionId;

        /**
        * 接口显示字段
        */
        private String sourceDisplayField;

        /**
        * 接口来源字段
        */
        private String sourceField;

        /**
        * 业务保存字段
        */
        private String businessField;

        /**
        * 远程查询路径（serviceImpl）
        */
        private String feignPath;

        /**
        * 远程查询方法
        */
        private String feignMethod;

        /**
        * 远程查询参数
        */
        private String feignParam;

        /**
        * 目标字段
        */
        private String targetField;

        /**
        * 是否禁用
        */
        private Boolean disabled;


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
        * cfg_query_option表id
        */
        @NotBlank(message = "cfg_query_option表id不能为空")
        @Size(max = 30,message = "cfg_query_option表id最大长度不能超过30位")
        private String cfgQueryOptionId;

        /**
        * 接口显示字段
        */
        @NotBlank(message = "接口显示字段不能为空")
        @Size(max = 30,message = "接口显示字段最大长度不能超过30位")
        private String sourceDisplayField;

        /**
        * 接口来源字段
        */
        @NotBlank(message = "接口来源字段不能为空")
        @Size(max = 30,message = "接口来源字段最大长度不能超过30位")
        private String sourceField;

        /**
        * 业务保存字段
        */
        @NotBlank(message = "业务保存字段不能为空")
        @Size(max = 64,message = "业务保存字段最大长度不能超过64位")
        private String businessField;

        /**
        * 远程查询路径（serviceImpl）
        */
        @NotBlank(message = "远程查询路径（serviceImpl）不能为空")
        @Size(max = 255,message = "远程查询路径（serviceImpl）最大长度不能超过255位")
        private String feignPath;

        /**
        * 远程查询方法
        */
        @NotBlank(message = "远程查询方法不能为空")
        @Size(max = 255,message = "远程查询方法最大长度不能超过255位")
        private String feignMethod;

        /**
        * 远程查询参数
        */
        @NotBlank(message = "远程查询参数不能为空")
        @Size(max = 255,message = "远程查询参数最大长度不能超过255位")
        private String feignParam;

        /**
        * 目标字段
        */
        @NotBlank(message = "目标字段不能为空")
        @Size(max = 64,message = "目标字段最大长度不能超过64位")
        private String targetField;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;


    }

    /**
     * 系统字段映射参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldMappingParamDTO {

        /**
         * 所用类型，CfgQueryOptionUseTypeEnum枚举
         */
        private String useType;

        /**
         * 业务key，CfgQueryOptionBussinessKeyEnum枚举
         */
        private String bussinessKey;

        /**
         * erp字段名称
         */
        private String sysField;

        /**
         * 字段所属
         */
        private String sysParentId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeignResultDTO {

        /**
         * 远程查询路径（serviceImpl）
         */
        private String feignPath;

        /**
         * 远程查询方法
         */
        private String feignMethod;

        /**
         * 远程查询参数
         */
        private String feignParam;

        /**
         * 结果集合
         */
        private List<Object> resultList;
    }

}