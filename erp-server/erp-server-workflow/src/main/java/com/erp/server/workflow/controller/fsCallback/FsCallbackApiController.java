package com.erp.server.workflow.controller.fsCallback;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.workflow.dto.FsCallbackApiReqDTO;
import com.erp.model.workflow.dto.FsCallbackApiRespDTO;
import com.erp.server.workflow.handler.CfgApproveSyncCallbackHandler;
import com.erp.server.workflow.handler.FsCallbackEventHandler;
import com.lark.oapi.sdk.servlet.ext.ServletAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/fs/callback/api")
public class FsCallbackApiController {

    @Value("${restcloud.url:172.16.100.96}")
    private String restcloudUrl;

    @Value("${restcloud.port:8080}")
    private String restcloudPort;

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
    @PostMapping("/fileRecordChange")
    @ResponseBody
    public ResponseEntity<FsCallbackApiRespDTO> fileRecordChange(@RequestParam Map<String, String> req, HttpServletRequest request) {
        log.warn("飞书表格文件更新回调开始：{}", JSON.toJSONString(req));
        FsCallbackApiRespDTO resp = new FsCallbackApiRespDTO();
        if (CollectionUtils.isEmpty(req)) {
            return ResponseEntity.badRequest().body(resp);
        }
        String tableName = request.getHeader("Table-Name");
        JSONObject requestParams = new JSONObject();
        requestParams.put("tableName", tableName);
        requestParams.put("data", Arrays.asList(req));
        HttpResponse response = HttpRequest.post("http://" + restcloudUrl + ":" + restcloudPort + "/restcloud/ods_feishu/ods_flow_feishu_file_callback")
                .header("Content-Type", "application/json")
                .body(requestParams.toJSONString())
                .timeout(60000)
                .execute();
        if (200 != response.getStatus()) {
            log.error("输出restCloud错误:{}", response.body());
            resp.setMsg("输出回调restCloud失败：" + response.body());
            resp.setCode(-1); // 标记失败
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        } else {
            String body = response.body();
            JSONObject responseJson = JSON.parseObject(body);
            Integer resultCode = responseJson.getInteger("resultCode");
            // 判断结果异常:ETLProcessRunResultCode
            if (null != resultCode && 1 == resultCode) {
                log.info("输出回调restCloud成功:{}", body);
            } else {
                resp.setMsg("输出回调restCloud失败：" + response.body());
                resp.setCode(-1); // 标记失败
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }
        }
        log.warn("飞书表格文件更新回调结束处理结束");
        return ResponseEntity.ok(resp);
    }
}
