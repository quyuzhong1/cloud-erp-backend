package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.server.wms.convert.FbaShipmentPackingConverter;
import com.erp.server.wms.mapper.FbaShipmentPackingMapper;
import com.erp.server.wms.service.FbaShipmentPackingService;
import com.erp.server.wms.service.FbaShipmentService;
import com.erp.server.wms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * fba货件装箱信息 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-09-03
 */
@Slf4j
@Service
public class FbaShipmentPackingServiceImpl extends SuperServiceImpl<FbaShipmentPackingMapper, FbaShipmentPackingEntity> implements FbaShipmentPackingService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private FbaShipmentService fbaShipmentService;

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Override
    @DataIdempotent(keyIdName = "data.boxNo")
    @Transactional(rollbackFor = Exception.class)
    public void handle(FbaShipmentPackingDTO.PackingDTO data) {
        if(Objects.isNull(data) || StringUtils.isBlank(data.getFbaShipmentCode())|| StringUtils.isBlank(data.getBoxNo()) ||CollectionUtil.isEmpty(data.getDetailDTOList())){
            return;
        }
        FbaShipmentEntity fbaShipmentEntity = fbaShipmentService.getByCode(data.getFbaShipmentCode());
        if(Objects.isNull(fbaShipmentEntity)){
            return;
        }
        String mainId = fbaShipmentEntity.getId();
        List<FbaShipmentPackingEntity> existList = getByMainIdAndBoxNo(mainId, data.getBoxNo());
        //已存在，删除后新增
        if(CollectionUtil.isNotEmpty(existList)){
            List<String> removeIds = existList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            log.error("删除装箱信息，idList:{}",removeIds);
            this.removeByIds(removeIds);
        }
        List<FbaShipmentPackingDTO.PackingDetailDTO> detailDTOList = data.getDetailDTOList();
        List<String> mskuList = detailDTOList.stream().map(FbaShipmentPackingDTO.PackingDetailDTO::getMsku).collect(Collectors.toList());
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
        paramDTO.setPlatformSkuNoList(mskuList);
        paramDTO.setShopIdList(Collections.singletonList(fbaShipmentEntity.getShopId()));
        paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
        paramDTO.setMatchResult(true);
        paramDTO.setIsExpire(false);
        // 查询ListingInfo和skuMapping的关系
        List<ListingInfoWithSkuMappingDTO> listingedInfoWithSkuMappingList = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO);

        // SKU相关信息
        Map<String, ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOMap = listingedInfoWithSkuMappingList.stream().collect(Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuNo, Function.identity()));

        List<FbaShipmentPackingEntity> addList = new ArrayList<>();
        detailDTOList.forEach(detailDTO -> {
            FbaShipmentPackingEntity entity = FbaShipmentPackingConverter.INSTANCE.fbaShipmentPackingConvert(data.getBoxNo(),detailDTO,fbaShipmentEntity,listingInfoWithSkuMappingDTOMap.get(detailDTO.getMsku()));
            addList.add(entity);
        });
        if(CollectionUtil.isNotEmpty(addList)){
            this.saveBatch(addList);
        }
    }

    @Override
    public List<FbaShipmentPackingEntity> getByMainIdAndBoxNo(String mainId, String boxNo) {
        if(StringUtils.isBlank(mainId) || StringUtils.isBlank(boxNo)){
            return new ArrayList<>();
        }
        return lambdaQuery().eq(FbaShipmentPackingEntity::getMainId, mainId).eq(FbaShipmentPackingEntity::getBoxNo, boxNo).list();
    }
}
