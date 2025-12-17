package com.sdk.wms.damai.dto.response;

import cn.hutool.core.text.CharSequenceUtil;
import jnr.ffi.annotations.In;
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
public class DaMaiPageBaseResp<T> {

    private Integer page;

    private Integer limit;

    private Integer count;

    private Integer totalPage;

    private Integer code;

    private T data;
    /**
     * 错误提示信息
     */
    private String msg;

    private Boolean countSql;

    public static <T> DaMaiPageBaseResp<T> error(String msg) {
        DaMaiPageBaseResp<T> apiResult = new DaMaiPageBaseResp<>();
        apiResult.setMsg(msg);
        return apiResult;
    }

    public static <T> DaMaiPageBaseResp<T> error(String formatedErrMsg, Object... params) {
        DaMaiPageBaseResp<T> apiResult = new DaMaiPageBaseResp<>();
        String msg = CharSequenceUtil.format(formatedErrMsg,params);
        apiResult.setMsg(msg);
        return apiResult;
    }
}
