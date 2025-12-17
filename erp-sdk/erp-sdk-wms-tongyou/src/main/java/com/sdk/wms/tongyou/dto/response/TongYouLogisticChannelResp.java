package com.sdk.wms.tongyou.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class TongYouLogisticChannelResp implements Serializable {

    /**
     *  物流渠道编码
     */
    @JSONField(name = "nums")
    private String logisticsChannelCode;
    /**
     * 物流渠道名称
     */
    @JSONField(name = "pname")
    private String logisticsChannelName;
    /**
     * 仓库编码
     */
    @JSONField(name = "storage")
    private String warehouseCode;
    /**
     * 区域
     */
    @JSONField(name = "area")
    private String area;
}
