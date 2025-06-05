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
 * cfg_query_option拓展表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-06-04
*/
@Data
@NoArgsConstructor
public class CfgQueryOptionExtDTO implements Serializable {




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
        private String cfgQueryOptionId;

        /**
        * 字段类型：class=类，enum=枚举
        */
        private String type;

        /**
        * 路径 （类路径，枚举路径）
        */
        private String classPath;

        /**
        * json(包含select,table,condition,sys_classify )
        */
        private String dataJson;


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
        private String cfgQueryOptionId;

        /**
        * 字段类型：class=类，enum=枚举
        */
        @NotBlank(message = "字段类型：class=类，enum=枚举不能为空")
        @Size(max = 32,message = "字段类型：class=类，enum=枚举最大长度不能超过32位")
        private String type;

        /**
        * 路径 （类路径，枚举路径）
        */
        @NotBlank(message = "路径 （类路径，枚举路径）不能为空")
        @Size(max = 64,message = "路径 （类路径，枚举路径）最大长度不能超过64位")
        private String classPath;

        /**
        * json(包含select,table,condition,sys_classify )
        */
        @NotBlank(message = "json(包含select,table,condition,sys_classify )不能为空")
        private String dataJson;


    }



    @Data
    @NoArgsConstructor
    public static class BooleanDTO {
        private String label;
        private String value;
    }


    @Data
    @NoArgsConstructor
    public static class ClassDTO {
        //系统归属
        private String sysClassify;
        //查询的值
        private String select;
        //表名
        private String tableName ;
        //条件设置
        private String condition;
        //字典表的type
        private String type;
    }
    @Data
    @NoArgsConstructor
    public static class EnumDTO {
        //系统归属
        private String sysClassify;
        //系统归属
        private String enumName;

    }


}