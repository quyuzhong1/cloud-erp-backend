package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformReturnOrderDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Magalu 售后退货/退款 MQ 推送处理器
 * 只推送经 getTicketActivities 筛选出的 return_info_sent（退货单）和 refunded（退款单）。
 * activityType 附在 reason 末尾，供下游区分生成退货还是退款。
 */
@Service
@Scope("prototype")
public class DmpOutputMagaluReturnRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String MAGALU_PLATFORM = "Magalu";

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoReturnInfoEntity> infoMap = new HashMap<>();
        Map<String, List<DmpSoReturnDetailEntity>> detailMap = new HashMap<>();
        Map<String, DmpSoReturnInfoEntity> infoByThird = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> e : convertMaps.entrySet()) {
            String storage = e.getKey().getStorageName();
            for (BaseEntity v : e.getValue()) {
                if ("dmp_so_return_info".equals(storage)) {
                    DmpSoReturnInfoEntity info = (DmpSoReturnInfoEntity) v;
                    infoMap.put(info.getId(), info);
                    if (StringUtils.isNotBlank(info.getThirdCode())) {
                        infoByThird.put(info.getThirdCode(), info);
                    }
                } else if ("dmp_so_return_detail".equals(storage)) {
                    DmpSoReturnDetailEntity det = (DmpSoReturnDetailEntity) v;
                    String key = StringUtils.isNotBlank(det.getMainId()) ? det.getMainId() : "";
                    detailMap.computeIfAbsent(key, k -> new ArrayList<>()).add(det);
                }
            }
        }

        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> e : dmpRequest.getChangeConvertInputDmpBaseEntityListMaps().entrySet()) {
            String storage = e.getKey().getStorageName();
            for (BaseEntity v : e.getValue()) {
                if ("dmp_so_return_info".equals(storage)) {
                    changeIds.add(v.getId());
                } else if ("dmp_so_return_detail".equals(storage)) {
                    DmpSoReturnDetailEntity det = (DmpSoReturnDetailEntity) v;
                    changeIds.add(StringUtils.isNotBlank(det.getMainId()) ? det.getMainId() : "");
                }
            }
        }

        Map<String, String> result = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String id : changeIds) {
            DmpSoReturnInfoEntity main = infoMap.get(id);
            List<DmpSoReturnDetailEntity> dets = detailMap.get(id);
            if (main == null && StringUtils.isNotBlank(id)) {
                main = infoByThird.get(id);  // fallback by thirdCode
            }
            if (dets == null && main != null && StringUtils.isNotBlank(main.getThirdCode())) {
                dets = detailMap.get(main.getThirdCode());
            }
            PlatformReturnOrderDTO dto = convert(main, dets, cfgOutputId);
            if (dto != null) {
                result.put(id, JSON.toJSONString(dto));
            }
        }
        return result;
    }

    private PlatformReturnOrderDTO convert(DmpSoReturnInfoEntity main, List<DmpSoReturnDetailEntity> details, String cfgOutputId) {
        if (main == null || CollUtil.isEmpty(details)) {
            return null;
        }
        if (validateDataBlack(main, cfgOutputId)) {
            return null;
        }
        PlatformReturnOrderDTO dto = new PlatformReturnOrderDTO();
        BeanUtils.copyProperties(main, dto);
        dto.setUniqueId(buildUniqueId(main));
        dto.setPlatformReturnNo(StringUtils.defaultIfBlank(main.getThirdCode(), main.getPlatformCode()));
        dto.setPlatformOrderNo(StringUtils.defaultIfBlank(main.getPlatformOrderCode(), main.getPlatformCode()));
        String reason = StringUtils.defaultIfBlank(main.getRemark(), "");
        // carry activity type so downstream can distinguish return_info_sent (退货) vs refunded (退款)
        String actType = main.getPlatformStatus();
        if (StringUtils.isNotBlank(actType) && ("return_info_sent".equalsIgnoreCase(actType) || "refunded".equalsIgnoreCase(actType))) {
            reason = reason + " [" + actType + "]";
        }
        dto.setReason(reason);
        dto.setDictPlatform(MAGALU_PLATFORM);
        dto.setPlatform(MAGALU_PLATFORM);
        dto.setDmpSyncTaskId(cfgOutputId);
        dto.setShopId(main.getShopId());
        dto.setDetailList(parseDetails(details));
        // 可选：batchNo 等按需从 extend 取
        return dto;
    }

    private String buildUniqueId(DmpSoReturnInfoEntity e) {
        String code = StringUtils.defaultString(e.getThirdCode());
        String batch = StringUtils.trimToEmpty(e.getBatchNo());
        return StringUtils.isBlank(batch) ? "return_" + code : "return_" + code + "_" + batch;
    }

    private List<PlatformReturnOrderDTO.Detail> parseDetails(List<DmpSoReturnDetailEntity> list) {
        List<PlatformReturnOrderDTO.Detail> res = new ArrayList<>();
        for (DmpSoReturnDetailEntity d : list) {
            PlatformReturnOrderDTO.Detail det = new PlatformReturnOrderDTO.Detail();
            det.setPlatformSkuNo(StringUtils.defaultString(d.getSkuNo()));
            det.setReturnQty(d.getQty() == null ? 0 : d.getQty());
            res.add(det);
        }
        return res;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("uniqueId");
    }
}
