package com.erp.model.srm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 采购对账单明细请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-01-19
*/
@Data
@NoArgsConstructor
public class PoReconciliationDetailDTO implements Serializable {


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

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 对账明细主键id
         */
        private String id;

        /**
         * 单据单号【可排序】
         */
        private String sourceCode;

        /**
         * 采购单号【可排序】
         */
        private String poCode;

        /**
         * 单据类型【可排序】
         */
        private String sourceType;

        /**
         * 单据类型名称
         */
        private String sourceTypeName;

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
         * 确认日期【可排序】
         */
        private LocalDate confirmDate;

        /**
         * SKU【可排序】
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 送货数量【可排序】
         */
        private String deliveryQty;

        /**
         * 收货数量【可排序】
         */
        private String receiveQty;

        /**
         * 税率【可排序】
         */
        private String taxRate;

        /**
         * 含税单价【可排序】
         */
        private String taxPrice;

        /**
         * 价税合计【可排序】
         */
        private String taxAmount;

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
         * 是否加入账单(true是，flase否)【可排序】
         */
        private Boolean isAddAccount;
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
        * 供应商id
        */
        private String supplierId;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 对账单Id
        */
        private String mainId;

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
        * 采购订单编码
        */
        private String poCode;

        /**
        * 采购订单id
        */
        private String poId;

        /**
        * 确认日期
        */
        private LocalDate confirmDate;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku编码
        */
        private String skuNo;

        /**
        * 送货数量
        */
        private Integer deliveryQty;

        /**
        * 收货数量
        */
        private Integer receiveQty;

        /**
        * 税率
        */
        private BigDecimal taxRate;

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
        * 付款条件
        */
        private String paymentCondition;

        /**
        * 业务状态
        */
        private String businessStatus;

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
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 是否加入账单
        */
        private Boolean isAddAccount;


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
    public static class UpdateDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 供方备注
         */
        private String supplierRemark;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
        * 供应商名称
        */
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 100,message = "供应商名称最大长度不能超过100位")
        private String supplierName;

        /**
        * 对账单Id
        */
        @NotBlank(message = "对账单Id不能为空")
        @Size(max = 19,message = "对账单Id最大长度不能超过19位")
        private String mainId;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源订单号
        */
        @NotBlank(message = "来源订单号不能为空")
        @Size(max = 64,message = "来源订单号最大长度不能超过64位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 采购订单编码
        */
        @NotBlank(message = "采购订单编码不能为空")
        @Size(max = 32,message = "采购订单编码最大长度不能超过32位")
        private String poCode;

        /**
        * 采购订单id
        */
        @NotBlank(message = "采购订单id不能为空")
        @Size(max = 19,message = "采购订单id最大长度不能超过19位")
        private String poId;

        /**
        * 确认日期
        */
        private LocalDate confirmDate;

        /**
        * skuId
        */
        private String skuId;

        /**
        * 送货数量
        */
        @NotNull(message = "送货数量不能为空")
        private Integer deliveryQty;

        /**
        * 收货数量
        */
        @NotNull(message = "收货数量不能为空")
        private Integer receiveQty;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxRate;

        /**
        * 含税单价
        */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxPrice;

        /**
        * 价税合计
        */
        @NotNull(message = "价税合计不能为空")
        @Digits(integer = 12, fraction = 4, message = "价税合计整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxAmount;

        /**
        * 结算组织id
        */
        @NotBlank(message = "结算组织id不能为空")
        @Size(max = 19,message = "结算组织id最大长度不能超过19位")
        private String settleOrgId;

        /**
        * 结算组织名称
        */
        @NotBlank(message = "结算组织名称不能为空")
        @Size(max = 100,message = "结算组织名称最大长度不能超过100位")
        private String settleOrgName;

        /**
        * 结算方式
        */
        @NotBlank(message = "结算方式不能为空")
        @Size(max = 32,message = "结算方式最大长度不能超过32位")
        private String settleDict;

        /**
        * 付款条件
        */
        @NotBlank(message = "付款条件不能为空")
        @Size(max = 32,message = "付款条件最大长度不能超过32位")
        private String paymentCondition;

        /**
        * 业务状态
        */
        @NotBlank(message = "业务状态不能为空")
        @Size(max = 32,message = "业务状态最大长度不能超过32位")
        private String businessStatus;

        /**
        * 供方备注
        */
        @NotBlank(message = "供方备注不能为空")
        @Size(max = 255,message = "供方备注最大长度不能超过255位")
        private String supplierRemark;

        /**
        * 采方备注
        */
        @NotBlank(message = "采方备注不能为空")
        @Size(max = 255,message = "采方备注最大长度不能超过255位")
        private String purchaseRemark;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 是否加入账单
        */
        @NotNull(message = "是否加入账单不能为空")
        private Boolean isAddAccount;


    }


}