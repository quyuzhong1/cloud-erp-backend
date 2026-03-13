package com.sdk.wms.zhongbao.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: wtr
 * @Date: 2026/3/12 15:51
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverseasOutboundQueryRequest {

    /**
     * 起始修改时间
     */
    @JSONField(name = "startUpdateTime")
    private String startUpdateTime;

    /**
     * 结束修改时间
     */
    @JSONField(name = "endUpdateTime")
    private String  endUpdateTime;
}
