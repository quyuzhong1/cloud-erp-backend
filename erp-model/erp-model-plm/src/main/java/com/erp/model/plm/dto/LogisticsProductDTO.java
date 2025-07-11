package com.erp.model.plm.dto;/**
 * @author Lambda
 * @Classname LogisticsProductDTO
 * @Description TODO
 * @Date 2023-11-06 14:05
 * @Created by yl
 */

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.utils.LengthConverterUtil;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-06 14:05
 */
@Data
@NoArgsConstructor
public class LogisticsProductDTO implements Serializable {


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
         * 物流产品信息id
         */
        private String logisticsProductId;

        /**
         * 出口申报价
         */
        private BigDecimal declarePrice;

        /**
         * 出口申报价币种
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
         * 备案审核状态
         */
        private String logisticsApproveStatus;

        /**
         * 备案审核状态名称
         */
        private String logisticsApproveStatusName;

        /**
         * 第一数量
         */
        private BigDecimal firstQty;

        /**
         * 第二数量
         */
        private BigDecimal secondQty;

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
    public static class TabListDTO {
        /**
         * 类型(toBeApprove 待审核，reject 审核不通过，approve 已审核)
         */
        private String tabFlag;

        /**
         * tab名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

//        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class UpdatePagingParamDTO extends SortDTO {
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
         * 操作项
         */
        private String operate;

        /**
         * 操作内容
         */
        private String operateContent;
        /**
         * 操作人
         */
        private String operateUserId;

        /**
         * 操作时间
         */
        private List<LocalDate> operateTimeList;

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

    }


    @Data
    @NoArgsConstructor
    public static class UpdatePagingDTO {


        private String id;

        private String skuId;

        private String skuNo;

        /**
         * spu
         */
        private String spuNo;


        /**
         * 品名
         */
        private String productName;

        /**
         * 操作项
         */
        private String operate;

        /**
         * 操作内容
         */
        private String operateContent;
        /**
         * 操作人
         */
        private String operateUserName;

        /**
         * 操作时间
         */
        private LocalDateTime operateTime;

    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * tab
         */
        private String tabFlag;
        /**
         * id集合
         */
        private List<String> idList;

        /**
         * 审核状态集合（无需传值）
         */
        private List<String> approveStatusList;

        /**
         * 备案审核状态集合,/api/scm/drop/down/approveStatus/list
         */
        private List<String> logisticsApproveStatusList;

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

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

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

        /**
         * 报关信息-汇率
         */
        private BigDecimal exchangeRate;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO {


        /**
         * 报关信息
         */
        @Valid
        private DeclareInfoDTO declareInfo;
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
         * 判断是否含电 true 展示输入输出电池电压功率  false不展示入输出电池电压功率
         */
        private boolean electric;
        /**
         * 输入参数 仅展示使用
         */
        private String inputParams;
        /**
         * 输出参数 仅展示使用
         */
        private String outputParams;

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
         * 产品尺寸长
         */
        private BigDecimal productLength;
        /**
         * 产品尺寸宽
         */
        private BigDecimal productWidth;
        /**
         * 产品尺寸高
         */
        private BigDecimal productHeight;
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
        @Size(max = 50, message = "中文报关名最大100字符")
        private String declareChineseName;

        /**
         * 英文报关名
         */
        @Size(max = 50, message = "英文报关名最大100字符")
        private String declareEnglishName;

        /**
         * 报关型号
         */
        @Size(max = 50, message = "报关型号最大100字符")
        private String declareModel;


        /**
         * 出口申报价
         */
        private BigDecimal declarePrice;

        /**
         * 出口申报价币种
         */
        private String declareCurrency;

        /**
         * 出口申报价币种符号
         */
        private String declareCurrencySymbol;


        /**
         * 报关单位,/api/plm/dict/list?type=declareUnit
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
        @Size(max = 50, message = "境内货源地最大50字符")
        private String sourceCargo;


        /**
         * 征免
         */
        private String exemption;

        /**
         * 原产国
         * 来源 http://172.16.100.11:3002/project/36/interface/api/13390
         */
        @Size(max = 50, message = "原产国最大50字符")
        private String sourceCountry;

        /**
         * 原产国中文名
         */
        private String sourceCountryName;


        /**
         * 组合品申报类型
         * split 拆分
         * combine 合并
         */
        private String combinationDeclareType;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
        /**
         * 审核人名称
         */
        private String approveUserName;
        /**
         * 审核人id
         */
        private String approveUserId;

        /**
         * 备案审核状态
         */
        private String logisticsApproveStatus;

        /**
         * 备案审核状态名称
         */
        private String logisticsApproveStatusName;

        /**
         * 第一数量
         */
        private BigDecimal firstQty;

        /**
         * 第二数量
         */
        private BigDecimal secondQty;
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        private List<String> ids;
    }


