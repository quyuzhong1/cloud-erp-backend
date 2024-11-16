package com.common.business.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.StringUtils;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.FeignServiceException;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.netflix.client.ClientException;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MappingException;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * @Classname: GlobalExceptionHandler
 * @CreateTime: 2023-04-13  19:34
 * @Author: zhangchunlin
 */
@Slf4j
@RestControllerAdvice(basePackages = {
        "com.erp.server.scm.controller.api",
        "com.erp.server.wms.controller.api",
        "com.erp.server.workflow.controller.api",
        "com.erp.server.auth.controller.api",
        "com.erp.server.bi.controller.api",
        "com.erp.server.plm.controller.api",
        "com.erp.server.sys.controller.api",
        "com.erp.server.oms.controller.api",
        "com.erp.server.tms.controller.api",
        "com.erp.server.srm.controller.api",
        "com.erp.server.dmp.controller.api",
        "com.erp.server.mrp.controller.api",

        "com.erp.server.scm.controller.pda",
        "com.erp.server.wms.controller.pda",
        "com.erp.server.workflow.controller.pda",
        "com.erp.server.auth.controller.pda",
        "com.erp.server.bi.controller.pda",
        "com.erp.server.plm.controller.pda",
        "com.erp.server.sys.controller.pda",
        "com.erp.server.oms.controller.pda"

})
public class GlobalExceptionHandler {

    @ExceptionHandler({ServiceException.class})
    public ApiResult resolveException(ServiceException e) {
        log.error("系统异常：{}", e.getMsg());
        ApiResult result = new ApiResult();
        result.setCode(e.getCode());
        result.setMsg(e.getMsg());
        // 某些异常需要返回data
        if (Objects.nonNull(e.getData())) {
            result.setData(e.getData());
        }
/*        if(ApiError.ERROR_401.code.equals(result.getCode())){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }else {
            response.setStatus(HttpServletResponse.SC_OK);
        }*/
        return result;
    }

//    /**
//     * 系统异常 监测
//     * @param e 异常
//     * @return  ApiResult
//     */
//    @ExceptionHandler({Exception.class})
//    public ApiResult resolveException(Exception e) {
//        log.error("系统异常：{}", null == e.getMessage()?e.toString(): e.getMessage());
//        ApiResult result = new ApiResult();
//        result.setCode(1000000);
//        result.setMsg(null==e.getMessage()?e.toString():e.getMessage());
//        // 某些异常需要返回data
//        if (Objects.nonNull(e.getStackTrace())) {
//            result.setData(e.getStackTrace());
//        }
//        return result;
//    }



