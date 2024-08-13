package com.erp.server.dmp.inout.handler.input.task.init.api.track123;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.enums.TrackQueryTypeEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.vo.request.LogisticsRegisterVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.sdk.tms.track123.model.request.TrackRequest;
import com.sdk.tms.track123.model.response.ResponseData;
import com.sdk.tms.track123.model.response.TrackDetail;
import com.sdk.tms.track123.model.response.TrackInfo;
import com.sdk.tms.track123.model.response.TrackResponse;
import com.sdk.tms.track123.service.TrackShipperService;
import io.seata.common.util.CollectionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Scope("prototype")
public class Track123LogisticsApiInitHandler implements DmpInputApiInitHandler {
    private static long pageSize = 100;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsBillFeign logisticsBillFeign;
    @Resource
    private TrackShipperService trackShipperService;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

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


        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();

        dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(responseDataList));
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);


        return dmpInputTaskInitDTOList;
    }

    private void getTrackData(LogisticsBillDetailQueryDTO query, List<ResponseData> responseDataList, CfgAppClientEntity cfgAppClient) {
        List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillFeign.listTrackDto(query);
        if (list.size() > MathUtil.NUMBER_100){
            //列表数据较多情况下，进行分割集合
            List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
            //物流商数据处理
            partition.forEach(e -> {
                ResponseData responseData = this.processTrackData(e, cfgAppClient);
                if (Objects.nonNull(responseData)){
                    responseDataList.add(responseData);
                }
            });
        }else {
            //物流商数据处理
            ResponseData responseData = this.processTrackData(list, cfgAppClient);
            if (Objects.nonNull(responseData)){
                responseDataList.add(responseData);
            }
        }
        log.info("========同步物流轨迹数据完成==========");
    }

    private ResponseData processTrackData(List<LogisticsTrackDTO.UpdateTrackDTO> records, CfgAppClientEntity cfgAppClient) {
        if (CollectionUtils.isNotEmpty(records)) {
            String token = cfgAppClient.getClientSecret();
            //根据配置进行获取
            List<LogisticsRegisterVO> logisticsRegisterVOS = new ArrayList<>();
            //根据配置进行组装注册数据
            records.forEach(updateTrackDTO -> {
                if (TrackQueryTypeEnum.TRACK_NO.getCode().equals(updateTrackDTO.getTrackQueryType()) && StrUtil.isNotBlank(updateTrackDTO.getTrackNo())){
                    logisticsRegisterVOS.add(LogisticsRegisterVO.builder()
                            .trackNo(updateTrackDTO.getTrackNo())
                            .phoneSuffix(updateTrackDTO.getTelNumber())
                            .build());

                }else {
                    String transportNo = updateTrackDTO.getTransportNo();
                    if (StrUtil.isNotBlank(transportNo)){
                        logisticsRegisterVOS.add(LogisticsRegisterVO.builder()
                                .trackNo(transportNo)
                                .phoneSuffix(updateTrackDTO.getTelNumber())
                                .build());
                    }
                }
            });
            if (CollectionUtils.isEmpty(logisticsRegisterVOS)){
                return null;
            }
            TrackRequest trackRequest = TrackRequest.builder()
                    .trackNos(logisticsRegisterVOS.stream().map(LogisticsRegisterVO::getTrackNo).distinct().collect(Collectors.toList()))
                    .cursor("")
                    .queryPageSize(100)
                    .build();
            try {
                TrackResponse track = trackShipperService.getTrack(token, trackRequest);
                return Objects.isNull(track) ? null : track.getData();
            } catch (Exception e) {
                log.error("获取Track123物流轨迹查询异常：{}", e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }
}
