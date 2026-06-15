package com.erp.model.tms.dto;

import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流商对账"合并 & 匹配"编排参数。
 * 复用物流费用导入（ImportHistoryRecordServiceImpl）的匹配/落库逻辑：
 * 把对账明细/费用项转为与导入一致的"识别号 + 费用项"行，调用 reconMatchAndGenerate
 * 完成「按识别单号 + 物流商匹配物流单 → 生成/更新物流费用单与费用项」。
 * </p>
 *
 * @author Will
 * @since 2026-06-11
 */
@Data
@NoArgsConstructor
public class LogisticsReconMatchDTO implements Serializable {

    /**
     * 匹配编排上下文
     */
    @Data
    @NoArgsConstructor
    public static class MatchContextDTO {
        /**
         * 命中的导入模板配置（提供物流商/平台、cfgType、importType 等匹配范围）
         */
        private CfgLogisticsCostImportEntity costImportEntity;
        /**
         * 导入模板字段配置（提供唯一识别字段 isUniqueKey）
         */
        private List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList;
        /**
         * 对账月份 YYYY-MM
         */
        private String reconciliationMonth;
        /**
         * 落库处理类型（ImportHistoryRecordProcessingTypeEnum：import / confirmImport）
         */
        private String processingType;
        /**
         * 是否要求识别号唯一命中一张物流单（手动 / 导入匹配为 true，命中多张视为失败）
         */
        private boolean requireUnique = true;
        /**
         * 是否按行内传入的识别字段匹配（手动 / 导入匹配使用）。
         * false 时按导入模板配置的唯一键匹配，true 时每行按用户填写的 ERP 单号字段匹配。
         */
        private boolean matchByProvidedIdentifyKeys = false;
        /**
         * 待匹配行（行 = 一条对账明细 logistics_recon_detail）
         */
        private List<MatchRowDTO> rows = new ArrayList<>();
    }

    /**
     * 待匹配行（对应一条对账明细及其下所有费用项）
     */
    @Data
    @NoArgsConstructor
    public static class MatchRowDTO {
        /**
         * 行标识（对账明细 id），用于回填匹配结果
         */
        private String rowKey;
        /**
         * 识别字段值（key = 导入模板唯一键 targetField，如 sourceCode / platformCode / trackNo / transportNo / soDeliveryCode）
         */
        private Map<String, String> identifyValues = new HashMap<>();
        /**
         * 对账类型 pay / refund
         */
        private String payType;
        /**
         * 原币别
         */
        private String currency;
        /**
         * 物流商计费重
         */
        private BigDecimal billingWeightLogistics;
        /**
         * 物流商实重
         */
        private BigDecimal thirdActualWeight;
        /**
         * 物流商重量单位（用于按单位换算计费重/实重，与导入 standardizeImportRowWeightValues 一致）
         */
        private String weightUnit;
        /**
         * 物流商尺寸-长
         */
        private BigDecimal thirdLength;
        /**
         * 物流商尺寸-宽
         */
        private BigDecimal thirdWidth;
        /**
         * 物流商尺寸-高
         */
        private BigDecimal thirdHeight;
        /**
         * 行内费用项
         */
        private List<MatchCostItemDTO> costItems = new ArrayList<>();
    }

    /**
     * 费用项
     */
    @Data
    @NoArgsConstructor
    public static class MatchCostItemDTO {
        /**
         * 对账费用项 id（logistics_recon_detail_sub.id），用于写关联关系与回填匹配状态
         */
        private String detailSubId;
        /**
         * 映射后费用项配置 id（cfg_cost.id）
         */
        private String cfgCostId;
        /**
         * 费用名称（错误提示用）
         */
        private String costName;
        /**
         * 实际金额（原币）
         */
        private BigDecimal actualAmount;
        /**
         * 预估金额（原币）
         */
        private BigDecimal estimatedAmount;
    }

    /**
     * 行匹配结果
     */
    @Data
    @NoArgsConstructor
    public static class MatchResultDTO {
        /**
         * 行标识（对账明细 id）
         */
        private String rowKey;
        /**
         * 是否匹配成功
         */
        private boolean success;
        /**
         * 失败原因
         */
        private String failReason;
        /**
         * 命中的导入处理类型（importUpdate / importAddOld）
         */
        private String importType;
        /**
         * 命中/生成的物流费用单关联（多张物流单时多条）
         */
        private List<BillRefDTO> billRefs = new ArrayList<>();
        /**
         * 匹配成功后按 detailSubId 回写的 ERP 费用配置（key = logistics_recon_detail_sub.id）
         */
        private Map<String, ResolvedCfgCostDTO> resolvedCfgCostBySubId = new HashMap<>();
    }

    /**
     * 匹配解析出的 ERP 费用项配置，用于回写 detail_sub.cfg_cost_id / cfg_cost_name
     */
    @Data
    @NoArgsConstructor
    public static class ResolvedCfgCostDTO {
        private String detailSubId;
        private String cfgCostId;
        private String cfgCostName;
    }

    /**
     * 费用项 ERP 单号匹配入参（手动匹配 / 导入匹配共用）
     */
    @Data
    @NoArgsConstructor
    public static class SubErpInputDTO {
        /**
         * 对账费用项 id（logistics_recon_detail_sub.id）
         */
        private String detailSubId;
        /**
         * ERP 销售单号
         */
        private String erpSoCode;
        /**
         * ERP 平台订单号
         */
        private String erpPlatformOrderNo;
        /**
         * ERP 物流跟踪号
         */
        private String erpTrackNo;
        /**
         * ERP 发货单号
         */
        private String erpSoDeliveryCode;
    }

    /**
     * 物流单/费用单关联快照
     */
    @Data
    @NoArgsConstructor
    public static class BillRefDTO {
        private String detailSubId;
        private String logisticsBillId;
        private String logisticsBillDetailId;
        private String logisticsBillCostId;
        private String tmsCostDetailId;
        private String reconciliationStatus;
    }
}
