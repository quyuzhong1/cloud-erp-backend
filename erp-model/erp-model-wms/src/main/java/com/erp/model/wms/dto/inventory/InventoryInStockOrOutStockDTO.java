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
 * @Classname: InventoryInStockOrOutStock
 * @Description: 出入库业务按业务类型，规则配置在表中，适应于在2个仓库中出入，即一个仓库出一个仓库进
 * @CreateTime: 2023-04-27  10:35
 * @Author: zhangchunlin
 */
@Data
public class InventoryInStockOrOutStockDTO implements Serializable {

    @NotNull(message = "sku信息不能为空")
    @Size(min = 1, message = "请至少传输一个sku信息")
    @Valid
    private List<InStockOrOutStockDTO> skus;

    @NotNull(message = "业务类型不能为空")
    @StateEnumValue(clazz = InventoryBusinessTypeEnum.class,message = "业务类型有误")
    private String businessType;

}