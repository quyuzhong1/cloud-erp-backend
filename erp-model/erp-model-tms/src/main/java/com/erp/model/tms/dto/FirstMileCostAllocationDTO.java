package com.erp.model.tms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 头程费用分摊请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
*/
@Data
@NoArgsConstructor
public class FirstMileCostAllocationDTO implements Serializable {




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
        * 备注
        */
        private String remark;

        /**
        * 对账单id
        */
        private String reconciliationId;

        /**
        * 对账单单据编号
        */
        private String reconciliationCode;

        /**
        * 物流单id
        */
        private String logisticsBillId;

        /**
        * 物流单明细id
        */
        private String logisticsBillDetailId;
        /**
         * 期初费用分摊id
         */
        private String initFirstMileId;
        /**
         * 期初费用分摊明细id
         */
        private String initFirstMileDetailId;
        /**
        * 暂估账单id
        */
        private String estimatedBillId;

        /**
        * SKU成本id
        */
        private String skuCostId;

        /**
        * SKU成本明细id
        */
        private String skuCostDetailId;

        /**
        * 重量分摊id
        */
        private String weightAllocationId;

        /**
        * 核算期间id
        */
        private String reportPeriodId;

        /**
        * 会计期间
        */
        private String accountPeriod;

        /**
        * 核算状态：waitConfirm=待确认，confirm=已确认
        */
        private String status;
        /**
         * 核算状态名称
         */
        private String statusName;
        /**
         * 发货单id
         */
        private String sourceId;
        /**
         * 发货单编码
         */
        private String sourceCode;

        /**
        * 物流商id
        */
        private String supplierId;

        /**
        * 物流商名称
        */
        private String supplierName;

        /**
        * {业务单号}取值发货单关联的业务单号
        * FBA：取值FBA货件单号
        * 第三方仓：海外仓入库单号
        */
        private String businessCode;

        /**
        * 业务类型：demandOverseasWarehouse=第三方仓，demandPlatformWarehouse=FBA
        */
        private String businessType;

        /**
        * 运单号
        */
        private String transportNo;

        /**
        * 对账月份
        */
        private LocalDate reconciliationMonth;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 发货仓库id
        */
        private String fromWarehouseId;

        /**
        * 发货仓库名称
        */
        private String fromWarehouseName;

        /**
        * 目的仓库id
        */
        private String toWarehouseId;

        /**
        * 目的仓库名称
        */
        private String toWarehouseName;


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
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 备注
        */
        private String remark;

        /**
        * 对账单id
        */
        @NotBlank(message = "对账单id不能为空")
        @Size(max = 19,message = "对账单id最大长度不能超过19位")
        private String reconciliationId;

        /**
        * 对账单单据编号
        */
        @NotBlank(message = "对账单单据编号不能为空")
        @Size(max = 64,message = "对账单单据编号最大长度不能超过64位")
        private String reconciliationCode;

        /**
        * 物流单id
        */
        @NotBlank(message = "物流单id不能为空")
        @Size(max = 19,message = "物流单id最大长度不能超过19位")
        private String logisticsBillId;

        /**
        * 物流单明细id
        */
        @NotBlank(message = "物流单明细id不能为空")
        @Size(max = 19,message = "物流单明细id最大长度不能超过19位")
        private String logisticsBillDetailId;

        /**
        * 暂估账单id
        */
        @NotBlank(message = "暂估账单id不能为空")
        @Size(max = 19,message = "暂估账单id最大长度不能超过19位")
        private String estimatedBillId;
        /**
         * 期初费用分摊id
         */
        @NotBlank(message = "期初费用分摊id不能为空")
        @Size(max = 19,message = "期初费用分摊id最大长度不能超过19位")
        private String initFirstMileId;
        /**
         * 期初费用分摊明细id
         */
        @NotBlank(message = "期初费用分摊明细id不能为空")
        @Size(max = 19,message = "期初费用分摊明细id最大长度不能超过19位")
        private String initFirstMileDetailId;

        /**
        * SKU成本id
        */
        @NotBlank(message = "SKU成本id不能为空")
        @Size(max = 19,message = "SKU成本id最大长度不能超过19位")
        private String skuCostId;

        /**
        * SKU成本明细id
        */
        @NotBlank(message = "SKU成本明细id不能为空")
        @Size(max = 19,message = "SKU成本明细id最大长度不能超过19位")
        private String skuCostDetailId;

        /**
        * 重量分摊id
        */
        @NotBlank(message = "重量分摊id不能为空")
        @Size(max = 19,message = "重量分摊id最大长度不能超过19位")
        private String weightAllocationId;

        /**
        * 核算期间id
        */
        @NotBlank(message = "核算期间id不能为空")
        @Size(max = 19,message = "核算期间id最大长度不能超过19位")
        private String reportPeriodId;

        /**
        * 会计期间
        */
        @NotBlank(message = "会计期间不能为空")
        @Size(max = 100,message = "会计期间最大长度不能超过100位")
        private String accountPeriod;

