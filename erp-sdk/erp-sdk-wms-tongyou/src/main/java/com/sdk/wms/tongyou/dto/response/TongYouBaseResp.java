package com.sdk.wms.tongyou.dto.response;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * tongyou基础响应类
 * @author will
 * @date 2025/11/13 10:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TongYouBaseResp<T> {

    /**
     * 错误码：0为成功，其他为失败
     */
    private String error;

    private T data;
    /**
     * 错误提示信息
     */
    private String content;


    public static <T> TongYouBaseResp<T> error(String msg) {
        TongYouBaseResp<T> apiResult = new TongYouBaseResp<>();
        apiResult.setError("F");
        apiResult.setContent(msg);
        return apiResult;
    }

    public static <T> TongYouBaseResp<T> error(String formatedErrMsg, Object... params) {
        TongYouBaseResp<T> apiResult = new TongYouBaseResp<>();
        apiResult.setError("F");
        String msg = CharSequenceUtil.format(formatedErrMsg,params);
        apiResult.setContent(msg);
        return apiResult;
    }
}
