package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
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
         * 统计日期
         */
        private LocalDate date;
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
        private Boolean isDiff;

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
    public static class VirtualIntervalDTO {
        /**
         * 区间数量
         */
        private Integer qty;
        /**
         * 占比
         */
        private Integer ratio;
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
        private LocalDate date;
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
        private Integer avgInventoryAgeDays;
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
         * 库龄（天）
         */
        private Integer inventoryAgeDays;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }
}
