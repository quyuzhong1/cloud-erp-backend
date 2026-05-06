package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.TmsAsyncTaskDetailService;

/**
 * 异步任务记录明细
 *
 * @author jack
 * @since 2026-01-28
 */
@Slf4j
@RestController
@LogSystemModule("异步任务记录明细")
@RequestMapping("/tmsAsyncTaskDetail")
public class TmsAsyncTaskDetailController extends BaseController {

    @Resource
    private TmsAsyncTaskDetailService tmsAsyncTaskDetailService;


}
