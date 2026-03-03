package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.TiktokFbtDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.enums.*;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.server.wms.service.FbtInboundService;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import com.erp.server.wms.service.TiktokFbtApiService;
import com.erp.server.wms.service.repository.FbtInboundRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
            List<TiktokFbtDTO.InventorySnapshotDTO> snapshots =
                    tiktokFbtApiService.queryInventorySnapshots(shopId, null, null);
            int handled = 0;
            for (TiktokFbtDTO.InventorySnapshotDTO snapshot : snapshots) {
                if (upsertInventorySnapshot(snapshot, provider)) {
                    handled++;
                }
            }
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
        if (snapshot == null || StrUtil.isBlank(snapshot.getWarehouseCode()) || StrUtil.isBlank(snapshot.getSkuCode())) {
            return false;
        }
        OverseasProviderEntity provider = null;
        if (StrUtil.isNotBlank(authId)) {
            provider = findAuthorizedFbtProviderById(authId);
        }
        if (provider == null && StrUtil.isNotBlank(snapshot.getShopId())) {
            provider = listAuthorizedFbtProviderMapByShopId().get(snapshot.getShopId());
        }
        return upsertInventorySnapshot(snapshot, provider);
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
        Map<String, String> accountToShopIdMap = listAuthorizedTiktokShops().stream()
                .filter(Objects::nonNull)
                .filter(shop -> !Boolean.TRUE.equals(shop.getDisabled()))
                .filter(shop -> StrUtil.isNotBlank(shop.getAccount()) && StrUtil.isNotBlank(shop.getId()))
                .collect(Collectors.toMap(
                        shop -> shop.getAccount().toLowerCase(Locale.ROOT),
                        ShopInfoEntity::getId,
                        (left, right) -> left,
                        LinkedHashMap::new));
        Map<String, OverseasProviderEntity> result = new LinkedHashMap<>();
        for (OverseasProviderEntity provider : providerList) {
            if (provider == null) {
                continue;
            }
            String shopId = resolveProviderShopId(provider, accountToShopIdMap);
            if (StrUtil.isBlank(shopId)) {
                continue;
            }
            result.putIfAbsent(shopId, provider);
        }
        return result;
    }

    private String resolveProviderShopId(OverseasProviderEntity provider, Map<String, String> accountToShopIdMap) {
        String shopId = firstMeaningful(
                provider.getAuthJson() == null ? null : provider.getAuthJson().get("shopId"));
        if (StrUtil.isNotBlank(shopId)) {
            return shopId;
        }

        String shopAccount = firstMeaningful(
                provider.getAuthJson() == null ? null : provider.getAuthJson().get("shopAccount"),
                provider.getPlatformAccount());
        if (StrUtil.isBlank(shopAccount)) {
            return null;
        }
        return accountToShopIdMap.get(shopAccount.toLowerCase(Locale.ROOT));
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
        String beforeStatus = current == null ? null : current.getDeliveryStatus();
        if (current == null) {
            current = createFbtInbound(inboundOrder);
            created = true;
        } else {
            updateFbtStatus(current, inboundOrder);
        }
        String afterStatus = current.getDeliveryStatus();
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
        List<ShopInfoEntity> tiktokFully = shopInfoFeign.listByParams(
                new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.TIK_TOK_FULLY.getCode(), null));
        Map<String, ShopInfoEntity> mergeMap = new LinkedHashMap<>();
        if (tiktok != null) {
            for (ShopInfoEntity shopInfo : tiktok) {
                if (shopInfo != null && StrUtil.isNotBlank(shopInfo.getId())) {
                    mergeMap.put(shopInfo.getId(), shopInfo);
                }
            }
        }
        if (tiktokFully != null) {
            for (ShopInfoEntity shopInfo : tiktokFully) {
                if (shopInfo != null && StrUtil.isNotBlank(shopInfo.getId())) {
                    mergeMap.put(shopInfo.getId(), shopInfo);
                }
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
        entity.setPlatformShipmentStatus(inboundOrder.getStatus());
        entity.setShipmentReceiveTime(LocalDateTime.now());
        fbtInboundRepository.updateShipment(entity);
        syncShipmentDetails(entity, inboundOrder);
    }

    private void syncShipmentDetails(FbaShipmentEntity shipment, TiktokFbtDTO.InboundOrderDTO inboundOrder) {
        if (shipment == null || StrUtil.isBlank(shipment.getId()) || inboundOrder == null
                || inboundOrder.getPlannedGoods() == null || inboundOrder.getPlannedGoods().isEmpty()) {
            return;
        }
        List<FbaShipmentDetailEntity> exists = fbtInboundRepository.listShipmentDetails(shipment.getId());
        Map<String, FbaShipmentDetailEntity> existsMap = new HashMap<>();
        Set<String> detailKeys = exists.stream()
                .map(this::buildDetailKey)
                .collect(Collectors.toSet());
        for (FbaShipmentDetailEntity detail : exists) {
            existsMap.put(buildDetailKey(detail), detail);
        }

        Map<String, Integer> receiveQtyByGoodsId = buildReceiveQtyByGoodsId(inboundOrder.getReceivedBatches());
        LocalDateTime receiveTime = inboundOrder.getUpdatedTime() == null ? LocalDateTime.now() : inboundOrder.getUpdatedTime();
        Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMapping = buildSkuMapping(inboundOrder);

        for (TiktokFbtDTO.PlannedGoodDTO plannedGood : inboundOrder.getPlannedGoods()) {
            if (plannedGood == null) {
                continue;
            }
            String detailKey = buildDetailKey(plannedGood);
            Integer receiveQty = receiveQtyByGoodsId.get(plannedGood.getGoodsId());
            String fnSku = plannedGood.getGoodsId();
            String msku = resolveMsku(plannedGood);
            SkuMappingDTO.MappingSkuViewDTO mappingDTO = findMappingByFnSkuAndMsku(skuMapping, fnSku, msku);
            String resolvedSkuNo = resolveDetailSkuNo(mappingDTO, msku, fnSku, plannedGood.getGoodsId());
            if (detailKeys.contains(detailKey)) {
                FbaShipmentDetailEntity detail = existsMap.get(detailKey);
                if (detail != null) {
                    if (mappingDTO != null && StrUtil.isNotBlank(mappingDTO.getPlatformSpuNo())) {
                        detail.setAsin(mappingDTO.getPlatformSpuNo());
                    } else if (StrUtil.isBlank(detail.getAsin())) {
                        detail.setAsin(plannedGood.getGoodsId());
                    }
                    detail.setMsku(msku);
                    detail.setFnSku(fnSku);
                    detail.setDeclareQty(plannedGood.getQuantity());
                    if (detail.getDeliveryQty() == null) {
                        detail.setDeliveryQty(plannedGood.getQuantity());
                    }
                    if (mappingDTO != null) {
                        detail.setSkuId(mappingDTO.getProductSkuId());
                    }
                    if (StrUtil.isBlank(detail.getSkuNo())) {
                        detail.setSkuNo(resolvedSkuNo);
                    }
                    detail.setPlatformProductName(plannedGood.getName());
                    detail.setIsCombination(false);
                    if (receiveQty != null) {
                        detail.setReceiveQty(receiveQty);
                        detail.setReceiveDate(receiveTime);
                    }
                    detail.setDiffQty(calcDiffQty(detail.getReceiveQty(), detail.getDeliveryQty()));
                    fbtInboundRepository.updateShipmentDetail(detail);
                }
                continue;
            }
            FbaShipmentDetailEntity detail = new FbaShipmentDetailEntity();
            detail.setMainId(shipment.getId());
            detail.setAsin(mappingDTO != null && StrUtil.isNotBlank(mappingDTO.getPlatformSpuNo()) ? mappingDTO.getPlatformSpuNo() : plannedGood.getGoodsId());
            detail.setMsku(msku);
            detail.setFnSku(fnSku);
            if (mappingDTO != null) {
                detail.setSkuId(mappingDTO.getProductSkuId());
            }
            detail.setSkuNo(resolvedSkuNo);
            detail.setDeclareQty(plannedGood.getQuantity());
            detail.setDeliveryQty(plannedGood.getQuantity());
            detail.setPlatformProductName(plannedGood.getName());
            detail.setIsCombination(false);
            if (receiveQty != null) {
                detail.setReceiveQty(receiveQty);
                detail.setReceiveDate(receiveTime);
            }
            detail.setDiffQty(calcDiffQty(detail.getReceiveQty(), detail.getDeliveryQty()));
            fbtInboundRepository.saveShipmentDetail(detail);
            detailKeys.add(detailKey);
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

    private Integer calcDiffQty(Integer receiveQty, Integer deliveryQty) {
        int receive = receiveQty == null ? 0 : receiveQty;
        int delivery = deliveryQty == null ? 0 : deliveryQty;
        return receive - delivery;
    }

    private String buildDetailKey(FbaShipmentDetailEntity detail) {
        return StrUtil.format("{}#{}",
                StrUtil.blankToDefault(detail.getFnSku(), ""),
                StrUtil.blankToDefault(detail.getMsku(), ""));
    }

    private String buildDetailKey(TiktokFbtDTO.PlannedGoodDTO plannedGood) {
        return StrUtil.format("{}#{}",
                StrUtil.blankToDefault(plannedGood.getGoodsId(), ""),
                StrUtil.blankToDefault(resolveMsku(plannedGood), ""));
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
        ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(inboundOrder.getShopId());
        if (shopInfo == null) {
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
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(StrUtil.blankToDefault(shopInfo.getDictPlatform(), PlatformDictEnum.TIK_TOK.getCode()));
        paramDTO.setShopIdList(Collections.singletonList(inboundOrder.getShopId()));
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

    private String resolveDetailSkuNo(SkuMappingDTO.MappingSkuViewDTO mappingDTO,
                                      String msku,
                                      String fnSku,
                                      String goodsId) {
        if (mappingDTO != null && StrUtil.isNotBlank(mappingDTO.getProductSkuNo())) {
            return mappingDTO.getProductSkuNo();
        }
        if (StrUtil.isNotBlank(msku)) {
            return msku;
        }
        if (StrUtil.isNotBlank(fnSku)) {
            return fnSku;
        }
        return StrUtil.blankToDefault(goodsId, "");
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
        String providerId = provider == null ? null : provider.getId();
        OverseasInventoryEntity inventory = fbtInboundRepository.findOverseasInventory(record.getWarehouseCode(), record.getSkuCode(), providerId);
        if (inventory == null) {
            inventory = new OverseasInventoryEntity();
            inventory.setWarehouseCode(record.getWarehouseCode());
            inventory.setPlatformSku(record.getSkuCode());
            inventory.setPlatformSkuName(record.getSkuCode());
            inventory.setSellableQty(Math.max(record.getDeltaQty(), 0));
            inventory.setDictPlatform(PlatformEnum.FBT.getName());
            inventory.setDownloadTime(LocalDateTime.now());
            inventory.setOverseasProviderId(providerId);
            if (provider != null && StrUtil.isNotBlank(provider.getShortName())) {
                inventory.setName(provider.getShortName());
            } else {
                inventory.setName(record.getWarehouseCode());
            }
            fbtInboundRepository.saveOverseasInventory(inventory);
            return;
        }
        int oldQty = inventory.getSellableQty() == null ? 0 : inventory.getSellableQty();
        int newQty = oldQty + (record.getDeltaQty() == null ? 0 : record.getDeltaQty());
        inventory.setSellableQty(Math.max(newQty, 0));
        inventory.setDownloadTime(LocalDateTime.now());
        if (StrUtil.isBlank(inventory.getOverseasProviderId())) {
            inventory.setOverseasProviderId(providerId);
        }
        if (StrUtil.isBlank(inventory.getName())) {
            inventory.setName(record.getWarehouseCode());
        }
        fbtInboundRepository.updateOverseasInventory(inventory);
    }

    private boolean upsertInventorySnapshot(TiktokFbtDTO.InventorySnapshotDTO snapshot, OverseasProviderEntity provider) {
        if (snapshot == null || StrUtil.isBlank(snapshot.getWarehouseCode()) || StrUtil.isBlank(snapshot.getSkuCode())) {
            return false;
        }
        String providerId = provider == null ? null : provider.getId();
        OverseasInventoryEntity inventory = fbtInboundRepository.findOverseasInventory(
                snapshot.getWarehouseCode(), snapshot.getSkuCode(), providerId);
        if (inventory == null) {
            inventory = new OverseasInventoryEntity();
            inventory.setWarehouseCode(snapshot.getWarehouseCode());
            inventory.setPlatformSku(snapshot.getSkuCode());
            inventory.setDictPlatform(PlatformEnum.FBT.getName());
            inventory.setOverseasProviderId(providerId);
        }
        inventory.setName(StrUtil.blankToDefault(snapshot.getWarehouseName(), snapshot.getWarehouseCode()));
        inventory.setPlatformSkuName(StrUtil.blankToDefault(snapshot.getGoodsName(), snapshot.getSkuCode()));
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
        return true;
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private FbaShipmentReceiveEntity buildReceiveEntity(FbaShipmentEntity shipment, TiktokFbtDTO.InventoryRecordDTO record, String idempotentRecordId) {
        FbaShipmentReceiveEntity entity = new FbaShipmentReceiveEntity();
        entity.setFbaShipmentId(shipment.getFbaShipmentId());
        entity.setMsku(record.getSkuCode());
        entity.setFnSku(record.getGoodsId());
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
