package com.erp.tms.aliexpress.model.handover.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.tms.aliexpress.model.handover.AddressInfo;
import com.erp.tms.aliexpress.model.handover.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ResourceRecommendRequest
 * @description: 揽收资源推荐
 * @date 2024年02月02日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRecommendRequest implements Serializable {
    /**
     *解决方案编码
     */
    @JSONField(name = "solution_code")
    private String solutionCode;
    /**
     *发货方式：上门揽收DOOR_PICKUP, 自寄SELF_POST, 自送SELF_SEND
     */
    @JSONField(name = "pickup_type")
    private String pickupType;
    /**
     * 揽收信息
     */
    @NotNull(message = "揽收信息不能为空")
    @JSONField(name = "pickup_info")
    private AddressInfo pickInfo;

    /**
     * 用户信息
     */
    @NotNull(message = "用户信息不能为空")
    @JSONField(name = "user_info")
    private UserInfo userInfo;

}
