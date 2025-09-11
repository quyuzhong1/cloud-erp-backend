package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.entity.CfgSettingEntity;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.server.oms.convert.SoB2cCoreConverter;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;

    @Resource
    private WmsVirtualWarehouseFeign wmsVirtualWarehouseFeign;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private ThirdWarehouseDeliveryFeign thirdWarehouseDeliveryFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;

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

        //买家信息
        List<SoB2cReceiverEntity> soB2cReceiverList = soB2cReceiverService.listByMainIds(dto.getIds());
        Map<String, SoB2cReceiverEntity> soB2cReceiverMap = soB2cReceiverList.stream().collect(Collectors.toMap(SoB2cReceiverEntity::getMainId, Function.identity()));

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

        //sku
        List<String> skuIdList = soB2cDetailList.stream().map(SoB2cDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);
        Map<String, String> skuMap = skuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));


        List<SoB2cCoreDTO.ListRetryOutstockDTO> list = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cList) {
            List<SoB2cDetailEntity> thisDetailList = detailMap.get(soB2cEntity.getId());
            if (CollUtil.isEmpty(thisDetailList)) {
                log.error("未找到销售订单明细，订单号：{}", soB2cEntity.getCode());
               throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND,CharSequenceUtil.format("订单{}明细信息",soB2cEntity.getCode()));
            }
            SoB2cReceiverEntity receiverEntity = soB2cReceiverMap.get(soB2cEntity.getId());
            if (ObjectUtil.isEmpty(receiverEntity)) {
                log.error("未找到销售订单买家信息，订单号：{}", soB2cEntity.getCode());
                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND,CharSequenceUtil.format("订单{}买家信息",soB2cEntity.getCode()));
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
                listRetryOutstockDTO.setCountry(receiverEntity.getCountry());
                listRetryOutstockDTO.setCountryName(receiverEntity.getCountryName());
                //产品名称
                listRetryOutstockDTO.setProductName(skuMap.get(soB2cDetailEntity.getSkuId()));
                if(Objects.nonNull(soB2cEntity.getSoOutstockDate())){
                    listRetryOutstockDTO.setOutstockTime(soB2cEntity.getSoOutstockDate().atStartOfDay());
                }
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
        //平台产品ID
        List<String> platformSpuNoList = soB2cDetailList.stream().map(SoB2cDetailEntity::getPlatformSpuNo).distinct().collect(Collectors.toList());

        // 查询该店铺所有平台sku
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setShopIdList(shopIdList);
        paramDTO.setPlatformList(platformList);
        paramDTO.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
        paramDTO.setPlatformSkuNoList(platformSkuNoList);
        paramDTO.setPlatformSpuNoList(platformSpuNoList);
        // 所有包含历史映射关系

        //物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(soIdList);
        Map<String, SoB2cLogisticsEntity> soB2cLogisticsMap = soB2cLogisticsList.stream().collect(Collectors.toMap(SoB2cLogisticsEntity::getMainId, Function.identity()));

        for (SoB2cEntity soB2cEntity : soB2cEntityList) {
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsMap.get(soB2cEntity.getId());
            if (ObjUtil.isEmpty(soB2cLogisticsEntity)) {
                throw new ServiceException(CharSequenceUtil.format("订单{}物流信息不存在", soB2cEntity.getCode()));
            }
            if (!OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode().equals(soB2cLogisticsEntity.getLogisticType())) {
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
        List<String> shopIdList = soB2cList.stream().map(SoB2cEntity::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.listByIds(shopIdList);
        Map<String, SoB2cEntity> soB2cMap = soB2cList.stream().collect(Collectors.toMap(SoB2cEntity::getId, Function.identity()));

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
            SoB2cEntity current = soB2cMap.get(receiverEntity.getMainId());
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(obj -> obj.getId().equals(current.getShopId())).findFirst().orElse(new ShopInfoEntity());
            soB2cReceiverService.buildPartitionId(receiverEntity,shopInfoEntity);
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新国家【{}】", receiverEntity.getCountryName()), ModuleTypeEnum.SO_B2C.getCode(), retryOutstockDTO.getB2cSoId(), "重新出库");
        }
        soB2cReceiverService.updateBatchById(soB2cReceiverList);

        Map<String, List<SoB2cCoreDTO.RetryOutstockDTO>> retryMap = list.stream().collect(Collectors.groupingBy(obj -> obj.getB2cSoId().concat(obj.getWarehouseId())));
        for (Map.Entry<String, List<SoB2cCoreDTO.RetryOutstockDTO>> entry : retryMap.entrySet()) {
            //主表
            SoB2cEntity soB2cEntity = soB2cMap.get(entry.getValue().get(0).getB2cSoId());
            List<String> b2cSoDetailIdList = entry.getValue().stream().map(SoB2cCoreDTO.RetryOutstockDTO::getB2cSoDetailId).distinct().collect(Collectors.toList());
            //明细
            List<SoB2cDetailEntity> thisDetailList = soB2cDetailList.stream().filter(obj -> b2cSoDetailIdList.contains(obj.getId())).collect(Collectors.toList());
            //虚拟仓库查询
            VirtualWarehouseChannelDTO.PlatformDTO platformDTO = new VirtualWarehouseChannelDTO.PlatformDTO();
            platformDTO.setDictPlatform(soB2cEntity.getDictPlatform());
            platformDTO.setRelationId(soB2cEntity.getShopId());
            platformDTO.setWarehouseIdList(warehouseIdList);
            SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverList.stream().filter(obj -> obj.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(new SoB2cReceiverEntity());
            platformDTO.setPartitionId(soB2cReceiverEntity.getPartitionId());
            List<VirtualWarehouseRelationEntity> virtualWarehouseList = wmsVirtualWarehouseFeign.getVirtualWarehouse(platformDTO);

            for (SoB2cDetailEntity soB2cDetailEntity : thisDetailList) {
                SoB2cCoreDTO.RetryOutstockDTO retryOutstockDTO = list.stream().filter(obj -> obj.getB2cSoId().equals(soB2cDetailEntity.getMainId()) && obj.getB2cSoDetailId().equals(soB2cDetailEntity.getId())).findFirst().orElse(new SoB2cCoreDTO.RetryOutstockDTO());
                soB2cDetailEntity.setWarehouseLocation(retryOutstockDTO.getWarehouseLocation());
                soB2cDetailEntity.setWarehouseId(retryOutstockDTO.getWarehouseId());
                soB2cDetailEntity.setWarehouseName(warehouseMap.get(retryOutstockDTO.getWarehouseId()));
                //虚拟仓信息
                String virtualWarehouseId = virtualWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), soB2cDetailEntity.getWarehouseId()))
                        .map(VirtualWarehouseRelationEntity::getVirtualWarehouseId).findFirst().orElse("");
                soB2cDetailEntity.setVirtualWarehouseId(virtualWarehouseId);

                operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新仓库【{}】、仓位【{}】", soB2cDetailEntity.getWarehouseName(),soB2cDetailEntity.getWarehouseLocation()), ModuleTypeEnum.SO_B2C.getCode(), retryOutstockDTO.getB2cSoId(), "重新出库");
            }
            soB2cDetailService.updateBatchById(thisDetailList);
            //出库
            String outPutClass =  "";
            String sourceCode = "";
            if (PlatformDictEnum.MERCADOLIBRE.getCode().equals(soB2cEntity.getDictPlatform())) {
                outPutClass = "MercadoOrderRocketMQTaskHandler";
                sourceCode = soB2cEntity.getPlatformCode();
            }  else if (PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(soB2cEntity.getDictPlatform())) {
                outPutClass = "MercadoLocalOrderRocketMQTaskHandler";
                sourceCode = soB2cEntity.getPlatformCode();
            } else if (PlatformDictEnum.SHOPEE.getCode().equals(soB2cEntity.getDictPlatform())) {
                outPutClass = "DmpOutputShopeeOrderRocketMQTaskHandler";
                sourceCode = soB2cEntity.getPlatformCode();
            }else if (PlatformDictEnum.LING_XING.getCode().equals(soB2cEntity.getThirdSystem())) {
                outPutClass = "DmpOutputLxOrderRocketMQTaskHandler";
                sourceCode = soB2cEntity.getThirdCode();
            }
            List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = dmpTaskFeign.getOutputTaskRecord(sourceCode,outPutClass);
            PlatformOrderDTO dto = null;
            if (CollectionUtils.isNotEmpty(dmpOutputTaskRecordEntityList)) {
                DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = dmpOutputTaskRecordEntityList.get(0);
                dto = JSONUtil.toBean(dmpOutputTaskRecordEntity.getRequestData(), PlatformOrderDTO.class);
                dto.setBillDate(entry.getValue().get(0).getOutstockTime().toLocalDate());
            }
            soB2cEntity.setSoOutstockDate(entry.getValue().get(0).getOutstockTime().toLocalDate());
            soB2cEntity.setDetailEntityList(thisDetailList);
            soB2cEntity.setCoverOutDate(true);
            SoB2cHandler.handleSoOutStock(dto, null, soB2cEntity);
            operateLogService.addModuleOperateLog("重新出库", ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "重新出库");

        }
        return Boolean.TRUE;
    }


    @Override
    public List<SoOutstockDTO.GenerateB2cDTO> splitB2cSoOutstock(SoB2cEntity mainEntity, SoOutstockDTO.GenerateB2cDTO generateB2cDTO) {
        if (!PlatformDictEnum.MERCADOLIBRE.getCode().equals(mainEntity.getDictPlatform())
                && !PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(mainEntity.getDictPlatform())
                && !PlatformDictEnum.SHOPEE.getCode().equals(mainEntity.getDictPlatform())
                && !PlatformDictEnum.LING_XING.getCode().equals(mainEntity.getThirdSystem())) {
            return Collections.singletonList(generateB2cDTO);
        }
        //封装明细仓库id和虚拟仓id
        List<SoB2cDetailEntity> soB2cDetailList = mainEntity.getDetailEntityList();
        LinkedList<SoOutstockDetailDTO.AddDTO> detailList = generateB2cDTO.getDetailList();
        if(CollUtil.isNotEmpty(soB2cDetailList)){
            List<String> soDetailIds = soB2cDetailList.stream().map(SoB2cDetailEntity::getId).distinct().collect(Collectors.toList());
            detailList =  detailList.stream().filter(v->soDetailIds.contains(v.getSoDetailId())).collect(Collectors.toCollection(LinkedList::new));
            detailList.forEach(v->{
                SoB2cDetailEntity soB2cDetailEntity = soB2cDetailList.stream().filter(obj -> obj.getId().equals(v.getSoDetailId())).findFirst().orElse(null);
                if(Objects.nonNull(soB2cDetailEntity)){
                    v.setWarehouseId(soB2cDetailEntity.getWarehouseId());
                    v.setWarehouseName(soB2cDetailEntity.getWarehouseName());
                    v.setVirtualWarehouseId(soB2cDetailEntity.getVirtualWarehouseId());
                    v.setWarehouseLocation(soB2cDetailEntity.getWarehouseLocation());
                }
            });
        }
        //根据明细仓库id分组
        Map<String, LinkedList<SoOutstockDetailDTO.AddDTO>> map = detailList.stream().collect(Collectors.groupingBy(SoOutstockDetailDTO.AddDTO::getWarehouseId,Collectors.toCollection(LinkedList::new)));
        List<SoOutstockDTO.GenerateB2cDTO> generateB2cList = new LinkedList<>();
        for (Map.Entry<String, LinkedList<SoOutstockDetailDTO.AddDTO>> entry :map.entrySet()) {
            SoOutstockDTO.GenerateB2cDTO generateB2c = new SoOutstockDTO.GenerateB2cDTO();
            BeanUtil.copyProperties(generateB2cDTO,generateB2c);
            generateB2c.setWarehouseId(entry.getKey());
            generateB2c.setWarehouseName(entry.getValue().get(0).getWarehouseName());
            if(mainEntity.isCoverOutDate()){
                generateB2c.setBillDate(mainEntity.getSoOutstockDate());
            }
            generateB2c.setDetailList(entry.getValue());
            generateB2cList.add(generateB2c);
        }
        return generateB2cList;
    }

    @Override
    public Boolean listPayMethodSetting(SoB2cEntity entity) {
        List<CfgSettingEntity> cfgSettingList = cfgSettingService.listSettingByKey(CfgSettingEnum.PAY_METHOD.getCode());
        if (CollUtil.isEmpty(cfgSettingList)) {
            return Boolean.FALSE;
        }
        List<CfgSettingDTO.PayMethodDTO> list = cfgSettingList.stream()
                .filter(obj -> CharSequenceUtil.isNotBlank(obj.getValue()))
                .map(obj -> JSONUtil.toBean(obj.getValue(), CfgSettingDTO.PayMethodDTO.class))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(list)) {
            log.error("未找到支付方式，平台：{}，支付方式：{}", entity.getDictPlatform(), entity.getDictPayMethod());
            return Boolean.FALSE;
        }
        Map<String, CfgSettingDTO.PayMethodDTO> map = list.stream().collect(Collectors.toMap(obj -> CharSequenceUtil.format("{}-{}", obj.getPlatform(), obj.getPayMethod()), Function.identity()));
        CfgSettingDTO.PayMethodDTO payMethodDTO = map.get(CharSequenceUtil.format("{}-{}", entity.getDictPlatform(), entity.getDictPayMethod()));
        if (ObjUtil.isEmpty(payMethodDTO)) {
            log.error("未找到支付方式配置，平台：{}，支付方式：{}", entity.getDictPlatform(), entity.getDictPayMethod());
           return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 付款信息验证
     * @author will
     * @date 2025/5/30 15:51
     * @param entity
     * @return void
     */
    @Override
    public void checkPayMent(SoB2cEntity entity) {
        //查询支付方式是否支持继续发货
        Boolean isFlag = this.listPayMethodSetting(entity);
        //如果支付状态是待付款，并且支付方式不支持继续发货，则抛出异常
        if ((ObjectUtil.isEmpty(entity.getPayStatus()) || SoB2cPayStatusEnum.ENUM_PAYMENT.getCode().equals(entity.getPayStatus())) && !isFlag) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_PAYMENT_NOT_OPERATE, entity.getCode());
        }
    }

    @Override
    public void generateDeliveryAndOutStock(SoB2cEntity entity, List<SoB2cDetailEntity> detailEntityList, SoB2cDTO.DeliveryWithNotOutboundDTO dto,SoB2cLogisticsEntity soB2cLogisticsEntity,SoB2cReceiverEntity soB2cReceiverEntity) {
        //虚拟仓库查询
        VirtualWarehouseChannelDTO.PlatformDTO platformDTO = new VirtualWarehouseChannelDTO.PlatformDTO();
        platformDTO.setDictPlatform(entity.getDictPlatform());
        platformDTO.setRelationId(entity.getShopId());
        platformDTO.setWarehouseIdList(Arrays.asList(dto.getWarehouseId()));
        platformDTO.setPartitionId(soB2cReceiverEntity.getPartitionId());
        List<VirtualWarehouseRelationEntity> virtualWarehouseList = wmsVirtualWarehouseFeign.getVirtualWarehouse(platformDTO);
        for (SoB2cDetailEntity detailEntity : detailEntityList) {
            if(CollectionUtils.isNotEmpty(virtualWarehouseList)){
                String virtualWarehouseId = virtualWarehouseList.get(0).getVirtualWarehouseId();
                detailEntity.setVirtualWarehouseId(virtualWarehouseId);
            }else{
                detailEntity.setVirtualWarehouseId("");
            }
        }
        //先更新订单信息
        entity.setSignOrderError("");
        soB2cService.updateById(entity);
        soB2cDetailService.updateBatchById(detailEntityList);

        //判断是三方仓还是自发货生成不同的发货单
        //检测是否是API 对接的仓库
        try {
            List<OverseasProviderWarehouseDTO.ViewDTO> overseasWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(Collections.singletonList(dto.getWarehouseId()));
            Boolean isThirdWarehouse = CollectionUtils.isNotEmpty(overseasWarehouseList);
            if(isThirdWarehouse){
                GenerateDeliveryAndOutStockDTO generateDeliveryAndOutStockDTO = new GenerateDeliveryAndOutStockDTO(entity,detailEntityList,dto,overseasWarehouseList.get(0),soB2cLogisticsEntity);
                thirdWarehouseDeliveryFeign.generateDeliveryAndOutStock(generateDeliveryAndOutStockDTO);
            }else{
                GenerateDeliveryAndOutStockDTO generateDeliveryAndOutStockDTO = new GenerateDeliveryAndOutStockDTO(entity,detailEntityList,dto,new OverseasProviderWarehouseDTO.ViewDTO(),soB2cLogisticsEntity);
                soB2cDeliveryFeign.generateDeliveryAndOutStock(generateDeliveryAndOutStockDTO);
            }
        }catch (Exception e){
            log.error("订单{}不出库发货生成发货单或出库单失败，异常信息：{}", entity.getCode(), e.getMessage());
            entity.setSignOrderError(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
            entity.setBillStatus(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
            entity.setIsNotOutbound(false);
            soB2cService.updateById(entity);
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public Boolean handleSoOutStock(String soId) {
        SoB2cEntity soB2cEntity = soB2cService.getById(soId);
        if (ObjectUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST, soId);
        }
        //明细
        List<SoB2cDetailEntity> thisDetailList = soB2cDetailService.listByMainId(soId);
        //出库
        String outPutClass =  "";
        String sourceCode = "";
        if (PlatformDictEnum.MERCADOLIBRE.getCode().equals(soB2cEntity.getDictPlatform())) {
            outPutClass = "MercadoOrderRocketMQTaskHandler";
            sourceCode = soB2cEntity.getPlatformCode();
        }  else if (PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(soB2cEntity.getDictPlatform())) {
            outPutClass = "MercadoLocalOrderRocketMQTaskHandler";
            sourceCode = soB2cEntity.getPlatformCode();
        } else if (PlatformDictEnum.SHOPEE.getCode().equals(soB2cEntity.getDictPlatform())) {
            outPutClass = "DmpOutputShopeeOrderRocketMQTaskHandler";
            sourceCode = soB2cEntity.getPlatformCode();
        }else if (PlatformDictEnum.LING_XING.getCode().equals(soB2cEntity.getThirdSystem())) {
            outPutClass = "DmpOutputLxOrderRocketMQTaskHandler";
            sourceCode = soB2cEntity.getThirdCode();
        }else if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(soB2cEntity.getDictPlatform())) {
            outPutClass = "DmpOutputAliExpressOrderRocketMQTaskHandler";
            sourceCode = soB2cEntity.getThirdCode();
        }
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = dmpTaskFeign.getOutputTaskRecord(sourceCode,outPutClass);
        PlatformOrderDTO dto = null;
        if (CollectionUtils.isNotEmpty(dmpOutputTaskRecordEntityList)) {
            DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = dmpOutputTaskRecordEntityList.get(0);
            dto = JSONUtil.toBean(dmpOutputTaskRecordEntity.getRequestData(), PlatformOrderDTO.class);
            dto.setBillDate(LocalDate.now());
        }
        soB2cEntity.setSoOutstockDate(LocalDate.now());
        soB2cEntity.setDetailEntityList(thisDetailList);
        soB2cEntity.setCoverOutDate(true);
        return SoB2cHandler.handleSoOutStock(dto, null, soB2cEntity);
    }
}
