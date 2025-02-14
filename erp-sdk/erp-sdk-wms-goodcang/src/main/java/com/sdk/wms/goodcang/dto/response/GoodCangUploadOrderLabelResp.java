package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class GoodCangUploadOrderLabelResp extends CleanBaseDTO implements Serializable {

    //订单信息
    @JSONField(name = "order_code")
    private Integer orderCode;
}
