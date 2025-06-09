package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 拣货车类型请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-06-20
*/
@Data
@NoArgsConstructor
public class PickingCartTypeDTO implements Serializable {




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


    }


    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateDTO {

        /**
        * 主键id
        */
        private String id;

        /**
         * 名称
         */
        @NotBlank(message = "名称不能为空")
        @Size(max = 64,message = "名称最大长度不能超过64位")
        private String name;
    }

    /**
     * 下拉
     */
    @Data
    @NoArgsConstructor
    public static class SelectDTO {

        /**
         * 关键词
         */
        private String searchKeyword;
    }

    /**
     * 下拉
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
       private String id;
        /**
         * 类型名称
         */
       private String name;
    }
}