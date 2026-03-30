package com.sdk.wms.zhongbao.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName OverseasInboundCancelResponse
 * @description: 取消入库单
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverseasInboundCancelResponse implements Serializable {
    //总数量
    private Integer totalQty;
    //成功数
    private Integer successQty;
    //失败数
    private Integer failQty;
    //成功列表
    private List<Order> successList;
    //失败列表
    private List<Order> failList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Order {
        //标记
        private String orderNo;
        //信息
        private String message;
    }
}
