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
 * ERP审批同步-推送信息配置请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class CfgApproveSyncFieldMapDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 国际化类型 默认zh-CN
        */
        private String locale;

        /**
         * 字段id
         */
        private String fieldId;

        /**
        * 字段名
        */
        private String fieldName;

        /**
        * 字段来源
        */
        private String fieldSource;

        /**
        * 是否快捷审批
        */
        private Boolean isQuick;

        /**
        * 排序
        */
        private Integer sort;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 国际化类型 默认zh-CN
        */
        @NotBlank(message = "国际化类型 默认zh不能为空")
        @Size(max = 50,message = "国际化类型 默认zh最大长度不能超过50位")
        private String locale;

        /**
         * 字段id
         */
        private String fieldId;

        /**
        * 字段名
        */
        @NotBlank(message = "字段名不能为空")
        @Size(max = 50,message = "字段名最大长度不能超过50位")
        private String fieldName;

        /**
        * 字段来源
        */
        @NotBlank(message = "字段来源不能为空")
        @Size(max = 50,message = "字段来源最大长度不能超过50位")
        private String fieldSource;

        /**
        * 是否快捷审批
        */
        @NotNull(message = "是否快捷审批不能为空")
        private Boolean isQuick;

        /**
        * 排序
        */
        @NotNull(message = "排序不能为空")
        private Integer sort;


    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class NoticeFieldMapDTO{

        /**
         *
         */
        private String id;
        /**
         * 主表id
         */
        private String mainId;

        /**
         * 国际化类型 默认zh-CN
         */
        private String locale;


        /**
         * 字段id
         */
        private String fieldId;
        /**
         * 字段名
         */
        @NotBlank(message = "字段名不能为空")
        @Size(max = 50,message = "字段名最大长度不能超过50位")
        private String fieldName;

        /**
         * 字段来源
         */
        @NotBlank(message = "字段来源不能为空")
        @Size(max = 50,message = "字段来源最大长度不能超过50位")
        private String fieldSource;

        /**
         * 是否快捷审批
         */
        private Boolean isQuick;

        /**
         * 排序
         */
        private Integer sort;
    }


}