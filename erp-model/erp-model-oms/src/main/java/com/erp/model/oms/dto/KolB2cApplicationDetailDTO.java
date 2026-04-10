package com.erp.model.oms.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * B2C寄样申请单明细请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-12-04
*/
@Data
@NoArgsConstructor
public class KolB2cApplicationDetailDTO implements Serializable {




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
        * 主表ID
        */
        private String mainId;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * SKU编码
        */
        private String skuNo;

        /**
         * SPU编码
         */
        private String spuNo;

        /**
        * 品牌id
        */
        private String brandId;

        /**
        * 品牌
        */
        private String brandName;

        /**
        * 达人ID
        */
        private String partnerId;

        /**
        * 达人昵称
        */
        private String nickname;

        /**
        * 申请数量
        */
        private Integer applyQty;

        /**
        * 预计回片日期
        */
        private LocalDate planFeedbackDate;

        /**
        * 备注
        */
        private String remark;

        /**
        * 项目标签
        */
        private String projectTag;


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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 主表ID
        */
        private String mainId;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU不能为空")
        @Size(max = 19,message = "SKU最大长度不能超过19位")
        private String skuId;

        /**
         * SKU编码
         */
        private String skuNo;

        /**
         * SPU编码
         */
        private String spuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
        * 品牌id
        */
        private String brandId;

        /**
        * 品牌
        */
        private String brandName;

        /**
        * 达人ID
        */
        @NotBlank(message = "达人不能为空")
        private String partnerId;

        /**
        * 达人昵称
        */
        private String nickname;

        /**
        * 申请数量
        */
        @NotNull(message = "申请数量不能为空")
        private Integer applyQty;

        /**
        * 预计回片日期
        */
        private LocalDate planFeedbackDate;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 项目标签
        */
        private String projectTag;

        private String projectTagName;
        /**
         * 项目标签
         */
        private List<String> projectTagList;


    }


}
