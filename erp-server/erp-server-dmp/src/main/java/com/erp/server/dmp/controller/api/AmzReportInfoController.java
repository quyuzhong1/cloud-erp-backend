package com.erp.server.dmp.controller.api;


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
import com.erp.server.dmp.service.AmzReportInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.AmzReportInfoDTO;

/**
 * 亚马逊报告请求记录
 *
 * @author Jim
 * @since 2024-01-18
 */
@Slf4j
@RestController
@LogSystemModule("亚马逊报告请求记录")
@RequestMapping("/amzReportInfo")
public class AmzReportInfoController extends BaseController {

    @Resource
    private AmzReportInfoService amzReportInfoService;


}
