package com.erp.model.dmp.dto;

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
 * 输出数据获取配置请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-02-10
*/
@Data
@NoArgsConstructor
public class DmpCfgOutputDataDTO implements Serializable {




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
        * 名称
        */
        private String name;

        /**
        * 获取数据类型：db=数据库,api=接口
        */
        private String type;

        /**
        * 数据库id
        */
        private String dbId;

        /**
        * sql语句
        */
        private String sqlString;

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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 500,message = "名称最大长度不能超过500位")
        private String name;

        /**
        * 获取数据类型：db=数据库,api=接口
        */
        @NotBlank(message = "获取数据类型：db=数据库,api=接口不能为空")
        @Size(max = 50,message = "获取数据类型：db=数据库,api=接口最大长度不能超过50位")
        private String type;

        /**
        * 数据库id
        */
        @NotBlank(message = "数据库id不能为空")
        @Size(max = 19,message = "数据库id最大长度不能超过19位")
        private String dbId;

        /**
        * sql语句
        */
        @NotBlank(message = "sql语句不能为空")
        @Size(max = 1024,message = "sql语句最大长度不能超过1,024位")
        private String sqlString;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;


    }


}