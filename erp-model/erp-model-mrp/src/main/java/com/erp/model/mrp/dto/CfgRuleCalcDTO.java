package com.erp.model.mrp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import cn.hutool.json.JSONUtil;
import com.erp.model.mrp.entity.CfgRuleCalcEntity;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
         * 主键id
         */
        private String id;

        /**
         * sku_id
         */
        private String skuJson;

        /**
         * 试算日期
         */
        private LocalDateTime calcDate;

        /**
         * 店铺json
         */
        private String shopJson;

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
         * 试算配置编号
         */
        private String code;

        /**
         * 试算状态
         */
        private String status;


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
        @NotBlank(message = "文件地址不能为空")
        @Size(max = 255, message = "文件地址最大长度不能超过255位")
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

        private String skuId;

        private String skuNo;

        private String platform;

        private String shopId;

        private String shopName;

        private LocalDate billDate;

        private Integer qty;

    }
}