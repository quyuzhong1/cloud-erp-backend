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

/**
 * @author zdy
 * @ClassName SubbagRequest
 * @description: 批次追加大包 请求
 * @date 2024年02月02日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubbagRequest implements Serializable {
    /**
     * 用户信息
     */
    @NotNull(message = "用户信息不能为空")
    @JSONField(name = "user_info")
    private UserInfo userInfo;
    /**
     * 销售订单号
     */
    @NotBlank(message = "批次约揽订单号不能为空")
    @JSONField(name = "order_code")
    private String orderCode;
    /**
     *
     * 批次追加大包数，单次不超过30个，总的不超过50个
     */
    @NotNull(message = "批次追加大包数不能为空")
    @JSONField(name = "add_subbag_quantity")
    private Integer addSubbagQuantity;
    /**
     * 多语言
     */
    @NotBlank(message = "多语言不能为空")
    @JSONField(name = "locale")
    private String locale;
}
