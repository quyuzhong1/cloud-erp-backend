package com.sdk.wms.zhongbao.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: wtr
 * @Date: 2026/3/11 8:58
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverseasOutboundCancelRequest {

    /**
     * 订单号列表
     */
    @JSONField(name = "orderNos")
    private List<String> orderNos;

    /**
     * 取消原因
     */
    @JSONField(name = "cancelRemark")
    private String  cancelRemark;

}
