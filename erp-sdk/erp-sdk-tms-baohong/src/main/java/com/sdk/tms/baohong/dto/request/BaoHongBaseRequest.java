package com.sdk.tms.baohong.dto.request;

import com.sdk.tms.baohong.api.order.CreateOrderInfo;
import com.sdk.tms.baohong.api.order.HeaderRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaoHongBaseRequest {

    @NotNull(message = "分页参数不能为空")
    protected Integer page;

    @NotNull(message = "分页参数不能为空")
    @Max(value = 100,message = "每页数量最大值为100")
    protected Integer pageSize;

    private HeaderRequest headerRequest;
    private CreateOrderInfo orderInfo;
}
