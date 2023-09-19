package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 产品便签关系表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Data
@NoArgsConstructor
public class ProductRefLabelDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * sku id
         */
        private String skuId;

        /**
         * 产品id
         */
        private String productId;

        /**
         * 标签id
         */
        private String labelId;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class BatchAddDTO {
        /**
         * 产品列表
         */
        @NotNull(message = "产品列表不能为空")
        private List<ProjectDTO> projectDTOs;

        /**
         * 标签ids
         */
        @NotNull(message = "标签id集合不能为空")
        private List<String> labelIds;


    }

    /**
     * 产品
     */
    @Data
    @NoArgsConstructor
    public static class ProjectDTO {
        /**
         * sku id
         */
        @NotBlank(message = "sku id不能为空")
        @Size(max = 19, message = "sku id最大长度不能超过19位")
        private String skuId;

        /**
         * 产品id
         */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 19, message = "产品id最大长度不能超过19位")
        private String productId;
    }

    /**
     * 标签
     */
    @Data
    @NoArgsConstructor
    public static class LabelDTO {
        /**
         * 关系id
         */
        private String id;
        /**
         * 标签id
         */
        @NotNull(message = "标签id不能为空")
        private String labelId;
        /**
         * 标签名称
         */
        private String name;
        /**
         * 标签颜色
         */
        private String colour;
        /**
         * 标签等级
         */
        private String level;
        /**
         * sku id
         */
        private String skuId;

        /**
         * 产品id
         */
        private String productId;
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

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class RemoveDTO {

        /**
         * 关系记录id集合
         */
        private List<String> ids;

        /**
         * sku id
         */
        @Size(max = 19, message = "sku id最大长度不能超过19位")
        private String skuId;

        /**
         * 产品id
         */
        @Size(max = 19, message = "产品id最大长度不能超过19位")
        private String productId;

        /**
         * 标签id
         */
        @Size(max = 19, message = "标签id最大长度不能超过19位")
        private String labelId;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * sku id
         */
        @NotBlank(message = "sku id不能为空")
        @Size(max = 19, message = "sku id最大长度不能超过19位")
        private String skuId;

        /**
         * 产品id
         */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 19, message = "产品id最大长度不能超过19位")
        private String productId;

        /**
         * 标签id
         */
        @NotBlank(message = "标签id不能为空")
        @Size(max = 19, message = "标签id最大长度不能超过19位")
        private String labelId;


    }


}