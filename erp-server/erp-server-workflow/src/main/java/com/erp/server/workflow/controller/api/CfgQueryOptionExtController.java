package com.erp.server.workflow.controller.api;


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
import com.erp.server.workflow.service.CfgQueryOptionExtService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgQueryOptionExtDTO;

/**
 * cfg_query_option拓展表
 *
 * @author jack
 * @since 2025-06-04
 */
@Slf4j
@RestController
@LogSystemModule("cfg_query_option拓展表")
@RequestMapping("/cfgQueryOptionExt")
public class CfgQueryOptionExtController extends BaseController {

}
