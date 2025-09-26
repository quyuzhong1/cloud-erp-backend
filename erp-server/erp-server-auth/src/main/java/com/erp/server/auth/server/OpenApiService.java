package com.erp.server.auth.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.rpc.wms.feign.AttachmentFeign;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.OpenApiInputDTO;
import com.erp.server.auth.utils.CheckObjectUtil;
import com.erp.server.auth.utils.InitOpenApiBeanUtil;
import com.erp.server.auth.utils.SignUtil;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OpenApiService {

    @Resource
    private InitOpenApiBeanUtil initGateWayBeanUtil;

    @Resource
    private AttachmentFeign attachmentFeign;
    
    public ApiResult<Object> unitPlatformService(OpenApiInputDTO input) {
    	ApiResult<Object> response = null;
    	String method = input.getMethod();
        String signType = input.getSignType();
        if (!SignUtil.equalsAny(input.getVersion(), "1.0.0") || !StringUtils.equalsIgnoreCase("UTF-8", input.getCharset())){
        	response = ApiResult.error(500, "版本号和编码方式不能为空");
        	return response;
        }
        
        String content = input.getData();
        String secretKey = input.getSecretKey();
        
        String charset = input.getCharset();
        
        // 校验加密方式
        if(!SignUtil.checkSign(input,charset, signType, input.getSign(), secretKey)){
        	response = ApiResult.error(500, "验签失败");
        	return response;
        }
        
        try {
            if (StringUtils.equalsIgnoreCase(input.getVersion(), "1.0.0")){
            	response = gatewayMethod(method, content);
            }
        }catch(ServiceException e){
        	response = ApiResult.error(500, e.getMsg());
        }catch(InvocationTargetException e) {
        	Throwable targetException = e.getTargetException();
        	if(targetException instanceof ServiceException) {
        		ServiceException serviceException = (ServiceException) targetException;
        		response = ApiResult.error(500, serviceException.getMsg());
        	}else {
        		log.error("统一对外接口处理异常{}" , e);
                response = ApiResult.error(500, "服务器内部错误，请联系实施人员");
        	}
        }catch(Exception e){
        	log.error("统一对外接口处理异常{}" , e);
            response = ApiResult.error(500, "服务器内部错误，请联系实施人员");
        }
        return response;
    }

    /**
     * 新逻辑服务方法 - 不需要签名验证（网关已处理）
     */
    public ApiResult<Object> unitPlatformServiceNew(OpenApiInputDTO input) {
        ApiResult<Object> response = null;
        String method = input.getMethod();
        
        // 新逻辑不需要版本号和编码验证，因为网关已经处理了
        String content = input.getData();
        
        try {
            // 直接调用业务方法，不进行签名验证
            response = gatewayMethod(method, content);
        }catch(ServiceException e){
            response = ApiResult.error(500, e.getMsg());
        }catch(InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if(targetException instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) targetException;
                response = ApiResult.error(500, serviceException.getMsg());
            }else {
                log.error("新逻辑统一对外接口处理异常{}" , e);
                response = ApiResult.error(500, "服务器内部错误，请联系实施人员");
            }
        }catch(Exception e){
            log.error("新逻辑统一对外接口处理异常{}" , e);
            response = ApiResult.error(500, "服务器内部错误，请联系实施人员");
        }
        return response;
    }

    private ApiResult<Object> gatewayMethod(String serviceName, String bizContent) throws InvocationTargetException, IllegalAccessException, InstantiationException {
    	ApiResult<Object> response = ApiResult.success();
    	InitOpenApiBeanUtil.GatewayBaseInfo gatewayBaseInfo = initGateWayBeanUtil.getGatewayMap().get(serviceName);
        if (null == gatewayBaseInfo){
        	response.setCode(500);
        	response.setMsg(serviceName + "服务方法未实现");
        	return response;
        }
        Method method = gatewayBaseInfo.getMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];

        Annotation[][] annotations = method.getParameterAnnotations();
        for(int i =0 ; i<parameterTypes.length; i++){
            args[i] = null;
            // 目前只解析第一个参数
            if(i == 0){
                if(StringUtils.isNotBlank(bizContent)){
                    try {
                        args[i] = parseContentToTargetType(bizContent, parameterTypes[i]);
                    } catch (Exception e) {
                        log.error("内容解析失败，输入内容：{}，目标类型：{}，错误信息：{}", bizContent, parameterTypes[i].getName(), e.getMessage());
                        throw new ServiceException("内容解析失败：" + e.getMessage());
                    }
                }else{
                    args[i] = parameterTypes[i].newInstance();
                }
                checkAnnotations(annotations, args[i], i);
            }
        }
        Object invoke = method.invoke(gatewayBaseInfo.getGatewayClass(), args);
        
        if (invoke instanceof ApiResult){
        	BeanUtil.copyProperties(invoke, response);
        }else {
        	response.setData(invoke);
        }
        return response;
    }

    /**
     * 智能解析内容到目标类型
     * 支持的类型：String、Boolean、Integer、Long、Double、Float、BigDecimal以及所有POJO对象（通过JSON解析）
     */
    private Object parseContentToTargetType(String bizContent, Class<?> targetType) {
        if (StringUtils.isBlank(bizContent)) {
            throw new ServiceException("业务内容不能为空");
        }
        
        String trimmedContent = bizContent.trim();
        
        // 处理String类型
        if (targetType == String.class) {
            return bizContent;
        }
        
        // 处理Boolean类型
        if (targetType == Boolean.class || targetType == boolean.class) {
            try {
                String lowerContent = trimmedContent.toLowerCase();
                if ("true".equals(lowerContent) || "1".equals(lowerContent) || "yes".equals(lowerContent)) {
                    return Boolean.TRUE;
                } else if ("false".equals(lowerContent) || "0".equals(lowerContent) || "no".equals(lowerContent)) {
                    return Boolean.FALSE;
                } else {
                    throw new ServiceException("无法将 '" + bizContent + "' 转换为布尔类型");
                }
            } catch (Exception e) {
                throw new ServiceException("布尔类型转换失败：" + e.getMessage());
            }
        }
        
        // 处理Integer类型
        if (targetType == Integer.class || targetType == int.class) {
            try {
                return Integer.parseInt(trimmedContent);
            } catch (NumberFormatException e) {
                throw new ServiceException("无法将 '" + bizContent + "' 转换为整数类型");
            }
        }
        
        // 处理Long类型
        if (targetType == Long.class || targetType == long.class) {
            try {
                return Long.parseLong(trimmedContent);
            } catch (NumberFormatException e) {
                throw new ServiceException("无法将 '" + bizContent + "' 转换为长整型");
            }
        }

        // 对于其他复杂类型，尝试JSON解析
        if (trimmedContent.startsWith("{") || trimmedContent.startsWith("[")) {
            // 标准的JSON格式，使用FastJSON解析
            try {
                log.debug("使用JSON解析转换：{} -> {}", bizContent, targetType.getSimpleName());
                return JSONObject.parseObject(bizContent, targetType);
            } catch (com.alibaba.fastjson.JSONException e) {
                throw new ServiceException("JSON格式错误：" + e.getMessage());
            } catch (Exception e) {
                throw new ServiceException("JSON解析失败：" + e.getMessage());
            }
        } else {
            // 尝试将简单的字符串转换为包装类型或查找构造函数
            try {
                // 检查是否有String构造函数
                log.debug("尝试String构造函数转换：{} -> {}", bizContent, targetType.getSimpleName());
                return targetType.getConstructor(String.class).newInstance(bizContent);
            } catch (NoSuchMethodException e) {
                // 如果没有String构造函数，检查是否为复杂类型
                if (targetType != String.class && !targetType.isPrimitive() && !isWrapperType(targetType)) {
                    throw new ServiceException("复杂对象类型需要使用JSON格式：期望JSON格式，实际输入：'" + bizContent + "'，目标类型：" + targetType.getSimpleName());
                }
                throw new ServiceException("无法将字符串 '" + bizContent + "' 转换为 " + targetType.getSimpleName() + " 类型，该类型不支持String构造函数");
            } catch (Exception e) {
                throw new ServiceException("字符串构造转换失败：'" + bizContent + "' 转换为 " + targetType.getSimpleName() + " 类型，错误：" + e.getMessage());
            }
        }
    }
    
    /**
     * 检查是否为包装类型
     */
    private boolean isWrapperType(Class<?> clazz) {
        return clazz == Boolean.class || clazz == Character.class || clazz == Byte.class ||
               clazz == Short.class || clazz == Integer.class || clazz == Long.class ||
               clazz == Float.class || clazz == Double.class;
    }

    private void checkAnnotations(Annotation[][] annotations, Object arg, int i) {
        if (annotations.length > i){
            Annotation[] annotationArr = annotations[i];
            if (null != annotationArr){
                for (Annotation annotation : annotationArr) {
                    if (annotation instanceof Valid || annotation instanceof Validated){
                        CheckObjectUtil.checkAnnotation(arg);
                    }
                }
            }
        }
    }

    @Async
    public void addByWarehouseEquipment(String fileUrl, String name) {
        attachmentFeign.addByWarehouseEquipment(new WmsAttachmentDTO.AddDTO(name,fileUrl));
    }
}
