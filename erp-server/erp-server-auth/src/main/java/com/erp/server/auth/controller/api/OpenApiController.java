package com.erp.server.auth.controller.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.erp.rpc.file.feign.FileFeign;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.OpenApiInputDTO;
import com.erp.model.sys.dto.OpenApiReqDTO;
import com.erp.model.sys.dto.OpenApiReqNewDTO;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import com.erp.rpc.sys.feign.SysRefereConfigFeign;
import com.erp.server.auth.server.OpenApiService;
import com.erp.server.auth.utils.IPUtils;
import com.erp.server.auth.utils.SignType;
import com.erp.server.auth.utils.SignUtil;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * openApi接口
 * @Author wuhaotian
 * @Date 2025/9/22 18:18
 **/
@Controller
@Slf4j
@RequestMapping("/open/api")
public class OpenApiController {

    @Value("${spring.profiles.active}")
    private String currentEnvironment;
    
    private static final Map<String, String> secretKeyMap = new HashMap<>();

    @Resource
    private OpenApiService openApiService;
    @Resource
    private FileFeign filefeign;
    @Resource
    private SysRefereConfigFeign sysRefereConfigFeign;

    @PostMapping("/upload")
    public @ResponseBody ApiResult<String> unitPlatformServiceUpload(@Valid OpenApiReqDTO input, HttpServletRequest request, MultipartFile file){
        log.warn("平台上传接口统一请求报文：{},文件名:{}" , JSON.toJSONString(input),file.getOriginalFilename());
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        String referer = request.getHeader("appId");
        if(StringUtils.isBlank(referer)) {
        	referer = request.getHeader("Referer");
        }
        if(StringUtils.isBlank(referer)) {
            referer = request.getHeader("referer");
        }
        if(StringUtils.isBlank(referer)) {
            return ApiResult.error(500, "请求头appId或referer不能为空");
        }
        if (CollectionUtils.isEmpty(fileMap) || 1 != fileMap.size()){
            return ApiResult.error(500, "仅能上传一个文件");
        }
        String secretKey = getSecretKey(referer);
        if(StringUtils.isBlank(secretKey)) {
            return ApiResult.error(500, "请求头referer="+ referer +"未配置秘钥，请联系实施人员");
        }
        String signType = input.getSignType();
        if (!SignUtil.equalsAny(input.getVersion(), "1.0.0") || !StringUtils.equalsIgnoreCase("UTF-8", input.getCharset())){
            return ApiResult.error(500, "版本号和编码方式不能为空");
        }

        String charset = input.getCharset();

        // 校验加密方式
        if(!SignUtil.checkSign(input,charset, signType, input.getSign(), secretKey)){
            return  ApiResult.error(500, "验签失败");
        }

        String fileUrl = "";
        try {
            MultipartFile multipartFile = new ArrayList<>(fileMap.values()).get(0);
            fileUrl = filefeign.uploadFile(multipartFile);
        } catch (Exception e) {
            log.error("openApi文件上传失败", e);
            return ApiResult.error(500, "上传失败，请联系实施人员");
        }
        ApiResult<String> success = ApiResult.success(fileUrl);
        log.warn("平台上传接口统一响应报文：{}" , JSON.toJSONString(success));
        //仓库设备发送文件信息，为了响应时间，需要在此处保存文件信息
        if("hczn".equals(referer)){
            openApiService.addByWarehouseEquipment(fileUrl, file.getOriginalFilename());
        }
		return success;
    }

    /**
     * 新逻辑上传接口 v2 - 简化版本，不需要签名验证
     */
    @PostMapping("/upload/v2")
    public @ResponseBody ApiResult<String> unitPlatformServiceUploadV2(HttpServletRequest request, MultipartFile file){
        log.warn("新逻辑上传接口请求，文件名:{}" , file.getOriginalFilename());
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        String referer = request.getHeader("App-Id");
        if(StringUtils.isBlank(referer)) {
            return ApiResult.error(500, "请求头App-Id不能为空");
        }
        if (CollectionUtils.isEmpty(fileMap) || 1 != fileMap.size()){
            return ApiResult.error(500, "仅能上传一个文件");
        }

        // 检查应用配置，确保使用新逻辑
        String logicType = getLogicType(referer);
        if (!"NEW".equalsIgnoreCase(logicType)) {
            log.warn("应用 {} 不是新逻辑类型，当前类型: {}", referer, logicType);
            return ApiResult.error(500, "该应用不支持新逻辑接口");
        }
        String fileUrl = "";
        try {
            MultipartFile multipartFile = new ArrayList<>(fileMap.values()).get(0);
            fileUrl = filefeign.uploadFile(multipartFile);
        } catch (Exception e) {
            log.error("新逻辑文件上传失败", e);
            return ApiResult.error(500, "上传失败，请联系实施人员");
        }
        
        ApiResult<String> success = ApiResult.success(fileUrl);
        log.warn("新逻辑上传接口响应：{}" , JSON.toJSONString(success));
        
        // 仓库设备发送文件信息，为了响应时间，需要在此处保存文件信息
        if("hczn".equals(referer)){
            openApiService.addByWarehouseEquipment(fileUrl, file.getOriginalFilename());
        }
        return success;
    }

