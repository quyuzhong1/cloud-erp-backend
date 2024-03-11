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
 * 金蝶员工任岗表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
*/
@Data
@NoArgsConstructor
public class KingdeeUserRefPostDTO implements Serializable {




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
        * erp 员工id
        */
        private String erpUserId;

        /**
        * 金蝶岗位表id kingdee_post 表
        */
        private String kingdeePostId;


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
        * erp 员工id
        */
        @NotBlank(message = "erp 员工id不能为空")
        @Size(max = 19,message = "erp 员工id最大长度不能超过19位")
        private String erpUserId;

        /**
        * 金蝶岗位表id kingdee_post 表
        */
        @NotBlank(message = "金蝶岗位表id kingdee_post 表不能为空")
        @Size(max = 19,message = "金蝶岗位表id kingdee_post 表最大长度不能超过19位")
        private String kingdeePostId;


    }


}