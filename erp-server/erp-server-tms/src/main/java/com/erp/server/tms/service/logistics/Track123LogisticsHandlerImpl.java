package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.request.LogisticsTrackVO;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.sdk.tms.track123.model.request.TrackRequest;
import com.sdk.tms.track123.model.response.*;
import com.sdk.tms.track123.service.TrackShipperService;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
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
    private LogisticsOrderOperateLogService logisticsOrderOperateLogService;

    /**
     * 轨迹查询
     *
     * @param logisticsTrackVO
     * @return
     */
    @Override
    public ApiResult<List<LogisticsTrackEntity>> getTrack(LogisticsTrackVO logisticsTrackVO) {
        List<LogisticsTrackEntity> logisticsTrackEntities = new ArrayList<>();
        //一次最多查询100个
        TrackRequest trackRequest = TrackRequest.builder()
                .trackNos(logisticsTrackVO.getTrackNos())
                .cursor("")
                .queryPageSize(100)
                .build();
        try {
            TrackResponse track = trackShipperService.getTrack(logisticsTrackVO.getAuthMap().get("clientSecret"), trackRequest);
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
                            logisticsTrackEntity.setStatus(convertTrackStatus(trackingDetail.getTransitSubStatus()));//转换类型
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
                        logisticsTrackEntity.setStatus(LogisticTrackStatusEnum.NOT_FIND.getCode());
                        logisticsTrackEntity.setContent(rejected.getError().getCode()+":"+rejected.getError().getMsg());
                        logisticsTrackEntity.setTrackTime(LocalDateTime.now());
                        logisticsTrackEntities.add(logisticsTrackEntity);
                    });
                }
                logisticsOrderOperateLogService.pullOperateLog(logisticsTrackVO.getAuthMap().get("id"),
                        UUID.randomUUID().toString(), BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsTrackVO), JSONUtil.toJsonStr(track));
                return success(logisticsTrackEntities);
            } else {
                logisticsOrderOperateLogService.pullOperateLog(logisticsTrackVO.getAuthMap().get("id"),
                        UUID.randomUUID().toString(), BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsTrackVO), JSONUtil.toJsonStr(track));
                return failure(track.getMsg());
            }
        }catch (Exception e){
            logisticsOrderOperateLogService.pullOperateLog(logisticsTrackVO.getAuthMap().get("id"),
                    UUID.randomUUID().toString(), BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsTrackVO), JSONUtil.toJsonStr(e.getMessage()));
            return failure(e.getMessage());
        }
    }

    /**
     * INIT	待查询	单号正在查询中，请等待
     * NO_RECORD	暂无信息	包裹无法查询到物流轨迹信息
     * INFO_RECEIVED	已接收	物流公司已经收到寄运订单，正在准备揽收包裹
     * IN_TRANSIT	运输中	包裹正在运输途中
     * WAITING_DELIVERY	派送中	包裹正在派送或已到达代收点等待收件人自提
     * DELIVERY_FAILED	投递失败	包裹尝试派送，但由于地址问题、收件人联系不上等原因导致派送失败
     * ABNORMAL	异常	包裹出现破损、退件、海关扣留等异常情况
     * DELIVERED	已成功	包裹投递成功
     * EXPIRED	已过期	包裹在最近的30天没有任何物流更新
     *
     * @param transitSubStatus
     * @return
     */
    private String convertTrackStatus(String transitSubStatus){
        if (StringUtils.isBlank(transitSubStatus)){//待查询
            return LogisticTrackStatusEnum.NOT_FIND.getCode();
        }else if (transitSubStatus.contains("INIT")){//待查询  单号正在查询中，请等待
            return LogisticTrackStatusEnum.NOT_FIND.getCode();
        }else if (transitSubStatus.contains("NO_RECORD")){//暂无信息 包裹无法查询到物流轨迹信息
            return LogisticTrackStatusEnum.NOT_FIND.getCode();
        }else if (transitSubStatus.contains("INFO_RECEIVED")){//已接收 物流公司已经收到寄运订单，正在准备揽收包裹
            return LogisticTrackStatusEnum.WAIT_COLLECT.getCode();
        }else if (transitSubStatus.contains("IN_TRANSIT")){//运输中 包裹正在运输途中
            return LogisticTrackStatusEnum.TRACK_ING.getCode();
        }else if (transitSubStatus.contains("WAITING_DELIVERY")){//派送中 包裹正在派送或已到达代收点等待收件人自提
            return LogisticTrackStatusEnum.DELIVERY_ING.getCode();
        }else if (transitSubStatus.contains("DELIVERY_FAILED")){//投递失败 包裹尝试派送，但由于地址问题、收件人联系不上等原因导致派送失败
            return LogisticTrackStatusEnum.DELIVERY_FAIL.getCode();
        }else if (transitSubStatus.contains("ABNORMAL")){//异常 包裹出现破损、退件、海关扣留等异常情况
            return LogisticTrackStatusEnum.MAYBE_EXCEPTION.getCode();
        }else if (transitSubStatus.contains("DELIVERED")){//已成功 包裹投递成功
            return LogisticTrackStatusEnum.SIGN.getCode();
        }else if (transitSubStatus.contains("EXPIRED")){//已过期 包裹在最近的30天没有任何物流更新
            return LogisticTrackStatusEnum.TRANSPORT_LONG.getCode();
        }
        return LogisticTrackStatusEnum.NOT_FIND.getCode();
    }
    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.TRACK123;
    }
}
