package com.erp.model.dmp.dto;

import java.util.Date;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.SortDTO;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.common.business.dto.AdvanceQueryDTO;

import java.util.Map;

/**
 * <p>
 * 平台库存差异请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
 */
@Data
@NoArgsConstructor
public class AdsErpInventoryDiffDTO implements Serializable {


    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsDTO {

        /**
         * ERP期末量
         */
        private Integer closingQty;

        /**
         * 平台期末数量
         */
        private Integer platformClosingQty;

        /**
         * 期末差异数量
         */
        private Integer diffClosingQty;

        public static StatisticsDTO init() {
            return new StatisticsDTO(0,0,0);
        }
    }


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
        private Map<String, String> sqlMap;

        /**
         * 勾选的id集合
         */
        private List<String> ids;

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
         * 数据唯一md5值
         */
        private String uniqueCode;

        /**
         * 来源平台：gyy，kingdee，mabang
         */
        private String sourceSystem;

        /**
         * 核对平台
         */
        private String checkPlatform;

        /**
         * 核对平台名称
         */
        private String checkPlatformName;

        /**
         * 平台账号编码
         */
        private String accountCode;

        /**
         * 店铺ID/海外仓授权ID
         */
        private String nextLevelId;

        /**
         * 业务类型
         */
        private String billTopic;

        /**
         * 来源ID
         */
        private String sourceId;

        /**
         * 最后一次当前数据是否变更（TRUE=已变更）
         */
        private String lastUpdateFlag;

        /**
         * 核对周期（YYYY-MM）
         */
        private String checkMonth;

        /**
         * 来源仓库ID
         */
        private String warehouseId;

        /**
         * 来源仓库编码
         */
        private String warehouseCode;

        /**
         * 来源系统仓库名称
         */
        private String warehouseName;

        /**
         * 来源系统SKU_ID
         */
        private String skuId;

        /**
         * 来源系统SKU编码
         */
        private String skuNo;

        /**
         * 来源系统产品名称
         */
        private String productName;

        /**
         * 本月期初库存
         */
        private Integer initQty;

        /**
         * 本期出库数量
         */
        private Integer outstockQty;

        /**
         * 本期入库数量
         */
        private Integer instockQty;

        /**
         * 本期在途数量
         */
        private Integer intransitQty;

        /**
         * 本月期末库存（计算值：期初+入库-出库）
         */
        private Integer closingQty;

        /**
         * 本月期末库存（每日来源数据）按照仓库核对月份月底的结余库存数量
         */
        private Integer estimatedClosingQty;

        /**
         * 平台在途期末库存-本月期末
         */
        private Integer platformClosingIntransitQty;

        /**
         * (目的仓)平台在库-本月期末(平台流水差异-本月期末(调整))
         */
        private Integer platformClosingQty;

        /**
         * (目的仓)平台仓期末库存合计（含在途）
         */
        private Integer platformClosingTotalQty;

        /**
         * 总差异数量（ERP期末-平台期末）/平台在库库存+平台在途-本月期末
         */
        private Integer diffClosingTotalQty;

        /**
         * 期末差异数量
         */
        private Integer diffClosingQty;

        /**
         * 在途差异数量
         */
        private Integer diffClosingIntransitQty;

        /**
         * 差异说明备注
         */
        private String remark;

        /**
         * 数据生成时间（业务生成时间）
         */
        private LocalDateTime finishTime;


        /**
         * 审核状态名称
         */
        private String approveStatusName;


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
     * 导出Excel
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
         * 勾选的id集合
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
         * 数据唯一md5值
         */
        private String uniqueCode;

        /**
         * 数据字段md5值（数据json+account_code+next_level_id+bill_topic）
         */
        private String dataEncrypt;

        /**
         * 来源平台：gyy，kingdee，mabang
         */
        private String sourceSystem;

        /**
         * 平台账号编码
         */
        private String accountCode;

        /**
         * 店铺ID/海外仓授权ID
         */
        private String nextLevelId;

        /**
         * 业务类型
         */
        private String billTopic;

        /**
         * 流程id
         */
        private String flowId;

        /**
         * ETL处理状态：ready=可处理，unready=不可处理
         */
        private String etlStatus;

        /**
         * 节点id
         */
        private String nodeId;

        /**
         * 实例id/etl任务id
         */
        private String instanceId;

