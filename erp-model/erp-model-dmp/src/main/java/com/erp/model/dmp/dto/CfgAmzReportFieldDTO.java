package com.erp.model.dmp.dto;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 亚马逊报告字段配置请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
*/
@Data
@NoArgsConstructor
public class CfgAmzReportFieldDTO implements Serializable {




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
        * 备注 需要的时候 用到
        */
        private String remark;

        /**
        * 报告列表名称
        */
        private String columnName;

        /**
        * 本地报告字段名称
        */
        private String fieldName;

        /**
        * 报告类型
        */
        private String reportType;

        /**
        * 启用状态
        */
        private Boolean status;


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
        * 备注 需要的时候 用到
        */
        @NotBlank(message = "备注 需要的时候 用到不能为空")
        @Size(max = 255,message = "备注 需要的时候 用到最大长度不能超过255位")
        private String remark;

        /**
        * 报告列表名称
        */
        @NotBlank(message = "报告列表名称不能为空")
        @Size(max = 50,message = "报告列表名称最大长度不能超过50位")
        private String columnName;

        /**
        * 本地报告字段名称
        */
        @NotBlank(message = "本地报告字段名称不能为空")
        @Size(max = 30,message = "本地报告字段名称最大长度不能超过30位")
        private String fieldName;

        /**
        * 报告类型
        */
        @NotBlank(message = "报告类型不能为空")
        @Size(max = 255,message = "报告类型最大长度不能超过255位")
        private String reportType;

        /**
        * 启用状态
        */
        @NotNull(message = "启用状态不能为空")
        private Boolean status;


    }


}