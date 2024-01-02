package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.PlatformAnnotate;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.WalmartShipDTO;
import com.common.business.dto.WalmartShipOrderDetailDTO;
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
        //检查销售订单是否存在
        SoB2cEntity soB2cEntity = soB2cFeign.getById(dto.getSoB2cId());
        if (ObjectUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //检查销售订单物流信息是否存在
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(soB2cEntity.getId()));
        if (CollectionUtils.isEmpty(soB2cLogisticsEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //映射主表字段
        WalmartShipDTO walmartShipDTO = WalmartShipOrderConverter.INSTANCE.soB2cEntityToWalmartShipDTO(soB2cEntity, soB2cLogisticsEntities.get(MathUtil.ZERO));

        //检查销售订单详情是否存在
        List<SoB2cDetailEntity> soB2cDetailEntities = soB2cFeign.listDetailByMainIds(Arrays.asList(dto.getSoB2cId()));
        if (CollectionUtils.isEmpty(soB2cDetailEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        //映射详情字段
        List<WalmartShipOrderDetailDTO> walmartShipOrderDetailDTOS = WalmartShipOrderConverter.INSTANCE.soB2cDetailEntityToWalmartShipOrderDetail(soB2cDetailEntities);
        walmartShipDTO.setDetailList(walmartShipOrderDetailDTOS);

        //调用sdk发货
        WalmartSdkClientService walmartSdkClientService = new WalmartSdkClientService();
        walmartSdkClientService.shipOrder(walmartShipDTO);
    }
}
