package com.erp.tms.aliexpress.model.handover;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName UserInfo
 * @description: TODO
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo implements Serializable {
    /**
     * 每个商家在ISV系统的唯一标识，一般为商家ISV账号的id
     */
    @JSONField(name = "top_user_key")
    private String topUserKey;
}
