package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseMappingDTO;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WarehouseMappingFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.wms.feign.WmsVirtualWarehouseFeign;
import com.erp.server.oms.convert.B2cOrderConsumerConverter;
import com.erp.server.oms.mapper.SoB2cDetailMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单明细表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cDetailServiceImpl extends SuperServiceImpl<SoB2cDetailMapper, SoB2cDetailEntity> implements SoB2cDetailService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SkuMappingService skuMappingService;

    @Lazy
    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoB2cRefService soB2cRefService;

    @Resource
    private WarehouseMappingFeign warehouseMappingFeign;

    @Resource
    private SoB2cErrorService soB2cErrorService;

    @Resource
    @Lazy
    private SoB2cDetailService service;

    @Resource
    private WmsVirtualWarehouseFeign wmsVirtualWarehouseFeign;


    @Override
    public Boolean add(SoB2cDTO.AddDTO addDTO, String mainId) {
        List<SoB2cDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        if (CollectionUtils.isEmpty(detailList)) {
           throw new ServiceException(ApiError.ERROR_1040, SourceTypeEnum.SO_B2C.getName());
        }
        List<SoB2cDetailEntity> list = BeanMapperUtils.copyList(SoB2cDetailEntity.class, detailList);

        //主表信息
        SoB2cEntity soB2cEntity = soB2cService.getById(mainId);
        if (ObjectUtils.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //处理明细中的数据id
        handleDetailList(list,soB2cEntity,Boolean.TRUE);
        //批量新增
        boolean flag = this.saveBatch(list);
        //新增拆分订单关联关系
        addSoB2cRef(addDTO,list,soB2cEntity);
        return flag;
    }

    @Override
    public Boolean update(List<SoB2cDetailDTO.UpdateDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_1040, SourceTypeEnum.SO_B2C.getName());
        }
        //原明细数据
        List<SoB2cDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoB2cDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_B2C.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        List<SoB2cDetailEntity> list = BeanMapperUtils.copyList(SoB2cDetailEntity.class, detailList);
        //主表信息
        SoB2cEntity soB2cEntity = soB2cService.getById(mainId);
        if (ObjectUtils.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //处理明细中的数据id
        handleDetailList(list,soB2cEntity,Boolean.FALSE);
        return service.saveOrUpdateBatch(list);
    }

    @Override
    public List<SoB2cDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cDetailEntity::getIsDeleted, false).eq(SoB2cDetailEntity::getMainId,mainId).list();
    }

    @Override
    public List<SoB2cDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SoB2cDetailEntity::getMainId,mainIds).list();
    }

    @Override
    public Boolean updateWarehouseIdByMainId(String mainId, String warehouseId,Boolean isCover) {

        List<SoB2cDetailEntity> detailList = this.listByMainId(mainId);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        List<String> detailIdList = detailList.stream().map(SoB2cDetailEntity::getId).collect(Collectors.toList());
        if (!isCover) {
            detailList = detailList.stream().filter(obj -> StrUtil.isBlank(obj.getWarehouseId())).collect(Collectors.toList());
            detailIdList = detailList.stream().map(SoB2cDetailEntity::getId).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(detailIdList)) {
            return Boolean.TRUE;
        }
        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        WarehouseDTO.UpdateDTO updateDTO = warehouseList.get(0);
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(updateDTO.getOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_NOT_EXIST_ORG,updateDTO.getName());
        }
        //SKU对照表信息
        List<SkuMappingDTO.ListSkuParamDTO> listParamList = detailList.stream().map(obj -> new SkuMappingDTO.ListSkuParamDTO(obj.getSkuNo(), warehouseId,null)).collect(Collectors.toList());
        ValidList<SkuMappingDTO.ListSkuParamDTO> listSkuParamList = new ValidList<>();
        listSkuParamList.setList(listParamList);
        List<SkuMappingDTO.ListSkuDTO> SkuMappingList = skuMappingService.listBySkuNoList(listSkuParamList);

        BaseIdDTO.CodeDTO companyDTO = accountingCompanyList.get(0);
        for (SoB2cDetailEntity detailEntity :detailList) {
            detailEntity.setWarehouseId(updateDTO.getId());
            detailEntity.setWarehouseName(updateDTO.getName());

            if (ObjectUtils.isNotEmpty(companyDTO)) {
                detailEntity.setWarehouseOrgId(updateDTO.getOrgId());
                detailEntity.setWarehouseOrgName(companyDTO.getName());
            }
            //库存SKU
            SkuMappingDTO.ListSkuDTO warehouseListSkuDTO = SkuMappingList.stream().filter(obj -> obj.getProductSkuId().equals(detailEntity.getSkuId()) && obj.getWarehouseId().equals(detailEntity.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(warehouseListSkuDTO)) {
                detailEntity.setWarehouseSkuNo(StrUtil.isBlank(warehouseListSkuDTO.getWarehouseSkuNo()) ? "" : warehouseListSkuDTO.getWarehouseSkuNo() );
            } else {
                detailEntity.setWarehouseSkuNo("");
            }
            detailEntity.setIsMatchWarehouseRule(Boolean.TRUE);
        }

       return this.saveOrUpdateBatch(detailList);
    }

    @Override
    public Boolean updateWarehouseId(SoB2cEntity entity,List<SoB2cDTO.SaveSoB2cDistributionDetailDTO> saveDetailList,Boolean isCover) {
        //明细数据
        List<String> ids = saveDetailList.stream().map(SoB2cDTO.SaveSoB2cDistributionDetailDTO::getDetailId).collect(Collectors.toList());
        List<SoB2cDetailEntity> detailList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        List<String> detailIdList = detailList.stream().map(SoB2cDetailEntity::getId).collect(Collectors.toList());
        if (!isCover) {
            detailList = detailList.stream().filter(obj -> StrUtil.isBlank(obj.getWarehouseId())).collect(Collectors.toList());
            detailIdList = detailList.stream().map(SoB2cDetailEntity::getId).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(detailIdList)) {
            return Boolean.TRUE;
        }
        //仓库信息
        List<String> warehouseIdList = saveDetailList.stream().map(SoB2cDTO.SaveSoB2cDistributionDetailDTO::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);

        //组织信息
        List<String> orgIdList = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);

        //SKU对照表信息
        List<SoB2cDetailEntity> finalDetailList = detailList;
        List<SkuMappingDTO.ListSkuParamDTO> listParamList = saveDetailList.stream().map(obj -> new SkuMappingDTO.ListSkuParamDTO(finalDetailList.stream().filter(e -> StrUtil.equals(e.getId(),obj.getDetailId())).map(SoB2cDetailEntity::getSkuNo).findFirst().orElse(""), obj.getWarehouseId(),null)).collect(Collectors.toList());
        ValidList<SkuMappingDTO.ListSkuParamDTO> listSkuParamList = new ValidList<>();
        listSkuParamList.setList(listParamList);
        List<SkuMappingDTO.ListSkuDTO> SkuMappingList = skuMappingService.listBySkuNoList(listSkuParamList);

        //虚拟仓库查询
        VirtualWarehouseChannelDTO.PlatformDTO platformDTO = new VirtualWarehouseChannelDTO.PlatformDTO();
        platformDTO.setDictPlatform(entity.getDictPlatform());
        platformDTO.setRelationId(entity.getShopId());
        platformDTO.setWarehouseIdList(warehouseIdList);
        List<VirtualWarehouseRelationEntity> virtualWarehouseList = wmsVirtualWarehouseFeign.getVirtualWarehouse(platformDTO);

        for (SoB2cDetailEntity detailEntity :detailList) {
            //仓库
            String warehouseId = saveDetailList.stream().filter(obj -> StrUtil.equals(obj.getDetailId(), detailEntity.getId()))
                    .map(SoB2cDTO.SaveSoB2cDistributionDetailDTO::getWarehouseId).findFirst().orElse("");
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), warehouseId)).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(updateDTO)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            //组织
            BaseIdDTO.CodeDTO companyDTO = accountingCompanyList.stream().filter(obj -> StrUtil.equals(obj.getId(), updateDTO.getOrgId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(companyDTO)) {
                throw new ServiceException(ApiError.ERROR_WAREHOUSE_NOT_EXIST_ORG,updateDTO.getName());
            }

            detailEntity.setWarehouseId(updateDTO.getId());
            detailEntity.setWarehouseName(updateDTO.getName());

            if (ObjectUtils.isNotEmpty(companyDTO)) {
                detailEntity.setWarehouseOrgId(updateDTO.getOrgId());
                detailEntity.setWarehouseOrgName(companyDTO.getName());
            }

            //虚拟仓信息
            String virtualWarehouseId = virtualWarehouseList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), detailEntity.getWarehouseId()))
                    .map(VirtualWarehouseRelationEntity::getVirtualWarehouseId).findFirst().orElse("");
            detailEntity.setVirtualWarehouseId(virtualWarehouseId);

            //库存SKU
            SkuMappingDTO.ListSkuDTO warehouseListSkuDTO = SkuMappingList.stream().filter(obj -> obj.getProductSkuId().equals(detailEntity.getSkuId()) && obj.getWarehouseId().equals(detailEntity.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(warehouseListSkuDTO)) {
                detailEntity.setWarehouseSkuNo(StrUtil.isBlank(warehouseListSkuDTO.getWarehouseSkuNo()) ? "" : warehouseListSkuDTO.getWarehouseSkuNo() );
            } else {
                detailEntity.setWarehouseSkuNo("");
            }
            detailEntity.setIsMatchWarehouseRule(Boolean.TRUE);
        }

        return this.saveOrUpdateBatch(detailList);
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public Boolean updateWarehouse(List<SoB2cDetailEntity> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //主表信息
        List<String> mainIdList = detailList.stream().map(SoB2cDetailEntity::getMainId).collect(Collectors.toList());
        List<SoB2cEntity> soB2cList = soB2cService.listByIds(mainIdList);
        if (CollectionUtils.isEmpty(soB2cList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        List<String> warehouseIdList = detailList.stream().map(SoB2cDetailEntity::getWarehouseId).collect(Collectors.toList());
        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        List<String> orgIdList = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_NOT_EXIST_ORG,warehouseList.stream().map(WarehouseDTO.UpdateDTO::getName).collect(Collectors.joining(",")));
        }
        //SKU对照表信息
        List<SkuMappingDTO.ListSkuParamDTO> listParamList = detailList.stream().map(obj -> new SkuMappingDTO.ListSkuParamDTO(obj.getSkuNo(), obj.getWarehouseId(),soB2cList.stream().filter(e -> e.getId().equals(obj.getMainId())).findFirst().flatMap(e ->Optional.ofNullable(e.getDictPlatform())).orElse(""))).collect(Collectors.toList());
        ValidList<SkuMappingDTO.ListSkuParamDTO> listSkuParamList = new ValidList<>();
        listSkuParamList.setList(listParamList);
        List<SkuMappingDTO.ListSkuDTO> skuMappingList = skuMappingService.listBySkuNoList(listSkuParamList);

        for (SoB2cDetailEntity entity : detailList) {
            WarehouseDTO.UpdateDTO warehouseDTO = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(warehouseDTO)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            entity.setWarehouseName(warehouseDTO.getName());
            entity.setWarehouseOrgId(warehouseDTO.getOrgId());
            BaseIdDTO.CodeDTO codeDTO = accountingCompanyList.stream().filter(obj -> obj.getId().equals(warehouseDTO.getOrgId())).findFirst().orElse(null);
            entity.setWarehouseOrgName(codeDTO.getName());

            //库存SKU
            SkuMappingDTO.ListSkuDTO warehouseListSkuDTO = skuMappingList.stream().filter(obj -> StrUtil.equals(obj.getProductSkuId(),entity.getSkuId()) && StrUtil.equals(obj.getWarehouseId(),entity.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(warehouseListSkuDTO)) {
                entity.setWarehouseSkuNo(warehouseListSkuDTO.getWarehouseSkuNo());
            }
        }
        return this.updateBatchById(detailList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SoB2cDetailEntity> saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, Map<String, List<ListingInfoWithSkuMappingDTO>> listingInfoWithSkuMappingDTOMap, ShopInfoEntity shopInfo, List<SkuInfoSimpleVO> skuList) {
        // 订单明细
        List<SoB2cDetailEntity> oldDetailEntityList = this.listByMainId(mainEntity.getId());
        // 来源为空
        if(CollectionUtils.isEmpty(dto.getDetails())){
            if (CollectionUtils.isEmpty(oldDetailEntityList)){
                // 新建空
                SoB2cDetailEntity detailEntity = B2cOrderConsumerConverter.INSTANCE.convertNewDetail(null, mainEntity.getId(),"", "","", "");
                if(!this.save(detailEntity)){
                    throw new ServiceException("[SoB2cDetailEntity] 保存失败");
                }
                return Collections.singletonList(detailEntity);
            }
            return oldDetailEntityList;
        }
        // 存在拆分忽略更新
        boolean existSplit = oldDetailEntityList.stream().anyMatch(e -> StringUtils.isNotBlank(e.getSplitDetailId()));
        if (existSplit){
            return oldDetailEntityList;
        }
        // 是否保留历史数量和sku信息：自发货订单已发货保留历史数量
        boolean keepHistory = !mainEntity.hasPlatformWarehouseOrder() && SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equalsIgnoreCase(mainEntity.getBillStatus());

        // 来源不为空
        // 历史map
        Map<String, SoB2cDetailEntity> oldDetailMap = oldDetailEntityList.stream()
                .filter(e -> StringUtils.isNotEmpty(e.getSourceDetailId()))
                .collect(Collectors.toMap(
                        SoB2cDetailEntity::getSourceDetailId,
                        Function.identity(),
                        (existing, replacement) -> existing // 保留现有的值，丢弃重复的
                ));

        //查询速卖通仓库名称是否映射ERP仓库
        List<WarehouseMappingDTO.MappingViewDTO> mappingViewDTOS = new ArrayList<>();
        if (LogisticsPlatformEnum.ALI_EXPRESS.getCode().equals(mainEntity.getDictPlatform())) {
            mappingViewDTOS = warehouseMappingFeign.listMappingViewByDictPlatform(mainEntity.getDictPlatform());
        }
        //已发货
        String shipped = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        String billStatus = mainEntity.getBillStatus();
        Boolean isShipped = shipped.equals(billStatus);
        // 新增或更新列表
        List<WarehouseMappingDTO.MappingViewDTO> finalMappingViewDTOS = mappingViewDTOS;
        List<SoB2cDetailEntity> saveOrUpdateList = dto.getDetails().stream().map(detailDTO -> {
            // 历史记录
            SoB2cDetailEntity oldEntity = oldDetailMap.get(detailDTO.getSourceDetailId());
            List<ListingInfoWithSkuMappingDTO> mappingDTOList;
            if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dto.getDictPlatform()) && StringUtils.isBlank(detailDTO.getPlatformSkuNo()) ){
                // 速卖通明细SKU为空按platformSpuNo匹配
                mappingDTOList = listingInfoWithSkuMappingDTOMap.values()
                        .stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());;
            } else {
                // 映射关系
                mappingDTOList = listingInfoWithSkuMappingDTOMap.get(detailDTO.getPlatformSkuNo());
            }

            // 检查和获取映射关系
            ListingInfoWithSkuMappingDTO mappingDTO = skuMappingService.checkAndMappingDTO(mappingDTOList, detailDTO.getPlatformSpuNo(), mainEntity.getDictPlatform());

            String skuId = null == oldEntity ? "" : oldEntity.getSkuId();
            String skuNO= null == oldEntity ? "" : oldEntity.getSkuNo();
            String imageUrl= null == oldEntity ? "" : oldEntity.getImageUrl();
            String platformSpuNo = null == oldEntity ? detailDTO.getPlatformSpuNo() : oldEntity.getPlatformSpuNo();
            // 历史不为空不更新
            if(null != mappingDTO && StringUtils.isBlank(skuNO) && StringUtils.isBlank(skuId)){
                skuId = mappingDTO.checkAndGetProductSkuId();
                skuNO = mappingDTO.checkAndGetProductSkuNo();
                imageUrl = mappingDTO.checkAndGetProductImageUrl();
            }
            if (null != mappingDTO && StringUtils.isBlank(platformSpuNo)){
                platformSpuNo = mappingDTO.getPlatformSpuNo();
            }

            SoB2cDetailEntity saveOrUpdateEntity;
            if (null != oldEntity) {
                // 更新指定内容
                saveOrUpdateEntity = B2cOrderConsumerConverter.INSTANCE.convertUpdateDetail(oldEntity, detailDTO, skuId, skuNO, imageUrl, platformSpuNo, keepHistory);
            } else {
                // 新记录
                saveOrUpdateEntity = B2cOrderConsumerConverter.INSTANCE.convertNewDetail(detailDTO, mainEntity.getId(), skuId, skuNO, imageUrl, platformSpuNo);
            }

            if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(mainEntity.getDictPlatform()) && mainEntity.hasPlatformWarehouseOrder() && isShipped) {
                //查询映射的仓库信息
                WarehouseMappingDTO.MappingViewDTO mappingViewDTO = finalMappingViewDTOS.stream()
                        .filter(req -> StringUtils.isNotBlank(detailDTO.getWarehouseName()) && detailDTO.getWarehouseName().equals(req.getThirdWarehouseName()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isNotEmpty(mappingViewDTO)) {
                    saveOrUpdateEntity.setWarehouseId(mappingViewDTO.getWarehouseId());
                    saveOrUpdateEntity.setWarehouseName(mappingViewDTO.getWarehouseName());
                    saveOrUpdateEntity.setWarehouseOrgId(mappingViewDTO.getWarehouseOrgId());
                    saveOrUpdateEntity.setWarehouseOrgName(mappingViewDTO.getWarehouseOrgName());
                } else {
                    saveOrUpdateEntity.setWarehouseId("");
                    saveOrUpdateEntity.setWarehouseName("");
                    saveOrUpdateEntity.setWarehouseOrgId("");
                    saveOrUpdateEntity.setWarehouseOrgName("");
                }
            }
            return saveOrUpdateEntity;
        }).collect(Collectors.toList());

        // 其他处理
        consumerHandleDetailList(saveOrUpdateList, mainEntity, skuList);

        // 批量保存和更新
         if (!this.saveOrUpdateBatch(saveOrUpdateList)){
            throw new ServiceException(" [SoB2cDetailEntity] 订单明细批量更新或保存失败");
        }
        return saveOrUpdateList;
    }

    @Override
    public List<SoB2cDetailEntity> listContainDeleted(List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        return baseMapper.listContainDeleted(ids);
    }

    @Override
    public void updateContainDeleted(List<String> revertDetailIds) {
        if(CollectionUtils.isEmpty(revertDetailIds)){
            return ;
        }
        baseMapper.updateContainDeleted(revertDetailIds);
    }

    @Override
    public List<SoB2cDetailEntity> listBySplitId(List<String> detailIds) {
        if(CollectionUtils.isEmpty(detailIds)){
            return new ArrayList<>();
        }
        return lambdaQuery().in(SoB2cDetailEntity::getSplitDetailId, detailIds).list();
    }

    /**
     * 消费明细处理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumerHandleDetailList(List<SoB2cDetailEntity> list, SoB2cEntity mainEntity, List<SkuInfoSimpleVO> skuList) {

        for (SoB2cDetailEntity detailEntity :list) {
            //产品信息
            SkuInfoSimpleVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst()
                    .orElse(null);

            detailEntity.setCurrency(mainEntity.getCurrency());
            detailEntity.setExchangeRate(mainEntity.getExchangeRate());


            //建议售价
            BigDecimal advicePrice = null == skuVO ? BigDecimal.ZERO : skuVO.getRetailPrice();
            detailEntity.setAdvicePrice(advicePrice);
            //含税单价
            BigDecimal costPrice = null == skuVO ? BigDecimal.ZERO : ObjectUtils.isEmpty(skuVO.getActualTaxCost()) ? skuVO.getTargetTaxCost() : skuVO.getActualTaxCost();
            detailEntity.setTaxCost(costPrice);
            detailEntity.setAmount(MathUtil.multiply(detailEntity.getPrice(),detailEntity.getQty()));
            // 产品图片
            detailEntity.setImageUrl(null == skuVO ? "" : skuVO.getSkuImagesUrl());
        }
    }

    @Override
    public Map<String, List<ListingInfoWithSkuMappingDTO>> mapListingByPlatformSkuId(List<String> platformSkuIdList, List<String> platformSpuList, String dictPlatform, String shopId, LocalDateTime platformOrderCreateTime, Boolean isExpire) {
        if (CollectionUtils.isEmpty(platformSkuIdList)) {
            return Collections.emptyMap();
        }
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(dictPlatform);
        paramDTO.setShopIdList(Collections.singletonList(shopId));
        paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
        paramDTO.setPlatformSpuNoList(platformSpuList);
        paramDTO.setPlatformSkuIdList(platformSkuIdList);
        paramDTO.setMatchResult(true);
        paramDTO.setLastExpireDate(platformOrderCreateTime);
        paramDTO.setIsExpire(isExpire);
        // 查询ListingInfo和skuMapping的关系
        List<ListingInfoWithSkuMappingDTO> listDto = skuMappingService.findListDto(paramDTO);
        listDto = listDto.stream().filter(v->StringUtils.isNotBlank(v.getProductSkuId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(listDto)){
            return Collections.emptyMap();
        }
        return listDto.stream()
                .collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getPlatformSkuId));
    }

    @Override
    public Boolean updateIsMatchWarehouseRule(String mainId,List<String> detailIdList) {
        if (CollectionUtils.isEmpty(detailIdList)) {
            return Boolean.TRUE;
        }
        return  lambdaUpdate()
                .in(SoB2cDetailEntity::getId,detailIdList)
                .set(SoB2cDetailEntity::getIsMatchWarehouseRule,Boolean.FALSE)
                .update();
    }

    @Override
    public List<SoB2cDetailDTO.OutstockDTO> listOutstockByMainId(String mainId) {
        List<SoB2cDetailDTO.OutstockDTO> outstockList=baseMapper.listOutstockByMainId(mainId);
        List<String> parentSkuIdList = outstockList.stream().map(SoB2cDetailDTO.OutstockDTO::getSkuId).collect(Collectors.toList());
        String combinationType= BomTypeEnum.COMBINATION.getType();
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(parentSkuIdList);
        if(CollectionUtils.isEmpty(bomChildrenSkuList)){
            return outstockList;
        }else{
            List<SoB2cDetailDTO.OutstockDTO> hasBomOutstockList=new ArrayList<>(bomChildrenSkuList.size());
            //表示有Bom
            for(SoB2cDetailDTO.OutstockDTO item:outstockList){
                //相当于父级
                String skuId = item.getSkuId();
                //相当于父级
                String skuNo = item.getSkuNo();
                //数量
                Integer qty = item.getQty();
                //套装的bom
                List<BomChildrenSkuDTO> bomChildrenSkuDTOS = bomChildrenSkuList.stream()
                        .filter(req -> req.getParentSkuId().equals(skuId)
                                && combinationType.equals(req.getType())
                        ).collect(Collectors.toList());

               if(CollectionUtils.isNotEmpty(bomChildrenSkuDTOS)){
                   //该sk是套装Bom
                   for(BomChildrenSkuDTO bomSku:bomChildrenSkuDTOS){
                       SoB2cDetailDTO.OutstockDTO  outstock=new SoB2cDetailDTO.OutstockDTO();
                       outstock.setSkuId(bomSku.getSkuId());
                       outstock.setSkuNo(bomSku.getSkuNo());
                       Integer quantity=bomSku.getQuantity();
                       outstock.setQty(qty*quantity);
                       outstock.setMianId(item.getMianId());
                       outstock.setWarehouseId(item.getWarehouseId());
                       outstock.setWarehouseName(item.getWarehouseName());
                       outstock.setWarehouseOrgId(item.getWarehouseOrgId());
                       outstock.setWarehouseOrgName(item.getWarehouseOrgName());
                       outstock.setWarehouseLocation(item.getWarehouseLocation());
                       outstock.setCurrency(item.getCurrency());
                       outstock.setExchangeRate(item.getExchangeRate());
                       outstock.setRemark(item.getRemark());
                       outstock.setSoDetailId(item.getSoDetailId());
                       hasBomOutstockList.add(outstock);
                   }
               }else{
                   //表示没有套装bom
                   hasBomOutstockList.add(item);
               }
            }
            return hasBomOutstockList;
        }

    }

    @Override
    public Boolean updateWarehouseByMapping(WarehouseMappingDTO.MappingViewDTO viewDTO,String soId,List<String> skuIdList) {
        if (ObjectUtil.isNotEmpty(viewDTO) && StringUtils.isNotBlank(soId) && CollectionUtils.isNotEmpty(skuIdList)) {
            lambdaUpdate()
                    .eq(SoB2cDetailEntity::getMainId, soId)
                    .in(SoB2cDetailEntity::getSkuId,skuIdList)
                    .set(SoB2cDetailEntity::getWarehouseId, viewDTO.getWarehouseId())
                    .set(SoB2cDetailEntity::getWarehouseName, viewDTO.getWarehouseName())
                    .set(SoB2cDetailEntity::getWarehouseOrgId, viewDTO.getWarehouseOrgId())
                    .set(SoB2cDetailEntity::getWarehouseOrgName, viewDTO.getWarehouseOrgName())
                    .update();
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public List<SoB2cDetailDTO.WaitDeliveryQtyDTO> listWaitDeliveryQty(SoB2cDetailDTO.WaitDeliveryParamDTO paramDTO) {
        if (Objects.isNull(paramDTO) || CollectionUtils.isEmpty(paramDTO.getDetailIdList()) || CollectionUtils.isEmpty(paramDTO.getSkuIdList())
        || CollectionUtils.isEmpty(paramDTO.getWarehouseIdList())){
            return Collections.emptyList();
        }
        return baseMapper.listWaitDeliveryQty(paramDTO);
    }

    @Override
    public Boolean updatePlatformPackageIdByMainId(String platformPackageId, String mainId) {
        if (StringUtils.isBlank(mainId)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().set(SoB2cDetailEntity::getPlatformPackageId, platformPackageId).eq(SoB2cDetailEntity::getMainId, mainId).update();
    }

    @Override
    public void updateSignShippedByDetailId(List<String> detailIdList) {
        if(CollectionUtils.isEmpty(detailIdList)){
            return;
        }
        this.lambdaUpdate().in(SoB2cDetailEntity::getId, detailIdList).set(SoB2cDetailEntity::getIsSignShipped, true).update();
    }

    @Override
    public List<SoB2cDetailDTO.PropertyDTO> handlePropertyDTOList(SkuVO.PropertyDTO skuPropertyDTO) {
        if(Objects.isNull(skuPropertyDTO)){
            return new ArrayList<>();
        }
        List<SoB2cDetailDTO.PropertyDTO> list = new ArrayList<>();
        if(skuPropertyDTO.getIsElectric()){
            list.add(new SoB2cDetailDTO.PropertyDTO("电",skuPropertyDTO.getElectricName()));
        }
        if(skuPropertyDTO.getIsMagnetism()){
            list.add(new SoB2cDetailDTO.PropertyDTO("磁",skuPropertyDTO.getMagnetismName()));
        }
        if(skuPropertyDTO.getIsLiquid()){
            list.add(new SoB2cDetailDTO.PropertyDTO("液",skuPropertyDTO.getLiquidName()));
        }
        if(skuPropertyDTO.getIsWood()){
            list.add(new SoB2cDetailDTO.PropertyDTO("木",skuPropertyDTO.getWoodName()));
        }
        if(skuPropertyDTO.getIsPowder()){
            list.add(new SoB2cDetailDTO.PropertyDTO("粉",skuPropertyDTO.getPowderName()));
        }
        if(skuPropertyDTO.getIsPlaster()){
            list.add(new SoB2cDetailDTO.PropertyDTO("膏",skuPropertyDTO.getPlasterName()));
        }
        if(skuPropertyDTO.getIsCuttingTool()){
            list.add(new SoB2cDetailDTO.PropertyDTO("刀",skuPropertyDTO.getCuttingToolName()));
        }
        if(skuPropertyDTO.getIsOther()){
            list.add(new SoB2cDetailDTO.PropertyDTO("其他",skuPropertyDTO.getOtherName()));
        }
        return list;
    }

    @Override
    public void updateWarehouse(SoB2cEntity entity, List<Pair<SoB2cDetailEntity,String>> updateWarehouseList) {
        if (Objects.isNull(entity) || CollectionUtils.isEmpty(updateWarehouseList)){
            return;
        }
        List<String> warehouseIdList = updateWarehouseList.stream().map(obj -> obj.getValue()).distinct().collect(Collectors.toList());

        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        List<String> orgIdList = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_NOT_EXIST_ORG,warehouseList.stream().map(WarehouseDTO.UpdateDTO::getName).collect(Collectors.joining(",")));
        }
        //SKU对照表信息
        List<SkuMappingDTO.ListSkuParamDTO> skuParamList = updateWarehouseList.stream().map(obj -> new SkuMappingDTO.ListSkuParamDTO(obj.getKey().getSkuNo(), obj.getValue(), entity.getDictPlatform())).collect(Collectors.toList());
        ValidList<SkuMappingDTO.ListSkuParamDTO> listSkuParamList = new ValidList<>();
        listSkuParamList.setList(skuParamList);
        List<SkuMappingDTO.ListSkuDTO> skuMappingList = skuMappingService.listBySkuNoList(listSkuParamList);

        //虚拟仓库查询
        VirtualWarehouseChannelDTO.PlatformDTO platformDTO = new VirtualWarehouseChannelDTO.PlatformDTO();
        platformDTO.setDictPlatform(entity.getDictPlatform());
        platformDTO.setRelationId(entity.getShopId());
        platformDTO.setWarehouseIdList(warehouseIdList);
        List<VirtualWarehouseRelationEntity> virtualWarehouseList = wmsVirtualWarehouseFeign.getVirtualWarehouse(platformDTO);

        for (Pair<SoB2cDetailEntity,String> pair : updateWarehouseList) {
            //仓库id
            String warehouseId = pair.getValue();
            //明细信息
            SoB2cDetailEntity detail = pair.getKey();

            WarehouseDTO.UpdateDTO warehouseDTO = warehouseList.stream().filter(obj -> obj.getId().equals(warehouseId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(warehouseDTO)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            String warehouseName = warehouseDTO.getName();
            String warehouseOrgId = warehouseDTO.getOrgId();
            String warehouseOrgName = null;
            BaseIdDTO.CodeDTO codeDTO = accountingCompanyList.stream().filter(obj -> obj.getId().equals(warehouseDTO.getOrgId())).findFirst().orElse(null);
            if (codeDTO != null) {
                warehouseOrgName = codeDTO.getName();
            }
            //库存SKU
            skuMappingList.stream().filter(obj -> StrUtil.equals(obj.getProductSkuId(), detail.getSkuId())
                            && StrUtil.equals(obj.getWarehouseId(), warehouseId))
                    .findFirst()
                    .ifPresent(warehouseListSkuDTO -> detail.setWarehouseSkuNo(warehouseListSkuDTO.getWarehouseSkuNo()));

            //虚拟仓信息
            String virtualWarehouseId = virtualWarehouseList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), warehouseId))
                    .map(VirtualWarehouseRelationEntity::getVirtualWarehouseId).findFirst().orElse("");

            //防止更新了其他字段
            lambdaUpdate().eq(SoB2cDetailEntity::getId, detail.getId())
                    .set(StringUtils.isNotEmpty(warehouseId),SoB2cDetailEntity::getWarehouseId, warehouseId)
                    .set(StringUtils.isNotEmpty(warehouseName),SoB2cDetailEntity::getWarehouseName, warehouseName)
                    .set(StringUtils.isNotEmpty(warehouseOrgId),SoB2cDetailEntity::getWarehouseOrgId, warehouseOrgId)
                    .set(StringUtils.isNotEmpty(warehouseOrgName) ,SoB2cDetailEntity::getWarehouseOrgName, warehouseOrgName)
                    .set(StringUtils.isNotEmpty(detail.getWarehouseSkuNo()), SoB2cDetailEntity::getWarehouseSkuNo, detail.getWarehouseSkuNo())
                    .set(StringUtils.isNotEmpty(virtualWarehouseId),SoB2cDetailEntity::getVirtualWarehouseId,virtualWarehouseId)
                    .set(SoB2cDetailEntity::getIsMatchWarehouseRule,Boolean.TRUE)
                    .set(SoB2cDetailEntity::getUpdateTime, LocalDate.now())
                    .set(SoB2cDetailEntity::getUpdateUserId, UserContext.getDefaultLoginUser().getUid())
                    .set(SoB2cDetailEntity::getUpdateUserName, UserContext.getDefaultLoginUser().getUserName())
                    .update();
        }
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<SoB2cDetailDTO.UpdateDTO> newList, List<SoB2cDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoB2cDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoB2cDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * @description: 明细处理
     * @author Will
     * @date: 2023/8/22 11:06
     * @param list
     * @param isAdd
     */
    private void handleDetailList (List<SoB2cDetailEntity> list,SoB2cEntity soB2cEntity,Boolean isAdd) {
        // 如果 revertId 不为空，分两种情况，第一种是要还原的订单ID与当前订单id一致，则将删除的明细还原，
        // 第二种是要还原的订单ID与当前订单id不一致，则是原订单捆绑拆分拆单后，当前订单做还原，需要将不是当前订单的明细删除 ，并将id清空，MainID设置为当前订单ID
        List<String> restoreIds = new ArrayList<>();
        List<String> revertDetailIdList = new ArrayList<>();
        for (SoB2cDetailEntity soB2cDetailEntity : list) {
            if(StringUtils.isNotBlank(soB2cDetailEntity.getRevertId())){
                if(soB2cEntity.getId().equals(soB2cDetailEntity.getMainId())){
                    restoreIds.add(soB2cDetailEntity.getId());
                }else{
                    revertDetailIdList.add(soB2cDetailEntity.getId());
                    soB2cDetailEntity.setId(null);
                    soB2cDetailEntity.setMainId(soB2cEntity.getId());
                    soB2cDetailEntity.setIsDeleted(false);
                }
            }
        }
        this.updateContainDeleted(restoreIds);
        List<SoB2cDetailEntity> needDeleteDetailList = this.listBySplitId(revertDetailIdList);
        needDeleteDetailList = needDeleteDetailList.stream().filter(v->!v.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(needDeleteDetailList)){
            //过滤掉主表是自动作废的
            List<String> allMainIds = needDeleteDetailList.stream().map(SoB2cDetailEntity::getMainId).distinct().collect(Collectors.toList());
            List<SoB2cEntity> allMains = soB2cService.listByIds(allMainIds);
            allMains = allMains.stream().filter(v->!SoB2cInvalidTypeEnum.ENUM_AUTOMATIC.getCode().equals(v.getInvalidType())).collect(Collectors.toList());
            List<String> filterMainIds = allMains.stream().map(SoB2cEntity::getId).distinct().collect(Collectors.toList());
            needDeleteDetailList = needDeleteDetailList.stream().filter(v->filterMainIds.contains(v.getMainId())).collect(Collectors.toList());
            List<String> needDeleteDetailIds = needDeleteDetailList.stream().map(SoB2cDetailEntity::getId).collect(Collectors.toList());
            service.removeByIds(needDeleteDetailIds);
        }

        //产品信息
        List<String> skuIds = list.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuCostByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            log.error("未找到SKU，warehouseIds = {}",skuList);
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //仓库信息
        List<String> warehouseIdList = list.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);

        List<String> orgIdList = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            log.error("未找到核算公司，orgIdList = {}",orgIdList);
            throw new ServiceException(ApiError.ERROR_9014);
        }

        //虚拟仓库查询
        VirtualWarehouseChannelDTO.PlatformDTO platformDTO = new VirtualWarehouseChannelDTO.PlatformDTO();
        platformDTO.setDictPlatform(soB2cEntity.getDictPlatform());
        platformDTO.setRelationId(soB2cEntity.getShopId());
        platformDTO.setWarehouseIdList(warehouseIdList);
        List<VirtualWarehouseRelationEntity> virtualWarehouseList = wmsVirtualWarehouseFeign.getVirtualWarehouse(platformDTO);

      /*  //SKU对照表信息
        List<SkuMappingDTO.ListSkuParamDTO> listParamList = list.stream().map(obj -> new SkuMappingDTO.ListSkuParamDTO(skuList.stream().filter(e -> e.getSkuId().equals(obj.getSkuId())).findFirst().flatMap(e -> Optional.ofNullable(e.getSkuNo())).orElse(""), obj.getWarehouseId(),soB2cEntity.getDictPlatform())).collect(Collectors.toList());
        ValidList<SkuMappingDTO.ListSkuParamDTO> listSkuParamList = new ValidList<>();
        listSkuParamList.setList(listParamList);
        List<SkuMappingDTO.ListSkuDTO> SkuMappingList = skuMappingService.listBySkuNoList(listSkuParamList);*/

        for (SoB2cDetailEntity detailEntity :list) {

            //产品信息
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst()
                    .orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            detailEntity.setMainId(soB2cEntity.getId());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setCurrency(soB2cEntity.getCurrency());
            detailEntity.setExchangeRate(soB2cEntity.getExchangeRate());
            detailEntity.setImageUrl(skuVO.getSkuImagesUrl());
            //含税单价
            BigDecimal costPrice = ObjectUtils.isEmpty(skuVO.getActualTaxCost()) ? skuVO.getTargetTaxCost() : skuVO.getActualTaxCost();
            detailEntity.setTaxCost(costPrice);

            //如果是拆分的情况，使用前端传的
            if(StringUtils.isBlank(detailEntity.getSplitDetailId())){
                //建议售价
                detailEntity.setAdvicePrice(skuVO.getRetailPrice());
                detailEntity.setAmount(MathUtil.multiply(detailEntity.getPrice(),detailEntity.getQty()));
            }
            //虚拟仓信息
            String virtualWarehouseId = virtualWarehouseList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), detailEntity.getWarehouseId()))
                    .map(VirtualWarehouseRelationEntity::getVirtualWarehouseId).findFirst().orElse("");
            if (StrUtil.isNotBlank(virtualWarehouseId)) {
                detailEntity.setVirtualWarehouseId(virtualWarehouseId);
            }

            //仓库名称
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream()
                    .filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(updateDTO)) {
                detailEntity.setWarehouseName(updateDTO.getName());
                //库存组织
                BaseIdDTO.CodeDTO companyDTO = accountingCompanyList.stream()
                        .filter(obj -> obj.getId().equals(updateDTO.getOrgId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(companyDTO)) {
                    detailEntity.setWarehouseOrgId(updateDTO.getOrgId());
                    detailEntity.setWarehouseOrgName(companyDTO.getName());
                }
            }

            //平台SKU
//            SkuMappingDTO.ListSkuDTO platformListSkuDTO = SkuMappingList.stream().filter(obj -> obj.getProductSkuId().equals(detailEntity.getSkuId()) && obj.getDictPlatform().equals(soB2cEntity.getDictPlatform())).findFirst().orElse(null);
//            if (ObjectUtils.isNotEmpty(platformListSkuDTO)) {
//                // 非平台下载的订单
//                if (!SourceTypeEnum.SO_B2C.getCode().equalsIgnoreCase(soB2cEntity.getSourceType())) {
//                detailEntity.setPlatformSkuNo(platformListSkuDTO.getPlatformSkuNo());
//                detailEntity.setPlatformSpuNo(platformListSkuDTO.getPlatformSpuNo());
//                }
//            } else {
//                detailEntity.setPlatformSkuNo("");
//                detailEntity.setPlatformSpuNo("");
//            }

            //操作日志
            if (StringUtils.isNotBlank(detailEntity.getId())) {
                SoB2cDetailEntity old = this.getById(detailEntity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98026);
                }
                operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SO_B2C.getCode(),old.getMainId(),"",String.format("【%s】",old.getSkuNo()));
            }
        }

        //添加操作日志
        List<SoB2cDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //新增不需要添加新增SKU的日志
        if (CollectionUtils.isNotEmpty(addList) && !isAdd) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(soB2cEntity.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.SO_B2C.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * @description: 新增b2c关联信息
     * @author Will
     * @date: 2023/12/28 11:36
     * @param addDTO
     * @param list
     * @param soB2cEntity
     */
    private void addSoB2cRef (SoB2cDTO.AddDTO addDTO,List<SoB2cDetailEntity> list,SoB2cEntity soB2cEntity) {
        if (ObjectUtils.isEmpty(addDTO.getOperateType())) {
            return;
        }
        List<SoB2cRefDTO.AddDTO> refList = new ArrayList<>();
        List<String> operateDetailIdList = list.stream().map(SoB2cDetailEntity::getOperateDetailId).collect(Collectors.toList());
        List<SoB2cDetailEntity> operateDetailList = this.listByIds(operateDetailIdList);

        for (SoB2cDetailEntity detailEntity :list) {
            SoB2cRefDTO.AddDTO addRefDTO = new SoB2cRefDTO.AddDTO();
            String sourceId = operateDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getOperateDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getMainId())).orElse("");
            if (StrUtil.isBlank(sourceId)) {
                log.error("addSoB2cRef >>>>>> 未找到销售订单明细，detailId = {}",detailEntity.getId());
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            addRefDTO.setSourceId(sourceId);
            addRefDTO.setTargetId(soB2cEntity.getId());
            addRefDTO.setSourceDetailId(detailEntity.getOperateDetailId());
            addRefDTO.setTargetDetailId(detailEntity.getId());
            addRefDTO.setType(addDTO.getOperateType().getCode());
            refList.add(addRefDTO);
        }
        soB2cRefService.add(refList);
    }
}
