package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.oms.mapper.ListingInfoMapper;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.SkuMappingService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    @Resource
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;
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

    @Override
    public PagingVO<ListingInfoDTO.PageDTO> paging(PagingDTO<ListingInfoDTO.PagingParamDTO> dto) {
        ListingInfoDTO.PagingParamDTO pagingParamDTO = dto.getParams();
        if(StringUtils.isBlank(pagingParamDTO.getWarehouseId()) && StringUtils.isBlank(pagingParamDTO.getShopId())){
            throw new ServiceException("店铺和仓库不能同时为空");
        }
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        //如果传仓库ID，查询服务商，如果服务商为空，则查询仓库id，如果传店铺id，查询店铺下SKU
        if(StringUtils.isNotBlank(dto.getParams().getWarehouseId())){
            List<OverseasProviderWarehouseDTO.ViewDTO> viewDTOS = wmsOverseasWarehouseFeign.listByWarehouseIdList(Arrays.asList(dto.getParams().getWarehouseId()));
            if(CollectionUtils.isNotEmpty(viewDTOS)){
                OverseasProviderWarehouseDTO.ViewDTO viewDTO = viewDTOS.get(0);
                pagingParamDTO.setProviderCode(viewDTO.getProviderCode());
            }
        }
        IPage<ListingInfoDTO.PageDTO> iPage = baseMapper.paging(query,pagingParamDTO);
        this.fillData(iPage.getRecords());
        return new PagingVO<>(iPage);
    }

    @Override
    public Boolean warehouseSkuMapping(ListingInfoDTO.WarehouseSkuMappingParamDTO dto) {
        SkuMappingEntity skuMapping = skuMappingService.getById(dto.getId());
        if (ObjectUtil.isEmpty(skuMapping)) {
            throw new ServiceException(ApiError.ERROR_M_SKU_NOT_EXIST);
        }
        ListingInfoEntity listingInfoEntity = this.getById(skuMapping.getListingId());
        if (ObjectUtil.isEmpty(listingInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_M_SKU_NOT_EXIST);
        }
        List<OverseasProviderWarehouseDTO.ViewDTO> viewDTOList = wmsOverseasWarehouseFeign.listByWarehouseIdList(Arrays.asList(dto.getWarehouseId()));
        if(CollectionUtils.isEmpty(viewDTOList)){
            throw new ServiceException("查询不到仓库服务商");
        }
        String provideCode = viewDTOList.get(0).getProviderCode();
        PlatformDictEnum platformDictEnum = PlatformDictEnum.getByCode(provideCode);
        if(platformDictEnum == null){
            throw new ServiceException("查询不到仓库服务商");
        }
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(Arrays.asList(dto.getSkuNo()));
        SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(dto.getSkuNo())).distinct().findFirst().orElse(new SkuVO());
        if (ObjectUtil.isEmpty(skuVO)) {
            throw new ServiceException("sku不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        skuMapping.setExpireTime(now);
        skuMapping.setIsExpire(Boolean.TRUE);
        if (!skuMappingService.updateById(skuMapping)) {
            throw new ServiceException("[SkuMapping] 历史映射修改失败");
        }

        SkuMappingEntity skuMappingEntity = new SkuMappingEntity();
        skuMappingEntity.setWarehouseId(dto.getWarehouseId());
        skuMappingEntity.setWarehouseName(viewDTOList.get(0).getWarehouseName());
        skuMappingEntity.setType(RuleTypeEnum.WAREHOUSE);
        skuMappingEntity.setProductSkuId(skuVO.getSkuId());
        skuMappingEntity.setProductSkuNo(skuVO.getSkuNo());
        skuMappingEntity.setProductName(skuVO.getSkuName());
        skuMappingEntity.setListingId(listingInfoEntity.getId());
        skuMappingEntity.setDictPlatform(platformDictEnum.getCode());
        skuMappingEntity.setPlatformName(platformDictEnum.getName());
        skuMappingEntity.setHasMappingAll(true);

        //生效时间
        skuMappingEntity.setEffectiveTime(now);
        skuMappingEntity.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
        skuMappingService.save(skuMappingEntity);

        return lambdaUpdate()
                .set(ListingInfoEntity::getMatchResult, Boolean.TRUE)
                .eq(ListingInfoEntity::getId, listingInfoEntity.getId())
                .update();
    }

    private void fillData(List<ListingInfoDTO.PageDTO> records) {
        List<String> skuNo = records.stream().map(ListingInfoDTO.PageDTO::getSkuNo).collect(Collectors.toList());
        List<String> skuIds = records.stream().map(ListingInfoDTO.PageDTO::getSkuId).collect(Collectors.toList());

        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNo);

        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);

        records.forEach(v->{
            SkuVO skuVO = skuVOList.stream().filter(t->t.getSkuNo().equals(v.getSkuNo())).findFirst().orElse(null);
            if(Objects.nonNull(skuVO)){
                v.setImagesUrl(skuVO.getSkuImagesUrl());
                v.setProductName(skuVO.getSkuName());
            }
            if (CollectionUtils.isNotEmpty(bomChildrenList)) {
                long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(v.getSkuId())).count();
                v.setIsCombination(count > 0);
            }
        });
    }
}
