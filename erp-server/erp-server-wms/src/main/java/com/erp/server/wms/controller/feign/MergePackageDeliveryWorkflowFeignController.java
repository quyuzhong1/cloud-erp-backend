package com.erp.server.wms.controller.feign;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.BaseController;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.AsyncService;
import com.erp.server.wms.service.PackageForecastService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.erp.server.wms.service.WaveListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组包预报自动出库任务编排节点（WMS 执行端）。
 */
@Slf4j
@RestController
@RequestMapping("/feign/mergePackageDeliveryWorkflow")
public class MergePackageDeliveryWorkflowFeignController extends BaseController {
    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private PackageForecastService packageForecastService;
    @Resource
    private WaveListService waveListService;
    @Resource
    private AsyncService asyncService;
    @Resource
    private RedisUtil redisUtil;

    /**
     * Node0: 校验并标记发货 + 波次状态收敛。
     */
    @PostMapping("/prepareAndMarkShipped")
    public WorkflowTaskRecordDTO.MqResponseDTO prepareAndMarkShipped(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto) {
        Map<String, Object> data = copyInputData(dto);
        String soId = resolveSoId(data);
        if (CharSequenceUtil.isBlank(soId)) {
            return failed("soId不能为空");
        }
        SoB2cDeliveryEntity deliveryEntity = findActiveDelivery(soId);
        if (deliveryEntity == null) {
            return waiting("未找到有效发货单，等待重试");
        }
        try {
            if (!SoB2cDeliveryStatusEnum.SHIPPED.getCode().equalsIgnoreCase(deliveryEntity.getStatus())) {
                packageForecastService.handleMergePackageDeliveryOther(soId, deliveryEntity);
                waveListService.waveListStatusAutoChange(deliveryEntity.getId());
            }
            data.put("soId", soId);
            data.put("deliveryId", deliveryEntity.getId());
            data.put("soCode", deliveryEntity.getSoCode());
            data.put("shopId", deliveryEntity.getShopId());
            data.put("dictPlatform", deliveryEntity.getDictPlatform());
            return success(data);
        } catch (Exception e) {
            log.error("组包编排Node0执行失败，soId={}", soId, e);
            return failed(resolveErrorMsg(e, "标记发货失败"));
        }
    }

    /**
     * Node1: 触发平台标发异步处理（不阻塞主链路）。
     */
    @PostMapping("/triggerSignShipAsync")
    public WorkflowTaskRecordDTO.MqResponseDTO triggerSignShipAsync(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto) {
        Map<String, Object> data = copyInputData(dto);
        String soId = resolveSoId(data);
        if (CharSequenceUtil.isBlank(soId)) {
            return failed("soId不能为空");
        }
        SoB2cEntity soB2cEntity = soB2cFeign.getById(soId);
        if (soB2cEntity == null) {
            return waiting("未找到销售订单，等待重试");
        }
        try {
            boolean shouldShip = soB2cFeign.checkPlatformShipOrder(soId);
            if (shouldShip) {
                String businessDesc = "组包任务编排";
                asyncService.asyncShipOrder(
                        soB2cEntity.getId(),
                        soB2cEntity.getCode(),
                        soB2cEntity.getDictPlatform(),
                        soB2cEntity.convertSubmitPlatformUniqueKey(),
                        JSONUtil.toJsonStr(data),
                        businessDesc,
                        false,
                        false
                );
            }
            data.put("signShipTriggered", shouldShip);
            return success(data);
        } catch (Exception e) {
            log.error("组包编排Node1执行失败，soId={}", soId, e);
            return failed(resolveErrorMsg(e, "触发平台标发失败"));
        }
    }

