package com.erp.model.tms.enums;

import lombok.Getter;

/**
 * @author liuruipeng
 * 调用物流第三方平台返回结果枚举
 * @date 2023年11月10日 9:12
 */
public enum LogisticsPlatformResultEnum {

    SUCESS("200","调用成功"),
    FAILURE("500","调用失败,物流平台:{}，订单号:{}，失败原因:{}")
    ;

    /**
     * 类型
     */
    @Getter
    private String code;
    /**
     * 名称
     */
    @Getter
    private String desc;

    LogisticsPlatformResultEnum(String code, String name){
        this.code = code;
        this.desc = name;
    }

}
