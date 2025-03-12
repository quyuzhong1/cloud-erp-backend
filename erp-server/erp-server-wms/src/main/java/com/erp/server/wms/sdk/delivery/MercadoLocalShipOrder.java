package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.lang.Tuple;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.sdk.oms.mercado.dto.MercadoShipOrderDTO;
import com.sdk.oms.mercado.service.MercadoLocalSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.MERCADOLIBRE_LOCAL)
public class MercadoLocalShipOrder extends AbstractShipOrder {
    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private MercadoLocalSdkClientService mercadoLocalSdkClientService;

    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        Tuple tuple = super.allSourceOrderInfo(dto);
        // 所有源单信息
        List<SoB2cEntity> sourceOrderList = tuple.get(0);
        // 当前单据物流信息
        SoB2cLogisticsEntity logisticsEntity = tuple.get(2);

        List<String> ids = sourceOrderList.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByIds(ids);

        for (SoB2cEntity entity : sourceOrderList) {
            //组装数据
            MercadoShipOrderDTO shipOrderDTO = new MercadoShipOrderDTO();
            shipOrderDTO.setShopId(entity.getShopId());
            JSONObject jsonObject = JSONObject.parseObject(entity.getExtendData());
            shipOrderDTO.setShipmentId(String.valueOf(jsonObject.get("shipmentId")));
            shipOrderDTO.setCarrier(logisticsEntity.getLogisticsChannelName());
            shipOrderDTO.setTrackingUrl("https://www.17track.net/en");

            //标记发货
            mercadoLocalSdkClientService.shipOrder(shipOrderDTO);
        }
        List<String> detailIdList = soB2cDetailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        return detailIdList;
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
