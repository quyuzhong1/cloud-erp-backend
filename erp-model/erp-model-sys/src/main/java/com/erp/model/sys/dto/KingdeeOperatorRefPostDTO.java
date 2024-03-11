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
 * 金蝶业务员表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
*/
@Data
@NoArgsConstructor
public class KingdeeOperatorRefPostDTO implements Serializable {




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
        * 金蝶业务员类型表id kingdee_operator_type
        */
        private String typeId;

        /**
        * 金蝶员工任岗表id  kingdee_user_ref_post 表
        */
        private String userPostId;


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
        * 金蝶业务员类型表id kingdee_operator_type
        */
        @NotBlank(message = "金蝶业务员类型表id kingdee_operator_type不能为空")
        @Size(max = 19,message = "金蝶业务员类型表id kingdee_operator_type最大长度不能超过19位")
        private String typeId;

        /**
        * 金蝶员工任岗表id  kingdee_user_ref_post 表
        */
        @NotBlank(message = "金蝶员工任岗表id  kingdee_user_ref_post 表不能为空")
        @Size(max = 19,message = "金蝶员工任岗表id  kingdee_user_ref_post 表最大长度不能超过19位")
        private String userPostId;


    }


}