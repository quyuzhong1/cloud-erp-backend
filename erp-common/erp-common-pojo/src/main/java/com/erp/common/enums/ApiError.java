package com.erp.common.enums;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 全局错误码定义，用于定义接口的响应数据，
 * 枚举名称全部使用代码命名，在系统中调用，免去取名难的问题。
 */
@NoArgsConstructor
@AllArgsConstructor
public enum ApiError implements Serializable {


    /**
     * 服务调用异常
     */
    Default(10000000, "操作失败！"),
    ERROR_5000(5000, "存在越权访问URL"),
    ERROR_5001(5001, "未授权访问!"),

    ERROR_403(403, "您未登录,请登录后操作"),


    /**
     * 通用错误
     */
    ERROR_1000(1000, "参数不全或类型错误！"),
    ERROR_1001(1001, "两次密码不一致"),
    ERROR_1002(1002, "保存失败"),
    ERROR_1003(1003, "id不能为空"),
    ERROR_1004(1004, "未授权访问！"),
    ERROR_1005(1005, "登录失败!"),


    /**
     * 系统错误码
     * 从9010 开始  以端口号
     */
    ERROR_9010(9010, "手机号码已存在"),
    ERROR_9011(9011, "用户不存在"),
    ERROR_9012(9012, "用户不存在或者密码错误"),
    ERROR_9013(9013, "存在父级节点，无法删除"),
    ERROR_9014(9014, "核算公司不存在"),
    ERROR_9015(9015, "密码不能为空"),
    ERROR_9016(9016, "账号已禁用"),
    ERROR_9017(9017, "账号密码错误"),
    ERROR_9018(9018, "绑定账号失败"),
    ERROR_9019(9019, "账号尚未绑定请绑定后在登录"),
    ERROR_9020(9020, "改账号已经绑定"),



    ERROR_end(1000000, "系统错误");

    public Integer code;
    public String msg;


}
