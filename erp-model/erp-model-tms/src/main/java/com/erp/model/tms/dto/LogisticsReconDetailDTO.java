package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流商对账明细（行级）请求响应实体
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Data
@NoArgsConstructor
public class LogisticsReconDetailDTO implements Serializable {

    /**
     * 分页查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 对账单 id
         */
        @NotBlank(message = "对账单id不能为空")
        private String mainId;

        /**
         * 费用项匹配状态过滤：unmatched / matching / matched / failed
         */
        private String matchStatus;

        /**
         * 关键字搜索（trackNo / transportNo / soCode / platformOrderNo）
         */
        private String keyword;

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认 key default
         */
        private Map<String, String> sqlMap;
    }

    /**
     * 行明细列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id（费用项 logistics_recon_detail_sub.id；列表为费用项粒度，一行一条费用项）
         */
        private String id;
        /**
         * 所属对账明细 id（logistics_recon_detail.id；行级匹配/操作以此为单位）
         */
        private String detailId;
        /**
         * 对账单 id
         */
        private String mainId;
        /**
         * Excel 原始行号
         */
        private Integer rowNo;
        /**
         * 同 detail 下费用项顺序
         */
        private Integer seqNo;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 平台订单号
         */
        private String platformOrderNo;
        /**
         * 物流跟踪号
         */
        private String trackNo;
        /**
         * 物流运单号
         */
        private String transportNo;
        /**
         * 发货单号
         */
        private String soDeliveryCode;
        /**
         * 匹配到的 ERP 销售单号（第三方/ERP 对照展示）
         */
        private String erpSoCode;
        /**
         * 匹配到的 ERP 平台订单号
         */
        private String erpPlatformOrderNo;
        /**
         * 匹配到的 ERP 物流跟踪号
         */
        private String erpTrackNo;
        /**
         * 匹配到的 ERP 发货单号
         */
        private String erpSoDeliveryCode;
        /**
         * 物流商费用名称（导入识别的原始费用名称 logistics_recon_detail_sub.cost_name）
         */
        private String costName;
        /**
         * ERP 费用名称（映射后费用项配置名称 logistics_recon_detail_sub.cfg_cost_name）
         */
        private String cfgCostName;
        /**
         * 金额（费用项实际金额 logistics_recon_detail_sub.actual_amount，原币）
         */
        private BigDecimal actualAmount;
        /**
         * 金额（带币别符号展示）
         */
        private String actualAmountStr;
        /**
         * 币别（原币别，取自费用项 logistics_recon_detail_sub.currency）
         */
        private String currency;
        /**
         * 币别符号
         */
        private String currencySymbol;
        /**
         * 对账类型 pay / refund
         */
        private String payType;
        /**
         * 物流商实重
         */
        private BigDecimal weightLogistics;
        /**
         * 物流商体积重
         */
        private BigDecimal volumeWeightLogistics;
        /**
         * 物流商计费重
         */
        private BigDecimal billingWeightLogistics;
        /**
         * 物流商重量单位
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
         * 物流商尺寸（长*宽*高 拼接展示，由 thirdLength/thirdWidth/thirdHeight 组装）
         */
        private String thirdSize;
        /**
         * 该行展开的费用项条数
         */
        private Integer costCount;
        /**
         * 费用项匹配状态 unmatched / matching / matched / failed
         */
        private String matchStatus;
        /**
         * 费用项匹配状态名称
         */
        private String matchStatusName;
        /**
         * 匹配失败原因
         */
        private String matchFailReason;
        /**
         * 费用项确认状态汇总 toBeConfirm / partialConfirm / confirmed
         */
        private String reconciliationStatus;
        /**
         * 费用项确认状态名称
         */
        private String reconciliationStatusName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 创建人名称
         */
        private String createUserName;
        /**
         * 关联到的 ERP 物流单号（来自 ref join logistics_bill，给前端展示）
         */
        private String matchedLogisticsBillCode;
    }

    /**
     * 导出 Excel 参数
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
         * 勾选的 id 集合
         */
        private List<String> ids;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends ListDTO {
        /**
         * 行内费用项
         */
        private List<LogisticsReconDetailSubDTO.ListDTO> subList;
    }

    /**
     * 行明细批量按 ids 查询
     */
    @Data
    @NoArgsConstructor
    public static class ListByIdsDTO {
        /**
         * 对账明细 id 集合
         */
        @NotEmpty(message = "id集合不能为空")
        private List<String> ids;
    }

    /**
     * 费用项数量合计（按 match_status 分组）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * tab 标识：all / unmatched / matching / matched / failed
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 行明细 tab 统计入参
     */
    @Data
    @NoArgsConstructor
    public static class TabListParamDTO {
        /**
         * 对账单 id
         */
        @NotBlank(message = "对账单id不能为空")
        private String mainId;
    }

    /**
     * 导入匹配（按对账费用项批量触发"合并 & 匹配"）
     */
    @Data
    @NoArgsConstructor
    public static class ImportMatchDTO {
        /**
         * 对账单 id
         */
        @NotBlank(message = "对账单id不能为空")
        private String mainId;

        /**
         * 待匹配的对账费用项 id 集合
         */
        @NotEmpty(message = "对账费用项id集合不能为空")
        private List<String> detailSubIds;
    }

    /**
     * 手动匹配（批量指定 detail_sub ↔ ERP 四个业务单号）
     */
    @Data
    @NoArgsConstructor
    public static class ManualMatchDTO {
        /**
         * 手动匹配明细
         */
        @NotEmpty(message = "手动匹配明细不能为空")
        private List<ManualMatchItemDTO> itemList;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 手动匹配明细
     */
    @Data
    @NoArgsConstructor
    public static class ManualMatchItemDTO {
        /**
         * 对账费用项 id
         */
        @NotBlank(message = "对账费用项id不能为空")
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
     * 新增费用单（基于对账费用项补建物流费用单后绑定）
     */
    @Data
    @NoArgsConstructor
    public static class AddLogisticsBillCostDTO {
        /**
         * 对账费用项 id
         */
        @NotBlank(message = "对账费用项id不能为空")
        private String detailSubId;

        /**
         * 业务字段后续按 logistics_bill / logistics_bill_cost 实际入参类型化补齐
         */
        @NotNull(message = "新建费用单参数不能为空")
        private Map<String, Object> billCostPayload;
    }
}
