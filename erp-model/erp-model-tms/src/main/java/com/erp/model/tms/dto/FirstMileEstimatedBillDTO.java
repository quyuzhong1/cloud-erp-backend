package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 头程暂估账单DTO
 * @date 2024-08-16
 * @author tanmujin
 */
@Data
@NoArgsConstructor
public class FirstMileEstimatedBillDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class View{
        private String id;

        /**
         * 业务单号【可排序】
         */
        private String businessCode;

        /**
         * 来源单据ID（发货单ID）
         */
        private String outStockId;

        /**
         * 物流运单号【可排序】
         */
        private String transportNo;

        /**
         * 暂估账单状态【可排序】
         * /api/tms/common/enumDropDown?type=ConfirmStatus
         */
        private String status;

        /**
         * 暂估账单状态（名称）
         * ConfirmStatusEnum
         */
        private String statusName;

        /**
         * 实际账单状态【可排序】
         */
        private String actualBillStatus;

        /**
         * 实际账单状态（名称）
         */
        private String actualBillStatusName;

        /**
         * 店铺（ID）
         */
        private String shopId;

        /**
         * 店铺（名称）【可排序】
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
         * 计费规则【可排序】
         */
        private String feeRule;

        /**
         * 计费规则（名称）
         */
        private String feeRuleName;

        /**
         * 预计费用总计
         */
        private BigDecimal costTotal;

        /**
         * 预计物流运费（总）
         */
        private BigDecimal logisticsCost = BigDecimal.ZERO;

        /**
         * 预计报关费用（总）
         */
        private BigDecimal customsClearanceCost = BigDecimal.ZERO;

        /**
         * 预计其他税费（总）
         */
        private BigDecimal otherTaxCost = BigDecimal.ZERO;

        /**
         * 预计其他费用（总）
         */
        private BigDecimal otherCost = BigDecimal.ZERO;

        /**
         * 预计计费重
         */
        private BigDecimal chargedWeight = BigDecimal.ZERO;

        /**
         * 预计实重
         */
        private BigDecimal actualWeight = BigDecimal.ZERO;

        /**
         * 预计体积重
         */
        private BigDecimal volumeWeight = BigDecimal.ZERO;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 物流费用ID
         */
        private String logisticsBillCostId;

        /**
         * 结算币种【可排序】
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 运输状态【可排序】
         */
        private String transportStatus;

        /**
         * 运输状态（名称）
         */
        private String transportStatusName;

        /**
         * 签收日期【可排序】
         */
        private LocalDateTime signTime;

        /**
         * 账单确认日期【可排序】
         */
        private LocalDateTime confirmTime;

        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;

        /**
         * 物流渠道ID
         */
        private String logisticsChannelId;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class PagingParam extends SortDTO {
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
    public static class UpdateStatus{
        private List<String> ids;

        /**
         * 下拉接口：/tms/common/enumDropDown?type=ConfirmStatus
         */
        private String type;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Tab{
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

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ExportParam extends PagingParam{
        private List<String> ids;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ExportExcel extends View{

    }
}
