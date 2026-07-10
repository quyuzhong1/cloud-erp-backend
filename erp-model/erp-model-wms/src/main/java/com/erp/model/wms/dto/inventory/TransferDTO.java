package com.erp.model.wms.dto.inventory;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
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
     * 当前虚拟仓库id
     */
    private String curVirtualWarehouseId;
    /**
     * 目的仓库
     */
    @NotEmpty(message = "目的仓库不能为空")
    private String targetWarehouseId;
    /**
     * 目的虚拟仓id
     */
    private String targetVirtualWarehouseId;

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

    /**
     * 库存状态运行期覆盖（仅对「调入端 TARGET」生效）：非空时调入仓库的 inventoryStatus
     * 取此值，调出仓库 inventoryStatus 仍按交易规则配置（一般为 {@link InventoryStatusEnum#USABLE}）。
     * <p>
     * 设计为「单边覆盖」是因为调拨业务的物理语义是：调出仓的物理库存搬到调入仓，
     * 调出端的库存分类必须与调出仓的实际库存匹配，否则会出现「调出仓 0 不良品库存」
     * 的库存不足报错；调入端则可以根据业务需要切换分类。
     * <p>
     * 当前已知用例：wego 海外仓签收 {@code defective_product_flag=true} 时，
     * 在途仓 USABLE 调出 → 目的仓 DEFECTIVE_PRODUCT 调入。
     * <p>
     * 为空时按原有规则走，保证全部历史调用链路行为不变。
     */
    private InventoryStatusEnum dictInventoryStatus;

    /**
     * 库存状态运行期覆盖（仅对「调出端 CURRENT」生效）：非空时调出仓库的 inventoryStatus
     * 取此值，调入仓库 inventoryStatus 仍按 {@link #dictInventoryStatus} 或交易规则配置。
     * <p>
     * 用于直接调拨单明细显式指定「调出库存状态」(out_inventory_status) 的场景，
     * 例如从冻结 / 不良品库存桶调出。为空时按原有交易规则走，保证历史链路行为不变。
     */
    private InventoryStatusEnum curInventoryStatus;

}