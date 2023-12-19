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
     * 销售平台
     */
    @NotBlank(message = "销售平台不能为空")
    @StateEnumValue(clazz = PlatformDictEnum.class, message = "销售平台有误")
    private String dictPlatform;

    /**
     * 第三方平台订单号
     */
    private String platformCode;

    /**
     * ERP订单编号
     */
    private String soCode;

    /**
     * 物流跟踪单号
     */
    private String trackNo;

    /**
     * 发货时间
     */
    private LocalDateTime shipDateTime;

    /**
     * 明细信息
     */
    private List<PlatformShipOrderDetailDTO> detailList;
}
