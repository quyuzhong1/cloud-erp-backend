package com.sdk.tms.track123.handler;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.*;
import com.common.business.handler.AbstractLogisticsTrackHandler;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.vo.request.LogisticsRegisterVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.sdk.tms.track123.dto.PlatformTrack123TrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDetail;
import com.sdk.tms.track123.model.request.TrackRequest;
import com.sdk.tms.track123.model.response.*;
import com.sdk.tms.track123.service.TrackShipperService;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物流轨迹查询
 *
 * @author Jim
 * @since 2023-11-01
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.TRACK123)
@BusinessType(BusinessTypeEnum.GET_TRACK)
public class Track123LogisticsHandler extends AbstractLogisticsTrackHandler<PlatformTrack123TrackDTO, PlatformTrackDTO> {
    private static long pageSize = 100;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsBillFeign logisticsBillFeign;
    @Resource
    private TrackShipperService trackShipperService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PlatformTrack123TrackDTO> download(JobTaskDTO data) {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.TRACK123_AUTHORIZE;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = null;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("erp-dmp服务获取Track123配置信息异常：{}", e.getMessage());
        }
        if (Objects.isNull(cfgAppClient)) {
            return Collections.emptyList();
        }
        long current = 1;
        //根据跟踪单获取跟踪轨迹
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(pageSize)
                .current(current)
                .registerStatus(1)
                .trackEnable(true)
                .transportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode())
                .build();
        List<ResponseData> responseDataList = new ArrayList<>();
        getTrackData(query, responseDataList, cfgAppClient);
        return responseDataList.stream().map(e -> new PlatformTrack123TrackDTO(e, data)).collect(Collectors.toList());
    }

    private void getTrackData(LogisticsBillDetailQueryDTO query, List<ResponseData> responseDataList, CfgAppClientEntity cfgAppClient) {
        List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillFeign.listTrackDto(query);
        //列表数据较多情况下，进行分割集合
        List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
        //物流商数据处理
        partition.forEach(e -> {
            ResponseData responseData = this.processTrackData(e, cfgAppClient);
            if (Objects.nonNull(responseData)){
                responseDataList.add(responseData);
            }
        });
        log.info("========同步物流轨迹数据完成==========");
    }

    private ResponseData processTrackData(List<LogisticsTrackDTO.UpdateTrackDTO> records, CfgAppClientEntity cfgAppClient) {
        if (CollectionUtils.isEmpty(records)) {
            return null;
        }

        String token = cfgAppClient.getClientSecret();
        List<LogisticsRegisterVO> logisticsRegisterVOS = convertToLogisticsRegisterVO(records);

        if (CollectionUtils.isEmpty(logisticsRegisterVOS)) {
            return null;
        }

        TrackRequest trackRequest = createTrackRequest(logisticsRegisterVOS);
        try {
            TrackResponse track = trackShipperService.getTrack(token, trackRequest);
            return Objects.isNull(track) ? null : track.getData();
        } catch (Exception e) {
            log.error("获取Track123物流轨迹查询异常：{}", e.getMessage());
            return null;
        }
    }

    private List<LogisticsRegisterVO> convertToLogisticsRegisterVO(List<LogisticsTrackDTO.UpdateTrackDTO> records) {
        List<LogisticsRegisterVO> logisticsRegisterVOS = new ArrayList<>();
        for (LogisticsTrackDTO.UpdateTrackDTO updateTrackDTO : records) {
            if (TrackQueryTypeEnum.TRANSPORT_NO.getCode().equals(updateTrackDTO.getTrackQueryType()) && CharSequenceUtil.isNotBlank(updateTrackDTO.getTransportNo())) {
                String transportNo = updateTrackDTO.getTransportNo();
                if (CharSequenceUtil.isNotBlank(transportNo)) {
                    logisticsRegisterVOS.add(LogisticsRegisterVO.builder()
                            .trackNo(transportNo)
                            .phoneSuffix(updateTrackDTO.getTelNumber())
                            .build());
                }
            } else {
                logisticsRegisterVOS.add(LogisticsRegisterVO.builder()
                        .trackNo(updateTrackDTO.getTrackNo())
                        .phoneSuffix(updateTrackDTO.getTelNumber())
                        .build());
            }
        }
        return logisticsRegisterVOS;
    }

    private TrackRequest createTrackRequest(List<LogisticsRegisterVO> logisticsRegisterVOS) {
        return TrackRequest.builder()
                .trackNos(logisticsRegisterVOS.stream().map(LogisticsRegisterVO::getTrackNo).distinct().collect(Collectors.toList()))
                .cursor("")
                .queryPageSize(100)
                .build();
    }


    @Override
    public List<PlatformTrackDTO> convert(List<PlatformTrack123TrackDTO> sourceDataList) {
        List<PlatformTrackDTO> resultList = new ArrayList<>();
        for (PlatformTrack123TrackDTO sourceDto : sourceDataList) {
            processAcceptedTracks(resultList, sourceDto);
            processRejectedTracks(resultList, sourceDto);
        }
        return resultList;
    }

    private void processAcceptedTracks(List<PlatformTrackDTO> resultList, PlatformTrack123TrackDTO sourceDto) {
        TrackInfo accepted = sourceDto.getAccepted();
        if (Objects.nonNull(accepted) && CollectionUtils.isNotEmpty(accepted.getContent())) {
            for (TrackDetail trackDetail : accepted.getContent()) {
                PlatformTrackDTO acceptedToSaveDto = createAcceptedTrackDTO(trackDetail, sourceDto);
                List<PlatformTrackDetail> details = createDetails(trackDetail);
                acceptedToSaveDto.setDetails(details);
                resultList.add(acceptedToSaveDto);
            }
        }
    }

    private void processRejectedTracks(List<PlatformTrackDTO> resultList, PlatformTrack123TrackDTO sourceDto) {
        if (CollectionUtils.isNotEmpty(sourceDto.getRejected())) {
            for (Rejected rejected : sourceDto.getRejected()) {
                PlatformTrackDTO rejectedToSaveDto = createRejectedTrackDTO(rejected, sourceDto);
                resultList.add(rejectedToSaveDto);
            }
        }
    }

    private PlatformTrackDTO createAcceptedTrackDTO(TrackDetail trackDetail, PlatformTrack123TrackDTO sourceDto) {
        PlatformTrackDTO dto = new PlatformTrackDTO();
        dto.setTrackNo(trackDetail.getTrackNo());
        dto.setUniqueId(sourceDto.getUniqueId());
        dto.setPlatform(sourceDto.getPlatform());
        return dto;
    }

    private List<PlatformTrackDetail> createDetails(TrackDetail trackDetail) {
        List<PlatformTrackDetail> details = new ArrayList<>();
        LocalLogisticsInfo localLogisticsInfo = trackDetail.getLocalLogisticsInfo();
        if (CollectionUtils.isNotEmpty(localLogisticsInfo.getTrackingDetails())) {
            for (TrackingDetail trackingDetail : localLogisticsInfo.getTrackingDetails()) {
                PlatformTrackDetail detail = new PlatformTrackDetail();
                detail.setTrackNo(trackDetail.getTrackNo());
                detail.setStatus(convertTrackStatus(trackingDetail.getTransitSubStatus()));
                LocalDateTime eventTime = LocalDateTime.parse(trackingDetail.getEventTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                detail.setTrackTime(eventTime);
                detail.setContent(trackingDetail.getEventDetail());
                details.add(detail);
            }
        }
        return details;
    }

    private PlatformTrackDTO createRejectedTrackDTO(Rejected rejected, PlatformTrack123TrackDTO sourceDto) {
        PlatformTrackDTO dto = new PlatformTrackDTO();
        dto.setTrackNo(rejected.getTrackNo());
        dto.setUniqueId(sourceDto.getUniqueId());
        dto.setPlatform(sourceDto.getPlatform());
        PlatformTrackDetail detail = new PlatformTrackDetail();
        detail.setTrackNo(rejected.getTrackNo());
        detail.setStatus(LogisticTrackStatusEnum.NOT_FIND.getCode());
        detail.setContent(rejected.getError().getCode() + ":" + rejected.getError().getMsg());
        detail.setTrackTime(LocalDateTime.now());
        dto.setDetails(Collections.singletonList(detail));
        return dto;
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

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.TRACK123.getCode();
    }

    /**
     * 是否发送MQ
     * true=发送
     * false=不发送（有其他详情需要额外拉取）
     */
    @Override
    public Boolean getIsSendMq() {
        return Boolean.TRUE;
    }
}
