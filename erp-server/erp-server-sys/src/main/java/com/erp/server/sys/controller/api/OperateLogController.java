package com.erp.server.sys.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.OperateLogService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.OperateLogDTO;

/**
 * 操作日志表
 *
 * @author jack
 * @since 2025-05-23
 */
@Slf4j
@RestController
@LogSystemModule("操作日志表")
@RequestMapping("/operateLog")
public class OperateLogController extends BaseController {
    }
