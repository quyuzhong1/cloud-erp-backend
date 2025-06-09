package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-07-19
*/
@Data
@NoArgsConstructor
public class QuerySchemeFavoriteDTO implements Serializable {




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
        * 名称
        */
        private String name;

        /**
        * 用户id
        */
        private String userId;

        /**
        * 模块跳转路径
        */
        private String modulePath;

        /**
        * 参数json
        */
        private LinkedList<LinkedHashMap<String, Object>> paramJson;

        /**
        * 备注
        */
        private String remark ;


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
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 255,message = "名称最大长度不能超过255位")
        private String name;

        /**
        * 模块跳转路径
        */
        @NotBlank(message = "模块跳转路径不能为空")
        @Size(max = 255,message = "模块跳转路径最大长度不能超过255位")
        private String modulePath;

        /**
        * 参数json
        */
        @NotEmpty(message = "参数json不能为空")
        private LinkedList<LinkedHashMap<String, Object>> paramJson;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark ;


    }


}