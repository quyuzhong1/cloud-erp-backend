package com.erp.server.dmp.inout.handler.output.task.mq.wego;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformInboundDTO.Receiving;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.sdk.wms.wego.dto.response.WegoInboundResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
 * WEGO 海外仓入库 DMP 输出 MQ 任务处理器。
 * <p>
 * 与 {@code JiFengInboundRocketMQTaskHandler} 链路一致，但解析协议按 WEGO 真实字段实现：
 * <ol>
 *   <li>从 DMP 层产出（{@code convertInputDmpBaseEntityListMaps}）中收集 {@code dmp_third_inbound} 实体；</li>
 *   <li>仅推送本次发生变更（{@code changeConvertInputDmpBaseEntityListMaps}）的记录；</li>
 *   <li>把 {@code detail_list_json}（{@link WegoInboundResp.InstockDTO} 列表）按
 *       「instocks[] × products[]」笛卡尔展开生成 {@link Receiving} 流水，每条流水保留
 *       {@code defectiveProductFlag} / {@code receiveUser} / {@code receiveTime} / {@code thirdId}；</li>
 *   <li>设置 {@code hasReceivedData=true}，让下游 {@code OverseasWarehouseInboundServiceImpl.handlePlatformMessage}
 *       直接按流水落 {@code overseas_warehouse_inbound_received}，而非按 SKU 差量推断。</li>
 * </ol>
 * <p>
 * 说明：
 * <ul>
 *   <li>WEGO 的 {@code createTime} 字段格式为 {@code yyyy-MM-dd HH:mm:ss}（仓侧本地时间），
 *       不需要做 UTC 时区转换；{@code batch} 为 {@code yyyy-MM-dd} 入库批次。</li>
 *   <li>幂等：流水 ID 取 {@code inOrderDetailId + '_' + batch + '_' + sku}，
 *       与下游 {@code receivedEntityMap} 的 {@code detailId + receiveQty + receiveTime} 联合去重一致。</li>
 *   <li>整箱不良品标记 {@code defectiveProductFlag} 来自 {@link WegoInboundResp.InstockDTO}，
 *       由箱内所有 {@code products} 共享。</li>
 * </ul>
 */
