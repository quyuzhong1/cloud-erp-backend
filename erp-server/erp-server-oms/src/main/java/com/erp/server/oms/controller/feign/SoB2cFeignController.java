package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.SoB2cDetailService;
import com.erp.server.oms.service.SoB2cLogisticsService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
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

    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Resource
    private SoB2cService soB2cService;


   /**
    * 根据b2c订单id获取物流信息
    * @author Will
    * @date: 2023/11/20 11:53
    * @param mainIdList
    * @return ApiResult<List<SoB2cLogisticsEntity>>
    */
    @PostMapping("/listSoB2cLogisticsByMainIdList")
    public List<SoB2cLogisticsEntity> listSoB2cLogisticsByMainIdList(@RequestBody List<String> mainIdList) {
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(mainIdList);
        return soB2cLogisticsList;
    }

    /**
     * 根据订单id获取物流费用的参数
     * @param orderId
     * @return
     */
    @PostMapping("/getShippingCalculationByOrderId")
    public SoB2cDTO.ShippingCalculationDTO getShippingCalculationByOrderId(@RequestBody String orderId){
        SoB2cDTO.ShippingCalculationDTO  result=  soB2cLogisticsService.getShippingCalculationByOrderId(orderId);
        return result;
    }

    /**
     * 根据b2c详情id获取详情
     * @author Will
     * @date: 2023/11/20 11:53
     * @param detailIdList
     * @return ApiResult<List<SoB2cLogisticsEntity>>
     */
    @PostMapping("/listDetailByIds")
    public List<SoB2cDetailEntity> listDetailByIds(@RequestBody List<String> detailIdList) {
        if(CollectionUtils.isEmpty(detailIdList)){
           return Collections.emptyList();
        }
        List<SoB2cDetailEntity> list = soB2cDetailService.listByIds(detailIdList);
        return list;
    }

    /**
     * 根据主表id查询B2C订单主表信息
     * @param soIds
     * @return
     */
    @PostMapping("/listByIds")
    List<SoB2cEntity> listByIds(List<String> soIds) {
        if(CollectionUtils.isEmpty(soIds)){
            return Collections.emptyList();
        }
        List<SoB2cEntity> list = soB2cService.listByIds(soIds);
        return list;
    }
}
