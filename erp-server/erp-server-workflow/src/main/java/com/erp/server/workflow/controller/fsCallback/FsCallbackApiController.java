package com.erp.server.workflow.controller.fsCallback;

import com.alibaba.fastjson.JSON;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.workflow.dto.FsCallbackApiReqDTO;
import com.erp.model.workflow.dto.FsCallbackApiRespDTO;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.handler.CfgApproveSyncCallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
@RequestMapping("/fs/callback/api")
public class FsCallbackApiController extends BaseController {

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private CfgApproveSyncCallbackHandler handler;

    @Autowired
    private FsService fsService;


    @PostMapping("/approve")
    @ResponseBody
    public FsCallbackApiRespDTO approve(@RequestBody FsCallbackApiReqDTO req, HttpServletRequest request){
    	log.info("飞书回调开始：{}", JSON.toJSONString(req));
        handler.quickApproveCallbackHandler(req);
        FsCallbackApiRespDTO resp = new FsCallbackApiRespDTO();
        log.info("飞书回调结束：{}",JSON.toJSONString(resp));
        return  resp;
    }


    /**
     * 获取飞书应用id
     * @author will
     * @date 2025/9/5 10:57
     * @return ApiResult<String>
     */
    @GetMapping("/getFsAppId")
    public ApiResult<String> getFsAppId() {
        return success(fsService.getFsClientId());
    }


}
