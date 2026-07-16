package com.erp.server.dmp.inout.handler.output.task.mq.aiya;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformInventoryDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInventoryEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 爱亚海外仓库存 DMP Output Handler，对齐 {@code WegoInventoryRocketMQTaskHandler}。
 * <p>
 * 将 dmp_third_inventory 中爱亚库存数据通过 RocketMQ 推送至 WMS，
 * 由 WMS PlatformInventoryConsumerService 消费后写入 overseas_inventory
 * （对应《爱亚海外仓对接方案文档》6.2.4「库存数据」要求的"写入【仓储管理-库存管理-三方仓库存】"）。
 * <p>
 * 该类本身与具体三方仓平台无关（平台区分依赖 {@link DmpThirdInventoryEntity#getSourcePlatform()}），
 * 之所以单独建类，是因为 DMP 框架按 {@code dmp_cfg_output.output_class} 配置的类名反射取 Spring bean，
 * 需要一个专属类名与爱亚的 Output 任务配置绑定。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaInventoryRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String STORAGE_NAME = "dmp_third_inventory";
    private static final String SOURCE_CODE_KEY_WAREHOUSE = "platformWarehouseCode";
    private static final String SOURCE_CODE_KEY_SKU = "productSku";

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps =
                dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpThirdInventoryEntity> inventoryEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isNotEmpty(value) && STORAGE_NAME.equals(entry.getKey().getStorageName())) {
                for (BaseEntity v : value) {
                    DmpThirdInventoryEntity entity = (DmpThirdInventoryEntity) v;
                    inventoryEntityMap.put(entity.getId(), entity);
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

        Map<String, String> result = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpThirdInventoryEntity entity = inventoryEntityMap.get(changeId);
            if (entity == null) {
                log.warn("AiyaInventoryRocketMQTaskHandler: changeId={} not found in inventoryEntityMap, cfgOutputId={}", changeId, cfgOutputId);
                continue;
            }
            PlatformInventoryDTO dto = convert(entity, cfgOutputId);
            if (dto != null) {
                result.put(entity.getId(), JSON.toJSONString(dto));
            }
        }
        return result;
    }

    private PlatformInventoryDTO convert(DmpThirdInventoryEntity entity, String cfgOutputId) {
        if (entity == null || this.validateDataBlack(entity, cfgOutputId)) {
            return null;
        }
        PlatformInventoryDTO dto = BeanUtil.copyProperties(entity, PlatformInventoryDTO.class);
        String sourcePlatform = entity.getSourcePlatform();
        dto.setPlatform(sourcePlatform);
        dto.setProvider(sourcePlatform);
        dto.setProviderErpId(entity.getAuthId());
        dto.setDownloadTime(LocalDateTime.now());
        dto.setAgeInfoList(new LinkedList<>());
        return dto;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList(SOURCE_CODE_KEY_WAREHOUSE, SOURCE_CODE_KEY_SKU);
    }
}
