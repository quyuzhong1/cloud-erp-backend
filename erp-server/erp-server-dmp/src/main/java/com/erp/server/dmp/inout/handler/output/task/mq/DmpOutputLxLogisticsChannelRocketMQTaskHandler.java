package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformThirdLogisticsChannelDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.Panno;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpLogisticsChannelEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.sdk.wangdian.dto.ErpShopDto;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class DmpOutputLxLogisticsChannelRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {
    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpLogisticsChannelEntity> dmpEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_logistics_channel".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpLogisticsChannelEntity dmpEntity = (DmpLogisticsChannelEntity) v;
                        dmpEntityMap.put(dmpEntity.getId(), dmpEntity);
                    }
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_logistics_channel".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            DmpLogisticsChannelEntity dmpEntity = dmpEntityMap.get(changId);
            PlatformThirdLogisticsChannelDTO channelDTO = this.convert(dmpEntity, cfgOutputId);
            if (channelDTO != null) {
                map.put(dmpEntity.getId(), JSON.toJSONString(channelDTO));
            }
        }
        return map;
    }

    /**
     * 解析
     **/
    public PlatformThirdLogisticsChannelDTO convert(DmpLogisticsChannelEntity dmpEntity, String cfgOutputId) {
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }
        PlatformThirdLogisticsChannelDTO mqDto = new PlatformThirdLogisticsChannelDTO();
        BeanUtils.copyProperties(dmpEntity, mqDto);

        mqDto.setPlatformCreateTime(dmpEntity.getCreateTime());
        mqDto.setPlatformCreateTime(dmpEntity.getUpdateTime());
        mqDto.setUniqueId(dmpEntity.getId());
        mqDto.setDmpSyncTaskId(dmpEntity.getId());
        mqDto.setPlatform(dmpEntity.getPlatformType());
        return mqDto;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("platformType", "type", "logisticsSupplierId", "logisticsTypeId");
    }
}