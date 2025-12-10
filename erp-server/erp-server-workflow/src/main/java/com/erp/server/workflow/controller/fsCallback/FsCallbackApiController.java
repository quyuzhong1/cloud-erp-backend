package com.erp.server.workflow.controller.fsCallback;

import com.alibaba.fastjson.JSON;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.workflow.dto.FsCallbackApiReqDTO;
import com.erp.model.workflow.dto.FsCallbackApiRespDTO;
import com.erp.server.workflow.handler.CfgApproveSyncCallbackHandler;
import com.lark.oapi.core.request.EventReq;
import com.lark.oapi.event.CustomEventHandler;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.sdk.servlet.ext.ServletAdapter;
import com.lark.oapi.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

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
     * 注册事件 Register event
     */
    private static final EventDispatcher EVENT_HANDLER = EventDispatcher.newBuilder("verificationToken", "encryptKey")
            .onCustomizedEvent("approval_instance", new CustomEventHandler() {
                @Override
                public void handle(EventReq event) throws Exception {
                    log.warn("[ onCustomizedEvent access ], type: message, data: %s\n", new String(event.getBody(), StandardCharsets.UTF_8));
                }
            })
            .build();

    /**
     * 创建路由处理器 Create route handler
     */
    @PostMapping("/webhook/event")
    public void event(HttpServletRequest request, HttpServletResponse response)
            throws Throwable {
        // 回调扩展包提供的事件回调处理器 Callback handler provided by the extension package
        servletAdapter.handleEvent(request, response, EVENT_HANDLER);
    }

    public static void main(String[] args) {
        // 构建 client Build client
        Client client = new Client.Builder("cli_a85cce7e7078901c", "3qrQgoTcDYmYmnoH2mNDUjdMAbjj4hwF")
                .eventHandler(EVENT_HANDLER)
                .build();
        // 建立长连接 Establish persistent connection
        client.start();
    }
}
