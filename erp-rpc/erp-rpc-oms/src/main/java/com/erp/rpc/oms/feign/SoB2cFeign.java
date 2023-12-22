package com.erp.rpc.oms.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.wms.dto.SoOutstockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soB2c")
public interface SoB2cFeign {

    /**
     * 根据b2c订单id获取物流信息
     */
    @PostMapping("/feign/soB2c/listSoB2cLogisticsByMainIdList")
    List<SoB2cLogisticsEntity> listSoB2cLogisticsByMainIdList(@RequestBody List<String> mainIdList);

    /**
     * 根据订单id 获取到运费估算的参数值
     *
     * @param orderId
     * @return
     */
    @PostMapping("/feign/soB2c/getShippingCalculationByOrderId")
    SoB2cDTO.ShippingCalculationDTO getShippingCalculationByOrderId(@RequestBody String orderId);

    /**
     * 获取明细信息
     * @param soDetailIdList
     * @return
     */
    @PostMapping("/feign/soB2c/listDetailByIds")
    List<SoB2cDetailEntity> listDetailByIds(@RequestBody List<String> soDetailIdList);

    /**
     * 根据主表id查询B2C订单主表信息
     * @param soIds
     * @return
     */
    @PostMapping("/feign/soB2c/listByIds")
    List<SoB2cEntity> listByIds(@RequestBody List<String> soIds);

    /**
     * 根据主表id查询B2C订单主表信息
     * @param soId
     * @return
     */
    @GetMapping("/feign/soB2c/getById")
    SoB2cEntity getById(@RequestParam("soId") String soId);

    /**
     * 更改销售订单已发货
     */
    @PostMapping("/feign/soB2c/orderShipped")
    SoOutstockDTO.GenerateB2cDTO orderShipped(@RequestBody String soId);

    /**
     * 根据b2c订单id查询详情信息
     * @Author Luo_WG
     * @Date 2023/12/19 15:22
     * @param mainIds
     * @return java.util.List<com.erp.model.oms.entity.SoB2cDetailEntity>
     **/
    @PostMapping("/feign/soB2c/listDetailByMainIds")
    List<SoB2cDetailEntity> listDetailByMainIds(@RequestBody List<String> mainIds);

    /**
     * 修改销售订单的物流面单字段
     * @Author Luo_WG
     * @Date 2023/12/19 15:22
     * @param waybillDTOList 物流面单
     * @return java.util.List<com.erp.model.oms.entity.SoB2cDetailEntity>
     **/
    @PostMapping("/feign/soB2c/updateLogisticsWaybill")
    Boolean updateLogisticsWaybill(@RequestBody List<SoB2cDTO.WaybillDTO> waybillDTOList);

    /**
     * 修改销售订单的配货单字段
     * @Author Luo_WG
     * @Date 2023/12/19 17:18
     * @param waybillDTOList 配货单
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/updateDistributeWaybill")
    Boolean updateDistributeWaybill(@RequestBody List<SoB2cDTO.WaybillDTO> waybillDTOList);

    /**
     * @description 添加异常订单信息
     * @param dto
     * @author Lambda
     * @return
     * @create 2023-12-20 11:06
     */
    @PostMapping("/feign/soB2cError/add")
    void addSoB2cError(SoB2cErrorDTO.AddDTO dto);

    /**
     * 删除异常信息
     * @description
     * @param deleteDTO
     * @author Lambda
     * @return
     * @create 2023-12-20 11:20
     */
    @PostMapping("/feign/soB2cError/delete")
    void deleteError(SoB2cErrorDTO.DeleteDTO deleteDTO);

    /**
     * 根据b2c订单id获取买家信息
     * @Author Luo_WG
     * @Date 2023/12/22 9:20
     * @param mainIdList
     * @return java.util.List<com.erp.model.oms.entity.SoB2cLogisticsEntity>
     **/
    @PostMapping("/feign/soB2c/listSoB2cReceiverByMainIdList")
    List<SoB2cReceiverEntity> listSoB2cReceiverByMainIdList(@RequestBody List<String> mainIdList);

    /** 获取标记发货 需要的参数
     * @description
     * @param soB2cId 销售订单id
     * @author Lambda
     * @return 
     * @create 2023-12-22 15:33
     */
    @PostMapping("/feign/soB2c/getSignShipParam")
    SoB2cDTO.SignShipOrderDTO getSignShipParam(@RequestBody String  soB2cId);
}
