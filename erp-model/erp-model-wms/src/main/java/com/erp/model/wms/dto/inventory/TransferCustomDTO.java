package com.erp.model.wms.dto.inventory;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Classname: TransferCustomDTO
 * @Description: 调拨操作请求实体，一个操作会导致2个仓库的库存状态变化适合使用；暂只考虑一个仓只能有一种状态变更，且无法走交易规则
 * @CreateTime: 2023-05-10  15:09
 * @Author: zhangchunlin
 */
@Data
public class TransferCustomDTO extends InventoryStockBaseDTO implements Serializable {

    /**
     * 单据来源
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
     * 原单据明细id
     */
    private String sourceDetailId;

    /**
     * 当前仓库组织
     */
    /*
    @NotEmpty(message = "当前仓库组织不能为空")
    private String curOrgId;
     */

    /**
     * 当前仓库
     */
    @NotEmpty(message = "当前仓库不能为空")
    private String curWarehouseId;

    /**
     * 当前仓库位id（没有不用传输，某些单据不需要选择库位信息）
     */
    private String curWarehouseLocation;

    /**
     * 需要修改的库存状态
     */
    @NotNull(message = "当前仓库存状态不能为空")
    private InventoryStatusEnum curInventoryStatus;

    /**
     * 目的仓库组织
     */
    /*
    @NotEmpty(message = "目的仓库组织不能为空")
    private String targetOrgId;
     */

    /**
     * 目的仓库
     */
    @NotEmpty(message = "目的仓库不能为空")
    private String targetWarehouseId;

    /**
     * 目的仓库位id
     * （有的话请务必传输，没有不用传输，某些单据不需要选择库位信息）
     */
    private String targetWarehouseLocation;

    /**
     * 库存状态
     * 需要修改的库存状态
     */
    @NotNull(message = "目的仓库存状态不能为空")
    private InventoryStatusEnum targetCurInventoryStatus;

    /**
     * sku id
     */
    @NotEmpty(message = "sku不能为空")
    private String skuId;

    /**
     * sku编号
     */
    @NotEmpty(message = "sku编码不能为空")
    private String skuNo;

    /**
     * 当前仓库存增加或减少（如果指定了库存状态，此字段必填）
     * 从规则中配置的交易不用配置，特殊情况需要人工指定库存状态的则需要传输，库存增加或减少（如果指定了库存状态，此字段必填）
     */
    @NotNull(message = "当前仓库存操作类型不能为空")
    private InventoryModeEnum curInventoryMode;

    /**
     * 目的仓库存增加或减少（如果指定了库存状态，此字段必填）
     * 从规则中配置的交易不用配置，特殊情况需要人工指定库存状态的则需要传输，库存增加或减少（如果指定了库存状态，此字段必填）
     */
    @NotNull(message = "目的仓库存操作类型不能为空")
    private InventoryModeEnum targetInventoryMode;

    /**
     * 增加或减少库存都传正数，程序判断正数或负数；当前仓和目的仓操作数量正反相等
     */
    @NotNull(message = "库存变更数量不能为空")
    @Min(value = 1,message = "库存变更数量不能小于1")
    private Integer qty;


}