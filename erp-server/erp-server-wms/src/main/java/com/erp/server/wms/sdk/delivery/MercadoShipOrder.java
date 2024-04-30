package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.sdk.oms.mercado.dto.MercadoShipOrderDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.MERCADOLIBRE)
public class MercadoShipOrder implements IPlatformService {
    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private MercadoSdkClientService mercadoSdkClientService;

    @Override
    public void shipOrder(PlatformShipOrderDTO dto) {
        SoB2cEntity entity = soB2cFeign.getById(dto.getSoB2cId());

        //映射发货需要的字段，如果合并的订单拆分返回
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(dto.getSoB2cId()));
        if (CollectionUtil.isEmpty(soB2cLogisticsEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //组装数据
        MercadoShipOrderDTO shipOrderDTO = new MercadoShipOrderDTO();
        shipOrderDTO.setShopId(entity.getShopId());
        JSONObject jsonObject = JSONObject.parseObject(entity.getExtendData());
        shipOrderDTO.setShipmentId(String.valueOf(jsonObject.get("shipmentId")));
        shipOrderDTO.setCarrier(soB2cLogisticsEntities.get(0).getLogisticsChannelName());
        shipOrderDTO.setTrackingUrl("https://www.17track.net/en");

        //标记发货
        mercadoSdkClientService.shipOrder(shipOrderDTO);
    }


    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        return null;
    }
}
