package com.erp.tms.aliexpress.model.handover;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName AddressInfo
 * @description: TODO
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressInfo implements Serializable {
    /**
     *
     * 退件地址
     */
    @JSONField(name = "address")
    private AddressBase address;

    /**
     *邮箱
     */
    @JSONField(name = "email")
    private String email;
    /**
     *移动电话, 校验格式：^1(3|4|5|6|7|8|9)\d{9}$
     */
    @JSONField(name = "mobile")
    private String mobile;
    /**
     *固定电话，可空，校验格式：(^0[\d]{2,3}-[\d]{7,8}$)|(^400[\d]{3,4}[\d]{3,4}$)|(400-[\d]{3,4}-[\d]{3,4}$)
     */
    @JSONField(name = "phone")
    private String phone;
    /**
     *
     * 退件联系人名称，必须包含中文字符
     */
    @JSONField(name = "name")
    private String name;
    /**
     *AE后台维护的退件地址ID
     */
    @JSONField(name = "address_id")
    private String addressId;
}
