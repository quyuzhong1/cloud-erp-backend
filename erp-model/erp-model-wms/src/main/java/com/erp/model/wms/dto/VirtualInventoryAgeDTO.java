package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 虚拟仓库龄
 * @author will
 * @date 2024/12/3 17:32
 */
@Data
@NoArgsConstructor
public class VirtualInventoryAgeDTO implements Serializable {

    /**
     * 虚拟仓新增对象
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * skuId
         */
        private String skuId;
        /**
         * 实体仓id
         */
        private String warehouseId;
        /**
         * 虚拟仓库id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓库存
         */
        private Integer virtualQty;
        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;
        /**
         * 虚拟仓冻结库存
         */
        private Integer virtualFrozenQty;
        /**
         * 平均库存（反推）
         */
        private BigDecimal backAvgInventoryAge;
        /**
         * 平均库龄（正推）
         */
        private BigDecimal avgInventoryAge;

        /**
         * 库龄计算差异
         */
        private String isDiff;
        /**
         * 单据冻结数
         */
        private Integer frozenQty;
        /**
         * 冻结差异
         */
        private String frozenIsDiff;
        /**
         * 日期
         */
        private LocalDate date;
    }


    /**
     * 虚拟仓新增对象
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

    }
    /**
     * 分页显示数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * skuId【可排序】
         */
        private String skuId;
        /**
         * SKU【可排序】
         */
        private String skuNo;
        /**
         * 产品名称【可排序】
         */
        private String productName;
        /**
         * 仓库id【可排序】
         */
        private String warehouseId;
        /**
         * 仓库名称【可排序】
         */
        private String warehouseName;

        /**
         * 虚拟仓库【可排序】
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓库编号【可排序】
         */
        private String virtualWarehouseCode;
        /**
         * 虚拟仓库名称【可排序】
         */
        private String virtualWarehouseName;
        /**
         * 虚拟仓库存【可排序】
         */
        private Integer virtualQty;
        /**
         * 虚拟仓可用库存【可排序】
         */
        private Integer virtualUsableQty;
        /**
         * 虚拟仓冻结库存【可排序】
         */
        private Integer virtualFrozenQty;
        /**
         * 平均库存（反推）
         */
        private BigDecimal backAvgInventoryAge;
        /**
         * 平均库龄（正推）
         */
        private BigDecimal avgInventoryAge;
        /**
         * 库龄计算差异
         */
        private String isDiff;
        /**
         * 单据冻结数
         */
        private Integer frozenQty;
        /**
         * 冻结差异
         */
        private String frozenIsDiff;
        /**
         * 统计日期
         */
        private LocalDate date;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 区间信息
         */
        private HashMap<String,VirtualIntervalDTO> map;


    }

    /**
     * 查询参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VirtualIntervalDTO {
        /**
         * 区间数量
         */
        private Integer qty;
        /**
         * 占比
         */
        private String ratio;
    }



    /**
     * 查询参数
     */
    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 日期
         */
        @NotNull(message = "统计日期不能为空")
        private LocalDate date;
    }

    @Data
    @NoArgsConstructor
    public static class HisInventoryAgeParamDTO {
        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库Id不能为空")
        private String warehouseId;
        /**
         * 虚拟仓id
         */
        @NotBlank(message = "虚拟仓Id不能为空")
        private String virtualWarehouseId;

        /**
         * 日期
         */
        private List<LocalDate> dateList;
    }

    @Data
    @NoArgsConstructor
    public static class HisInventoryAgeDetailParamDTO {
        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库Id不能为空")
        private String warehouseId;
        /**
         * 虚拟仓id
         */
        @NotBlank(message = "虚拟仓Id不能为空")
        private String virtualWarehouseId;

        /**
         * 日期
         */
        @NotNull(message = "导出日期不能为空")
        private LocalDate date;

        /**
         * 区间集合（前端无需传值）
         */
        private List<Integer> daysList;
    }

    @Data
    @NoArgsConstructor
    public static class FrameParamDTO {
        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库Id不能为空")
        private String warehouseId;
        /**
         * 虚拟仓id
         */
        @NotBlank(message = "虚拟仓Id不能为空")
        private String virtualWarehouseId;

        /**
         * 日期
         */
        @NotNull(message = "统计日期不能为空")
        private LocalDate date;

        /**
         * 天数间隔区间
         */
        private String daysInterval;
    }

    /**
     * 历史库龄图形DTO
     */
    @Data
    @NoArgsConstructor
    public static class HisInventoryAgeChartDTO {

        /**
         * 日期集合
         */
        private List<LocalDate> dateList;
        /**
         * 平均库龄
         */
        private List<String> avgInventoryAgeList;
    }

    /**
     * 详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * skuId
         */
        private String skuId;
        /**
         * SKU
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 图片
         */
        private String skuImgUrl;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 虚拟仓库id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓编码
         */
        private String virtualWarehouseCode;
        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;

    }

    /**
     * 历史库龄DTO
     */
    @Data
    @NoArgsConstructor
    public static class HisInventoryAgeDTO {
        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseCode;
        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;
        /**
         * 统计日期
         */
        private LocalDate date;
        /**
         * 平均库龄（天）
         */
        private BigDecimal avgInventoryAgeDays;

        /**
         * 平均库龄（天）(去零)
         */
        private String avgInventoryAgeDaysStr;
    }

    /**
     * 历史库龄明细DTO
     */
    @Data
    @NoArgsConstructor
    public static class HisInventoryAgeDetailDTO extends HisInventoryAgeDTO{
        /**
         * 批次号
         */
        private String batchNo;
        /**
         * 批次出入库时间
         */
        private LocalDateTime tradeTime;
        /**
         * 流水号
         */
        private String transactionNo;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 来源类型名称
         */
        private String sourceTypeName;
        /**
         * 单据编号
         */
        private String sourceCode;
        /**
         * 库存状态
         */
        private String dictInventoryStatus;
        /**
         * 库存状态名称
         */
        private String dictInventoryStatusName;
        /**
         * 批次入库数量
         */
        private Integer batchQty;
        /**
         * 批次剩余数量
         */
        private Integer waitBatchQty;
        /**
         * 最后出库时间
         */
        private LocalDate lastOutstockDate;
        /**
         * 库龄（天）
         */
        private Integer inventoryAgeDays;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 虚拟仓数量
         */
        private Integer virtualQty;
        /**
         * 虚拟仓可用数量
         */
        private Integer virtualUsableQty;
        /**
         * 虚拟仓冻结数量
         */
        private Integer virtualFrozenQty;
    }



    /**
     *  历史库龄明细库存数据
     * @author will
     * @date 2024/12/10 17:09
     */
    @Data
    @NoArgsConstructor
    public static class viewHisInventoryAgeDetailDTO extends HisInventoryAgeDTO{
        /**
         * skuId
         */
        private String skuId;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 批次剩余数量
         */
        private Integer qty;

        /**
         * 批次剩余数量(逆推)
         */
        private Integer waitQty;

        /**
         * 虚拟仓数量
         */
        private Integer virtualQty;
        /**
         * 虚拟仓可用数量
         */
        private Integer virtualUsableQty;
        /**
         * 虚拟仓冻结数量
         */
        private Integer virtualFrozenQty;
        /**
         * 库龄（天）
         */
        private Integer inventoryAgeDays;
    }

    /**
     * 飞书通知DTO(按sku+仓库)
     */
    @Data
    @NoArgsConstructor
    public static class SendNoticeSkuDTO {
        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 仓库Id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 虚拟仓库Id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓库名称
         */
        private String virtualWarehouseName;
        /**
         * 平均库龄（反推）
         */
        private BigDecimal backAvgInventoryAge;
        /**
         * 平均库龄（正推）
         */
        private BigDecimal avgInventoryAge;
    }


    /**
     * 飞书通知DTO(汇总)
     */
    @Data
    @NoArgsConstructor
    public static class SendNoticeTotalDTO {
        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 虚拟仓库Id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓库名称
         */
        private String virtualWarehouseName;
        /**
         * 平均库龄（反推）
         */
        private BigDecimal backAvgInventoryAge;
        /**
         * 平均库龄（正推）
         */
        private BigDecimal avgInventoryAge;
    }


    @Data
    @NoArgsConstructor
    public static class InventoryAgeFlowParamDTO extends SortDTO{

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库Id不能为空")
        private String warehouseId;
        /**
         * 虚拟仓id
         */
        @NotBlank(message = "虚拟仓Id不能为空")
        private String virtualWarehouseId;

        /**
         * 批次号
         */
        @NotBlank(message = "批次号不能为空")
        private String batchNo;
    }

    /**
     * 批次库龄流水DTO
     */
    @Data
    @NoArgsConstructor
    public static class InventoryAgeFlowDTO {
        /**
         * 出入库时间
         */
        private LocalDate billDate;
        /**
         * 单据类型
         */
        private String sourceType;
        /**
         * 单据类型名称
         */
        private String sourceTypeName;
        /**
         * 单据单号
         */
        private String sourceCode;
        /**
         * 批次号
         */
        private String batchNo;
        /**
         * 操作
         */
        private String operationMode;
        /**
         * 操作名称
         */
        private String operationModeName;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编码
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 库存状态
         */
        private String dictInventoryStatus;
        /**
         * 库存状态名称
         */
        private String dictInventoryStatusName;
        /**
         * 出入库数量
         */
        private Integer qty;
        /**
         * 操作后库存数量
         */
        private Integer curInventoryQty;
        /**
         * 组织id
         */
        private String orgId;
        /**
         * 组织名称
         */
        private String orgName;
    }
}
