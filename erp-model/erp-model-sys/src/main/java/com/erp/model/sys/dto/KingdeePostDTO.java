package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 金蝶岗位表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
*/
@Data
@NoArgsConstructor
public class KingdeePostDTO implements Serializable {




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
        * 金蝶code
        */
        private String code;

        /**
        * 金蝶部门表id  kingdee_department 表id
        */
        private String kingdeeDeptId;

        /**
        * 使用组织id
        */
        private String useOrgId;

        /**
        * 使用组织名称
        */
        private String useOrgName;

        /**
        * 金蝶id
        */
        private String kingdeeId;

        /**
        * 启用禁用
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
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 32,message = "名称最大长度不能超过32位")
        private String name;

        /**
        * 金蝶部门表id  kingdee_department 表id
        */
        @NotBlank(message = "金蝶部门表id  kingdee_department 表id不能为空")
        @Size(max = 19,message = "金蝶部门表id  kingdee_department 表id最大长度不能超过19位")
        private String kingdeeDeptId;

        /**
        * 使用组织id
        */
        @NotBlank(message = "使用组织id不能为空")
        @Size(max = 19,message = "使用组织id最大长度不能超过19位")
        private String useOrgId;

        /**
        * 使用组织名称
        */
        @NotBlank(message = "使用组织名称不能为空")
        @Size(max = 32,message = "使用组织名称最大长度不能超过32位")
        private String useOrgName;

        /**
        * 金蝶id
        */
        @NotBlank(message = "金蝶id不能为空")
        @Size(max = 16,message = "金蝶id最大长度不能超过16位")
        private String kingdeeId;

        /**
        * 启用禁用
        */
        @NotNull(message = "启用禁用不能为空")
        private Boolean disabled;


    }


}