        /**
         * 拉取任务id
         */
        private String taskId;

        /**
         * 来源ID
         */
        private String sourceId;

        /**
         * 最后一次当前数据是否变更（TRUE=已变更）
         */
        private String lastUpdateFlag;

        /**
         * 核对周期（YYYY-MM）
         */
        private String checkMonth;

        /**
         * 来源仓库ID
         */
        private String warehouseId;

        /**
         * 来源仓库编码
         */
        private String warehouseCode;

        /**
         * 来源系统仓库名称
         */
        private String warehouseName;

        /**
         * 来源系统SKU_ID
         */
        private String skuId;

        /**
         * 来源系统SKU编码
         */
        private String skuNo;

        /**
         * 来源系统产品名称
         */
        private String productName;

        /**
         * 本月期初库存
         */
        private Integer initQty;

        /**
         * 本期出库数量
         */
        private Integer outstockQty;

        /**
         * 本期入库数量
         */
        private Integer instockQty;

        /**
         * 本月期末库存（计算值：期初+入库-出库）
         */
        private Integer closingQty;

        /**
         * 本月期末库存（每日来源数据）按照仓库核对月份月底的结余库存数量
         */
        private Integer estimatedClosingQty;

        /**
         * 平台在途期末库存-本月期末
         */
        private Integer platformClosingIntransitQty;

        /**
         * (目的仓)平台在库-本月期末(平台流水差异-本月期末(调整))
         */
        private Integer platformClosingQty;

        /**
         * (目的仓)平台仓期末库存合计（含在途）
         */
        private Integer platformClosingTotalQty;

        /**
         * 总差异数量（ERP期末-平台期末）/平台在库库存+平台在途-本月期末
         */
        private Integer diffClosingTotalQty;

        /**
         * 期末差异数量
         */
        private Integer diffClosingQty;

        /**
         * 在途差异数量
         */
        private Integer diffClosingIntransitQty;

        /**
         * 差异说明备注
         */
        private String remark;

        /**
         * 数据生成时间（业务生成时间）
         */
        private LocalDateTime finishTime;


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
         * 数据唯一md5值
         */
        @NotBlank(message = "数据唯一md5值不能为空")
        @Size(max = 192, message = "数据唯一md5值最大长度不能超过192位")
        private String uniqueCode;

        /**
         * 数据字段md5值（数据json+account_code+next_level_id+bill_topic）
         */
        @NotBlank(message = "数据字段md5值（数据json+account_code+next_level_id+bill_topic）不能为空")
        @Size(max = 192, message = "数据字段md5值（数据json+account_code+next_level_id+bill_topic）最大长度不能超过192位")
        private String dataEncrypt;

        /**
         * 来源平台：gyy，kingdee，mabang
         */
        @NotBlank(message = "来源平台：gyy，kingdee，mabang不能为空")
        @Size(max = 192, message = "来源平台：gyy，kingdee，mabang最大长度不能超过192位")
        private String sourceSystem;

        /**
         * 平台账号编码
         */
        @NotBlank(message = "平台账号编码不能为空")
        @Size(max = 192, message = "平台账号编码最大长度不能超过192位")
        private String accountCode;

        /**
         * 店铺ID/海外仓授权ID
         */
        @NotBlank(message = "店铺ID/海外仓授权ID不能为空")
        @Size(max = 192, message = "店铺ID/海外仓授权ID最大长度不能超过192位")
        private String nextLevelId;

        /**
         * 业务类型
         */
        @NotBlank(message = "业务类型不能为空")
        @Size(max = 192, message = "业务类型最大长度不能超过192位")
        private String billTopic;

        /**
         * 流程id
         */
        private String flowId;

        /**
         * ETL处理状态：ready=可处理，unready=不可处理
         */
        @NotBlank(message = "ETL处理状态：ready=可处理，unready=不可处理不能为空")
        @Size(max = 192, message = "ETL处理状态：ready=可处理，unready=不可处理最大长度不能超过192位")
        private String etlStatus;

        /**
         * 节点id
         */
        private String nodeId;

        /**
         * 实例id/etl任务id
         */
        private String instanceId;

        /**
         * 拉取任务id
         */
        @NotBlank(message = "拉取任务id不能为空")
        @Size(max = 192, message = "拉取任务id最大长度不能超过192位")
        private String taskId;

