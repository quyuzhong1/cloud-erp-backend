package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cOptionTypeEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.oms.service.*;
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

    @Resource
    private SoB2cReceiverService soB2cReceiverService;

    @Resource
    private SoB2cRefService soB2cRefService;


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
     * 根据code 获取到需要销售出单的数据
     * @description
     * @param
     * @author Lambda
     * @return
     * @create 2023-12-27 19:54
     */
    @PostMapping("/getSoOutstockInfoByCode")
    public SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoByCode(@RequestBody String soCode) {
        SoOutstockDTO.GenerateB2cDTO result = soB2cService.getSoOutstockInfoByCode(soCode);
        return result;
    }

    /**
     * 根据id 获取到需要销售出单的数据
     * @description
     * @param
     * @author Lambda
     * @return
     * @create 2023-12-27 19:54
     */
    @PostMapping("/getSoOutstockInfoById")
    public SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoById(@RequestBody String soId) {
        SoOutstockDTO.GenerateB2cDTO result = soB2cService.getSoOutstockInfoById(soId);
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
     * @param waybillDTOList 物流面单
     * @return java.util.List<com.erp.model.oms.entity.SoB2cDetailEntity>
     **/
    @PostMapping("/updateLogisticsWaybill")
    public Boolean updateLogisticsWaybill(@RequestBody List<SoB2cDTO.WaybillDTO> waybillDTOList) {
        Boolean flag = soB2cService.updateLogisticsWaybill(waybillDTOList);
        return flag;
    }

    /**
     * 根据b2c订单id获取买家信息
     * @Author Luo_WG
     * @Date 2023/12/22 9:20
     * @param mainIdList
     * @return java.util.List<com.erp.model.oms.entity.SoB2cLogisticsEntity>
     **/
    @PostMapping("/listSoB2cReceiverByMainIdList")
    public List<SoB2cReceiverEntity> listSoB2cReceiverByMainIdList(@RequestBody List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        List<SoB2cReceiverEntity> list = soB2cReceiverService.listByMainIds(mainIdList);
        return list;
    }

    /**
     * 获取标记发货参数
     * @return
     */
    @PostMapping("/getSignShipParam")
    public SoB2cDTO.SignShipOrderDTO getSignShipParam(@RequestBody String soB2cId){
         return  soB2cService.getSignShipParam(soB2cId);
    }

    /**
     * 校验是否需要调用第三方标记发货
     * @Author Luo_WG
     * @Date 2023/12/27 11:29
     * @param soB2cId
     * @return java.lang.Boolean
     **/
    @PostMapping("/checkPlatformShipOrder")
    public Boolean checkPlatformShipOrder(@RequestBody String soB2cId) {
        return soB2cService.checkPlatformShipOrder(soB2cId);
    }

    /**
     * 修改b2c销售单状态
     * @Author Luo_WG
     * @Date 2023/12/27 20:14
     * @param soB2cIds
     * @param status
     * @return java.lang.Boolean
     **/
    @PostMapping("/updateSoB2cStatus")
    Boolean updateSoB2cStatus(@RequestParam("soB2cIds") List<String> soB2cIds, @RequestParam("status") String status) {
        return soB2cService.updateSoB2cStatus(soB2cIds, status);
    }


    /**
     * 获取销售订单物流渠道 根据渠道id
     * @Author yl
     * @Date 2023/12/27 11:29
     * @param channelId
     * @return java.lang.Boolean
     **/
    @PostMapping("/listSoB2cLogisticsByChannelId")
    public List<SoB2cLogisticsEntity> listSoB2cLogisticsByChannelId(@RequestBody String channelId) {
        return soB2cLogisticsService.listByChannelId(channelId);
    }
}
