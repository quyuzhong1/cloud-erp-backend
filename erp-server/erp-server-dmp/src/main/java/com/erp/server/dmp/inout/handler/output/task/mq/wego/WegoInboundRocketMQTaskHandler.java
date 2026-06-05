package com.erp.server.dmp.inout.handler.output.task.mq.wego;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformInboundDTO.Receiving;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
 * WEGO 海外仓入库 DMP 输出 MQ 任务处理器。
 * <p>
 * 与 {@code JiFengInboundRocketMQTaskHandler} 对齐：
 * <ol>
 *   <li>从 DMP 层产出（{@code convertInputDmpBaseEntityListMaps}）中收集 {@code dmp_third_inbound} 实体；</li>
 *   <li>仅推送本次发生变更（{@code changeConvertInputDmpBaseEntityListMaps}）的记录；</li>
 *   <li>转换为统一的 {@link PlatformInboundDTO}，由父类发送到 {@code dmp_cfg_mq} 配置的 topic/tag
 *       （即 {@code DMP_PLATFORM_INBOUND_TO_WMS_TOPIC} / {@code DMP_PLATFORM_INBOUND_TO_WMS_TAG}）。</li>
 * </ol>
 * <p>
 * 说明：明细 JSON 字段名（{@code sku} / {@code putawayCount} / {@code putawayLastTime}）与状态码（0/3/4）
 * 暂沿用 jifeng 的接口契约，等 WEGO 入库回执接口接入后按实际字段调整。
 */
@Service
@Scope("prototype")
public class WegoInboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String STORAGE_NAME = "dmp_third_inbound";

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
        platformInboundDTO.setPlatform(sourcePlatform);
        platformInboundDTO.setProvider(sourcePlatform);

        JSONArray skuArray = JSON.parseArray(dmpThirdInboundEntity.getDetailListJson());
        platformInboundDTO.setReceivingStatus(
                this.convertStatus(Integer.valueOf(dmpThirdInboundEntity.getReceivingStatus()), skuArray));

        List<Receiving> receivingDataList = new ArrayList<>();
        if (CollUtil.isNotEmpty(skuArray)) {
            for (Object obj : skuArray) {
                JSONObject sku = (JSONObject) obj;
                Receiving receiving = new Receiving();
                String putawayLastTime = sku.getString("putawayLastTime");
                LocalDateTime receiveTime = parseUtcToLocal(putawayLastTime);
                platformInboundDTO.setDownloadTime(receiveTime);
                receiving.setProductSku(sku.getString("sku"));
                Integer putawayCount = sku.getInteger("putawayCount");
                receiving.setReceiveQty(Objects.isNull(putawayCount) ? 0 : putawayCount);
                receiving.setReceiveTime(receiveTime);
                receivingDataList.add(receiving);
            }
        }
        platformInboundDTO.setReceivingDataList(receivingDataList);

        this.groupBySku(platformInboundDTO);
        return platformInboundDTO;
    }

    /**
     * 将 UTC 字符串时间转为系统默认时区的 {@link LocalDateTime}。
     */
    private LocalDateTime parseUtcToLocal(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.parse(time);
        ZonedDateTime utcZoned = localDateTime.atZone(ZoneId.of("UTC"));
        ZonedDateTime systemZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault());
        return systemZoned.toLocalDateTime();
    }

    /**
     * 按 SKU 汇总每个 sku 的实际签收数量，写入 {@link PlatformInboundDTO#setItems(List)}。
     */
    private void groupBySku(PlatformInboundDTO dto) {
        if (CollUtil.isEmpty(dto.getReceivingDataList())) {
            return;
        }
        Map<String, Integer> receivedQuantityMap = dto.getReceivingDataList().stream()
                .collect(Collectors.groupingBy(Receiving::getProductSku, Collectors.summingInt(Receiving::getReceiveQty)));
        List<PlatformInboundDTO.Item> items = receivedQuantityMap.entrySet().stream()
                .map(entry -> new PlatformInboundDTO.Item(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        dto.setItems(items);
    }

    /**
     * 入库单状态映射，等 WEGO 状态枚举确定后按实际取值调整。
     */
    private String convertStatus(Integer status, JSONArray skuArray) {
        if (status == null) {
            return OverseasInstockStatusEnum.TO_BE_SIGNED.getCode();
        }
        if (status == 4) {
            return OverseasInstockStatusEnum.SIGNED.getCode();
        }
        if (status == 3 && CollUtil.isNotEmpty(skuArray) && hasAnyPutaway(skuArray)) {
            return OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode();
        }
        if (status == 0) {
            return OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode();
        }
        return OverseasInstockStatusEnum.TO_BE_SIGNED.getCode();
    }

    private boolean hasAnyPutaway(JSONArray skuArray) {
        for (Object obj : skuArray) {
            JSONObject sku = (JSONObject) obj;
            Integer putawayCount = sku.getInteger("putawayCount");
            if (putawayCount != null && putawayCount > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("receivingCode");
    }
}
