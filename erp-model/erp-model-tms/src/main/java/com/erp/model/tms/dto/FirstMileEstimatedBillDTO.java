package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
public class FirstMileEstimatedBillDTO implements Serializable {

    @Data
    public static class View{
        private String id;

        /**
         * 业务单号
         */
        private String businessCode;

        /**
         * 物流运单号
         */
        private String transportNo;

        /**
         * 暂估账单状态
         */
        private String status;

        /**
         * 暂估账单状态（名称）
         */
        private String statusName;

        /**
         * 实际账单状态
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
         * 店铺（名称）
         */
        private String shopName;

        /**
         * 目的国家
         */
        private String toCountry;

        /**
         * 目的国家（名称）
         */
        private String toCountryName;

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
        private BigDecimal logisticsCost;

        /**
         * 预计报关费用（总）
         */
        private BigDecimal customsClearanceCost;

        /**
         * 预计其他税费（总）
         */
        private BigDecimal otherTaxCost;

        /**
         * 预计其他费用（总）
         */
        private BigDecimal otherCost;

        /**
         * 预计计费重
         */
        private int chargedWeight;

        /**
         * 预计实重
         */
        private int actualWeight;

        /**
         * 预计体积重
         */
        private int volumeWeight;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 结算币种
         */
        private String currency;

        /**
         * 运输状态
         */
        private String transportStatus;

        /**
         * 运输状态（名称）
         */
        private String transportStatusName;

        /**
         * 签收日期
         */
        private LocalDateTime signTime;

        /**
         * 账单确认日期
         */
        private LocalDateTime confirmTime;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    @Data
    public static class PagingParam{
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
    public static class TabList{
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
}
