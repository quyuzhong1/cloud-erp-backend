package com.erp.server.wms.controller.feign;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.lingxing.FbaReceiveGroupEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.server.wms.convert.FbaShipmentReceiveConverter;
import com.erp.server.wms.rocketmq.consumer.PlatformFbaShipmentConsumerService;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import com.erp.server.wms.service.FbaShipmentService;
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
    public ApiResult consumerPullShipment(@RequestBody PlatformFbaShipmentDTO platformFbaShipmentDTO){
        return platformFbaShipmentConsumerService.handle(new JSONObject(platformFbaShipmentDTO));
    }


    /**
     * 保存签收记录并检查调拨
     * @author Jim
     */
    @PostMapping("/saveAndCheckTransfer")
    public Boolean saveAndCheckTransfer(@RequestBody FbaReceiveGroupEntity groupEntity){
        FbaShipmentEntity entity = fbaShipmentReceiveService.getAndPullResend(groupEntity);
        if (null == entity){
            return true;
        }
        List<FbaShipmentReceiveEntity> receiveEntityList = FbaShipmentReceiveConverter.INSTANCE.sourceListToEntityList(groupEntity.getDetailList());
        Map<String, List<FbaShipmentReceiveEntity>> groupMap = receiveEntityList.stream().collect(Collectors.groupingBy(e -> CharSequenceUtil.format("{}_{}", e.getFbaShipmentId(), e.getReceiveDate())));
//        groupMap.forEach((key, value) -> fbaShipmentReceiveService.saveAndCheckTransfer(value, entity));
        for (Map.Entry<String, List<FbaShipmentReceiveEntity>> entry : groupMap.entrySet()) {
            fbaShipmentReceiveService.saveAndCheckTransfer(entry.getValue(), entity);
        }
        return true;
    }

}