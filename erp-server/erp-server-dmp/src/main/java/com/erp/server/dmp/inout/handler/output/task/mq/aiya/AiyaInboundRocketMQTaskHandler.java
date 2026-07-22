package com.erp.server.dmp.inout.handler.output.task.mq.aiya;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformInboundDTO.Receiving;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.sdk.wms.aiya.dto.response.AiyaInboundResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 爱亚（AIYA/百世 GLINK）海外仓入库验货明细 DMP 输出 MQ 任务处理器。
 * <p>
 * 数据模型：爱亚 {@code GLINK_BATCH_QUERY_ASN_NOTIFY} 的 {@code asnItemReceiveDetails}
 * （{@link AiyaInboundResp.AsnItemReceiveDetailDTO}）同时包含收货流水（detailId 前缀 RV）与上架流水
 * （detailId 前缀 PV），二者数量重复；签收以「上架流水（PV）」为准，{@code putawayQty} 为上架数量，
 * {@code skuStatus} 区分良品（GOOD）/不良品（DAMAGE）。RV/PV 去重过滤在 InitHandler 完成，
 * {@code detail_list_json} 仅保留 PV 流水。因此：
 * <ol>
 *   <li>把 {@code detail_list_json} 中的上架流水逐行展开为签收流水 {@link Receiving}，
 *       {@code receiveQty} 取 {@code putawayQty}，{@code defectiveProductFlag} 取自 {@code skuStatus}，
 *       {@code thirdId} 由 {@code asnNumber_detailId_lineNo_sku_skuStatus} 组成以保证唯一（防重复拉取重复落库）；</li>
 *   <li>设置 {@code hasReceivedData=true}，让消费端
 *       {@code OverseasWarehouseInboundServiceImpl.handlePlatformMessage} 直接按流水落
 *       {@code overseas_warehouse_inbound_received}，并按良品/不良品分别生成直接调拨单、即时库存。</li>
 * </ol>
 * <p>
 * 幂等：与 wego 一致，消费端对爱亚按 {@code flow_id}（thirdId）强去重，避免时间窗口重叠重复拉取导致重复签收。
 * <p>
 * 多例：{@link Scope}({@code prototype})。
 */
