package com.erp.server.wms.controller.feign;


import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.enums.CfgRuleOutEnum;
import com.erp.server.wms.service.SoB2cDeliveryDetailService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 三方仓发货
 *
 */
@Slf4j
@RestController
@LogSystemModule("三方仓发货")
@RequestMapping("/feign/thirdWarehouseDelivery")
public class ThirdWarehouseDeliveryFeignController extends BaseController {

    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

    @PostMapping("/add")
    public void add(@RequestBody ThirdWarehouseDeliveryEntity entity) {
        thirdWarehouseDeliveryService.add(entity);
    }

}
