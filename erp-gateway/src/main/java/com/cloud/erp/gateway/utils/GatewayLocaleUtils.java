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
            return messageSource.getMessage(apiError.getMessageKey(), args, locale);
        } catch (NoSuchMessageException e) {
            return apiError.getMsg();
        }
    }

    private Locale resolveLocale(ServerHttpRequest request) {
        String lang = request.getHeaders().getFirst("Accept-Language");
        if (lang != null && lang.toLowerCase().startsWith("en")) {
            return Locale.US;
        }
        return Locale.SIMPLIFIED_CHINESE;
    }
}

