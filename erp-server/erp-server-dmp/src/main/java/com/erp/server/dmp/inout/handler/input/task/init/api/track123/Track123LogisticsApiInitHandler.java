package com.erp.server.dmp.inout.handler.input.task.init.api.track123;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.enums.TrackQueryTypeEnum;
import com.common.business.utils.RedisUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.erp.server.dmp.service.ForeignService;
import com.sdk.tms.track123.model.request.TrackRequest;
import com.sdk.tms.track123.model.response.Rejected;
import com.sdk.tms.track123.model.response.ResponseData;
import com.sdk.tms.track123.model.response.TrackDetail;
import com.sdk.tms.track123.model.response.TrackResponse;
import com.sdk.tms.track123.service.TrackShipperService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@Scope("prototype")
public class Track123LogisticsApiInitHandler implements DmpInputApiInitHandler {
    private static final int pageSize = 100;
    public static final String PAGE_SIZE = "pageSize";
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private TrackShipperService trackShipperService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private LogisticsBillFeign logisticsBillFeign;
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

        // 每页数量
        int pageSizeValue = getPageSizeValue(dmpInputApiInitRequest);

        long current = 1;
        LocalDateTime trackTime = LocalDateTime.now().minusMonths(3);
        //根据跟踪单获取跟踪轨迹
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(pageSizeValue)
                .current(current)
                .registerStatus(1)
                .trackEnable(true)
                .trackTime(trackTime)
                .transportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode())
                .build();
        ResponseData trackData = getTrackData(query, cfgAppClient);
        if (ObjectUtil.isEmpty(trackData)) {
            return Collections.emptyList();
        }

        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();

        List<TrackDetail> list = new ArrayList<>();

        if (ObjectUtil.isNotEmpty(trackData.getAccepted())) {
            list.addAll(trackData.getAccepted().getContent());
        }
        if (CollectionUtils.isNotEmpty(trackData.getRejected())) {
            for (Rejected rejected : trackData.getRejected()) {
                TrackDetail trackDetail = new TrackDetail();
                trackDetail.setRejected(rejected);
                list.add(trackDetail);
            }
        }
        dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(list));
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

        return dmpInputTaskInitDTOList;
    }

    private ResponseData getTrackData(LogisticsBillDetailQueryDTO query, CfgAppClientEntity cfgAppClient) {
        List<LogisticsTrackDTO.UpdateTrackDTO> list =  pageDmpLogisticsTrack(query);
        return processTrackData(list, cfgAppClient);
    }

    private ResponseData processTrackData(List<LogisticsTrackDTO.UpdateTrackDTO> records, CfgAppClientEntity cfgAppClient) {
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> billDetailIdList = records.stream().map(LogisticsTrackDTO.UpdateTrackDTO::getId).distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(billDetailIdList)){
                mqProducerService.asyncClassMsg(RocketMqTopic.TMS_123_LOGISTICS_TRACK, RocketMqTagEnum.ASYNC_GET_TRACK123_LOGISTICS_TRACK.getName(), billDetailIdList, "getTrack");
            }
            String token = cfgAppClient.getClientSecret();
            //根据配置进行获取
            List<String> trackNoList = new ArrayList<>();
            //根据配置进行组装注册数据
            records.forEach(record -> {
                String trackNo = TrackQueryTypeEnum.TRACK_NO.getCode().equals(record.getTrackQueryType()) && StrUtil.isNotBlank(record.getTrackNo()) ? record.getTrackNo() : record.getTransportNo();
                if (StrUtil.isBlank(trackNo) && StrUtil.isNotBlank(record.getTrackNo())) {
                    trackNo = record.getTrackNo();
                }
                if (CharSequenceUtil.isNotBlank(trackNo)){
                    trackNoList.add(trackNo);
                }
            });
            if (CollectionUtils.isEmpty(trackNoList)) {
                return null;
            }
            TrackRequest trackRequest = TrackRequest.builder()
                    .trackNos(trackNoList)
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

    /**
     * 分页查询
     */
    private List<LogisticsTrackDTO.UpdateTrackDTO> pageDmpLogisticsTrack(LogisticsBillDetailQueryDTO query) {
        return logisticsBillFeign.listRegisterByConfig(query,query.getTrackQueryMode());
    }

    private static int getPageSizeValue(DmpInputApiInitRequest dmpInputApiInitRequest) {
        int pageSizeValue = pageSize;
        String requestParam = dmpInputApiInitRequest.getRequestParam();
        if (StringUtils.isNotBlank(requestParam)){
            JSONObject jsonObject = JSON.parseObject(requestParam);
            Integer intValue = jsonObject.getInteger(PAGE_SIZE);
            if (null != intValue){
                pageSizeValue = intValue;
            }
        }
        return pageSizeValue;
    }
}
