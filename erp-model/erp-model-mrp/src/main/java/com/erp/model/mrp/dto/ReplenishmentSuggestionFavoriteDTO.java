package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 补货建议关注表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-30
*/
@Data
@NoArgsConstructor
public class ReplenishmentSuggestionFavoriteDTO implements Serializable {




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
        * 用户id
        */
        private String userId;

        /**
        * 补货建议id
        */
        private String replenishmentSuggestionId;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 用户id
        */
        @NotBlank(message = "用户id不能为空")
        @Size(max = 19,message = "用户id最大长度不能超过19位")
        private String userId;

        /**
        * 补货建议id
        */
        @NotBlank(message = "补货建议id不能为空")
        @Size(max = 19,message = "补货建议id最大长度不能超过19位")
        private String replenishmentSuggestionId;


    }


}