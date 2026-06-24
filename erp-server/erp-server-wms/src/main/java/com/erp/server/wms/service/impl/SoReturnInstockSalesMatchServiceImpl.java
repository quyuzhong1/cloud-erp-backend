package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoMultiChannelFeign;
import com.erp.server.wms.service.SoReturnInstockSalesMatchService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 多渠道退货入库：通过 WFHD 关联三方发货单/多渠道订单，回溯原始销售订单。
 */
@Slf4j
@Service
public class SoReturnInstockSalesMatchServiceImpl implements SoReturnInstockSalesMatchService {

    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

    @Resource
    private SoMultiChannelFeign soMultiChannelFeign;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Override
    public SoB2cEntity matchOriginalSoB2c(PlatformReturnInstockDTO dto) {
        if (Objects.isNull(dto)) {
            return null;
        }
        String deliveryCode = extractDeliveryCode(dto);
        if (CharSequenceUtil.isBlank(deliveryCode)) {
            return null;
        }
        SoB2cEntity soB2cEntity = resolveByThirdWarehouseDelivery(deliveryCode);
        if (Objects.nonNull(soB2cEntity)) {
            log.info("【退货入库】WFHD关联三方发货单匹配原始销售单: deliveryCode={}, soCode={}", deliveryCode, soB2cEntity.getCode());
            return soB2cEntity;
        }
        soB2cEntity = resolveByMultiChannel(deliveryCode);
        if (Objects.nonNull(soB2cEntity)) {
            log.info("【退货入库】WFHD关联多渠道订单匹配原始销售单: deliveryCode={}, soCode={}", deliveryCode, soB2cEntity.getCode());
        }
        return soB2cEntity;
    }

    private SoB2cEntity resolveByThirdWarehouseDelivery(String deliveryCode) {
        ThirdWarehouseDeliveryEntity thirdWarehouseDelivery = thirdWarehouseDeliveryService.getLatestByCode(deliveryCode);
        if (Objects.isNull(thirdWarehouseDelivery)) {
            return null;
        }
        if (CharSequenceUtil.isNotBlank(thirdWarehouseDelivery.getSoId())) {
            SoB2cEntity soB2cEntity = soB2cFeign.getById(thirdWarehouseDelivery.getSoId());
            if (Objects.nonNull(soB2cEntity)) {
                return soB2cEntity;
            }
        }
        if (CharSequenceUtil.isNotBlank(thirdWarehouseDelivery.getSoCode())) {
            return soB2cFeign.getSoCode(thirdWarehouseDelivery.getSoCode());
        }
        return null;
    }

    private SoB2cEntity resolveByMultiChannel(String deliveryCode) {
        SoMultiChannelEntity soMultiChannelEntity = soMultiChannelFeign.getByDeliveryCode(deliveryCode);
        if (Objects.isNull(soMultiChannelEntity) || CharSequenceUtil.isBlank(soMultiChannelEntity.getSoId())) {
            return null;
        }
        return soB2cFeign.getById(soMultiChannelEntity.getSoId());
    }

    private String extractDeliveryCode(PlatformReturnInstockDTO dto) {
        if (containsWfhd(dto.getOrderReferenceNo())) {
            return dto.getOrderReferenceNo();
        }
        if (containsWfhd(dto.getPlatformOrderNo())) {
            return dto.getPlatformOrderNo();
        }
        if (containsWfhd(dto.getPlatformReturnOrderNo())) {
            return dto.getPlatformReturnOrderNo();
        }
        return null;
    }

    private boolean containsWfhd(String value) {
        return CharSequenceUtil.isNotBlank(value) && value.contains(BusinessNoConstant.WFHD);
    }
}
