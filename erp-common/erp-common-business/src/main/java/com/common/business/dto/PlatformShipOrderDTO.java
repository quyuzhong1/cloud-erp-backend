package com.common.business.dto;

import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.StateEnumValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformShipOrderDTO {
    /**
     * b2c销售单id
     */
    @NotBlank(message = "b2c销售单id不能为空")
    private String soB2cId;

    /**
     * 销售平台
     */
    @NotBlank(message = "销售平台不能为空")
    @StateEnumValue(clazz = PlatformDictEnum.class, message = "销售平台有误")
    private String dictPlatform;
}
