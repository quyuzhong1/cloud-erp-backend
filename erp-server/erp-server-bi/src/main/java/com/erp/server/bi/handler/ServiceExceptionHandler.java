package com.erp.server.bi.handler;

import com.erp.common.dto.base.ApiResult;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Classname ServiceExceptionHandler
 * @Description TODO
 * @Date 2022-12-08 15:29
 * @Created by yl
 */
@RestControllerAdvice
public class ServiceExceptionHandler {
    /**
     * 捕获ServiceException
     *
     * @param e
     * @return
     */

    @ExceptionHandler({ServiceException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResult serviceExceptionHandler(ServiceException e) {
        return new ApiResult(e);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult defaultErrorHandler(MethodArgumentNotValidException e) {
        ApiResult result = new ApiResult();
        result.setCode(ApiError.ERROR_1000.code);
        result.setMsg(e.getBindingResult().getFieldError().getDefaultMessage());
        return result;
    }

    @ExceptionHandler(value = Exception.class)
    public ApiResult defaultErrorHandler(Exception e) {
        ApiResult result = new ApiResult();
        result.setCode(ApiError.ERROR_500.code);
        result.setMsg(getLastCauseString(e,0));
        return result;
    }

    public String getExceptionMessage(Exception ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        ex.printStackTrace(writer);
        StringBuffer buffer = stringWriter.getBuffer();
        return buffer.toString();
    }




    /**
     * 获取最后导致错误的跟踪信息
     *
     * @param e           错误信息
     * @param limitLength 限定长度,0或-1表示不限定长度
     * @return
     */
    public static String getLastCauseString(Throwable e, int limitLength) {
        String errMsg = (e.getMessage() == null ? e.toString() : e.getMessage());
        try {
            errMsg = errMsg + ":\r\n " + getCauseTrace(getLastCause(e));
            if (limitLength > 0 && errMsg.length() > limitLength) {
                return errMsg.substring(0, limitLength);
            }
        } catch (Exception e2) {
        }
        return errMsg;
    }

    /**
     * 获取错误跟踪信息(全部)
     *
     * @param e 错误信息
     * @return
     */
    public static String getCauseTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    /**
     * 获取最后一个导致错误的跟踪信息
     *
     * @param e 错误信息
     * @return
     */
    public static Throwable getLastCause(Throwable e) {
        Throwable e1 = e.getCause();
        if (e1 != null) {
            //e1.printStackTrace();
            return getLastCause(e1);
        } else {
            //e.printStackTrace();
            return e;
        }
    }
}
