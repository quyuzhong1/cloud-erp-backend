package com.erp.server.wms.controller.feign;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.CfgRuleOutEnum;
import com.erp.server.wms.service.SoB2cDeliveryDetailService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * b2c发货单
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@RestController
@LogSystemModule("b2c发货单")
@RequestMapping("/feign/soB2cDelivery")
public class SoB2cDeliveryFeignController extends BaseController {

    @Resource
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @PostMapping("/listBySoDetailIds")
    public List<SoB2cDeliveryDetailEntity> listBySoDetailIds(@RequestBody List<String> soDetailIdList) {
        List<SoB2cDeliveryDetailEntity> list = soB2cDeliveryDetailService.listBySoDetailIds(soDetailIdList);
        return list;
    }

    @PostMapping("/updateB2cDeliveryWeightBySoId")
    public Boolean updateB2cDeliveryWeightBySoId(@RequestBody SoB2cDeliveryDTO.UpdateWeightDTO dto) {
        return soB2cDeliveryService.updateB2cDeliveryWeightBySoId(dto);
    }

     /** 
      * @description 添加B2C发货单
      * @param dto
      * @author Lambda
      * @return 
      * @create 2023-12-18 15:47
      */
    @PostMapping("/add")
    public Boolean listBySoDetailIds(@RequestBody SoB2cDeliveryDTO.AddDTO dto) {
        Boolean addResult = soB2cDeliveryService.add(dto);
        return addResult;
    }

    /**
     * 手动标发
     * @Author Luo_WG
     * @Date 2023/12/27 15:30
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    @PostMapping("/falseDelivery")
    public BatchResultDTO falseDelivery(@RequestBody String id) {
        BatchResultDTO resultDTO = soB2cDeliveryService.falseDelivery(id);
        return resultDTO;
    }

    /**
     * 根据来源id查询发货单
     * @Author Luo_WG
     * @Date 2023/12/27 15:40
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoB2cDeliveryEntity>
     **/
    @PostMapping("/listBySourceId")
    public List<SoB2cDeliveryEntity> listBySourceId(@RequestBody List<String> sourceIds) {
        List<SoB2cDeliveryEntity> deliveryEntityList = soB2cDeliveryService.listBySourceIds(sourceIds);
        return deliveryEntityList;
    }

    /**
     * 修改发货状态
     * @Author Luo_WG
     * @Date 2023/12/27 16:00
     * @param ids
     * @param status
     * @return java.lang.Boolean
     **/
    @PostMapping("/updateStatus")
    public Boolean updateStatus(@RequestParam("ids") List<String> ids, @RequestParam("status") String status) {
        Boolean flag = soB2cDeliveryService.updateStatus(ids, status);
        return flag;
    }

    /**
     * 手动标发
     * @Author Luo_WG
     * @Date 2023/12/27 16:00
     * @param ids 发货单id
     * @return java.lang.Boolean
     **/
    @PostMapping("/falseDeliveryBatch")
    public Boolean falseDeliveryBatch(@RequestBody List<String> ids) {
        Boolean flag = soB2cDeliveryService.falseDeliveryBatch(ids);
        return flag;
    }

    /**
     * 平台标记发货
     **/
    @PostMapping("/shipOrder")
    public Boolean shipOrder(@RequestBody @Validated PlatformShipOrderDTO platformShipOrderDTO) {
        return soB2cDeliveryService.shipOrder(platformShipOrderDTO);
    }

    /**
     * 流水线称重
     * @author will
     * @date 2024/6/28 15:48
     * @param dto
     * @return String
     */
    @PostMapping("/dimensionalWeightPipeline")
    public ApiResult<String> dimensionalWeightPipeline(@RequestBody @Validated DimensionalWeightDTO dto) {
        try {
            long startTime = System.currentTimeMillis();
            ApiResult<String> result = soB2cDeliveryService.dimensionalWeightPipeline(dto);
            long endTime = System.currentTimeMillis();
            log.warn("dimensionalWeightPipeline 流水线称重耗时:{},barcode:{}",endTime - startTime,dto.getBarCode());
            return result;
        }catch (Exception e){
            log.error("流水线称重异常",e);
            return ApiResult.error(StrUtil.format("系统异常:{}",e.getMessage()), CfgRuleOutEnum.EquipmentSortingPortEnum.NINE.getCode());
        }
    }

    /**
     * 修改发货单标发类型
     * @param ids 发货单id
     * @param code 类型
     */
    @PostMapping("/updateShipmentMark")
    void updateShipmentMark(List<String> ids, String code) {
        soB2cDeliveryService.updateShipmentMark(ids, code);
    }

   /**
    * 生成直接调拨单
    * @author will
    * @date 2024/8/1 9:47
    * @param soId
    * @return Boolean
    */
    @PostMapping("/afreshPushTransferInfo")
    public Boolean afreshPushTransferInfo(@RequestBody String soId) {
        return soB2cDeliveryService.afreshPushTransferInfo(soId);
    }
    /**
     * 重试发货虚拟仓库存扣减
     * @author will
     * @date 2024/8/1 10:37
     * @param soId
     * @return Boolean
     */
    @PostMapping("/afreshOutFreezeVirtualInventory")
    public Boolean afreshOutFreezeVirtualInventory(@RequestBody String soId) {
        return soB2cDeliveryService.afreshOutFreezeVirtualInventory(soId);
    }
}
