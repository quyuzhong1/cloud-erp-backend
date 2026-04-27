package com.erp.server.dmp.inout.handler.output.task.mq.kuaidi100;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpLogisticsTrackEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能描述：快递100 物流轨迹推送 MQ 处理器
 *
 * @author jack
 * @date 2026-03-31
 */
@Slf4j
@Service
@Scope("prototype")
public class Kuaidi100MQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private final static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpLogisticsTrackEntity> dmpLogisticsTrackEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = entry.getKey().getStorageName();
                if ("dmp_logistics_track".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpLogisticsTrackEntity dmpLogisticsTrackEntity = (DmpLogisticsTrackEntity) v;
                        dmpLogisticsTrackEntityMap.put(dmpLogisticsTrackEntity.getId(), dmpLogisticsTrackEntity);
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();

        List<DmpLogisticsTrackEntity> trackEntityList = new ArrayList<>(dmpLogisticsTrackEntityMap.values());
        // 按单号分组推送给 TMS
        Map<String, List<DmpLogisticsTrackEntity>> trackGroupByTrackNo = trackEntityList.stream()
                .collect(Collectors.groupingBy(DmpLogisticsTrackEntity::getTrackNo));

        for (Map.Entry<String, List<DmpLogisticsTrackEntity>> entry : trackGroupByTrackNo.entrySet()) {
            PlatformTrackDTO platformTrackDTO = this.convert(entry.getValue(), cfgOutputId);
            if (platformTrackDTO != null) {
                // key 为 ID，value 为序列化后的 DTO
                map.put(entry.getValue().get(0).getId(), JSON.toJSONString(platformTrackDTO));
            }
        }
        return map;
    }

    /**
     * 将 DMP 轨迹实体转换为平台通用轨迹 DTO
     */
    public PlatformTrackDTO convert(List<DmpLogisticsTrackEntity> trackEntityList, String cfgOutputId) {
        if (CollUtil.isEmpty(trackEntityList)) {
            return null;
        }

        // 黑名单过滤逻辑（如果有配置）
        if (this.validateDataBlack(trackEntityList.get(0), cfgOutputId)) {
            return null;
        }

        PlatformTrackDTO platformTrackDTO = new PlatformTrackDTO();
        platformTrackDTO.setTrackNo(trackEntityList.get(0).getTrackNo());
        platformTrackDTO.setPlatform(PlatformDictEnum.KUAIDI100.getCode());
        platformTrackDTO.setUniqueId(UUID.randomUUID().toString());

        List<PlatformTrackDetail> details = new ArrayList<>();
        for (DmpLogisticsTrackEntity dmpLogisticsTrackEntity : trackEntityList) {
            PlatformTrackDetail platformTrackDetail = new PlatformTrackDetail();
            platformTrackDetail.setTrackNo(dmpLogisticsTrackEntity.getTrackNo());
            platformTrackDetail.setTrackTime(dmpLogisticsTrackEntity.getTrackTime());
            platformTrackDetail.setStatus(dmpLogisticsTrackEntity.getStatus());
            platformTrackDetail.setOrderStatus(dmpLogisticsTrackEntity.getOrderStatus());
            platformTrackDetail.setContent(dmpLogisticsTrackEntity.getContent());
//            platformTrackDetail.setAddress(dmpLogisticsTrackEntity.getAddress());

            if (ObjectUtil.isNotNull(dmpLogisticsTrackEntity.getTrackTime())) {
                // 设置去重 MD5：单号 + 内容 + 格式化后的时间
                platformTrackDetail.setMd5(getDataMd5(dmpLogisticsTrackEntity));
            } else {
                log.warn("快递100 数据异常: 轨迹时间缺失: {}", dmpLogisticsTrackEntity.getTrackNo());
                continue;
            }
            details.add(platformTrackDetail);
        }

        platformTrackDTO.setDetails(details);
        return platformTrackDTO;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("trackNo");
    }

    /**
     * 生成轨迹项的唯一 MD5 标识
     */
    private String getDataMd5(DmpLogisticsTrackEntity dmpEntity) {
        String trackTime = dmpEntity.getTrackTime().format(TIME_FORMAT);
        return DigestUtil.md5Hex(dmpEntity.getTrackNo() + "-" + dmpEntity.getContent() + "-" + trackTime);
    }
}
