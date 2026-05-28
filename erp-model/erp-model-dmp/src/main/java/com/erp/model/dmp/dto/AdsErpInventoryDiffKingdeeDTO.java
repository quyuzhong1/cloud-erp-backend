package com.erp.model.dmp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 金蝶库存差异请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
 */
@Data
@NoArgsConstructor
public class AdsErpInventoryDiffKingdeeDTO implements Serializable {


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
         * 金蝶期末数量
         */
        private Integer kingdeeClosingQty;

        /**
         * 期末差异数量
         */
        private Integer diffClosingQty;

        public static StatisticsDTO init() {
            return new StatisticsDTO(0, 0, 0);
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
         * 来源平台：gyy，kingdee，mabang
         */
        private String sourceSystem;

        /**
         * 核对周期（YYYY-MM）
         */
        private String checkMonth;

        /**
         * 核对仓库ID
         */
        private String warehouseId;

        /**
         * 核对仓库编码
         */
        private String warehouseCode;

        /**
         * 核对系统仓库名称
         */
        private String warehouseName;

        /**
         * 核对SKU_ID
         */
        private String skuId;

        /**
         * 核对SKU编码
         */
        private String skuNo;

        /**
         * 核对产品名称
         */
        private String productName;

        /**
         * ERP期末在库
         */
        private Integer closingQty;

        /**
         * ERP期末在途
         */
        private Integer intransitQty;

        /**
         * 金蝶仓库ID
         */
        private String kingdeeWarehouseId;

        /**
         * 金蝶仓库编码
         */
        private String kingdeeWarehouseCode;

        /**
         * 金蝶仓库名称
         */
        private String kingdeeWarehouseName;

        /**
         * 金蝶仓位编码
         */
        private String kingdeeWarehouseLocation;

        /**
         * 金蝶仓位名称
         */
        private String kingdeeWarehouseLocationName;

        /**
         * 金蝶SKU_ID
         */
        private String kingdeeSkuId;

        /**
         * 金蝶SKU编码
         */
        private String kingdeeSkuNo;

        /**
         * 金蝶产品名称
         */
        private String kingdeeProductName;

        /**
         * 货主
         */
        private String kingdeeOwner;

        /**
         * 本月期初
         * 金蝶期初在库库存
         */
        private Integer kingdeeInitQty;

        /**
         * 本期-入库
         * 金蝶本期在库入库数量
         */
        private Integer kingdeeInstockQty;

        /**
         * 本期-出库
         * 金蝶本期在库出库数量
         */
        private Integer kingdeeOutstockQty;

        /**
         * 本月期末
         * 金蝶期末在库库存
         */
        private Integer kingdeeClosingQty;

        /**
         * 金蝶期末在途库存
         */
        private Integer kingdeeClosingIntransitQty;

        /**
         * 金蝶本期在途出库数量
         */
        private Integer kingdeeOutstockIntransitQty;

        /**
         * 金蝶本期在途入库数量
         */
        private Integer kingdeeInstockIntransitQty;

        /**
         * 期末在途差异数量
         */
        private Integer diffClosingIntransitQty;

        /**
         * 期末在库差异数量
         */
        private Integer diffClosingQty;

        /**
         * 差异说明备注
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
     *
     */
    @Data
    @NoArgsConstructor
    public static class GenerateDiffDTO {

        /**
         * 生成月份:格式:202511
         */
        @NotBlank(message = "核算周期不能为空")
        private String checkMonth;

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
         * 核对仓库ID
         */
        private String warehouseId;

        /**
         * 核对仓库编码
         */
        private String warehouseCode;

        /**
         * 核对系统仓库名称
         */
        private String warehouseName;

        /**
         * 核对SKU_ID
         */
        private String skuId;

        /**
         * 核对SKU编码
         */
        private String skuNo;

        /**
         * 核对产品名称
         */
        private String productName;

        /**
         * ERP期末在库
         */
        private Integer closingQty;

        /**
         * ERP期末在途
         */
        private Integer intransitQty;

        /**
         * 金蝶仓库ID
         */
        private String kingdeeWarehouseId;

        /**
         * 金蝶仓库编码
         */
        private String kingdeeWarehouseCode;

        /**
         * 金蝶仓库名称
         */
        private String kingdeeWarehouseName;

        /**
         * 金蝶仓位编码
         */
        private String kingdeeWarehouseLocation;

        /**
         * 金蝶仓位名称
         */
        private String kingdeeWarehouseLocationName;

        /**
         * 金蝶SKU_ID
         */
        private String kingdeeSkuId;

        /**
         * 金蝶SKU编码
         */
        private String kingdeeSkuNo;

        /**
         * 金蝶产品名称
         */
        private String kingdeeProductName;

        /**
         * 货主
         */
        private String kingdeeOwner;

        /**
         * 金蝶期末在库库存
         */
        private Integer kingdeeClosingQty;

        /**
         * 金蝶本期在库出库数量
         */
        private Integer kingdeeOutstockQty;

        /**
         * 金蝶本期在库入库数量
         */
        private Integer kingdeeInstockQty;

        /**
         * 金蝶期末在途库存
         */
        private Integer kingdeeClosingIntransitQty;

        /**
         * 金蝶本期在途出库数量
         */
        private Integer kingdeeOutstockIntransitQty;

        /**
         * 金蝶本期在途入库数量
         */
        private Integer kingdeeInstockIntransitQty;

        /**
         * 期末在途差异数量
         */
        private Integer diffClosingIntransitQty;

        /**
         * 期末在库差异数量
         */
        private Integer diffClosingQty;

        /**
         * 差异说明备注
         */
        private String remark;


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
         * 核对仓库ID
         */
        @NotBlank(message = "核对仓库ID不能为空")
        @Size(max = 96, message = "核对仓库ID最大长度不能超过96位")
        private String warehouseId;

        /**
         * 核对仓库编码
         */
        @NotBlank(message = "核对仓库编码不能为空")
        @Size(max = 96, message = "核对仓库编码最大长度不能超过96位")
        private String warehouseCode;

        /**
         * 核对系统仓库名称
         */
        private String warehouseName;

        /**
         * 核对SKU_ID
         */
        @NotBlank(message = "核对SKU_ID不能为空")
        @Size(max = 192, message = "核对SKU_ID最大长度不能超过192位")
        private String skuId;

        /**
         * 核对产品名称
         */
        private String productName;

        /**
         * ERP期末在库
         */
        @NotNull(message = "ERP期末在库不能为空")
        private Integer closingQty;

        /**
         * ERP期末在途
         */
        @NotNull(message = "ERP期末在途不能为空")
        private Integer intransitQty;

        /**
         * 金蝶仓库ID
         */
        @NotBlank(message = "金蝶仓库ID不能为空")
        @Size(max = 96, message = "金蝶仓库ID最大长度不能超过96位")
        private String kingdeeWarehouseId;

        /**
         * 金蝶仓库编码
         */
        @NotBlank(message = "金蝶仓库编码不能为空")
        @Size(max = 96, message = "金蝶仓库编码最大长度不能超过96位")
        private String kingdeeWarehouseCode;

        /**
         * 金蝶仓库名称
         */
        private String kingdeeWarehouseName;

        /**
         * 金蝶仓位编码
         */
        private String kingdeeWarehouseLocation;

        /**
         * 金蝶仓位名称
         */
        private String kingdeeWarehouseLocationName;

        /**
         * 金蝶SKU_ID
         */
        @NotBlank(message = "金蝶SKU_ID不能为空")
        @Size(max = 192, message = "金蝶SKU_ID最大长度不能超过192位")
        private String kingdeeSkuId;

        /**
         * 金蝶SKU编码
         */
        @NotBlank(message = "金蝶SKU编码不能为空")
        @Size(max = 192, message = "金蝶SKU编码最大长度不能超过192位")
        private String kingdeeSkuNo;

        /**
         * 金蝶产品名称
         */
        private String kingdeeProductName;

        /**
         * 货主
         */
        private String kingdeeOwner;

        /**
         * 金蝶期末在库库存
         */
        @NotNull(message = "金蝶期末在库库存不能为空")
        private Integer kingdeeClosingQty;

        /**
         * 金蝶本期在库出库数量
         */
        @NotNull(message = "金蝶本期在库出库数量不能为空")
        private Integer kingdeeOutstockQty;

        /**
         * 金蝶本期在库入库数量
         */
        @NotNull(message = "金蝶本期在库入库数量不能为空")
        private Integer kingdeeInstockQty;

        /**
         * 金蝶期末在途库存
         */
        @NotNull(message = "金蝶期末在途库存不能为空")
        private Integer kingdeeClosingIntransitQty;

        /**
         * 金蝶本期在途出库数量
         */
        @NotNull(message = "金蝶本期在途出库数量不能为空")
        private Integer kingdeeOutstockIntransitQty;

        /**
         * 金蝶本期在途入库数量
         */
        @NotNull(message = "金蝶本期在途入库数量不能为空")
        private Integer kingdeeInstockIntransitQty;

        /**
         * 期末在途差异数量
         */
        @NotNull(message = "期末在途差异数量不能为空")
        private Integer diffClosingIntransitQty;

        /**
         * 期末在库差异数量
         */
        @NotNull(message = "期末在库差异数量不能为空")
        private Integer diffClosingQty;

        /**
         * 差异说明备注
         */
        private String remark;


    }


}