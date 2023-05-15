package com.erp.model.wms.dto.inventory;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname: InventoryUnApproveDTO
 * @Description: 库存批量反审核操作
 * @CreateTime: 2023-04-28  16:44
 * @Author: zhangchunlin
 */
@Data
public class InventoryBatchUnApproveDTO implements Serializable {

    @NotNull(message = "单据来源不能为空")
    @StateEnumValue(clazz = InventorySourceTypeEnum.class, message = "单据来源错误")
    private InventorySourceTypeEnum sourceType;

    /**
     * 待反审核的原建议单据id
     */
    @NotEmpty(message = "单据id集合不能为空")
    @Size(min = 1, message = "至少需要上传一条单据")
    private List<String> billIds;

}