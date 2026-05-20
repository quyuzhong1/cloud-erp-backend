package com.sdk.wms.zhongbao.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: wtr
 * @Date: 2026/3/11 8:59
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverseasOutboundCancelResponse implements Serializable {
    //总数量
    private Integer totalQty;
    //成功数量
    private Integer successQty;
    //失败数量
    private Integer failQty;
    //成功列表
    private List<Order> successList;
    //失败列表
    private List<Order> failList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Order {
        //单号
        private String orderNo;
        //信息
        private String message;
    }
}
