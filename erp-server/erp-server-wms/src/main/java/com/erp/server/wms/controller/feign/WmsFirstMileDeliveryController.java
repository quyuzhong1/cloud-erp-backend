package com.erp.server.wms.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.server.wms.service.CfgAmzFulfillmentCenterService;
import com.erp.server.wms.service.FirstMileDeliveryDetailService;
import com.erp.server.wms.service.FirstMileDeliveryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 头程发货单feign
 **/
@RestController
@RequestMapping("feign/firstMileDelivery")
public class WmsFirstMileDeliveryController {


    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;
    @Resource
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;
    @Resource
    private CfgAmzFulfillmentCenterService cfgAmzFulfillmentCenterService;

    /**
     * 根据入参查询单据数量
     **/
    @PostMapping("/getGenerateLogisticDTO")
    public List<FirstMileDeliveryDTO.GenerateLogisticDTO> getGenerateLogisticDTO(@RequestBody FirstMileDeliveryDTO.GenerateLogisticReqDTO dto) {
        return firstMileDeliveryService.getGenerateLogisticDTO(dto);
    }

    /**
     * 更新状态
     **/
    @PostMapping("/updateStatus")
    public Boolean updateStatus(@RequestBody FirstMileDeliveryDTO.UpdateStatusDTO dto) {
        return firstMileDeliveryService.updateStatus(dto);
    }

    /**
     * 统计
     * @param dto
     * @return
     */
    @PostMapping("/logisticStatistics")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:paging",
            tableAlias = "md"
    )
    public List<FirstMileDeliveryDTO.LogisticStatisticsDTO> logisticStatistics(@RequestBody FirstMileDeliveryDTO.StatisticsReq dto){
        return firstMileDeliveryService.logisticStatistics(dto);
    }


    /**
     * 高级查询
     * @param
     * @return
     */
    @PostMapping("/advanceQuery")
    @WebAdvanceQuery
    public List<FirstMileDeliveryEntity> advanceQuery(@RequestBody AdvanceQueryContainer advanceQueryContainer){
        return firstMileDeliveryService.advanceQuery(advanceQueryContainer);
    }

    /**
     * 查询可以生成报关单的发货单
     **/
    @PostMapping("/getCanGenerateDeclare")
    public List<TmsDeclareBillDTO.DeliveryDTO> getCanGenerateDeclare(@RequestBody TmsDeclareBillDTO.QuerySourceDTO dto) {
        return firstMileDeliveryService.getCanGenerateDeclare(dto);
    }
    /**
     * 查询可以生成报关单的发货单
     **/
    @PostMapping("/listByIds")
    List<FirstMileDeliveryEntity> listByIds(@RequestBody List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        return firstMileDeliveryService.listByIds(ids);
    }
    /**
     * 根据主表id获取明细记录
     */
    @PostMapping("/listDetailByMainIds")
    List<FirstMileDeliveryDetailEntity> listDetailByMainIds(@RequestBody List<String> mainIds){
        if (CollectionUtils.isEmpty(mainIds)){
            return Collections.emptyList();
        }
        return firstMileDeliveryDetailService.listByMainIds(mainIds);
    }

    /**
     * 亚马逊仓库中心配置
     */
    @GetMapping("/feign/firstMileDelivery/getCfgAmzCenter")
    List<CfgAmzFulfillmentCenterEntity> getCfgAmzCenter(){
        return cfgAmzFulfillmentCenterService.list();
    }

    /**
     * 根据编码查询发货单明细
     */
    @PostMapping("/listDetailByCodes")
    public List<FirstMileDeliveryDTO.ListFirstMileDTO> listDetailByCodes(@RequestBody List<String> codes){
        return firstMileDeliveryService.listDetailByCodes(codes, null);
    }
    /**
     * 根据来源编码查询发货单明细
     */
    @PostMapping("/listDetailBySourceCodes")
    public List<FirstMileDeliveryDTO.ListFirstMileDTO> listDetailBySourceCodes(@RequestBody List<String> sourceCodes){
        return firstMileDeliveryService.listDetailByCodes(null, sourceCodes);
    }

    /**
     * 根据业务单号统计签收数量
     * @param dto 业务单号  亚马逊签收报告/第三方仓签收
     * @return
     */
    @PostMapping("/countReceiveQtyByParams")
    public List<FirstMileDeliveryDTO.ReceiveDTO> countReceiveQtyByParams(@RequestBody FirstMileDeliveryDTO.RequestReceiveDTO dto){
        return firstMileDeliveryService.countReceiveQtyByParams(dto);
    }
}
