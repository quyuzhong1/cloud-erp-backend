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
 * @Classname: InventoryTransferDTO
 * @Description: TODO
 * @CreateTime: 2023-04-28  11:35
 * @Author: zhangchunlin
 */
@Data
public class InventoryTransferDTO implements Serializable {

    @NotNull(message = "sku信息不能为空")
    @Size(min = 1, message = "请至少传输一个sku信息")
    @Valid
    private List<TransferDTO> skus;

    @NotNull(message = "业务类型不能为空")
    @StateEnumValue(clazz = InventoryBusinessTypeEnum.class,message = "业务类型有误")
    private String businessType;

}