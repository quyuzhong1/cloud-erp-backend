package com.erp.model.wms.dto;

import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Lambda
 * @Classname QcProductDTO
 * @Description TODO
 * @Date 2023-04-14 15:31
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcProductDTO {


    /**
     * 暂存 质检产品信息
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        private String id;


        /**
         * sku
         */
        @NotBlank(message = "sku不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private String skuId;


        /**
         * 产品长
         */
        @NotNull(message = "产品尺寸长不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private BigDecimal productLength;

        /**
         * 产品宽
         */
        @NotNull(message = "产品尺寸宽不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private BigDecimal productWidth;

        /**
         * 产品高
         */
        @NotNull(message = "产品尺寸高不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private BigDecimal productHeight;

        /**
         * 箱长
         */
        @NotNull(message = "外箱尺寸长不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private BigDecimal boxLength;

        /**
         * 箱宽
         */
        @NotNull(message = "外箱尺寸宽不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private BigDecimal boxWidth;

        /**
         * 箱高
         */
        @NotNull(message = "外箱尺寸高不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private BigDecimal boxHeight;

        /**
         * 产品净重
         */
        @NotNull(message = "产品净重不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private BigDecimal productNetWeight;

        /**
         * 外箱重量
         */
        @NotNull(message = "外箱重量不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private BigDecimal boxWeight;

        /**
         * 外箱图片地址集合
         */
        private List<String> boxImageUrlList;

        /**
         * 外箱图片名称集合
         */
        private List<String> boxImageNameList;


        /**
         * 产品图片地址集合
         */
        private List<String> productImageUrlList;

        /**
         * 产品名称地址集合
         */
        private List<String> productImageNameList;



    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        private String id;


        /**
         * 产品名称
         */
        private String productName;


        /**
         * 产品等级
         */
        private String productGrade;

        /**
         * 产品变体信息
         */
        private String variantProperty;


        /**
         * sku
         */
        private String skuId;



        /**
         * sku
         */
        private String skuNo;


        /**
         * 产品长
         */
        private BigDecimal productLength;

        /**
         * 产品宽
         */
        private BigDecimal productWidth;

        /**
         * 产品高
         */
        private BigDecimal productHeight;

        /**
         * 箱长
         */
        private BigDecimal boxLength;

        /**
         * 箱宽
         */
        private BigDecimal boxWidth;

        /**
         * 箱高
         */
        private BigDecimal boxHeight;

        /**
         * 产品净重
         */
        private BigDecimal productNetWeight;

        /**
         * 外箱重量
         */
        private BigDecimal boxWeight;

        /**
         * 外箱图片地址集合
         */
        private List<String> boxImageUrlList;

        /**
         * 外箱图片名称集合
         */
        private List<String> boxImageNameList;


        /**
         * 产品图片地址集合
         */
        private List<String> productImageUrlList;

        /**
         * 产品名称地址集合
         */
        private List<String> productImageNameList;



    }
}
