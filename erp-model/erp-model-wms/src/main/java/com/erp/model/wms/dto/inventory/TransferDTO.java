package com.erp.model.wms.dto.inventory;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Classname: InventoryTransferDTO
 * @Description: 调拨操作请求实体，一个操作会导致2个仓库的库存状态变化适合使用；只考虑一个仓只能有一种状态变更
 * @CreateTime: 2023-04-26  14:34
 * @Author: zhangchunlin
 */
@Data
public class TransferDTO extends InventoryStockBaseDTO implements Serializable {

    /**
     * 单据类型
     */
    @NotNull(message = "单据类型不能为空")
    @StateEnumValue(clazz = InventorySourceTypeEnum.class,message = "单据类型有误")
    private InventorySourceTypeEnum sourceType;

    /**
     * 单据id
     */
    @NotEmpty(message = "单据id不能为空")
    private String sourceId;

    /**
     * 单据编号
     */
    @NotEmpty(message = "单据编号不能为空")
    private String sourceCode;

    /**
     * 单据日期
     */
    @NotNull(message = "单据日期不能为空")
    private LocalDate billDate;

    /**
     * 单据明细id
     * 有的话请务必传输
     */
    private String sourceDetailId;

    /**
     * 当前仓库
     */
    @NotEmpty(message = "当前仓库不能为空")
    private String curWarehouseId;

    /**
     * 当前仓库位id
     * （有的话请务必传输，没有不用传输，某些单据不需要选择库位信息）
     */
    private String curWarehouseLocation;


    /**
     * 目的仓库
     */
    @NotEmpty(message = "目的仓库不能为空")
    private String targetWarehouseId;

    /**
     * 目的仓库位id（没有不用传输，某些单据不需要选择库位信息）
     */
    private String targetWarehouseLocation;

    /**
     * 库存交易数量
     * 增加或减少库存都传正数，程序判断正数或负数；当前仓和目的仓操作数量正反相等
     */
    @NotNull(message = "库存变更数量不能为空")
    // @Min(value = 1,message = "库存变更数量不能小于1")
    private Integer qty;



}