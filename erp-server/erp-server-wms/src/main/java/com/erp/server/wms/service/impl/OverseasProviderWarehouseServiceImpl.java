package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OverseasInventoryDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.OverseasWarehouseConverter;
import com.erp.server.wms.mapper.OverseasProviderWarehouseMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 海外物流商仓库 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasProviderWarehouseServiceImpl extends SuperServiceImpl<OverseasProviderWarehouseMapper, OverseasProviderWarehouseEntity> implements OverseasProviderWarehouseService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private WarehouseService warehouseService;

    @Resource
    private OverseasProviderService overseasProviderService;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private OverseasInventoryService overseasInventoryService;
    /**
     * 修改
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasProviderDTO.UpdateDTO updateDTO, String mainId) {
        List<OverseasProviderWarehouseDTO.UpdateDTO> detailList = updateDTO.getDetailList();
        //映射字段
        List<OverseasProviderWarehouseEntity> list = BeanMapperUtils.copyList(OverseasProviderWarehouseEntity.class, detailList);
        // 数据处理
        handleData(list, mainId);
        boolean save = this.updateBatchById(list);
        if (!save) {
            throw new ServiceException("海外物流商仓库保存失败");
        } else {
            //第三方映射绑定
            addThirdMapping(updateDTO, list);
        }
        return Boolean.TRUE;
    }

    /**
     * 第三方映射绑定
     *
     * @param updateDTO
     * @param list
     */
    private void addThirdMapping(OverseasProviderDTO.UpdateDTO updateDTO, List<OverseasProviderWarehouseEntity> list) {
        ThirdMappingDTO.FeignMappingDTO feignMappingDTO = new ThirdMappingDTO.FeignMappingDTO();
        List<ThirdMappingDTO.ThirdAddDTO> addDTOList = new ArrayList<>();
        list.forEach(item -> {
            if (CharSequenceUtil.isNotBlank(item.getWarehouseId()) && Objects.equals(item.getDisabled(), false)) {
                //绑定第三方配置关系
                ThirdMappingDTO.ThirdAddDTO addDTO = new ThirdMappingDTO.ThirdAddDTO();
                addDTO.setType(ThirdSysTypeEnum.WAREHOUSE.getCode());
                addDTO.setSysId(item.getWarehouseId());
                addDTO.setSysCode(item.getWarehouseCode());
                addDTO.setSysName(item.getWarehouseName());
                addDTO.setSysType(updateDTO.getCode());
                addDTO.setThirdId(item.getId());
                addDTO.setThirdCode(item.getPlatformWarehouseCode());
                addDTO.setThirdName(item.getPlatformWarehouseName());
                addDTOList.add(addDTO);
            }
        });
        feignMappingDTO.setType(ThirdSysTypeEnum.WAREHOUSE.getCode());
        feignMappingDTO.setThirdSysType(updateDTO.getCode());
        feignMappingDTO.setAddDTOList(addDTOList);
        dmpThirdMappingFeign.batchAdd(feignMappingDTO);
    }

    @Override
    public OverseasProviderWarehouseEntity getByWarehouseId(String warehouseId) {
        return lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getWarehouseId, warehouseId)
                .orderByAsc(OverseasProviderWarehouseEntity::getId)
                .last("LIMIT 1")
                .one();
    }


    @Override
    public OverseasProviderWarehouseEntity getByWarehouseIdWithNotDisabled(String warehouseId) {
        return lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getWarehouseId, warehouseId)
                .eq(OverseasProviderWarehouseEntity::getDisabled, false)
                .orderByAsc(OverseasProviderWarehouseEntity::getId)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<OverseasProviderWarehouseEntity> listByWarehouseIds(List<String> warehouseIds) {
        if (CollectionUtils.isEmpty(warehouseIds)) {
            return Collections.emptyList();
        }

        return lambdaQuery()
                .in(OverseasProviderWarehouseEntity::getWarehouseId, warehouseIds)
                .list();
    }

    @Override
    public List<OverseasProviderWarehouseDTO.ViewDTO> listByWarehouseIdList(List<String> warehouseIds) {
        if (CollectionUtils.isEmpty(warehouseIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listByWarehouseIdList(warehouseIds);
    }

    @Override
    public OverseasProviderWarehouseEntity getByPlatform(String mainId, String platformWarehouseCode) {
        return lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getMainId, mainId)
                .eq(OverseasProviderWarehouseEntity::getPlatformWarehouseCode, platformWarehouseCode)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<OverseasProviderWarehouseEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(OverseasProviderWarehouseEntity::getMainId, mainIds).list();
    }

    @Override
    public List<OverseasProviderWarehouseEntity> listByPlatformWarehouseCode(List<String> warehouseCodeList, String platform) {
        String mainId = overseasProviderService.getByPlatformCode(platform).getId();
        return lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getMainId, mainId)
                .in(OverseasProviderWarehouseEntity::getPlatformWarehouseCode, warehouseCodeList)
                .list();
    }

    @Override
    public OverseasProviderEntity findPlatformByWarehouseId(String warehouseId) {
        List<OverseasProviderWarehouseEntity> entityList = listByWarehouseIds(Collections.singletonList(warehouseId));
        if (CollectionUtils.isEmpty(entityList)) {
            return null;
        }
        List<String> mainIds = entityList.stream().map(v->v.getMainId()).distinct().collect(Collectors.toList());
        List<OverseasProviderEntity> overseasProviderEntityList = overseasProviderService.listByIds(mainIds);
        overseasProviderEntityList = overseasProviderEntityList.stream().filter(v->v.getAuthStatus().equals(AuthStatusEnum.ALREADY.getCode())).collect(Collectors.toList());
        return CollectionUtils.isEmpty(overseasProviderEntityList)?null:overseasProviderEntityList.get(0);
    }

    @Override
    public PagingVO<ThirdWarehouseDTO.PageSelectDTO> pagingSelect(PagingDTO<OverseasProviderWarehouseDTO.SelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ThirdWarehouseDTO.PageSelectDTO> pageData = this.baseMapper.pagingSelect(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO<>(pageData);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(List<OverseasProviderWarehouseEntity> list, String mainId) {
        List<OverseasProviderWarehouseEntity> oldList = this.listByMainIds(Collections.singletonList(mainId));
        List<String> warehouseIds = list.stream().map(req -> req.getWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseDtoList = warehouseService.listWarehouseByIds(warehouseIds);

        //查询绑定的仓库
        List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntities = this.listByWarehouseIds(warehouseIds);

        for (OverseasProviderWarehouseEntity detailEntity : list) {
            //仓库信息
            WarehouseDTO.UpdateDTO updateDTO = warehouseDtoList.stream().filter(req -> req.getId().equals(detailEntity.getWarehouseId())).distinct().findFirst().orElse(new WarehouseDTO.UpdateDTO());

            //如果启用，校验仓库是否绑定
            if (!detailEntity.getDisabled()) {
                if (CharSequenceUtil.isBlank(detailEntity.getWarehouseId())) {
                    throw new ServiceException(ApiError.ERROR_NOT_WAREHOUSE);
                }

                //已绑定第三方供应商仓，一个仓库只能绑定一个第三方仓
                long warehouseCount = list.stream()
                        .filter(req -> !req.getDisabled()
                                && req.getWarehouseId().equals(detailEntity.getWarehouseId()))
                        .count();
                if (warehouseCount > 1) {
                    throw new ServiceException(ApiError.WAREHOUSE_REPEAT_BINDING, updateDTO.getName());
                }
                long count = overseasProviderWarehouseEntities.stream().filter(req -> !req.getDisabled()
                        && req.getWarehouseId().equals(detailEntity.getWarehouseId()) && !Objects.equals(req.getMainId(), mainId)).count();
                if (count > 1) {
                    throw new ServiceException(ApiError.WAREHOUSE_REPEAT_BINDING, updateDTO.getName());
                }
            }
            detailEntity.setMainId(mainId);
            detailEntity.setWarehouseCode(updateDTO.getKingdeeWarehouseCode());
            detailEntity.setWarehouseName(updateDTO.getName());

            //校验是否是修改，如果是就新增修改日志
            if (CharSequenceUtil.isNotBlank(detailEntity.getId())) {
                OverseasProviderWarehouseEntity old = oldList.stream().filter(obj -> obj.getId().equals(detailEntity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_NOT_FBA_DELIVERY_DETAIL);
                }
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), mainId, "", String.format("【%s】", old.getPlatformWarehouseName()));
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean feignBind(OverseasProviderDTO.FeignDTO feignDTO) {
        //获取系统仓库获取绑定的第三方仓
        OverseasProviderWarehouseEntity providerWarehouseEntity = this.getByWarehouseId(feignDTO.getWarehouseId());
        if (Objects.nonNull(providerWarehouseEntity)) {
            //有效数据直接删除关联
            if (!providerWarehouseEntity.getDisabled() && !Objects.equals(providerWarehouseEntity.getId(), feignDTO.getOverseasProviderWarehouseId())) {
                OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = new OverseasProviderWarehouseEntity();
                overseasProviderWarehouseEntity.setWarehouseId("");
                overseasProviderWarehouseEntity.setWarehouseName("");
                overseasProviderWarehouseEntity.setId(providerWarehouseEntity.getId());
                overseasProviderWarehouseEntity.setWarehouseCode("");
                overseasProviderWarehouseEntity.setDisabled(true);
                baseMapper.updateById(overseasProviderWarehouseEntity);
            }

        }
        OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = new OverseasProviderWarehouseEntity();
        overseasProviderWarehouseEntity.setWarehouseId(feignDTO.getWarehouseId());
        overseasProviderWarehouseEntity.setWarehouseName(feignDTO.getWarehouseName());
        overseasProviderWarehouseEntity.setId(feignDTO.getOverseasProviderWarehouseId());
        overseasProviderWarehouseEntity.setWarehouseCode(feignDTO.getWarehouseCode());
        overseasProviderWarehouseEntity.setDisabled(feignDTO.getDisabled());
        int flag = baseMapper.updateById(overseasProviderWarehouseEntity);
        if (flag <= 0) {
            throw new ServiceException(ApiError.ERROR_BINDING);
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean isApiWarehouse(String destWarehouseId) {
        if(CharSequenceUtil.isBlank(destWarehouseId)){
            return false;
        }
        OverseasProviderWarehouseEntity entity = getByWarehouseId(destWarehouseId);
        if (null == entity || entity.getDisabled()) {
            return false;
        }

        OverseasProviderEntity providerEntity = overseasProviderService.getById(entity.getMainId());
        if (null == providerEntity || !AuthStatusEnum.ALREADY.getCode().equals(providerEntity.getAuthStatus())) {
            return false;
        }

        return true;
    }

    @Override
    public List<String> listProviderWarehouseBySql(String compareCodeSplicingValueSql) {
        return baseMapper.listProviderWarehouseBySql(compareCodeSplicingValueSql);
    }

    @Override
    public List<OverseasProviderWarehouseDTO.ShippedViewDTO> getShippedInfo(OverseasProviderWarehouseDTO.ShippedDTO shippedDTO) {
        //仓库列表
        List<String> warehouseCodeList = shippedDTO.getWarehouseCodeList();
        if (CollUtil.isEmpty(warehouseCodeList)){
            return Collections.emptyList();
        }
        //根据平台配置进行匹配产品sku
        ListingInfoParamDTO dto = new ListingInfoParamDTO();
        dto.setPlatform(PlatformDictEnum.SHOPIFY.getCode());
//        dto.setType(RuleTypeEnum.PLATFORM.code);
        String platformSku = shippedDTO.getPlatformSku();
        if (CharSequenceUtil.isNotBlank(platformSku)){
            dto.setPlatformSkuNoList(Collections.singletonList(platformSku));
        }
        String platformProductId = shippedDTO.getPlatformProductId();
        if (CharSequenceUtil.isNotBlank(platformProductId)){
            dto.setPlatformSpuNoList(Collections.singletonList(platformProductId));
        }
        dto.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
        dto.setIsExpire(false);
        List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOS = skuMappingFeign.listingInfoWithSkuMappingList(dto);
        if (CollUtil.isEmpty(listingInfoWithSkuMappingDTOS)){
            return Collections.emptyList();
        }
        ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = listingInfoWithSkuMappingDTOS.get(0);
        //erp映射的sku
        List<String> skuIds;
        //根据是否组合品获取子件
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(Collections.singletonList(listingInfoWithSkuMappingDTO.getProductSkuId()));
        if (CollUtil.isNotEmpty(bomChildrenSkuList)){
            List<BomChildrenSkuDTO> bomChildren = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(listingInfoWithSkuMappingDTO.getProductSkuId())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).collect(Collectors.toList());
            skuIds = bomChildren.stream().map(BomChildrenSkuDTO::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        }else {
            skuIds = Collections.singletonList(listingInfoWithSkuMappingDTO.getProductSkuId());
        }
        //查询三方仓库存
        OverseasInventoryDTO.QueryDTO queryDTO = new OverseasInventoryDTO.QueryDTO();
        queryDTO.setSkuIds(skuIds);
        if (CollUtil.isNotEmpty(skuIds) && 1 == skuIds.size()){
            queryDTO.setPlatformSku(platformSku);
        }
        queryDTO.setPlatformWarehouseCodeList(warehouseCodeList);
        List<OverseasInventoryEntity> inventoryEntityList = overseasInventoryService.listBySkuAndWarehouseCode(queryDTO);
        if (CollUtil.isEmpty(inventoryEntityList)){
            return Collections.emptyList();
        }
        //匹配存在库存的仓库
        List<OverseasInventoryEntity> overseasInventoryEntities = inventoryEntityList.stream().filter(e -> Objects.nonNull(e.getSellableQty()) && e.getSellableQty() > 0 && warehouseCodeList.contains(e.getWarehouseCode())).collect(Collectors.toList());
        if (CollUtil.isEmpty(overseasInventoryEntities)){
            return Collections.emptyList();
        }
        return OverseasWarehouseConverter.INSTANCE.inventoryToShipmentDTO(overseasInventoryEntities);
    }
}
