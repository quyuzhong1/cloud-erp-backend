package com.sdk.tms.track123.model.response;

import lombok.Builder;
import lombok.Data;

/**
 * @author Will
 * @version 1.0
 * @date 2024/4/9 16:49
 */
@Data
public class OceanCurrentStatus {


    /**
     * 状态详情
     */
    private String eventDetails;

    /**
     * 状态更新时间
     */
    private String eventTime;

    /**
     * 状态更新时间UTC
     */
    private String eventTimeUTC;

    /**
     * 状态发生地
     */
    private String locationName;

    /**
     * 状态发生地类型
     */
    private String locationType;

}
