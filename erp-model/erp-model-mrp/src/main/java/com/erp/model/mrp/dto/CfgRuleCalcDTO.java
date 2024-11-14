package com.erp.model.mrp.dto;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.mrp.entity.CfgRuleCalcEntity;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 试算配置请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Data
@NoArgsConstructor
public class CfgRuleCalcDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * id
         */
        private String id;
        /**
         * sku_id
         */
        private List<String> skuIds;

        /**
         * 试算开始日期
         */
        private LocalDate startCalcDate;

        /**
         * 试算结束日期
         */
        private LocalDate endCalcDate;

        /**
         * 店铺json
         */
        private List<String> shopIds;

        /**
         * 历史销量类型
         */
        private String saleType;

        /**
         * 文件地址
         */
        private String fileUrl;

        /**
         * 试算配置名称
         */
        private String name;

        /**
         * 默认日销量
         */
        private CfgRuleSalesFormulaCalcDTO.ViewDTO defaultSalesQtyDTO;

        /**
         * 动态日销量
         */
        private List<CfgRuleSalesFormulaCalcDTO.ViewDTO> dynamicSalesQtyList;

        /**
         * 固定日销量
         */
        private List<CfgRuleSalesFormulaCalcDTO.ViewDTO> fixedSalesQtyList;

        /**
         * 销量去噪
         */
        private List<CfgRuleSalesDenoisingCalcDTO.ViewDTO> salesDenoisingList;

    }

    /**
     * 新增
     */
    @Getter
    @Setter
    public static class AddDTO {
        /**
         * sku_id
         */
        @Size(min = 1, message = "sku不能为空")
        private List<String> skuIds;

        /**
         * 试算日期
         */
        @NotNull(message = "开始试算日期不能为空")
        private LocalDate startCalcDate;

        /**
         * 试算日期
         */
        @NotNull(message = "结束试算日期不能为空")
        private LocalDate endCalcDate;

        /**
         * 店铺json
         */
        @Size(min = 1, message = "店铺不能为空")
        private List<String> shopIds;

        /**
         * 历史销量类型
         */
        @NotBlank(message = "历史销量类型不能为空")
        @Size(max = 255, message = "历史销量类型最大长度不能超过255位")
        private String saleType;

        /**
         * 文件地址
         */
        private String fileUrl;

        /**
         * 试算配置名称
         */
        @NotBlank(message = "试算配置名称不能为空")
        @Size(max = 255, message = "试算配置名称最大长度不能超过255位")
        private String name;

        /**
         * 默认日销量
         */
        @Valid
        private CfgRuleSalesFormulaDTO.DefaultUpdateDTO defaultSalesQtyDTO;

        /**
         * 动态日销量
         */
        @Valid
        private List<CfgRuleSalesFormulaDTO.DynamicUpdateDTO> dynamicSalesQtyList;

        /**
         * 固定日销量
         */
        @Valid
        private List<CfgRuleSalesFormulaDTO.FixedUpdateDTO> fixedSalesQtyList;

        /**
         * 销量去噪
         */
        @Valid
        private List<CfgRuleSalesDenoisingDTO.UpdateDTO> salesDenoisingList;

        public static CfgRuleCalcEntity buildCfgRuleCalcEntity(AddDTO addDTO) {
            CfgRuleCalcEntity entity = new CfgRuleCalcEntity();
            entity.setStartCalcDate(addDTO.getStartCalcDate());
            entity.setEndCalcDate(addDTO.getEndCalcDate());
            entity.setSaleType(addDTO.getSaleType());
            entity.setName(addDTO.getName());
            entity.setSkuJson(JSONUtil.parseArray(addDTO.getSkuIds()));
            entity.setShopJson(JSONUtil.parseArray(addDTO.getShopIds()));
            entity.setFileUrl(addDTO.getFileUrl());
            return entity;
        }

    }

    /**
     * 下载
     */
    @Getter
    @Setter
    public static class DownloadDTO {
        /**
         * sku_id
         */
        @Size(min = 1, message = "sku不能为空")
        private List<String> skuIds;

        /**
         * 试算日期
         */
        @NotNull(message = "开始试算日期不能为空")
        private LocalDate startCalcDate;

        /**
         * 店铺json
         */
        @Size(min = 1, message = "店铺不能为空")
        private List<String> shopIds;
    }

    @Getter
    @Setter
    public static class HistorySaleDTO {
        /**
         * SKU
         */
        private String skuId;
        /**
         * SKU
         */
        private String skuNo;
        /**
         * 平台
         */
        private String platform;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 店铺
         */
        private String shopName;
        /**
         * 日期
         */
        private LocalDate billDate;
        /**
         * 数量
         */
        private Integer qty;

    }

    @Getter
    @Setter
    public static class HistorySaleImportDTO {
        /**
         * SKU
         */
        @ExcelProperty(value = "SKU", index = 0)
        @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 32)
        private String skuNo;
        /**
         * 平台
         */
        @ExcelProperty(value = "平台", index = 1)
        @FieldValid(fieldName = "平台", isNotBlank = true, maxLength = 32)
        private String platform;
        /**
         * 店铺
         */
        @ExcelProperty(value = "店铺", index = 2)
        @FieldValid(fieldName = "店铺", isNotBlank = true, maxLength = 32)
        private String shopName;

        /**
         * 日期
         */
        @ExcelProperty(value = "日期", index = 3)
        @FieldValid(fieldName = "日期", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DATE)
        private String billDate;

        /**
         * 数量
         */
        @ExcelProperty(value = "数量", index = 0)
        @FieldValid(fieldName = "数量", isNotBlank = true, maxLength = 32)
        private String qty;

        /**
         * 错误数据
         */
        private String errorMsg;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class GroupDTO {
        /**
         * SKU
         */
        private String skuId;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 日期
         */
        private LocalDate billDate;

    }
}