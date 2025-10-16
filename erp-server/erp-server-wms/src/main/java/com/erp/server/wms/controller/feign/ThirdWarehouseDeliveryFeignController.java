package com.erp.server.wms.controller.feign;


import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DistributeLocker;
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
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.enums.CfgRuleOutEnum;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
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
    private OperateLogService operateLogService;

    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;
    @Resource
    private ThirdWarehouseDeliveryDetailService thirdWarehouseDeliveryDetailService;

    @PostMapping("/add")
    public ThirdWarehouseDeliveryEntity add(@RequestBody ThirdWarehouseDeliveryEntity entity) {
        return thirdWarehouseDeliveryService.add(entity,true);
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
    @DistributeLocker(keyName = "generateDeliveryAndOutStockDTO.entity.id")
    public  void generateDeliveryAndOutStock(@RequestBody GenerateDeliveryAndOutStockDTO generateDeliveryAndOutStockDTO) {
        thirdWarehouseDeliveryService.generateDeliveryAndOutStock(generateDeliveryAndOutStockDTO);
    }
    /**
     * 根据三方仓发货单编号和订单编号获取三方仓发货单
     * @param code
     * @param soId
     * @return
     */
    @GetMapping("/getByCodeAndSoId")
    public ThirdWarehouseDeliveryEntity getByCodeAndSoId(@RequestParam("code") String code, @RequestParam("soId") String soId){
        return thirdWarehouseDeliveryService.getByCodeAndSoId(code, soId);
    }

    /**
     * 根据主表id获取详情列表
     * @param mainIds
     * @return
     */
    @PostMapping("/listByMainIds")
    public List<ThirdWarehouseDeliveryDetailEntity> listByMainIds(@RequestBody List<String> mainIds){
        return thirdWarehouseDeliveryDetailService.listByMainIds(mainIds);
    }

    /**
     * 根据主表id获取详情列表
     * @return
     */
    @PostMapping("/listWaitShipByWarehouseIds")
    public List<ThirdWarehouseDeliveryEntity> listWaitShipByWarehouseIds(@RequestBody List<String> warehouseIds){
        return thirdWarehouseDeliveryDetailService.listWaitShipByWarehouseIds(warehouseIds);
    }

}
