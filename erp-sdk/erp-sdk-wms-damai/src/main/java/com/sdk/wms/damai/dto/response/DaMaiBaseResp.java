package com.sdk.wms.damai.dto.response;

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
public class DaMaiBaseResp<T> {

    private String status;

    private T data;
    /**
     * 错误提示信息
     */
    private String msg;


    public static <T> DaMaiBaseResp<T> error(String msg) {
        DaMaiBaseResp<T> apiResult = new DaMaiBaseResp<>();
        apiResult.setStatus("fail");
        apiResult.setMsg(msg);
        return apiResult;
    }

    public static <T> DaMaiBaseResp<T> error(String formatedErrMsg, Object... params) {
        DaMaiBaseResp<T> apiResult = new DaMaiBaseResp<>();
        apiResult.setStatus("fail");
        String msg = CharSequenceUtil.format(formatedErrMsg,params);
        apiResult.setMsg(msg);
        return apiResult;
    }
}
