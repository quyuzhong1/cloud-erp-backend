package com.erp.model.scm.dto;

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
 * 供应商资质字典表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-15
*/
@Data
@NoArgsConstructor
public class DictCredentialDTO implements Serializable {




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
        * 资质编号
        */
        private String code;

        /**
        * 资质名称
        */
        private String name;

        /**
        * 是否启用
        */
        private Boolean disabled;

        /**
        * 排序
        */
        private Integer sort;


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
        * 资质名称
        */
        @NotBlank(message = "资质名称不能为空")
        @Size(max = 128,message = "资质名称最大长度不能超过128位")
        private String name;

    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 资质编号
         */
        private String code;

        /**
         * 资质名称
         */
        private String name;

        /**
         * 是否启用
         */
        private Boolean disabled;

        /**
         * 排序
         */
        private Integer sort;


    }


}