package com.sdk.tms.kuaidi100.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 功能描述：快递100实时查询响应对象
 *
 * @author jack
 * @date 2026-03-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kuaidi100QueryResponse implements Serializable {

    /**
     * 响应消息（如 "ok"）
     */
    private String message;

    /**
     * 快递单号
     */
    private String nu;

    /**
     * 是否签收标记：0在途，1已签收
     */
    private String ischeck;

    /**
     * 快递公司编码
     */
    private String com;

    /**
     * HTTP 状态码
     */
    private String status;

    /**
     * 快递单当前状态，包括0在途、1揽收、2疑难、3签收、4退签、5派件、6退回等
     */
    private String state;

    /**
     * 轨迹数据明细
     */
    private List<Kuaidi100TrackData> data;

    /**
     * 轨迹数据项
     */
    @Data
    public static class Kuaidi100TrackData implements Serializable {
        /**
         * 时间，格式：2012-07-07 13:35:14
         */
        private String time;

        /**
         * 轨迹描述
         */
        private String context;

        /**
         * 状态描述（揽收、在途、派件、签收等）
         */
        private String status;

        /**
         * 行政区域代码
         */
        private String areaCode;

        /**
         * 行政区域名称
         */
        private String areaName;
    }
}
