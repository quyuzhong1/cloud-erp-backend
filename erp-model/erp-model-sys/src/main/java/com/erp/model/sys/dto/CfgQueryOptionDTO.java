package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * 查询option配置表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-01-04
*/
@Data
@NoArgsConstructor
public class CfgQueryOptionDTO implements Serializable {



    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String  id;

        /**
         * 接口名称

         */
        private String apiName;

        /**
         * 请求路径
         */
        private String apiUrl;

        /**
         * 请求方法 eg:post,get
         */
        private String requestMethod;

        /**
         * 请求参数
         */
        private Map<String,Object> param;

        /**
         * 下拉框显示值
         */
        private String selectLabel;

        /**
         * 下拉框绑定值
         */
        private String selectValue;

        /**
         * 下拉框禁用绑定字段
         */
        private String selectDisabled;
        /**
         * 查询绑定属性
         */
        private String searchKeyField;


        /**
         * 请求参数
         */
        private Map<String,Object> props;

        /**
         * api接口类型
         */
        private String apiType;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
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
        * 接口名称

        */
        private String apiName;

        /**
        * 请求路径
        */
        private String apiUrl;

        /**
        * 请求方法 eg:post,get
        */
        private String requestMethod;

        /**
        * 请求参数
        */
        private Map<String,Object> param;

        /**
        * 下拉框显示值
        */
        private String selectLabel;

        /**
        * 下拉框绑定值
        */
        private String selectValue;

        /**
        * 下拉框禁用绑定字段
        */
        private String selectDisabled;
        /**
         * 查询绑定属性
         */
        private String searchKeyField;


        /**
         * 组件props参数
         */
        private Map<String,Object> props;
    }

    @Data
    @NoArgsConstructor
    public static class ParamDTO{
        /**
         * api名称
         */
        private String apiName;
        /**
         * api路径
         */
        private String apiUrl;
        /**
         * api类型
         */
        private String apiType;
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
        * 接口名称

        */
        @NotBlank(message = "接口名称 不能为空")
        @Size(max = 100,message = "接口名称 最大长度不能超过100位")
        private String apiName;

        /**
        * 请求路径
        */
        @NotBlank(message = "请求路径不能为空")
        @Size(max = 100,message = "请求路径最大长度不能超过100位")
        private String apiUrl;

        /**
        * 请求方法 eg:post,get
        */
        @NotBlank(message = "请求方法 eg:post,get不能为空")
        @Size(max = 30,message = "请求方法 eg:post,get最大长度不能超过30位")
        private String requestMethod;

        /**
        * 请求参数
        */
        private Map<String,Object> param;

        /**
        * 下拉框显示值
        */
        private String selectLabel;

        /**
        * 下拉框绑定值
        */
        private String selectValue;

        /**
        * 下拉框禁用绑定字段
        */
        private String selectDisabled;
        /**
         * 查询绑定属性
         */
        private String searchKeyField;

        /**
         * 组件props参数
         */
        private Map<String,Object> props;

        /**
         * 接口类型
         */
        private String apiType;

    }


}