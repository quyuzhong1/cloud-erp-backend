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
 * @CreateTime: 2023-05-25  09:38
 * @Author: zhangchunlin
 */
@Data
public class InventoryInOutStockRuleDTO implements Serializable {

    @NotNull(message = "sku信息不能为空")
    @Size(min = 1, message = "请至少传输一个sku信息")
    @Valid
    private List<InOutStockDTO> members;

    @NotNull(message = "业务类型不能为空")
    @StateEnumValue(clazz = InventoryBusinessTypeEnum.class,message = "业务类型有误")
    private String businessType;

    @NotNull(message = "规则信息不能为空")
    @Size(min = 1, message = "请至少传输一个规则信息")
    @Valid
    private List<TransactionRuleDTO> rules;

}