    @Getter
    @Setter
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
         * EAN 吗
         */
        private String ean;

        /**
         * 不含税成本
         */
        private BigDecimal actualNoTaxCost;

        /**
         * 含税成本
         */
        private BigDecimal actualTaxCost;

        /**
         * 产品尺寸长
         */
        private BigDecimal productLength;
        /**
         * 产品尺寸宽
         */
        private BigDecimal productWidth;
        /**
         * 产品尺寸高
         */
        private BigDecimal productHeight;
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


        private String destCurrencySymbol;


        /**
         * 申报要素
         */
        private String declareElement;


        /**
         * 境内货源地
         */
        @Size(max = 50, message = "境内货源地最大50字符")
        private String sourceCargo;


        /**
         * 征免
         */
        private String exemption;

        /**
         * 原产国
         */
        @Size(max = 50, message = "原产国最大50字符")
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


        /**
         * 属性id
         */
        private String propertyId;
        /**
         * 物流属性 对应属性
         */
        private String logisticsPropertyName;

        /**
         * 单据审核状态
         */
        private String logisticsApproveStatus;
        /**
         * 单据审核状态
         */
        private String logisticsApproveStatusName;

        /**
         * 第一数量
         */
        private BigDecimal firstQty;

        /**
         * 第二数量
         */
        private BigDecimal secondQty;

        public void setProductLength(BigDecimal productLength) {
            this.productLength = LengthConverterUtil.mmToCm(productLength);
        }

        public void setProductWidth(BigDecimal productWidth) {
            this.productWidth = LengthConverterUtil.mmToCm(productWidth);
        }

        public void setProductHeight(BigDecimal productHeight) {
            this.productHeight = LengthConverterUtil.mmToCm(productHeight);
        }
    }


    /**
     * 产品信息
     */
    @Data
    @NoArgsConstructor
    public static class ProductDTO {

        /**
         * 备案审核状态
         */
        private String approveStatus;

        /**
         * 第一数量
         */
        private BigDecimal firstQty;

        /**
         * 第二数量
         */
        private BigDecimal secondQty;

        /**
         * 长
         */
        private BigDecimal boxSizeLength;

        /**
         * 宽
         */
        private BigDecimal boxSizeWide;
        /**
         * 高
         */
        private BigDecimal boxSizeHigh;

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
         * 产品属性
         */
        private String productProperty;
        /**
         * 子订单id
         */
        private Long childOrderId;

        private String imagesUrl;

        private BigDecimal amount;

       private String spuNo;
        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 出口申报价/报关申报价
         */
        private BigDecimal declarePrice;

        /**
         * 毛重
         */
        private BigDecimal grossWeight;

        private Integer weight;

        /**
         * 是否带电
         */
        private Boolean isElectric;
        /**
         * 是否纯电
         */
        private Boolean onlyBattery;

        /**
         * 是否液体
         */
        private Boolean isLiquid;


        /**
         * 是否带电
         */
        private String batteryType;

        private String skuId;

        private String skuNo;


        /**
         * 属性id
         */
        private String productPropertyId;

        /**
         * 报关型号
         */
        private String declareModel;

        /**
         * 报关中文名
         */
        private String declareChineseName;

        /**
         * 报关英文名
         */
        private String declareEnglishName;

        /**
         * 产品中文品名
         */
        private String cnName;

        /**
         * 产品英文品名
         */
        private String enName;

        /**
         * 海关编码
         */
        private String customsCode;

        /**
         * 报关单位
         */
        private String declareUnit;

        /**
         * 报关单位名称
         */
        private String declareUnitName;
        /**
         * 申报要素
         */
        private String declareElement;

        /**
         * 英文材质
         */
        private String englishMaterial;

        /**
         * 英文用途
         */
        private String englishUsage;


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
         * 目的国币种
         */
        private String destCurrency;


        /**
         * 目的国币种符号
         */
        private String destCurrencySymbol;

        /**
         * 征免
         */
        private String exemption;

        /**
         * 境内货源地
         */
        private String sourceCargo;


        /**
         * 原产国
         */
        private String sourceCountry;

        /**
         * 组合品申报类型
         */
        private String combinationDeclareType;

        /**
         * 不含税成本
         */
        private BigDecimal actualNoTaxCost;

        /**
         * 含税成本
         */
        private BigDecimal actualTaxCost;

    }


    @Data
    @NoArgsConstructor
    public static class PushRegistrationDTO{

        /**
         * 物流产品id集合
         */
        @NotEmpty(message = "物流产品id集合不能为空")
        private List<String> logisticsProductIdList;

        /**
         * 报关物流商集合，/tms/transferLogisticsSupplier/listAll
         */
        @NotEmpty(message = "报关物流商Id集合不能为空")
        private String declareSupplierId;
    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编码
         */
        private String skuNo;
    }
}
