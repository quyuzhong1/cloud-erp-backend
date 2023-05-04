package com.erp.model.wms.dto.inventory;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.SourceTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname: InventoryUnApproveDTO
 * @Description: 库存反审核操作
 * @CreateTime: 2023-04-28  16:44
 * @Author: zhangchunlin
 */
@Data
public class InventoryUnApproveDTO implements Serializable {

    @NotNull(message = "单据来源不能为空")
    @StateEnumValue(clazz = SourceTypeEnum.class, message = "单据来源错误")
    private SourceTypeEnum sourceType;

    /**
     * 待反审核的原建议单据id
     */
    @NotEmpty(message = "单据id不能为空")
    private String billId;

}