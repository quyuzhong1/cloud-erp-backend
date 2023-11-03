package com.sdk.tms.yuntu.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author liuruipeng
 */
@Data
@Builder
public class YunTuPrintLabelRequest {

    //物流系统运单号，客户订单或跟踪号
    @NotBlank(message = "物流系统运单号，客户订单或跟踪号不能为空")
    private List<String> orderNumbers;

}
