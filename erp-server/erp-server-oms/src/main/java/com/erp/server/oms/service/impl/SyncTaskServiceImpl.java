package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.wms.entity.AliexpressDeliveryDetailEntity;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
import com.erp.server.oms.kingdee.*;
import com.erp.server.oms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/10/30 11:51
 */
@Service
@Slf4j
public class SyncTaskServiceImpl implements SyncTaskService {

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private SyncKingdeeCustomerService syncKingdeeCustomerInfoService;

    @Resource
    private CustomerContactService customerContactService;

    @Resource
    private SyncKingdeeCustomerContactService syncKingdeeCustomerContactService;

    @Resource
    private CustomerGroupService customerGroupService;

    @Resource
    private SyncKingdeeCustomerGroupService syncKingdeeCustomerGroupService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SyncKingdeeSoService syncKingdeeSoService;

    @Resource
    private SoChangeService soChangeService;

    @Resource
    private SyncKingdeeSoChangeService syncKingdeeSoChangeService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Resource
    private SyncSoB2cService syncSoB2cService;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ListingInfoService listingInfoService;

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SoChangeDetailService soChangeDetailService;
    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        switch (sourceType) {
            case CUSTOMER_INFO:
                resultList = syncCustomerInfo(sourceDetailList);
                break;
            case CUSTOMER_CONTACT:
                resultList = syncCustomerContract(sourceDetailList);
                break;
            case CUSTOMER_GROUP:
                resultList = syncCustomerGroup(sourceDetailList);
                break;
            case SO_INFO:
                resultList = syncSoInfo(sourceDetailList);
                break;
            case SO_CHANGE:
                resultList = syncSoChange(sourceDetailList);
                break;
            default:
                break;
        }
        //推送金蝶
        List<DmpPushTaskEntity> finalResultList = resultList;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(finalResultList);
            }
        });
    }

    /**
     * @param sourceDetailList
     * @description: 同步客户
     * @author Will
     * @date: 2023/10/30 11:22
     */
    private List<DmpPushTaskEntity> syncCustomerInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerInfoEntity> list = customerInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerInfo >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            CustomerInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            List<DmpPushTaskEntity> dmpPushTaskList = syncKingdeeCustomerInfoService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.addAll(dmpPushTaskList);
        }
        return resultList;
    }

    /**
     * @param sourceDetailList
     * @description: 同步客户联系人
     * @author Will
     * @date: 2023/10/30 11:22
     */
    private List<DmpPushTaskEntity> syncCustomerContract(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerContactEntity> list = customerContactService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerContract >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            CustomerContactEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCustomerContactService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * @param sourceDetailList
     * @description: 同步客户分组
     * @author Will
     * @date: 2023/10/30 11:22
     */
    private List<DmpPushTaskEntity> syncCustomerGroup(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerGroupEntity> list = customerGroupService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerGroup >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            CustomerGroupEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCustomerGroupService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * @param sourceDetailList
     * @description: 同步销售订单
     * @author Will
     * @date: 2023/10/30 11:22
     */
    private List<DmpPushTaskEntity> syncSoInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoInfoEntity> list = soInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoInfo >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            SoInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSoService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
            //同步订单数据到dmp
            syncKingdeeSoService.syncOrderToDmp(entity, syncParamDetailDTO.getSyncOperate());
        }
        return resultList;
    }

    /**
     * @param sourceDetailList
     * @description: 同步销售变更
     * @author Will
     * @date: 2023/10/30 11:22
     */
    private List<DmpPushTaskEntity> syncSoChange(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoChangeEntity> list = soChangeService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoChange >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            SoChangeEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSoChangeService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    @Override
    public Map<String, Map<String, Object>> newFindDataSendSyncTask(SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        Map<String, Map<String, Object>> resultList = new HashMap<>();
        switch (sourceType) {
            case CUSTOMER_INFO:
                resultList = newSyncCustomerInfo(sourceDetailList);
                break;
            case CUSTOMER_CONTACT:
                resultList = newSyncCustomerContract(sourceDetailList);
                break;
            case CUSTOMER_GROUP:
                resultList = newSyncCustomerGroup(sourceDetailList);
                break;
            case SO_INFO:
                resultList = newSyncSoInfo(sourceDetailList);
                break;
            case SO_CHANGE:
                resultList = newSyncSoChange(sourceDetailList);
                break;
            case SDY_SKU_MAPPING:
                resultList = newSyncSdySkuMapping(sourceDetailList);
                break;
            case SDY_CUSTOMER_INFO:
                resultList = newSyncSdyCustomerInfo(sourceDetailList);
                break;
            case SDY_DELIVERY_ORDER:
                resultList = newSyncSdyDeliveryOrder(sourceDetailList);
                break;
            case SDY_OFFLINE_ORDER:
                resultList = newSyncSdyOfflineOrder(sourceDetailList);
                break;
            case SDY_SELF_DELIVERY_ORDER:
                resultList = newSyncSdySelfAddDeliveryOrder(sourceDetailList);
                break;
            case SDY_ALIEXPRESS_DELIVERY_ORDER:
                resultList = newSyncSdyAliExpressDeliveryOrder(sourceDetailList);
                break;

            default:
                break;
        }
        return resultList;
    }

    /**
     * @param sourceDetailList
     * @description: 同步客户
     * @author Will
     * @date: 2023/10/30 11:22
     */
    private Map<String, Map<String, Object>> newSyncCustomerInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String, Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerInfoEntity> list = customerInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerInfo >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            CustomerInfoEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCustomerInfoService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @param sourceDetailList
     * @description: 同步客户联系人
     * @author Will
     * @date: 2023/10/30 11:22
     */
    private Map<String, Map<String, Object>> newSyncCustomerContract(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String, Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerContactEntity> list = customerContactService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerContract >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            CustomerContactEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCustomerContactService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @param sourceDetailList
     * @description: 同步客户分组
     * @author Will
     * @date: 2023/10/30 11:22
     */
    private Map<String, Map<String, Object>> newSyncCustomerGroup(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String, Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerGroupEntity> list = customerGroupService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerGroup >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            CustomerGroupEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCustomerGroupService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @param sourceDetailList
     * @description: 同步销售订单
     * @author Will
     * @date: 2023/10/30 11:22
     */
    private Map<String, Map<String, Object>> newSyncSoInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String, Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoInfoEntity> list = soInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoInfo >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            SoInfoEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @param sourceDetailList
     * @description: 同步销售变更
     * @author Will
     * @date: 2023/10/30 11:22
     */
    private Map<String, Map<String, Object>> newSyncSoChange(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String, Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoChangeEntity> list = soChangeService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoChange >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            SoChangeEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoChangeService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    private Map<String, Map<String, Object>> newSyncSdySkuMapping(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String, Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SkuMappingEntity> list = skuMappingService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("newSyncSdySkuMapping >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            SkuMappingEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), skuMappingService.newSyncDataToSdy(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    private Map<String, Map<String, Object>> newSyncSdyCustomerInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String, Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerInfoEntity> list = customerInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("newSyncSdyCustomerInfo >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            CustomerInfoEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCustomerInfoService.newSyncDataToSdy(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    private Map<String, Map<String, Object>> newSyncSdyDeliveryOrder(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String, Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        //订单同步数帝云是详情级别同步，所以查询详情
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByIds(sourceIdList);

        //根据详情获取订单主表
        List<String> soIdList = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getMainId).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(soIdList)) {
            return resultList;
        }

        List<SoB2cEntity> soB2cEntities = soB2cService.listByIds(soIdList);
        if (CollectionUtils.isEmpty(soB2cEntities)) {
            log.error("newSyncSdyDeliveryOrder >>>> 未找到数据！");
            return resultList;
        }

        List<String> ids = soB2cEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoB2cDetailEntity> detailEntityList = soB2cDetailService.listByMainIds(ids);

        //产品信息
        List<String> skuNos = soB2cDetailEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        List<String> skuIds = soB2cDetailEntityList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //父类产品
        List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> parentSkuList = new ArrayList<>();
        if (CollUtil.isNotEmpty(parentSkuId)) {
            parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                    .in(ProductDetailEntity::getId, parentSkuId)
                    .list();
        }
        //平台sku映射信息
        List<String> platformSkuNoList = soB2cDetailEntityList.stream().map(req -> req.getPlatformSkuNo()).distinct().collect(Collectors.toList());
        List<ListingInfoEntity> listingInfoEntities = listingInfoService.lambdaQuery().in(ListingInfoEntity::getPlatformSkuNo, platformSkuNoList).list();

        //币别
        List<String> currency = soB2cEntities.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currency);

        //店铺
        List<String> shopIds = soB2cEntities.stream().map(req -> req.getShopId()).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(shopIds)) {
            shopInfoList = shopInfoService.lambdaQuery().in(ShopInfoEntity::getId, shopIds).list();
        }

        List<String> tradeCurrency = shopInfoList.stream().map(req -> req.getTradeCurrency()).distinct().collect(Collectors.toList());
        List<DictCurrencyEntity> dictCurrencyEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(tradeCurrency)) {
            dictCurrencyEntities = FeignQuery.create(DictCurrencyEntity.class).in(DictCurrencyEntity::getId, tradeCurrency).list();
        }

        //客户
        List<String> customerIdList = shopInfoList.stream().map(req -> req.getCustomerId()).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(customerIdList)) {
            customerInfoList = customerInfoService.lambdaQuery().in(CustomerInfoEntity::getId, customerIdList).list();
        }

        List<String> orgList = new ArrayList<>();
        List<String> orgIds = customerInfoList.stream().map(req -> req.getFinancialOrganization()).distinct().collect(Collectors.toList());
        orgList.addAll(orgIds);
        List<String> salseOrgIds = shopInfoList.stream().map(req -> req.getSalesOrgId()).distinct().collect(Collectors.toList());
        orgList.addAll(salseOrgIds);

        List<BaseIdDTO.CodeDTO> companyEntities = new ArrayList<>();

        if (CollUtil.isNotEmpty(orgList)) {
            companyEntities = sysUserFeign.getAccountingCompanyList(orgList);
        }
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.SALES_PLATFORM.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);

        List<String> platformTypeList = customerInfoList.stream().map(req -> req.getPlatformType()).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictList = dictBasicService.lambdaQuery().eq(DictBasicEntity::getType, "sdySubPlatform").in(DictBasicEntity::getName, platformTypeList).list();

        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntityList.stream().filter(req -> req.getId().equalsIgnoreCase(sourceId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cDetailEntity)) {
                continue;
            }
            SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(soB2cDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cEntity)) {
                continue;
            }
            List<SoB2cDetailEntity> detailEntities = detailEntityList.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(detailEntities)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncSoB2cService.syncDataToSdyFieldHandler(soB2cEntity,
                    detailEntities,
                    soB2cDetailEntity,
                    syncParamDetailDTO.getSyncOperate(),
                    skuVOList,
                    bomChildrenSkuDTOS,
                    parentSkuList,
                    listingInfoEntities,
                    currencyList,
                    dictCurrencyEntities,
                    shopInfoList,
                    customerInfoList,
                    companyEntities,
                    dictBasicEntityList,
                    dictList));
        }
        return resultList;
    }

    private Map<String, Map<String, Object>> newSyncSdyOfflineOrder(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String, Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        //订单同步数帝云是详情级别同步，所以查询详情
        List<SoDetailEntity> soDetailEntityList = soDetailService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(soDetailEntityList)) {
            log.error("newSyncSdyOfflineOrder >>>> 未找到数据！");
            return resultList;
        }

        //产品信息
        List<String> skuNos = soDetailEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        List<String> skuIds = soDetailEntityList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //父类产品
        List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> parentSkuList = new ArrayList<>();
        if (CollUtil.isNotEmpty(parentSkuId)) {
            parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                    .in(ProductDetailEntity::getId, parentSkuId)
                    .list();
        }
        List<String> ids = soDetailEntityList.stream().map(req -> req.getMainId()).collect(Collectors.toList());
        List<SoInfoEntity> list = soInfoService.listByIds(ids);

        List<String> customerIds = list.stream().map(req -> req.getCustomerId()).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(customerIds)) {
            customerInfoEntities = customerInfoService.listByIds(customerIds);
        }

        //组织信息
        List<String> orgIdList = new ArrayList<>();
        List<String> orgIds = customerInfoEntities.stream().map(req -> req.getFinancialOrganization()).distinct().collect(Collectors.toList());
        orgIdList.addAll(orgIds);
        List<String> salesOrgIds = list.stream().map(req -> req.getSalesOrgId()).collect(Collectors.toList());
        orgIdList.addAll(salesOrgIds);
        List<BaseIdDTO.CodeDTO> companyEntities = sysUserFeign.getAccountingCompanyList(orgIdList);

        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.SALES_PLATFORM.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);

        List<String> currencyIds = list.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIds);

        List<String> soDetailIds = soDetailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoChangeDetailEntity> soChangeDetailEntities = soChangeDetailService.listBySoDetailIdList(soDetailIds);

        List<String> subPlatformType = customerInfoEntities.stream().map(req -> req.getPlatformType()).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictList = dictBasicService.lambdaQuery().eq(DictBasicEntity::getType, "sdySubPlatform").in(DictBasicEntity::getName, subPlatformType).list();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            SoDetailEntity soDetailEntity = soDetailEntityList.stream().filter(req -> req.getId().equalsIgnoreCase(sourceId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soDetailEntity)) {
                continue;
            }
            SoInfoEntity soInfoEntity = list.stream().filter(req -> req.getId().equals(soDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soInfoEntity)) {
                continue;
            }
            List<SoDetailEntity> detailEntityList = soDetailEntityList.stream().filter(req -> req.getMainId().equals(soInfoEntity.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(detailEntityList)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoService.syncDataToSdyFieldHandler(soInfoEntity, soDetailEntity, detailEntityList, syncParamDetailDTO.getSyncOperate(), skuVOList, bomChildrenSkuDTOS, parentSkuList, customerInfoEntities, companyEntities, dictBasicEntityList, currencyList, soChangeDetailEntities, dictList));
        }
        return resultList;
    }

    /**
     * B2C发货单推送
     */
    private Map<String, Map<String, Object>> newSyncSdySelfAddDeliveryOrder(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        // 订单同步数帝云是详情级别同步，所以查询详情
        // 查询发货单明细
        List<SoB2cDeliveryDetailEntity> curDeliveryDetail = FeignQuery.create(SoB2cDeliveryDetailEntity.class)
                .in(SoB2cDeliveryDetailEntity::getId, sourceIdList)
                .list();

        //根据详情获取主表
        List<String> mainIdList = curDeliveryDetail.stream().map(SoB2cDeliveryDetailEntity::getMainId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(mainIdList)) {
            return resultList;
        }
        // 发货单所有
        List<SoB2cDeliveryDetailEntity> allDeliveryDetail = FeignQuery.create(SoB2cDeliveryDetailEntity.class)
                .in(SoB2cDeliveryDetailEntity::getMainId, mainIdList)
                .list();

        List<SoB2cDeliveryEntity> deliveryList = FeignQuery.create(SoB2cDeliveryEntity.class)
                .in(SoB2cDeliveryEntity::getId, mainIdList)
                .list();
        if (CollectionUtils.isEmpty(deliveryList)) {
            log.error("newSyncSdySelfAddDeliveryOrder >>>> 未找到主表数据: soB2cDeliveryId={}", deliveryList);
            return resultList;
        }

        List<String> soIds = deliveryList.stream().map(SoB2cDeliveryEntity::getSourceId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cList = soB2cService.listByIds(soIds);
        if (CollectionUtils.isEmpty(soB2cList)) {
            log.error("newSyncSdySelfAddDeliveryOrder >>>> 未找B2C销售订单主数据：soId={}", soIds);
            return resultList;
        }

        List<String> soDetailIds = soB2cList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainIds(soDetailIds);
        if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
            log.error("newSyncSdySelfAddDeliveryOrder >>>> 未找B2C销售订单明细数据：soId={}", soIds);
            return resultList;
        }

        //产品信息
        List<String> skuNos = allDeliveryDetail.stream().map(SoB2cDeliveryDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
        skuNos.addAll(soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSkuNo).distinct().collect(Collectors.toList()));
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        List<String> skuIds = allDeliveryDetail.stream().map(SoB2cDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        skuIds.addAll(soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSkuId).distinct().collect(Collectors.toList()));
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //父类产品
        List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> parentSkuList = new ArrayList<>();
        if (CollUtil.isNotEmpty(parentSkuId)) {
            parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                    .in(ProductDetailEntity::getId, parentSkuId)
                    .list();
        }
        //平台sku映射信息
        List<String> platformSkuNoList = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).distinct().collect(Collectors.toList());
        List<ListingInfoEntity> listingInfoEntities = listingInfoService.lambdaQuery().in(ListingInfoEntity::getPlatformSkuNo, platformSkuNoList).list();

        //币别
        List<String> currency = soB2cList.stream().map(SoB2cEntity::getCurrency).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currency);

        //店铺
        List<String> shopIds = soB2cList.stream().map(SoB2cEntity::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(shopIds)) {
            shopInfoList = shopInfoService.lambdaQuery().in(ShopInfoEntity::getId, shopIds).list();
        }

        List<String> tradeCurrency = shopInfoList.stream().map(ShopInfoEntity::getTradeCurrency).distinct().collect(Collectors.toList());
        List<DictCurrencyEntity> dictCurrencyEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(tradeCurrency)) {
            dictCurrencyEntities = FeignQuery.create(DictCurrencyEntity.class).in(DictCurrencyEntity::getId, tradeCurrency).list();
        }

        //客户
        List<String> customerIdList = shopInfoList.stream().map(ShopInfoEntity::getCustomerId).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(customerIdList)) {
            customerInfoList = customerInfoService.lambdaQuery().in(CustomerInfoEntity::getId, customerIdList).list();
        }

        List<String> orgIds = customerInfoList.stream().map(CustomerInfoEntity::getFinancialOrganization).distinct().collect(Collectors.toList());
        List<String> orgList = new ArrayList<>(orgIds);
        List<String> salesOrgIds = shopInfoList.stream().map(ShopInfoEntity::getSalesOrgId).distinct().collect(Collectors.toList());
        orgList.addAll(salesOrgIds);

        List<BaseIdDTO.CodeDTO> companyEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(orgList)) {
            companyEntities = sysUserFeign.getAccountingCompanyList(orgList);
        }

        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.SALES_PLATFORM.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);

        List<String> platformTypeList = customerInfoList.stream().map(CustomerInfoEntity::getPlatformType).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictList = dictBasicService.lambdaQuery().eq(DictBasicEntity::getType, "sdySubPlatform").in(DictBasicEntity::getName, platformTypeList).list();

        // 计算自发货明细单价
        Map<String, BigDecimal> deliveryDetailPriceMap = convertAllDeliveryDetailPrice(allDeliveryDetail, soB2cDetailEntityList, skuVOList, bomChildrenSkuDTOS);

        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            SoB2cDeliveryDetailEntity deliveryDetailEntity = curDeliveryDetail.stream().filter(req -> req.getId().equalsIgnoreCase(sourceId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(deliveryDetailEntity)) {
                continue;
            }
            SoB2cDeliveryEntity deliveryEntity = deliveryList.stream().filter(req -> req.getId().equalsIgnoreCase(deliveryDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(deliveryEntity)) {
                continue;
            }
            SoB2cEntity soB2cEntity = soB2cList.stream().filter(req -> req.getId().equals(deliveryEntity.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cEntity)) {
                continue;
            }
            List<SoB2cDetailEntity> detailEntities = soB2cDetailEntityList.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(detailEntities)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncSoB2cService.syncSelfAddDataToSdyFieldHandler(
                    soB2cEntity,
                    detailEntities,
                    deliveryEntity,
                    allDeliveryDetail,
                    deliveryDetailEntity,
                    syncParamDetailDTO.getSyncOperate(),
                    skuVOList,
                    bomChildrenSkuDTOS,
                    parentSkuList,
                    listingInfoEntities,
                    currencyList,
                    dictCurrencyEntities,
                    shopInfoList,
                    customerInfoList,
                    companyEntities,
                    dictBasicEntityList,
                    dictList,
                    deliveryDetailPriceMap
            ));
        }
        return resultList;
    }

    /**
     * 速卖通发货推送
     */
    private Map<String, Map<String, Object>> newSyncSdyAliExpressDeliveryOrder(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        // 订单同步数帝云是详情级别同步，所以查询详情
        // 查询速卖通发货单明细
        List<AliexpressDeliveryDetailEntity> deliveryDetailList = FeignQuery.create(AliexpressDeliveryDetailEntity.class)
                .in(SoB2cDeliveryDetailEntity::getId, sourceIdList)
                .list();

        //根据详情获取主表
        List<String> mainIdList = deliveryDetailList.stream().map(AliexpressDeliveryDetailEntity::getMainId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(mainIdList)) {
            return resultList;
        }

        List<AliexpressDeliveryEntity> deliveryList = FeignQuery.create(AliexpressDeliveryEntity.class)
                .in(AliexpressDeliveryEntity::getId, mainIdList)
                .list();
        if (CollectionUtils.isEmpty(deliveryList)) {
            log.error("newSyncSdyAliExpressDeliveryOrder >>>> 未找到主表数据: soB2cDeliveryId={}", deliveryList);
            return resultList;
        }

        List<String> soIds = deliveryList.stream().map(AliexpressDeliveryEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cList = soB2cService.listByIds(soIds);
        if (CollectionUtils.isEmpty(soB2cList)) {
            log.error("newSyncSdyAliExpressDeliveryOrder >>>> 未找B2C销售订单主数据：soId={}", soIds);
            return resultList;
        }

        List<String> soDetailIds = soB2cList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainIds(soDetailIds);
        if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
            log.error("newSyncSdyAliExpressDeliveryOrder >>>> 未找B2C销售订单明细数据：soId={}", soIds);
            return resultList;
        }

        // 整个发货明细
        // 查询速卖通发货单明细
        List<AliexpressDeliveryDetailEntity> allDeliveryDetailList = FeignQuery.create(AliexpressDeliveryDetailEntity.class)
                .in(SoB2cDeliveryDetailEntity::getMainId, mainIdList)
                .list();
        if (CollectionUtils.isEmpty(allDeliveryDetailList)) {
            log.error("newSyncSdyAliExpressDeliveryOrder >>>> 未找速卖通明细数据：id={}", mainIdList);
            return resultList;
        }

        //产品信息
        List<String> skuNos = allDeliveryDetailList.stream().map(AliexpressDeliveryDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
        skuNos.addAll(soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSkuNo).distinct().collect(Collectors.toList()));
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        List<String> skuIds = allDeliveryDetailList.stream().map(AliexpressDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        skuIds.addAll(soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSkuId).distinct().collect(Collectors.toList()));
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //父类产品
        List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> parentSkuList = new ArrayList<>();
        if (CollUtil.isNotEmpty(parentSkuId)) {
            parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                    .in(ProductDetailEntity::getId, parentSkuId)
                    .list();
        }
        //平台sku映射信息
        List<String> platformSkuNoList = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).distinct().collect(Collectors.toList());
        List<ListingInfoEntity> listingInfoEntities = listingInfoService.lambdaQuery().in(ListingInfoEntity::getPlatformSkuNo, platformSkuNoList).list();

        //币别
        List<String> currency = soB2cList.stream().map(SoB2cEntity::getCurrency).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currency);

        //店铺
        List<String> shopIds = soB2cList.stream().map(SoB2cEntity::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(shopIds)) {
            shopInfoList = shopInfoService.lambdaQuery().in(ShopInfoEntity::getId, shopIds).list();
        }

        List<String> tradeCurrency = shopInfoList.stream().map(ShopInfoEntity::getTradeCurrency).distinct().collect(Collectors.toList());
        List<DictCurrencyEntity> dictCurrencyEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(tradeCurrency)) {
            dictCurrencyEntities = FeignQuery.create(DictCurrencyEntity.class).in(DictCurrencyEntity::getId, tradeCurrency).list();
        }

        //客户
        List<String> customerIdList = shopInfoList.stream().map(ShopInfoEntity::getCustomerId).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(customerIdList)) {
            customerInfoList = customerInfoService.lambdaQuery().in(CustomerInfoEntity::getId, customerIdList).list();
        }

        List<String> orgIds = customerInfoList.stream().map(CustomerInfoEntity::getFinancialOrganization).distinct().collect(Collectors.toList());
        List<String> orgList = new ArrayList<>(orgIds);
        List<String> salesOrgIds = shopInfoList.stream().map(ShopInfoEntity::getSalesOrgId).distinct().collect(Collectors.toList());
        orgList.addAll(salesOrgIds);

        List<BaseIdDTO.CodeDTO> companyEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(orgList)) {
            companyEntities = sysUserFeign.getAccountingCompanyList(orgList);
        }

        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.SALES_PLATFORM.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);

        List<String> platformTypeList = customerInfoList.stream().map(CustomerInfoEntity::getPlatformType).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictList = dictBasicService.lambdaQuery().eq(DictBasicEntity::getType, "sdySubPlatform").in(DictBasicEntity::getName, platformTypeList).list();

        // 计算自发货明细单价
        Map<String, BigDecimal> deliveryDetailPriceMap = convertAllAliExpressDeliveryDetailPrice(allDeliveryDetailList, soB2cDetailEntityList, skuVOList);

        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            AliexpressDeliveryDetailEntity deliveryDetailEntity = deliveryDetailList.stream().filter(req -> req.getId().equalsIgnoreCase(sourceId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(deliveryDetailEntity)) {
                continue;
            }
            AliexpressDeliveryEntity deliveryEntity = deliveryList.stream().filter(req -> req.getId().equalsIgnoreCase(deliveryDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(deliveryEntity)) {
                continue;
            }
            SoB2cEntity soB2cEntity = soB2cList.stream().filter(req -> req.getId().equals(deliveryEntity.getSoId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cEntity)) {
                continue;
            }
            List<SoB2cDetailEntity> detailEntities = soB2cDetailEntityList.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(detailEntities)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncSoB2cService.syncAliExpressDataToSdyFieldHandler(
                    soB2cEntity,
                    detailEntities,
                    deliveryEntity,
                    deliveryDetailList,
                    deliveryDetailEntity,
                    syncParamDetailDTO.getSyncOperate(),
                    skuVOList,
                    bomChildrenSkuDTOS,
                    parentSkuList,
                    listingInfoEntities,
                    currencyList,
                    dictCurrencyEntities,
                    shopInfoList,
                    customerInfoList,
                    companyEntities,
                    dictBasicEntityList,
                    dictList,
                    deliveryDetailPriceMap
                    ));
        }
        return resultList;
    }

    /**
     * 计算自发发货单明细单价
     *
     * @param deliveryDetailList    自发货单明细
     * @param soB2cDetailEntityList 销售订单明细
     * @param skuVOList sku列表
     * @param bomChildrenSkuDTOS bom信息
     * @return Map<自发货明细ID, 平分单价>
     */
    private Map<String, BigDecimal> convertAllDeliveryDetailPrice(List<SoB2cDeliveryDetailEntity> deliveryDetailList,
                                                                  List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                                  List<SkuVO> skuVOList,
                                                                  List<BomChildrenSkuDTO> bomChildrenSkuDTOS
    ) {
        // Map<自发货明细ID, 平均采购含税成本>
        Map<String, BigDecimal> resultMap = new HashMap<>();

        Map<String, SoB2cDetailEntity> detailEntityMap = soB2cDetailEntityList.stream().collect(Collectors.toMap(BaseEntity::getId, e -> e));

        Map<String, SkuVO> skuVoMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, e -> e));

        // 按明细ID分组
        Map<String, List<SoB2cDeliveryDetailEntity>> deliveryMap = deliveryDetailList
                .stream()
                .collect(Collectors.groupingBy(SoB2cDeliveryDetailEntity::getSourceDetailId));

        for (Map.Entry<String, List<SoB2cDeliveryDetailEntity>> entry : deliveryMap.entrySet()) {
            String soDetailId = entry.getKey();
            SoB2cDetailEntity soDetailEntity = detailEntityMap.get(soDetailId);
            if (null == soDetailEntity){
                ServiceException.runError("未找到订单明细:明细ID={}", soDetailId);
            }
            // 未拆分
            if (1 == entry.getValue().size()){
                SoB2cDeliveryDetailEntity b2cDeliveryDetailEntity = entry.getValue().get(0);
                if (b2cDeliveryDetailEntity.getSkuId().equalsIgnoreCase(soDetailEntity.getSkuId())){
                    resultMap.put(b2cDeliveryDetailEntity.getId(), soDetailEntity.getPrice());
                    continue;
                } else {
                    ServiceException.runError("未拆分订单明细:发货单明细sku和销售订单明细不相同:delivery_sku={}, so_sku={}",
                            b2cDeliveryDetailEntity.getSkuId(),
                            soDetailEntity.getSkuId()
                    );
                }
            }

            // 总单价
            BigDecimal price = soDetailEntity.getPrice();

            // 存在bom
            List<BomChildrenSkuDTO> bomList = bomChildrenSkuDTOS.stream().filter(e -> e.getParentSkuId().equalsIgnoreCase(soDetailEntity.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(bomList)){
                ServiceException.runError("未找到BOM:sku={}", soDetailEntity.getSkuId());
            }
            // 计算bom总成本
            BigDecimal totalCostAmount = BigDecimal.ZERO;
            // sku
            for (BomChildrenSkuDTO bomChildrenSkuDTO : bomList) {
                SkuVO skuVO = skuVoMap.get(bomChildrenSkuDTO.getSkuId());
                if (null == skuVO){
                    ServiceException.runError("未找sku信息:skuId={}", bomChildrenSkuDTO.getSkuId());
                }
                BigDecimal costPrice = ObjectUtils.isEmpty(skuVO.getActualTaxCost()) ? skuVO.getTargetTaxCost() : skuVO.getActualTaxCost();
                if (null == costPrice){
                    ServiceException.runError("未找到成本信息:skuId={}", bomChildrenSkuDTO.getSkuId());
                }
                if (0 == costPrice.compareTo(BigDecimal.ZERO)){
                    ServiceException.runError("成本信息为0:skuId={}", bomChildrenSkuDTO.getSkuId());
                }
                BigDecimal allItemPrice = costPrice.multiply(BigDecimal.valueOf(bomChildrenSkuDTO.getQuantity()));
                totalCostAmount = totalCostAmount.add(allItemPrice);
            }

            // 按明细创建时间排序
            List<SoB2cDeliveryDetailEntity> curDetailList = entry.getValue().stream().sorted(Comparator.comparing(SoB2cDeliveryDetailEntity::getId)).collect(Collectors.toList());
            for (int i = 0; i < curDetailList.size(); i++) {
                SoB2cDeliveryDetailEntity b2cDeliveryDetailEntity = curDetailList.get(i);
                BomChildrenSkuDTO curBom =  bomList.stream().filter(e -> e.getSkuId().equalsIgnoreCase(b2cDeliveryDetailEntity.getSkuId())).findFirst().orElse(null);
                if (null == curBom){
                    ServiceException.runError("未找到bom:skuId={},parentId={}", b2cDeliveryDetailEntity.getSkuId(), soDetailEntity.getSkuId());
                }
                SkuVO skuVO = skuVoMap.get(b2cDeliveryDetailEntity.getSkuId());
                if (i == curDetailList.size() - 1){
                    resultMap.put(b2cDeliveryDetailEntity.getId(), price);
                } else {
                    // 当前单价 = 明细单价 * (bom成本 * bom数量 / bom总成本) / bom数量
                    BigDecimal curPrice = price.multiply(skuVO.getActualTaxCost())
                            .divide(totalCostAmount, 4, RoundingMode.DOWN);
                    resultMap.put(b2cDeliveryDetailEntity.getId(), curPrice);
                    // 剩余单价 = 当前单价 * bom数量
                    price = price.subtract(curPrice.multiply(BigDecimal.valueOf(curBom.getQuantity())));
                }
            }
        }
        return resultMap;
    }


    /**
     * 计算自发发货单明细单价
     *
     * @param deliveryDetailList    速卖通发货单明细
     * @param soB2cDetailEntityList
     * @param skuVOList             sku列表
     * @return Map<速卖通明细ID, 平分单价>
     */
    private Map<String, BigDecimal> convertAllAliExpressDeliveryDetailPrice(List<AliexpressDeliveryDetailEntity> deliveryDetailList,
                                                                            List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                                            List<SkuVO> skuVOList
    ) {
        // Map<速卖通明细ID, 平均采购含税成本>
        Map<String, BigDecimal> resultMap = new HashMap<>();

        Map<String, SkuVO> skuVoMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, e -> e));

        // 平台产品ID 分组
        Map<String, List<AliexpressDeliveryDetailEntity>> deliveryMap = deliveryDetailList
                .stream()
                .collect(Collectors.groupingBy(AliexpressDeliveryDetailEntity::getPlatformSpuNo));

        for (Map.Entry<String, List<AliexpressDeliveryDetailEntity>> entry : deliveryMap.entrySet()) {
            // 未拆分
            if (1 == entry.getValue().size()){
                for (AliexpressDeliveryDetailEntity aliExpressDetailEntity : entry.getValue()) {
                    resultMap.put(aliExpressDetailEntity.getId(), aliExpressDetailEntity.getPrice());
                }
                continue;
            }
            // 平台库存产品ID一样 = 未拆分
            if (1 == entry.getValue().stream().map(AliexpressDeliveryDetailEntity::getScItemId).count()){
                for (AliexpressDeliveryDetailEntity aliExpressDetailEntity : entry.getValue()) {
                    resultMap.put(aliExpressDetailEntity.getId(), aliExpressDetailEntity.getPrice());
                }
                continue;
            }

            // 总单价
            BigDecimal price = entry.getValue().get(0).getPrice();

            BigDecimal finalPrice = price;
            // 根据产品ID匹配, 目前速卖通明细产品ID唯一
            SoB2cDetailEntity detailEntity = soB2cDetailEntityList.stream().filter(
                    e -> e.getPlatformSpuNo().equalsIgnoreCase(entry.getKey())).findFirst().orElse(null);
            if (null == detailEntity){
                ServiceException.runError("未找对应明细:产品ID={}", entry.getKey());
            }

            BigDecimal totalCostAmount = BigDecimal.ZERO;
            // 计算总成本
            // sku
            for (AliexpressDeliveryDetailEntity deliveryDetailEntity : deliveryDetailList) {
                SkuVO skuVO = skuVoMap.get(deliveryDetailEntity.getSkuId());
                if (null == skuVO){
                    ServiceException.runError("未找sku信息:skuId={}", deliveryDetailEntity.getSkuId());
                }
                BigDecimal costPrice = ObjectUtils.isEmpty(skuVO.getActualTaxCost()) ? skuVO.getTargetTaxCost() : skuVO.getActualTaxCost();
                if (null == costPrice){
                    ServiceException.runError("未找到成本信息:skuId={}", deliveryDetailEntity.getSkuId());
                }
                if (0 == costPrice.compareTo(BigDecimal.ZERO)){
                    ServiceException.runError("成本信息为0:skuId={}", deliveryDetailEntity.getSkuId());
                }
                BigDecimal allItemPrice = costPrice.multiply(BigDecimal.valueOf(deliveryDetailEntity.getOrderLineQty()));
                totalCostAmount = totalCostAmount.add(allItemPrice);
            }
            totalCostAmount = totalCostAmount.divide(BigDecimal.valueOf(detailEntity.getQty()), 4, RoundingMode.DOWN);

            // 汇总
            Map<String, List<AliexpressDeliveryDetailEntity>> groupMap = entry.getValue()
                    .stream()
                    .collect(Collectors.groupingBy(AliexpressDeliveryDetailEntity::getSkuId));
            List<Map.Entry<String, List<AliexpressDeliveryDetailEntity>>> entryList = new ArrayList<>(groupMap.entrySet());

            // 剩余价格
            BigDecimal lastPrice = price;
            for (int i = 0; i < entryList.size(); i++) {
                Map.Entry<String, List<AliexpressDeliveryDetailEntity>> curEntry = entryList.get(i);

                SkuVO skuVO = skuVoMap.get(curEntry.getKey());
                List<AliexpressDeliveryDetailEntity> value = curEntry.getValue();
                if (i == entryList.size() - 1){
                    for (AliexpressDeliveryDetailEntity deliveryDetailEntity : value) {
                        resultMap.put(deliveryDetailEntity.getId(), lastPrice);
                    }
                } else {
                    // 当前单价 = 明细单价 * (成本 / 总成本)
                    BigDecimal curPrice = price.multiply(skuVO.getActualTaxCost())
                            .divide(totalCostAmount, 4, RoundingMode.DOWN);
                    for (AliexpressDeliveryDetailEntity deliveryDetailEntity : value) {
                        resultMap.put(deliveryDetailEntity.getId(), curPrice);
                    }
                    int sum = value.stream().mapToInt(AliexpressDeliveryDetailEntity::getOrderLineQty).sum();
                    // 剩余单价 = 当前单价 * 发货明细数量 / 明细数量
                    BigDecimal planBomPrice = curPrice.multiply(BigDecimal.valueOf(sum))
                            .divide(BigDecimal.valueOf(detailEntity.getQty()), 4, RoundingMode.DOWN);
                    lastPrice = lastPrice.subtract(planBomPrice);
                }
            }
        }

        return resultMap;
    }
}
