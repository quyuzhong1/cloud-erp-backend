package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.wms.entity.AliexpressDeliveryDetailEntity;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.*;
import com.erp.server.oms.service.*;
import com.erp.wms.aliexpress.model.product.AliexpressProductDTO;
import com.sdk.third.lingxing.dto.ProductInfo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.math3.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    private SyncAmazonSoMultiChannelService syncAmazonSoMultiChannelService;

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
    private SoB2cReceiverService soB2cReceiverService;
    @Resource
    private SoMultiChannelService soMultiChannelService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
//            syncKingdeeSoService.syncOrderToDmp(entity, syncParamDetailDTO.getSyncOperate());
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
            case SO_MULTI_CHANNEL:
                resultList = newSyncSoMultiChannel(sourceDetailList);
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
            case CAINIAO_LISTING:
                resultList = newSyncCaiNiaoListing(sourceDetailList);
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
     * @description: 同步多渠道订单
     * @author Will
     * @date: 2023/10/30 11:22
     */
    private Map<String, Map<String, Object>> newSyncSoMultiChannel(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String, Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoMultiChannelEntity> list = soMultiChannelService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoInfo >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO : sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            SoMultiChannelEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncAmazonSoMultiChannelService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
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

        List<SoB2cReceiverEntity> soB2cReceiverEntityList = soB2cReceiverService.listByMainIds(soIdList);
        if (CollectionUtils.isEmpty(soB2cReceiverEntityList)) {
            log.error("newSyncSdyDeliveryOrder >>>> 未找B2C销售订单收件人数据：soId={}", soIdList);
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
        List<DictBasicEntity> omsAllDictList = FeignQuery.create(DictBasicEntity.class)
                .in(DictBasicEntity::getType, Arrays.asList(DictBasicTypeEnum.SALES_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_SUB_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(),
                        DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType()
                )).list();

        // 军区信息
        List<DictPartitionEntity> partitionEntityList = FeignQuery.create(DictPartitionEntity.class).list();

        // 国家信息
        List<DictCountryEntity> countryEntityList = FeignQuery.create(DictCountryEntity.class).list();

        // 子区域信息
        List<DictGlobalAreaEntity> dictGlobalEntityList = FeignQuery.create(DictGlobalAreaEntity.class).list();

        // 部门信息
        List<SysDepartmentEntity> deptList = sysUserFeign.getDeptEntityList();
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
            SoB2cReceiverEntity receiverEntity = soB2cReceiverEntityList.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cEntity)) {
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
                    receiverEntity,
                    omsAllDictList,
                    partitionEntityList,
                    countryEntityList,
                    dictGlobalEntityList,
                    deptList));
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

        List<String> currencyIds = list.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIds);

        List<String> soDetailIds = soDetailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoChangeDetailEntity> soChangeDetailEntities = soChangeDetailService.listBySoDetailIdList(soDetailIds);

        List<DictBasicEntity> omsAllDictList = FeignQuery.create(DictBasicEntity.class)
                .in(DictBasicEntity::getType, Arrays.asList(DictBasicTypeEnum.SALES_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_SUB_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(),
                        DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType()
                )).list();

        // 军区信息
        List<DictPartitionEntity> partitionEntityList = FeignQuery.create(DictPartitionEntity.class).list();

        // 国家信息
        List<DictCountryEntity> countryEntityList = FeignQuery.create(DictCountryEntity.class).list();

        // 子区域信息
        List<DictGlobalAreaEntity> dictGlobalEntityList = FeignQuery.create(DictGlobalAreaEntity.class).list();

        // 部门信息
        List<SysDepartmentEntity> deptList = sysUserFeign.getDeptEntityList();

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
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoService.syncDataToSdyFieldHandler(soInfoEntity,
                    soDetailEntity,
                    detailEntityList,
                    syncParamDetailDTO.getSyncOperate(),
                    skuVOList,
                    bomChildrenSkuDTOS,
                    parentSkuList,
                    customerInfoEntities,
                    companyEntities,
                    currencyList,
                    soChangeDetailEntities,
                    omsAllDictList,
                    partitionEntityList,
                    countryEntityList,
                    dictGlobalEntityList,
                    deptList));
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
        List<SoB2cReceiverEntity> soB2cReceiverEntityList = soB2cReceiverService.listByMainIds(soIds);
        if (CollectionUtils.isEmpty(soB2cReceiverEntityList)) {
            log.error("newSyncSdySelfAddDeliveryOrder >>>> 未找B2C销售订单收件人数据：soId={}", soIds);
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

        List<DictBasicEntity> omsAllDictList = FeignQuery.create(DictBasicEntity.class)
                .in(DictBasicEntity::getType, Arrays.asList(DictBasicTypeEnum.SALES_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_SUB_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(),
                        DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType()
                )).list();

        // 军区信息
        List<DictPartitionEntity> partitionEntityList = FeignQuery.create(DictPartitionEntity.class).list();

        // 国家信息
        List<DictCountryEntity> countryEntityList = FeignQuery.create(DictCountryEntity.class).list();

        // 子区域信息
        List<DictGlobalAreaEntity> dictGlobalEntityList = FeignQuery.create(DictGlobalAreaEntity.class).list();

        // 部门信息
        List<SysDepartmentEntity> deptList = sysUserFeign.getDeptEntityList();

        // 计算自发货明细单价
        Map<String, Pair<BigDecimal, BigDecimal>> deliveryDetailPriceMap = syncSoB2cService.convertAllDeliveryDetailPrice(allDeliveryDetail, soB2cDetailEntityList, skuVOList, bomChildrenSkuDTOS);

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

            SoB2cReceiverEntity receiverEntity = soB2cReceiverEntityList.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cEntity)) {
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
                    deliveryDetailPriceMap,
                    receiverEntity,
                    omsAllDictList,
                    partitionEntityList,
                    countryEntityList,
                    dictGlobalEntityList,
                    deptList
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

        List<SoB2cReceiverEntity> soB2cReceiverEntityList = soB2cReceiverService.listByMainIds(soIds);
        if (CollectionUtils.isEmpty(soB2cReceiverEntityList)) {
            log.error("newSyncSdyAliExpressDeliveryOrder >>>> 未找B2C销售订单收件人数据：soId={}", soIds);
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

        List<DictBasicEntity> omsAllDictList = FeignQuery.create(DictBasicEntity.class)
                .in(DictBasicEntity::getType, Arrays.asList(DictBasicTypeEnum.SALES_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_SUB_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(),
                        DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType()
                )).list();

        // 军区信息
        List<DictPartitionEntity> partitionEntityList = FeignQuery.create(DictPartitionEntity.class).list();

        // 国家信息
        List<DictCountryEntity> countryEntityList = FeignQuery.create(DictCountryEntity.class).list();

        // 子区域信息
        List<DictGlobalAreaEntity> dictGlobalEntityList = FeignQuery.create(DictGlobalAreaEntity.class).list();

        // 部门信息
        List<SysDepartmentEntity> deptList = sysUserFeign.getDeptEntityList();

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
            SoB2cReceiverEntity receiverEntity = soB2cReceiverEntityList.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cEntity)) {
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
                    receiverEntity,
                    omsAllDictList,
                    partitionEntityList,
                    countryEntityList,
                    dictGlobalEntityList,
                    deptList
                    ));
        }
        return resultList;
    }
    /**
     * 查询同步SKU到领星
     */
    private Map<String , Map<String, Object>> newSyncCaiNiaoListing(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<ListingInfoEntity> list = listingInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("newSyncWarehouseListing >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            ListingInfoEntity productDetailEntity = list.stream()
                    .filter(obj -> obj.getId().equals(sourceId))
                    .findFirst()
                    .orElse(null);
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                continue;
            }
            AliexpressProductDTO productInfo = listingInfoService.convertAliexpressProductDTO(productDetailEntity);
            if(Objects.isNull(productInfo)){
                log.warn("newSyncCaiNiaoListing >>>> 未找到产品信息: sourceId={}", sourceId);
                continue;
            }
            Map<String, Object> dataMap = JSONUtil.parseObj(productInfo);
            resultList.put(syncParamDetailDTO.getDataId(), dataMap);
        }
        return resultList;
    }
}
