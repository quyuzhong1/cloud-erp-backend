package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsAuthFieldEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthFieldService;
import com.erp.server.tms.service.LogisticsAuthService;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.sdk.tms.track123.model.request.TrackRequest;
import com.sdk.tms.track123.model.response.*;
import com.sdk.tms.track123.service.TrackShipperService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName Track123LogisticsHandlerImpl
 * @description: track123
 * @date 2023年11月14日
 * @version: 1.0
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.TRACK123)
public class Track123LogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    TrackShipperService trackShipperService;
    @Resource
    LogisticsAuthService logisticsAuthService;
    @Resource
    private LogisticsAuthFieldService logisticsAuthFieldService;
    @Resource
    private LogisticsOrderOperateLogService logisticsOrderOperateLogService;

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return null;
    }

    @Override
    public Map<String, String> getLogisticsAuthConfig(String authId) {
        List<LogisticsAuthFieldEntity> fieldEntities = null;
        if (StringUtils.isNoneBlank(authId)) {
            fieldEntities = logisticsAuthFieldService.listByLogisticsAuthId(authId);
        } else {
            LogisticsAuthEntity authEntity = logisticsAuthService.lambdaQuery()
                    .eq(LogisticsAuthEntity::getLogisticsPlatform, getPlatForm().getCode()).one();
            if (Objects.nonNull(authEntity)) {
                fieldEntities = logisticsAuthFieldService.listByLogisticsAuthId(authEntity.getId());
            }
        }
        Map<String, String> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(fieldEntities)) {
            fieldEntities.forEach(logisticsAuthFieldEntity -> {
                map.put(logisticsAuthFieldEntity.getFieldCode(), logisticsAuthFieldEntity.getFieldValue());
            });
        }
        return map;
    }

    /**
     * 轨迹查询
     *
     * @param logisticsQueryBaseVOS
     * @return
     */
    @Override
    public ApiResult<List<LogisticsTrackEntity>> getTrack(List<LogisticsQueryBaseVO> logisticsQueryBaseVOS) {
        List<LogisticsTrackEntity> logisticsTrackEntities = new ArrayList<>();
        //一次最多查询100个
        LogisticsQueryBaseVO logisticsQueryBaseVO = logisticsQueryBaseVOS.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        TrackRequest trackRequest = TrackRequest.builder()
                .trackNos(logisticsQueryBaseVOS.stream().map(LogisticsQueryBaseVO::getTrackNo).collect(Collectors.toList()))
                .cursor("")
                .queryPageSize(100)
                .build();
        TrackResponse track = trackShipperService.getTrack(logisticsQueryBaseVO.getAuthMap().get("clientSecret"), trackRequest);
        //成功
        if ("00000".equalsIgnoreCase(track.getCode())) {
            //查询成功的单号
            List<TrackDetail> accepted = track.getData().getAccepted().getContent();
            if (CollectionUtils.isNotEmpty(accepted)){
                accepted.forEach(trackDetail -> {
                    List<TrackingDetail> trackingDetails = trackDetail.getLocalLogisticsInfo().getTrackingDetails();
                    //本地物流
                    trackingDetails.forEach(trackingDetail -> {
                        LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
                        logisticsTrackEntity.setTrackNo(trackDetail.getTrackNo());
                        logisticsTrackEntity.setStatus(trackingDetail.getTransitSubStatus());//转换类型
                        LocalDateTime eventTime = LocalDateTime.parse(trackingDetail.getEventTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        logisticsTrackEntity.setTrackTime(eventTime);
                        logisticsTrackEntity.setContent(trackingDetail.getEventDetail());
                        logisticsTrackEntities.add(logisticsTrackEntity);
                    });
                });
            }
            //查询失败的单号
            List<Rejected> rejecteds = track.getData().getRejected();
            if (CollectionUtils.isNotEmpty(rejecteds)){
                rejecteds.forEach(rejected -> {
                    LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
                    logisticsTrackEntity.setTrackNo(rejected.getTrackNo());
                    logisticsTrackEntity.setStatus("0");
                    logisticsTrackEntity.setContent(rejected.getError().getCode()+":"+rejected.getError().getMsg());
                    logisticsTrackEntity.setTrackTime(LocalDateTime.now());
                    logisticsTrackEntities.add(logisticsTrackEntity);
                });
            }
            return success(logisticsTrackEntities);
        } else {
            return failure(track.getMsg());
        }
    }

}
