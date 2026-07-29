package com.erp.server.dmp.inout.handler.output.task.mq.aiya;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdOutboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.sdk.wms.aiya.enums.AiyaEnums;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AIYA 2C 出库单 DMP 输出 MQ 任务处理器。
 * 读取 dmp_third_outbound 实体，按 {@link AiyaEnums.OrderStatusEnum} 做状态映射后推送到
 * dmp_platform_outbound_to_wms_topic（具体 Topic/Tag 由 {@code dmp_cfg_mq} 数据库配置驱动，
 * 非本类硬编码），由 PlatformOutboundConsumerService 统一消费处理。
 * <p>
 * 结构与 {@code WegoOutboundRocketMQTaskHandler} 完全等价，仅状态枚举来源不同。
 * <p>
 * 另外把 {@code detail_list_json} 解析为 {@code items}（含实际发货数量），供
 * {@code PlatformOutboundConsumerService} 做超发判定，见 {@link #convertItems}。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaOutboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String STORAGE_NAME = "dmp_third_outbound";

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertMap = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpThirdOutboundEntity> entityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertMap.entrySet()) {
            if (CollUtil.isNotEmpty(entry.getValue()) && STORAGE_NAME.equals(entry.getKey().getStorageName())) {
                for (BaseEntity v : entry.getValue()) {
                    DmpThirdOutboundEntity e = (DmpThirdOutboundEntity) v;
                    entityMap.put(e.getId(), e);
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeMap = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : changeMap.entrySet()) {
            if (CollUtil.isNotEmpty(entry.getValue()) && STORAGE_NAME.equals(entry.getKey().getStorageName())) {
                for (BaseEntity v : entry.getValue()) {
                    changeIds.add(v.getId());
                }
            }
        }

        Map<String, String> result = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpThirdOutboundEntity entity = entityMap.get(changeId);
            if (entity == null) { continue; }
            PlatformOutboundDTO dto = convert(entity, cfgOutputId);
            if (dto != null) {
                result.put(entity.getId(), JSON.toJSONString(dto));
            }
        }
        return result;
    }

    public PlatformOutboundDTO convert(DmpThirdOutboundEntity entity, String cfgOutputId) {
        if (this.validateDataBlack(entity, cfgOutputId)) { return null; }
        String orderStatus = entity.getOrderStatus();
        String erpOrderStatus = AiyaEnums.OrderStatusEnum.getErpOrderStatus(orderStatus);
        if (StringUtils.isBlank(erpOrderStatus)) { return null; }
        PlatformOutboundDTO dto = BeanUtil.copyProperties(entity, PlatformOutboundDTO.class);
        String sourcePlatform = entity.getSourcePlatform();
        dto.setPlatform(sourcePlatform);
        dto.setProvider(sourcePlatform);
        dto.setOutBoundTime(entity.getDateShipping());
        dto.setOrderStatus(erpOrderStatus);
        dto.setThirdOrderStatus(AiyaEnums.OrderStatusEnum.getName(orderStatus));
        dto.setTrackNo(entity.getTrackingNo());
        dto.setItems(convertItems(entity.getDetailListJson(), entity.getOrderCode()));
        return dto;
    }

    /**
     * 解析 {@code detail_list_json}（爱亚 {@code items} 原样透传，见 {@code AiyaOutBoundDmpHandler}）
     * 为 {@link PlatformOutboundDTO.Item} 列表，供下游 {@code PlatformOutboundConsumerService} 做
     * 超发（实际数量 &gt; 应发数量）判定。
     * <p>
     * {@code productSku} 取自爱亚 {@code items[].sku}，是<b>爱亚侧 SKU</b>（建单时下发给爱亚的
     * {@code sku} 原样透传，非 ERP 自身 SKU），下游比对时必须用同口径的
     * {@code ThirdWarehouseDeliveryDetailEntity#platformSkuNo}，不能用 {@code skuNo}。
     * 数量字段优先取官方字段名 {@code quantity}，兼容历史字段名 {@code qty}。
     *
     * @param detailListJson dmp_third_outbound.detail_list_json 原文
     * @param orderCode      爱亚出库单号，仅用于日志定位
     * @return 出库明细列表；解析失败或为空时返回空列表，不中断整体推送
     */
    private List<PlatformOutboundDTO.Item> convertItems(String detailListJson, String orderCode) {
        if (StringUtils.isBlank(detailListJson)) {
            return Collections.emptyList();
        }
        try {
            JSONArray jsonArray = JSON.parseArray(detailListJson);
            if (CollUtil.isEmpty(jsonArray)) {
                return Collections.emptyList();
            }
            List<PlatformOutboundDTO.Item> items = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject row = jsonArray.getJSONObject(i);
                if (row == null) {
                    continue;
                }
                String sku = row.getString("sku");
                if (StringUtils.isBlank(sku)) {
                    log.warn("[AIYA出库] 出库明细sku缺失，跳过该行, orderCode={}, row={}", orderCode, row);
                    continue;
                }
                Integer qty = row.getInteger("quantity");
                if (qty == null) {
                    qty = row.getInteger("qty");
                }
                if (qty == null) {
                    log.warn("[AIYA出库] 出库明细数量缺失，跳过该行, orderCode={}, sku={}", orderCode, sku);
                    continue;
                }
                items.add(new PlatformOutboundDTO.Item(sku, qty));
            }
            return items;
        } catch (Exception e) {
            log.error("[AIYA出库] 解析出库明细 detail_list_json 异常, orderCode={}, detailListJson={}",
                    orderCode, detailListJson, e);
            return Collections.emptyList();
        }
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("orderCode");
    }
}
