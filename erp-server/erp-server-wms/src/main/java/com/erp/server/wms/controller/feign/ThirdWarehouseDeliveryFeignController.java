package com.erp.server.wms.controller.feign;


import cn.hutool.core.util.StrUtil;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.GenerateDeliveryAndOutStockDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.oms.dto.GenerateDeliveryAndOutStockDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.enums.CfgRuleOutEnum;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoB2cDeliveryDetailService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    private OperateLogService operateLogService;

    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

    @PostMapping("/add")
    public ThirdWarehouseDeliveryEntity add(@RequestBody ThirdWarehouseDeliveryEntity entity) {
        return thirdWarehouseDeliveryService.add(entity);
    }

    @PostMapping("/getLatestBySoId")
    public ThirdWarehouseDeliveryEntity getLatestBySoId(@RequestBody String soId) {
        return thirdWarehouseDeliveryService.getLatestBySoId(soId);
    }

    @PostMapping("/update")
    public boolean update(@RequestBody ThirdWarehouseDeliveryEntity entity) {
        operateLogService.addModuleOperateLog(StrUtil.format("状态变更为{}", SoB2cWarehouseDeliveryStatusEnum.getName(entity.getStatus())), ModuleTypeEnum.THIRD_WAREHOUSE_DELIVERY.getCode(),entity.getId(), "状态变更");
       return thirdWarehouseDeliveryService.updateById(entity);
    }

    @PostMapping("/generateDeliveryAndOutStock")
    public  void generateDeliveryAndOutStock(@RequestBody GenerateDeliveryAndOutStockDTO generateDeliveryAndOutStockDTO) {
        thirdWarehouseDeliveryService.generateDeliveryAndOutStock(generateDeliveryAndOutStockDTO);
    }

}
