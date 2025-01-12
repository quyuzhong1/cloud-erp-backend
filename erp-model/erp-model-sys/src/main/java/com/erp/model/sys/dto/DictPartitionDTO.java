package com.erp.model.sys.dto;

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
 * 分区表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2025-01-03
*/
@Data
@NoArgsConstructor
public class DictPartitionDTO implements Serializable {




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
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 编码
        */
        private String code;

        /**
        * 名称
        */
        private String name;

        /**
        * 排序字段
        */
        private Integer index;


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
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 255,message = "名称最大长度不能超过255位")
        private String name;

        /**
        * 排序字段
        */
        @NotNull(message = "排序字段不能为空")
        private Integer index;


    }


    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 关键词
         */
        private String searchKeyword;
        /**
         * 是否禁用
         */
        private Boolean disabled;
    }
    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class DictDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 是否禁用
         */
        private Boolean disabled;

        /**
         * 编码
         */
        private String code;

        /**
         * 名称
         */
        private String value;
        private String name;

        /**
         * 排序字段
         */
        private Integer index;


    }
}