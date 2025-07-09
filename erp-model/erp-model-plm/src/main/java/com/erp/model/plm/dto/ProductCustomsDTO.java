package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO{
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
        private String customsCode;

        /**
         * 税率
         */
        private BigDecimal taxRate;

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
        private String customsCode;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * CustomsTypeEnum 海关类型：出关 exitCustoms 清关 clearanceCustoms 默认：清关
         */
        private String type;
        private String typeName;

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


        /**
         * 报关中文名
         */
        private String declareChineseName;




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

    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class AddDTO extends CommonDTO{


    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class UpdateDTO extends CommonDTO{

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
    }


}
