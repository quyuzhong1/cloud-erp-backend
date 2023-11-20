package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.SoB2cLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * B2c销售订单
 * @author Will
 * @date: 2023/11/20 11:51
 */
@Slf4j
@RestController
@RequestMapping("/feign/soB2c")
public class SoB2cFeignController extends BaseController {

    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;


   /**
    * 根据b2c订单id获取物流信息
    * @author Will
    * @date: 2023/11/20 11:53
    * @param mainIdList
    * @return ApiResult<List<SoB2cLogisticsEntity>>
    */
    @PostMapping("/listSoB2cLogisticsByMainIdList")
    public ApiResult<List<SoB2cLogisticsEntity>> listSoB2cLogisticsByMainIdList(@RequestBody List<String> mainIdList) {
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(mainIdList);
        return success(soB2cLogisticsList);
    }

}
