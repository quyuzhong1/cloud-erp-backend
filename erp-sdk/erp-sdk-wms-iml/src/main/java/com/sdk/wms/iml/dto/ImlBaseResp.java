package com.sdk.wms.iml.dto;

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
public class ImlBaseResp<T> {

    /**
     * 错误码：0为成功，其他为失败
     */
    private Integer code;

    private T data;
    /**
     * 错误提示信息
     */
    private String message;


    public static <T> ImlBaseResp<T> error(String msg) {
        ImlBaseResp<T> apiResult = new ImlBaseResp<>();
        apiResult.setCode(-1);
        apiResult.setMessage(msg);
        return apiResult;
    }

    public static <T> ImlBaseResp<T> error(String formatedErrMsg,Object... params) {
        ImlBaseResp<T> apiResult = new ImlBaseResp<>();
        apiResult.setCode(-1);
        String msg = CharSequenceUtil.format(formatedErrMsg,params);
        apiResult.setMessage(msg);
        return apiResult;
    }
}
