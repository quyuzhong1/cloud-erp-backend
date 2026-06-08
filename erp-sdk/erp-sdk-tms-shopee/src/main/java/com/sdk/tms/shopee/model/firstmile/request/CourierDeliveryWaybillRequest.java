package com.sdk.tms.shopee.model.firstmile.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 快递寄送模式获取面单请求。
 */
@Data
@Builder
public class CourierDeliveryWaybillRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "绑定ID列表不能为空")
    @JSONField(name = "binding_id_list")
    private List<String> bindingIdList;
}
