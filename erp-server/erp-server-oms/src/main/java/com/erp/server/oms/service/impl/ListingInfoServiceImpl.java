package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.oms.convert.SkuMappingConverter;
import com.erp.server.oms.mapper.ListingInfoMapper;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SkuMappingService;
import com.erp.server.oms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
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

    @Resource
    private ListingInfoServiceImpl service;
    @Resource
    private OperateLogService operateLogService;

    @Autowired
    private ShopInfoService shopInfoService;

    @Resource
    private SkuMappingExtendService skuMappingExtendService;

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
     *
     * @param platform
     * @param platformSkuNo
     * @param authId
     * @return com.erp.model.oms.entity.ListingInfoEntity
     * @author yl
     * @date 2023-08-21 11:57
     */
    @Override
    public ListingInfoEntity getByPlatformSkuNo(String platform, String platformSkuNo, String authId) {
        return lambdaQuery().eq(ListingInfoEntity::getPlatformSkuNo, platformSkuNo).
                eq(ListingInfoEntity::getPlatform, platform).
                eq(ListingInfoEntity::getAuthId, authId).
                last("LIMIT 1").
                one();
    }

    @Override
    public List<ListingInfoEntity> listByParam(String type, String platform, List<String> skuNoList) {
        return lambdaQuery().eq(ListingInfoEntity::getType, type).
                eq(platform != null ,ListingInfoEntity::getPlatform, platform).
                in(ListingInfoEntity::getPlatformSkuNo, skuNoList)
                .list();
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
    public Boolean skuMapping(FbaShipmentDTO.SkuMappingParamDTO dto) {
        SkuMappingEntity skuMapping = skuMappingService.getById(dto.getId());
        if (ObjectUtil.isEmpty(skuMapping)) {
            throw new ServiceException(ApiError.ERROR_M_SKU_NOT_EXIST);
        }
        ListingInfoEntity listingInfoEntity = this.getById(skuMapping.getListingId());
        if (ObjectUtil.isEmpty(listingInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_M_SKU_NOT_EXIST);
        }
        List<SkuVO> skuVOList;
        if(StringUtils.isNotBlank(dto.getSkuId())){
            skuVOList = plmTaskFeign.listSkuProductByIds(Collections.singletonList(dto.getSkuId()));
        } else {
            skuVOList = plmTaskFeign.listBySkuNoList(Collections.singletonList(dto.getSkuNo()));
        }

        SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(dto.getSkuNo())).distinct().findFirst().orElse(null);
        if (null == skuVO) {
            throw new ServiceException("sku不存在");
        }
        SkuMappingEntity lastestSkuMapping  = skuMapping;
        if (StringUtils.isBlank(skuMapping.getProductSkuId()) && StringUtils.isBlank(skuMapping.getProductSkuNo())){
            SkuMappingEntity oldSkuMapping = SkuMappingConverter.INSTANCE.copySkuMappingEntity(skuMapping);
            // 历史无映射关系
            skuMapping.setProductSkuId(skuVO.getSkuId());
            skuMapping.setProductSkuNo(skuVO.getSkuNo());
            skuMapping.setProductName(skuVO.getSkuName());
            if (!skuMappingService.updateById(skuMapping)) {
                throw new ServiceException("[SkuMapping] 首次映射修改失败");
            }
            operateLogService.addModuleOperateLogByObj(oldSkuMapping, skuMapping, ModuleTypeEnum.LISTING_INFO.getCode(), skuMapping.getListingId(), StrUtil.format("用户【{}】首次映射sku", UserContext.getDefaultLoginUser().getUserName()));
        } else {
            LocalDateTime now = LocalDateTime.now();
            skuMapping.setExpireTime(now);
//        skuMapping.setIsDeleted(true);
            skuMapping.setIsExpire(Boolean.TRUE);
            if (!skuMappingService.updateById(skuMapping)) {
                throw new ServiceException("[SkuMapping] 历史映射修改失败");
            }

            if(StringUtils.isBlank(dto.getShopId())){
                throw new ServiceException("店铺ID不能为空");
            }

            //平台如果为空 通过设置平台信息
            if(StringUtils.isBlank(dto.getPlatform())){
                ShopInfoEntity shopInfoEntity = shopInfoService.getById(dto.getId());
                if(Objects.isNull(shopInfoEntity)){
                    throw new ServiceException("店铺为空");
                }
                dto.setPlatform(shopInfoEntity.getDictPlatform());
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
            skuMappingEntity.setDictPlatform(dto.getPlatform());
            PlatformDictEnum platformDictEnum = PlatformDictEnum.checkAndGetByCode(dto.getPlatform());
            skuMappingEntity.setPlatformName(platformDictEnum.getDesc());

            //生效时间
            skuMappingEntity.setEffectiveTime(now);
            skuMappingEntity.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
            skuMappingService.save(skuMappingEntity);
            // 记录日志
            operateLogService.addModuleOperateLogByObj(skuMapping, skuMappingEntity, ModuleTypeEnum.LISTING_INFO.getCode(), skuMappingEntity.getListingId(), StrUtil.format("用户【{}】编辑sku映射表",UserContext.getDefaultLoginUser().getUserName()));
            lastestSkuMapping = skuMappingEntity;
        }
        //速卖通相同店铺，skuNo,平台产品ID 有多个listingInfo, 需要同步映射关系
        if(PlatformDictEnum.ALI_EXPRESS.getCode().equals(dto.getPlatform())){
            this.handleAliExpress(lastestSkuMapping,skuVO,listingInfoEntity);
        }

        return lambdaUpdate()
                .set(ListingInfoEntity::getMatchResult, Boolean.TRUE)
                .eq(ListingInfoEntity::getId, listingInfoEntity.getId())
                .update();
    }


    @Override
    public void handleAliExpress(SkuMappingEntity lastestSkuMapping, SkuVO skuVO, ListingInfoEntity listingInfoEntity) {
        List<ListingInfoEntity> listingInfoEntities = lambdaQuery()
                .eq(ListingInfoEntity::getPlatformSkuNo,listingInfoEntity.getPlatformSkuNo())
                .eq(ListingInfoEntity :: getPlatformSpuNo,listingInfoEntity.getPlatformSpuNo())
                .eq(ListingInfoEntity::getPlatform,listingInfoEntity.getPlatform())
                .ne(ListingInfoEntity::getId,listingInfoEntity.getId())
                .list();
        if(CollectionUtils.isEmpty(listingInfoEntities)){
            return;
        }
        List<String> listingIds = listingInfoEntities.stream().map(ListingInfoEntity::getId).collect(Collectors.toList());
        List<SkuMappingEntity> skuMappingEntityList = skuMappingService.listByListingIds(listingIds);
        skuMappingEntityList = skuMappingEntityList.stream().filter(v->lastestSkuMapping.getShopId().equals(v.getShopId())).collect(Collectors.toList());
        List<SkuMappingEntity> updateList = new ArrayList<>();
        List<SkuMappingEntity> addList = new ArrayList<>();
        for (SkuMappingEntity skuMappingEntity : skuMappingEntityList) {
            if (StringUtils.isBlank(skuMappingEntity.getProductSkuId()) && StringUtils.isBlank(skuMappingEntity.getProductSkuNo())){
                // 历史无映射关系
                skuMappingEntity.setProductSkuId(skuVO.getSkuId());
                skuMappingEntity.setProductSkuNo(skuVO.getSkuNo());
                skuMappingEntity.setProductName(skuVO.getSkuName());
                updateList.add(skuMappingEntity);
                // 记录日志
                operateLogService.addModuleOperateLogByObj(skuMappingEntity, skuMappingEntity, ModuleTypeEnum.LISTING_INFO.getCode(), skuMappingEntity.getListingId(), StrUtil.format("用户【{}】首次映射sku", UserContext.getDefaultLoginUser().getUserName()));
            } else {
                LocalDateTime now = LocalDateTime.now();
                skuMappingEntity.setExpireTime(now);
                skuMappingEntity.setIsExpire(Boolean.TRUE);
                updateList.add(skuMappingEntity);
                SkuMappingEntity addSkuMappingEntity = new SkuMappingEntity();
                addSkuMappingEntity.setType(RuleTypeEnum.PLATFORM);
                addSkuMappingEntity.setShopId(skuMappingEntity.getShopId());
                addSkuMappingEntity.setProductSkuId(skuVO.getSkuId());
                addSkuMappingEntity.setProductSkuNo(skuVO.getSkuNo());
                addSkuMappingEntity.setProductName(skuVO.getSkuName());
                addSkuMappingEntity.setListingId(skuMappingEntity.getListingId());
                addSkuMappingEntity.setDictPlatform(skuMappingEntity.getDictPlatform());
                PlatformDictEnum platformDictEnum = PlatformDictEnum.checkAndGetByCode(skuMappingEntity.getDictPlatform());
                addSkuMappingEntity.setPlatformName(platformDictEnum.getDesc());

                //生效时间
                addSkuMappingEntity.setEffectiveTime(now);
                addSkuMappingEntity.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
                addList.add(addSkuMappingEntity);
                // 记录日志
                operateLogService.addModuleOperateLogByObj(skuMappingEntity, addSkuMappingEntity, ModuleTypeEnum.LISTING_INFO.getCode(), skuMappingEntity.getListingId(), StrUtil.format("用户【{}】编辑sku映射表",UserContext.getDefaultLoginUser().getUserName()));
            }
        }
        lambdaUpdate()
                .set(ListingInfoEntity::getMatchResult, Boolean.TRUE)
                .in(ListingInfoEntity::getId, listingIds)
                .update();
        service.batchOperation(updateList,addList);
        //更新发货设置信息
        skuMappingExtendService.copyBySkuMapping(lastestSkuMapping,skuMappingEntityList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchOperation(List<SkuMappingEntity> updateList, List<SkuMappingEntity> addList) {
        if(CollectionUtils.isNotEmpty(updateList)){
            skuMappingService.updateBatchById(updateList);
        }
        if(CollectionUtils.isNotEmpty(addList)){
            skuMappingService.saveBatch(addList);
        }
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
                pagingParamDTO.setAuthId(viewDTO.getMainId());
            }
        }
        IPage<ListingInfoDTO.PageDTO> iPage = baseMapper.paging(query,pagingParamDTO);
        this.fillData(iPage.getRecords());
        //如果是平台sku查询并且是list 按照顺序返回，因为可能是快粘贴
        List<AdvanceQueryDTO> advanceQueryDTOList = dto.getParams().getAdvanceQueryDTOList();
        List<String> platformSkuList = com.common.business.utils.CollectionUtils.convertStrClzToList(advanceQueryDTOList.stream().filter(v->"li.platform_sku_no".equals(v.getField())&& QueryConditionEnum.IN_LIST.getCompareCode().equals( v.getCompare())).findFirst().orElse(new AdvanceQueryDTO()).getValue());
        if(CollectionUtils.isNotEmpty(platformSkuList)){
            List<ListingInfoDTO.PageDTO> pageDTOList = new ArrayList<>();
            List<ListingInfoDTO.PageDTO> dbList = iPage.getRecords();
            for (String platformSku : platformSkuList) {
                ListingInfoDTO.PageDTO pageDTO = dbList.stream().filter(v->v.getPlatformSku().equals(platformSku)).findFirst().orElse(new ListingInfoDTO.PageDTO());
                pageDTOList.add(pageDTO);
            }
            iPage.setRecords(pageDTOList);
        }
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
        String provideCode;
        if(CollectionUtils.isEmpty(viewDTOList)){
            provideCode = "";
        }else{
            provideCode = viewDTOList.get(0).getProviderCode();
        }
        PlatformDictEnum platformDictEnum = PlatformDictEnum.getByCode(provideCode);
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
        skuMappingEntity.setDictPlatform(platformDictEnum == null ? "":platformDictEnum.getCode());
        skuMappingEntity.setPlatformName(platformDictEnum == null ? "":platformDictEnum.getName());
        skuMappingEntity.setHasMappingAll(true);

        //生效时间
        skuMappingEntity.setEffectiveTime(now);
        skuMappingEntity.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
        skuMappingService.save(skuMappingEntity);

        operateLogService.addModuleOperateLogByObj(skuMapping, skuMappingEntity, ModuleTypeEnum.LISTING_INFO.getCode(), skuMapping.getListingId(), StrUtil.format("用户【{}】编辑sku映射表",UserContext.getDefaultLoginUser().getUserName()));

        return lambdaUpdate()
                .set(ListingInfoEntity::getMatchResult, Boolean.TRUE)
                .eq(ListingInfoEntity::getId, listingInfoEntity.getId())
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchImport(List<ListingInfoEntity> addListingInfoEntityList, List<SkuMappingEntity> updateSkuMappingList, List<ListingInfoEntity> updateListingInfoList, List<SkuMappingEntity> addSkuMappingList, List<Pair<String, String>> addLogPairList, List<Pair<String, String>> updateLogPairList) {
        if (CollectionUtils.isNotEmpty(addListingInfoEntityList)) {
            service.saveBatch(addListingInfoEntityList);
        }

        if (CollectionUtils.isNotEmpty(updateSkuMappingList)){
            if (!skuMappingService.updateBatchById(updateSkuMappingList)){
                throw new ServiceException("映射关系更新异常");
            }
        }
        if (CollectionUtils.isNotEmpty(updateListingInfoList)){
            if (!service.updateBatchById(updateListingInfoList)){
                throw new ServiceException("Listing更新异常");
            }
        }
        if (CollectionUtils.isNotEmpty(addSkuMappingList)) {
            skuMappingService.saveBatch(addSkuMappingList);
        }
        if (CollectionUtils.isNotEmpty(addLogPairList)) {
            operateLogService.batchAddModuleOperateLog(StrUtil.format("用户【{}】新增了sku映射表",UserContext.getDefaultLoginUser().getUserName())+"id为【%s】", ModuleTypeEnum.LISTING_INFO.getCode(), addLogPairList,"新增操作");
        }
        if (CollectionUtils.isNotEmpty(updateLogPairList)) {
            operateLogService.batchAddModuleOperateLog(StrUtil.format("用户【{}】编辑sku映射表",UserContext.getDefaultLoginUser().getUserName())+"，【%s】", ModuleTypeEnum.LISTING_INFO.getCode(), updateLogPairList,"编辑操作");
        }
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
                v.setBoxQty(skuVO.getBoxQty());
            }
            if (CollectionUtils.isNotEmpty(bomChildrenList)) {
                long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(v.getSkuId())).count();
                v.setIsCombination(count > 0);
            }
        });
    }
}
