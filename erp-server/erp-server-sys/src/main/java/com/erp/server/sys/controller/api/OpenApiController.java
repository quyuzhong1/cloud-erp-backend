package com.erp.server.sys.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.OpenApiInputDTO;
import com.erp.model.sys.dto.OpenApiReqDTO;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import com.erp.server.sys.service.IOpenApiService;
import com.erp.server.sys.service.SysRefererConfigService;
import com.erp.server.sys.utils.IPUtils;
import com.erp.server.sys.utils.SignType;
import com.erp.server.sys.utils.SignUtil;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/open/api")
public class OpenApiController {

    @Value("${spring.profiles.active}")
    private String currentEnvironment;
    
    private final static Map<String, String> secretKeyMap = new HashMap<>();

    @Resource
    private IOpenApiService openApiService;
    
    @Resource
    private SysRefererConfigService sysRefererConfigService;
    
    @PostMapping("/service")
    @ResponseBody
    public ApiResult<?> service(@Valid @RequestBody OpenApiReqDTO req, HttpServletRequest request){
    	String referer = request.getHeader("Referer");
    	if(StringUtils.isBlank(referer)) {
    		referer = request.getHeader("referer");
    	}
    	if(StringUtils.isBlank(referer)) {
    		return ApiResult.error(500, "请求头referer不能为空");
    	}
    	log.info("{}平台接口统一请求报文：{}" , referer , JSON.toJSONString(req));
    	
    	OpenApiInputDTO openApiInputDTO = new OpenApiInputDTO();
        BeanUtils.copyProperties(req , openApiInputDTO);
        String secretKey = getSecretKey(referer);
        if(StringUtils.isBlank(secretKey)) {
        	return ApiResult.error(500, "请求头referer="+ referer +"未配置秘钥，请联系实施人员");
        }
		openApiInputDTO.setSecretKey(secretKey);
        openApiInputDTO.setRequestIp(IPUtils.getIpAddr(request));
        
        ApiResult<?> result = openApiService.unitPlatformService(openApiInputDTO);
        log.info("平台接口统一响应报文：{}" , JSON.toJSONString(result));
        return result;
    }

    private String getSecretKey(String referer) {
    	String secretKey = secretKeyMap.get(referer);
    	if(secretKey == null) {
    		List<SysRefererConfigEntity> list = sysRefererConfigService.lambdaQuery().eq(SysRefererConfigEntity::getReferer, referer).select(SysRefererConfigEntity::getSecretKey).list();
    		if(CollUtil.isNotEmpty(list)) {
    			secretKey = list.get(0).getSecretKey();
    			secretKeyMap.put(referer, secretKey);
    		}
    	}
		return secretKey;
    }
    
    @PostMapping("/getMD5/{serviceMethod}")
    @ResponseBody
    public OpenApiReqDTO getMD5Sign(@RequestBody(required = false) Map<String, Object> map, @PathVariable String serviceMethod){
    	return getSign(map, serviceMethod, SignType.MD5);
    }
    
    @PostMapping("/getAES/{serviceMethod}")
    @ResponseBody
    public OpenApiReqDTO getAESSign(@RequestBody(required = false) Map<String, Object> map, @PathVariable String serviceMethod){
        return getSign(map, serviceMethod, SignType.AES);
    }
    
    private OpenApiReqDTO getSign(Map<String, Object> map ,String serviceMethod , String signType) {
    	OpenApiReqDTO input = new OpenApiReqDTO();
    	input.setTimestamp(System.currentTimeMillis());
        input.setSignType(signType);
        input.setVersion("1.0.0");
        input.setMethod(serviceMethod);
        input.setCharset("UTF-8");
        if(map != null) {
        	input.setData(JSON.toJSONString(map));
        }
        if("prod".equalsIgnoreCase(currentEnvironment)) {
        	input.setSign("生产环境不允许调用");
        }else {
        	input.setSign(SignUtil.genSign(SignUtil.getSignStr(input), input.getCharset(), input.getSignType(), getSecretKey("test")));
        }
        return input;
    }

}
