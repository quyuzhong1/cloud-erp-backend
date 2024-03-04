package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.convert.OmsListingConverter;
import com.erp.server.oms.mapper.ListingInfoMapper;
import com.erp.server.oms.service.ListingInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.oms.service.SkuMappingService;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 对应平台sku 表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-18
 */
@Service
public class ListingInfoServiceImpl extends SuperServiceImpl<ListingInfoMapper, ListingInfoEntity> implements ListingInfoService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SkuMappingService skuMappingService;


    /**
     * 添加库存sku
     *
     * @param skuNo
     * @param productName
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addWarehouseSku(String skuNo, String productName) {
        String id = IdWorker.getIdStr();
        ListingInfoEntity listingInfoEntity = new ListingInfoEntity();
        listingInfoEntity.setId(id);
        listingInfoEntity.setPlatformSkuNo(skuNo);
        listingInfoEntity.setPlatformSkuName(productName);
        listingInfoEntity.setType(RuleTypeEnum.WAREHOUSE.getCode());
        if (this.save(listingInfoEntity)) {
            return id;
        } else {
            return "";
        }
    }


    /**
     * 根据平台sku ，平台 获取到对应的list
     * @author yl
     * @date 2023-08-21 11:57
     * @param platformSkuNo
     * @param platform
     * @return com.erp.model.oms.entity.ListingInfoEntity
     */
    @Override
    public ListingInfoEntity getByPlatformSkuNo(String platform, String platformSkuNo) {
        return lambdaQuery().eq(ListingInfoEntity::getPlatformSkuNo, platformSkuNo).
                eq(ListingInfoEntity::getPlatform, platform).
                last("LIMIT 1").
                one();
    }


    /**
     * 根据类型获取到对应数据
     * @author yl
     * @date 2023-08-24 14:54
     * @param type
     * @return java.util.List<com.erp.model.oms.dto.ListingInfoDTO.ListDTO>
     */
    @Override
    public List<ListingInfoDTO.ListDTO> listByType(String type) {
        return baseMapper.listByType(type);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean skuMapping(FbaShipmentDTO.skuMappingParamDTO dto) {
        SkuMappingEntity skuMapping = skuMappingService.getById(dto.getId());
        if (ObjectUtil.isEmpty(skuMapping)) {
            throw new ServiceException(ApiError.ERROR_M_SKU_NOT_EXIST);
        }
        ListingInfoEntity listingInfoEntity = this.getById(skuMapping.getListingId());
        if (ObjectUtil.isEmpty(listingInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_M_SKU_NOT_EXIST);
        }
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(Arrays.asList(dto.getSkuNo()));
        SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(dto.getSkuNo())).distinct().findFirst().orElse(null);
        if (ObjectUtil.isEmpty(skuVO)) {
            throw new ServiceException("sku不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        skuMapping.setExpireTime(now);
        skuMapping.setIsDeleted(true);
        skuMapping.setIsExpire(Boolean.TRUE);
        if (!skuMappingService.updateById(skuMapping)) {
            throw new ServiceException("[SkuMapping] 历史映射修改失败");
        }

        SkuMappingEntity skuMappingEntity = new SkuMappingEntity();
        skuMappingEntity.setWarehouseId("");
        skuMappingEntity.setWarehouseName("");
        skuMappingEntity.setType(RuleTypeEnum.PLATFORM);
        skuMappingEntity.setShopId(dto.getShopId());
        skuMappingEntity.setProductSkuId(skuVO.getSkuId());
        skuMappingEntity.setProductSkuNo(skuVO.getSkuNo());
        skuMappingEntity.setProductName(skuVO.getSkuName());
        skuMappingEntity.setListingId(listingInfoEntity.getId());
        skuMappingEntity.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
        skuMappingEntity.setPlatformName(PlatformDictEnum.AMAZON.getName());

        //生效时间
        skuMappingEntity.setEffectiveTime(now);
        skuMappingEntity.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
        skuMappingService.save(skuMappingEntity);

        return lambdaUpdate()
                .set(ListingInfoEntity::getMatchResult, Boolean.TRUE)
                .eq(ListingInfoEntity::getId, listingInfoEntity.getId())
                .update();
    }

    @Override
    public List<ListingInfoDTO.BaseDropDownDTO> listByTypeWithFieldName(ListingInfoDTO.BaseDropDownParamDTO dto) {
        String type = dto.checkAndGetType();

        List<ListingInfoEntity> list = lambdaQuery()
                .eq(ListingInfoEntity::getType, type)
                .list();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        String fieldName = dto.checkAndGetFieldName();
        // 组合
        return list.stream()
                .map(e -> ReflectUtil.getFieldValue(e, fieldName))
                .distinct()
                .filter(e -> Objects.nonNull(e) && StringUtils.isNotBlank(e.toString()))
                .map(e -> ListingInfoDTO.BaseDropDownDTO.init(e.toString()))
                .collect(Collectors.toList());
    }

    @Override
    public ListingInfoEntity getByPlatformSkuNoAndSpu(String platformSkuNo, String platformSpuNo, String typeCode) {
        return this.lambdaQuery().
                eq(ListingInfoEntity::getPlatformSkuNo,platformSkuNo).
                eq(ListingInfoEntity::getPlatformSpuNo,platformSpuNo).
                eq(ListingInfoEntity::getType,typeCode).
                last("LIMIT 1").one();
    }

    @Override
    public void updateMatchResult(String listingId, Boolean matchResult) {
        this.lambdaUpdate().set(ListingInfoEntity::getMatchResult,matchResult).
                eq(ListingInfoEntity::getId,listingId).update(new ListingInfoEntity());
    }
}
