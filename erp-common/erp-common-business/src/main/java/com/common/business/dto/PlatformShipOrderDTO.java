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

    /**
     * 提交平台标识发货唯一key:{平台代号}_{平台单号}_{店铺ID}
     */
    @NotBlank(message = "提交平台标识发货唯一key不能为空")
    private String submitPlatformUniqueKey;

    /**
     * 是否手动标发
     */
    private boolean falseDeliveryFlag;

    /**
     * 是否查询拆分前的原订单
     */
    private boolean hasFindSourcePlatformOrder = false;
}
