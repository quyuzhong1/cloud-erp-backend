package com.cloud.erp.common.common.exception;

import com.cloud.erp.common.common.ApiError;
import com.cloud.erp.common.common.ApiRest;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理类
 *
 * @Classname ServiceExceptionHandler
 * @Description TODO
 * @Date 2022-07-06 14:17
 * @Created by yl
 */

@RestControllerAdvice
public class ServiceExceptionHandler {
    /**
     * 应用到所有@RequestMapping注解方法，在其执行之前初始化数据绑定器
     *
     * @param binder
     */
    @InitBinder
    public void initWebBinder(WebDataBinder binder) {

    }


    /**
     * 把值绑定到Model中，使全局@RequestMapping可以获取到该值
     *
     * @param model
     */
    @ModelAttribute
    public void addAttribute(Model model) {

    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiRest defaultErrorHandler(MethodArgumentNotValidException e) {
        ApiRest result = new ApiRest();
        result.setCode(ApiError.ERROR_10000.code);
        result.setMsg(e.getBindingResult().getFieldError().getDefaultMessage());
        return result;
    }
}
