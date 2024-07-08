package com.erp.model.wms.dto.inventory;

import com.common.business.validator.ValidGroup;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @Classname: InventoryTransactionDTO
 * @Description: 库存交易信息类
 * @CreateTime: 2024.07.04
 * @Author: Edison.qu
 */
@Data
public class InventoryTransactionDTO implements Serializable {

    /**
     * 库存交易id
     */
    private String id;

    // 是否忽略交易（冗余字段）
    private boolean isIgnoreTransaction = false;
    // 是否允许负库存（冗余字段）
    private boolean allowNegativeInventory = false;

    /**
     * 交易批次号
     */
    @NotEmpty(message = "交易批次号 不能为空", groups = {ValidGroup.Update.class})
    private String transactionNo;

    /**
     * 交易规则id
     */
    private String transactionRuleId;

    /**
     * 库存Id
     */
    private String inventoryId;

    /**
     * sku id
     */
    @NotEmpty(message = "sku id不能为空")
    private String skuId;

    /**
     * sku编码
     */
    @NotEmpty(message = "sku编码不能为空")
    private String skuNo;

    /**
     * 货主组织id
     */
    @NotEmpty(message = "库存组织 不能为空", groups = {ValidGroup.Update.class})
    private String orgId;

    //库存组织名称（冗余字段）
    @NotEmpty(message = "库存组织名称 不能为空", groups = {ValidGroup.Update.class})
    private String orgName;

    /**
     * 仓库id
     */
    @NotEmpty(message = "仓库id 不能为空", groups = {ValidGroup.Update.class})
    private String warehouseId;

    //仓库名称（冗余字段）
    @NotEmpty(message = "仓库名称 不能为空", groups = {ValidGroup.Update.class})
    private String warehouseName;

    /**
     * 库位id（没有不用传输，某些单据不需要选择库位信息）
     */
    @NotEmpty(message = "仓位 不能为空", groups = {ValidGroup.Update.class})
    private String warehouseLocation;

    //仓位名称（冗余字段）
    private String warehouseLocationName;

    /**
     * 仓库存状态
     */
    @NotEmpty(message = "库存状态 不能为空", groups = {ValidGroup.Update.class})
    private String inventoryStatus;

    // 库存状态名称（冗余字段）
    @NotEmpty(message = "库存状态名称 不能为空", groups = {ValidGroup.Update.class})
    private String inventoryStatusName;

    /**
     * 单据日期
     */
    @NotNull(message = "单据日期不能为空")
    private LocalDate billDate;

    /**
     * 业务类型
     */
    @NotNull(message = "单据类型不能为空")
    private String sourceType;

    @NotNull(message = "业务类型不能为空")
    private String dictBizType;

    // 业务类型名称（冗余字段）
    private String sourceTypeName;

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
     * 原单明细id
     */
    @NotEmpty(message = "原单明细id不能为空")
    private String sourceDetailId;

    /**
     * 库存交易数量
     * 增加或减少库存都传正数，程序判断正数或负数
     */
    @NotNull(message = "库存变更数量不能为空")
    private Integer qty;

    // 用户id （冗余字段）
    @NotEmpty(message = "用户ID 不能为空", groups = {ValidGroup.Update.class})
    private String userId;

    // 用户名称 （冗余字段）
    @NotEmpty(message = "用户姓名 不能为空", groups = {ValidGroup.Update.class})
    private String userName;
}