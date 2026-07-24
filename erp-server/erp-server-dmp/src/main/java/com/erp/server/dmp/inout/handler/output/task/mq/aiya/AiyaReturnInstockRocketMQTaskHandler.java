package com.erp.server.dmp.inout.handler.output.task.mq.aiya;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundDetailEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundEntity;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 爱亚退货入库 DMP 输出 MQ 任务处理器，对齐 {@code WegoReturnInstockRocketMQTaskHandler}。
 * <p>
 * InitHandler 已过滤为完全上架（Fulfilled + COMPLETED）；下游按
 * {@code platformReturnOrderNo}（爱亚 asnNumber）幂等去重，生成退货入库单/预入库单。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaReturnInstockRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String STORAGE_MAIN = "dmp_third_return_inbound";
    private static final String STORAGE_DETAIL = "dmp_third_return_inbound_detail";

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest,
                                                   DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertMaps =
                dmpRequest.getConvertInputDmpBaseEntityListMaps();

        Map<String, DmpThirdReturnInboundEntity> mainEntityMap = new HashMap<>();
        Map<String, List<DmpThirdReturnInboundDetailEntity>> detailEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertMaps.entrySet()) {
            List<BaseEntity> entities = entry.getValue();
            if (CollUtil.isEmpty(entities)) {
                continue;
            }
            String storageName = entry.getKey().getStorageName();
            if (STORAGE_MAIN.equals(storageName)) {
                for (BaseEntity e : entities) {
                    DmpThirdReturnInboundEntity main = (DmpThirdReturnInboundEntity) e;
                    mainEntityMap.put(main.getId(), main);
                }
            } else if (STORAGE_DETAIL.equals(storageName)) {
                for (BaseEntity e : entities) {
                    DmpThirdReturnInboundDetailEntity detail = (DmpThirdReturnInboundDetailEntity) e;
                    detailEntityMap
                            .computeIfAbsent(detail.getMainId(), k -> new ArrayList<>())
                            .add(detail);
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeMaps =
                dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : changeMaps.entrySet()) {
            List<BaseEntity> entities = entry.getValue();
            if (CollUtil.isEmpty(entities)) {
                continue;
            }
            String storageName = entry.getKey().getStorageName();
            if (STORAGE_MAIN.equals(storageName)) {
                entities.forEach(e -> changeIds.add(e.getId()));
            } else if (STORAGE_DETAIL.equals(storageName)) {
                entities.forEach(e -> changeIds.add(((DmpThirdReturnInboundDetailEntity) e).getMainId()));
            }
        }

        Map<String, String> result = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpThirdReturnInboundEntity main = mainEntityMap.get(changeId);
            if (main == null) {
                log.warn("[爱亚退货入库] changeId={} 在 convert map 中未找到主表记录, cfgOutputId={}",
                        changeId, cfgOutputId);
                continue;
            }
            List<DmpThirdReturnInboundDetailEntity> details =
                    detailEntityMap.getOrDefault(changeId, Collections.emptyList());
            PlatformReturnInstockDTO dto = convert(main, details, cfgOutputId);
            if (dto != null) {
                result.put(main.getId(), JSON.toJSONString(dto));
            }
        }
        return result;
    }

    private PlatformReturnInstockDTO convert(DmpThirdReturnInboundEntity main,
                                              List<DmpThirdReturnInboundDetailEntity> detailList,
                                              String cfgOutputId) {
        if (validateDataBlack(main, cfgOutputId)) {
            return null;
        }
        if (Objects.isNull(main.getPutAwayTime())) {
            log.warn("[爱亚退货入库] 退货单[{}] putAwayTime 为空，跳过推送", main.getPlatformReturnOrderNo());
            return null;
        }

        List<DmpThirdReturnInboundDetailEntity> validDetails = detailList.stream()
                .filter(d -> Objects.nonNull(d.getRealQty()) && d.getRealQty() > 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(validDetails)) {
            log.warn("[爱亚退货入库] 退货单[{}] 无有效明细（realQty>0），跳过推送",
                    main.getPlatformReturnOrderNo());
            return null;
        }

        PlatformReturnInstockDTO dto = BeanUtil.copyProperties(main, PlatformReturnInstockDTO.class);
        dto.setPlatform(main.getSourcePlatform());
        dto.setAuthId(main.getNextLevelId());
        dto.setPutawayTime(main.getPutAwayTime());
        dto.setReturnLogisticCode(main.getReturnLogisticCode());
        if (StringUtils.isBlank(dto.getReturnType())) {
            dto.setReturnType(ReturnTypeEnum.OTHER.getCode());
        }

        List<PlatformReturnInstockDTO.Detail> detailDTOList = validDetails.stream()
                .map(this::convertDetail)
                .collect(Collectors.toList());
        dto.setProductDetailList(detailDTOList);
        return dto;
    }

    private PlatformReturnInstockDTO.Detail convertDetail(DmpThirdReturnInboundDetailEntity detail) {
        PlatformReturnInstockDTO.Detail d = new PlatformReturnInstockDTO.Detail();
        d.setProductSku(detail.getProductSku());
        d.setMustQty(detail.getMustQty());
        d.setReceiveQty(detail.getReceiveQty());
        d.setRealQty(detail.getRealQty());
        d.setThirdId(detail.getThirdDetailId());
        d.setDefectiveProductFlag(detail.getDefectiveProductFlag());
        return d;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("platformReturnOrderNo");
    }
}
