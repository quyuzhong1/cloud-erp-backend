package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformRefundOrderDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoRefundDetailEntity;
import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@Scope("prototype")
public class DmpOutputTikTokRefundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoRefundInfoEntity> dmpEntityMap = new HashMap<>();
        Map<String, List<DmpSoRefundDetailEntity>> dmpSoRefundDetailEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_refund_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoRefundInfoEntity dmpEntity = (DmpSoRefundInfoEntity) v;
                        dmpEntityMap.put(dmpEntity.getId(), dmpEntity);
                    }
                } else if ("dmp_so_refund_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoRefundDetailEntity dmpSoDetailEntity = (DmpSoRefundDetailEntity) v;
                        String mainId = dmpSoDetailEntity.getMainId();
                        List<DmpSoRefundDetailEntity> list = dmpSoRefundDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoDetailEntity);
                        dmpSoRefundDetailEntityMap.put(mainId, list);
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
                if ("dmp_so_refund_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_refund_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoRefundDetailEntity dmpSoRefundDetailEntity = (DmpSoRefundDetailEntity) v;
                        changeIds.add(dmpSoRefundDetailEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            PlatformRefundOrderDTO orderDTO = this.convert(dmpEntityMap.get(changId), dmpSoRefundDetailEntityMap.get(changId), cfgOutputId);
            if (orderDTO != null) {
                map.put(changId, JSON.toJSONString(orderDTO));
            }
        }
        return map;
    }

    public PlatformRefundOrderDTO convert(DmpSoRefundInfoEntity dmpEntity, List<DmpSoRefundDetailEntity> dmpDetailList, String cfgOutputId) {
        if (dmpEntity == null) {
            return null;
        }
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }
        if (!StringUtils.equalsIgnoreCase(dmpEntity.getPlatformOriginalStatus(), "RETURN_OR_REFUND_REQUEST_COMPLETE")) {
            return null;
        }
        if (CollUtil.isEmpty(dmpDetailList)) {
            return null;
        }

        PlatformRefundOrderDTO dto = new PlatformRefundOrderDTO();
        BeanUtils.copyProperties(dmpEntity, dto);
        dto.setUniqueId(dmpEntity.getThirdCode());
        dto.setPlatformRefundNo(dmpEntity.getThirdCode());
        dto.setPlatformOrderNo(StringUtils.defaultIfBlank(dmpEntity.getPlatformOrderCode(), dmpEntity.getPlatformCode()));
        dto.setRemark(StringUtils.defaultIfBlank(dmpEntity.getRemark(), dmpEntity.getReason()));
        String platform = StringUtils.defaultIfBlank(dmpEntity.getSourcePlatform(), dmpEntity.getSourceSystem());
        dto.setDictPlatform(platform);
        dto.setPlatform(platform);
        dto.setRefundAmount(dmpEntity.getAmount());
        dto.setCurrency(dmpEntity.getCurrencyCode());
        dto.setDmpSyncTaskId(cfgOutputId);

        List<PlatformRefundOrderDTO.Detail> detailList = new LinkedList<>();
        if (!CollectionUtils.isEmpty(dmpDetailList)) {
            detailList = parseRefundDetailList(dmpDetailList);
        }
        dto.setDetailList(detailList);
        return dto;
    }

    private List<PlatformRefundOrderDTO.Detail> parseRefundDetailList(List<DmpSoRefundDetailEntity> dmpDetailList) {
        List<PlatformRefundOrderDTO.Detail> resultList = new LinkedList<>();
        for (DmpSoRefundDetailEntity dmpDetailEntity : dmpDetailList) {
            PlatformRefundOrderDTO.Detail detail = new PlatformRefundOrderDTO.Detail();
            String platformSkuNo = StringUtils.defaultIfBlank(dmpDetailEntity.getSkuId(), dmpDetailEntity.getSkuNo());
            detail.setPlatformSkuNo(StringUtils.defaultString(platformSkuNo));
            detail.setRefundQty(dmpDetailEntity.getQty());
            resultList.add(detail);
        }
        return resultList;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("uniqueId");
    }
}

