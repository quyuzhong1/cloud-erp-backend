package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.entity.DmpThirdInventoryEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.TiktokFbtDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentExtendEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.model.wms.entity.OperateLogEntity;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.enums.*;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.server.wms.service.FbaShipmentStatusService;
import com.erp.server.wms.service.FbaShipmentExtendService;
import com.erp.server.wms.service.FbtInboundService;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.TiktokFbtApiService;
import com.erp.server.wms.service.repository.FbtInboundRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FbtInboundServiceImpl implements FbtInboundService {

    private static final String LOCK_KEY_PREFIX = "fbt:sync:";
    private static final int DEFAULT_JOB_LOOKBACK_MINUTES = 30;

    @Resource
    private TiktokFbtApiService tiktokFbtApiService;
    @Resource
    private FbtInboundRepository fbtInboundRepository;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private FbaShipmentReceiveService fbaShipmentReceiveService;
    @Resource
    private FbaShipmentStatusService fbaShipmentStatusService;
    @Resource
    private FbaShipmentExtendService fbaShipmentExtendService;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void syncInboundOrder(String inboundOrderId) {
        syncInboundOrder(inboundOrderId, null);
    }

    @Override
    public void syncInboundOrder(String inboundOrderId, String sellerOpenId) {
        String lockKey = LOCK_KEY_PREFIX + inboundOrderId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, System.currentTimeMillis(), 5, TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("FBT同步跳过，锁已存在, inboundOrderId={}, sellerOpenId={}", inboundOrderId, sellerOpenId);
            return;
        }
        long start = System.currentTimeMillis();
        try {
            TiktokFbtDTO.InboundOrderDTO inboundOrder = findInboundOrderById(inboundOrderId, sellerOpenId);
            if (inboundOrder == null) {
                log.warn("未查询到FBT货件, inboundOrderId={}, sellerOpenId={}", inboundOrderId, sellerOpenId);
                return;
            }
            syncInboundOrderInternal(inboundOrder);
        } finally {
            redisTemplate.delete(lockKey);
            log.info("FBT同步结束, inboundOrderId={}, sellerOpenId={}, costMs={}",
                    inboundOrderId, sellerOpenId, System.currentTimeMillis() - start);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncInboundOrderFromDmp(TiktokFbtDTO.InboundOrderDTO inboundOrder) {
        if (inboundOrder == null || StrUtil.isBlank(inboundOrder.getInboundOrderId())) {
            return;
        }
        String inboundOrderId = inboundOrder.getInboundOrderId();
        String lockKey = LOCK_KEY_PREFIX + inboundOrderId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, System.currentTimeMillis(), 5, TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("FBT DMP同步跳过，锁已存在, inboundOrderId={}", inboundOrderId);
            return;
        }
        long start = System.currentTimeMillis();
        try {
            syncInboundOrderInternal(inboundOrder);
        } finally {
            redisTemplate.delete(lockKey);
            log.info("FBT DMP同步结束, inboundOrderId={}, costMs={}",
                    inboundOrderId, System.currentTimeMillis() - start);
        }
    }

    @Override
    public void syncRecentInboundOrders() {
        syncRecentInboundOrders(null, null);
    }

    @Override
    public void syncRecentInboundOrders(Long startTime, Long endTime) {
        List<String> shopIds = listAuthorizedFbtShopIds();
        if (shopIds == null || shopIds.isEmpty()) {
            log.info("FBT库存记录定时同步跳过，未找到FBT已授权店铺");
            return;
        }
        long nowSeconds = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
        Long normalizedStart = normalizeEpochSeconds(startTime);
        Long normalizedEnd = normalizeEpochSeconds(endTime);
        long queryEnd = normalizedEnd != null && normalizedEnd > 0 ? normalizedEnd : nowSeconds;
        long queryStart = normalizedStart != null && normalizedStart > 0 ? normalizedStart : queryEnd - DEFAULT_JOB_LOOKBACK_MINUTES * 60L;
        if (queryStart > queryEnd) {
            queryStart = queryEnd - DEFAULT_JOB_LOOKBACK_MINUTES * 60L;
        }
        for (String currentShopId : shopIds) {
            List<TiktokFbtDTO.InventoryRecordDTO> records =
                    tiktokFbtApiService.queryInventoryRecords(currentShopId, null, null, queryStart, queryEnd);
            int handleCount = 0;
            for (TiktokFbtDTO.InventoryRecordDTO record : records) {
                if (record == null) {
                    continue;
                }
                if (handleInventoryRecord(currentShopId, record)) {
                    handleCount++;
                }
            }
            log.info("FBT库存记录定时同步完成, shopId={}, startTime={}, endTime={}, handled={}",
                    currentShopId, queryStart, queryEnd, handleCount);
        }
    }

    @Override
    public void syncRecentInventorySnapshots() {
        Map<String, OverseasProviderEntity> providerMap = listAuthorizedFbtProviderMapByShopId();
        if (providerMap.isEmpty()) {
            log.info("FBT库存快照同步跳过，未找到FBT已授权店铺");
            return;
        }
        for (Map.Entry<String, OverseasProviderEntity> entry : providerMap.entrySet()) {
            String shopId = entry.getKey();
            OverseasProviderEntity provider = entry.getValue();
            int handled = syncInventorySnapshotsByShop(shopId, provider);
            log.info("FBT库存快照同步完成, shopId={}, handled={}", shopId, handled);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleInventoryRecordFromDmp(TiktokFbtDTO.InventoryRecordDTO record) {
        if (record == null || StrUtil.isBlank(record.getInboundOrderId())) {
            return false;
        }
        return handleInventoryRecord(record.getShopId(), record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean upsertInventorySnapshotFromDmp(TiktokFbtDTO.InventorySnapshotDTO snapshot, String authId) {
        if (snapshot == null || StrUtil.isBlank(snapshot.getSkuCode())) {
            return false;
        }
        OverseasProviderEntity provider = resolveAuthorizedFbtProvider(authId, snapshot.getShopId());
        String shopId = resolveAuthorizedFbtShopId(snapshot.getShopId(), provider);
        String resolvedAuthId = StrUtil.isNotBlank(authId)
                ? authId
                : provider == null ? null : provider.getId();
        if (provider != null && StrUtil.isNotBlank(shopId) && StrUtil.isNotBlank(resolvedAuthId)) {
            OverseasProviderWarehouseEntity shopBoundWarehouse = findShopBoundProviderWarehouse(provider, shopId);
            if (shopBoundWarehouse != null && StrUtil.isNotBlank(shopBoundWarehouse.getPlatformWarehouseCode())) {
                TiktokFbtDTO.InventorySnapshotDTO mergedSnapshot =
                        buildMergedSnapshotFromDmpThirdInventory(snapshot, resolvedAuthId, shopId, shopBoundWarehouse);
                OverseasInventoryEntity inventory = upsertInventorySnapshot(mergedSnapshot, provider);
                if (inventory != null) {
                    cleanupMergedSkuInventories(provider, mergedSnapshot.getSkuCode(), inventory.getId());
                }
                return inventory != null;
            }
        }
        return upsertInventorySnapshot(snapshot, provider) != null;
    }

    private Long normalizeEpochSeconds(Long rawTime) {
        if (rawTime == null || rawTime <= 0) {
            return null;
        }
        // 兼容毫秒时间戳入参
        if (rawTime > 99999999999L) {
            return rawTime / 1000;
        }
        return rawTime;
    }

    private List<String> listAuthorizedFbtShopIds() {
        return new ArrayList<>(listAuthorizedFbtProviderMapByShopId().keySet());
    }

    private Map<String, OverseasProviderEntity> listAuthorizedFbtProviderMapByShopId() {
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.FBT.getCode())
                .list();
        if (providerList == null || providerList.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ShopInfoEntity> authorizedShopList = listAuthorizedTiktokShops().stream()
                .filter(Objects::nonNull)
                .filter(shop -> !Boolean.TRUE.equals(shop.getDisabled()))
                .filter(shop -> StrUtil.isNotBlank(shop.getId()))
                .collect(Collectors.toList());
        Map<String, String> accountToShopIdMap = new LinkedHashMap<>();
        Map<String, String> platformShopCodeToShopIdMap = new LinkedHashMap<>();
        for (ShopInfoEntity shop : authorizedShopList) {
            putShopIdLookup(accountToShopIdMap, shop.getAccount(), shop.getId());
            putShopIdLookup(platformShopCodeToShopIdMap, shop.getPlatformShopCode(), shop.getId());
        }
        Map<String, OverseasProviderEntity> result = new LinkedHashMap<>();
        for (OverseasProviderEntity provider : providerList) {
            if (provider == null) {
                continue;
            }
            String shopId = resolveProviderShopId(provider, accountToShopIdMap, platformShopCodeToShopIdMap);
            if (StrUtil.isBlank(shopId)) {
                continue;
            }
            result.putIfAbsent(shopId, provider);
        }
        return result;
    }

    private int syncInventorySnapshotsByShop(String shopId, OverseasProviderEntity provider) {
        List<TiktokFbtDTO.InventorySnapshotDTO> snapshots =
                tiktokFbtApiService.queryInventorySnapshots(shopId, null, null);
        List<TiktokFbtDTO.InventorySnapshotDTO> aggregatedSnapshots =
                aggregateInventorySnapshots(shopId, snapshots, provider);
        int handled = 0;
        List<OverseasInventoryEntity> updatedInventories = new ArrayList<>();
        for (TiktokFbtDTO.InventorySnapshotDTO snapshot : aggregatedSnapshots) {
            OverseasInventoryEntity inventory = upsertInventorySnapshot(snapshot, provider);
            if (inventory != null) {
                handled++;
                updatedInventories.add(inventory);
            }
        }
        cleanupSnapshotInventories(provider, updatedInventories);
        return handled;
    }

    private String resolveProviderShopId(OverseasProviderEntity provider,
                                         Map<String, String> accountToShopIdMap,
                                         Map<String, String> platformShopCodeToShopIdMap) {
        String shopId = firstMeaningful(
                provider.getAuthJson() == null ? null : provider.getAuthJson().get("shopId"));
        if (StrUtil.isNotBlank(shopId)) {
            return shopId;
        }

        String shopAccount = firstMeaningful(
                provider.getAuthJson() == null ? null : provider.getAuthJson().get("shopAccount"),
                provider.getAuthJson() == null ? null : provider.getAuthJson().get("platformShopCode"),
                provider.getAuthJson() == null ? null : provider.getAuthJson().get("shopCode"),
                provider.getPlatformAccount());
        if (StrUtil.isBlank(shopAccount)) {
            return null;
        }
        String normalizedShopAccount = normalizeLookupKey(shopAccount);
        return firstMeaningful(
                accountToShopIdMap.get(normalizedShopAccount),
                platformShopCodeToShopIdMap.get(normalizedShopAccount));
    }

    private void putShopIdLookup(Map<String, String> lookupMap, String rawKey, String shopId) {
        String normalizedKey = normalizeLookupKey(rawKey);
        if (lookupMap == null || StrUtil.isBlank(normalizedKey) || StrUtil.isBlank(shopId)) {
            return;
        }
        lookupMap.putIfAbsent(normalizedKey, shopId);
    }

    private String normalizeLookupKey(String rawKey) {
        if (StrUtil.isBlank(rawKey)) {
            return null;
        }
        return rawKey.trim().toLowerCase(Locale.ROOT);
    }

    private String firstMeaningful(Object... values) {
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value);
            if (StrUtil.isNotBlank(text) && !"null".equalsIgnoreCase(text)) {
                return text;
            }
        }
        return null;
    }

    private OverseasProviderEntity resolveAuthorizedFbtProvider(String authId, String shopId) {
        OverseasProviderEntity provider = null;
        if (StrUtil.isNotBlank(authId)) {
            provider = findAuthorizedFbtProviderById(authId);
        }
        if (provider == null && StrUtil.isNotBlank(shopId)) {
            provider = listAuthorizedFbtProviderMapByShopId().get(shopId);
        }
        return provider;
    }

    private String resolveAuthorizedFbtShopId(String shopId, OverseasProviderEntity provider) {
        if (StrUtil.isNotBlank(shopId) || provider == null || StrUtil.isBlank(provider.getId())) {
            return shopId;
        }
        String providerId = provider.getId();
        return listAuthorizedFbtProviderMapByShopId().entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> StrUtil.equals(providerId, entry.getValue().getId()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private OverseasProviderEntity findAuthorizedFbtProviderById(String authId) {
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getId, authId)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.FBT.getCode())
                .list();
        if (providerList == null || providerList.isEmpty()) {
            return null;
        }
        return providerList.get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncInboundOrderInternal(TiktokFbtDTO.InboundOrderDTO inboundOrder) {
        String inboundOrderId = inboundOrder.getInboundOrderId();
        long start = System.currentTimeMillis();
        FbaShipmentEntity current = fbtInboundRepository.findShipmentByInboundOrderId(inboundOrderId);
        boolean created = false;
        String beforeStatus = current == null ? null : current.getPlatformShipmentStatus();
        if (current == null) {
            current = createFbtInbound(inboundOrder);
            created = true;
        } else {
            updateFbtStatus(current, inboundOrder);
        }
        ensureCreateOperateLog(current);
        if (!created && !StrUtil.equals(beforeStatus, current.getPlatformShipmentStatus())) {
            addStatusChangeOperateLog(current, beforeStatus, current.getPlatformShipmentStatus());
        }
        String afterStatus = current.getPlatformShipmentStatus();
        syncInventoryRecord(current, inboundOrder);
        log.info("FBT货件同步完成, inboundOrderId={}, isNew={}, statusBefore={}, statusAfter={}, costMs={}",
                inboundOrderId, created, beforeStatus, afterStatus, System.currentTimeMillis() - start);
    }

    private TiktokFbtDTO.InboundOrderDTO findInboundOrderById(String inboundOrderId, String sellerOpenId) {
        List<String> candidateShopIds = new ArrayList<>();
        if (StrUtil.isNotBlank(sellerOpenId)) {
            String shopId = findAuthorizedShopIdByOpenId(sellerOpenId);
            if (StrUtil.isBlank(shopId)) {
                log.warn("FBT同步未匹配到授权店铺, sellerOpenId={}, inboundOrderId={}", sellerOpenId, inboundOrderId);
                return null;
            }
            candidateShopIds.add(shopId);
            log.info("FBT同步命中授权店铺, sellerOpenId={}, shopId={}, inboundOrderId={}",
                    sellerOpenId, shopId, inboundOrderId);
        } else {
            candidateShopIds = listAuthorizedTiktokShopIds();
        }
        if (candidateShopIds == null || candidateShopIds.isEmpty()) {
            return null;
        }
        for (String shopId : candidateShopIds) {
            List<TiktokFbtDTO.InboundOrderDTO> result = tiktokFbtApiService.queryInboundOrders(shopId, Collections.singletonList(inboundOrderId), null);
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
        }
        return null;
    }

    private String findAuthorizedShopIdByOpenId(String sellerOpenId) {
        List<ShopInfoEntity> shopList = listAuthorizedTiktokShops();
        for (ShopInfoEntity shopInfo : shopList) {
            if (shopInfo == null || Boolean.TRUE.equals(shopInfo.getDisabled())) {
                continue;
            }
            Map<String, Object> extendData = shopInfo.getExtendData();
            if (extendData == null || extendData.isEmpty()) {
                continue;
            }
            Object openIdObj = extendData.get("openId");
            if (openIdObj != null && StrUtil.equals(String.valueOf(openIdObj), sellerOpenId)) {
                return shopInfo.getId();
            }
        }
        return null;
    }

    private List<String> listAuthorizedTiktokShopIds() {
        return listAuthorizedTiktokShops().stream()
                .filter(Objects::nonNull)
                .filter(shop -> !Boolean.TRUE.equals(shop.getDisabled()))
                .map(ShopInfoEntity::getId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<ShopInfoEntity> listAuthorizedTiktokShops() {
        List<ShopInfoEntity> tiktok = shopInfoFeign.listByParams(
                new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.TIK_TOK.getCode(), null));
        if (tiktok == null) {
            return new ArrayList<>();
        }
        Map<String, ShopInfoEntity> mergeMap = new LinkedHashMap<>();
        for (ShopInfoEntity shopInfo : tiktok) {
            if (shopInfo != null && StrUtil.isNotBlank(shopInfo.getId())) {
                mergeMap.put(shopInfo.getId(), shopInfo);
            }
        }
        return new ArrayList<>(mergeMap.values());
    }

    private FbaShipmentEntity createFbtInbound(TiktokFbtDTO.InboundOrderDTO inboundOrder) {
        FbaShipmentEntity entity = new FbaShipmentEntity();
        entity.setShopId(inboundOrder.getShopId());
        fillShopCountryInfo(entity, inboundOrder.getShopId());
        entity.setCode(StrUtil.blankToDefault(inboundOrder.getInboundOrderId(), ""));
        entity.setName(StrUtil.blankToDefault(inboundOrder.getShipmentName(), "FBT-" + inboundOrder.getInboundOrderId()));
        entity.setFbaShipmentId(StrUtil.blankToDefault(inboundOrder.getInboundOrderId(), ""));
        entity.setFulfillmentCenter(StrUtil.blankToDefault(inboundOrder.getWarehouseCode(), ""));
        // FBT接口当前未返回发货/配送地址，先兜底空串以满足fba_shipment非空约束
        entity.setDeliveryFromAddress("");
        entity.setDeliveryToAddress("");
        entity.setSourceType(ShipmentSourceTypeEnum.FBT.getCode());
        entity.setShipmentCreateTime(LocalDateTime.now());
        entity.setPlatformShipmentStatus(inboundOrder.getStatus());
        entity.setDeliveryStatus(DeliveryStatusEnum.UN_SHIPPED.getCode());
        fbtInboundRepository.saveShipment(entity);
        saveOrUpdateFbtExtend(entity.getId(), inboundOrder);
        fbaShipmentStatusService.saveByFbaShipment(entity);
        syncShipmentDetails(entity, inboundOrder);
        return entity;
    }

    private void fillShopCountryInfo(FbaShipmentEntity entity, String shopId) {
        ShopInfoEntity shopInfo = null;
        try {
            shopInfo = shopInfoFeign.getShopInfoById(shopId);
        } catch (Exception e) {
            log.warn("查询店铺信息失败, shopId={}, err={}", shopId, e.getMessage());
        }
        if (shopInfo != null) {
            entity.setShopName(StrUtil.blankToDefault(shopInfo.getName(), ""));
            entity.setCountryId(StrUtil.blankToDefault(shopInfo.getDictCountryCode(), ""));
            entity.setCountryName(StrUtil.blankToDefault(shopInfo.getCountryName(), ""));
            return;
        }
        entity.setShopName("");
        entity.setCountryId("");
        entity.setCountryName("");
    }

    private void updateFbtStatus(FbaShipmentEntity entity, TiktokFbtDTO.InboundOrderDTO inboundOrder) {
        String oldPlatformShipmentStatus = entity.getPlatformShipmentStatus();
        String newPlatformShipmentStatus = StrUtil.blankToDefault(inboundOrder.getStatus(), "");
        entity.setPlatformShipmentStatus(newPlatformShipmentStatus);
        entity.setShipmentReceiveTime(LocalDateTime.now());
        fbtInboundRepository.updateShipment(entity);
        saveOrUpdateFbtExtend(entity.getId(), inboundOrder);
        if (!StrUtil.equals(oldPlatformShipmentStatus, newPlatformShipmentStatus)) {
            fbaShipmentStatusService.saveByFbaShipment(entity);
        }
        syncShipmentDetails(entity, inboundOrder);
    }

    private void saveOrUpdateFbtExtend(String mainId, TiktokFbtDTO.InboundOrderDTO inboundOrder) {
        if (StrUtil.isBlank(mainId) || inboundOrder == null) {
            return;
        }
        TiktokFbtDTO.CarrierDTO firstCarrier = Optional.ofNullable(inboundOrder.getCarriers())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        String carrierName = firstCarrier == null ? "" : StrUtil.blankToDefault(firstCarrier.getCarrierName(), "");
        String trackingNo = firstCarrier == null ? "" : StrUtil.blankToDefault(firstCarrier.getTrackingNumber(), "");
        List<FbaShipmentExtendEntity> extendEntityList = fbaShipmentExtendService.listByMainIds(Collections.singletonList(mainId));
        FbaShipmentExtendEntity extendEntity = CollectionUtils.isNotEmpty(extendEntityList) ? extendEntityList.get(0) : new FbaShipmentExtendEntity();
        extendEntity.setMainId(mainId);
        extendEntity.setCarrierName(carrierName);
        extendEntity.setTrackingNo(trackingNo);
        if (StrUtil.isNotBlank(extendEntity.getId())) {
            fbaShipmentExtendService.updateById(extendEntity);
        } else {
            fbaShipmentExtendService.save(extendEntity);
        }
    }

    private void ensureCreateOperateLog(FbaShipmentEntity entity) {
        if (entity == null || StrUtil.isBlank(entity.getId())) {
            return;
        }
        Integer count = operateLogService.lambdaQuery()
                .eq(OperateLogEntity::getBusinessId, entity.getId())
                .eq(OperateLogEntity::getOperation, "新增FBT货件")
                .count();
        if (count != null && count > 0) {
            return;
        }
        String msg = StrUtil.format("新增了FBT货件【{}】", entity.getFbaShipmentId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_SHIPMENT.getCode(), entity.getId(), "新增FBT货件");
    }

    private void addStatusChangeOperateLog(FbaShipmentEntity entity, String oldStatus, String newStatus) {
        String msg = StrUtil.format("FBT货件【{}】平台状态由【{}】变更为【{}】",
                entity.getFbaShipmentId(),
                StrUtil.blankToDefault(oldStatus, ""),
                StrUtil.blankToDefault(newStatus, ""));
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_SHIPMENT.getCode(), entity.getId(), "FBT货件状态变更");
    }

    private void syncShipmentDetails(FbaShipmentEntity shipment, TiktokFbtDTO.InboundOrderDTO inboundOrder) {
        if (shipment == null || StrUtil.isBlank(shipment.getId()) || inboundOrder == null
                || inboundOrder.getPlannedGoods() == null || inboundOrder.getPlannedGoods().isEmpty()) {
            log.warn("FBT货件同步明细跳过, shipmentId={}, inboundOrderId={}, plannedGoodsEmpty={}",
                    shipment == null ? null : shipment.getId(),
                    inboundOrder == null ? null : inboundOrder.getInboundOrderId(),
                    inboundOrder == null || inboundOrder.getPlannedGoods() == null || inboundOrder.getPlannedGoods().isEmpty());
            return;
        }
        List<FbaShipmentDetailEntity> exists = fbtInboundRepository.listShipmentDetails(shipment.getId());
        Map<String, FbaShipmentDetailEntity> existsMap = new LinkedHashMap<>();
        for (FbaShipmentDetailEntity detail : exists) {
            String detailKey = buildDetailKey(detail);
            if (StrUtil.isBlank(detailKey)) {
                continue;
            }
            FbaShipmentDetailEntity duplicate = existsMap.putIfAbsent(detailKey, detail);
            if (duplicate != null && !StrUtil.equals(duplicate.getId(), detail.getId())) {
                log.warn("FBT货件明细存在重复key, shipmentId={}, detailKey={}, keepId={}, skipId={}",
                        shipment.getId(), detailKey, duplicate.getId(), detail.getId());
            }
        }

        Map<String, Integer> receiveQtyByGoodsId = buildReceiveQtyByGoodsId(inboundOrder.getReceivedBatches());
        LocalDateTime receiveTime = inboundOrder.getUpdatedTime() == null ? LocalDateTime.now() : inboundOrder.getUpdatedTime();
        Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMapping = buildSkuMapping(inboundOrder);
        List<TiktokFbtDTO.PlannedGoodDTO> mergedPlannedGoods = mergePlannedGoods(inboundOrder.getPlannedGoods());
        log.info("FBT货件同步明细开始, shipmentId={}, inboundOrderId={}, existsSize={}, plannedGoodsSize={}, mergedPlannedGoodsSize={}",
                shipment.getId(),
                inboundOrder.getInboundOrderId(),
                exists.size(),
                inboundOrder.getPlannedGoods().size(),
                mergedPlannedGoods.size());

        for (TiktokFbtDTO.PlannedGoodDTO plannedGood : mergedPlannedGoods) {
            if (plannedGood == null) {
                continue;
            }
            String detailKey = buildDetailKey(plannedGood);
            Integer receiveQty = receiveQtyByGoodsId.get(plannedGood.getGoodsId());
            String msku = resolveMsku(plannedGood);
            String fnSku = StrUtil.blankToDefault(msku, "");
            SkuMappingDTO.MappingSkuViewDTO mappingDTO = findMappingByFnSkuAndMsku(skuMapping, fnSku, msku);
            String resolvedSkuNo = resolveDetailSkuNo(mappingDTO);
            if (StrUtil.isNotBlank(detailKey)) {
                FbaShipmentDetailEntity detail = existsMap.get(detailKey);
                if (detail != null) {
                    detail.setAsin(StrUtil.blankToDefault(plannedGood.getGoodsId(), ""));
                    detail.setMsku(msku);
                    detail.setFnSku(fnSku);
                    detail.setDeclareQty(plannedGood.getQuantity());
                    if (mappingDTO != null) {
                        detail.setSkuId(mappingDTO.getProductSkuId());
                        detail.setSkuNo(resolveDetailSkuNo(mappingDTO));
                    } else if (shouldClearFallbackSku(detail, msku, fnSku, plannedGood.getGoodsId())) {
                        detail.setSkuId(null);
                        detail.setSkuNo("");
                    }
                    detail.setDeliveryQty(0);
                    detail.setPlatformProductName(plannedGood.getName());
                    detail.setIsCombination(false);
                    if (receiveQty != null) {
                        detail.setReceiveQty(receiveQty);
                        detail.setReceiveDate(receiveTime);
                    }
                    int deliveryQty = ObjectUtil.defaultIfNull(detail.getDeliveryQty(), 0);
                    int receivedQty = ObjectUtil.defaultIfNull(detail.getReceiveQty(), 0);
                    detail.setDiffQty(receivedQty - deliveryQty);
                    fbtInboundRepository.updateShipmentDetail(detail);
                    continue;
                }
            }
            FbaShipmentDetailEntity detail = new FbaShipmentDetailEntity();
            detail.setMainId(shipment.getId());
            detail.setAsin(StrUtil.blankToDefault(plannedGood.getGoodsId(), ""));
            detail.setMsku(msku);
            detail.setFnSku(fnSku);
            if (mappingDTO != null) {
                detail.setSkuId(mappingDTO.getProductSkuId());
            }
            detail.setSkuNo(resolvedSkuNo);
            detail.setDeclareQty(plannedGood.getQuantity());
            detail.setDeliveryQty(0);
            detail.setPlatformProductName(plannedGood.getName());
            detail.setIsCombination(false);
            if (receiveQty != null) {
                detail.setReceiveQty(receiveQty);
                detail.setReceiveDate(receiveTime);
            }
            int deliveryQty = ObjectUtil.defaultIfNull(detail.getDeliveryQty(), 0);
            int receivedQty = ObjectUtil.defaultIfNull(detail.getReceiveQty(), 0);
            detail.setDiffQty(receivedQty - deliveryQty);
            log.info("FBT货件新增明细, shipmentId={}, inboundOrderId={}, goodsId={}, referenceCode={}, msku={}, fnSku={}, declareQty={}",
                    shipment.getId(),
                    inboundOrder.getInboundOrderId(),
                    plannedGood.getGoodsId(),
                    plannedGood.getReferenceCode(),
                    msku,
                    fnSku,
                    detail.getDeclareQty());
            fbtInboundRepository.saveShipmentDetail(detail);
            if (StrUtil.isNotBlank(detailKey)) {
                existsMap.put(detailKey, detail);
            }
        }
    }

    private Map<String, Integer> buildReceiveQtyByGoodsId(List<TiktokFbtDTO.ReceivedBatchDTO> receivedBatches) {
        Map<String, Integer> result = new HashMap<>();
        if (receivedBatches == null || receivedBatches.isEmpty()) {
            return result;
        }
        for (TiktokFbtDTO.ReceivedBatchDTO batch : receivedBatches) {
            if (batch == null || StrUtil.isBlank(batch.getGoodsId())) {
                continue;
            }
            int qty = batch.getTotalQuantity() == null ? 0 : batch.getTotalQuantity();
            if (qty <= 0) {
                int normal = batch.getNormalQuantity() == null ? 0 : batch.getNormalQuantity();
                int defective = batch.getDefectiveQuantity() == null ? 0 : batch.getDefectiveQuantity();
                qty = normal + defective;
            }
            result.put(batch.getGoodsId(), result.getOrDefault(batch.getGoodsId(), 0) + qty);
        }
        return result;
    }

    private String buildDetailKey(FbaShipmentDetailEntity detail) {
        if (detail == null) {
            return null;
        }
        return buildDetailKey(resolveSourceGoodsKey(detail.getAsin(), detail.getFnSku()), detail.getMsku());
    }

    private String buildDetailKey(TiktokFbtDTO.PlannedGoodDTO plannedGood) {
        if (plannedGood == null) {
            return null;
        }
        return buildDetailKey(resolveSourceGoodsKey(plannedGood.getGoodsId(), firstSkuId(plannedGood.getSkuIds())),
                resolveMsku(plannedGood));
    }

    private String buildDetailKey(String sourceGoodsKey, String msku) {
        if (StrUtil.isBlank(sourceGoodsKey) || StrUtil.isBlank(msku)) {
            return null;
        }
        return StrUtil.format("{}#{}", sourceGoodsKey, msku);
    }

    private String resolveSourceGoodsKey(String primary, String fallback) {
        if (StrUtil.isNotBlank(primary)) {
            return primary;
        }
        return StrUtil.blankToDefault(fallback, null);
    }

    private List<TiktokFbtDTO.PlannedGoodDTO> mergePlannedGoods(List<TiktokFbtDTO.PlannedGoodDTO> plannedGoods) {
        if (CollUtil.isEmpty(plannedGoods)) {
            return Collections.emptyList();
        }
        Map<String, TiktokFbtDTO.PlannedGoodDTO> result = new LinkedHashMap<>();
        for (TiktokFbtDTO.PlannedGoodDTO plannedGood : plannedGoods) {
            if (plannedGood == null) {
                continue;
            }
            String detailKey = buildDetailKey(plannedGood);
            if (StrUtil.isBlank(detailKey)) {
                result.put(UUID.randomUUID().toString(), copyPlannedGood(plannedGood));
                continue;
            }
            TiktokFbtDTO.PlannedGoodDTO exists = result.get(detailKey);
            if (exists == null) {
                result.put(detailKey, copyPlannedGood(plannedGood));
                continue;
            }
            exists.setQuantity(ObjectUtil.defaultIfNull(exists.getQuantity(), 0)
                    + ObjectUtil.defaultIfNull(plannedGood.getQuantity(), 0));
        }
        return new ArrayList<>(result.values());
    }

    private TiktokFbtDTO.PlannedGoodDTO copyPlannedGood(TiktokFbtDTO.PlannedGoodDTO plannedGood) {
        TiktokFbtDTO.PlannedGoodDTO copied = new TiktokFbtDTO.PlannedGoodDTO();
        copied.setGoodsId(plannedGood.getGoodsId());
        copied.setReferenceCode(plannedGood.getReferenceCode());
        copied.setName(plannedGood.getName());
        copied.setQuantity(plannedGood.getQuantity());
        copied.setSkuIds(plannedGood.getSkuIds() == null ? new ArrayList<>() : new ArrayList<>(plannedGood.getSkuIds()));
        return copied;
    }

    private String firstSkuId(List<String> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return null;
        }
        for (String skuId : skuIds) {
            if (StrUtil.isNotBlank(skuId)) {
                return skuId;
            }
        }
        return null;
    }

    private String resolveMsku(TiktokFbtDTO.PlannedGoodDTO plannedGood) {
        if (plannedGood == null) {
            return null;
        }
        if (StrUtil.isNotBlank(plannedGood.getReferenceCode())) {
            return plannedGood.getReferenceCode();
        }
        return firstSkuId(plannedGood.getSkuIds());
    }

    private Map<String, SkuMappingDTO.MappingSkuViewDTO> buildSkuMapping(TiktokFbtDTO.InboundOrderDTO inboundOrder) {
        Map<String, SkuMappingDTO.MappingSkuViewDTO> result = new HashMap<>();
        if (inboundOrder == null || inboundOrder.getPlannedGoods() == null || inboundOrder.getPlannedGoods().isEmpty()) {
            return result;
        }
        List<String> platformSkuNoList = inboundOrder.getPlannedGoods().stream()
                .filter(Objects::nonNull)
                .flatMap(g -> java.util.stream.Stream.of(g.getGoodsId(), resolveMsku(g)))
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (inboundOrder.getReceivedBatches() != null && !inboundOrder.getReceivedBatches().isEmpty()) {
            inboundOrder.getReceivedBatches().stream()
                    .filter(Objects::nonNull)
                    .map(TiktokFbtDTO.ReceivedBatchDTO::getGoodsId)
                    .filter(StrUtil::isNotBlank)
                    .forEach(platformSkuNoList::add);
            platformSkuNoList = platformSkuNoList.stream().distinct().collect(Collectors.toList());
        }
        if (platformSkuNoList.isEmpty()) {
            return result;
        }
        OverseasProviderEntity provider = listAuthorizedFbtProviderMapByShopId().get(inboundOrder.getShopId());
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(OmsPlatformEnum.FBT.getCode());
        paramDTO.setType(RuleTypeEnum.WAREHOUSE.getCode());
        if (provider == null || StrUtil.isBlank(provider.getId())) {
            return result;
        }
        paramDTO.setAuthId(provider.getId());
        paramDTO.setPlatformSkuNoList(platformSkuNoList);
        List<SkuMappingDTO.MappingSkuViewDTO> mappingList = skuMappingFeign.listByPlatformSkuNoAndPlatform(paramDTO);
        if (mappingList == null || mappingList.isEmpty()) {
            return result;
        }
        for (SkuMappingDTO.MappingSkuViewDTO mapping : mappingList) {
            if (mapping != null && StrUtil.isNotBlank(mapping.getPlatformSkuNo())) {
                result.putIfAbsent(mapping.getPlatformSkuNo(), mapping);
            }
            if (mapping != null && StrUtil.isNotBlank(mapping.getPlatformFnSku())) {
                result.putIfAbsent(mapping.getPlatformFnSku(), mapping);
            }
        }
        return result;
    }

    private SkuMappingDTO.MappingSkuViewDTO findMappingByFnSkuAndMsku(Map<String, SkuMappingDTO.MappingSkuViewDTO> mappingMap,
                                                                      String fnSku,
                                                                      String msku) {
        if (mappingMap == null || mappingMap.isEmpty()) {
            return null;
        }
        if (StrUtil.isNotBlank(msku) && mappingMap.containsKey(msku)) {
            return mappingMap.get(msku);
        }
        if (StrUtil.isNotBlank(fnSku) && mappingMap.containsKey(fnSku)) {
            return mappingMap.get(fnSku);
        }
        return null;
    }

    private String resolveDetailSkuNo(SkuMappingDTO.MappingSkuViewDTO mappingDTO) {
        if (mappingDTO != null && StrUtil.isNotBlank(mappingDTO.getProductSkuNo())) {
            return mappingDTO.getProductSkuNo();
        }
        return "";
    }

    private boolean shouldClearFallbackSku(FbaShipmentDetailEntity detail,
                                           String msku,
                                           String fnSku,
                                           String goodsId) {
        if (detail == null || StrUtil.isNotBlank(detail.getSkuId()) || StrUtil.isBlank(detail.getSkuNo())) {
            return false;
        }
        return StrUtil.equalsAny(detail.getSkuNo(), StrUtil.blankToDefault(msku, ""), StrUtil.blankToDefault(fnSku, ""), StrUtil.blankToDefault(goodsId, ""));
    }

    private void syncInventoryRecord(FbaShipmentEntity shipment, TiktokFbtDTO.InboundOrderDTO inboundOrder) {
        List<TiktokFbtDTO.InventoryRecordDTO> records = tiktokFbtApiService.queryInventoryRecords(
                inboundOrder.getShopId(),
                inboundOrder.getGoodsIds(),
                inboundOrder.getFbtWarehouseIds(),
                null,
                null
        );
        for (TiktokFbtDTO.InventoryRecordDTO record : records) {
            if (!StrUtil.equals(inboundOrder.getInboundOrderId(), record.getInboundOrderId())) {
                continue;
            }
            handleInventoryRecord(inboundOrder.getShopId(), record);
        }
    }

    private boolean handleInventoryRecord(String shopId, TiktokFbtDTO.InventoryRecordDTO record) {
        if (record == null || StrUtil.isBlank(record.getInboundOrderId())) {
            return false;
        }
        FbaShipmentEntity shipment = fbtInboundRepository.findShipmentByInboundOrderId(record.getInboundOrderId());
        if (shipment == null) {
            log.info("FBT库存流水跳过，未找到货件, shopId={}, inboundOrderId={}, recordId={}",
                    shopId, record.getInboundOrderId(), record.getRecordId());
            return false;
        }
        String idempotentRecordId = buildRecordId(record);
        if (fbtInboundRepository.existsInventoryRecord(idempotentRecordId)) {
            return false;
        }
        applyOverseasInventory(record);
        FbaShipmentReceiveEntity receiveEntity = buildReceiveEntity(shipment, record, idempotentRecordId);
        if (StrUtil.isBlank(receiveEntity.getFnSku()) || StrUtil.isBlank(receiveEntity.getMsku())) {
            log.warn("FBT库存流水跳过，未匹配到货件明细, inboundOrderId={}, recordId={}, skuCode={}, goodsId={}",
                    record.getInboundOrderId(), record.getRecordId(), record.getSkuCode(), record.getGoodsId());
            return false;
        }
        return Boolean.TRUE.equals(fbaShipmentReceiveService.saveAndCheckTransfer(Collections.singletonList(receiveEntity), shipment));
    }

    private void applyOverseasInventory(TiktokFbtDTO.InventoryRecordDTO record) {
        Map<String, OverseasProviderEntity> providerMap = listAuthorizedFbtProviderMapByShopId();
        OverseasProviderEntity provider = providerMap.get(record.getShopId());
        applyOverseasInventory(record, provider);
    }

    private void applyOverseasInventory(TiktokFbtDTO.InventoryRecordDTO record, OverseasProviderEntity provider) {
        if (StrUtil.isBlank(record.getWarehouseCode()) || StrUtil.isBlank(record.getSkuCode())) {
            return;
        }
        InventoryWarehouseContext warehouseContext = resolveInventoryWarehouse(
                provider,
                record.getShopId(),
                record.getWarehouseCode(),
                null);
        if (warehouseContext == null || StrUtil.isBlank(warehouseContext.getWarehouseCode())) {
            return;
        }
        String providerId = provider == null ? null : provider.getId();
        OverseasInventoryEntity inventory = findInventoryForUpsert(
                providerId,
                warehouseContext.getWarehouseCode(),
                record.getSkuCode());
        if (inventory == null) {
            inventory = new OverseasInventoryEntity();
            inventory.setWarehouseCode(warehouseContext.getWarehouseCode());
            inventory.setPlatformSku(record.getSkuCode());
            inventory.setPlatformSkuName(record.getSkuCode());
            inventory.setSellableQty(Math.max(record.getDeltaQty(), 0));
            inventory.setDictPlatform(PlatformEnum.FBT.getName());
            inventory.setDownloadTime(LocalDateTime.now());
            inventory.setOverseasProviderId(providerId);
            inventory.setName(StrUtil.blankToDefault(
                    warehouseContext.getWarehouseName(),
                    StrUtil.blankToDefault(record.getWarehouseCode(), provider == null ? null : provider.getShortName())));
            fbtInboundRepository.saveOverseasInventory(inventory);
            return;
        }
        inventory.setWarehouseCode(warehouseContext.getWarehouseCode());
        int oldQty = inventory.getSellableQty() == null ? 0 : inventory.getSellableQty();
        int newQty = oldQty + (record.getDeltaQty() == null ? 0 : record.getDeltaQty());
        inventory.setSellableQty(Math.max(newQty, 0));
        inventory.setDownloadTime(LocalDateTime.now());
        if (StrUtil.isBlank(inventory.getOverseasProviderId())) {
            inventory.setOverseasProviderId(providerId);
        }
        if (StrUtil.isBlank(inventory.getName())) {
            inventory.setName(StrUtil.blankToDefault(warehouseContext.getWarehouseName(), warehouseContext.getWarehouseCode()));
        }
        fbtInboundRepository.updateOverseasInventory(inventory);
    }

    private OverseasInventoryEntity upsertInventorySnapshot(TiktokFbtDTO.InventorySnapshotDTO snapshot, OverseasProviderEntity provider) {
        if (snapshot == null || StrUtil.isBlank(snapshot.getWarehouseCode()) || StrUtil.isBlank(snapshot.getSkuCode())) {
            return null;
        }
        String providerId = provider == null ? null : provider.getId();
        InventoryWarehouseContext warehouseContext = resolveInventoryWarehouse(
                provider,
                snapshot.getShopId(),
                snapshot.getWarehouseCode(),
                snapshot.getWarehouseName());
        if (warehouseContext == null || StrUtil.isBlank(warehouseContext.getWarehouseCode())) {
            return null;
        }
        OverseasProviderWarehouseEntity providerWarehouse = warehouseContext.getProviderWarehouse();
        OverseasInventoryEntity inventory = findInventoryForUpsert(
                providerId,
                warehouseContext.getWarehouseCode(),
                snapshot.getSkuCode());
        if (inventory == null) {
            inventory = new OverseasInventoryEntity();
            inventory.setWarehouseCode(warehouseContext.getWarehouseCode());
            inventory.setPlatformSku(snapshot.getSkuCode());
        }
        inventory.setWarehouseCode(warehouseContext.getWarehouseCode());
        inventory.setDictPlatform(PlatformEnum.FBT.getName());
        if (StrUtil.isNotBlank(providerId) && StrUtil.isBlank(inventory.getOverseasProviderId())) {
            inventory.setOverseasProviderId(providerId);
        }
        inventory.setName(StrUtil.blankToDefault(warehouseContext.getWarehouseName(), warehouseContext.getWarehouseCode()));
        inventory.setPlatformSkuName(StrUtil.blankToDefault(snapshot.getGoodsName(), snapshot.getSkuCode()));
        fillInventorySkuMapping(inventory, snapshot, provider, providerWarehouse);
        inventory.setSellableQty(defaultZero(snapshot.getAvailableQty()));
        inventory.setReservedQty(defaultZero(snapshot.getReservedQty()));
        inventory.setFrozenQty(defaultZero(snapshot.getReservedQty()));
        inventory.setUnsellableQty(defaultZero(snapshot.getUnfulfillableQty()));
        inventory.setDeliverOnwayQty(defaultZero(snapshot.getInTransitQty()));
        LocalDateTime downloadTime = snapshot.getUpdatedTime() == null ? LocalDateTime.now() : snapshot.getUpdatedTime();
        inventory.setDownloadTime(downloadTime);
        if (StrUtil.isBlank(inventory.getId())) {
            fbtInboundRepository.saveOverseasInventory(inventory);
        } else {
            fbtInboundRepository.updateOverseasInventory(inventory);
        }
        return inventory;
    }

    private TiktokFbtDTO.InventorySnapshotDTO buildMergedSnapshotFromDmpThirdInventory(TiktokFbtDTO.InventorySnapshotDTO sourceSnapshot,
                                                                                       String authId,
                                                                                       String shopId,
                                                                                       OverseasProviderWarehouseEntity shopBoundWarehouse) {
        TiktokFbtDTO.InventorySnapshotDTO mergedSnapshot = new TiktokFbtDTO.InventorySnapshotDTO();
        mergedSnapshot.setShopId(StrUtil.blankToDefault(shopId, sourceSnapshot.getShopId()));
        mergedSnapshot.setWarehouseCode(shopBoundWarehouse.getPlatformWarehouseCode());
        mergedSnapshot.setWarehouseName(firstMeaningful(
                shopBoundWarehouse.getPlatformWarehouseName(),
                shopBoundWarehouse.getWarehouseName(),
                sourceSnapshot.getWarehouseName(),
                shopBoundWarehouse.getPlatformWarehouseCode()));
        mergedSnapshot.setSkuCode(sourceSnapshot.getSkuCode());
        mergedSnapshot.setGoodsId(sourceSnapshot.getGoodsId());
        mergedSnapshot.setGoodsName(sourceSnapshot.getGoodsName());
        mergedSnapshot.setAvailableQty(0);
        mergedSnapshot.setReservedQty(0);
        mergedSnapshot.setUnfulfillableQty(0);
        mergedSnapshot.setInTransitQty(0);
        mergedSnapshot.setUpdatedTime(sourceSnapshot.getUpdatedTime());

        List<DmpThirdInventoryEntity> dmpInventoryList = FeignQuery.create(DmpThirdInventoryEntity.class)
                .eq(DmpThirdInventoryEntity::getSourcePlatform, OmsPlatformEnum.FBT.getCode())
                .eq(DmpThirdInventoryEntity::getAuthId, authId)
                .eq(DmpThirdInventoryEntity::getProductSku, sourceSnapshot.getSkuCode())
                .list();
        if (CollUtil.isEmpty(dmpInventoryList)) {
            mergedSnapshot.setUpdatedTime(mergedSnapshot.getUpdatedTime() == null ? LocalDateTime.now() : mergedSnapshot.getUpdatedTime());
            return mergedSnapshot;
        }
        for (DmpThirdInventoryEntity item : dmpInventoryList) {
            if (item == null) {
                continue;
            }
            mergedSnapshot.setAvailableQty(defaultZero(mergedSnapshot.getAvailableQty()) + defaultZero(item.getSellable()));
            mergedSnapshot.setReservedQty(defaultZero(mergedSnapshot.getReservedQty()) + defaultZero(item.getReserved()));
            mergedSnapshot.setUnfulfillableQty(defaultZero(mergedSnapshot.getUnfulfillableQty()) + defaultZero(item.getUnsellable()));
            mergedSnapshot.setInTransitQty(defaultZero(mergedSnapshot.getInTransitQty()) + resolveDmpInTransitQty(item));
            mergedSnapshot.setUpdatedTime(laterDateTime(mergedSnapshot.getUpdatedTime(), item.getUpdateTime()));
        }
        return mergedSnapshot;
    }

    private List<TiktokFbtDTO.InventorySnapshotDTO> aggregateInventorySnapshots(String shopId,
                                                                                List<TiktokFbtDTO.InventorySnapshotDTO> snapshots,
                                                                                OverseasProviderEntity provider) {
        if (CollUtil.isEmpty(snapshots)) {
            return Collections.emptyList();
        }
        LinkedHashMap<String, TiktokFbtDTO.InventorySnapshotDTO> aggregated = new LinkedHashMap<>();
        for (TiktokFbtDTO.InventorySnapshotDTO snapshot : snapshots) {
            if (snapshot == null || StrUtil.isBlank(snapshot.getSkuCode())) {
                continue;
            }
            InventoryWarehouseContext warehouseContext = resolveInventoryWarehouse(
                    provider,
                    shopId,
                    snapshot.getWarehouseCode(),
                    snapshot.getWarehouseName());
            if (warehouseContext == null || StrUtil.isBlank(warehouseContext.getWarehouseCode())) {
                continue;
            }
            String key = StrUtil.format("{}#{}", warehouseContext.getWarehouseCode(), snapshot.getSkuCode());
            TiktokFbtDTO.InventorySnapshotDTO aggregatedSnapshot = aggregated.computeIfAbsent(key, k -> {
                TiktokFbtDTO.InventorySnapshotDTO item = new TiktokFbtDTO.InventorySnapshotDTO();
                item.setShopId(StrUtil.blankToDefault(shopId, snapshot.getShopId()));
                item.setWarehouseCode(warehouseContext.getWarehouseCode());
                item.setWarehouseName(warehouseContext.getWarehouseName());
                item.setSkuCode(snapshot.getSkuCode());
                item.setGoodsId(snapshot.getGoodsId());
                item.setGoodsName(snapshot.getGoodsName());
                item.setAvailableQty(0);
                item.setReservedQty(0);
                item.setUnfulfillableQty(0);
                item.setInTransitQty(0);
                item.setUpdatedTime(snapshot.getUpdatedTime());
                return item;
            });
            aggregatedSnapshot.setAvailableQty(defaultZero(aggregatedSnapshot.getAvailableQty()) + defaultZero(snapshot.getAvailableQty()));
            aggregatedSnapshot.setReservedQty(defaultZero(aggregatedSnapshot.getReservedQty()) + defaultZero(snapshot.getReservedQty()));
            aggregatedSnapshot.setUnfulfillableQty(defaultZero(aggregatedSnapshot.getUnfulfillableQty()) + defaultZero(snapshot.getUnfulfillableQty()));
            aggregatedSnapshot.setInTransitQty(defaultZero(aggregatedSnapshot.getInTransitQty()) + defaultZero(snapshot.getInTransitQty()));
            if (StrUtil.isBlank(aggregatedSnapshot.getGoodsId()) && StrUtil.isNotBlank(snapshot.getGoodsId())) {
                aggregatedSnapshot.setGoodsId(snapshot.getGoodsId());
            }
            if (StrUtil.isBlank(aggregatedSnapshot.getGoodsName()) && StrUtil.isNotBlank(snapshot.getGoodsName())) {
                aggregatedSnapshot.setGoodsName(snapshot.getGoodsName());
            }
            aggregatedSnapshot.setUpdatedTime(laterDateTime(aggregatedSnapshot.getUpdatedTime(), snapshot.getUpdatedTime()));
        }
        return new ArrayList<>(aggregated.values());
    }

    private void cleanupSnapshotInventories(OverseasProviderEntity provider,
                                            List<OverseasInventoryEntity> updatedInventories) {
        if (provider == null || StrUtil.isBlank(provider.getId())) {
            return;
        }
        List<OverseasInventoryEntity> snapshotList = updatedInventories == null
                ? Collections.emptyList()
                : updatedInventories;
        Set<String> validInventoryIds = snapshotList.stream()
                .filter(Objects::nonNull)
                .map(OverseasInventoryEntity::getId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        List<OverseasInventoryEntity> inventoryList = fbtInboundRepository.listOverseasInventoryByProvider(
                provider.getId(),
                PlatformEnum.FBT.getName());
        if (CollUtil.isEmpty(inventoryList)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (OverseasInventoryEntity inventory : inventoryList) {
            if (inventory == null || StrUtil.isBlank(inventory.getId())) {
                continue;
            }
            if (validInventoryIds.contains(inventory.getId())) {
                continue;
            }
            markInventoryDeleted(inventory, now);
        }
    }

    private void cleanupMergedSkuInventories(OverseasProviderEntity provider,
                                             String skuCode,
                                             String keepInventoryId) {
        if (provider == null || StrUtil.isBlank(provider.getId()) || StrUtil.isBlank(skuCode)) {
            return;
        }
        Set<String> providerWarehouseCodes = listProviderWarehouseCodes(provider);
        List<OverseasInventoryEntity> inventoryList = fbtInboundRepository.listOverseasInventoryByPlatformAndSku(
                PlatformEnum.FBT.getName(),
                skuCode);
        if (CollUtil.isEmpty(inventoryList)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (OverseasInventoryEntity inventory : inventoryList) {
            if (inventory == null) {
                continue;
            }
            boolean sameProviderInventory = StrUtil.equals(provider.getId(), inventory.getOverseasProviderId());
            boolean legacyProviderInventory = StrUtil.isBlank(inventory.getOverseasProviderId())
                    && providerWarehouseCodes.contains(inventory.getWarehouseCode());
            if (!sameProviderInventory && !legacyProviderInventory) {
                continue;
            }
            if (StrUtil.isNotBlank(keepInventoryId) && StrUtil.equals(keepInventoryId, inventory.getId())) {
                continue;
            }
            markInventoryDeleted(inventory, now);
        }
    }

    private void markInventoryDeleted(OverseasInventoryEntity inventory, LocalDateTime now) {
        if (inventory == null) {
            return;
        }
        inventory.setSellableQty(0);
        inventory.setReservedQty(0);
        inventory.setFrozenQty(0);
        inventory.setUnsellableQty(0);
        inventory.setDeliverOnwayQty(0);
        inventory.setDownloadTime(now);
        inventory.setIsDeleted(true);
        fbtInboundRepository.updateOverseasInventory(inventory);
    }

    private OverseasInventoryEntity findInventoryForUpsert(String providerId,
                                                           String warehouseCode,
                                                           String skuCode) {
        if (StrUtil.isNotBlank(providerId) && StrUtil.isNotBlank(skuCode)) {
            List<OverseasInventoryEntity> providerInventoryList = fbtInboundRepository.listOverseasInventoryByProviderAndSku(
                    providerId,
                    PlatformEnum.FBT.getName(),
                    skuCode);
            OverseasInventoryEntity providerInventory = selectPreferredInventory(providerInventoryList, warehouseCode);
            if (providerInventory != null) {
                return providerInventory;
            }
        }
        if (StrUtil.isBlank(warehouseCode) || StrUtil.isBlank(skuCode)) {
            return null;
        }
        return fbtInboundRepository.findOverseasInventory(warehouseCode, skuCode);
    }

    private OverseasInventoryEntity selectPreferredInventory(List<OverseasInventoryEntity> inventoryList,
                                                             String preferredWarehouseCode) {
        if (CollUtil.isEmpty(inventoryList)) {
            return null;
        }
        return inventoryList.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing((OverseasInventoryEntity item) -> !StrUtil.equals(preferredWarehouseCode, item.getWarehouseCode()))
                        .thenComparing(item -> item.getUpdateTime() == null ? LocalDateTime.MIN : item.getUpdateTime(), Comparator.reverseOrder()))
                .findFirst()
                .orElse(null);
    }

    private Set<String> listProviderWarehouseCodes(OverseasProviderEntity provider) {
        if (provider == null || StrUtil.isBlank(provider.getId())) {
            return new HashSet<>();
        }
        List<OverseasProviderWarehouseEntity> warehouseList = FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .eq(OverseasProviderWarehouseEntity::getMainId, provider.getId())
                .list();
        if (CollUtil.isEmpty(warehouseList)) {
            return new HashSet<>();
        }
        return warehouseList.stream()
                .filter(Objects::nonNull)
                .filter(item -> !Boolean.TRUE.equals(item.getDisabled()))
                .map(OverseasProviderWarehouseEntity::getPlatformWarehouseCode)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private InventoryWarehouseContext resolveInventoryWarehouse(OverseasProviderEntity provider,
                                                                String shopId,
                                                                String fallbackWarehouseCode,
                                                                String fallbackWarehouseName) {
        OverseasProviderWarehouseEntity shopBoundWarehouse = findShopBoundProviderWarehouse(provider, shopId);
        if (shopBoundWarehouse != null && StrUtil.isNotBlank(shopBoundWarehouse.getPlatformWarehouseCode())) {
            String warehouseName = firstMeaningful(
                    shopBoundWarehouse.getWarehouseName(),
                    shopBoundWarehouse.getPlatformWarehouseName(),
                    fallbackWarehouseName,
                    shopBoundWarehouse.getPlatformWarehouseCode());
            return new InventoryWarehouseContext(shopBoundWarehouse, shopBoundWarehouse.getPlatformWarehouseCode(), warehouseName);
        }
        OverseasProviderWarehouseEntity matchedWarehouse = findProviderWarehouse(provider, fallbackWarehouseCode, fallbackWarehouseName);
        if (matchedWarehouse != null && StrUtil.isNotBlank(matchedWarehouse.getPlatformWarehouseCode())) {
            String warehouseName = firstMeaningful(
                    matchedWarehouse.getPlatformWarehouseName(),
                    matchedWarehouse.getWarehouseName(),
                    fallbackWarehouseName,
                    matchedWarehouse.getPlatformWarehouseCode());
            return new InventoryWarehouseContext(matchedWarehouse, matchedWarehouse.getPlatformWarehouseCode(), warehouseName);
        }
        if (StrUtil.isBlank(fallbackWarehouseCode)) {
            return null;
        }
        return new InventoryWarehouseContext(null,
                fallbackWarehouseCode,
                firstMeaningful(fallbackWarehouseName, fallbackWarehouseCode));
    }

    private OverseasProviderWarehouseEntity findShopBoundProviderWarehouse(OverseasProviderEntity provider, String shopId) {
        if (provider == null || StrUtil.isBlank(provider.getId()) || StrUtil.isBlank(shopId)) {
            return null;
        }
        ShopInfoEntity shopInfo;
        try {
            shopInfo = shopInfoFeign.getShopInfoById(shopId);
        } catch (Exception e) {
            log.warn("查询FBT店铺绑定仓失败, shopId={}, err={}", shopId, e.getMessage());
            return null;
        }
        if (shopInfo == null || StrUtil.isBlank(shopInfo.getWarehouseId())) {
            return null;
        }
        List<OverseasProviderWarehouseEntity> warehouseList = FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .eq(OverseasProviderWarehouseEntity::getMainId, provider.getId())
                .eq(OverseasProviderWarehouseEntity::getWarehouseId, shopInfo.getWarehouseId())
                .list();
        if (CollUtil.isEmpty(warehouseList)) {
            return null;
        }
        return warehouseList.stream()
                .filter(Objects::nonNull)
                .filter(item -> !Boolean.TRUE.equals(item.getDisabled()))
                .filter(item -> StrUtil.isNotBlank(item.getPlatformWarehouseCode()))
                .findFirst()
                .orElse(null);
    }

    private OverseasProviderWarehouseEntity findProviderWarehouse(OverseasProviderEntity provider,
                                                                  String platformWarehouseCode,
                                                                  String platformWarehouseName) {
        if (provider == null || StrUtil.isBlank(provider.getId())) {
            return null;
        }
        List<OverseasProviderWarehouseEntity> warehouseList = FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .eq(OverseasProviderWarehouseEntity::getMainId, provider.getId())
                .list();
        if (CollUtil.isEmpty(warehouseList)) {
            return null;
        }
        return warehouseList.stream()
                .filter(Objects::nonNull)
                .filter(item -> !Boolean.TRUE.equals(item.getDisabled()))
                .sorted(Comparator.comparingInt(item -> {
                    if (StrUtil.isNotBlank(platformWarehouseCode)
                            && StrUtil.equalsIgnoreCase(platformWarehouseCode, item.getPlatformWarehouseCode())) {
                        return 0;
                    }
                    if (StrUtil.isNotBlank(platformWarehouseName)
                            && StrUtil.equals(platformWarehouseName, item.getPlatformWarehouseName())) {
                        return 1;
                    }
                    return 2;
                }))
                .filter(item -> (StrUtil.isNotBlank(platformWarehouseCode)
                        && StrUtil.equalsIgnoreCase(platformWarehouseCode, item.getPlatformWarehouseCode()))
                        || (StrUtil.isNotBlank(platformWarehouseName)
                        && StrUtil.equals(platformWarehouseName, item.getPlatformWarehouseName())))
                .findFirst()
                .orElse(null);
    }

    private void fillInventorySkuMapping(OverseasInventoryEntity inventory,
                                         TiktokFbtDTO.InventorySnapshotDTO snapshot,
                                         OverseasProviderEntity provider,
                                         OverseasProviderWarehouseEntity providerWarehouse) {
        if (inventory == null || snapshot == null || StrUtil.isBlank(snapshot.getSkuCode())) {
            return;
        }
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(OmsPlatformEnum.FBT.getCode());
        paramDTO.setType(RuleTypeEnum.WAREHOUSE.getCode());
        paramDTO.setIsExpire(false);
        paramDTO.setPlatformSkuNoList(Collections.singletonList(snapshot.getSkuCode()));
        if (provider != null && StrUtil.isNotBlank(provider.getId())) {
            paramDTO.setAuthId(provider.getId());
        }
        if (providerWarehouse != null && StrUtil.isNotBlank(providerWarehouse.getWarehouseId())) {
            paramDTO.setWarehouseIdList(Collections.singletonList(providerWarehouse.getWarehouseId()));
        }
        List<ListingInfoWithSkuMappingDTO> mappingList = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO);
        if (CollUtil.isEmpty(mappingList)
                && CollUtil.isNotEmpty(paramDTO.getWarehouseIdList())
                && StrUtil.isNotBlank(paramDTO.getAuthId())) {
            ListingInfoParamDTO fallbackParam = new ListingInfoParamDTO();
            fallbackParam.setPlatform(paramDTO.getPlatform());
            fallbackParam.setType(paramDTO.getType());
            fallbackParam.setIsExpire(paramDTO.getIsExpire());
            fallbackParam.setPlatformSkuNoList(paramDTO.getPlatformSkuNoList());
            fallbackParam.setAuthId(paramDTO.getAuthId());
            mappingList = skuMappingFeign.listingInfoWithSkuMappingList(fallbackParam);
        }
        if (CollUtil.isEmpty(mappingList)) {
            return;
        }
        ListingInfoWithSkuMappingDTO mappingDTO = selectInventoryMapping(mappingList, providerWarehouse);
        if (mappingDTO == null) {
            return;
        }
        if (StrUtil.isNotBlank(mappingDTO.getPlatformSkuName())) {
            inventory.setPlatformSkuName(mappingDTO.getPlatformSkuName().trim());
        }
        if (StrUtil.isNotBlank(mappingDTO.getProductName())) {
            inventory.setProductName(mappingDTO.getProductName().trim());
        }
        if (StrUtil.isNotBlank(mappingDTO.getProductSkuId())) {
            inventory.setSkuId(mappingDTO.getProductSkuId().trim());
        }
        if (StrUtil.isNotBlank(mappingDTO.getProductSkuNo())) {
            inventory.setSkuNo(mappingDTO.getProductSkuNo().trim());
        }
    }

    private ListingInfoWithSkuMappingDTO selectInventoryMapping(List<ListingInfoWithSkuMappingDTO> mappingList,
                                                                OverseasProviderWarehouseEntity providerWarehouse) {
        if (CollUtil.isEmpty(mappingList)) {
            return null;
        }
        if (providerWarehouse != null && StrUtil.isNotBlank(providerWarehouse.getWarehouseId())) {
            ListingInfoWithSkuMappingDTO warehouseMatched = mappingList.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> StrUtil.equals(providerWarehouse.getWarehouseId(), item.getWarehouseId()))
                    .findFirst()
                    .orElse(null);
            if (warehouseMatched != null) {
                return warehouseMatched;
            }
        }
        ListingInfoWithSkuMappingDTO allWarehouseMatched = mappingList.stream()
                .filter(Objects::nonNull)
                .filter(item -> Boolean.TRUE.equals(item.getHasMappingAll()))
                .findFirst()
                .orElse(null);
        if (allWarehouseMatched != null) {
            return allWarehouseMatched;
        }
        return mappingList.stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Integer resolveDmpInTransitQty(DmpThirdInventoryEntity dmpInventory) {
        if (dmpInventory == null) {
            return 0;
        }
        Integer transferOnway = dmpInventory.getTransferOnway();
        if (transferOnway != null) {
            return transferOnway;
        }
        return defaultZero(dmpInventory.getOnway());
    }

    private String buildInventoryKey(String warehouseCode, String skuCode) {
        return StrUtil.format("{}#{}", warehouseCode, skuCode);
    }

    private LocalDateTime laterDateTime(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }

    private static class InventoryWarehouseContext {
        private final OverseasProviderWarehouseEntity providerWarehouse;
        private final String warehouseCode;
        private final String warehouseName;

        private InventoryWarehouseContext(OverseasProviderWarehouseEntity providerWarehouse,
                                          String warehouseCode,
                                          String warehouseName) {
            this.providerWarehouse = providerWarehouse;
            this.warehouseCode = warehouseCode;
            this.warehouseName = warehouseName;
        }

        public OverseasProviderWarehouseEntity getProviderWarehouse() {
            return providerWarehouse;
        }

        public String getWarehouseCode() {
            return warehouseCode;
        }

        public String getWarehouseName() {
            return warehouseName;
        }
    }

    private FbaShipmentReceiveEntity buildReceiveEntity(FbaShipmentEntity shipment, TiktokFbtDTO.InventoryRecordDTO record, String idempotentRecordId) {
        FbaShipmentReceiveEntity entity = new FbaShipmentReceiveEntity();
        entity.setFbaShipmentId(shipment.getFbaShipmentId());
        entity.setMsku(record.getSkuCode());
        entity.setFnSku(record.getSkuCode());
        entity.setAsin(record.getGoodsId());
        entity.setReceiveQty(record.getDeltaQty());
        entity.setReceiveDate(record.getEventTime() == null ? LocalDateTime.now() : record.getEventTime());
        entity.setFulfillmentCenter(record.getWarehouseCode());
        entity.setSourcePlatform(PlatformEnum.FBT.getName());
        entity.setSourceType(SignSourceTypeEnum.API.getCode());
        entity.setHandleStatus(FbaReceiveHandleStatusEnum.NONE.getCode());
        entity.setUniqueMd5(idempotentRecordId);
        entity.setUniqueIndex(calculateUniqueIndex(record.getRecordId(), idempotentRecordId));
        bindDetailIfMatch(shipment, entity, record);
        return entity;
    }

    private String calculateUniqueIndex(String recordId, String uniqueMd5) {
        String seed = StrUtil.blankToDefault(recordId, uniqueMd5);
        if (StrUtil.isBlank(seed)) {
            return "0";
        }
        // fba_shipment_receive.unique_index 是 smallint，范围必须控制在 [0, 32767]
        return String.valueOf(seed.hashCode() & 0x7FFF);
    }

    private void bindDetailIfMatch(FbaShipmentEntity shipment, FbaShipmentReceiveEntity entity, TiktokFbtDTO.InventoryRecordDTO record) {
        List<FbaShipmentDetailEntity> details = fbtInboundRepository.listShipmentDetails(shipment.getId());
        if (details == null || details.isEmpty()) {
            entity.setDetailId("");
            return;
        }
        for (FbaShipmentDetailEntity detail : details) {
            if (detail == null) {
                continue;
            }
            boolean match = (StrUtil.isNotBlank(record.getSkuCode()) && StrUtil.equals(record.getSkuCode(), detail.getMsku()))
                    || (StrUtil.isNotBlank(record.getGoodsId()) && StrUtil.equals(record.getGoodsId(), detail.getFnSku()));
            if (match) {
                entity.setMsku(detail.getMsku());
                entity.setFnSku(detail.getFnSku());
                entity.setDetailId(detail.getId());
                entity.setSkuNo(detail.getSkuNo());
                entity.setSkuId(detail.getSkuId());
                if (StrUtil.isNotBlank(detail.getAsin())) {
                    entity.setAsin(detail.getAsin());
                }
                return;
            }
        }
        entity.setDetailId("");
    }

    private String buildRecordId(TiktokFbtDTO.InventoryRecordDTO record) {
        if (StrUtil.isNotBlank(record.getRecordId())) {
            return record.getRecordId();
        }
        return StrUtil.format("{}:{}:{}:{}",
                record.getInboundOrderId(),
                record.getSkuCode(),
                record.getDeltaQty(),
                record.getEventTime());
    }
}
