package com.common.core.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;


/**
 * @Classname: ResultUtil
 * @Description: TODO
 * @CreateTime: 2023-05-06  09:22
 * @Author: zhangchunlin
 */
@Slf4j
public class ResultUtil {

    /**
     * 远程调用判断
     * @param result
     */
    public static void checkRemoteResult(ApiResult result) {
        checkRemoteResult(result,null);
    }

    /**
     * 远程调用判断
     * @param result
     * @param errorMsg（优先使用传输的异常信息，默认使用下游服务的异常描述）
     */
    public static void checkRemoteResult(ApiResult result,String errorMsg) {
        if (result == null) {
            throw new ServiceException(ApiError.Default);
        }
        boolean success = result.getCode() == 200;
        if (!success) {
            log.error(StrUtil.format("远程调用获取数据失败:{}", JSONObject.toJSONString(result)));
            throw new ServiceException(result.getCode(), StrUtils.isEmpty(errorMsg) ? result.getMsg() : errorMsg);
        }
    }

}