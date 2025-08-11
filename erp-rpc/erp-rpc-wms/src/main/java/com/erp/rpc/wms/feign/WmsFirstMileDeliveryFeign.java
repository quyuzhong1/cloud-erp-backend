package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.AdvanceQueryContainer;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "firstMileDeliveryFeign",configuration = {FeignErrorDecoder.class})
public interface WmsFirstMileDeliveryFeign {


    /**
     * 查询已装箱并且未生成物流单的发货单
     */
    @PostMapping("/feign/firstMileDelivery/getGenerateLogisticDTO")
    List<FirstMileDeliveryDTO.GenerateLogisticDTO> getGenerateLogisticDTO(@RequestBody FirstMileDeliveryDTO.GenerateLogisticReqDTO dto);

    /**
     * 更新状态
     */
    @PostMapping("/feign/firstMileDelivery/updateStatus")
    Boolean updateStatus(@RequestBody FirstMileDeliveryDTO.UpdateStatusDTO dto);


    /**
     * 查询已装箱并且未生成物流单的发货单
     */
    @PostMapping("/feign/firstMileDelivery/logisticStatistics")
    List<FirstMileDeliveryDTO.LogisticStatisticsDTO> logisticStatistics(@RequestBody FirstMileDeliveryDTO.StatisticsReq dto);


    /**
     * 高级查询发货单
     */
    @PostMapping("/feign/firstMileDelivery/advanceQuery")
    List<FirstMileDeliveryEntity> advanceQuery(@RequestBody AdvanceQueryContainer advanceQueryContainer);

    /**
     * 高级查询发货单
     */
    @PostMapping("/feign/firstMileDelivery/listByIds")
    List<FirstMileDeliveryEntity> listByIds(@RequestBody List<String> ids);
    /**
     * 根据主表id获取明细记录
     */
    @PostMapping("/feign/firstMileDelivery/listDetailByMainIds")
    List<FirstMileDeliveryDetailEntity> listDetailByMainIds(@RequestBody List<String> mainIds);
    /**
     * 根据编码查询发货单明细
     */
    @PostMapping("/feign/firstMileDelivery/listDetailByCodes")
    List<FirstMileDeliveryDTO.ListFirstMileDTO> listDetailByCodes(@RequestBody List<String> codes);
    /**
     * 根据来源编码查询发货单明细
     */
    @PostMapping("/feign/firstMileDelivery/listDetailBySourceCodes")
    List<FirstMileDeliveryDTO.ListFirstMileDTO> listDetailBySourceCodes(@RequestBody List<String> sourceCodes);
    /**
     * 查询可以生成报关单的发货单
     */
    @PostMapping("/feign/firstMileDelivery/getCanGenerateDeclare")
    List<TmsDeclareBillDTO.DeliveryDTO> getCanGenerateDeclare(@RequestBody TmsDeclareBillDTO.QuerySourceDTO querySourceDTO);

    /**
     * 亚马逊仓库中心配置
     */
    @GetMapping("/feign/firstMileDelivery/getCfgAmzCenter")
    List<CfgAmzFulfillmentCenterEntity> getCfgAmzCenter();

    /**
     * 根据业务单号统计签收数量
     * @param dto 业务单号  亚马逊签收报告/第三方仓签收
     * @return
     */
    @PostMapping("/feign/firstMileDelivery/countReceiveQtyByParams")
    List<FirstMileDeliveryDTO.ReceiveDTO> countReceiveQtyByParams(@RequestBody FirstMileDeliveryDTO.RequestReceiveDTO dto);

    /**
     * 发货单查询业务单号
     * @param ids
     * @return
     */
    @PostMapping("/feign/firstMileDelivery/getBusinessCodeByIds")
    List<FirstMileDeliveryDTO.BusinessDTO> getBusinessCodeByIds(@RequestBody List<String> ids);

    /**发货单查询业务单号
     *
     * @param deliveryCodes
     * @return
     */
    @PostMapping("/feign/firstMileDelivery/getBusinessCodeByCodes")
    List<FirstMileDeliveryDTO.BusinessDTO> getBusinessCodeByCodes(List<String> deliveryCodes);

    /**业务单号查询
     * @param businessCodes
     * @return
     */
    @PostMapping("/feign/firstMileDelivery/getDeliveryCodeByBusinessCodes")
    List<FirstMileDeliveryDTO.BusinessDTO> getDeliveryCodeByBusinessCodes(List<String> businessCodes);

    @PostMapping("/feign/firstMileDelivery/listGenerateLogisticDTO")
    List<FirstMileDeliveryDTO.GenerateLogisticDTO> listGenerateLogisticDTO(@RequestBody List<String> deliveryCodes);

    /**
     * 根据发货单中的目的仓查询三方仓关联的服务商
     * @param deliveryIds
     * @return
     */
    @PostMapping("/feign/firstMileDelivery/listOverseasProvider")
    List<OverseasProviderWarehouseDTO.ProviderDTO> listOverseasProvider(@RequestBody List<String> deliveryIds);
}
