package com.sdk.tms.track123.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName TrackingDetail
 * @description: 轨迹明细
 * @date 2023年11月07日
 * @version: 1.0
 */
@Data
public class OceanTrackingDetail implements Serializable {

    /**
     * 状态详情
     */
    private String eventDetails;

    /**
     * 状态更新时间
     */
    private String eventTime;

    /**
     * 轨迹状态
     */
    private String eventStatus;

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

    /**
     * 运输状态
     */
    private String transitStatus;

    /**
     * 运输子状态
     */
    private String transitSubStatus;

    /**
     * 船名字
     */
    private String vesselName;

    /**
     * 航线+航次
     */
    private String voyage;
}
