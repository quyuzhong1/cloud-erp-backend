package com.erp.model.oms.dto;


import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class RefreshShopTokenDTO {
    /**
     * 店铺id
     */
    private String shopId;

    @NotNull(message = "平台编号不能为空")
    @StateEnumValue(clazz = PlatformDictEnum.class, message = "平台编号有误")
    private String platformCode;
}
