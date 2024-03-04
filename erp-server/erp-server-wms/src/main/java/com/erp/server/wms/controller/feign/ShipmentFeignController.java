package com.erp.server.wms.controller.feign;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.server.wms.rocketmq.consumer.PlatformFbaShipmentConsumerService;
import com.erp.server.wms.service.FbaInventoryService;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 货件feign控制器
 *
 */
@RestController
@RequestMapping("/feign/shipment")
public class ShipmentFeignController extends BaseController {

    @Resource
    private PlatformFbaShipmentConsumerService<?> platformFbaShipmentConsumerService;
    @Resource
    private FbaShipmentReceiveService fbaShipmentReceiveService;


    /**
     * 直接消费货件处理
     * @author Jim
     */
    @PostMapping("/consumer")
    public ApiResult<?> consumerPullShipment(@RequestBody PlatformFbaShipmentDTO platformFbaShipmentDTO){
        return platformFbaShipmentConsumerService.handle(new JSONObject(platformFbaShipmentDTO));
    }


    /**
     * 保存签收记录并检查调拨
     * @author Jim
     */
    @PostMapping("/saveAndCheckTransfer")
    public Boolean saveAndCheckTransfer(@RequestBody List<FbaShipmentReceiveEntity> entityList){
        Map<String, List<FbaShipmentReceiveEntity>> groupMap = entityList.stream().collect(Collectors.groupingBy(e -> StrUtil.format("{}_{}", e.getFbaShipmentId(), e.getReceiveDate())));
        groupMap.forEach((key, value) -> fbaShipmentReceiveService.saveAndCheckTransfer(value));
        return true;
    }

}