package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.enums.CfgSettingOrderTypeEnum;
import com.erp.server.wms.service.RequisitionApplicationService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.erp.server.wms.service.SoDeliveryNoticeService;
import com.erp.server.wms.service.VirtualFlowRefactorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VirtualFlowRefactorServiceImpl implements VirtualFlowRefactorService {

    @Resource
    private RequisitionApplicationService requisitionApplicationService;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Override
    public void rebuildFlow(String jobParam) {
        List<String> orderTypeList = CharSequenceUtil.isNotBlank(jobParam) ? Arrays.asList(jobParam.split(",")) :
                Arrays.stream(CfgSettingOrderTypeEnum.values()).map(CfgSettingOrderTypeEnum::getCode).collect(Collectors.toList());
        log.info("VirtualFlowRefactorServiceImpl rebuildFlow start");
        List<String> finalOrderTypeList = orderTypeList;
        finalOrderTypeList.stream().peek(obj -> {
            if ( CfgSettingOrderTypeEnum.B2B.getCode().equals(obj)) {
                //b2b
                rebuildB2bFlow();
            }
            if ( CfgSettingOrderTypeEnum.B2C.getCode().equals(obj)) {
                //b2c
                rebuildB2cFlow();
            }
            if ( CfgSettingOrderTypeEnum.FIRST_MILE.getCode().equals(obj)) {
                //firstMile
                rebuildFirstMileFlow();
            }
        }).collect(Collectors.toList());
        log.info("VirtualFlowRefactorServiceImpl rebuildFlow end");
    }

    /**
     * b2b流水
     * @author will
     * @date 2025/3/31 10:52
     */
    private void rebuildB2bFlow() {
       List<VirtualInventoryStockDTO.OutInStockDTO> list =  soDeliveryNoticeService.rebuildB2bVirtualFlow();
       if (CollUtil.isEmpty(list)) {
           return;
       }

    }

    /**
     * b2c流水
     * @author will
     * @date 2025/3/31 10:52
     */
    private void rebuildB2cFlow() {
        List<VirtualInventoryStockDTO.OutInStockDTO> list =  soB2cDeliveryService.rebuildB2cVirtualFlow();
    }

    /**
     * 头程流水
     * @author will
     * @date 2025/3/31 10:52
     */
    private void rebuildFirstMileFlow() {
        List<VirtualInventoryStockDTO.OutInStockDTO> list =  requisitionApplicationService.rebuildFirstMileVirtualFlow();

    }
}
