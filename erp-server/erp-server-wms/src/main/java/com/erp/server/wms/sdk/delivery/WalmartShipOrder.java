package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.PlatformAnnotate;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.WalmartShipDTO;
import com.common.business.dto.WalmartShipOrderDetailDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.convert.WalmartShipOrderConverter;
import com.sdk.oms.walmart.service.WalmartSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.WALMART)
public class WalmartShipOrder implements IPlatformService {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Override
    public void shipOrder(PlatformShipOrderDTO dto) {
        //映射发货需要的字段，如果合并的订单拆分返回
        List<WalmartShipDTO> walmartShipOrderParam = soB2cFeign.getWalmartShipOrderParam(dto.getSoB2cId());

        //调用sdk发货
        for (WalmartShipDTO walmartShipDTO : walmartShipOrderParam) {
            WalmartSdkClientService walmartSdkClientService = new WalmartSdkClientService();

            if (LogisticsPlatformEnum.YAN_WEN.getCode().equals(walmartShipDTO.getLogisticsPlatformCode())) {
                walmartShipDTO.setLogisticsPlatformCode("Yanwen");
            } else if (LogisticsPlatformEnum.SF_EXPRESS.getCode().equals(walmartShipDTO.getLogisticsPlatformCode())) {
                walmartShipDTO.setLogisticsPlatformCode("SF Express");
            }
            walmartSdkClientService.shipOrder(walmartShipDTO);
        }
    }


}