@Service
@Slf4j
@Scope("prototype")
public class WegoInboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String STORAGE_NAME = "dmp_third_inbound";

    /**
     * WEGO 入库批次 {@code createTime} / {@code updateTime} 格式，与服务端返回保持一致。
     */
    private static final DateTimeFormatter WEGO_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * WEGO 入库批次 {@code batch} 格式（仅日期）。
     */
    private static final DateTimeFormatter WEGO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * WEGO {@link WegoInboundResp.InstockDTO#getStatus()} 已上架枚举值（与服务端约定值对齐）。
     */
    private static final int INSTOCK_STATUS_DONE = 2;

    /**
     * WEGO {@link WegoInboundResp.InorderDTO#getStatus()} 已完结枚举值。
     */
    private static final int INORDER_STATUS_FINISHED = 4;

    /**
     * WEGO {@link WegoInboundResp.InorderDTO#getStatus()} 上架中枚举值。
     */
    private static final int INORDER_STATUS_PUTAWAY = 3;

    /**
     * WEGO {@link WegoInboundResp.InorderDTO#getStatus()} 待入库枚举值，
     * 对齐 jifeng 中 {@code status == 0} 的「待发货」语义。
     */
    private static final int INORDER_STATUS_TO_SHIP = 1;

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

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
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
                log.warn("WegoInbound: changeId={} 未在convert map中找到, cfgOutputId={}", changeId, cfgOutputId);
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
        platformInboundDTO.setPlatform(sourcePlatform);
        platformInboundDTO.setProvider(sourcePlatform);

        List<WegoInboundResp.InstockDTO> instockList = parseInstockList(dmpThirdInboundEntity.getDetailListJson());

        // 入库单状态：用 inorder.status 优先映射；instockList 仅作为「是否已经有上架明细」的辅助判断
        platformInboundDTO.setReceivingStatus(this.convertStatus(
                parseInt(dmpThirdInboundEntity.getReceivingStatus()), instockList));

        List<Receiving> receivingDataList = buildReceivingList(instockList);
        if (CollUtil.isNotEmpty(receivingDataList)) {
            platformInboundDTO.setHasReceivedData(true);
            platformInboundDTO.setReceivingDataList(receivingDataList);
            // 取最新一次入库时间作为下载时间，保证下游主表 receive_time 单调递增
            LocalDateTime latest = receivingDataList.stream()
                    .map(Receiving::getReceiveTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            if (latest != null) {
                platformInboundDTO.setDownloadTime(latest);
            }
        } else {
            platformInboundDTO.setReceivingDataList(new ArrayList<>());
        }

        this.groupBySku(platformInboundDTO, receivingDataList);
        return platformInboundDTO;
    }

    /**
     * 反序列化 {@code detail_list_json} 为 WEGO 入库批次明细列表。
     * <p>
     * 失败返回空列表，避免 NPE；上层会兜底为「无签收流水」继续推送状态。
     */
    private List<WegoInboundResp.InstockDTO> parseInstockList(String detailListJson) {
        if (StringUtils.isBlank(detailListJson)) {
            return new ArrayList<>();
        }
        try {
            List<WegoInboundResp.InstockDTO> list =
                    JSON.parseArray(detailListJson, WegoInboundResp.InstockDTO.class);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 把 {@link WegoInboundResp.InstockDTO} 列表按「批次 × 箱内 SKU」笛卡尔展开为签收流水。
     * <ul>
     *   <li>过滤 {@code deletedFlag = true} 的入库批次；</li>
     *   <li>每条流水的 {@code defectiveProductFlag} 来自所属批次；</li>
     *   <li>{@code receiveUser} 优先取 {@code upUserName}，缺省回退 {@code createUserName}；</li>
     *   <li>{@code receiveTime} 优先取 {@code createTime} 字符串解析，缺省回退 {@code batch} 当日 00:00:00；</li>
     *   <li>{@code thirdId}（流水ID）取
     *       {@code inOrderDetailId + '_' + batch + '_' + createTimeRaw + '_' + sku}。
     *       {@code createTimeRaw} 使用 wego 服务端返回的原始字符串（秒级精度），保证同一箱、同一天、
     *       同一 SKU 但不同时间点的两次上架不会生成相同的 thirdId，与下游 wego 分支
     *       基于 {@code flowId + authId} 的强幂等共同保证多次部分签收不丢、不重。</li>
     * </ul>
     */
    private List<Receiving> buildReceivingList(List<WegoInboundResp.InstockDTO> instockList) {
        List<Receiving> receivingList = new ArrayList<>();
        if (CollUtil.isEmpty(instockList)) {
            return receivingList;
        }
        for (WegoInboundResp.InstockDTO instock : instockList) {
            if (instock == null || Boolean.TRUE.equals(instock.getDeletedFlag())) {
                continue;
            }
            List<WegoInboundResp.ProductDTO> products = instock.getProducts();
            if (CollUtil.isEmpty(products)) {
                continue;
            }
            LocalDateTime receiveTime = resolveReceiveTime(instock);
            String receiveUser = resolveReceiveUser(instock);
            Boolean defectiveProductFlag = instock.getDefectiveProductFlag();
            String batch = StringUtils.defaultString(instock.getBatch());
            String createTimeRaw = StringUtils.defaultString(instock.getCreateTime());
            String inOrderDetailIdStr = instock.getInOrderDetailId() == null
                    ? "" : instock.getInOrderDetailId().toString();
            String thirdIdPrefix = inOrderDetailIdStr + "_" + batch + "_" + createTimeRaw + "_";
            for (WegoInboundResp.ProductDTO product : products) {
                if (product == null || StringUtils.isBlank(product.getSku())) {
                    continue;
                }
                Receiving receiving = new Receiving();
                receiving.setProductSku(product.getSku());
                receiving.setReceiveQty(product.getQty() == null ? 0 : product.getQty());
                receiving.setReceiveTime(receiveTime);
                receiving.setReceiveUser(receiveUser);
                receiving.setDefectiveProductFlag(defectiveProductFlag);
                receiving.setThirdId(thirdIdPrefix + product.getSku());
                receivingList.add(receiving);
            }
        }
        return receivingList;
    }

    /**
     * 解析 WEGO 入库批次的签收时间，优先取 {@code createTime}，缺省回退 {@code batch} 00:00:00。
     */
    private LocalDateTime resolveReceiveTime(WegoInboundResp.InstockDTO instock) {
        String createTime = instock.getCreateTime();
        if (StringUtils.isNotBlank(createTime)) {
            try {
                return LocalDateTime.parse(createTime.trim(), WEGO_DATETIME_FORMATTER);
            } catch (Exception ignore) {
                // 解析失败时继续尝试 batch
            }
        }
        String batch = instock.getBatch();
        if (StringUtils.isNotBlank(batch)) {
            try {
                return java.time.LocalDate.parse(batch.trim(), WEGO_DATE_FORMATTER).atStartOfDay();
            } catch (Exception ignore) {
                return null;
            }
        }
        return null;
    }

    /**
     * 解析操作员：上架人优先，回退创建人。
     */
    private String resolveReceiveUser(WegoInboundResp.InstockDTO instock) {
        if (StringUtils.isNotBlank(instock.getUpUserName())) {
            return instock.getUpUserName();
        }
        if (StringUtils.isNotBlank(instock.getCreateUserName())) {
            return instock.getCreateUserName();
        }
        return null;
    }

    /**
     * 按 SKU 汇总每个 sku 的实际签收数量，写入 {@link PlatformInboundDTO#setItems(List)}。
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
     * 入库单状态映射：以订单状态为主，兼容存在已上架批次的过渡状态。
     */
    private String convertStatus(Integer inorderStatus, List<WegoInboundResp.InstockDTO> instockList) {
        if (inorderStatus == null) {
            return OverseasInstockStatusEnum.TO_BE_SIGNED.getCode();
        }
        if (inorderStatus == INORDER_STATUS_FINISHED) {
            return OverseasInstockStatusEnum.SIGNED.getCode();
        }
        if (inorderStatus == INORDER_STATUS_PUTAWAY && hasAnyInstockDone(instockList)) {
            return OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode();
        }
        if (inorderStatus == INORDER_STATUS_TO_SHIP) {
            return OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode();
        }
        return OverseasInstockStatusEnum.TO_BE_SIGNED.getCode();
    }

    /**
     * 是否存在任何一条已上架的批次（{@link WegoInboundResp.InstockDTO#getStatus()} == 2）。
     */
    private boolean hasAnyInstockDone(List<WegoInboundResp.InstockDTO> instockList) {
        if (CollUtil.isEmpty(instockList)) {
            return false;
        }
        for (WegoInboundResp.InstockDTO instock : instockList) {
            if (instock == null || Boolean.TRUE.equals(instock.getDeletedFlag())) {
                continue;
            }
            if (instock.getStatus() != null && instock.getStatus() == INSTOCK_STATUS_DONE) {
                return true;
            }
        }
        return false;
    }

    private int parseInt(String text) {
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("receivingCode");
    }
}