        /**
         * 来源ID
         */
        @NotBlank(message = "来源ID不能为空")
        @Size(max = 192, message = "来源ID最大长度不能超过192位")
        private String sourceId;

        /**
         * 最后一次当前数据是否变更（TRUE=已变更）
         */
        @NotBlank(message = "最后一次当前数据是否变更（TRUE=已变更）不能为空")
        private String lastUpdateFlag;

        /**
         * 核对周期（YYYY-MM）
         */
        @NotBlank(message = "核对周期（YYYY不能为空")
        @Size(max = 96, message = "核对周期（YYYY最大长度不能超过96位")
        private String checkMonth;

        /**
         * 来源仓库ID
         */
        @NotBlank(message = "来源仓库ID不能为空")
        @Size(max = 96, message = "来源仓库ID最大长度不能超过96位")
        private String warehouseId;

        /**
         * 来源仓库编码
         */
        @NotBlank(message = "来源仓库编码不能为空")
        @Size(max = 96, message = "来源仓库编码最大长度不能超过96位")
        private String warehouseCode;

        /**
         * 来源系统仓库名称
         */
        private String warehouseName;

        /**
         * 来源系统SKU_ID
         */
        @NotBlank(message = "来源系统SKU_ID不能为空")
        @Size(max = 192, message = "来源系统SKU_ID最大长度不能超过192位")
        private String skuId;

        /**
         * 来源系统产品名称
         */
        private String productName;

        /**
         * 本月期初库存
         */
        @NotNull(message = "本月期初库存不能为空")
        private Integer initQty;

        /**
         * 本期出库数量
         */
        @NotNull(message = "本期出库数量不能为空")
        private Integer outstockQty;

        /**
         * 本期入库数量
         */
        @NotNull(message = "本期入库数量不能为空")
        private Integer instockQty;

        /**
         * 本月期末库存（计算值：期初+入库-出库）
         */
        @NotNull(message = "本月期末库存（计算值：期初+入库不能为空")
        private Integer closingQty;

        /**
         * 本月期末库存（每日来源数据）按照仓库核对月份月底的结余库存数量
         */
        @NotNull(message = "本月期末库存（每日来源数据）按照仓库核对月份月底的结余库存数量不能为空")
        private Integer estimatedClosingQty;

        /**
         * 平台在途期末库存-本月期末
         */
        @NotNull(message = "平台在途期末库存不能为空")
        private Integer platformClosingIntransitQty;

        /**
         * (目的仓)平台在库-本月期末(平台流水差异-本月期末(调整))
         */
        @NotNull(message = "(目的仓)平台在库不能为空")
        private Integer platformClosingQty;

        /**
         * (目的仓)平台仓期末库存合计（含在途）
         */
        @NotNull(message = "(目的仓)平台仓期末库存合计（含在途）不能为空")
        private Integer platformClosingTotalQty;

        /**
         * 总差异数量（ERP期末-平台期末）/平台在库库存+平台在途-本月期末
         */
        @NotNull(message = "总差异数量（ERP期末不能为空")
        private Integer diffClosingTotalQty;

        /**
         * 期末差异数量
         */
        @NotNull(message = "期末差异数量不能为空")
        private Integer diffClosingQty;

        /**
         * 在途差异数量
         */
        @NotNull(message = "在途差异数量不能为空")
        private Integer diffClosingIntransitQty;

        /**
         * 差异说明备注
         */
        private String remark;

        /**
         * 数据生成时间（业务生成时间）
         */
        private LocalDateTime finishTime;


    }

    /**
     * 更新物流状态
     */
    @Data
    @NoArgsConstructor
    public static class UpdateRemarkDTO {

        /**
         * id
         */
        @NotNull(message = "id不能为空")
        private String id;

        /**
         * 备注
         */
        private String remark;

    }


    /**
     * 仓库IDS
     */
    @Data
    @NoArgsConstructor
    public static class CfgSettingDTO  {

        /**
         * 仓库IDS
         */
        private List<String> warehouseIdList;

    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class GenerateDiffDTO  {

        /**
         * 生成月份:格式:202511
         */
        private String checkMonth;

    }


    /**
     * 仓库列表
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseListDTO {

        /**
         * 参考id
         */
        private String id;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 配置是否已选择
         */
        private Boolean selected;
    }
}