package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 补货建议标签关系表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-30
*/
@Data
@NoArgsConstructor
public class ReplenishmentRefLabelDTO implements Serializable {




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
        * 补货建议id
        */
        private String refId;

        /**
        * 标签id
        */
        private String labelId;

        /**
        * 类型   补货建议
        */
        private String type;


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * 标签id
         */
        @NotEmpty(message = "标签id不能为空")
        @Size(max = 19,message = "标签id最大长度不能超过19位")
        private List<String> labelIdList;

        /**
         * 类型   补货建议
         */
        @NotBlank(message = "类型   补货建议不能为空")
        @Size(max = 255,message = "类型   补货建议最大长度不能超过255位")
        private String type;

        /**
         * 是否增量
         */
        private Boolean isIncrement = Boolean.FALSE;
    }


}