package com.erp.model.wms.dto.inventory;

import com.erp.model.wms.enums.inventory.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Classname: InStockOrOutStockTransformDTO
 * @Description: TODO
 * @CreateTime: 2023-04-28  15:09
 * @Author: zhangchunlin
 */
@Data
public class InOutStockTransformDTO extends InventoryStockBaseDTO implements Serializable {

    /**
     * 单据类型
     */
    private InventorySourceTypeEnum sourceType;

    /**
     * 单据id
     */
    private String sourceId;

    /**
     * 单据编号
     */
    private String sourceCode;

    /**
     * 单据日期
     */
    private LocalDate billDate;

    /**
     * 可能没有单据明细id
     */
    private String sourceDetailId;

    /**
     * 仓库
     */
    private String warehouseId;

    /**
     * 仓库位id（没有不用传输，某些单据不需要选择库位信息）
     */
    private String warehouseLocation;

    /**
     * sku id
     */
    private String skuId;

    /**
     * sku编码
     */
    private String skuNo;

    /**
     * 库存交易数量
     * 增加或减少库存都传正数，程序判断正数或负数；当前仓和目的仓操作数量正反相等
     */
    private Integer qty;

    /**
     * 仓库选项
     */
    private InventoryWarehouseOptionEnum warehouseOptionEnum;

    /**
     * 仓库存状态
     */
    private InventoryStatusEnum inventoryStatus;

    /**
     * 仓库交易方向
     */
    private InventoryModeEnum inventoryMode;

    /**
     * 操作类型；默认为审核，后补单时需赋值，反审核有专门的方法入口
     */
    private InventoryOperationModeEnum operationMode =  InventoryOperationModeEnum.APPROVE;


}