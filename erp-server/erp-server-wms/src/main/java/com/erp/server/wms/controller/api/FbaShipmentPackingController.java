package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.FbaShipmentPackingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;

/**
 * fba货件装箱信息
 *
 * @author lrp
 * @since 2024-09-03
 */
@Slf4j
@RestController
@LogSystemModule("fba货件装箱信息")
@RequestMapping("/fbaShipmentPacking")
public class FbaShipmentPackingController extends BaseController {

    @Resource
    private FbaShipmentPackingService fbaShipmentPackingService;


}
