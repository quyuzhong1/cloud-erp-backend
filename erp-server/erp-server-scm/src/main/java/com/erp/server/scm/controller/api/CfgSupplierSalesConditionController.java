package com.erp.server.scm.controller.api;


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
import com.erp.server.scm.service.CfgSupplierSalesConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.CfgSupplierSalesConditionDTO;

/**
 * 销量设置条件明细
 *
 * @author jack
 * @since 2025-06-13
 */
@Slf4j
@RestController
@LogSystemModule("销量设置条件明细")
@RequestMapping("/cfgSupplierSalesCondition")
public class CfgSupplierSalesConditionController extends BaseController {



}
