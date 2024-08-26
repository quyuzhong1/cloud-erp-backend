package com.erp.server.tms.schedule;

import com.common.business.dto.base.BatchResultDTO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.service.FirstMileWeightAllocationService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 头程重量分摊相关任务
 * @date 2024-08-22
 * @author tanmujin
 */
@Slf4j
@Component
public class FirstMileWeightAllocationJob {

    @Resource
    private TmsFirstMileLogisticService firstMileLogisticService;
    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private LogisticsChannelService logisticsChannelService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    /**
     * 自动生成头程重量分摊
     */
    @XxlJob("autoGenFirstMileWeightAllocation")
    public ReturnT<String> autoGenFirstMileWeightAllocation() {
        //重量分摊基础数据
        List<TmsFirstMileLogisticDTO.WeightAllocationDTO> list = firstMileLogisticService.assembleFirstMileEstimatedList();
        if(list.isEmpty()){
            XxlJobHelper.log("没有找到物流状态为【已下单】的物流单");
            return ReturnT.SUCCESS;
        }
        //头程发货单
        List<String> outstockIdList = list.stream().map(item -> item.getSourceId()).distinct().collect(Collectors.toList());
        FirstMileDeliveryDTO.GenerateLogisticReqDTO reqDto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        reqDto.setIds(outstockIdList);
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTOList = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(reqDto);
        Map<String, FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTOMap = generateLogisticDTOList.stream().collect(Collectors.toMap(item -> item.getOutstockId(), item2 -> item2));
        //物流渠道
        List<String> channelIds = list.stream().map(item -> item.getChannelId()).distinct().collect(Collectors.toList());
        List<LogisticsChannelEntity> channelList = logisticsChannelService.listByIds(channelIds);
        Map<String, LogisticsChannelEntity> channelMap = channelList.stream().collect(Collectors.toMap(item -> item.getId(), item2 -> item2));
        //物流商
        List<String> supplierIds = list.stream().map(item -> item.getSupplierId()).distinct().collect(Collectors.toList());
        List<LogisticsSupplierEntity> supplierList = logisticsSupplierService.listByIds(supplierIds);
        Map<String, LogisticsSupplierEntity> supplierMap = supplierList.stream().collect(Collectors.toMap(item -> item.getId(), item2 -> item2));
        for (TmsFirstMileLogisticDTO.WeightAllocationDTO allocationDTO : list) {
            FirstMileWeightAllocationDTO.AddDTO dto = new FirstMileWeightAllocationDTO.AddDTO();
            BeanMapper.copy(allocationDTO, dto);
            if(generateLogisticDTOMap.containsKey(allocationDTO.getSourceId())){
                FirstMileDeliveryDTO.GenerateLogisticDTO deliveryDto = generateLogisticDTOMap.get(allocationDTO.getSourceId());
                dto.setFromWarehouseId(deliveryDto.getFromWarehouseId());
                dto.setPackingDTOList(deliveryDto.getPackingDTOList());
            }
            if(channelMap.containsKey(allocationDTO.getChannelId())){
                LogisticsChannelEntity logisticsChannel = channelMap.get(allocationDTO.getChannelId());
                dto.setChannelId(allocationDTO.getChannelId());
                dto.setVolumeSetting(logisticsChannel.getVolumeSetting());
                dto.setFeeRule(logisticsChannel.getFeeRule());
            }
            if(supplierMap.containsKey(allocationDTO.getSupplierId())){
                dto.setSupplierName(supplierMap.get(allocationDTO.getSupplierId()).getSupplierName());
            }
            try {
                BatchResultDTO resultDTO = firstMileWeightAllocationService.add(dto);
            }catch (ServiceException e){
                XxlJobHelper.log("生成重量分摊失败：{} {}",allocationDTO.getSourceCode(), e.getMessage());
            }
        }
        return ReturnT.SUCCESS;
    }
}
