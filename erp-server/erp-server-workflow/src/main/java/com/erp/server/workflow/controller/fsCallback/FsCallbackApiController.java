package com.erp.server.workflow.controller.fsCallback;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.workflow.dto.FsCallbackApiReqDTO;
import com.erp.model.workflow.dto.FsCallbackApiRespDTO;
import com.erp.server.workflow.handler.CfgApproveSyncCallbackHandler;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
@RequestMapping("/fs/callback/api")
public class FsCallbackApiController {

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private CfgApproveSyncCallbackHandler handler;

    @PostMapping("/approve")
    @ResponseBody
    public FsCallbackApiRespDTO approve(@RequestBody FsCallbackApiReqDTO req, HttpServletRequest request){
    	log.info("飞书回调开始：{}" ,"fs", JSON.toJSONString(req));
        String message = handler.quickApproveCallbackHandler(req);
        FsCallbackApiRespDTO resp = new FsCallbackApiRespDTO();
        if(StringUtil.isNotBlank(message)){
            resp.setMessage("message");
            resp.setCode(400);
        }
        log.info("飞书回调结束");
        return  resp;
    }





}
