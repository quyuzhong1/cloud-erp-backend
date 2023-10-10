package com.erp.server.dmp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpPullTaskService;


/**
 * 中台同步任务表
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@RestController
@RequestMapping("/dmpPullTask")
public class DmpPullTaskController extends BaseController {

    @Autowired
    private DmpPullTaskService dmpPullTaskService;



}
