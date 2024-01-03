package com.erp.server.oms.controller.feign;


import com.common.business.dto.PrintWayBillPdfDTO;
import com.common.business.dto.WalmartShipDTO;
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
     * @param soId 销售订单id
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-18 11:09
     */
    @PostMapping("/orderShipped")
    public Boolean orderShipped(@RequestBody String soId) {
        Boolean result = soB2cService.orderShipped(soId);
        return result;
    }

    /**
     * 根据code 获取到需要销售出单的数据
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-27 19:54
     */
    @PostMapping("/getSoOutstockInfoByCode")
    public SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoByCode(@RequestBody String soCode) {
        SoOutstockDTO.GenerateB2cDTO result = soB2cService.getSoOutstockInfoByCode(soCode);
        return result;
    }

    /**
     * 根据id 获取到需要销售出单的数据
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-27 19:54
     */
    @PostMapping("/getSoOutstockInfoById")
    public SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoById(@RequestBody String soId) {
        SoOutstockDTO.GenerateB2cDTO result = soB2cService.getSoOutstockInfoById(soId);
        return result;
    }


    /**
     * 根据b2c订单id查询详情信息
     *
     * @param mainIds
     * @return java.util.List<com.erp.model.oms.entity.SoB2cDetailEntity>
     * @Author Luo_WG
     * @Date 2023/12/19 15:22
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
     * 根据b2c订单id获取买家信息
     *
     * @param mainIdList
     * @return java.util.List<com.erp.model.oms.entity.SoB2cLogisticsEntity>
     * @Author Luo_WG
     * @Date 2023/12/22 9:20
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
     *
     * @return
     */
    @PostMapping("/getSignShipParam")
    public SoB2cDTO.SignShipOrderDTO getSignShipParam(@RequestBody String soB2cId) {
        return soB2cService.getSignShipParam(soB2cId);
    }

    /**
     * 校验是否需要调用第三方标记发货
     *
     * @param soB2cId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/12/27 11:29
     **/
    @PostMapping("/checkPlatformShipOrder")
    public Boolean checkPlatformShipOrder(@RequestBody String soB2cId) {
        return soB2cService.checkPlatformShipOrder(soB2cId);
    }

    /**
     * 修改b2c销售单状态
     *
     * @param soB2cIds
     * @param status
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/12/27 20:14
     **/
    @PostMapping("/updateSoB2cStatus")
    Boolean updateSoB2cStatus(@RequestParam("soB2cIds") List<String> soB2cIds, @RequestParam("status") String status) {
        return soB2cService.updateSoB2cStatus(soB2cIds, status);
    }


    /**
     * 获取销售订单物流渠道 根据渠道id
     *
     * @param channelId
     * @return java.lang.Boolean
     * @Author yl
     * @Date 2023/12/27 11:29
     **/
    @PostMapping("/listSoB2cLogisticsByChannelId")
    public List<SoB2cLogisticsEntity> listSoB2cLogisticsByChannelId(@RequestBody String channelId) {
        return soB2cLogisticsService.listByChannelId(channelId);
    }

    /**
     * 设置打印面单需要的字段
     *
     * @param soIds
     * @return java.util.List<com.common.business.dto.PrintWayBillPdfDTO>
     * @Author Luo_WG
     * @Date 2023/12/28 15:37
     **/
    @PostMapping("/printWayBillPdf")
    public List<PrintWayBillPdfDTO> printWayBillPdf(@RequestBody List<String> soIds) {
        return soB2cService.printWayBillPdf(soIds);
    }

    /**
     * 更改订单状态
     *
     * @param dto
     * @Author yl
     * @Date 2023/12/28 15:37
     **/
    @PostMapping("/updateSoB2cStatusByParams")
    public Boolean updateSoB2cStatusByParams(@RequestBody SoB2cDTO.UpdateStatusDTO dto) {
        return soB2cService.updateSoB2cStatusByParams(dto);
    }

    /**
     * 获取客户信息
     *
     * @param soId
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-29 16:42
     */
    @PostMapping("/getB2cCustomerById")
    public SoB2cDTO.CustomerDTO getB2cCustomerById(@RequestBody String soId) {
        return soB2cService.getB2cCustomerById(soId);
    }

    /**
     * @param soIdList
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-01 9:38
     */
    @PostMapping("/listCustomer")
    public List<SoB2cDTO.CustomerDTO> listCustomer(@RequestBody List<String> soIdList) {
        return soB2cService.listCustomer(soIdList);
    }

    /**
     * 获取沃尔玛发货参数
     * @Author Luo_WG
     * @Date 2024/1/3 17:23
     * @param soId
     * @return com.common.business.dto.WalmartShipDTO
     **/
    @PostMapping("/getWalmartShipOrderParam")
    public List<WalmartShipDTO> getWalmartShipOrderParam(@RequestBody String soId) {
        return soB2cService.getWalmartShipOrderParam(soId);
    }

    /**
     * 获取b2c 销售订单信息 仓库为空
     *
     * @param soIdList
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-03 16:47
     */
    @PostMapping("/listWarehouseIsEmpty")
    public List<SoB2cEntity> listWarehouseIsEmpty(@RequestBody List<String> soIdList) {
        return soB2cService.listWarehouseIsEmpty(soIdList);
    }

    /**
     * 根据店铺仓库更新仓库
     *
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-03 17:27
     */
    @PostMapping("updateWarehouseByShopId")
    public Boolean updateWarehouseByShopId(@RequestParam("id") String id, @RequestParam("shopId") String shopId) {
        return soB2cService.updateWarehouseByShopId(id, shopId);
    }

}
