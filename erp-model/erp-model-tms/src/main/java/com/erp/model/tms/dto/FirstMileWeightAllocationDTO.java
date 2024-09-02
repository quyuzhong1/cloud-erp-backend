package com.erp.model.tms.dto;

import java.math.BigDecimal;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 头程重量分摊请求响应实体
 * </p>
 *
 * @author tmj
 * @since 2024-08-20
*/
@Data
@NoArgsConstructor
public class FirstMileWeightAllocationDTO implements Serializable {

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
        * 来源ID
        */
        private String sourceId;

        /**
        * 来源单号【可排序】
        */
        private String sourceCode;

        /**
        * 发货单明细id
        */
        private String deliveryDetailId;

        /**
        * 业务单号【可排序】
        */
        private String businessCode;

        /**
        * 物流运单号【可排序】
        */
        private String transportNo;

        /**
        * 产品名称【可排序】
        */
        private String productName;

        /**
        * 费用分摊状态【可排序】
        * CostAllocationStatusEnum
        */
        private String costAllocationStatus;

        /**
         * 费用分摊状态(名称)
         */
        private String costAllocationStatusName;

        /**
        * 最新核算期间
        */
        private LocalDate latestCostAllocationMonth;

        /**
         * 最新核算期间Str
         */
        private String latestCostAllocationMonthStr;

        private String skuId;

        /**
         * skuNo【可排序】
         */
        private String skuNo;

        /**
        * 平台skuId
        */
        private String platformSkuId;

        /**
        * 平台skuNo【可排序】
        */
        private String platformSkuNo;

        /**
        * 箱ID
        */
        private String boxId;

        /**
        * 箱号【可排序】
        */
        private String boxNo;

        /**
        * 发货量【可排序】
        */
        private Integer deliveryQty;

        /**
        * 箱长
        */
        private Integer boxLength;

        /**
        * 箱宽
        */
        private Integer boxWidth;

        /**
        * 箱高
        */
        private Integer BoxHeight;

        /**
        * 箱子尺寸单位
        */
        private String boxSizeUnit;

        /**
         * 出库尺寸（导出用）
         */
        private String boxSizeStr;

        /**
         * 出库重量
         */
        private BigDecimal outStockWeight;

        /**
        * 出库计费重【可排序】
        */
        private BigDecimal chargedWeight;

        /**
         * 体积重
         */
        private BigDecimal volumeWeight;

        /**
        * 单产品重量【可排序】
        */
        private BigDecimal productWeight;

        /**
        * 分摊重量【可排序】
        */
        private BigDecimal allocationWeight;

        /**
        * 重量单位
        */
        private String weightUnit;

        /**
        * 物流商ID
        */
        private String supplierId;

        /**
        * 物流商名称【可排序】
        */
        private String supplierName;

        /**
        * 重量分摊方式【可排序】
        */
        private String allocationType;

        /**
         * 重量分摊方式名称
         * WeightAllocationTypeEnum
         */
        private String allocationTypeName;

        /**
        * 计费规则【可排序】
        */
        private String feeRule;

        /**
         * 计费规则名称
         * ShippingFeeRuleEnum
         */
        private String feeRuleName;

        /**
        * 店铺ID
        */
        private String shopId;

        /**
        * 店铺名称【可排序】
        */
        private String shopName;

        /**
        * 目的国家【可排序】
        */
        private String toCountry;

        /**
         * 目的国家（名称）
         */
        private String toCountryName;

        /**
        * 发货仓库ID【可排序】
        */
        private String fromWarehouseId;

        /**
         * 发货仓库（名称）
         */
        private String fromWarehouseName;

        /**
        * 最新核算期间id
        */
        private String calculatePeriodId;

        /**
        * 核算月份
        */
        private String calculateMonth;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建时间（导出用）
         */
        private String createTimeStr;

        /**
         * 物流单ID
         */
        private String logisticsBillId;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 头程物流单ID
         */
        private String logisticsBillId;

        /**
         * 来源ID
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 发货单明细ID
         */
        private String deliveryDetailId;

        /**
         * 业务单号
         */
        private String businessCode;

        /**
         * 物流运单号
         */
        private String transportNo;

        /**
         * 装箱信息列表
         */
        private List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDTOList;

        private List<ProductDetailDTO> productDetailList;

        /**
         * 物流商ID
         */
        private String supplierId;

        /**
         * 物流商名称
         */
        private String supplierName;

        /**
         * 物流渠道的计费规则
         * ShippingFeeRuleEnum
         */
        private String feeRule;

        /**
         * 店铺ID
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 目的国家编码
         */
        private String toCountry;

        /**
         * 目的国家名称
         */
        private String toCountryName;

        /**
         * 发货仓库ID
         */
        private String fromWarehouseId;

