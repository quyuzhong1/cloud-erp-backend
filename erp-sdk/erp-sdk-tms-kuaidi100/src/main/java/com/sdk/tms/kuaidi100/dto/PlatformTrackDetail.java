package com.sdk.tms.kuaidi100.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 功能描述：平台轨迹明细 DTO
 *
 * @author jack
 * @date 2026-03-31
 */
@Data
public class PlatformTrackDetail implements Serializable {
    /**
     * 运单号
     */
    private String trackNo;

    /**
     * 轨迹发生时间
     */
    private LocalDateTime trackTime;

    /**
     * 轨迹状态 (如：WAIT_COLLECT, TRACK_ING, SIGN 等)
     */
    private String status;

    /**
     * 订单总体状态 (通常与最后一条轨迹状态一致)
     */
    private String orderStatus;

    /**
     * 轨迹具体内容详情
     */
    private String content;

    /**
     * 轨迹发生的地点 (可选)
     */
    private String address;

    /**
     * 数据唯一识别码 (通常为 trackNo+content+trackTime 的 MD5)
     */
    private String md5;
}
