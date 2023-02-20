package com.erp.model.sys.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @Classname ThirdPlatformEnums
 * @Description TODO
 * @Date 2022-11-14 11:22
 * @Created by yl
 */
@NoArgsConstructor
@AllArgsConstructor
public enum ThirdPlatformEnums {
    FS("FS","飞书"),
    DD("DD","订单"),
    QYWX("QYWX","企业微信");

    public String code;
    public String name;
}
