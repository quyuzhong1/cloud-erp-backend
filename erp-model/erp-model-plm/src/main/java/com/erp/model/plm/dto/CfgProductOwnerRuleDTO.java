package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 产品归属规则配置表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
*/
@Data
@NoArgsConstructor
public class CfgProductOwnerRuleDTO implements Serializable {




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
        * 分类id
        */
        private String categoryId;
        /**
        * 组织id
        */
        private String orgId;

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
        * 分类id
        */
        @NotBlank(message = "分类id不能为空")
        @Size(max = 19,message = "分类id最大长度不能超过19位")
        private String categoryId;
        /**
        * 组织id
        */
        @NotBlank(message = "组织id不能为空")
        @Size(max = 19,message = "组织id最大长度不能超过19位")
        private String orgId;

    }


}