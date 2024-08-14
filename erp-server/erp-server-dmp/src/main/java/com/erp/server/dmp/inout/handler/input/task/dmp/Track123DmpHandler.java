package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.util.ObjectUtil;
import com.common.core.utils.ObjectUtils;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDetail;
import com.sdk.tms.track123.model.response.Rejected;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Scope("prototype")
public class Track123DmpHandler extends DmpInputDbConvertDmpHandler {
    @Override
    protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(List<Map<String, Object>> dmpInputMongoEntityList) {
        Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps = new HashMap<>();
        for (Map<String, Object> dmpInputMongoBaseEntity : dmpInputMongoEntityList) {
            ArrayList<TreeMap<String, Object>> valueList = new ArrayList<>();

            if (ObjectUtil.isNotEmpty(dmpInputMongoBaseEntity.get("accepted"))) {
                Map<String, Object> acceptedMap = (Map<String, Object>) dmpInputMongoBaseEntity.get("accepted");
                if (ObjectUtil.isNotEmpty(acceptedMap.get("content"))) {

                    List<Map<String, Object>> contentList = (List<Map<String, Object>>) acceptedMap.get("content");
                    for (Map<String, Object> contentMap : contentList) {

                        Object localLogisticsInfo = contentMap.get("localLogisticsInfo");
                        if (ObjectUtil.isNotEmpty(localLogisticsInfo)) {
                            Map<String, Object> localLogisticsInfoMap = (Map<String, Object>) localLogisticsInfo;
                            if (ObjectUtil.isNotEmpty(localLogisticsInfoMap.get("trackingDetails"))) {
                                List<Map<String, Object>> trackingDetails = (List<Map<String, Object>>) localLogisticsInfoMap.get("trackingDetails");

                                for (Map<String, Object> trackingDetail : trackingDetails) {
                                    TreeMap<String, Object> dmpInputDmpBaseEntity = new TreeMap<>();
                                    this.afterDmpInputMongoEntityFixedValue(dmpInputDmpBaseEntity);
                                    dmpInputDmpBaseEntity.put("courierCode", localLogisticsInfoMap.get("courierCode"));
                                    dmpInputDmpBaseEntity.put("courierName", localLogisticsInfoMap.get("courierNameCN"));
                                    dmpInputDmpBaseEntity.put("trackNo", contentMap.get("trackNo"));
                                    dmpInputDmpBaseEntity.put("trackTime", trackingDetail.get("eventTime"));
                                    dmpInputDmpBaseEntity.put("status", convertTrackStatus(trackingDetail.get("transitSubStatus").toString()));
                                    dmpInputDmpBaseEntity.put("content", trackingDetail.get("eventDetail").toString());
                                    valueList.add(dmpInputDmpBaseEntity);
                                }
                            } else {
                                TreeMap<String, Object> dmpInputDmpBaseEntity = new TreeMap<>();
                                this.afterDmpInputMongoEntityFixedValue(dmpInputDmpBaseEntity);
                                dmpInputDmpBaseEntity.put("courierCode", localLogisticsInfoMap.get("courierCode"));
                                dmpInputDmpBaseEntity.put("courierName", localLogisticsInfoMap.get("courierNameCN"));
                                dmpInputDmpBaseEntity.put("trackNo", contentMap.get("trackNo"));
                                valueList.add(dmpInputDmpBaseEntity);
                            }

                        }
                    }
                }
            }
            List<Map<String, Object>> rejectedList = (List<Map<String, Object>>) dmpInputMongoBaseEntity.get("rejected");
            if (CollectionUtils.isNotEmpty(rejectedList)) {
                for (Map<String, Object> map : rejectedList) {
                    TreeMap<String, Object> dmpInputDmpBaseEntity = new TreeMap<>();
                    this.afterDmpInputMongoEntityFixedValue(dmpInputDmpBaseEntity);
                    dmpInputDmpBaseEntity.put("trackNo", map.get("trackNo"));
                    dmpInputDmpBaseEntity.put("status", LogisticTrackStatusEnum.NOT_FIND.getCode());
                    if (ObjectUtil.isNotEmpty(map.get("error"))) {
                        Map<String, Object> errorMap = (Map<String, Object>) map.get("error");
                        dmpInputDmpBaseEntity.put("content", errorMap.get("code") + ":" + errorMap.get("msg"));
                        dmpInputDmpBaseEntity.put("trackTime", LocalDateTime.now());
                    }
                    valueList.add(dmpInputDmpBaseEntity);
                }
            }

            ArrayList<Map<String, Object>> keyList = new ArrayList<>();
            keyList.add(dmpInputMongoBaseEntity);
            dmpInputDataDmpRelationMaps.put(keyList, valueList);
        }
        this.afterConvertData(dmpInputDataDmpRelationMaps);
        return dmpInputDataDmpRelationMaps;
    }

    private String convertTrackStatus(String transitSubStatus) {
        if (StringUtils.isBlank(transitSubStatus)) {//待查询
            return LogisticTrackStatusEnum.NOT_FIND.getCode();
        } else if (transitSubStatus.contains("INIT")) {//待查询  单号正在查询中，请等待
            return LogisticTrackStatusEnum.NOT_FIND.getCode();
        } else if (transitSubStatus.contains("NO_RECORD")) {//暂无信息 包裹无法查询到物流轨迹信息
            return LogisticTrackStatusEnum.NOT_FIND.getCode();
        } else if (transitSubStatus.contains("INFO_RECEIVED")) {//已接收 物流公司已经收到寄运订单，正在准备揽收包裹
            return LogisticTrackStatusEnum.WAIT_COLLECT.getCode();
        } else if (transitSubStatus.contains("IN_TRANSIT")) {//运输中 包裹正在运输途中
            return LogisticTrackStatusEnum.TRACK_ING.getCode();
        } else if (transitSubStatus.contains("WAITING_DELIVERY")) {//派送中 包裹正在派送或已到达代收点等待收件人自提
            return LogisticTrackStatusEnum.DELIVERY_ING.getCode();
        } else if (transitSubStatus.contains("DELIVERY_FAILED")) {//投递失败 包裹尝试派送，但由于地址问题、收件人联系不上等原因导致派送失败
            return LogisticTrackStatusEnum.DELIVERY_FAIL.getCode();
        } else if (transitSubStatus.contains("ABNORMAL")) {//异常 包裹出现破损、退件、海关扣留等异常情况
            return LogisticTrackStatusEnum.MAYBE_EXCEPTION.getCode();
        } else if (transitSubStatus.contains("DELIVERED")) {//已成功 包裹投递成功
            return LogisticTrackStatusEnum.SIGN.getCode();
        } else if (transitSubStatus.contains("EXPIRED")) {//已过期 包裹在最近的30天没有任何物流更新
            return LogisticTrackStatusEnum.TRANSPORT_LONG.getCode();
        }
        return LogisticTrackStatusEnum.NOT_FIND.getCode();
    }
}
