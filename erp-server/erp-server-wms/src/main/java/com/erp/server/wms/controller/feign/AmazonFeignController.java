package com.erp.server.wms.controller.feign;

import cn.hutool.json.JSONObject;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.server.wms.rocketmq.consumer.PlatformFbaShipmentConsumerService;
import com.erp.server.wms.service.CfgAmzFulfillmentCenterService;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * feign控制器
 *
 */
@RestController
@RequestMapping("/feign/amz")
public class AmazonFeignController extends BaseController{

    @Resource
    private PlatformFbaShipmentConsumerService<?> platformFbaShipmentConsumerService;
    @Resource
    private FbaShipmentReceiveService fbaShipmentReceiveService;
    @Resource
    private CfgAmzFulfillmentCenterService cfgAmzFulfillmentCenterService;


    /**
     * 批量新增未知国家仓库中心代号记录
     */
    @PostMapping("/CfgAmzFulfillmentCenter/batchInsert")
    public ApiResult<?> addCfgAmzFulfillmentCenterList(@RequestBody List<CfgAmzFulfillmentCenterEntity> newCenterList){
        cfgAmzFulfillmentCenterService.saveBatch(newCenterList);
        return ApiResult.success();
    }
}