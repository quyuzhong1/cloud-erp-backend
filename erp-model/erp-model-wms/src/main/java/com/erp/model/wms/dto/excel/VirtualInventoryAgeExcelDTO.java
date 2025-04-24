package com.erp.model.wms.dto.excel;

import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

/**
 * 库龄分析导出excel
 * @author will
 * @date 2024/12/23 16:48
 */
@Data
@NoArgsConstructor
public class VirtualInventoryAgeExcelDTO {
    /**
     * SKU
     */
    private String skuNo;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 仓库名称
     */
    private String warehouseName;
    /**
     * 虚拟仓库编号
     */
    private String virtualWarehouseCode;
    /**
     * 虚拟仓库名称
     */
    private String virtualWarehouseName;
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
     * 平均库龄(天)
     */
    private BigDecimal backAvgInventoryAge;
    /**
     * 统计日期
     */
    private LocalDate date;

    /**
     * 单据冻结数
     */
    private Integer frozenQty;

    /**
     * 平均库龄(正)
     */
    private BigDecimal avgInventoryAge;
    /**
     * 库龄计算差异
     */
    private String isDiff;

    /**
     * 冻结库存差异
     */
    private String frozenIsDiff;

    /**
     * 区间信息
     */
    private HashMap<String, VirtualInventoryAgeDTO.VirtualIntervalDTO> map;
}
