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
 * @Description: 出入库业务按业务类型，规则配置在表中，出入库只针对一个仓库的操作
 * @CreateTime: 2023-04-27  10:35
 * @Author: zhangchunlin
 */
@Data
public class InventoryInOutStockDTO implements Serializable {

    /**
     * 库存交易信息
     */
    @NotNull(message = "库存交易信息不能为空")
    @Size(min = 1, message = "请至少传输一个库存交易信息")
    @Valid
    private List<InOutStockDTO> members;

    /**
     * 业务类型
     */
    @NotNull(message = "业务类型不能为空")
    @StateEnumValue(clazz = InventoryBusinessTypeEnum.class,message = "业务类型有误")
    private String businessType;

}