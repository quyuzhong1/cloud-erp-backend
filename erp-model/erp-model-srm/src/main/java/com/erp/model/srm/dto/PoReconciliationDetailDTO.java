package com.erp.model.srm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.utils.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
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
         * 数量【可排序】
         */
        private Integer qty;

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
        * 数量
        */
        private Integer qty;

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

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class ScmUpdateDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

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
        @Size(max = 32,message = "采购订单编码最大长度不能超过32位")
        private String poCode;

        /**
        * 采购订单id
        */
        @Size(max = 19,message = "采购订单id最大长度不能超过19位")
        private String poId;

        /**
         * 采购订单详情id
         */
        @Size(max = 19,message = "采购订单id最大长度不能超过19位")
        private String poDetailId;

        /**
        * 单据日期
        */
        private LocalDate date;

        /**
        * skuId
        */
        @NotBlank(message = "SKUid不能为空")
        private String skuId;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 含税单价
        */
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxPrice;

        /**
        * 结算组织id
        */
        @Size(max = 19,message = "结算组织id最大长度不能超过19位")
        private String settleOrgId;

        /**
        * 业务状态
        */
        @NotBlank(message = "业务状态不能为空")
        @Size(max = 32,message = "业务状态最大长度不能超过32位")
        private String businessStatus;

        /**
        * 币别
        */
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;

        /**
         * 退货来源
         */
        private String returnSourceType;
        /**
         * 送货单id
         */
        private String deliveryId;
        /**
         * 送货明细id
         */
        private String deliveryDetailId;
        /**
         * 送货单编码
         */
        private String deliveryCode;

        /**
         * 单据备注
         */
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class AddPoReconciliationViewDTO {

        /**
         * 对账单id
         */
        private String id;

        /**
         * 对账单编号
         */
        private String code;

    }

        @Data
    @NoArgsConstructor
    public static class GeneratePoReconciliationDTO {

        /**
         * 选择明细id集合
         */
        @NotEmpty(message = "选择明细id集合不能未空")
        private List<String> detailIdList;

        /**
         * 对账账单类型，/srm/drop/down/dict/list?key=poReconciliationGenerateType
         */
        @NotBlank(message = "对账单账单不能为空")
        private String generateType;

        /**
         * 对账周期
         */
        private List<LocalDate> reconciliationDateList;

        /**
         * 对账单id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateBusinessStatusDTO {

        /**
         * 来源id集合
         */
        @NotEmpty(message = "来源id集合不能为空")
        private List<String> sourceIdList;

        /**
         * 业务状态
         */
        @NotBlank(message = "业务状态不能为空")
        private String businessStatus;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeDTO {

        /**
         * 入库/退货单号
         */
        @NotEmpty(message = "入库/退货单号不能为空")
        private List<String> codeList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateParamDTO {
        /**
         * 编码
         */
        private String code;
        /**
         * 明细id
         */
        private String detailId;

        public GenerateParamDTO (String code) {
            this.code = code;
        }
    }


        /**
     * 添加设置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddSettingDTO {

        /**
         * 付款条件集合
         */
        @NotEmpty(message = "付款条件不能为空")
        private List<String> paymentConditionList;

    }


    /**
     * 导入质
     */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {

        /**
         * 成功返回数据
         */
        private List<PoReconciliationDetailDTO.ViewDTO> successList;

        /**
         * 错误的url
         */
        private String errorUrl;
    }
}