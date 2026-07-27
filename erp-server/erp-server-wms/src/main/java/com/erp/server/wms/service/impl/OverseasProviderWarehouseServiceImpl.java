package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.ListingMatchResultEnum;
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
import com.erp.model.wms.enums.ShopSiteEnum;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.server.wms.convert.OverseasWarehouseConverter;
import com.erp.server.wms.mapper.OverseasProviderWarehouseMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Resource
    private ShopInfoFeign shopInfoFeign;
    /**
     * 修改
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasProviderDTO.UpdateDTO updateDTO, String mainId) {
        List<OverseasProviderWarehouseDTO.UpdateDTO> detailList = updateDTO.getDetailList();
        //映射字段
        List<OverseasProviderWarehouseEntity> list = BeanMapperUtils.copyList(OverseasProviderWarehouseEntity.class, detailList);
        // 数据处理
        handleData(list, mainId, updateDTO);
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
        return resolvePreferredByWarehouseId(warehouseId);
    }


    @Override
    public OverseasProviderWarehouseEntity getByWarehouseIdWithNotDisabled(String warehouseId) {
        return resolvePreferredByWarehouseId(warehouseId);
    }

    /**
     * 按 ERP 仓库 ID 解析启用中的三方仓映射。
     * <p>
     * 同一仓库可能残留多条映射（例如历史取消授权未解绑）。优先返回所属服务商
     * {@code auth_status=already} 的映射；若均未授权则回退最早一条启用映射，兼容无 API 对接仓。
     *
     * @param warehouseId ERP 仓库 ID
     * @return 优先的三方仓映射；无启用映射时返回 null
     */
    private OverseasProviderWarehouseEntity resolvePreferredByWarehouseId(String warehouseId) {
        if (CharSequenceUtil.isBlank(warehouseId)) {
            return null;
        }
        List<OverseasProviderWarehouseEntity> list = lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getWarehouseId, warehouseId)
                .eq(OverseasProviderWarehouseEntity::getDisabled, false)
                .orderByAsc(OverseasProviderWarehouseEntity::getId)
                .list();
        return pickPreferredProviderWarehouse(list);
    }

    /**
     * 在多条启用映射中挑选优先项：所属海外服务商已授权（already）优先。
     *
     * @param list 同一 warehouseId 下未禁用的映射列表（已按 id 升序）
     * @return 优先映射；列表为空返回 null
     */
    private OverseasProviderWarehouseEntity pickPreferredProviderWarehouse(List<OverseasProviderWarehouseEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        List<String> mainIds = list.stream()
                .map(OverseasProviderWarehouseEntity::getMainId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(mainIds)) {
            return list.get(0);
        }
        // listByIds 受 @TableLogic 过滤，已软删的服务商不会返回
        Set<String> alreadyAuthMainIds = overseasProviderService.listByIds(mainIds).stream()
                .filter(p -> AuthStatusEnum.ALREADY.getCode().equals(p.getAuthStatus()))
                .map(OverseasProviderEntity::getId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(alreadyAuthMainIds)) {
            return list.get(0);
        }
        return list.stream()
                .filter(w -> alreadyAuthMainIds.contains(w.getMainId()))
                .findFirst()
                .orElse(list.get(0));
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
                .in(CollectionUtils.isNotEmpty(warehouseCodeList),OverseasProviderWarehouseEntity::getPlatformWarehouseCode, warehouseCodeList)
                .list();
    }

    @Override
    public OverseasProviderEntity findPlatformByWarehouseId(String warehouseId) {
        // 与 getByWarehouseId 一致：优先未禁用且服务商已授权的映射，避免历史脏数据抢先命中
        OverseasProviderWarehouseEntity preferredWarehouse = resolvePreferredByWarehouseId(warehouseId);
        if (preferredWarehouse != null) {
            OverseasProviderEntity preferredProvider = overseasProviderService.getById(preferredWarehouse.getMainId());
            if (preferredProvider != null
                    && AuthStatusEnum.ALREADY.getCode().equals(preferredProvider.getAuthStatus())) {
                return preferredProvider;
            }
        }
        // 回退：兼容映射被禁用但仍有已授权服务商的历史数据
        List<OverseasProviderWarehouseEntity> entityList = listByWarehouseIds(Collections.singletonList(warehouseId));
        if (CollectionUtils.isEmpty(entityList)) {
            return null;
        }
        List<String> mainIds = entityList.stream().map(OverseasProviderWarehouseEntity::getMainId).distinct().collect(Collectors.toList());
        List<OverseasProviderEntity> overseasProviderEntityList = overseasProviderService.listByIds(mainIds).stream()
                .filter(v -> AuthStatusEnum.ALREADY.getCode().equals(v.getAuthStatus()))
                .collect(Collectors.toList());
        return CollectionUtils.isEmpty(overseasProviderEntityList) ? null : overseasProviderEntityList.get(0);
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
    private void handleData(List<OverseasProviderWarehouseEntity> list, String mainId, OverseasProviderDTO.UpdateDTO dto) {
        List<OverseasProviderWarehouseEntity> oldList = this.listByMainIds(Collections.singletonList(mainId));
        List<String> warehouseIds = list.stream().map(req -> req.getWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseDtoList = warehouseService.listWarehouseByIds(warehouseIds);
        List<OverseasProviderDTO.ListWithWarehouseDTO> allList = overseasProviderService.listAllMatch();
        allList = allList.stream().filter(v->!v.getId().equals(mainId)).collect(Collectors.toList());
        //查询绑定的仓库
        List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntities = this.listByWarehouseIds(warehouseIds);

        for (OverseasProviderWarehouseEntity detailEntity : list) {
            //仓库信息
            WarehouseDTO.UpdateDTO updateDTO = warehouseDtoList.stream().filter(req -> req.getId().equals(detailEntity.getWarehouseId())).distinct().findFirst().orElse(new WarehouseDTO.UpdateDTO());

            //如果启用，校验仓库是否绑定
            if (!detailEntity.getDisabled()) {
                if (CharSequenceUtil.isBlank(detailEntity.getWarehouseId())) {
                    throw new ServiceException(ApiError.WH_NOT_MATCHED);
                }

                //已绑定第三方供应商仓，一个仓库只能绑定一个第三方仓
                String sameWarehouse = list.stream()
                        .filter(req -> !req.getDisabled()
                                && CharSequenceUtil.equals(req.getWarehouseId(),detailEntity.getWarehouseId())
                                && !CharSequenceUtil.equals(req.getPlatformWarehouseCode(),detailEntity.getPlatformWarehouseCode()))
                        .map(OverseasProviderWarehouseEntity::getPlatformWarehouseName).findFirst().orElse(null);
                if (StringUtils.isNotBlank(sameWarehouse)) {
                    throw new ServiceException("系统仓库【{}】已映射【{}】-【{}】",detailEntity.getWarehouseName(),dto.getName(),sameWarehouse);
                }
                OverseasProviderDTO.ListWithWarehouseDTO other = allList.stream().filter(req -> !req.getDisabled()&& req.getWarehouseId().equals(detailEntity.getWarehouseId())).findFirst().orElse(null);
                if (Objects.nonNull(other)) {
                    throw new ServiceException("系统仓库【{}】已映射【{}】-【{}】",detailEntity.getWarehouseName(),other.getName(),other.getPlatformWarehouseName());
                }
                long count = overseasProviderWarehouseEntities.stream().filter(req -> !req.getDisabled()
                        && req.getWarehouseId().equals(detailEntity.getWarehouseId()) && !Objects.equals(req.getMainId(), mainId)).count();
                if (count > 1) {
                    throw new ServiceException(ApiError.WH_REPEAT_BINDING_THIRD_WAREHOUSE, updateDTO.getName());
                }
            }
            detailEntity.setMainId(mainId);
            detailEntity.setWarehouseCode(updateDTO.getKingdeeWarehouseCode());
            detailEntity.setWarehouseName(updateDTO.getName());

            //校验是否是修改，如果是就新增修改日志
            if (CharSequenceUtil.isNotBlank(detailEntity.getId())) {
                OverseasProviderWarehouseEntity old = oldList.stream().filter(obj -> obj.getId().equals(detailEntity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.FIRST_MILE_SHIPMENT_NOT_FOUND);
                }
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), mainId, "", String.format("【%s】", old.getPlatformWarehouseName()));
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean feignBind(OverseasProviderDTO.FeignDTO feignDTO) {
        //获取系统仓库获取绑定的第三方仓
        List<OverseasProviderWarehouseEntity> providerWarehouseEntityList = this.listByWarehouseIds(Arrays.asList(feignDTO.getWarehouseId()));
        if (CollectionUtils.isNotEmpty(providerWarehouseEntityList)) {
            List<OverseasProviderWarehouseEntity> needDisabledList = providerWarehouseEntityList.stream()
                    .filter(providerWarehouseEntity -> !providerWarehouseEntity.getDisabled() && !Objects.equals(providerWarehouseEntity.getId(), feignDTO.getOverseasProviderWarehouseId()))
                    .collect(Collectors.toList());
            if( CollectionUtils.isNotEmpty(needDisabledList)) {
                needDisabledList.forEach(v->{
                    v.setWarehouseId("");
                    v.setWarehouseName("");
                    v.setWarehouseCode("");
                    v.setDisabled(true);
                });
                this.updateBatchById(needDisabledList);
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
            throw new ServiceException(ApiError.COMMON_BINDING_ERROR);
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean isApiWarehouse(String destWarehouseId) {
        if (CharSequenceUtil.isBlank(destWarehouseId)) {
            return false;
        }
        // getByWarehouseId 已优先已授权服务商，避免历史取消授权映射导致误判为非 API 仓
        OverseasProviderWarehouseEntity entity = getByWarehouseId(destWarehouseId);
        if (null == entity || Boolean.TRUE.equals(entity.getDisabled())) {
            return false;
        }
        OverseasProviderEntity providerEntity = overseasProviderService.getById(entity.getMainId());
        return providerEntity != null
                && AuthStatusEnum.ALREADY.getCode().equals(providerEntity.getAuthStatus());
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
        dto.setPlatformSkuNoList(CharSequenceUtil.isNotBlank(platformSku) ? Collections.singletonList(platformSku) : Collections.singletonList(CharSequenceUtil.EMPTY));
        String platformProductId = shippedDTO.getPlatformProductId();
        dto.setPlatformSpuNoList(CharSequenceUtil.isNotBlank(platformProductId) ? Collections.singletonList(platformProductId) : Collections.singletonList(CharSequenceUtil.EMPTY));
        //店铺id
        List<String> shopIdList = getShopIdBySite(shippedDTO.getSite());
        if (CollUtil.isNotEmpty(shopIdList)){
            dto.setShopIdList(shopIdList);
        }
        dto.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
        dto.setIsExpire(false);
        List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOS = skuMappingFeign.listingInfoWithSkuMappingList(dto);
        if (CollUtil.isEmpty(listingInfoWithSkuMappingDTOS)){
            return Collections.emptyList();
        }
        ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = listingInfoWithSkuMappingDTOS.get(0);
        //erp映射的sku
        List<String> skuIds = null;
        List<String> childSkuIds;
        //根据是否组合品获取子件
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(Collections.singletonList(listingInfoWithSkuMappingDTO.getProductSkuId()));
        if (CollUtil.isNotEmpty(bomChildrenSkuList)){
            List<BomChildrenSkuDTO> bomChildren = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(listingInfoWithSkuMappingDTO.getProductSkuId())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).collect(Collectors.toList());
            childSkuIds = bomChildren.stream().map(BomChildrenSkuDTO::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        } else {
            childSkuIds = null;
        }
        //合并sku
        if (CollUtil.isEmpty(childSkuIds)){
            skuIds = Collections.singletonList(listingInfoWithSkuMappingDTO.getProductSkuId());
        }else {
            skuIds = Stream.concat(childSkuIds.stream(), Stream.of(listingInfoWithSkuMappingDTO.getProductSkuId())).distinct().collect(Collectors.toList());
        }
        //查询三方仓库存
        OverseasInventoryDTO.QueryDTO queryDTO = new OverseasInventoryDTO.QueryDTO();
        queryDTO.setSkuIds(skuIds);
        queryDTO.setPlatformWarehouseCodeList(warehouseCodeList);
        List<OverseasInventoryEntity> inventoryEntityList = overseasInventoryService.listBySkuAndWarehouseCode(queryDTO);
        if (CollUtil.isEmpty(inventoryEntityList)){
            return Collections.emptyList();
        }
        //先匹配产品skuId 匹配不到存在子件，取子件最小数量
        OverseasInventoryEntity entity = inventoryEntityList.stream().filter(e -> Objects.nonNull(e.getSellableQty()) && e.getSellableQty() > 0 && warehouseCodeList.contains(e.getWarehouseCode())
                && Objects.equals(listingInfoWithSkuMappingDTO.getProductSkuId(), e.getSkuId())).max(Comparator.comparing(OverseasInventoryEntity::getSellableQty)).orElse(null);
        if (Objects.nonNull(entity)){
            return Collections.singletonList(OverseasWarehouseConverter.INSTANCE.inventoryToShipmentDTO(entity));
        }
        if (CollUtil.isEmpty(childSkuIds)){
            return Collections.emptyList();
        }
        //判断子件是否都有库存， 存在无库存子件 则返回空
        for (String skuId : childSkuIds){
            OverseasInventoryEntity entity2 = inventoryEntityList.stream().filter(e -> Objects.nonNull(e.getSellableQty()) && e.getSellableQty() > 0
                    && warehouseCodeList.contains(e.getWarehouseCode())
                    && Objects.equals(skuId,e.getSkuId())).min(Comparator.comparing(OverseasInventoryEntity::getSellableQty)).orElse(null);
            if (Objects.isNull(entity2)){
                return Collections.emptyList();
            }
        }
        //都存在按照最小子件数量返回
        OverseasInventoryEntity entity3 = inventoryEntityList.stream().filter(e -> Objects.nonNull(e.getSellableQty()) && e.getSellableQty() > 0 && warehouseCodeList.contains(e.getWarehouseCode())
                && childSkuIds.contains(e.getSkuId())).min(Comparator.comparing(OverseasInventoryEntity::getSellableQty)).orElse(null);
        if (Objects.isNull(entity3)){
            return Collections.emptyList();
        }
        return Collections.singletonList(OverseasWarehouseConverter.INSTANCE.inventoryToShipmentDTO(entity3));
    }

    @Override
    public BaseResultDTO.AddDTO addThirdWarehouse(ThirdWarehouseDTO.AddDTO addDTO) {
        OverseasProviderEntity overseasProviderEntity = overseasProviderService.getByPlatformCodeAndShortName(addDTO.getSysType(),addDTO.getThirdShortName());
        if( Objects.isNull(overseasProviderEntity)){
            throw new ServiceException("海外物流商不存在");
        }
        OverseasProviderWarehouseEntity exist = this.getByPlatform(overseasProviderEntity.getId(),addDTO.getCode());
        if(Objects.nonNull(exist)){
            throw new ServiceException("海外仓库编号已存在");
        }
        OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = new OverseasProviderWarehouseEntity();
        overseasProviderWarehouseEntity.setPlatformWarehouseCode(addDTO.getCode());
        overseasProviderWarehouseEntity.setPlatformWarehouseName(addDTO.getName());
        overseasProviderWarehouseEntity.setMainId(overseasProviderEntity.getId());
        if(!save(overseasProviderWarehouseEntity)){
            throw new ServiceException("保存失败");
        }
        return new BaseResultDTO.AddDTO(overseasProviderWarehouseEntity.getId(),overseasProviderWarehouseEntity.getPlatformWarehouseCode());
    }

    @Override
    public List<OverseasProviderWarehouseEntity> getOverseasWarehouseListByPlatformCode(String platform) {
        String mainId = overseasProviderService.getByPlatformCode(platform).getId();
        return lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getMainId, mainId)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int[] syncFromWego(String mainId, JSONArray wegoWarehouseList) {
        if (CharSequenceUtil.isBlank(mainId)) {
            return new int[]{0, 0, 0};
        }
        JSONArray apiList = Objects.isNull(wegoWarehouseList) ? new JSONArray() : wegoWarehouseList;

        List<OverseasProviderWarehouseEntity> existingList = lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getMainId, mainId)
                .list();
        Set<String> existingCodeSet = existingList.stream()
                .map(OverseasProviderWarehouseEntity::getPlatformWarehouseCode)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());

        Set<String> apiWarehouseCodes = new HashSet<>();
        List<OverseasProviderWarehouseEntity> toInsert = new ArrayList<>();
        for (int i = 0; i < apiList.size(); i++) {
            JSONObject wh = apiList.getJSONObject(i);
            if (Objects.isNull(wh)) {
                continue;
            }
            String warehouseCode = wh.getString("warehouseCode");
            if (CharSequenceUtil.isBlank(warehouseCode)) {
                log.warn("[WEGO仓库同步] mainId={} 跳过 warehouseCode 为空的记录: {}", mainId, wh);
                continue;
            }
            apiWarehouseCodes.add(warehouseCode);
            // DB 已存在的记录不做任何处理，仅参与后续过期判定
            if (existingCodeSet.contains(warehouseCode)) {
                continue;
            }
            AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(wh.getString("warehouseRegion"));
            OverseasProviderWarehouseEntity insert = new OverseasProviderWarehouseEntity();
            insert.setMainId(mainId);
            insert.setPlatformWarehouseCode(warehouseCode);
            insert.setPlatformWarehouseName(wh.getString("warehouseName"));
            insert.setCountry(wh.getString("warehouseRegion"));
            insert.setCountryName(marketPlaceEnum != null ? marketPlaceEnum.getName() : wh.getString("warehouseRegion"));
            insert.setDisabled(Boolean.TRUE);
            toInsert.add(insert);
        }

        // 处理「DB有、API无」的过期记录：warehouse_id 为空软删，否则禁用
        List<String> toDeleteIds = new ArrayList<>();
        List<OverseasProviderWarehouseEntity> toDisable = new ArrayList<>();
        for (OverseasProviderWarehouseEntity exist : existingList) {
            String code = exist.getPlatformWarehouseCode();
            if (CharSequenceUtil.isBlank(code) || apiWarehouseCodes.contains(code)) {
                continue;
            }
            if (CharSequenceUtil.isBlank(exist.getWarehouseId())) {
                toDeleteIds.add(exist.getId());
            } else if (!Boolean.TRUE.equals(exist.getDisabled())) {
                OverseasProviderWarehouseEntity update = new OverseasProviderWarehouseEntity();
                update.setId(exist.getId());
                update.setDisabled(Boolean.TRUE);
                toDisable.add(update);
            }
        }

        if (CollUtil.isNotEmpty(toInsert)) {
            this.saveBatch(toInsert);
        }
        if (CollUtil.isNotEmpty(toDeleteIds)) {
            this.removeByIds(toDeleteIds);
        }
        if (CollUtil.isNotEmpty(toDisable)) {
            this.updateBatchById(toDisable);
        }
        return new int[]{toInsert.size(), toDeleteIds.size(), toDisable.size()};
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int[] syncFromAiya(String mainId, JSONArray aiyaWarehouseList) {
        if (CharSequenceUtil.isBlank(mainId)) {
            return new int[]{0, 0, 0};
        }
        JSONArray apiList = Objects.isNull(aiyaWarehouseList) ? new JSONArray() : aiyaWarehouseList;

        List<OverseasProviderWarehouseEntity> existingList = lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getMainId, mainId)
                .list();
        Set<String> existingCodeSet = existingList.stream()
                .map(OverseasProviderWarehouseEntity::getPlatformWarehouseCode)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());

        Set<String> apiWarehouseCodes = new HashSet<>();
        List<OverseasProviderWarehouseEntity> toInsert = new ArrayList<>();
        for (int i = 0; i < apiList.size(); i++) {
            JSONObject wh = apiList.getJSONObject(i);
            if (Objects.isNull(wh)) {
                continue;
            }
            // 爱亚仓库编码字段：warehouseCode（对应海外仓仓库代码）
            String warehouseCode = wh.getString("warehouseCode");
            if (CharSequenceUtil.isBlank(warehouseCode)) {
                log.warn("[爱亚仓库同步] mainId={} 跳过 warehouseCode 为空的记录: {}", mainId, wh);
                continue;
            }
            apiWarehouseCodes.add(warehouseCode);
            // 数大臣已存在：忽略（需求a），仅参与后续过期判定
            if (existingCodeSet.contains(warehouseCode)) {
                continue;
            }
            // 数大臣未存在：新增存储（需求b），默认禁用等待人工映射
            String country = wh.getString("country");
            AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(country);
            OverseasProviderWarehouseEntity insert = new OverseasProviderWarehouseEntity();
            insert.setMainId(mainId);
            insert.setPlatformWarehouseCode(warehouseCode);
            // 爱亚仓库名称字段：warehouseDescription（对应海外仓仓库名称）
            insert.setPlatformWarehouseName(wh.getString("warehouseDescription"));
            insert.setCountry(country);
            insert.setCountryName(marketPlaceEnum != null ? marketPlaceEnum.getName() : country);
            insert.setDisabled(Boolean.TRUE);
            toInsert.add(insert);
        }

        // 处理「DB有、API无」的过期记录：warehouse_id 为空（未映射）软删（需求c），否则禁用映射关系（需求d）
        List<String> toDeleteIds = new ArrayList<>();
        List<OverseasProviderWarehouseEntity> toDisable = new ArrayList<>();
        for (OverseasProviderWarehouseEntity exist : existingList) {
            String code = exist.getPlatformWarehouseCode();
            if (CharSequenceUtil.isBlank(code) || apiWarehouseCodes.contains(code)) {
                continue;
            }
            if (CharSequenceUtil.isBlank(exist.getWarehouseId())) {
                toDeleteIds.add(exist.getId());
            } else if (!Boolean.TRUE.equals(exist.getDisabled())) {
                OverseasProviderWarehouseEntity update = new OverseasProviderWarehouseEntity();
                update.setId(exist.getId());
                update.setDisabled(Boolean.TRUE);
                toDisable.add(update);
            }
        }

        if (CollUtil.isNotEmpty(toInsert)) {
            this.saveBatch(toInsert);
        }
        if (CollUtil.isNotEmpty(toDeleteIds)) {
            this.removeByIds(toDeleteIds);
        }
        if (CollUtil.isNotEmpty(toDisable)) {
            this.updateBatchById(toDisable);
        }
        return new int[]{toInsert.size(), toDeleteIds.size(), toDisable.size()};
    }

    private List<String> getShopIdBySite(String site) {
        if (CharSequenceUtil.isBlank(site)){
            return Collections.emptyList();
        }
        String code = EnumMessage.getNameByCode(ShopSiteEnum.class, site);
        if (CharSequenceUtil.isBlank(code)){
            return Collections.emptyList();
        }
        ShopInfoDTO.ListParamDTO dto = new ShopInfoDTO.ListParamDTO();
        dto.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        dto.setDictPlatform(PlatformDictEnum.SHOPIFY.getCode());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listByParams(dto);
        if (CollUtil.isEmpty(shopInfoEntityList)){
            return Collections.emptyList();
        }
        return shopInfoEntityList.stream().filter(e -> e.getDomain().equalsIgnoreCase(code)).map(ShopInfoEntity::getId).distinct().collect(Collectors.toList());
    }
}