    /**
     * Node2: 生成直接调拨单。
     */
    @PostMapping("/pushTransferInfo")
    public WorkflowTaskRecordDTO.MqResponseDTO pushTransferInfo(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto) {
        Map<String, Object> data = copyInputData(dto);
        String soId = resolveSoId(data);
        if (CharSequenceUtil.isBlank(soId)) {
            return failed("soId不能为空");
        }
        SoB2cDeliveryEntity deliveryEntity = findActiveDelivery(soId);
        if (deliveryEntity == null) {
            return waiting("未找到有效发货单，等待重试");
        }
        try {
            Boolean pushed = soB2cDeliveryService.pushTransferInfoError(deliveryEntity);
            if (!Boolean.TRUE.equals(pushed)) {
                return waiting("生成直接调拨单未完成，等待重试");
            }
            data.put("transferPushed", Boolean.TRUE);
            return success(data);
        } catch (Exception e) {
            log.error("组包编排Node2执行失败，soId={}", soId, e);
            return failed(resolveErrorMsg(e, "生成直接调拨单失败"));
        }
    }

    /**
     * Node3: 生成销售出库并收尾。
     */
    @PostMapping("/generateOutstockAndFinalize")
    public WorkflowTaskRecordDTO.MqResponseDTO generateOutstockAndFinalize(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto) {
        Map<String, Object> data = copyInputData(dto);
        String soId = resolveSoId(data);
        if (CharSequenceUtil.isBlank(soId)) {
            return failed("soId不能为空");
        }
        SoB2cDeliveryEntity deliveryEntity = findActiveDelivery(soId);
        if (deliveryEntity == null) {
            return failed("未找到有效发货单，等待重试");
        }
        try {
            Boolean generated = soB2cDeliveryService.generateB2cSoOutstock(deliveryEntity);
            if (!Boolean.TRUE.equals(generated)) {
                return failed("生成销售出库单失败");
            }
            redisUtil.del(CharSequenceUtil.format(RedisCacheConstants.MERGE_PACKAGE_RETRY_COUNT_KEY, soId));
            data.put("outstockGenerated", Boolean.TRUE);
            return success(data);
        } catch (Exception e) {
            log.error("组包编排Node3执行失败，soId={}", soId, e);
            return failed(resolveErrorMsg(e, "生成销售出库单失败"));
        }
    }

    private WorkflowTaskRecordDTO.MqResponseDTO success(Map<String, Object> data) {
        WorkflowTaskRecordDTO.MqResponseDTO responseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        responseDTO.setData(data);
        return responseDTO;
    }

    private WorkflowTaskRecordDTO.MqResponseDTO failed(String errorMsg) {
        WorkflowTaskRecordDTO.MqResponseDTO responseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        responseDTO.setErrorMsg(errorMsg);
        responseDTO.setStatus(WorkflowTaskRecordStatusEnum.FAILED.getCode());
        return responseDTO;
    }

    private WorkflowTaskRecordDTO.MqResponseDTO waiting(String errorMsg) {
        WorkflowTaskRecordDTO.MqResponseDTO responseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        responseDTO.setErrorMsg(errorMsg);
        responseDTO.setStatus(WorkflowTaskRecordStatusEnum.WAITING.getCode());
        return responseDTO;
    }

    private Map<String, Object> copyInputData(WorkflowTaskRecordDTO.MqRequestDTO dto) {
        if (dto == null || dto.getData() == null) {
            return new HashMap<>();
        }
        return new HashMap<>(dto.getData());
    }

    private String resolveSoId(Map<String, Object> data) {
        if (data == null) {
            return "";
        }
        Object soIdObj = data.containsKey("soId") ? data.get("soId") : data.get("id");
        return soIdObj == null ? "" : String.valueOf(soIdObj);
    }

    private SoB2cDeliveryEntity findActiveDelivery(String soId) {
        List<SoB2cDeliveryEntity> deliveryEntities = soB2cDeliveryService.listBySourceIds(java.util.Collections.singletonList(soId));
        return deliveryEntities.stream()
                .filter(v -> !SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(v.getStatus()))
                .findFirst()
                .orElse(null);
    }

    private String resolveErrorMsg(Exception e, String fallback) {
        if (e instanceof ServiceException && CharSequenceUtil.isNotBlank(((ServiceException) e).getMsg())) {
            return ((ServiceException) e).getMsg();
        }
        return CharSequenceUtil.isNotBlank(e.getMessage()) ? e.getMessage() : fallback;
    }
}
