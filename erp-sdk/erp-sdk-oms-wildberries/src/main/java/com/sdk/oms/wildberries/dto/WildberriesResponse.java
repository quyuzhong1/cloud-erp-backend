package com.sdk.oms.wildberries.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zdy
 * @ClassName WildberriesResponse
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WildberriesResponse extends BaseResponse{
    /**
     * isoTime
     * ZonedDateTime zdt = ZonedDateTime.parse(isoTime);
     * zdt.withZoneSameInstant(java.time.ZoneId.of("Asia/Shanghai"))
     */
    @JsonProperty("TS")
    private String TS;


}
