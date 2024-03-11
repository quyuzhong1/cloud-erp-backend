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
 * 请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
*/
@Data
@NoArgsConstructor
public class KingdeeOperatorTypeDTO implements Serializable {




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
        * 金蝶id
        */
        private String kingdeeId;

        /**
        * 金蝶code
        */
        private String code;

        /**
        * 金蝶name
        */
        private String name;


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
        * 金蝶id
        */
        @NotBlank(message = "金蝶id不能为空")
        @Size(max = 19,message = "金蝶id最大长度不能超过19位")
        private String kingdeeId;

        /**
        * 金蝶name
        */
        @NotBlank(message = "金蝶name不能为空")
        @Size(max = 32,message = "金蝶name最大长度不能超过32位")
        private String name;


    }


}