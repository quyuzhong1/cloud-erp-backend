package com.sdk.wms.antu.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AntuGetOutboundRefReq implements Serializable {

    //参考号,仅返回有效订单数据
    @JSONField(name = "reference_no")
    private String referenceNo;
}
