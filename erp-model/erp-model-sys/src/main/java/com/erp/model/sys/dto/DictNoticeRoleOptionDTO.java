package com.erp.model.sys.dto;

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
 * 请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-06-04
*/
@Data
@NoArgsConstructor
public class DictNoticeRoleOptionDTO implements Serializable {


    /**
     * 下拉值
     */
    @Data
    @NoArgsConstructor
    public static class DropDownDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 单据类型
         */
        private String businessType;

        /**
         * 字段（驼峰命名）
         */
        private String field;

        /**
         * 字段名
         */
        private String fieldName;

        /**
         * 排序
         */
        private Integer index;


    }


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
        * 单据类型
        */
        private String businessType;

        /**
        * 表类型：table=主表,detail=明细表
        */
        private String tableType;

        /**
        * 表名
        */
        private String tableName;

        /**
        * 系统归属
        */
        private String sysClassify;

        /**
        * 类路径
        */
        private String classPath;

        /**
        * 字段（驼峰命名）
        */
        private String field;

        /**
        * 字段名
        */
        private String fieldName;

        /**
        * 关联字段（明细表必填）
        */
        private String refField;

        /**
        * 是否启用
        */
        private Boolean disabled;

        /**
        * 备注
        */
        private String remark;

        /**
        * 排序
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
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 64,message = "单据类型最大长度不能超过64位")
        private String businessType;

        /**
        * 表类型：table=主表,detail=明细表
        */
        @NotBlank(message = "表类型：table=主表,detail=明细表不能为空")
        @Size(max = 64,message = "表类型：table=主表,detail=明细表最大长度不能超过64位")
        private String tableType;

        /**
        * 表名
        */
        @NotBlank(message = "表名不能为空")
        @Size(max = 64,message = "表名最大长度不能超过64位")
        private String tableName;

        /**
        * 系统归属
        */
        @NotBlank(message = "系统归属不能为空")
        @Size(max = 32,message = "系统归属最大长度不能超过32位")
        private String sysClassify;

        /**
        * 类路径
        */
        @NotBlank(message = "类路径不能为空")
        @Size(max = 64,message = "类路径最大长度不能超过64位")
        private String classPath;

        /**
        * 字段（驼峰命名）
        */
        @NotBlank(message = "字段（驼峰命名）不能为空")
        @Size(max = 64,message = "字段（驼峰命名）最大长度不能超过64位")
        private String field;

        /**
        * 字段名
        */
        @NotBlank(message = "字段名不能为空")
        @Size(max = 64,message = "字段名最大长度不能超过64位")
        private String fieldName;

        /**
        * 关联字段（明细表必填）
        */
        @NotBlank(message = "关联字段（明细表必填）不能为空")
        @Size(max = 64,message = "关联字段（明细表必填）最大长度不能超过64位")
        private String refField;

        /**
        * 是否启用
        */
        @NotNull(message = "是否启用不能为空")
        private Boolean disabled;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 排序
        */
        @NotNull(message = "排序不能为空")
        private Integer index;


    }


}