package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.SoB2cDetailService;
import com.erp.server.oms.service.SoB2cLogisticsService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * B2c销售订单
 *
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
     *
     * @param mainIdList
     * @return ApiResult<List < SoB2cLogisticsEntity>>
     * @author Will
     * @date: 2023/11/20 11:53
     */
    @PostMapping("/listSoB2cLogisticsByMainIdList")
    public List<SoB2cLogisticsEntity> listSoB2cLogisticsByMainIdList(@RequestBody List<String> mainIdList) {
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(mainIdList);
        return soB2cLogisticsList;
    }

    /**
     * 根据订单id获取物流费用的参数
     *
     * @param orderId
     * @return
     */
    @PostMapping("/getShippingCalculationByOrderId")
    public SoB2cDTO.ShippingCalculationDTO getShippingCalculationByOrderId(@RequestBody String orderId) {
        SoB2cDTO.ShippingCalculationDTO result = soB2cLogisticsService.getShippingCalculationByOrderId(orderId);
        return result;
    }

    /**
     * 根据b2c详情id获取详情
     *
     * @param detailIdList
     * @return ApiResult<List < SoB2cLogisticsEntity>>
     * @author Will
     * @date: 2023/11/20 11:53
     */
    @PostMapping("/listDetailByIds")
    public List<SoB2cDetailEntity> listDetailByIds(@RequestBody List<String> detailIdList) {
        if (CollectionUtils.isEmpty(detailIdList)) {
            return Collections.emptyList();
        }
        List<SoB2cDetailEntity> list = soB2cDetailService.listByIds(detailIdList);
        return list;
    }

    /**
     * 根据主表id查询B2C订单主表信息
     *
     * @param soIds
     * @return
     */
    @PostMapping("/listByIds")
    public List<SoB2cEntity> listByIds(@RequestBody List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return Collections.emptyList();
        }
        List<SoB2cEntity> list = soB2cService.listByIds(soIds);
        return list;
    }

    /**
     * 根据主表id查询B2C订单主表信息
     *
     * @param soId
     * @return
     */
    @GetMapping("/getById")
    public SoB2cEntity getById(@RequestParam("soId") String soId) {
        if (StringUtils.isBlank(soId)) {
            return null;
        }
        return soB2cService.getById(soId);
    }

    /**
     * @description
     * @param soId 销售订单id
     * @return
     * @author Lambda
     * @create 2023-12-18 11:09
     */
    @PostMapping("/orderShipped")
    public SoOutstockDTO.GenerateB2cDTO orderShipped(@RequestBody String soId) {
        SoOutstockDTO.GenerateB2cDTO result = soB2cService.orderShipped(soId);
        return result;
    }

    /**
     * 根据b2c订单id查询详情信息
     * @Author Luo_WG
     * @Date 2023/12/19 15:22
     * @param mainIds
     * @return java.util.List<com.erp.model.oms.entity.SoB2cDetailEntity>
     **/
    @PostMapping("/listDetailByMainIds")
    public List<SoB2cDetailEntity> listDetailByMainIds(@RequestBody List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        List<SoB2cDetailEntity> list = soB2cDetailService.listByMainIds(mainIds);
        return list;
    }

    /**
     * 修改销售订单的物流面单字段
     * @Author Luo_WG
     * @Date 2023/12/19 15:22
     * @param id b2c销售单id
     * @param logisticsWaybill 物流面单
     * @return java.util.List<com.erp.model.oms.entity.SoB2cDetailEntity>
     **/
    @PostMapping("/updateLogisticsWaybill")
    public Boolean updateLogisticsWaybill(@RequestParam(value = "id") String id, @RequestParam(value = "logisticsWaybill") String logisticsWaybill) {
        Boolean flag = soB2cService.updateLogisticsWaybill(id, logisticsWaybill);
        return flag;
    }

    /**
     * 修改销售订单的配货单字段
     * @Author Luo_WG
     * @Date 2023/12/19 17:18
     * @param id b2c销售单id
     * @param distributeWaybill 配货单
     * @return java.lang.Boolean
     **/
    @PostMapping("/updateDistributeWaybill")
    public Boolean updateDistributeWaybill(@RequestParam(value = "id") String id, @RequestParam(value = "distributeWaybill") String distributeWaybill) {
        Boolean flag = soB2cService.updateDistributeWaybill(id, distributeWaybill);
        return flag;
    }
}
