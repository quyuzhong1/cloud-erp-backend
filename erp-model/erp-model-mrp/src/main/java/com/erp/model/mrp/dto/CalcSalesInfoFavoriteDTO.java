package com.erp.model.mrp.dto;

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
 * 试算关注表请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
*/
@Data
@NoArgsConstructor
public class CalcSalesInfoFavoriteDTO implements Serializable {




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
        * 试算id
        */
        private String calcSalesInfoDimId;


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
        * 用户id
        */
        @NotBlank(message = "用户id不能为空")
        @Size(max = 19,message = "用户id最大长度不能超过19位")
        private String userId;

        /**
        * 试算id
        */
        @NotBlank(message = "试算id不能为空")
        @Size(max = 19,message = "试算id最大长度不能超过19位")
        private String calcSalesInfoDimId;


    }


}