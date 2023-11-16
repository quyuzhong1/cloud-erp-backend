package com.erp.model.plm.dto;/**
 * @author Lambda
 * @Classname LogisticsProductDTO
 * @Description TODO
 * @Date 2023-11-06 14:05
 * @Created by yl
 */

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-06 14:05
 */
@Data
@NoArgsConstructor
public class LogisticsProductDTO {


    @Data
    @NoArgsConstructor
    public static class PagingVO {

        /**
         * sku id
         */
        private String id;

        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 分类名
         */
        private String categoryName;


        /**
         * sku no
         */
        private String skuNo;


        /**
         * sku id
         */
        private String skuId;


        /**
         * spu
         */
        private String spuNo;

        /**
         * 产品名称
         */
        private String productName;
        /**
         * 中文报关名
         */
        private String declareChineseName;
        /**
         * 产品属性
         */
        private String productPropertyName;


        /**
         * 报关申报价
         */
        private BigDecimal declarePrice;

        /**
         * 报关申报价币种
         */
        private String declareCurrency;

        /**
         * 报关申报价币种符号
         */
        private String declareCurrencySymbol;

        /**
         * 目的国申报价
         */
        private BigDecimal destDeclarePrice;

        /**
         * 目的国申报价币种
         */
        private String destCurrency;

        /**
         * 目的国申报价币种符号
         */
        private String destCurrencySymbol;

        /**
         * 申报要素
         */
        private String declareElement;


        /**
         * 报关型号
         */
        private String declareModel;

        /**
         * 中国海关编码（报关HSCODE）
         */
        private String customsCode;


        /**
         * 原产国
         */
        private String sourceCountry;

        /**
         * sku 审核状态
         */
        private Integer approveStatus;

        /**
         * sku 审核状态名
         */
        private String approveStatusName;

        /**
         * 销售状态
         */
        private Integer salesStatus;

        /**
         * 销售状态名
         */
        private String salesStatusName;

        /**
         * 品牌名
         */
        private String brandName;

        /**
         * 产品经理
         */
        private String chargeName;

        /**
         * 产品经理id
         */
        private String chargeId;

        /**
         * 是否组合品
         */
        private Boolean isCombination;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;


    }


    @Data
    @NoArgsConstructor
    public static class TabListDTO{
        /**
         * 数量
         */
        private Integer count;

        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class UpdatePagingParamDTO extends SortDTO{
        /**
         * skulist
         */
        private List<String> skuNoList;

        /**
         * spu
         */
        private String spuNo;


        /**
         * 品名
         */
        private String productName;

        /**
         *  操作项
         */
        private String operate;

        /**
         *  操作内容
         */
        private String operateContent;
        /**
         *  操作人
         */
        private String operateUserId;

        /**
         *  操作时间
         */
        private List<LocalDate> operateTimeList;

    }


    @Data
    @NoArgsConstructor
    public static class UpdatePagingDTO {


        private String id;

        private String skuId;

        private String  skuNo;

        /**
         * spu
         */
        private String spuNo;


        /**
         * 品名
         */
        private String productName;

        /**
         *  操作项
         */
        private String operate;

        /**
         *  操作内容
         */
        private String operateContent;
        /**
         *  操作人
         */
        private String operateUserName;

        /**
         *  操作时间
         */
        private LocalDateTime operateTime;

    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 分类id
         */
        private List<String> categoryIdList;

        /**
         * skulist
         */
        private List<String> skuNoList;


        /**
         * 品名
         */
        private String productName;

        /**
         * 中文报关名
         */
        private String declareNameCn;

        /**
         * 产品属性id
         */
        private List<String> productPropertyIdList;

        /**
         * 原产国
         */
        private String sourceCountry;

        /**
         * 产品经理
         */
        private List<String> productChargeIdList;

        /**
         * spu
         */
        private String spuNo;


        /**
         * 报关型号
         */
        private String declareModel;

        /**
         * 申报要素
         */
        private String declareElement;

        /**
         * 销售状态
         */
        private List<Integer> salesStatusList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 更新时间
         */
        private List<LocalDate> updateTimeList;


    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        /**
         * 产品基本信息
         */
        private ProductBaseInfoDTO productBaseInfo;


        /**
         * 报关信息
         */
        private DeclareInfoDTO declareInfo;


        /**
         * 目的国海关编码
         */
        private List<ProductCustomsDTO.ViewDTO> customsList;


    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO {



        /**
         * 报关信息
         */
        @Valid
        private DeclareInfoDTO declareInfo;


        /**
         * 目的国海关编码
         */
        private List<ProductCustomsDTO.ViewDTO> customsList;


    }


    /**
     * 产品基础信息
     */
    @Data
    @NoArgsConstructor
    public static class ProductBaseInfoDTO {

        /**
         * sku no
         */
        private String skuNo;


        /**
         * 产品图片
         */
        private String imagesUrl;


        private String categoryId;

        /**
         * 分类名
         */
        private String categoryName;

        /**
         * 销售状态
         */
        private Integer salesStatus;

        /**
         * 销售状态名
         */
        private String salesStatusName;


