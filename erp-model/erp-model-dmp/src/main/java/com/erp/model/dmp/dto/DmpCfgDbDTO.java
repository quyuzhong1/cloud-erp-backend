package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 输入输出db信息请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpCfgDbDTO implements Serializable {




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
        * 系统id
        */
        private String systemId;

        /**
        * 输入输出类型：input=输入，output=输出
        */
        private String type;

        /**
        * db类型:pg=pg，mysql=mysql
        */
        private String dbType;

        /**
        * 主机
        */
        private String host;

        /**
        * 端口
        */
        private Integer port;

        /**
        * 用户名
        */
        private String userName;

        /**
        * 密码
        */
        private String passWord;

        /**
        * 数据库名
        */
        private String dbName;

        /**
        * 表名
        */
        private String tableName;

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
        * 系统id
        */
        @NotBlank(message = "系统id不能为空")
        @Size(max = 50,message = "系统id最大长度不能超过50位")
        private String systemId;

        /**
        * 输入输出类型：input=输入，output=输出
        */
        @NotBlank(message = "输入输出类型：input=输入，output=输出不能为空")
        @Size(max = 50,message = "输入输出类型：input=输入，output=输出最大长度不能超过50位")
        private String type;

        /**
        * db类型:pg=pg，mysql=mysql
        */
        @NotBlank(message = "db类型:pg=pg，mysql=mysql不能为空")
        @Size(max = 255,message = "db类型:pg=pg，mysql=mysql最大长度不能超过255位")
        private String dbType;

        /**
        * 主机
        */
        @NotBlank(message = "主机不能为空")
        @Size(max = 50,message = "主机最大长度不能超过50位")
        private String host;

        /**
        * 端口
        */
        @NotNull(message = "端口不能为空")
        private Integer port;

        /**
        * 用户名
        */
        @NotBlank(message = "用户名不能为空")
        @Size(max = 255,message = "用户名最大长度不能超过255位")
        private String userName;

        /**
        * 密码
        */
        @NotBlank(message = "密码不能为空")
        @Size(max = 255,message = "密码最大长度不能超过255位")
        private String passWord;

        /**
        * 数据库名
        */
        @NotBlank(message = "数据库名不能为空")
        @Size(max = 255,message = "数据库名最大长度不能超过255位")
        private String dbName;

        /**
        * 表名
        */
        @NotBlank(message = "表名不能为空")
        @Size(max = 255,message = "表名最大长度不能超过255位")
        private String tableName;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;


    }


}