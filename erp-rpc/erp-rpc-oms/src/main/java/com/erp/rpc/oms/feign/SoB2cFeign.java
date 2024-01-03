package com.erp.rpc.oms.feign;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.PrintWayBillPdfDTO;
import com.common.business.dto.WalmartShipDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cOptionTypeEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
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
     *
     * @param soDetailIdList
     * @return
     */
    @PostMapping("/feign/soB2c/listDetailByIds")
    List<SoB2cDetailEntity> listDetailByIds(@RequestBody List<String> soDetailIdList);

    /**
     * 根据主表id查询B2C订单主表信息
     *
     * @param soIds
     * @return
     */
    @PostMapping("/feign/soB2c/listByIds")
    List<SoB2cEntity> listByIds(@RequestBody List<String> soIds);

    /**
     * 根据主表id查询B2C订单主表信息
     *
     * @param soId
     * @return
     */
    @GetMapping("/feign/soB2c/getById")
    SoB2cEntity getById(@RequestParam("soId") String soId);

    /**
     * 获取销售出库的需要的参数 根据销售code
     */
    @PostMapping("/feign/soB2c/getSoOutstockInfoByCode")
    SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoByCode(@RequestBody String code);

    /**
     * 取销售出库的需要的参数 根据销售单id
     */
    @PostMapping("/feign/soB2c/getSoOutstockInfoById")
    SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoById(@RequestBody String soId);

    /**
     * 获取销售出库的需要的参数
     */
    @PostMapping("/feign/soB2c/orderShipped")
    Boolean orderShipped(@RequestBody String soId);




    /**
     * 根据b2c订单id查询详情信息
     *
     * @param mainIds
     * @return java.util.List<com.erp.model.oms.entity.SoB2cDetailEntity>
     * @Author Luo_WG
     * @Date 2023/12/19 15:22
     **/
    @PostMapping("/feign/soB2c/listDetailByMainIds")
    List<SoB2cDetailEntity> listDetailByMainIds(@RequestBody List<String> mainIds);

    /**
     * @param dto
     * @return
     * @description 添加异常订单信息
     * @author Lambda
     * @create 2023-12-20 11:06
     */
    @PostMapping("/feign/soB2cError/add")
    void addSoB2cError(@RequestBody SoB2cErrorDTO.AddDTO dto);

    /**
     * 删除异常信息
     *
     * @param deleteDTO
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-20 11:20
     */
    @PostMapping("/feign/soB2cError/delete")
    void deleteError(@RequestBody SoB2cErrorDTO.DeleteDTO deleteDTO);

    /**
     * 根据b2c订单id获取买家信息
     *
     * @param mainIdList
     * @return java.util.List<com.erp.model.oms.entity.SoB2cLogisticsEntity>
     * @Author Luo_WG
     * @Date 2023/12/22 9:20
     **/
    @PostMapping("/feign/soB2c/listSoB2cReceiverByMainIdList")
    List<SoB2cReceiverEntity> listSoB2cReceiverByMainIdList(@RequestBody List<String> mainIdList);

    /**
     * 获取标记发货 需要的参数
     *
     * @param soB2cId 销售订单id
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-22 15:33
     */
    @PostMapping("/feign/soB2c/getSignShipParam")
    SoB2cDTO.SignShipOrderDTO getSignShipParam(@RequestBody String soB2cId);

    /**
     * 校验是否需要调用第三方标记发货
     * @Author Luo_WG
     * @Date 2023/12/27 11:29
     * @param soB2cId
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/checkPlatformShipOrder")
    Boolean checkPlatformShipOrder(@RequestBody String soB2cId);

    /**
     * 修改b2c销售单状态
     * @Author Luo_WG
     * @Date 2023/12/27 20:14
     * @param soB2cIds
     * @param status
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/updateSoB2cStatus")
    Boolean updateSoB2cStatus(@RequestParam("soB2cIds") List<String> soB2cIds, @RequestParam("status") String status);

    /**
     * 获取销售订单物流渠道
     * @Author yl
     * @Date 2023/12/28 11:29
     * @param channelId
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/listSoB2cLogisticsByChannelId")
    List<SoB2cLogisticsEntity> listSoB2cLogisticsByChannelId(@RequestBody String channelId);

    /**
     * 设置打印面单需要的字段
     * @Author Luo_WG
     * @Date 2023/12/28 15:37
     * @param soIds
     * @return java.util.List<com.common.business.dto.PrintWayBillPdfDTO>
     **/
    @PostMapping("/feign/soB2c/printWayBillPdf")
    List<PrintWayBillPdfDTO> printWayBillPdf(@RequestBody List<String> soIds);


    /**
     * 修改b2c销售单状态
     * @Author yl
     * @Date 2023/12/27 20:14
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/updateSoB2cStatusByParams")
    Boolean updateSoB2cStatusByParams(@RequestBody SoB2cDTO.UpdateStatusDTO dto);


    /**
     * 获取销售订单客户信息
     * @param soId
     * @return
     */
    @PostMapping("/feign/soB2c/getB2cCustomerById")
    SoB2cDTO.CustomerDTO getB2cCustomerById(@RequestBody String soId);

    /**
     * 根据销售订单idlist 获取到客户信息
     * @param soIdList
     * @return
     */
    @PostMapping("/feign/soB2c/listCustomer")
    List<SoB2cDTO.CustomerDTO> listCustomer(@RequestBody List<String> soIdList);

    /**
     * 获取异常信息
     * @description
     * @param mainId
     * @param errorType
     * @author Lambda
     * @return 
     * @create 2024-01-01 12:27
     */
    @PostMapping("/feign/soB2cError/getB2cError")
    SoB2cErrorEntity getB2cError(@RequestParam("mainId")String mainId, @RequestParam("errorType") String errorType);

    /**
     * 获取沃尔玛发货参数
     * @Author Luo_WG
     * @Date 2024/1/3 17:23
     * @param soId
     * @return com.common.business.dto.WalmartShipDTO
     **/
    @PostMapping("/feign/soB2c/getWalmartShipOrderParam")
    List<WalmartShipDTO> getWalmartShipOrderParam(@RequestBody String soId);

    /**
     * 查询仓库为空的销售订单
     * @description
     * @param ids
     * @author Lambda
     * @return
     * @create 2024-01-03 17:12
     */
    @PostMapping("/feign/soB2c/listWarehouseIsEmpty")
    List<SoB2cEntity> listWarehouseIsEmpty(@RequestBody List<String> ids);

    @PostMapping("/feign/soB2c/updateWarehouseByShopId")
    Boolean updateWarehouseByShopId(@RequestParam("id")String id,@RequestParam("shopId") String shopId);

    @GetMapping("/feign/soB2c/findMergeByTargetId")
    List<SoB2cRefEntity> findMergeByTargetId(@RequestParam("targetId")String targetId);
}
