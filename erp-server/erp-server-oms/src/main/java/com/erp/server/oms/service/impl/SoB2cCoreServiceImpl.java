package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.bi.entity.BiProductDetailEntity;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SoB2cCoreDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.server.oms.convert.SoB2cCoreConverter;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * b2c扩展类
 * @author will
 * @date 2025/4/24 20:12
 */
@Slf4j
@Service
public class SoB2cCoreServiceImpl implements SoB2cCoreService {

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private SkuMappingService skuMappingService;
    @Resource
    private SoB2cReceiverService soB2cReceiverService;
    @Resource
    private OperateLogService operateLogService;

    @Override
    public List<SoB2cCoreDTO.ListRetryOutstockDTO> listRetryOutstock(BaseIdsDTO.IdsDTO dto) {
        List<SoB2cEntity> soB2cList = soB2cService.listByIds(dto.getIds());
        if (CollUtil.isEmpty(soB2cList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainIds(dto.getIds());
        if (CollUtil.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        Map<String, List<SoB2cDetailEntity>> detailMap = soB2cDetailList.stream().collect(Collectors.groupingBy(SoB2cDetailEntity::getMainId));

        //平台
        List<DictBasicDTO.ViewDTO> dictBasicList = dictBasicService.getByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
        Map<String, String> platformMap = dictBasicList.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getValue, DictBasicDTO.ViewDTO::getName));

        //仓库
        List<String> warehouseIdList = soB2cDetailList.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = FeignQuery.getByIds(WarehouseEntity.class, warehouseIdList);
        Map<String, String> warehouseMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));
        //仓位
        List<String> warehouseLocationIdList = soB2cDetailList.stream().map(SoB2cDetailEntity::getWarehouseLocation).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = FeignQuery.getByIds(WarehouseLocationEntity.class, warehouseLocationIdList);
        Map<String, String> warehouseLocationMap = warehouseLocationList.stream().collect(Collectors.toMap(WarehouseLocationEntity::getId, WarehouseLocationEntity::getName));

