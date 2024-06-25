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
 * 输入输出api信息请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpCfgApiDTO implements Serializable {




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
        * api类型
        */
        private String apiType;

        /**
        * api名称
        */
        private String name;

        /**
        * api实现类
        */
        private String apiClass;

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
        * api类型
        */
        @NotBlank(message = "api类型不能为空")
        @Size(max = 255,message = "api类型最大长度不能超过255位")
        private String apiType;

        /**
        * api名称
        */
        @NotBlank(message = "api名称不能为空")
        @Size(max = 50,message = "api名称最大长度不能超过50位")
        private String name;

        /**
        * api实现类
        */
        @NotBlank(message = "api实现类不能为空")
        @Size(max = 50,message = "api实现类最大长度不能超过50位")
        private String apiClass;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;


    }


}