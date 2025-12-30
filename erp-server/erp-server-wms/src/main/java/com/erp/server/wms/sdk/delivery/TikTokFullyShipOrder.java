package com.erp.server.wms.sdk.delivery;

import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.TIK_TOK_FULLY)
public class TikTokFullyShipOrder extends AbstractShipOrder {

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;
    public static void main(String[] args) {
    }

    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        return Collections.emptyList();
    }

    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        return null;
    }

    @Override
    public Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto) {
        return null;
    }

    @Override
    public Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList) {
        return null;
    }
}
