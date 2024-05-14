package com.sdk.tms.track123.model.response;

import lombok.Data;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @date 2024/4/9 16:47
 */
@Data
public class OceanContainerInfo {
    /**
     * 箱号
     */
    private String containerNo;

    /**
     * 箱高度
     */
    private String containerSize;
    /**
     * 箱型号
     */
    private String containerType;
    /**
     *
     */
    private String containerDetails;
    /**
     * 最近一条轨迹的时间
     */
    private String lastTrackingTime;
    /**
     * 当前运输状态
     */
    private OceanCurrentStatus currentStatus;

    /**
     * 运输状态
     */
    private String transitStatus;

    /**
     * 运输子状态
     */
    private String transitSubStatus;

    /**
     * 船名
     */
    private String vesselName;

    /**
     * 航线+航次
     */
    private String voyage;

    /**
     * 轨迹明细
     */
    private List<OceanTrackingDetail> trackingDetails;
}
