package com.erp.server.workflow.controller.fsCallback;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.utils.HttpCommonUtil;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.workflow.dto.FsCallbackApiReqDTO;
import com.erp.model.workflow.dto.FsCallbackApiRespDTO;
import com.erp.server.workflow.handler.CfgApproveSyncCallbackHandler;
import com.erp.server.workflow.handler.FsCallbackEventHandler;
import com.lark.oapi.sdk.servlet.ext.ServletAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/fs/callback/api")
public class FsCallbackApiController {

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private CfgApproveSyncCallbackHandler handler;

    @Resource
    private ServletAdapter servletAdapter;

    @Resource
    private FsCallbackEventHandler fsCallbackEventHandler;




    @PostMapping("/approve")
    @ResponseBody
    public FsCallbackApiRespDTO approve(@RequestBody FsCallbackApiReqDTO req, HttpServletRequest request){
        request.getHeaders("");
    	log.info("飞书回调开始：{}", JSON.toJSONString(req));
        handler.quickApproveCallbackHandler(req,request);
        FsCallbackApiRespDTO resp = new FsCallbackApiRespDTO();
        log.info("飞书回调结束：{}",JSON.toJSONString(resp));
        return  resp;
    }




    /**
     * 创建路由处理器 Create route handler
     * @author will
     * @date 2025/12/11 11:32
     * @param request
     * @param response
     * @return void
     */
    @PostMapping("/webhook/event")
    public void event(HttpServletRequest request, HttpServletResponse response)
            throws Throwable {
        // 包装请求，使其可以重复读取body
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        // 第一次读取（用于日志）
        byte[] requestBodyBytes = wrappedRequest.getContentAsByteArray();
        String requestBody = new String(requestBodyBytes, wrappedRequest.getCharacterEncoding());

        log.warn("飞书事件触发器:url:{},method:{}，body:{}", request.getRequestURL(), request.getMethod(), requestBody);
        // 回调扩展包提供的事件回调处理器
        servletAdapter.handleEvent(request, response, fsCallbackEventHandler.getEventHandler());
    }

    /**
     * 飞书表格文件记录新增或更新回调接口
     */
    @PostMapping("/fileRecordAddOrUpdate")
    @ResponseBody
    public FsCallbackApiRespDTO fileRecordAddOrUpdate(@RequestBody Map<String, String> req, HttpServletRequest request){
        log.info("飞书表格文件更新回调开始：{}", JSON.toJSONString(req));
        FsCallbackApiRespDTO resp = new FsCallbackApiRespDTO();
        log.info("飞书表格文件更新回调结束：{}",JSON.toJSONString(resp));
        return  resp;
    }
}
