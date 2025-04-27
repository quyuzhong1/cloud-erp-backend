package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInventoryTransFlowEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundDetailEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.sdk.wms.goodcang.enums.GoodCangEnums;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputGoodCangReturnInstockRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpThirdReturnInboundEntity> dmpMainEntityMap = new HashMap<>();
        Map<String, List<DmpThirdReturnInboundDetailEntity>> dmpDetailEntityMap = new HashMap<>();
        Map<String, DmpThirdInventoryTransFlowEntity> dmpFlowEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_third_return_inbound".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdReturnInboundEntity dmpMainEntity = (DmpThirdReturnInboundEntity) v;
                        dmpMainEntityMap.put(dmpMainEntity.getId(), dmpMainEntity);
                    }
                } else if ("dmp_third_return_inbound_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdReturnInboundDetailEntity dmpDetailEntity = (DmpThirdReturnInboundDetailEntity) v;
                        String mainId = dmpDetailEntity.getMainId();
                        List<DmpThirdReturnInboundDetailEntity> list = dmpDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpDetailEntity);
                        dmpDetailEntityMap.put(mainId, list);
                    }
                } else if ("dmp_third_inventory_trans_flow".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdInventoryTransFlowEntity flowEntity = (DmpThirdInventoryTransFlowEntity) v;
                        dmpFlowEntityMap.put(flowEntity.getId(), flowEntity);
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
                if ("dmp_third_inventory_trans_flow".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            // 流水维度推送
            DmpThirdInventoryTransFlowEntity dmpFlowEntity = dmpFlowEntityMap.get(changId);
            PlatformReturnInstockDTO dto = this.convert(dmpFlowEntity, dmpMainEntityMap.get(dmpFlowEntity.getMainId()), cfgOutputId);
            if (null != dto) {
                map.put(dmpFlowEntity.getId(), JSON.toJSONString(dto));
            }
        }
        return map;
    }

    /**
     * 解析退货单入库数据
     **/
    public PlatformReturnInstockDTO convert(DmpThirdInventoryTransFlowEntity dmpFlowEntity, DmpThirdReturnInboundEntity dmpMainEntity, String cfgOutputId) {
        if (this.validateDataBlack(dmpFlowEntity, cfgOutputId)) {
            return null;
        }
        String returnStatus = dmpMainEntity.getReturnType();
        // T状态转换
        String erpStatus = GoodCangEnums.ReturnInstockTypeEnum.getErpStatus(returnStatus);
        if (StringUtils.isBlank(erpStatus)) {
            return null;
        }
        PlatformReturnInstockDTO dto = BeanUtil.copyProperties(dmpFlowEntity, PlatformReturnInstockDTO.class);
        String sourcePlatform = dmpFlowEntity.getSourcePlatform();
        dto.setPlatform(sourcePlatform);
        dto.setPutawayTime(dmpFlowEntity.getPlatformCreateTime());
        dto.setReturnType(erpStatus);
        dto.setUniqueId(dmpFlowEntity.getThirdId());

        // 明细
        List<PlatformReturnInstockDTO.Detail> detailList = Collections.singletonList(convertDetail(dmpFlowEntity));
        dto.setProductDetailList(detailList);
        return dto;
    }

    /**
     * 明细转换
     */
    private PlatformReturnInstockDTO.Detail convertDetail(DmpThirdInventoryTransFlowEntity detailEntity) {
        PlatformReturnInstockDTO.Detail detail = new PlatformReturnInstockDTO.Detail();
        detail.setProductSku(detailEntity.getProductSku());
        detail.setMustQty(detailEntity.getChangeQty());
        detail.setRealQty(detailEntity.getChangeQty());
        detail.setReceiveQty(detailEntity.getChangeQty());
        return detail;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("uniqueId");
    }
}
