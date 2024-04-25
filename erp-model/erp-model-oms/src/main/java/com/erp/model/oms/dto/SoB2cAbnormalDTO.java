package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @date 2024/4/22 9:11
 */
@Data
@NoArgsConstructor
public class SoB2cAbnormalDTO  implements Serializable {

    /**
     * 分页查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 销售订单主键id
         */
        private String id;
        /**
         * 销售单号【可排序】
         */
        private String code;

        /**
         * 销售平台【可排序】
         */
        private String dictPlatform;

        /**
         * 审核状态【可排序】
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 单据状态【可排序】
         */
        private String billStatus;

        /**
         * 单据状态
         */
        private String billStatusName;

        /**
         * 平台sku编号【可排序】
         */
        private String platformSkuNo;

        /**
         * 平台 产品id【可排序】
         */
        private String platformSpuNo;

        /**
         * 系统SKU【可排序】
         */
        private String skuNo;

        /**
         * 订单异常标示【可排序】
         */
        private String signOrderError;

        /**
         * 订单异常标示名称
         */
        private String signOrderErrorName;

        /**
         * 异常信息【可排序】
         */
        private String message;

        /**
         * 物流渠道名称【可排序】
         */
        private String logisticsChannelName;

        /**
         * 付款时间【可排序】
         */
        private LocalDateTime payTime;

        /**
         * 异常生成时间【可排序】
         */
        private LocalDateTime signOrderErrorTime;
    }

}
