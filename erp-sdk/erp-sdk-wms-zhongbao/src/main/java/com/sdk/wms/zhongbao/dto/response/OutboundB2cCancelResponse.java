package com.sdk.wms.zhongbao.dto.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName OverseasInboundCreateRequest
 * @description: 海外仓入库单
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboundB2cCancelResponse implements Serializable {
    //总数量
    private Integer totalQty;
    //成功数量
    private Integer successQty;
    //失败数量
    private Integer failQty;
    //成功列表
    private List<OpenB2c> successList;
    //失败列表
    private List<OpenB2c> failList;



    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OpenB2c {
        //标记
        private String orderNo;
        //信息
        private String message;
    }
}