@Service
@Slf4j
@Scope("prototype")
public class AiyaInboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String STORAGE_NAME = "dmp_third_inbound";

    /**
     * 爱亚货物状态：不良品。
     */
    private static final String SKU_STATUS_DAMAGE = "DAMAGE";

    /**
     * 爱亚入库单状态：入库单完成。
     */
    private static final String ASN_STATUS_FULFILLED = "Fulfilled";

    /**
     * 爱亚入库单状态：揽收成功。
     */
    private static final String ASN_STATUS_RECEIVED = "Received";

    /**
     * 爱亚入库单状态：入库单取消。
     */
    private static final String ASN_STATUS_VOIDED = "Voided";

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps =
                dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpThirdInboundEntity> dmpThirdInboundEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isNotEmpty(value) && STORAGE_NAME.equals(entry.getKey().getStorageName())) {
                for (BaseEntity v : value) {
                    DmpThirdInboundEntity dmpThirdInboundEntity = (DmpThirdInboundEntity) v;
                    dmpThirdInboundEntityMap.put(dmpThirdInboundEntity.getId(), dmpThirdInboundEntity);
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps =
                dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isNotEmpty(value) && STORAGE_NAME.equals(entry.getKey().getStorageName())) {
                for (BaseEntity v : value) {
                    changeIds.add(v.getId());
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpThirdInboundEntity dmpThirdInboundEntity = dmpThirdInboundEntityMap.get(changeId);
            if (dmpThirdInboundEntity == null) {
                log.warn("AiyaInbound: changeId={} 未在convert map中找到, cfgOutputId={}", changeId, cfgOutputId);
                continue;
            }
            PlatformInboundDTO platformInboundDTO = this.convert(dmpThirdInboundEntity, cfgOutputId);
            if (platformInboundDTO != null) {
                map.put(dmpThirdInboundEntity.getId(), JSON.toJSONString(platformInboundDTO));
            }
        }
        return map;
    }

    /**
     * 把 {@link DmpThirdInboundEntity} 转换为通用的 {@link PlatformInboundDTO}。
     *
     * @param dmpThirdInboundEntity DMP 层第三方入库实体
     * @param cfgOutputId           当前输出配置 id，用于命中黑名单时跳过推送
     * @return 通用平台入库 DTO；命中黑名单时返回 {@code null}
     */
    public PlatformInboundDTO convert(DmpThirdInboundEntity dmpThirdInboundEntity, String cfgOutputId) {
        if (this.validateDataBlack(dmpThirdInboundEntity, cfgOutputId)) {
            return null;
        }
        PlatformInboundDTO platformInboundDTO = BeanUtil.copyProperties(dmpThirdInboundEntity, PlatformInboundDTO.class);
        String sourcePlatform = dmpThirdInboundEntity.getSourcePlatform();
        // authId 取 nextLevelId（overseas_provider.id），供下游按 flowId+authId 幂等落签收记录
        platformInboundDTO.setAuthId(dmpThirdInboundEntity.getNextLevelId());
        platformInboundDTO.setPlatform(sourcePlatform);
        platformInboundDTO.setProvider(sourcePlatform);

        List<AiyaInboundResp.AsnItemReceiveDetailDTO> putawayDetails =
                parseAsnItems(dmpThirdInboundEntity.getId(), dmpThirdInboundEntity.getDetailListJson());

        platformInboundDTO.setReceivingStatus(this.convertStatus(dmpThirdInboundEntity.getReceivingStatus(), putawayDetails));

        List<Receiving> receivingDataList = buildReceivingList(putawayDetails);
        if (CollUtil.isNotEmpty(receivingDataList)) {
            platformInboundDTO.setHasReceivedData(true);
            platformInboundDTO.setReceivingDataList(receivingDataList);
            LocalDateTime latest = receivingDataList.stream()
                    .map(Receiving::getReceiveTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            platformInboundDTO.setDownloadTime(latest);
        } else {
            platformInboundDTO.setReceivingDataList(new ArrayList<>());
            platformInboundDTO.setDownloadTime(LocalDateTime.now());
        }

        this.groupBySku(platformInboundDTO, receivingDataList);
        return platformInboundDTO;
    }

    /**
     * 反序列化 {@code detail_list_json} 为爱亚上架流水（{@code asnItemReceiveDetails} 中 detailId 前缀为 PV 的记录，
     * InitHandler 已完成 RV/PV 去重过滤，仅保留上架流水）。
     * <p>
     * 空串属正常场景（尚无上架流水）；非空却解析失败视为签收数据不完整，抛出 {@link ServiceException}
     * 中止本次推送，避免「无流水签收」静默落库导致漏记/库存不同步。
     */
    private List<AiyaInboundResp.AsnItemReceiveDetailDTO> parseAsnItems(String inboundId, String detailListJson) {
        if (StringUtils.isBlank(detailListJson)) {
            return new ArrayList<>();
        }
        try {
            List<AiyaInboundResp.AsnItemReceiveDetailDTO> list =
                    JSON.parseArray(detailListJson, AiyaInboundResp.AsnItemReceiveDetailDTO.class);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            log.error("[爱亚入库] 解析 detail_list_json 失败，签收数据不完整，中止本次推送。inboundId={}, jsonLength={}, jsonSummary={}",
                    inboundId, detailListJson.length(), StringUtils.abbreviate(detailListJson, 500), e);
            throw new ServiceException(e, ApiError.WH_AIYA_INBOUND_DETAIL_JSON_PARSE_FAILED, inboundId);
        }
    }

    /**
     * 把爱亚上架流水（PV）逐行展开为签收流水。
     * <ul>
     *   <li>{@code productSku} 取 sku；{@code receiveQty} 取上架数量 putawayQty；</li>
     *   <li>{@code defectiveProductFlag} = (skuStatus == DAMAGE)，区分良品/不良品；</li>
     *   <li>{@code thirdId} = asnNumber_detailId_lineNo_sku_skuStatus，保证同一行重复拉取不重复落库；</li>
     *   <li>{@code receiveTime} 取 ASN 完成收货时间（PV 流水无收货时间，InitHandler 已回填到每行）。</li>
     * </ul>
     */
    private List<Receiving> buildReceivingList(List<AiyaInboundResp.AsnItemReceiveDetailDTO> putawayDetails) {
        List<Receiving> receivingList = new ArrayList<>();
        if (CollUtil.isEmpty(putawayDetails)) {
            return receivingList;
        }
        for (AiyaInboundResp.AsnItemReceiveDetailDTO item : putawayDetails) {
            if (item == null || StringUtils.isBlank(item.getSku())) {
                continue;
            }
            boolean defective = SKU_STATUS_DAMAGE.equalsIgnoreCase(item.getSkuStatus());
            Receiving receiving = new Receiving();
            receiving.setProductSku(item.getSku());
            receiving.setReceiveQty(item.getPutawayQty() == null ? 0 : item.getPutawayQty());
            receiving.setReceiveTime(resolveReceiveTime(item.getReceiveTime()));
            receiving.setDefectiveProductFlag(defective);
            receiving.setThirdId(buildThirdId(item));
            receivingList.add(receiving);
        }
        return receivingList;
    }

    /**
     * 组装签收流水唯一 ID：asnNumber_detailId_lineNo_sku_skuStatus。
     * <p>
     * 爱亚同一 PV 上架流水 detailId 会被多行共用，需叠加 lineNo/sku/skuStatus 保证每行唯一，
     * 与下游按 flow_id 强去重共同保证重复拉取不重复落库。
     */
    private String buildThirdId(AiyaInboundResp.AsnItemReceiveDetailDTO item) {
        return StringUtils.defaultString(item.getAsnNumber())
                + "_" + StringUtils.defaultString(item.getDetailId())
                + "_" + StringUtils.defaultString(item.getLineNo())
                + "_" + StringUtils.defaultString(item.getSku())
                + "_" + StringUtils.defaultString(item.getSkuStatus());
    }

    /**
     * 按 SKU 汇总实际验货数量（良品+不良品），写入 {@link PlatformInboundDTO#setItems(List)}，
     * 供消费端更新入库明细的累计签收数量。
     */
    private void groupBySku(PlatformInboundDTO dto, List<Receiving> receivingDataList) {
        if (CollUtil.isEmpty(receivingDataList)) {
            dto.setItems(new ArrayList<>());
            return;
        }
        Map<String, Integer> receivedQuantityMap = receivingDataList.stream()
                .filter(r -> StringUtils.isNotBlank(r.getProductSku()))
                .collect(Collectors.groupingBy(
                        Receiving::getProductSku,
                        Collectors.summingInt(r -> r.getReceiveQty() == null ? 0 : r.getReceiveQty())));
        List<PlatformInboundDTO.Item> items = receivedQuantityMap.entrySet().stream()
                .map(entry -> new PlatformInboundDTO.Item(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        dto.setItems(items);
    }

    /**
     * 入库单状态映射：以爱亚 ASN 状态为主（该字段仅用于展示，主表状态由消费端按收发差异计算）。
     */
    private String convertStatus(String aiyaStatus, List<AiyaInboundResp.AsnItemReceiveDetailDTO> putawayDetails) {
        if (ASN_STATUS_FULFILLED.equalsIgnoreCase(aiyaStatus)) {
            return OverseasInstockStatusEnum.SIGNED.getCode();
        }
        if (ASN_STATUS_VOIDED.equalsIgnoreCase(aiyaStatus)) {
            return OverseasInstockStatusEnum.CANCELED.getCode();
        }
        if (ASN_STATUS_RECEIVED.equalsIgnoreCase(aiyaStatus) && hasAnyReceived(putawayDetails)) {
            return OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode();
        }
        return OverseasInstockStatusEnum.TO_BE_SIGNED.getCode();
    }

    private boolean hasAnyReceived(List<AiyaInboundResp.AsnItemReceiveDetailDTO> putawayDetails) {
        if (CollUtil.isEmpty(putawayDetails)) {
            return false;
        }
        return putawayDetails.stream().anyMatch(v -> v != null && v.getPutawayQty() != null && v.getPutawayQty() > 0);
    }

    /**
     * 解析爱亚收货时间，兼容多种常见格式，全部解析失败时回退当前时间（保证下游 downloadTime 非空）。
     */
    private LocalDateTime resolveReceiveTime(String receiveTime) {
        if (StringUtils.isBlank(receiveTime)) {
            return LocalDateTime.now();
        }
        String value = receiveTime.trim();
        if (value.chars().allMatch(Character::isDigit)) {
            try {
                long epoch = Long.parseLong(value);
                if (value.length() <= 10) {
                    epoch = epoch * 1000L;
                }
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
            } catch (Exception ignore) {
                return LocalDateTime.now();
            }
        }
        for (DateTimeFormatter formatter : Arrays.asList(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (Exception ignore) {
                // 尝试下一个格式
            }
        }
        try {
            return java.time.LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        } catch (Exception ignore) {
            log.warn("[爱亚入库] 无法解析收货时间, 回退当前时间, receiveTime={}", receiveTime);
            return LocalDateTime.now();
        }
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("receivingCode");
    }
}
