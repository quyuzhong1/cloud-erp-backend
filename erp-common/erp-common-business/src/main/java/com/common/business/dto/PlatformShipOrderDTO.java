package com.common.business.dto;

import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.StateEnumValue;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
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
