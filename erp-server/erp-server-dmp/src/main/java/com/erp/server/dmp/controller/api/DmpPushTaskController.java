package com.erp.server.dmp.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpPushTaskDTO;

/**
 * 中台同步任务表
 *
 * @author Cloud
 * @since 2023-09-06
 */
@Slf4j
@RestController
@RequestMapping("/dmpPushTask")
public class DmpPushTaskController extends BaseController {

    @Autowired
    private DmpPushTaskService dmpPushTaskService;


}
