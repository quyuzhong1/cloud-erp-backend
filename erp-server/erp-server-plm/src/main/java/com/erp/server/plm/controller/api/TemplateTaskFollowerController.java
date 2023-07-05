package com.erp.server.plm.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.plm.service.TemplateTaskFollowerService;


/**
 * 任务关注的人
 *
 * @author Luo_WG
 * @since 2023-06-20
 */
@RestController
@RequestMapping("/templateTaskFollower")
public class TemplateTaskFollowerController extends BaseController {

    @Autowired
    private TemplateTaskFollowerService templateTaskFollowerService;



}
