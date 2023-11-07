package com.sdk.tms.disifang.model.base;

import lombok.Data;

/**
 * @author zdy
 * @ClassName ResponseMsg
 * @description: TODO
 * @date 2023年11月02日
 * @version: 1.0
 */
@Data
public class ResponseMsg {
    public String result;

    public String msg;

    public error errors;

    public Object data;

    class error {
        String errorCode;

        String errorMsg;
    }

    public static ResponseMsg fial(String msg) {
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setMsg(msg);
        return responseMsg;
    }
}
