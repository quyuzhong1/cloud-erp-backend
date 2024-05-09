package com.common.business.dto;

import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.StateEnumValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformOrderQueryDTO {

    /**
     * b2c销售单id
     */
    @NotBlank(message = "b2c销售单id不能为空")
    private String soB2cId;

    /**
     * 平台订单号
     */
    @NotBlank(message = "平台订单号不能为空")
    private String platformCode;

    /**
     * 销售平台
     */
    @NotBlank(message = "销售平台不能为空")
    @StateEnumValue(clazz = PlatformDictEnum.class, message = "销售平台有误")
    private String dictPlatform;

    /**
     * 店铺id
     */
    @NotBlank(message = "店铺id不能为空")
    private String shopId;
}
