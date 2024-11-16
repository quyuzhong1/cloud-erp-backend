package com.erp.model.wms.dto.inventory;

import com.erp.model.wms.enums.inventory.InventoryOperationModeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Classname: InOutStockCoreDTO
 * @Description: 出入库核心实体
 * @CreateTime: 2023-05-10  11:28
 * @Author: zhangchunlin
 */
@Data
public class InOutStockCoreDTO implements Serializable {

    /**
     * 仓库
     */
    private String warehouseId;

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
     * 原单明细id
     */
    private String sourceDetailId;

    /**
     * sku id
     */
    private String skuId;

    /**
     * sku编码
     */
    private String skuNo;

    /**
     * 库位id（没有不用传输，某些单据不需要选择库位信息）
     */
    private String warehouseLocation;

    /**
     * 库存变更数量
     */
    /**
     * 增加或减少库存都传正数，程序判断正数或负数
     */
    private Integer qty;

    /**
     * 操作类型；默认为审核，后补单时需赋值，反审核有专门的方法入口
     */
    private InventoryOperationModeEnum operationMode =  InventoryOperationModeEnum.APPROVE;

    /**
     * 库存锁等待时间  默认： 5s
     */
    private Long lockWaitTime;


}