        /**
        * 核算状态：waitConfirm=待确认，confirm=已确认
        */
        @NotBlank(message = "核算状态：waitConfirm=待确认，confirm=已确认不能为空")
        @Size(max = 30,message = "核算状态：waitConfirm=待确认，confirm=已确认最大长度不能超过30位")
        private String status;

        /**
        * 物流商id
        */
        @NotBlank(message = "物流商id不能为空")
        @Size(max = 19,message = "物流商id最大长度不能超过19位")
        private String supplierId;

        /**
        * 物流商名称
        */
        @NotBlank(message = "物流商名称不能为空")
        @Size(max = 200,message = "物流商名称最大长度不能超过200位")
        private String supplierName;
        /**
         * 发货单id
         */
        @NotBlank(message = "发货单id不能为空")
        @Size(max = 19,message = "发货单id最大长度不能超过19位")
        private String sourceId;
        /**
         * 发货单编码
         */
        @NotBlank(message = "发货单编码不能为空")
        @Size(max = 64,message = "发货单编码最大长度不能超过64位")
        private String sourceCode;

        /**
        * {业务单号}取值发货单关联的业务单号
        * FBA：取值FBA货件单号
        * 第三方仓：海外仓入库单号
        */
        @NotBlank(message = "业务单号取值发货单关联的业务单号 FBA：取值FBA货件单号 第三方仓：海外仓入库单号不能为空")
        @Size(max = 64,message = "业务单号取值发货单关联的业务单号 FBA：取值FBA货件单号 第三方仓：海外仓入库单号最大长度不能超过64位")
        private String businessCode;

        /**
        * 业务类型：demandOverseasWarehouse=第三方仓，demandPlatformWarehouse=FBA
        */
        @NotBlank(message = "业务类型：demandOverseasWarehouse=第三方仓，demandPlatformWarehouse=FBA不能为空")
        @Size(max = 30,message = "业务类型：demandOverseasWarehouse=第三方仓，demandPlatformWarehouse=FBA最大长度不能超过30位")
        private String businessType;

        /**
        * 运单号
        */
        @NotBlank(message = "运单号不能为空")
        @Size(max = 200,message = "运单号最大长度不能超过200位")
        private String transportNo;

