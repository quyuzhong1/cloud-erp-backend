package com.sdk.wms.weishi.dto.response;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WeiShiBaseResp<T> {

    private Integer code;

    private T data;
    /**
     * 错误提示信息
     */
    private String msg;

    private Boolean success;

    public static <T> WeiShiBaseResp<T> error(String msg) {
        WeiShiBaseResp<T> apiResult = new WeiShiBaseResp<>();
        apiResult.setCode(-1);
        apiResult.setMsg(msg);
        apiResult.setSuccess(false);
        return apiResult;
    }

    public static <T> WeiShiBaseResp<T> error(String formatedErrMsg, Object... params) {
        WeiShiBaseResp<T> apiResult = new WeiShiBaseResp<>();
        apiResult.setCode(-1);
        String msg = CharSequenceUtil.format(formatedErrMsg,params);
        apiResult.setMsg(msg);
        apiResult.setSuccess(false);
        return apiResult;
    }
}
