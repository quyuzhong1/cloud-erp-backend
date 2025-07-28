package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.oms.dto.GenerateDeliveryAndOutStockDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.server.wms.mapper.ThirdWarehouseDeliveryMapper;
import com.erp.server.wms.rocketmq.consumer.PlatformOutboundConsumerService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryDetailService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import io.seata.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 三方仓发货单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
@Slf4j
@Service
public class ThirdWarehouseDeliveryServiceImpl extends SuperServiceImpl<ThirdWarehouseDeliveryMapper, ThirdWarehouseDeliveryEntity> implements ThirdWarehouseDeliveryService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private ThirdWarehouseDeliveryDetailService detailService;

    @Resource
    private ThirdWarehouseDeliveryService service;

    @Resource
    private PlatformOutboundConsumerService platformOutboundConsumerService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ThirdWarehouseDeliveryEntity add(ThirdWarehouseDeliveryEntity entity) {

        // 生成单号
        boolean save = super.save(entity);
        if(!save) {
            throw new ServiceException("三方仓发货单保存失败");
        }
        entity.getDetailEntityList().forEach(v->{
            v.setMainId(entity.getId());
        });
        detailService.saveBatch(entity.getDetailEntityList());
        return entity;
    }

    @Override
    public ThirdWarehouseDeliveryEntity getByCodeAndSoId(String outCode, String soId) {
        if(StringUtils.isBlank(outCode) || StringUtils.isBlank(soId)){
            return null;
        }
        return lambdaQuery().eq(ThirdWarehouseDeliveryEntity::getCode,outCode).eq(ThirdWarehouseDeliveryEntity::getSoId,soId).last("LIMIT 1").one();
    }

    @Override
    public ThirdWarehouseDeliveryEntity getLatestBySoId(String soId) {
        if(StringUtils.isBlank(soId)){
            return null;
        }
        return lambdaQuery().eq(ThirdWarehouseDeliveryEntity::getSoId, soId)
                .orderByDesc(ThirdWarehouseDeliveryEntity::getCreateTime).last("LIMIT 1").one();
    }

    @Override
    public ThirdWarehouseDeliveryEntity getLatestByCode(String code) {
        if(StringUtils.isBlank(code)){
            return null;
        }
        return lambdaQuery().eq(ThirdWarehouseDeliveryEntity::getCode, code).one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateDeliveryAndOutStock(GenerateDeliveryAndOutStockDTO generateDeliveryAndOutStockDTO) {
        SoB2cEntity entity = generateDeliveryAndOutStockDTO.getEntity();
        List<SoB2cDetailEntity> soB2cDetailEntityList = generateDeliveryAndOutStockDTO.getDetailEntityList();
        SoB2cDTO.DeliveryWithNotOutboundDTO deliveryWithNotOutboundDTO = generateDeliveryAndOutStockDTO.getDto();
        OverseasProviderWarehouseDTO.ViewDTO viewDTO = generateDeliveryAndOutStockDTO.getOverseasWarehouseDto();
        ThirdWarehouseDeliveryEntity addThirdWarehouseDeliveryEntity = new ThirdWarehouseDeliveryEntity();
        addThirdWarehouseDeliveryEntity.setSoCode(entity.getCode());
        addThirdWarehouseDeliveryEntity.setSoId(entity.getId());
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_WFHD);
        addThirdWarehouseDeliveryEntity.setCode(code);
        addThirdWarehouseDeliveryEntity.setDictPlatform(entity.getDictPlatform());
        addThirdWarehouseDeliveryEntity.setPlatformCode(entity.getPlatformCode());
        addThirdWarehouseDeliveryEntity.setThirdWarehousePlatform(viewDTO.getProviderCode());
        addThirdWarehouseDeliveryEntity.setShippingMethod(deliveryWithNotOutboundDTO.getLogisticsChannelCode());
        List<ThirdWarehouseDeliveryDetailEntity> thirdWarehouseDetailList = new ArrayList<>();
        for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailEntityList) {
            ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity = new ThirdWarehouseDeliveryDetailEntity();
            thirdWarehouseDeliveryDetailEntity.setSkuId(soB2cDetailEntity.getSkuId());
            thirdWarehouseDeliveryDetailEntity.setSkuNo(soB2cDetailEntity.getSkuNo());
            thirdWarehouseDeliveryDetailEntity.setDeliveryQty(soB2cDetailEntity.getQty());
            thirdWarehouseDeliveryDetailEntity.setWarehouseId(deliveryWithNotOutboundDTO.getWarehouseId());
            thirdWarehouseDeliveryDetailEntity.setPlatformSkuNo("");
            thirdWarehouseDeliveryDetailEntity.setPlatformWarehouseCode(viewDTO.getPlatformWarehouseCode());
            thirdWarehouseDeliveryDetailEntity.setSourceSkuId(soB2cDetailEntity.getSkuId());
            thirdWarehouseDeliveryDetailEntity.setSourceSkuNo(soB2cDetailEntity.getSkuNo());
            thirdWarehouseDetailList.add(thirdWarehouseDeliveryDetailEntity);
        }
        addThirdWarehouseDeliveryEntity.setDetailEntityList(thirdWarehouseDetailList);
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = service.add(addThirdWarehouseDeliveryEntity);
        PlatformOutboundDTO platformOutboundDTO = new PlatformOutboundDTO();
        platformOutboundDTO.setOutBoundTime(deliveryWithNotOutboundDTO.getDeliveryTime());
        platformOutboundDTO.setTrackNo(deliveryWithNotOutboundDTO.getTrackNo());
        platformOutboundConsumerService.generateSoOut(entity,thirdWarehouseDeliveryEntity,platformOutboundDTO);
    }

}