        /**
        * 对账月份
        */
        private LocalDate reconciliationMonth;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19,message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 200,message = "店铺名称最大长度不能超过200位")
        private String shopName;

        /**
        * 发货仓库id
        */
        @NotBlank(message = "发货仓库id不能为空")
        @Size(max = 19,message = "发货仓库id最大长度不能超过19位")
        private String fromWarehouseId;

        /**
        * 发货仓库名称
        */
        @NotBlank(message = "发货仓库名称不能为空")
        @Size(max = 200,message = "发货仓库名称最大长度不能超过200位")
        private String fromWarehouseName;

        /**
        * 目的仓库id
        */
        @NotBlank(message = "目的仓库id不能为空")
        @Size(max = 19,message = "目的仓库id最大长度不能超过19位")
        private String toWarehouseId;

        /**
        * 目的仓库名称
        */
        @NotBlank(message = "目的仓库名称不能为空")
        @Size(max = 200,message = "目的仓库名称最大长度不能超过200位")
        private String toWarehouseName;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }
    @Data
    @NoArgsConstructor
    public static class IdsDTO extends PermissionsDTO {
        /**
         * 核算期间id
         */
        @NotBlank(message = "核算期间id不能为空")
        private String reportPeriodId;

        /**
         * 表 ids
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

    }

    /**
     * 分页参数
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

    @Data
    @NoArgsConstructor
    public static class PagingVO {
        /**
         * 主键id
         *
         */
        private String  id;

        /**
         * 备注
         */
        private String remark;

        /**
         * 对账单id
         */
        private String reconciliationId;
        /**
         * 对账单明细id
         */
        private String reconciliationDetailId;

        /**
         * 对账单单据编号
         */
        private String reconciliationCode;

        /**
         * 物流单id
         */
        private String logisticsBillId;

        /**
         * 物流单明细id
         */
        private String logisticsBillDetailId;
        /**
         * 签收时间
         */
        private LocalDateTime receiveTime;
        /**
         * 期初费用分摊id
         */
        private String initFirstMileId;
        /**
         * 期初费用分摊明细id
         */
        private String initFirstMileDetailId;

        /**
         * 暂估账单id
         */
        private String estimatedBillId;

        /**
         * SKU成本id
         */
        private String skuCostId;

        /**
         * SKU成本明细id
         */
        private String skuCostDetailId;

        /**
         * 重量分摊id
         */
        private String weightAllocationId;

        /**
         * 核算期间id
         */
        private String reportPeriodId;
        /**
         * 核算月份
         */
        private LocalDate reportPeriodMonth;
        /**
         * 核算月份【导出使用】
         */
        private String reportPeriodStr;

        /**
         * 会计期间
         */
        private String accountPeriod;

        /**
         * 核算状态：waitConfirm=待确认，confirm=已确认
         */
        private String status;
        /**
         * 核算状态名称
         */
        private String statusName;
        /**
         * 发货单id
         */
        private String sourceId;
        /**
         * 发货单明细id
         */
        private String sourceDetailId;
        /**
         * 发货单编码
         */
        private String sourceCode;

        /**
         * 物流商id
         */
        private String supplierId;

        /**
         * 物流商名称
         */
        private String supplierName;

        /**
         * {业务单号}取值发货单关联的业务单号
         * FBA：取值FBA货件单号
         * 第三方仓：海外仓入库单号
         */
        private String businessCode;

        /**
         * 业务类型：demandOverseasWarehouse=第三方仓，demandPlatformWarehouse=FBA
         */
        private String businessType;

        /**
         * 运单号
         */
        private String transportNo;

        /**
         * 对账月份
         */
        private LocalDate reconciliationMonth;
        /**
         * 对账月份【导出使用】
         */
        private String reconciliationMonthStr;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 发货仓库id
         */
        private String fromWarehouseId;

        /**
         * 发货仓库名称
         */
        private String fromWarehouseName;

        /**
         * 目的仓库id
         */
        private String toWarehouseId;

        /**
         * 目的仓库名称
         */
        private String toWarehouseName;

        /**
         * sku主键id
         */
        private String  costId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNO
         */
        private String skuNo;

        /**
         * 平台skuId
         */
        private String platformSkuId;

        /**
         * 平台skuNo
         */
        private String platformSkuNo;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 分摊重量
         */
        private BigDecimal allocatedWeight;

        /**
         * 单位成本
         */
        private BigDecimal productCost;

        /**
         * 产品总成本
         */
        private BigDecimal productTotalCost;

        /**
         * 期初签收数量
         */
        private Integer initReceiveQty;

        /**
         * 上月签收数量
         */
        private Integer lastMonthReceiveQty;

        /**
         * 本月签收数量
         */
        private Integer currentMonthReceiveQty;
        /**
         * 截止本月签收数量
         */
        private Integer asCurrentMonthReceiveQty;
        /**
         * 截止上月签收数量
         */
        private Integer asLastMonthReceiveQty;

        /**
         * 重量单位（默认kg）
         */
        private String weightUnit;

        /**
         * 币种（默认CNY）
         */
        private String currency;

        /**
         * 费用来源：estimated=预估账单，actual=实际账单
         * ReconciliationBillTypeEnum
         */
        private String billSourceType;
        /**
         * 费用来源名称
         */
        private String billSourceTypeName;

        /**
         * 费用类型：shippingCost=运费，tariffFee=关税，otherTaxFee=其他税费，otherFee=其他费用
         * AllocationFeeTypeEnum
         */
        private String feeType;
        /**
         * 费用类型名称
         */
        private String feeTypeName;

        /**
         * 费用分摊方式：weightAllocation=按重量分摊，costAllocation=按成本分摊
         * CostAllocationEnum
         */
        private String allocationType;
        /**
         * 费用分摊方式名称
         */
        private String allocationTypeName;

        /**
         * 头程总金额
         */
        private BigDecimal amount;

        /**
         * 头程分摊金额
         */
        private BigDecimal allocatedAmount;

        /**
         * 单个产品分摊金额
         */
        private BigDecimal productAllocatedAmount;

        /**
         * 期初在途费用
         */
        private BigDecimal initTransitCost;

        /**
         * 期初暂估费用
         */
        private BigDecimal initEstimatedCost;

        /**
         * 冲期初在途费用
         */
        private BigDecimal midPeriodTransitCost;

        /**
         * 本期分摊费用
         */
        private BigDecimal currentPeriodAllocatedCost;

        /**
         * 期末在途费用
         */
        private BigDecimal endPeriodTransitCost;

        /**
         * 期末暂估费用
         */
        private BigDecimal endPeriodEstimatedCost;
        /**
         * 分摊组织id
         */
        private String orgId;
        /**
         * 分摊组织名称
         */
        private String orgName;
    }
    @Data
    @NoArgsConstructor
    public static class JudgeReconciliationDTO {
        //本月开始有实际账单
        private boolean currencyMonthReconciliation = false;
        //上月开始有账单
        private boolean lastMonthReconciliation = false;
    }

    @Data
    @NoArgsConstructor
    public static class LastedAllocMonthDTO{
        /**
         * 物流单ID
         */
        private String logisticsBillId;
        /**
         * 最新核算期间
         */
        private LocalDate latestMonth;
        /**
         * 核算期间ID
         */
        private String reportPeriodId;

        /**
         * 费用来源：estimated=预估账单，actual=实际账单
         * ReconciliationBillTypeEnum
         */
        private String billSourceType;

        /**
         * 期末在途费用
         */
        private BigDecimal endPeriodTransitCost;
        /**
         * 最新核算状态
         */
        private String status;
    }
}