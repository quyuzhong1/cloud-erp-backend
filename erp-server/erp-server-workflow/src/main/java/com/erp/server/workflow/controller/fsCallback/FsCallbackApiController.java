package com.erp.server.workflow.controller.fsCallback;

import com.alibaba.fastjson.JSON;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.FsCallbackApiReqDTO;
import com.erp.server.workflow.handler.CfgApproveSyncCallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
@RequestMapping("/fs/callback/api")
public class FsCallbackApiController {

    @Resource
    private CfgApproveSyncCallbackHandler handler;

    @PostMapping("/approve")
    @ResponseBody
    public ApiResult<Object> approve(@RequestBody FsCallbackApiReqDTO req, HttpServletRequest request){
    	log.info("飞书回调开始：{}" ,"fs", JSON.toJSONString(req));
        Boolean b = handler.quickApproveCallbackHandler(req);
        log.info("飞书回调结束");
        return ApiResult.success();
    }





}
