package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.WalmartShipDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.LogisticsMappingFeign;
import com.sdk.oms.walmart.service.WalmartSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.WALMART)
public class WalmartShipOrder implements IPlatformService {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private LogisticsMappingFeign logisticsMappingFeign;

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
            //标发订单类型
            String standardOrderType = walmartShipDTO.getOrderDeliveryMarkType();
            walmartShipDTO.setOrderDeliveryMarkType(standardOrderType);

            walmartSdkClientService.shipOrder(walmartShipDTO);
        }
    }

    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        return null;
    }

    /**
     * 查询并更新平台订单状态
     */
    @Override
    public Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto) {
        return null;
    }
}
