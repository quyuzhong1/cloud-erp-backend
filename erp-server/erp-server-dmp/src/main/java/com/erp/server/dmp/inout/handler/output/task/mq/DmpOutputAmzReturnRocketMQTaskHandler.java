package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.PlatformReturnOrderDTO;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class DmpOutputAmzReturnRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoReturnInfoEntity> dmpEntityMap = new HashMap<>();
        Map<String, List<DmpSoReturnDetailEntity>> dmpDetailEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_return_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnInfoEntity dmpEntity = (DmpSoReturnInfoEntity) v;
                        dmpEntityMap.put(dmpEntity.getId(), dmpEntity);
                    }
                } else if ("dmp_so_return_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnDetailEntity dmpSoDetailEntity = (DmpSoReturnDetailEntity) v;
                        String mainId = dmpSoDetailEntity.getMainId();
                        List<DmpSoReturnDetailEntity> list = dmpDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoDetailEntity);
                        dmpDetailEntityMap.put(mainId, list);
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
                if ("dmp_so_return_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_return_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnDetailEntity dmpDetailEntity = (DmpSoReturnDetailEntity) v;
                        changeIds.add(dmpDetailEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            PlatformReturnOrderDTO orderDTO = this.convert(
                    dmpEntityMap.get(changId),
                    dmpDetailEntityMap.get(changId),
                    cfgOutputId);
            if (orderDTO != null) {
                map.put(changId, JSON.toJSONString(orderDTO));
            }
        }
        return map;
    }


    /**
     * 解析数据
     **/
    private PlatformReturnOrderDTO convert(DmpSoReturnInfoEntity dmpEntity, List<DmpSoReturnDetailEntity> dmpDetailList, String cfgOutputId) {
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }
        if (CollUtil.isEmpty(dmpDetailList)) {
            return null;
        }
        PlatformReturnOrderDTO dto = new PlatformReturnOrderDTO();
        BeanUtils.copyProperties(dmpEntity, dto);
        dto.setUniqueId(dmpEntity.getThirdCode());
        dto.setPlatformReturnNo(dmpEntity.getThirdCode());
        dto.setPlatformOrderNo(dmpEntity.getPlatformCode());
        dto.setReason(dmpEntity.getRemark());
        dto.setDictPlatform(dmpEntity.getSourceSystem());
        dto.setPlatform(dmpEntity.getSourceSystem());
        dto.setDmpSyncTaskId(cfgOutputId);
        // 明细
        List<PlatformReturnOrderDTO.Detail> detailList = parseReturnDetailList(dmpDetailList);
        dto.setDetailList(detailList);
        return dto;
    }


    /**
     * 批量转换退货单明细DTO
     */
    private List<PlatformReturnOrderDTO.Detail> parseReturnDetailList(List<DmpSoReturnDetailEntity> dmpDetailList) {
        List<PlatformReturnOrderDTO.Detail> resultList = new LinkedList<>();
        for (DmpSoReturnDetailEntity dmpDetailEntity : dmpDetailList) {
            PlatformReturnOrderDTO.Detail detail = new PlatformReturnOrderDTO.Detail();
            detail.setPlatformSkuNo(dmpDetailEntity.getSkuNo());
            detail.setReturnQty(dmpDetailEntity.getQty());
            resultList.add(detail);
        }
        return resultList;
    }


    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("platformCode", "shopId");
    }
}

