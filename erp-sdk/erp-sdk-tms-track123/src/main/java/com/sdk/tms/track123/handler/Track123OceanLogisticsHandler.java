package com.sdk.tms.track123.handler;

import cn.hutool.core.collection.ListUtil;
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
import com.erp.model.tms.dto.LogisticsTrackBaseDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.sdk.tms.track123.dto.PlatformTrack123OceanTrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDetail;
import com.sdk.tms.track123.model.response.*;
import com.sdk.tms.track123.service.TrackShipperOceanService;
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
@BusinessType(BusinessTypeEnum.GET_OCEAN_TRACK)
public class Track123OceanLogisticsHandler extends AbstractLogisticsTrackHandler<PlatformTrack123OceanTrackDTO, PlatformTrackDTO> {
    private static long pageSize = 100;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsBillFeign logisticsBillFeign;
    @Resource
    private TrackShipperOceanService trackShipperOceanService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PlatformTrack123OceanTrackDTO> download(JobTaskDTO data) {
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
                .transportType(LogisticsTransportTypeEnum.OCEAN.getCode())
                .build();
        List<OceanResponseData> responseDataList = new ArrayList<>();
        getTrackData(query, responseDataList, cfgAppClient);
        return responseDataList.stream().map(e -> new PlatformTrack123OceanTrackDTO(e, data)).collect(Collectors.toList());
    }


    private void getTrackData(LogisticsBillDetailQueryDTO query, List<OceanResponseData> responseDataList, CfgAppClientEntity cfgAppClient) {
        List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillFeign.listTrackDto(query);
            //列表数据较多情况下，进行分割集合
            List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
            //物流商数据处理
            partition.forEach(e -> {
                OceanResponseData responseData = this.processTrackData(e, cfgAppClient);
                if (Objects.nonNull(responseData)){
                    responseDataList.add(responseData);
                }
            });
        log.info("========同步物流轨迹数据完成==========");
    }

    private OceanResponseData processTrackData(List<LogisticsTrackDTO.UpdateTrackDTO> records, CfgAppClientEntity cfgAppClient) {
        if (CollectionUtils.isNotEmpty(records)) {
            List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> list = new ArrayList<>();
            String token = cfgAppClient.getClientSecret();
            for (LogisticsTrackDTO.UpdateTrackDTO trackRecord : records) {
              LogisticsTrackBaseDTO.OceanTrackRequestDTO oceanTrackRequestDTO = LogisticsTrackBaseDTO.OceanTrackRequestDTO.builder()
                      .trackingNo(trackRecord.getTrackNo())
                      .orderNo(trackRecord.getPlatformOrderNo())
                      .type(MathUtil.THREE)
                      .build();
                list.add(oceanTrackRequestDTO);
            }
            try {
                TrackOceanResponse track = trackShipperOceanService.getTrack(token, list);
                return track.getData();
            } catch (Exception e) {
                log.error("获取Track123物流轨迹查询异常：{}", e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public List<PlatformTrackDTO> convert(List<PlatformTrack123OceanTrackDTO> sourceDataList) {
        List<PlatformTrackDTO> resultList = new ArrayList<>();
        for (PlatformTrack123OceanTrackDTO sourceDto : sourceDataList) {
            processAcceptedTracks(resultList, sourceDto);
            processRejectedTracks(resultList, sourceDto);
        }
        return resultList;
    }

    private void processAcceptedTracks(List<PlatformTrackDTO> resultList, PlatformTrack123OceanTrackDTO sourceDto) {
        List<OceanTrackInfo> accepted = sourceDto.getAccepted();
        if (Objects.isNull(accepted)) {
            return;
        }

        accepted.stream()
                .filter(Objects::nonNull)
                .forEach(oceanTrackInfo -> processContainerInfo(resultList, oceanTrackInfo, sourceDto));
    }

    private void processContainerInfo(List<PlatformTrackDTO> resultList, OceanTrackInfo oceanTrackInfo, PlatformTrack123OceanTrackDTO sourceDto) {
        List<OceanContainerInfo> containerInfoList = oceanTrackInfo.getContainerInfo();
        if (CollectionUtils.isEmpty(containerInfoList)) {
            return;
        }

        containerInfoList.stream()
                .filter(Objects::nonNull)
                .forEach(containerInfo -> {
                    List<PlatformTrackDetail> details = createDetails(oceanTrackInfo, containerInfo);
                    if (!details.isEmpty()) {
                        PlatformTrackDTO acceptedToSaveDto = createAcceptedTrackDTO(oceanTrackInfo, sourceDto);
                        acceptedToSaveDto.setDetails(details);
                        resultList.add(acceptedToSaveDto);
                    }
                });
    }

    private void processRejectedTracks(List<PlatformTrackDTO> resultList, PlatformTrack123OceanTrackDTO sourceDto) {
        List<Rejected> rejectedList = sourceDto.getRejected();
        if (CollectionUtils.isNotEmpty(rejectedList)) {
            for (Rejected rejected : rejectedList) {
                PlatformTrackDTO rejectedToSaveDto = createRejectedTrackDTO(rejected, sourceDto);
                resultList.add(rejectedToSaveDto);
            }
        }
    }

    private PlatformTrackDTO createAcceptedTrackDTO(OceanTrackInfo oceanTrackInfo, PlatformTrack123OceanTrackDTO sourceDto) {
        PlatformTrackDTO dto = new PlatformTrackDTO();
        dto.setTrackNo(oceanTrackInfo.getTrackingNo());
        dto.setUniqueId(sourceDto.getUniqueId());
        dto.setPlatform(sourceDto.getPlatform());
        return dto;
    }

    private List<PlatformTrackDetail> createDetails(OceanTrackInfo oceanTrackInfo, OceanContainerInfo containerInfo) {
        List<PlatformTrackDetail> details = new ArrayList<>();
        List<OceanTrackingDetail> trackingDetails = containerInfo.getTrackingDetails();
        if (CollectionUtils.isNotEmpty(trackingDetails)) {
            for (OceanTrackingDetail trackingDetail : trackingDetails) {
                PlatformTrackDetail detail = new PlatformTrackDetail();
                detail.setTrackNo(oceanTrackInfo.getTrackingNo());
                detail.setStatus(convertOceanTrackStatus(trackingDetail.getEventStatus()));
                LocalDateTime eventTime = LocalDateTime.parse(trackingDetail.getEventTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                detail.setTrackTime(eventTime);
                detail.setContent(trackingDetail.getEventDetails());
                details.add(detail);
            }
        }
        return details;
    }

    private PlatformTrackDTO createRejectedTrackDTO(Rejected rejected, PlatformTrack123OceanTrackDTO sourceDto) {
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
     * 【与track123轨迹对应关系-到港之前均为运输中，到港后更新「已到港」，查验变更为[查验中]】
     * @param eventStatus
     * @return
     */
    private String convertOceanTrackStatus(String eventStatus) {
        if (StringUtils.isBlank(eventStatus)) {
            return FmLogisticTrackStatusEnum.TRACK_ING.getCode();
        } else if (eventStatus.contains("ARRI")) {
            return FmLogisticTrackStatusEnum.ARRIVED.getCode();
        } else if (eventStatus.contains("HOLD")) {
            return FmLogisticTrackStatusEnum.INSPECTING.getCode();
        }
        return FmLogisticTrackStatusEnum.TRACK_ING.getCode();
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