    @ExceptionHandler(value = FeignServiceException.class)
    public ApiResult resolveException(FeignServiceException e) {
        log.error("系统异常：{}", e.getMsg(), e);
        ApiResult result = new ApiResult();
        result.setCode(e.getCode());
        result.setMsg(e.getMsg());

/*        if(ApiError.ERROR_401.code.equals(result.getCode())){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }else {
            response.setStatus(HttpServletResponse.SC_OK);
        }*/

        return result;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult resolveException(MethodArgumentNotValidException e) {
        log.error("系统异常：", e);
        ApiResult result = new ApiResult();
        result.setCode(ApiError.ERROR_1000.code);
        result.setMsg(e.getBindingResult().getFieldError().getDefaultMessage());
        return result;
    }


    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ApiResult resolveException(HttpRequestMethodNotSupportedException e) {
        log.error("系统异常：", e);
        return ApiResult.error(ApiError.ERROR_405);
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ApiResult resolveException(MissingServletRequestParameterException e) {
        log.error("系统异常：", e);
        return ApiResult.error(ApiError.ERROR_600);
    }

    @ExceptionHandler(value = BindException.class)
    public ApiResult resolveException(BindException e) {
        log.error("系统异常：", e);
        List<ObjectError> fieldErrors = e.getBindingResult().getAllErrors();
        if (fieldErrors != null && fieldErrors.size() > 0) {
            ObjectError objectError = ValidatorUtil.getPermanentError(fieldErrors);
            return ApiResult.error(ApiError.ERROR_99999.code, objectError.getDefaultMessage());
        }
        return ApiResult.error(ApiError.ERROR_99999);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ApiResult resolveException(DataIntegrityViolationException e) {
        log.error("系统异常：", e);
        if (e.getMessage().contains("value too long")) {
            return ApiResult.error(ApiError.ERROR_1025);
        } else {
            return ApiResult.error(ApiError.Default);
        }
    }

    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public ApiResult resolveException(MaxUploadSizeExceededException e) {
        log.error("系统异常：", e);
        return ApiResult.error(ApiError.ERROR_1021);
    }


    @ExceptionHandler(value = IllegalStateException.class)
    public ApiResult resolveException(IllegalStateException e) {
        log.error("系统异常：", e);
        if (!StringUtils.isEmpty(e.getMessage()) && e.getMessage().contains("No instances available for")) {
            return ApiResult.error(ApiError.ERROR_1023);
        } else {
            return ApiResult.error(ApiError.Default);
        }
    }

    @ExceptionHandler(value = MissingRequestHeaderException.class)
    public ApiResult resolveException(MissingRequestHeaderException e) {
        log.error("系统异常：", e);
        return ApiResult.error(ApiError.ERROR_400);
    }

    @ExceptionHandler(value = DuplicateKeyException.class)
    public ApiResult resolveException(DuplicateKeyException e) {
        log.error("系统异常:", e);
        if (!StringUtils.isEmpty(e.getMessage()) && e.getMessage().contains("Duplicate entry")
                && e.getMessage().contains("for key")) {
            String duplicateKey = e.getMessage().substring(e.getMessage().indexOf("Duplicate entry") + 15, e.getMessage().indexOf("for key"));
            return ApiResult.error(ApiError.ERROR_1024.code, StrUtil.format("数据【{}】重复，请修改后再提交", duplicateKey));
        } else {
            return ApiResult.error(ApiError.ERROR_1024);
        }
    }

    @ExceptionHandler(value = IllegalMonitorStateException.class)
    public ApiResult resolveException(IllegalMonitorStateException ex) {
        log.error("系统异常:", ex);
        if (StrUtils.isNotEmpty(ex.getMessage()) && ex.getMessage().contains("attempt to unlock lock, not locked by current thread by node id")) {
            return ApiResult.error(ApiError.ERROR_1026);
        } else {
            return ApiResult.error(ApiError.Default);
        }
    }

    @ExceptionHandler(value = NullPointerException.class)
    public ApiResult resolveException(NullPointerException ex) {
        log.error("系统异常:", ex);
        return ApiResult.error(ApiError.Default);
    }

    @ExceptionHandler(value = ClientException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult resolveException(ClientException ex) {
        log.error("系统异常:", ex);
        return ApiResult.error(ApiError.ERROR_1023);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ApiResult resolveException(HttpMessageNotReadableException e) {
        log.error("系统异常：", e);
        if (StrUtils.isNotEmpty(e.getMessage())) {
            return ApiResult.error(ApiError.ERROR_600.code, ApiError.ERROR_600.msg + ":" + e.getMessage());
        }
        return ApiResult.error(ApiError.ERROR_600);
    }

    @ExceptionHandler(value = MappingException.class)
    public ApiResult resolveException(MappingException e) {
        log.error("系统异常：", e);
        return ApiResult.error(ApiError.ERROR_COPY_ERROR);
    }

    @ExceptionHandler(value = ClientAbortException.class)
    @ResponseBody
    protected ApiResult<?> handlerClientAbortException(HttpServletRequest request, HttpServletResponse response, Throwable ex) {
        //日志自己处理
        log.warn("in clientAbortException handler,ex:{}", ex.getMessage());

        //此处一定要返回null了，因为客户端已经断开连接，返回请求没啥用了
        return null;
    }
}