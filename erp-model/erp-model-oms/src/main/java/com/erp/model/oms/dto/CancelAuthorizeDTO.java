package com.erp.model.oms.dto;

import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class CancelAuthorizeDTO implements Serializable {
    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 平台编号
     */
    @NotNull(message = "平台编号不能为空")
    @StateEnumValue(clazz = PlatformDictEnum.class, message = "平台编号有误")
    private String platformCode;
}
