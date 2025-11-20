package com.sdk.wms.tongyou.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class TongYouInboundResp {
    /**
     * 参考单号
     */
    @JSONField(name = "waybill")
    private String waybill;
    /**
     * 0,待确认,1头程待入库,2头程已入库,3海外仓待入库,4海外仓已入库,5,异常订单,6确认未通过,7回收站
     * 只有7回收站状态，ERP才允许取消入库单成功，否则提示：三方仓单据未取消，ERP不允许取消
     */
    @JSONField(name = "order_pb")
    private String status;

}