        /**
         * spu
         */
        private String spuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 产品经理
         */
        private String chargeName;


        /**
         * 品牌名
         */
        private String brandName;

        /**
         * 产品属性
         */
        private String propertyName;


        /**
         * 物流属性
         */
        private String logisticsPropertyName;

        /**
         * ENA 吗
         */
        private String ean;

        /**
         * 不含税成本
         */
        private String actualNoTaxCost;

        /**
         * 含税成本
         */
        private String actualTaxCost;


        /**
         * 产品尺寸
         */
        private String productSize;
        /**
         * 毛重
         */
        private BigDecimal grossWeight;

        /**
         * 净重
         */
        private BigDecimal netWeight;


        /**
         * 材质
         */
        private String materials;

        /**
         * 用途
         */
        private String usageDesc;

        /**
         * 是否组合品
         */
        private Boolean isCombination;

    }

    /**
     * 报关信息
     */
    @Data
    @NoArgsConstructor
    public static class DeclareInfoDTO {

        /**
         * id
         */
        private String id;


        /**
         * sku id
         */
        @NotBlank(message = "sku 不能为空")
        private String skuId;


        /**
         * 中文报关名
         */
        @Size(max =50,message = "中文报关名最大100字符")
        private String declareChineseName;

        /**
         * 英文报关名
         */
        @Size(max =50,message = "英文报关名最大100字符")
        private String declareEnglishName;

        /**
         * 报关型号
         */
        @Size(max =50,message = "报关型号最大100字符")
        private String declareModel;


        /**
         * 报关申报价
         */
        private BigDecimal declarePrice;

        /**
         * 报关申报价币种
         */
        private String declareCurrency;

        /**
         * 报关申报价币种符号
         */
        private String declareCurrencySymbol;



        /**
         * 报关单位
         */
        private String declareUnit;


        /**
         * 中国海关编码
         */
        private String customsCode;


        /**
         * 目的国申报价
         */
        private BigDecimal destDeclarePrice;


        /**
         * 目的国申报价币种
         */
        private String destCurrency;

        /**
         * 目的国申报价币种符号
         */
        private String destCurrencySymbol;



        /**
         * 申报要素
         */
        private String declareElement;


        /**
         * 境内货源地
         */
        @Size(max =50,message = "境内货源地最大50字符")
        private String sourceCargo;


        /**
         * 征免
         */
        private String exemption;

        /**
         * 原产国
         */
        @Size(max =50,message = "原产国最大50字符")
        private String sourceCountry;


        /**
         * 组合品申报类型
         * split 拆分
         * combine 合并
         */
        private String combinationDeclareType;


    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO{
        private List<String> ids;
    }


    @Data
    @NoArgsConstructor
    public static class ExportInfoDTO {



        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 分类名
         */
        private String categoryName;


        /**
         * sku no
         */
        private String skuNo;


        /**
         * sku id
         */
        private String skuId;



        /**
         * 产品经理
         */
        private String chargeName;


        /**
         * spu
         */
        private String spuNo;

        /**
         * 产品名称
         */
        private String productName;



        /**
         * 品牌名
         */
        private String brandName;

        /**
         * 产品属性
         */
        private String propertyName;


        /**
         * 销售状态
         */
        private Integer salesStatus;

        /**
         * 销售状态名
         */
        private String salesStatusName;

        /**
         * ENA 吗
         */
        private String ena;

        /**
         * 不含税成本
         */
        private BigDecimal actualNoTaxCost;

        /**
         * 含税成本
         */
        private BigDecimal actualTaxCost;


        /**
         * 产品尺寸
         */
        private String productSize;
        /**
         * 毛重
         */
        private BigDecimal grossWeight;

        /**
         * 净重
         */
        private BigDecimal netWeight;


        /**
         * 材质
         */
        private String materials;

        /**
         * 用途
         */
        private String usageDesc;

        /**
         * 中文报关名
         */
        private String declareChineseName;





        /**
         * 英文报关名
         */
        private String declareEnglishName;

        /**
         * 报关型号
         */
        private String declareModel;


        /**
         * 报关申报价
         */
        private String declarePrice;

        /**
         * 报关申报价 币种符号
         */
        private String declareCurrencySymbol;


        /**
         * 报关单位
         */
        private String declareUnit;


        /**
         * 中国海关编码
         */
        private String customsCode;


        /**
         * 目的国申报价
         */
        private BigDecimal destDeclarePrice;


        private String  destCurrencySymbol;




        /**
         * 申报要素
         */
        private String declareElement;


        /**
         * 境内货源地
         */
        @Size(max =50,message = "境内货源地最大50字符")
        private String sourceCargo;


        /**
         * 征免
         */
        private String exemption;

        /**
         * 原产国
         */
        @Size(max =50,message = "原产国最大50字符")
        private String sourceCountry;


        /**
         * 组合品申报类型
         * split 拆分
         * combine 合并
         */
        private String combinationDeclareType;



        /**
         * 国家
         */
        private String countryName;

        /**
         * 海关编码
         */
        private String destCustomsCode;

        /**
         * 税率
         */
        private BigDecimal taxRate;





    }
}
