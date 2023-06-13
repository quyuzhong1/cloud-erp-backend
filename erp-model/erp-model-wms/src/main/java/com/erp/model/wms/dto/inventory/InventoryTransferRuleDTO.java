package com.erp.model.wms.dto.inventory;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname: InventoryTransferRuleDTO
 * @Description: 调拨业务按业务规则
 * 规则配置无法配置在表中
 * 因为无法确定调整成什么状态，需要自定义规则，，适应于在2个仓库中出入，即一个仓库出一个仓库进，适应于直接调拨单，库存调整单
 * @CreateTime: 2023-04-28  15:49
 * @Author: zhangchunlin
 */
@Data
public class InventoryTransferRuleDTO implements Serializable {

    @NotNull(message = "sku信息不能为空")
    @Size(min = 1, message = "请至少传输一个sku信息")
    @Valid
    private List<TransferDTO> members;

    @NotNull(message = "业务类型不能为空")
    @StateEnumValue(clazz = InventoryBusinessTypeEnum.class,message = "业务类型有误")
    private String businessType;

    @NotNull(message = "规则信息不能为空")
    @Size(min = 1, message = "请至少传输一个规则信息")
    @Valid
    private List<TransactionRuleDTO> rules;

}