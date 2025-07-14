package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ProductCustomsDTO {
    /**
     * id
     */
    private String id;

    /**
     * skuId
     */
    private String skuId;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 国家
     */
    private String country;

    /**
     * 国家
     */
    private String countryName;

    /**
     * 海关编码
     */
    @Size(max = 255, message = "海关编码最大255字符")
    private String customsCode;

    /**
     * 税率
     */
    @Digits(integer = 16,fraction = 2,message = "税率最大16字符，小数位不能大于2个字符")
    private BigDecimal taxRate;

    /**
     * 海关类型：出关 exitCustoms 清关 clearanceCustoms 默认：清关
     * 获取地址：plm/common/enumDropDown?type=CustomsType
     */
    private String type;

    /**
     * 目的国申报价
     */
    private BigDecimal toDeclarePrice;
    /**
     * 目的国申报币种
     */
    private String toCurrency;
    /**
     * 货币符号
     */
    private String toCurrencySymbol;



    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        private String SkuId;
        /**
         * sku
         */
        private String SkuNo;
        /**
         * 产品图片
         */
        private String imageUrl;
        /**
         * 报关中文名
         */
        private String declareName;

        private List<ProductCustomsDTO.ViewDetailDTO> detailDTOList;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDetailDTO{
        /**
         * 主键id
         */
        private String id;

        /**
         * SKU
         */
        private String skuId;

        private String skuNo;

        private String imagesUrl;

        private String declareName;

        /**
         * 国家
         */
        private String country;

        private String countryName;

        /**
         * 目的国申报币种
         */
        private String toCurrency;
        /**
         * 货币符号
         */
        private String toCurrencySymbol;

        /**
         * 目的国清关英文名
         */
        private String destinationCustomsEnName;

        /**
         * 目的国申报价
         */
        private BigDecimal toDeclarePrice;

        /**
         * 目的国海关编码
         */
        private String destinationCustomsCode;

        /**
         * 目的国关税税率%
         */
        private BigDecimal taxRate;

        /**
         * 目的国增值税税率%
         */
        private BigDecimal destinationVatRate;

        /**
         * 目的国附加关税税率%
         */
        private BigDecimal destinationAdditionalDutyRate;

        /**
         * 目的国反倾销税税率%
         */
        private BigDecimal destinationAntiDumpingDutyRate;

        /**
         * 目的国其他税率%
         */
        private BigDecimal destinationOtherTaxRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        private String tabFlag;

        private String tabFlagName;

        private Integer count;
    }

    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO {
        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 产品图片
         */
        private String imageUrl;

        /**
         * 报关中文名
         */
        private String declareChineseName;

        /**
         * 中国海关编码
         */
        private String chinaCustomsCode;

        /**
         * 国家
         */
        private String country;

        /**
         * 国家
         */
        private String countryName;

        /**
         * 目的国清关英文名
         */
        private String destinationCustomsEnName;

        /**
         * 目的国申报价
         */
        private BigDecimal toDeclarePrice;

        /**
         * 目的国海关编码
         */
        private String destinationCustomsCode;

        /**
         * 目的国关税税率%
         */
        private BigDecimal taxRate;

        /**
         * CustomsTypeEnum 海关类型：出关 exitCustoms 清关 clearanceCustoms 默认：清关
         */
        private String type;

        private String typeName;

        /**
         * 目的国申报币种
         */
        private String toCurrency;
        /**
         * 货币符号
         */
        private String toCurrencySymbol;

        /**
         * 目的国增值税税率%
         */
        private BigDecimal destinationVatRate;

        /**
         * 目的国附加关税税率%
         */
        private BigDecimal destinationAdditionalDutyRate;

        /**
         * 目的国反倾销税税率%
         */
        private BigDecimal destinationAntiDumpingDutyRate;

        /**
         * 目的国其他税率%
         */
        private BigDecimal destinationOtherTaxRate;

        /**
         *报关型号
         */
        private String declareModel;
        /**
         *申报要素
         */
        private String declareElement;

    }

    @Data
    @NoArgsConstructor
    public static class BaseDTO {
        /**
         * id
         */
        private String id;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 主键id
         */
        private List<String> ids;

        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * SKU
         */
        private String skuId;

        /**
         * 国家
         */
        @NotBlank(message = "国家不能为空")
        private String country;

        /**
         * 目的国申报币种
         */
        private String toCurrency;
        /**
         * 货币符号
         */
        private String toCurrencySymbol;

        /**
         * 目的国清关英文名
         */
        private String destinationCustomsEnName;

        /**
         * 目的国申报价
         */
        @NotNull(message = "目的国申报价不能为空")
        @Min(value = 0, message = "目目的国申报价不能为负数")
        private BigDecimal toDeclarePrice= BigDecimal.ZERO;;

        /**
         * 目的国海关编码
         */
        private String destinationCustomsCode;

        /**
         * 目的国关税税率%
         */

        @Min(value = 0, message = "目的国关税税率不能为负数")
        @Max(value = 100, message = "目的国关税税率不能超过100")
        @Digits(integer = 3, fraction = 2, message = "目的国关税税率整数位最多3位，小数位最多2位")
        private BigDecimal taxRate= BigDecimal.ZERO;;

        /**
         * 目的国增值税税率%
         */
        @Min(value = 0, message = "目的国增值税税率不能为负数")
        @Max(value = 100, message = "目的国增值税税率不能超过100")
        @Digits(integer = 3, fraction = 2, message = "目的国增值税税率整数位最多3位，小数位最多2位")
        private BigDecimal destinationVatRate= BigDecimal.ZERO;;

        /**
         * 目的国附加关税税率%
         */
        @Min(value = 0, message = "目的国附加关税税率不能为负数")
        @Max(value = 100, message = "目的国附加关税税率不能超过100")
        @Digits(integer = 3, fraction = 2, message = "目的国附加关税税率整数位最多3位，小数位最多2位")
        private BigDecimal destinationAdditionalDutyRate= BigDecimal.ZERO;;

        /**
         * 目的国反倾销税税率%
         */
        @Min(value = 0, message = "目的国反倾销税税率不能为负数")
        @Max(value = 100, message = "目的国反倾销税税率不能超过100")
        @Digits(integer = 3, fraction = 2, message = "目的国反倾销税税率整数位最多3位，小数位最多2位")
        private BigDecimal destinationAntiDumpingDutyRate= BigDecimal.ZERO;;

        /**
         * 目的国其他税率%
         */
        @Min(value = 0, message = "目的国其他税率不能为负数")
        @Max(value = 100, message = "目的国其他税率不能超过100")
        @Digits(integer = 3, fraction = 2, message = "目的国其他税率整数位最多3位，小数位最多2位")
        private BigDecimal destinationOtherTaxRate = BigDecimal.ZERO;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO{

        /**
         * sku
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        @NotEmpty(message = "列表不能为空")
        private List<ProductCustomsDTO. @Valid CommonDTO> detailDTOList;


    }

    @Data
    @NoArgsConstructor
    public static class AddListDTO {

        @NotEmpty(message = "列表不能为空")
        private List<ProductCustomsDTO. @Valid AddDTO> list;
    }


}