        /**
         * 物流渠道ID
         */
        private String channelId;
        /**
         * 材积设置
         */
        private int volumeSetting;
    }

    @Data
    public static class ProductDetailDTO{
        private String skuId;
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 平台skuNo
         */
        private String platformSkuNo;
        /**
         * 发货数量
         */
        private int deliveryQty;
        /**
         * 箱ID
         */
        private String boxId;
        /**
         * 箱编号
         */
        private String boxNo;
        /**
         * 箱子重量(出库重量)
         */
        private BigDecimal outStockWeight;
        /**
         * 重量单位
         */
        private String weightUnit;
        /**
         * 箱长
         */
        private BigDecimal boxLength;
        /**
         * 箱宽
         */
        private BigDecimal boxWidth;
        /**
         * 箱高
         */
        private BigDecimal boxHeight;
        /**
         * 尺寸单位
         */
        private String boxSizeUnit;
        /**
         * 单产品重量
         */
        private BigDecimal productWeight;
        /**
         * 体积重
         */
        private BigDecimal volumeWeight;
        /**
         * 计费重
         */
        private BigDecimal chargedWeight;
        /**
         * 分摊重量
          */
        private BigDecimal allocationWeight;
        /**
         * 重量分摊方式
         */
        private String allocationType;
    }

    @Data
    @NoArgsConstructor
    public static class BoxDTO{
        /**
         * 箱子ID
         */
        private String boxId;
        /**
         * 箱号
         */
        private String boxNo;

        private String skuId;

        private String skuNo;

        private String productName;

        /**
         * 平台sku id
         */
        private String platformSkuId;

        /**
         * 平台sku no
         */
        private String platformSkuNo;

        /**
         * 箱子长
         */
        private String boxLength;
        /**
         * 箱子宽
         */
        private String boxWidth;
        /**
         * 箱子高
         */
        private String boxHeight;
        /**
         * 箱子尺寸单位
         */
        private String boxSizeUnit;
    }

    /**
    * 修改
    */
    @EqualsAndHashCode(callSuper = true)
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
        * 来源ID
        */
        private String sourceId;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 发货单明细id
        */
        private String deliveryDetailId;

        /**
        * 业务单号
        */
        private String businessCode;

        /**
        * 物流运单号
        */
        private String transportNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 费用状态
        */
        private String allocationStatus;

        private String skuId;

        /**
        * 平台skuId
        */
        private String platformSkuId;

        /**
        * 平台skuNo
        */
        private String platformSkuNo;

        /**
        * 箱ID
        */
        private String boxId;

        /**
        * 箱号
        */
        private String boxNo;

        /**
        * 发货量
        */
        private Integer deliveryQty;

        /**
        * 箱长
        */
        private Integer boxLength;

        /**
        * 箱宽
        */
        private Integer boxWide;

        /**
        * 箱高
        */
        private Integer boxHigh;

        /**
        * 箱子尺寸单位
        */
        private String boxSizeUnit;

        /**
        * 出库计费重
        */
        private BigDecimal chargedWeight;

        /**
        * 体积重
        */
        private BigDecimal volumeWeight;

        /**
        * 单产品重量
        */
        private BigDecimal productWeight;

        /**
        * 分摊重量
        */
        private BigDecimal allocationWeight;

        /**
        * 重量单位
        */
        private String weightUnit;

        /**
        * 物流商ID
        */
        private String supplierId;

        /**
        * 物流商名称
        */
        private String supplierName;

        /**
        * 重量分摊方式
         * CostAllocationEnum
        */
        private String allocationType;

        /**
        * 计费规则
        */
        private String billingRule;

        /**
        * 店铺ID
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 目的国家编码
        */
        private String toCountry;

        /**
        * 发货仓库ID
        */
        private String fromWarehouseId;

        /**
        * 核算期间id
        */
        private String calculatePeriodId;

        /**
        * 核算月份
        */
        private String calculateMonth;


    }

    /**
     * 分页查询参数
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
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
     * 导出参数
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class ExportParamDTO extends PagingParamDTO{
        private List<String> ids;
    }

    /**
     * Tab统计
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TabDTO{
        /**
         * 标签编码
         */
        private String tabFlag;
        /**
         * 标签名称
         */
        private String tabFlagName;
        /**
         * 统计数量
         */
        private int count;
    }

    /**
     * 下推费用分摊
     */
    @Data
    @NoArgsConstructor
    public static class PushCostAllocationDTO{
        /**
         * 来源ids
         */
        private List<String> sourceIds;

        /**
         * 期间
         */
        private String period;
    }

    /**
     * 重量重算
     */
    @Data
    @NoArgsConstructor
    public static class WeightReComputeDTO{
        /**
         * 头程物流单ID
         */
        private List<String> logisticsBillIds;
    }

    /**
     * 批量删除
     */
    @Data
    @NoArgsConstructor
    public static class DeleteDTO{
        /**
         * 头程物流单ID
         */
        private List<String> logisticsBillIds;
    }

    @Data
    public static class LogisticsBillInfoDTO{
        /**
         * 头程物流单ID
         */
        private String logisticsBillId;
        /**
         * 来源ID
         */
        private String sourceId;
        /**
         * 来源单号
         */
        private String sourceCode;
        /**
         * 业务单号
         * FBA：取值FBA货件单号
         * 第三方仓：海外仓入库单号
         */
        private String businessCode;
        /**
         * 物流运单号
         */
        private String transportNo;
        /**
         * 物流商ID
         */
        private String supplierId;
        /**
         * 店铺ID
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 目的国家编码
         */
        private String toCountry;

        /**
         * 物流渠道ID
         */
        private String channelId;
    }

    @Data
    public static class CostAllocationDTO{
        /**
         *
         */
        private String logisticsBillId;
        /**
         *
         */
        private String reportPeriodId;
        /**
         *
         */
        private String accountPeriod;
        /**
         *
         */
        private String costAllocationStatus;
        /**
         *
         */
        private String skuId;
        /**
         *
         */
        private String skuNo;
        /**
         *
         */
        private String billSourceType;
        /**
         *
         */
        private BigDecimal endPeriodTransitCost;
        /**
         *
         */
        private LocalDate reportPeriod;
    }
}