package com.erp.tms.aliexpress.model.handover.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.tms.aliexpress.model.handover.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author zdy
 * @ClassName HandoverQueryRequest
 * @description: 打包信息查询
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverQueryRequest implements Serializable {
    /**
     * 用户信息
     */
    @JSONField(name = "user_info")
    private UserInfo userInfo;
    /**
     * 交接物物流订单编码,和交接物运单号参数可以任选其一即可
     */
    @JSONField(name = "order_code")
    private String orderCode;
    /**
     *交接物运单号，和交接物物流订单编码参数任选其一即可
     */
    @JSONField(name = "tracking_number")
    private String trackingNumber;
    /**
     * 客户端名称，ISV：ISV-ISV英文或拼音名称、商家ERP：SELLER-商家英文或拼音名称
     */
    @NotBlank(message = "客户端不能为空")
    @JSONField(name = "client")
    private String client;
    /**
     * 多语言
     */
    @JSONField(name = "locale")
    private String locale;
}
