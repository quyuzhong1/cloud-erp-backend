package com.sdk.wms.zhongbao.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author zdy
 * @ClassName WarehouseResponse
 * @description: TODO
 * @date 2026年03月02日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChannelResponse extends PageResponse {
    private List<Channel> list;

    @Data
    public static class Channel {
        //渠道名称
        private String name;
        //英文名称
        private String nameEn;
        //渠道代码
        private String code;
        //渠道类型:1=>物流渠道,2=>面单渠道
        private Integer methodType;
        //是否支持面单:1=>是,-1=>否
        private Integer isDelivery = -1;
        //是否支持普通订单:1=>是,-1=>否
        private Integer isOrder = -1;
        //是否支持退货:1=>是,-1=>否
        private Integer isReturn = -1;
        //是否支持转单:1=>是,-1=>否
        private Integer isTransferOrder = -1;
        //是否支持签名服务:1=>是,-1=>否
        private Integer isSignature = -1;
        //是否支持保险服务:1=>是,-1=>否
        private Integer isInsurance = -1;
        //是否支持拆单:1=>是,-1=>否
        private Integer isSplitOrder = -1;
        //备注
        private String remark;
        private Warehouse openWarehouse;
    }

    @Data
    public static class Warehouse {
        //仓库代码
        private String warehouseCode;
    }
}
