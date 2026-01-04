package com.common.core.utils;

import com.common.core.enums.ApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageUtils {

    private static MessageSource messageSource;

    @Autowired
    public MessageUtils(MessageSource messageSource) {
        MessageUtils.messageSource = messageSource;
    }

    public static String getMessage(ApiError apiError, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        // 尝试从国际化文件读取，失败则回退到默认msg
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

    public static String getMessage(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            return key;
        }
    }

    private static String format(String msg, Object... args) {
        return args == null || args.length == 0
                ? msg
                : java.text.MessageFormat.format(msg, args);
    }
}
