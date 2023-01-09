package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.server.plm.service.BomOperateLogService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * bom 操作记录日志表(BomOperateLog)表控制层
 *
 * @author yl
 * @since 2023-01-09 11:42:04
 */
@RestController
@RequestMapping("bomOperateLog")
public class BomOperateLogController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private BomOperateLogService bomOperateLogService;



}

