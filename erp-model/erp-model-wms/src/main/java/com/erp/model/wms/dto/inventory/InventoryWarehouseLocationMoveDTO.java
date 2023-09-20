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
 * @Classname: InventoryWarehouseLocationMoveDTO
 * @Description: 仓位移动业务按业务类型，
 * 规则配置在表中，不需要手工指定更改成什么状态
 * 适应于在2个仓库中出入，即一个仓库出一个仓库进
 * @Author Luo_WG
 * @Date 2023/8/28 18:03
 **/
@Data
public class InventoryWarehouseLocationMoveDTO implements Serializable {

    @NotNull(message = "sku信息不能为空")
    @Size(min = 1, message = "请至少传输一个sku信息")
    @Valid
    private List<WarehouseLocationMoveDTO> members;

    @NotNull(message = "业务类型不能为空")
    @StateEnumValue(clazz = InventoryBusinessTypeEnum.class,message = "业务类型有误")
    private String businessType;
}