        //店铺
        List<String> shopIdList = soB2cList.stream().map(SoB2cEntity::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopList = shopInfoService.listByIds(shopIdList);
        Map<String, ShopInfoEntity> shopMap = shopList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, Function.identity()));

        //sku
        List<String> skuIdList = soB2cDetailList.stream().map(SoB2cDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BiProductDetailEntity> skuList = FeignQuery.getByIds(BiProductDetailEntity.class, skuIdList);
        Map<String, String> skuMap = skuList.stream().collect(Collectors.toMap(BiProductDetailEntity::getId, BiProductDetailEntity::getName));


        List<SoB2cCoreDTO.ListRetryOutstockDTO> list = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cList) {
            List<SoB2cDetailEntity> thisDetailList = detailMap.get(soB2cEntity.getId());
            if (CollUtil.isEmpty(thisDetailList)) {
                log.error("未找到销售订单明细，订单号：{}", soB2cEntity.getCode());
               throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND,CharSequenceUtil.format("订单{}明细信息",soB2cEntity.getCode()));
            }
            //店铺信息
            ShopInfoEntity shopInfoEntity = shopMap.get(soB2cEntity.getShopId());
            if (ObjUtil.isEmpty(shopInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, CharSequenceUtil.format("订单{}店铺信息",soB2cEntity.getCode()));
            }
            for (SoB2cDetailEntity soB2cDetailEntity : thisDetailList) {
                SoB2cCoreDTO.ListRetryOutstockDTO listRetryOutstockDTO =  SoB2cCoreConverter.INSTANCE.convertSoB2cToRetryOutstock(soB2cEntity,soB2cDetailEntity);
                //平台名称
                listRetryOutstockDTO.setPlatformName(platformMap.get(soB2cEntity.getDictPlatform()));
                //仓库名称
                String warehouseName = warehouseMap.get(soB2cDetailEntity.getWarehouseId());
                listRetryOutstockDTO.setWarehouseName(warehouseName);
                //仓位名称
                String warehouseLocationName = warehouseLocationMap.get(soB2cDetailEntity.getWarehouseLocation());
                listRetryOutstockDTO.setWarehouseLocationName(warehouseLocationName);
                //国家信息
                listRetryOutstockDTO.setCountry(shopInfoEntity.getDictCountryCode());
                listRetryOutstockDTO.setCountryName(shopInfoEntity.getCountryName());
                //产品名称
                listRetryOutstockDTO.setProductName(skuMap.get(soB2cDetailEntity.getSkuId()));
                list.add(listRetryOutstockDTO);
            }
        }
        return list;
    }
    /**
     * 校验
     * @author will
     * @date 2025/4/25 12:29
     * @param soB2cEntityList
     * @param soB2cDetailList
     * @return void
     */
    private void checkRetryOutsrock (List<SoB2cCoreDTO.RetryOutstockDTO> outstockList,List<SoB2cEntity> soB2cEntityList,List<SoB2cDetailEntity> soB2cDetailList) {
        if (CollUtil.isEmpty(soB2cEntityList)) {
            return;
        }
        long count = outstockList.stream().filter(obj -> obj.getOutstockTime().isEqual(LocalDateTime.now()) || obj.getOutstockTime().isAfter(LocalDateTime.now())).count();
        if (count > 0) {
            throw new ServiceException("出库时间必须小于当前时间");
        }

        //明细MAP
        Map<String, List<SoB2cDetailEntity>> detailMap = soB2cDetailList.stream().collect(Collectors.groupingBy(SoB2cDetailEntity::getMainId));
        /**
         * - 订单类型为平台仓订单
         * - 订单状态为已发货
         * - 订单无销售出库单
         * - 订单必须全部明细已映射
         */
        List<String> soIdList = soB2cEntityList.stream().map(SoB2cEntity::getId).distinct().collect(Collectors.toList());
        List<SoOutstockEntity> list = FeignQuery.create(SoOutstockEntity.class).in(SoOutstockEntity::getSoId, soIdList).list();
        Map<String, List<SoOutstockEntity>> soOutstockMap = list.stream().collect(Collectors.groupingBy(SoOutstockEntity::getSoId));
        //店铺
        List<String> shopIdList = soB2cEntityList.stream().map(SoB2cEntity::getShopId).distinct().collect(Collectors.toList());
        //平台
        List<String> platformList = soB2cEntityList.stream().map(SoB2cEntity::getDictPlatform).distinct().collect(Collectors.toList());
        //平台sku
        List<String> platformSkuNoList = soB2cDetailList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).distinct().collect(Collectors.toList());

        // 查询该店铺所有平台sku
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setShopIdList(shopIdList);
        paramDTO.setPlatformList(platformList);
        paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
        paramDTO.setPlatformSkuNoList(platformSkuNoList);
        // 所有包含历史映射关系
        List<ListingInfoWithSkuMappingDTO> listingInfoEntityList = skuMappingService.findListDto(paramDTO);
        Map<String, List<ListingInfoWithSkuMappingDTO>> listingMap = listingInfoEntityList.stream().distinct().collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}-{}",obj.getPlatform(),obj.getPlatformSkuNo(),obj.getShopId())));

        for (SoB2cEntity soB2cEntity : soB2cEntityList) {
            if (!soB2cEntity.hasPlatformWarehouseOrder()) {
                throw new ServiceException(CharSequenceUtil.format("订单{}非平台仓订单不支持重新出库", soB2cEntity.getCode()));
            }
            if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())) {
                throw new ServiceException(CharSequenceUtil.format("订单{}非已发货状态不支持重新出库", soB2cEntity.getCode()));
            }
            List<SoOutstockEntity> soOutstockList = soOutstockMap.get(soB2cEntity.getId());
            if (CollUtil.isNotEmpty(soOutstockList)) {
                throw new ServiceException(CharSequenceUtil.format("订单{}已存在出库单不支持重新出库", soB2cEntity.getCode()));
            }
            List<SoB2cDetailEntity> thisDetailList = detailMap.get(soB2cEntity.getId());
            if (CollUtil.isEmpty(thisDetailList)) {
                log.error("未找到销售订单明细，订单号：{}", soB2cEntity.getCode());
                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND,CharSequenceUtil.format("订单{}明细信息",soB2cEntity.getCode()));
            }
            for (SoB2cDetailEntity soB2cDetailEntity : thisDetailList) {
                List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingList = listingMap.get(CharSequenceUtil.format("{}-{}-{}", soB2cEntity.getDictPlatform(), soB2cDetailEntity.getPlatformSkuNo(), soB2cEntity.getShopId()));
                if (CollUtil.isEmpty(listingInfoWithSkuMappingList)) {
                    throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND,CharSequenceUtil.format("订单{}平台SKU{}映射关系",soB2cEntity.getCode(),soB2cDetailEntity.getPlatformSkuNo()));
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean retryOutstock(List<SoB2cCoreDTO.RetryOutstockDTO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<String> b2cSoIdList = list.stream().map(SoB2cCoreDTO.RetryOutstockDTO::getB2cSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cList = soB2cService.listByIds(b2cSoIdList);
        if (CollUtil.isEmpty(soB2cList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainIds(b2cSoIdList);
        if (CollUtil.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //校验
        checkRetryOutsrock(list,soB2cList,soB2cDetailList);

        List<String> countryList = list.stream().map(SoB2cCoreDTO.RetryOutstockDTO::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryEntityList = FeignQuery.getByIds(DictCountryEntity.class, countryList);
        Map<String, String> dictCountryMap = countryEntityList.stream().collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getNameCn));

        //仓库
        List<String> warehouseIdList = list.stream().map(SoB2cCoreDTO.RetryOutstockDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = FeignQuery.getByIds(WarehouseEntity.class, warehouseIdList);
        Map<String, String> warehouseMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        //买家信息
        List<SoB2cReceiverEntity> soB2cReceiverList = soB2cReceiverService.listByMainIds(b2cSoIdList);
        for (SoB2cReceiverEntity receiverEntity : soB2cReceiverList) {
            SoB2cCoreDTO.RetryOutstockDTO retryOutstockDTO = list.stream().filter(obj -> obj.getB2cSoId().equals(receiverEntity.getMainId())).findFirst().orElse(new SoB2cCoreDTO.RetryOutstockDTO());
            receiverEntity.setCountry(retryOutstockDTO.getCountry());
            receiverEntity.setCountryName(dictCountryMap.get(retryOutstockDTO.getCountry()));
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新国家【{}】", receiverEntity.getCountryName()), ModuleTypeEnum.SO_B2C.getCode(), retryOutstockDTO.getB2cSoId(), "重新出库");
        }
        soB2cReceiverService.updateBatchById(soB2cReceiverList);

        for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailList) {
            SoB2cCoreDTO.RetryOutstockDTO retryOutstockDTO = list.stream().filter(obj -> obj.getB2cSoId().equals(soB2cDetailEntity.getMainId()) && obj.getB2cSoDetailId().equals(soB2cDetailEntity.getId())).findFirst().orElse(new SoB2cCoreDTO.RetryOutstockDTO());
            soB2cDetailEntity.setWarehouseLocation(retryOutstockDTO.getWarehouseLocation());
            soB2cDetailEntity.setWarehouseId(retryOutstockDTO.getWarehouseId());
            soB2cDetailEntity.setWarehouseName(warehouseMap.get(retryOutstockDTO.getWarehouseId()));
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新仓库【{}】、仓位【{}】", soB2cDetailEntity.getWarehouseName(),soB2cDetailEntity.getWarehouseLocation()), ModuleTypeEnum.SO_B2C.getCode(), retryOutstockDTO.getB2cSoId(), "重新出库");
        }
        soB2cDetailService.updateBatchById(soB2cDetailList);

        //出库

        return Boolean.TRUE;
    }
}
