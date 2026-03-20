package com.erp.server.oms.controller.api;

import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.EnumCacheUtils;
import com.erp.server.oms.service.OmsAttachmentService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 下拉-枚举下拉
 * @Classname
 * @Description 公共接口
 * @Date 2023-04-14 12:06
 * @Created by zhangchunlin
 */
@RestController
@RequestMapping("common")
@Slf4j
public class CommonController extends BaseController {

    @Resource
    private OmsAttachmentService omsAttachmentService;

    @Resource
    @Lazy
    private CommonController commonController;
    /**
     * 批量获取枚举下拉框，供前端调用，不用每个枚举类都提供一个单独的接口（每个服务都有专属自己的）
     * @param types
     * @return
     */
    @GetMapping("enumDropDownBatch")
    public ApiResult<Map<String,List<Map<String,Object>>>> enumSelect(@RequestParam(value = "types")List<String> types) {
        Map<String,List<Map<String,Object>>> typeMaps = Maps.newHashMap();
        Map<String,List<Map<String,Object>>> enumMaps = EnumCacheUtils.getInstance().getData();
        if(CollUtil.isNotEmpty(types)) {
            types.stream().forEach(r-> typeMaps.put(r,enumMaps.get(r)));
        }
        return success(typeMaps);
    }

    /**
     * 获取枚举下拉框，供前端调用，不用每个枚举类都提供一个单独的接口（每个服务都有专属自己的）
     * @param type
     * @return
     */
    @GetMapping("enumDropDown")
    public ApiResult<List<Map<String,Object>>> enumSelect(@RequestParam(value = "type")String type) {
        Map<String,List<Map<String,Object>>> enumMaps = EnumCacheUtils.getInstance().getData();
        return success(enumMaps.get(type));

    }
    /**
     * 新增附件
     * @return
     */
    @PostMapping("addAttachment")
    public ApiResult<BatchResultDTO> addAttachment(@RequestBody @Validated BaseDTO.AddAttachmentDTO addAttachmentDTO){
        BatchResultDTO batchResultDTO = omsAttachmentService.addAttachment(addAttachmentDTO);
        if(batchResultDTO.getSuccess()){
            return success(batchResultDTO);
        }
        return failure(batchResultDTO);
    }

    /**
     */
    @GetMapping("testSkyWalking")
    public ApiResult<String> testSkyWalking() {
        // 获取 SkyWalking TraceId
        String traceId = TraceContext.traceId();
        String threadName = Thread.currentThread().getName();
        log.error("threadName {},traceId:{}",threadName,traceId);
        commonController.printTraceId();
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        return success("skywalking tid:"+traceId+"MDC  :" + mdcMap);

    }

    @Async
    public void printTraceId(){
        String threadName = Thread.currentThread().getName();
        String traceId = TraceContext.traceId();
        log.error("多线程 {}获取traceId:{}",threadName,traceId);
    }

}
