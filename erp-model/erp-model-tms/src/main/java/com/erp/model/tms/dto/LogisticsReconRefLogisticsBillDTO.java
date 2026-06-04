package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 物流商对账关联关系 请求响应实体
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Data
@NoArgsConstructor
public class LogisticsReconRefLogisticsBillDTO implements Serializable {

    /**
     * 关联视图（带 ERP 物流单号 / 费用单号展示）
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 对账单 id
         */
        private String mainId;
        /**
         * 对账明细 id
         */
        private String detailId;
        /**
         * 对账费用项 id
         */
        private String detailSubId;
        /**
         * ERP 物流单 id
         */
        private String logisticsBillId;
        /**
         * ERP 物流单号
         */
        private String logisticsBillCode;
        /**
         * ERP 物流单明细 id
         */
        private String logisticsBillDetailId;
        /**
         * ERP 物流费用单 id
         */
        private String logisticsBillCostId;
        /**
         * ERP 物流费用单号
         */
        private String logisticsBillCostCode;
        /**
         * ERP 费用项明细 id（tms_cost_detail.id）
         */
        private String tmsCostDetailId;
        /**
         * 匹配类型 auto / manual / newBill
         */
        private String matchType;
        /**
         * 匹配类型名称
         */
        private String matchTypeName;
        /**
         * 匹配人 id
         */
        private String matchUserId;
        /**
         * 匹配人姓名
         */
        private String matchUserName;
        /**
         * 匹配时间
         */
        private LocalDateTime matchTime;
        /**
         * 合并匹配时使用的导入处理类型 importUpdate / importAddOld / importAddNew
         */
        private String importType;
        /**
         * 关联物流费用单对账状态快照
         */
        private String reconciliationStatus;
    }
}