    @PostMapping("/service")
    @ResponseBody
    public ApiResult<Object> service(@Validated @RequestBody OpenApiReqDTO req, HttpServletRequest request){
    	String referer = request.getHeader("appId");
        if(StringUtils.isBlank(referer)) {
        	referer = request.getHeader("Referer");
        }
    	if(StringUtils.isBlank(referer)) {
    		referer = request.getHeader("referer");
    	}
    	if(StringUtils.isBlank(referer)) {
    		return ApiResult.error(500, "请求头appId或referer不能为空");
    	}
    	log.warn("{}平台接口统一请求报文：{}" , referer , JSON.toJSONString(req));
    	
    	OpenApiInputDTO openApiInputDTO = new OpenApiInputDTO();
        BeanUtils.copyProperties(req , openApiInputDTO);
        String secretKey = getSecretKey(referer);
        if(StringUtils.isBlank(secretKey)) {
        	return ApiResult.error(500, "请求头referer="+ referer +"未配置秘钥，请联系实施人员");
        }
		openApiInputDTO.setSecretKey(secretKey);
        openApiInputDTO.setRequestIp(IPUtils.getIpAddr(request));
        
        ApiResult<Object> result = openApiService.unitPlatformService(openApiInputDTO);
        log.warn("平台接口统一响应报文：{}" , JSON.toJSONString(result));
        return result;
    }

    /**
     * 新逻辑服务接口 v2 - 简化版本，不需要签名验证（网关已处理）
     */
    @PostMapping("/service/v2")
    @ResponseBody
    public ApiResult<Object> serviceV2(@RequestBody OpenApiReqNewDTO req, HttpServletRequest request){
        String referer = request.getHeader("App-Id");
        if(StringUtils.isBlank(referer)) {
            return ApiResult.error(500, "请求头App-Id不能为空");
        }
        log.warn("{}新逻辑服务接口请求报文：{}" , referer , JSON.toJSONString(req));
        
        // 检查应用配置，确保使用新逻辑
        String logicType = getLogicType(referer);
        if (!"NEW".equalsIgnoreCase(logicType)) {
            log.warn("应用 {} 不是新逻辑类型，当前类型: {}", referer, logicType);
            return ApiResult.error(500, "该应用不支持新逻辑接口");
        }

        OpenApiInputDTO openApiInputDTO = new OpenApiInputDTO();
        BeanUtils.copyProperties(req , openApiInputDTO);
        openApiInputDTO.setRequestIp(IPUtils.getIpAddr(request));

        // 调用新的服务方法（不需要签名验证）
        ApiResult<Object> result = openApiService.unitPlatformServiceNew(openApiInputDTO);
        log.warn("新逻辑服务接口响应报文：{}" , JSON.toJSONString(result));
        return result;
    }

    private String getSecretKey(String referer) {
    	String secretKey = secretKeyMap.get(referer);
    	if(secretKey == null) {
    		List<SysRefererConfigEntity> list = FeignQuery.create(SysRefererConfigEntity.class).eq(SysRefererConfigEntity::getAppId, referer).list();
    		if(CollUtil.isNotEmpty(list)) {
    			secretKey = list.get(0).getAppSecret();
    			secretKeyMap.put(referer, secretKey);
    		}
    	}
		return secretKey;
    }

    /**
     * 获取应用逻辑类型
     */
    private String getLogicType(String appId) {
        try {
            ApiResult<List<SysRefererConfigEntity>> result = sysRefereConfigFeign.getByAppId(appId);
            if (result == null || !result.isSuccess() || result.getData() == null || result.getData().isEmpty()) {
                log.warn("未找到应用配置，appId: {}", appId);
                return "OLD"; // 默认使用旧逻辑
            }
            List<SysRefererConfigEntity> configList = result.getData();
            for (SysRefererConfigEntity config : configList) {
                if (StringUtils.isNotBlank(config.getLogicType())) {
                    return config.getLogicType();
                }
            }
            return "OLD"; // 默认使用旧逻辑
        } catch (Exception e) {
            log.error("获取应用逻辑类型失败，appId: {}", appId, e);
            return "OLD"; // 出错时默认使用旧逻辑
        }
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
