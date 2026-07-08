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

import java.util.*;

/**
 * Magalu 退款 (refunded + closed=true) MQ 推送处理器
 * 使用独立的 refund 存储 (dmp_so_refund_info / detail) 和 PlatformRefundOrderDTO。
 * 与退货 (return_info_sent) 使用不同的 Output Handler。
 */
@Service
@Scope("prototype")
public class DmpOutputMagaluRefundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String MAGALU_PLATFORM = "Magalu";

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoRefundInfoEntity> infoMap = new HashMap<>();
        Map<String, List<DmpSoRefundDetailEntity>> detailMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> e : convertMaps.entrySet()) {
            String storage = e.getKey().getStorageName();
            for (BaseEntity v : e.getValue()) {
                if ("dmp_so_refund_info".equals(storage)) {
                    DmpSoRefundInfoEntity info = (DmpSoRefundInfoEntity) v;
                    infoMap.put(info.getId(), info);
                } else if ("dmp_so_refund_detail".equals(storage)) {
                    DmpSoRefundDetailEntity det = (DmpSoRefundDetailEntity) v;
                    detailMap.computeIfAbsent(det.getMainId(), k -> new ArrayList<>()).add(det);
                }
            }
        }

        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> e : dmpRequest.getChangeConvertInputDmpBaseEntityListMaps().entrySet()) {
            String storage = e.getKey().getStorageName();
            for (BaseEntity v : e.getValue()) {
                if ("dmp_so_refund_info".equals(storage)) {
                    changeIds.add(v.getId());
                } else if ("dmp_so_refund_detail".equals(storage)) {
                    DmpSoRefundDetailEntity det = (DmpSoRefundDetailEntity) v;
                    changeIds.add(StringUtils.defaultString(det.getMainId()));
                }
            }
        }

        Map<String, String> result = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String id : changeIds) {
            PlatformRefundOrderDTO dto = convert(infoMap.get(id), detailMap.get(id), cfgOutputId);
            if (dto != null) {
                result.put(id, JSON.toJSONString(dto));
            }
        }
        return result;
    }

    private PlatformRefundOrderDTO convert(DmpSoRefundInfoEntity main, List<DmpSoRefundDetailEntity> details, String cfgOutputId) {
        if (main == null) {
            return null;
        }
        if (this.validateDataBlack(main, cfgOutputId)) {
            return null;
        }
        if (CollUtil.isEmpty(details)) {
            // 允许无明细的退款单
        }
        PlatformRefundOrderDTO dto = new PlatformRefundOrderDTO();
        BeanUtils.copyProperties(main, dto);
        dto.setUniqueId(main.getThirdCode());
        dto.setPlatformRefundNo(StringUtils.defaultIfBlank(main.getThirdCode(), main.getPlatformCode()));
        dto.setPlatformOrderNo(StringUtils.defaultIfBlank(main.getPlatformCode(), main.getPlatformOrderCode()));
        dto.setRemark(main.getRemark());
        dto.setDictPlatform(MAGALU_PLATFORM);
        dto.setPlatform(MAGALU_PLATFORM);
        dto.setRefundAmount(main.getAmount());
        dto.setCurrency(main.getCurrencyCode());
        dto.setDmpSyncTaskId(cfgOutputId);
        dto.setRefundTime(main.getRefundTime());
        dto.setDetailList(parseDetails(details));
        return dto;
    }

    private List<PlatformRefundOrderDTO.Detail> parseDetails(List<DmpSoRefundDetailEntity> list) {
        List<PlatformRefundOrderDTO.Detail> res = new ArrayList<>();
        if (list == null) return res;
        for (DmpSoRefundDetailEntity d : list) {
            PlatformRefundOrderDTO.Detail det = new PlatformRefundOrderDTO.Detail();
            det.setPlatformSkuNo(StringUtils.defaultString(d.getSkuNo()));
            det.setRefundQty(d.getQty() == null ? 0 : d.getQty());
            res.add(det);
        }
        return res;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("uniqueId");
    }
}
