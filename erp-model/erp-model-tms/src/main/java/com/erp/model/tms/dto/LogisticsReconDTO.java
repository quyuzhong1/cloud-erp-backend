package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流商对账单（主表）请求响应实体
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Data
@NoArgsConstructor
public class LogisticsReconDTO implements Serializable {

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * tab 标识（all / check_status[importing/pending/confirmed] / match_status）
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 对账月份 YYYY-MM
         */
        private String reconciliationMonth;
        /**
         * 业务类型
         */
        private String businessType;
        /**
         * 物流商/平台 id
         */
        private String supplierId;
        /**
         * 校验状态过滤（已合并导入状态）importing / pending / confirmed / all
         */
        private String checkStatus;
        /**
         * 匹配状态过滤 unmatched / partial / matched
         */
        private String matchStatus;
        /**
         * 关键字搜索（code / supplier_name 等）
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
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 批次单号
         */
        private String code;
        /**
         * 对账月份 YYYY-MM
         */
        private String reconciliationMonth;
        /**
         * 业务类型
         */
        private String businessType;
        /**
         * 匹配到的导入模板配置 id
         */
        private String cfgImportId;
        /**
         * 配置类型 logisticsSupplier / platform
         */
        private String cfgType;
        /**
         * 物流商/平台 id
         */
        private String supplierId;
        /**
         * 物流商/平台名称
         */
        private String supplierName;
        /**
         * 处理表格源 sheet 名称
         */
        private String sheetName;
        /**
         * 上传文件 URL
         */
        private String fileUrl;
        /**
         * 上传文件名
         */
        private String fileName;
        /**
         * 导入失败原因
         */
        private String importFailReason;
        /**
         * 校验状态（已合并导入状态）importing / pending / confirmed
         */
        private String checkStatus;
        /**
         * 校验状态名称
         */
        private String checkStatusName;
        /**
         * 匹配状态 unmatched / partial / matched（查询时按费用项 cost_count + 已匹配费用项数实时派生，非主表存储）
         */
        private String matchStatus;
        /**
         * 匹配状态名称
         */
        private String matchStatusName;
        /**
         * 导入明细行数
         */
        private Integer importCount;
        /**
         * 导入费用项条数
         */
        private Integer costCount;
        /**
         * 已匹配费用项数（查询时实时聚合 logistics_recon_detail_sub.match_status='matched'，非主表存储）
         */
        private Integer matchCount;
        /**
         * 对账总金额
         */
        private BigDecimal totalAmount;
        /**
         * 对账总金额（带币别符号展示）
         */
        private String totalAmountStr;
        /**
         * 匹配成功金额
         */
        private BigDecimal matchSuccessAmount;
        /**
         * 匹配成功金额（带币别符号展示）
         */
        private String matchSuccessAmountStr;
        /**
         * 匹配失败金额
         */
        private BigDecimal matchFailAmount;
        /**
         * 匹配失败金额（带币别符号展示）
         */
        private String matchFailAmountStr;
        /**
         * 总金额币别（多币别则空）
         */
        private String currency;
        /**
         * 币别符号
         */
        private String currencySymbol;
        /**
         * 校验人 id
         */
        private String checkUserId;
        /**
         * 校验人姓名
         */
        private String checkUserName;
        /**
         * 校验完成时间
         */
        private LocalDateTime checkTime;
        /**
         * 备注
         */
        private String remark;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 创建人名称
         */
        private String createUserName;
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
    public static class ViewDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 对账月份（展示格式：yyyy年M月份）
         */
        private String reconciliationMonth;
        /**
         * 物流商/平台名称
         */
        private String supplierName;
        /**
         * 对账总金额（带币别符号展示）
         */
        private String totalAmountStr;
    }

    /**
     * 通用字段（新增/修改共享）
     */
    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {
        /**
         * 对账月份 YYYY-MM
         */
        @NotBlank(message = "对账月份不能为空")
        @Size(max = 7, message = "对账月份最大长度不能超过7位")
        private String reconciliationMonth;

        /**
         * 业务类型
         */
        @NotBlank(message = "业务类型不能为空")
        private String businessType;

        /**
         * 匹配到的导入模板配置 id
         */
        private String cfgImportId;

        /**
         * 配置类型 logisticsSupplier / platform
         */
        private String cfgType;

        /**
         * 物流商/平台 id
         */
        private String supplierId;

        /**
         * 物流商/平台名称
         */
        private String supplierName;

        /**
         * 处理表格源 sheet 名称
         */
        private String sheetName;

        /**
         * 备注
         */
        @Size(max = 500, message = "备注最大长度不能超过500位")
        private String remark;
    }

    /**
     * 新增（一般由"导入"自动生成，少数场景手动新增）
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

    /**
     * 模板下载
     */
    @Data
    @NoArgsConstructor
    public static class DownloadTemplateDTO {
        /**
         * 导入模板配置 id（cfg_logistics_cost_import.id），决定 sheet 与列
         */
        @NotBlank(message = "导入模板配置id不能为空")
        private String cfgImportId;
    }

    /**
     * 导入触发
     * processingType：
     *   importOnly  - 仅导入，落对账单 + 明细，校验状态 pending
     *   importCheck - 导入并校验，落对账单 + 明细，校验状态 confirmed
     */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 导入模板配置 id（cfg_logistics_cost_import.id）
         */
        @NotBlank(message = "导入模板配置id不能为空")
        private String cfgImportId;

        /**
         * 对账月份 YYYY-MM
         */
        @NotBlank(message = "对账月份不能为空")
        @Size(max = 7, message = "对账月份最大长度不能超过7位")
        private String reconciliationMonth;

        /**
         * 文件 URL
         */
        @NotBlank(message = "文件URL不能为空")
        private String fileUrl;

        /**
         * 文件名
         */
        @NotBlank(message = "文件名不能为空")
        private String fileName;

        /**
         * 处理类型 importOnly / importCheck
         */
        @NotBlank(message = "处理类型不能为空")
        private String processingType;
    }

    /**
     * 预处理（试解析，不落库）
     */
    @Data
    @NoArgsConstructor
    public static class PreprocessingDTO {
        /**
         * 导入模板配置 id
         */
        @NotBlank(message = "导入模板配置id不能为空")
        private String cfgImportId;

        /**
         * 文件 URL
         */
        @NotBlank(message = "文件URL不能为空")
        private String fileUrl;

        /**
         * 文件名
         */
        @NotBlank(message = "文件名不能为空")
        private String fileName;
    }

    /**
     * 校验状态切换（批量）
     */
    @Data
    @NoArgsConstructor
    public static class UpdateCheckStatusDTO {
        /**
         * 对账单 id 集合
         */
        @NotEmpty(message = "id集合不能为空")
        private List<String> ids;

        /**
         * 目标状态 pending / confirmed
         */
        @NotBlank(message = "校验状态不能为空")
        private String checkStatus;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 合并 & 匹配（按对账单批量触发）
     */
    @Data
    @NoArgsConstructor
    public static class BatchMatchDTO {
        /**
         * 对账单 id 集合
         */
        @NotEmpty(message = "对账单id集合不能为空")
        private List<String> ids;
    }

    /**
     * 账单确认（批量）
     */
    @Data
    @NoArgsConstructor
    public static class BatchConfirmBillDTO {
        /**
         * 对账单 id 集合
         */
        @NotEmpty(message = "对账单id集合不能为空")
        private List<String> ids;
        /**
         * 物流费用单目标对账状态（ReconciliationStatusEnum：toBeConfirm / confirmed）
         */
        @NotBlank(message = "对账状态不能为空")
        private String reconciliationStatus;
    }

    /**
     * 解绑匹配
     */
    @Data
    @NoArgsConstructor
    public static class BatchUnbindMatchDTO {
        /**
         * 对账费用项 id 集合
         */
        @NotEmpty(message = "对账费用项id集合不能为空")
        private List<String> detailSubIds;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 配置外费用项补齐 cfg_cost_id
     */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateDetailSubCfgDTO {
        /**
         * 费用项 id 集合
         */
        @NotEmpty(message = "费用项id集合不能为空")
        private List<String> detailSubIds;

        /**
         * 费用项配置 id（cfg_cost.id）
         */
        @NotBlank(message = "费用项配置id不能为空")
        private String cfgCostId;

        /**
         * 费用项配置名称
         */
        private String cfgCostName;
    }
}
