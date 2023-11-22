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
import com.erp.server.sys.service.ImlDictCityService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.ImlDictCityDTO;

/**
 * 艾姆勒城市字典表
 *
 * @author lrp
 * @since 2023-11-22
 */
@Slf4j
@RestController
@LogSystemModule("艾姆勒城市字典表")
@RequestMapping("/imlDictCity")
public class ImlDictCityController extends BaseController {

    @Resource
    private ImlDictCityService imlDictCityService;


}
