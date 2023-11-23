package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *  平台城市字典dto,所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformCityDictDTO extends UniqueDto {

    /**
     *  区域id
     */
    private String regionId;

    /**
     *  父级区域id
     */
    private String parentRegionId;

    /**
     *  区域名称
     */
    private String regionName;

    /**
     *  区域等级
     */
    private String regionLevel;

}
