package com.erp.server.wms.controller.feign;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.lingxing.FbaReceiveGroupEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.server.wms.convert.FbaShipmentReceiveConverter;
import com.erp.server.wms.rocketmq.consumer.PlatformFbaShipmentConsumerService;
import com.erp.server.wms.rocketmq.consumer.PlatformSoOutStockConsumerService;
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
 * feign控制器
 *
 */
@RestController
@RequestMapping("/feign/amz")
public class AmazonFeignController extends BaseController {

    @Resource
    private PlatformFbaShipmentConsumerService<?> platformFbaShipmentConsumerService;
    @Resource
    private FbaShipmentReceiveService fbaShipmentReceiveService;
    @Resource
    private PlatformSoOutStockConsumerService<?> platformSoOutStockConsumerService;

    /**
     * 直接消费销售出库单
     * @author Jim
     */
    @PostMapping("/soOutStock/consumer")
    public ApiResult<?> consumerPullShipment(@RequestBody PlatformSoOutStockDTO platformSoOutStockDTO){
        return platformSoOutStockConsumerService.handle(new JSONObject(platformSoOutStockDTO));
    }

}