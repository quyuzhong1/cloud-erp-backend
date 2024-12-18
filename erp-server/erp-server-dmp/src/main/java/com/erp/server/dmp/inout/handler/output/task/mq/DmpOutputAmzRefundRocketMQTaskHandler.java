package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformRefundOrderDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoRefundDetailEntity;
import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@Scope("prototype")
public class DmpOutputAmzRefundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {


    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoRefundInfoEntity> dmpEntityMap = new HashMap<>();
        //  Map<退款单中台ID, 退款单列表>>
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
        // 变动的 changeIds
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_refund_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDetailEntity dmpSoReturnDetailEntity = (DmpSoDetailEntity) v;
                        changeIds.add(dmpSoReturnDetailEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            PlatformRefundOrderDTO orderDTO = this.convert(dmpEntityMap.get(changId),
                    dmpSoRefundDetailEntityMap.get(changId),
                    cfgOutputId);
            if (orderDTO != null) {
                map.put(changId, JSON.toJSONString(orderDTO));
            }
        }
        return map;
    }


    /**
     * 批量转换退款单DTO
     */
    public PlatformRefundOrderDTO convert(DmpSoRefundInfoEntity dmpEntity, List<DmpSoRefundDetailEntity> dmpDetailList, String cfgOutputId) {
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }
        if (CollUtil.isEmpty(dmpDetailList)) {
            return null;
        }
        PlatformRefundOrderDTO dto = new PlatformRefundOrderDTO();
        BeanUtils.copyProperties(dmpEntity, dto);
        dto.setUniqueId(dmpEntity.getThirdCode());
        dto.setPlatformRefundNo(dmpEntity.getThirdCode());
        dto.setPlatformOrderNo(dmpEntity.getPlatformCode());
        dto.setRemark(dmpEntity.getRemark());
        dto.setDictPlatform(dmpEntity.getSourceSystem());
        dto.setPlatform(dmpEntity.getSourceSystem());
        dto.setRefundAmount(dmpEntity.getAmount());
        dto.setCurrency(dmpEntity.getCurrencyCode());
        dto.setDmpSyncTaskId(cfgOutputId);
        List<PlatformRefundOrderDTO.Detail> detailList = new LinkedList<>();
        // 退款单可能存在没有明细
        if (!CollectionUtils.isEmpty(dmpDetailList)){
            // 明细
            detailList = parseRefundDetailList(dmpDetailList);
        }
        dto.setDetailList(detailList);
        return dto;
    }

    /**
     * 批量转换退款单明细DTO
     */
    private List<PlatformRefundOrderDTO.Detail> parseRefundDetailList(List<DmpSoRefundDetailEntity> dmpDetailList) {
        List<PlatformRefundOrderDTO.Detail> resultList = new LinkedList<>();
        for (DmpSoRefundDetailEntity dmpDetailEntity : dmpDetailList) {
            PlatformRefundOrderDTO.Detail detail = new PlatformRefundOrderDTO.Detail();
            detail.setPlatformSkuNo(dmpDetailEntity.getSkuNo());
            detail.setRefundQty(dmpDetailEntity.getQty());
            resultList.add(detail);
        }
        return resultList;
    }


    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("platformCode", "shopId");
    }


}
