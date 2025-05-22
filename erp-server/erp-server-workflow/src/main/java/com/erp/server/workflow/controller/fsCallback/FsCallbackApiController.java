package com.erp.server.workflow.controller.fsCallback;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.OpenApiInputDTO;
import com.erp.model.sys.dto.OpenApiReqDTO;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import com.erp.model.workflow.dto.FsCallbackApiReqDTO;
import com.erp.server.workflow.handler.CfgApproveSyncCallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/fs/callback/api")
public class FsCallbackApiController {

    @Resource
    private CfgApproveSyncCallbackHandler handler;

    @PostMapping("/approve")
    @ResponseBody
    public ApiResult<Object> approve(@RequestBody FsCallbackApiReqDTO req, HttpServletRequest request){
//    	log.warn("{}平台接口统一请求报文：{}" ,"fs", JSON.toJSONString(req));
        handler.quickApproveCallbackHandler(req);
//        log.warn("平台接口统一响应报文：{}" ,"fs");
        return ApiResult.success();
    }





}
