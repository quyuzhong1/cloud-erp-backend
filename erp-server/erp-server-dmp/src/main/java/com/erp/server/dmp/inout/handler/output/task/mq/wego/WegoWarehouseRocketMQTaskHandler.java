package com.erp.server.dmp.inout.handler.output.task.mq.wego;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformWarehouseDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdWarehouseInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WEGO 海外仓库 DMP 输出 MQ 任务处理器。
 * <p>
 * 与 {@code JiFengWarehouseRocketMQTaskHandler} 保持一致：
 * <ol>
 *   <li>从 DMP 层产出（{@code convertInputDmpBaseEntityListMaps}）中收集 {@code dmp_third_warehouse_info} 实体；</li>
 *   <li>仅推送本次发生变更（{@code changeConvertInputDmpBaseEntityListMaps}）的记录；</li>
 *   <li>转换为统一的 {@link PlatformWarehouseDTO}，由父类 {@link DmpOutputRocketMQTaskHandler} 发送到
 *       {@code dmp_cfg_mq} 配置的 topic/tag（即 {@code DMP_PLATFORM_WAREHOUSE_TO_WMS_TOPIC} / {@code DMP_PLATFORM_WAREHOUSE_TO_WMS_TAG}），
 *       最终由 {@code PlatformNewWarehouseConsumerService} 消费写入 {@code overseas_provider_warehouse}。</li>
 * </ol>
 * <p>
 * 多例：{@link Scope}({@code prototype})，与父类 {@link DmpOutputRocketMQTaskHandler} 的生命周期约定一致。
 */
@Service
@Scope("prototype")
public class WegoWarehouseRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    /**
     * 与 {@code DmpThirdWarehouseInfoEntity} 对应的存储名（即 {@code dmp_cfg_input_convert.storage_name}）。
     */
    private static final String STORAGE_NAME = "dmp_third_warehouse_info";

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpThirdWarehouseInfoEntity> dmpThirdWarehouseInfoEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isNotEmpty(value) && STORAGE_NAME.equals(entry.getKey().getStorageName())) {
                for (BaseEntity v : value) {
                    DmpThirdWarehouseInfoEntity dmpThirdWarehouseInfoEntity = (DmpThirdWarehouseInfoEntity) v;
                    dmpThirdWarehouseInfoEntityMap.put(dmpThirdWarehouseInfoEntity.getId(), dmpThirdWarehouseInfoEntity);
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
            DmpThirdWarehouseInfoEntity dmpThirdWarehouseInfoEntity = dmpThirdWarehouseInfoEntityMap.get(changeId);
            PlatformWarehouseDTO warehouse = this.convert(dmpThirdWarehouseInfoEntity, cfgOutputId);
            if (warehouse != null) {
                map.put(dmpThirdWarehouseInfoEntity.getId(), JSON.toJSONString(warehouse));
            }
        }
        return map;
    }

    /**
     * 把 {@link DmpThirdWarehouseInfoEntity} 转换为通用的 {@link PlatformWarehouseDTO}。
     * <p>
     * {@code providerErpId} 透传自 INIT 阶段注入的 {@code authId}（即 {@code overseas_provider.id}），
     * 消费侧据此与 {@code overseas_provider_warehouse.main_id} 关联。
     *
     * @param dmpThirdWarehouseInfoEntity DMP 层第三方仓库实体
     * @param cfgOutputId                 当前输出配置 id，用于命中黑名单时跳过推送
     * @return 通用平台仓库 DTO；命中黑名单时返回 {@code null}
     */
    public PlatformWarehouseDTO convert(DmpThirdWarehouseInfoEntity dmpThirdWarehouseInfoEntity, String cfgOutputId) {
        if (this.validateDataBlack(dmpThirdWarehouseInfoEntity, cfgOutputId)) {
            return null;
        }
        PlatformWarehouseDTO warehouse = new PlatformWarehouseDTO();
        warehouse.setWarehousePlatformType(dmpThirdWarehouseInfoEntity.getWarehousePlatformType());
        String sourcePlatform = dmpThirdWarehouseInfoEntity.getSourcePlatform();
        warehouse.setPlatform(sourcePlatform);
        warehouse.setProvider(sourcePlatform);
        warehouse.setWarehouseCode(dmpThirdWarehouseInfoEntity.getWarehouseCode());
        warehouse.setWarehouseName(dmpThirdWarehouseInfoEntity.getWarehouseName());
        warehouse.setCountryCode(dmpThirdWarehouseInfoEntity.getCountryCode());
        warehouse.setProviderErpId(dmpThirdWarehouseInfoEntity.getAuthId());
        return warehouse;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("warehouseCode");
    }
}
