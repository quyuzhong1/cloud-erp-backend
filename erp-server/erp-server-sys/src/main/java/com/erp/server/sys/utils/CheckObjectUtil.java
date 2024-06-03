package com.erp.server.sys.utils;

import java.lang.reflect.Field;
import java.util.Collection;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.common.core.exception.ServiceException;

public class CheckObjectUtil {
    /**
     * 校验注解
     * @param object
     * @return
     * @throws Exception
     */
    public static void checkAnnotation(Object object)  {

        try {
            checkAnnotationEx(object);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("校验参数异常" + e.getMessage());
        }
    }
    public static void checkAnnotationEx(Object object) throws Exception {
        if (object != null) {
            Class<?> clz = object.getClass();
            Field[] fields = clz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                NotBlank notBlank = field.getDeclaredAnnotation(NotBlank.class);
                if (null != notBlank){
                    String message = notBlank.message();
                    if (null == field.get(object) || StringUtils.isBlank(field.get(object).toString())){
                        throw new ServiceException(message);
                    }
                }
                NotNull notNull = field.getDeclaredAnnotation(NotNull.class);
                if (notNull != null) {
                    String message = notNull.message();
                    if (null == field.get(object)){
                        throw new ServiceException(message);
                    }
                }
                NotEmpty notEmpty = field.getDeclaredAnnotation(NotEmpty.class);
                if (notEmpty != null) {
                    String message = notEmpty.message();
                    if (null == field.get(object) ){
                        throw new ServiceException(message);
                    }
                    if (field.get(object) instanceof Collection){
                        if (CollectionUtils.isEmpty((Collection)field.get(object))){
                            throw new ServiceException(message);
                        }
                    }else {
                        throw new ServiceException("not Collection Data");
                    }
                }
            }
        }
    }
}
