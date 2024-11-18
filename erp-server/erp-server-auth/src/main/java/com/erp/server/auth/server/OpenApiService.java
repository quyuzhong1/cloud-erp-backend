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
                    args[i] = JSONObject.parseObject(bizContent, parameterTypes[i]);
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
