package com.erp.server.auth.utils;

import java.lang.reflect.Field;
import java.util.Collection;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.common.core.exception.ServiceException;

public class CheckObjectUtil {
	
	private CheckObjectUtil(){
		
	}
	
	private static final String CHECK_ERROR = "业务参数校验：";
	
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
    public static void checkAnnotationEx(Object object) {
        if (object != null) {
            Class<?> clz = object.getClass();
            Field[] fields = clz.getDeclaredFields();
            for (Field field : fields) {
            	field.setAccessible(true);
            	Object fieldObject = null;
        		try {
        			fieldObject = field.get(object);
        		} catch (IllegalArgumentException e) {
        			throw new ServiceException("获取属性参数错误");
        		} catch (IllegalAccessException e) {
        			throw new ServiceException("获取属性权限不足");
        		}
        		checkAnnotationNotBlank(field, fieldObject);
        		checkAnnotationNotEmpty(field, fieldObject);
            }
        }
    }
    
    private static void checkAnnotationNotBlank(Field field , Object fieldObject) {
    	NotBlank notBlank = field.getDeclaredAnnotation(NotBlank.class);
		if (null != notBlank){
            String message = notBlank.message();
            if (null == fieldObject || StringUtils.isBlank(fieldObject.toString())){
                throw new ServiceException(CHECK_ERROR + message);
            }
        }
        NotNull notNull = field.getDeclaredAnnotation(NotNull.class);
        if (notNull != null) {
            String message = notNull.message();
            if (null == fieldObject){
                throw new ServiceException(CHECK_ERROR + message);
            }
        }
    }
    
    private static void checkAnnotationNotEmpty(Field field , Object fieldObject) {
        NotEmpty notEmpty = field.getDeclaredAnnotation(NotEmpty.class);
        if (notEmpty != null) {
            String message = notEmpty.message();
            if (null == fieldObject ){
                throw new ServiceException(CHECK_ERROR + message);
            }
            if (fieldObject instanceof Collection){
                if (CollectionUtils.isEmpty((Collection)fieldObject)){
                    throw new ServiceException(CHECK_ERROR + message);
                }
            }else {
                throw new ServiceException("not Collection Data");
            }
        }
    }
    
}
