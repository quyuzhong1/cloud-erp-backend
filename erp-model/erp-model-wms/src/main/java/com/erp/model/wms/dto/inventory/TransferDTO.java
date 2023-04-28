package com.erp.model.wms.dto.inventory;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryOperationModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Classname: InventoryTransferDTO
 * @Description: 调拨操作请求实体，一个操作会导致2个仓库的库存状态变化适合使用；暂只考虑一个仓只能有一种状态变更
 * @CreateTime: 2023-04-26  14:34
 * @Author: zhangchunlin
 */
@Data
public class TransferDTO implements Serializable {

    /**
     * 提示：远程调用需增加分布式锁 @GlobalTransactional(rollBack=Exception.class)
     */
    @NotNull(message = "单据类型不能为空")
    @StateEnumValue(clazz = SourceTypeEnum.class,message = "单据类型有误")
    private SourceTypeEnum sourceType;

    @NotEmpty(message = "单据id不能为空")
    private String sourceId;

    @NotEmpty(message = "单据编号不能为空")
    private String sourceCode;

    @NotNull(message = "单据日期不能为空")
    private LocalDate billDate;

    /**
     * 可能没有单据明细id
     */
    private String sourceDetailId;

    @NotEmpty(message = "当前仓库组织不能为空")
    private String curOrgId;

    @NotEmpty(message = "当前仓库不能为空")
    private String curWarehouseId;

    /**
     * 当前仓库位id（可以传输，某些单据没有库位信息）
     */
    private String curWarehouseLocation;

    /**
     * 需要修改的库存状态（可以不传）
     */
    private InventoryStatusEnum curInventoryStatus;

    @NotEmpty(message = "目的仓库组织不能为空")
    private String targetOrgId;

    @NotEmpty(message = "目的仓库不能为空")
    private String targetWarehouseId;

    /**
     * 目的仓库位id（可以传输，某些单据没有库位信息）
     */
    private String targetWarehouseLocation;

    /**
     * 需要修改的库存状态（可以不传）
     */
    private InventoryStatusEnum targetCurInventoryStatus;

    @NotEmpty(message = "sku不能为空")
    private String skuId;

    @NotEmpty(message = "sku编码不能为空")
    private String skuNo;

    /**
     * 当前仓库存增加或减少（如果指定了库存状态，此字段必填）
     */
    private InventoryModeEnum curInventoryMode;

    /**
     * 目的仓库存增加或减少（如果指定了库存状态，此字段必填）
     */
    private InventoryModeEnum targetInventoryMode;

    /**
     * 增加或减少库存都传正数，程序判断正数或负数；当前仓和目的仓操作数量正反相等
     */
    @NotNull(message = "库存变更数量不能为空")
    private Integer qty;

    /**
     * 操作类型；默认为审核，后补单时需赋值，反审核有专门的方法入口
     */
    private InventoryOperationModeEnum operationMode =  InventoryOperationModeEnum.APPROVE;


}