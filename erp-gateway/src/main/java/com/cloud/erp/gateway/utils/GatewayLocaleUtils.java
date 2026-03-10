package com.cloud.erp.gateway.utils;

import com.common.core.enums.ApiError;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class GatewayLocaleUtils {

    private final MessageSource messageSource;

    public GatewayLocaleUtils(@Qualifier("gatewayMessageSource") MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(ApiError apiError, ServerHttpRequest request, Object... args) {
        Locale locale = resolveLocale(request);
        try {
            String msg = messageSource.getMessage(apiError.getMessageKey(), args, locale);
            // ⚠️ 防止返回 key 本身
            if (msg == null || msg.equals(apiError.getMessageKey())) {
                return format(apiError.getMsg(), args);
            }
            return msg;
        } catch (NoSuchMessageException e) {
            // 国际化异常 → 中文兜底
            return format(apiError.getMsg(), args);
        }
    }

    private Locale resolveLocale(ServerHttpRequest request) {
        String lang = request.getHeaders().getFirst("Accept-Language");
        if (lang != null && lang.toLowerCase().startsWith("en")) {
            return Locale.US;
        }
        return Locale.SIMPLIFIED_CHINESE;
    }
    private static String format(String msg, Object... args) {
        return args == null || args.length == 0
                ? msg
                : java.text.MessageFormat.format(msg, args);
    }
}

