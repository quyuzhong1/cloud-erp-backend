package com.erp.tms.aliexpress.model.handover.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.tms.aliexpress.model.handover.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName CloudPrintRequest
 * @description: TODO
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudPrintRequest implements Serializable {
    /**
     * 大包运单号
     */
    @NotBlank(message = "大包运单号不能为空")
    @JSONField(name = "tracking_number")
    private String trackingNumber;

    /**
     *
     * 大包物流单LP号
     */
    @NotBlank(message = "大包物流单LP号不能为空")
    @JSONField(name = "order_code")
    private String orderCode;

    /**
     * 用户信息
     */
    @JSONField(name = "user_info")
    private UserInfo userInfo;

    /**
     *
     * ISV名称，ISV：ISV-ISV英文或拼音名称、商家ERP：SELLER-商家英文或拼音名称
     */
    @NotBlank(message = "ISV名称不能为空")
    @JSONField(name = "client")
    private String client;
    /**
     * 多语言
     */
    @JSONField(name = "locale")
    private String locale;
}
