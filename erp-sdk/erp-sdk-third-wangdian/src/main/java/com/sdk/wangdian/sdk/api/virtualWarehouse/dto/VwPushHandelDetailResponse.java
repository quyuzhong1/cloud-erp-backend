package com.sdk.wangdian.sdk.api.virtualWarehouse.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * 虚拟仓分货单推送返回数据
 */
@Getter
@Setter
public class VwPushHandelDetailResponse {
    /**
     * 状态：0成功 100失败
     */
    @SerializedName("status")
    private Integer status;
    /**
     * 状态：0成功 100失败
     */
    @SerializedName("data")
    private MsgDto data;
    /**
     * 信息
     */
    @SerializedName("message")
    private String message;

    @Getter
    @Setter
    public static class MsgDto {
        /**
         * 状态：0成功 100失败
         */
        @SerializedName("status")
        private Integer status;
        /**
         * 信息
         */
        @SerializedName("message")
        private String message;
    }
}
