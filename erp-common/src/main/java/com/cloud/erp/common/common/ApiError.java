package com.cloud.erp.common.common;


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
    Default(10000000, "操作失败！");







    public Integer code;
    public String msg;


}
