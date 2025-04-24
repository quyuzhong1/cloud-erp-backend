package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.server.mrp.service.DeliverySuggestSysService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 建议发货变更表
 *
 * @author will
 * @since 2024-10-21
 */
@Slf4j
@RestController
@LogSystemModule("建议发货变更表")
@RequestMapping("/deliverySuggestChange")
public class DeliverySuggestSysController extends BaseController {

    @Resource
    private DeliverySuggestSysService deliverySuggestSysService;




}
