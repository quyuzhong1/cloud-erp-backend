package com.erp.model.srm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.utils.MathUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 采购对账单明细已对账信息请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-12-22
*/
@Data
@NoArgsConstructor
public class PoReconciliationRefDetailDTO implements Serializable {


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
         * 供应商Id
         */
        private String supplierId;

        /**
         * 是否是srm
         */
        private Boolean isSrm = Boolean.FALSE;
    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 序号
         */
        private Integer index;

        /**
         * 对账明细主键id
         */
        private String id;

        /**
         * 采购对账单id
         */
        private String poReconciliationId;
        /**
         * 采购对账单明细id
         */
        private String poReconciliationDetailId;

        /**
         * 单据单号【可排序】
         */
        private String sourceCode;

        /**
         * 采购单Id【可排序
         */
        private String poId;

        /**
         * 采购单号【可排序】
         */
        private String poCode;
        /**
         * 采购单来源单号类型
         */
        private String poSourceType;

        /**
         * 采购单来源单号
         */
        private String poSourceCode;

        /**
         * 单据类型【可排序】
         */
        private String sourceType;

        /**
         * 退货类型【可排序】
         */
        private String returnSourceType;

        /**
         * 单据类型名称
         */
        private String sourceTypeName;

        /**
         * 供应商Id【可排序】
         */
        private String supplierId;


        /**
         * 供应商名称【可排序】
         */
        private String supplierName;

        /**
         * 业务状态【可排序】
         */
        private String businessStatus;

        /**
         * 业务状态名称
         */
        private String businessStatusName;

        /**
         * 单据日期【可排序】
         */
        private LocalDate date;

        /**
         * SKUId【可排序】
         */
        private String skuId;

        /**
         * SKU【可排序】
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 本期对账数量【可排序】
         */
        private Integer qty;
        /**
         * 单据数量（待对账明细数量）
         */
        private Integer billQty;
        /**
         * 已对账数量
         */
        private Integer reconciledQty;
        /**
         * 单位
         */
        private String unitName;

        /**
         * 税率【可排序】
         */
        private BigDecimal taxRate;

        /**
         * 税率（%）
         */
        private String taxRateStr;

        /**
         * 含税单价【可排序】
         */
        private BigDecimal taxPrice;

        /**
         * 价税合计【可排序】
         */
        private BigDecimal taxAmount;

        /**
         * 币别【可排序】
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 结算组织名称【可排序】
         */
        private String settleOrgName;

        /**
         * 结算方式【可排序】
         */
        private String settleDict;

        /**
         * 结算方式名称
         */
        private String settleDictName;

        /**
         * 付款条件【可排序】
         */
        private String paymentCondition;

        /**
         * 付款条件名称
         */
        private String paymentConditionName;

        /**
         * 供应商备注
         */
        private String supplierRemark;

        /**
         * 采购备注
         */
        private String purchaseRemark;

        /**
         * 单据备注
         */
        private String remark;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 对账状态，poReconciliationDetailStatus字典
         */
        private String status;
        /**
         * 对账状态名称
         */
        private String statusName;
        /**
         * 送货单id
         */
        private String deliveryId;
        /**
         * 送货单明细id
         */
        private String deliveryDetailId;
        /**
         * 送货编码
         */
        private String deliveryCode;
        /**
         * 折扣税率
         */
        private BigDecimal discountRate;
        /**
         * 折扣税率,%
         */
        private String discountRateStr;
        /**
         * 折扣额
         */
        private BigDecimal discountAmount;
        /**
         * 预付金额
         */
        private BigDecimal prepayAmount;
        /**
         * 税价合计（折扣）
         */
        private BigDecimal discountTaxAmount;

        /**
         * 采购申请单id集合
         */
        @JsonIgnore
        private List<String> purchaseApplicationIds;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 采购对账单id
         */
        @NotBlank(message = "采购对账单id不能为空")
        private String poReconciliationId;

        /**
         * 采购对账单明细id
         */
        @NotBlank(message = "采购对账单明细id不能为空")
        private String poReconciliationDetailId;

        /**
         * 供方备注
         */
        private String supplierRemark;

        /**
         * 本期对账数量
         */
        @NotNull(message = "本期对账数量不能为空")
        private Integer qty;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class ScmUpdateDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 采购对账单id
         */
        @NotBlank(message = "采购对账单id不能为空")
        private String poReconciliationId;

        /**
         * 采购对账单明细id
         */
        @NotBlank(message = "采购对账单明细id不能为空")
        private String poReconciliationDetailId;


        /**
         * 本期对账数量
         */
        @NotNull(message = "本期对账数量不能为空")
        private Integer qty;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 折扣率
         */
        private BigDecimal discountRate;

        /**
         * 预付金额
         */
        private BigDecimal prepayAmount;

        /**
         * 采方备注
         */
        private String purchaseRemark;
    }

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
         * 采购对账单id
         */
        private String poReconciliationId;
        /**
         * 采购对账单明细id
         */
        private String poReconciliationDetailId;


        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源订单号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 单据类型名称
         */
        private String sourceTypeName;

        /**
         * 采购订单编码
         */
        private String poCode;

        /**
         * 采购订单id
         */
        private String poId;
        /**
         * 采购订单来源单号
         */
        private String poSourceCode;

        /**
         * 单据日期
         */
        private LocalDate date;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 本期对账数量
         */
        private Integer qty;
        /**
         * 单据数量（待对账明细数量）
         */
        private Integer billQty;
        /**
         * 已对账数量
         */
        private Integer reconciledQty;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 税率（%）
         */
        private String taxRateStr;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 价税合计
         */
        private BigDecimal taxAmount;

        /**
         * 结算组织id
         */
        private String settleOrgId;

        /**
         * 结算组织名称
         */
        private String settleOrgName;

        /**
         * 结算方式
         */
        private String settleDict;

        /**
         * 结算方式名称
         */
        private String settleDictName;

        /**
         * 付款条件
         */
        private String paymentCondition;

        /**
         * 付款条件名称
         */
        private String paymentConditionName;

        /**
         * 业务状态
         */
        private String businessStatus;

        /**
         * 业务状态名称
         */
        private String businessStatusName;

        /**
         * 供方备注
         */
        private String supplierRemark;

        /**
         * 采方备注
         */
        private String purchaseRemark;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 对账状态，poReconciliationDetailStatus字典
         */
        private String status;
        /**
         * 送货单id
         */
        private String deliveryId;
        /**
         * 送货单明细id
         */
        private String deliveryDetailId;
        /**
         * 送货编码
         */
        private String deliveryCode;
        /**
         * 折扣税率
         */
        private BigDecimal discountRate;
        /**
         * 折扣额
         */
        private BigDecimal discountAmount;
        /**
         * 预付金额
         */
        private BigDecimal prepayAmount;
        /**
         * 税价合计（折扣）
         */
        private BigDecimal discountTaxAmount;
        /**
         * 单据备注
         */
        private String remark;

        /**
         * 折扣额
         */
        public BigDecimal getDiscountAmount () {
            return MathUtil.multiplyWithFour(discountRate,discountRate);
        }
    }
}