package com.erp.server.srm.controller.api;


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
import com.erp.server.srm.service.DeliveryOrderDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;

/**
 * 送货单明细
 *
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@RestController
@LogSystemModule("送货单明细")
@RequestMapping("/deliveryOrderDetail")
public class DeliveryOrderDetailController extends BaseController {

}
