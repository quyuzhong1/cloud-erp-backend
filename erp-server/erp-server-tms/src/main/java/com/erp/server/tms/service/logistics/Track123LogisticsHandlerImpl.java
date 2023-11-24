package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.LogisticsRegisterVO;
import com.erp.model.tms.vo.request.LogisticsTrackVO;
import com.erp.model.tms.vo.request.RegisterTrackVO;
import com.erp.model.tms.vo.response.RegisterResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.track123.model.request.RegisterRequest;
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
    private TrackShipperService trackShipperService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsOperateService logisticsOperateService;

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
        ValidatorUtil.validateEntity(trackRequest);
        try {
            TrackResponse<ResponseData> track = trackShipperService.getTrack(logisticsTrackVO.getAuthMap().get("clientSecret"), trackRequest);
            //成功
            if ("00000".equalsIgnoreCase(track.getCode())) {
                //查询成功的单号
                List<TrackDetail> accepted = track.getData().getAccepted().getContent();
                if (CollectionUtils.isNotEmpty(accepted)) {
                    accepted.forEach(trackDetail -> {
                        List<TrackingDetail> trackingDetails = trackDetail.getLocalLogisticsInfo().getTrackingDetails();
                        if(CollectionUtils.isNotEmpty(trackingDetails)){
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
                        }else if (StringUtils.isNotEmpty(trackDetail.getTransitStatus())){
                            LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
                            logisticsTrackEntity.setTrackNo(trackDetail.getTrackNo());
                            logisticsTrackEntity.setStatus(convertTrackStatus(trackDetail.getTransitStatus()));//转换类型
                            LocalDateTime eventTime = LocalDateTime.parse(trackDetail.getCreateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            logisticsTrackEntity.setTrackTime(eventTime);
                            logisticsTrackEntity.setContent("暂无信息");
                            logisticsTrackEntities.add(logisticsTrackEntity);
                        }

                    });
                }
                //查询失败的单号
                List<Rejected> rejecteds = track.getData().getRejected();
                if (CollectionUtils.isNotEmpty(rejecteds)) {
                    rejecteds.forEach(rejected -> {
                        LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
                        logisticsTrackEntity.setTrackNo(rejected.getTrackNo());
                        logisticsTrackEntity.setStatus(LogisticTrackStatusEnum.NOT_FIND.getCode());
                        logisticsTrackEntity.setContent(rejected.getError().getCode() + ":" + rejected.getError().getMsg());
                        logisticsTrackEntity.setTrackTime(LocalDateTime.now());
                        logisticsTrackEntities.add(logisticsTrackEntity);
                    });
                }
                logisticsOperateService.pullOperateLog(logisticsTrackVO.getAuthMap().get("id"),
                        UUID.randomUUID().toString(), BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsTrackVO), JSONUtil.toJsonStr(track));
                return success(logisticsTrackEntities);
            } else {
                logisticsOperateService.pullOperateLog(logisticsTrackVO.getAuthMap().get("id"),
                        UUID.randomUUID().toString(), BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsTrackVO), JSONUtil.toJsonStr(track));
                return failure(track.getMsg());
            }
        } catch (Exception e) {
            logisticsOperateService.pullOperateLog(logisticsTrackVO.getAuthMap().get("id"),
                    UUID.randomUUID().toString(), BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsTrackVO), JSONUtil.toJsonStr(e));
            return failure(e.getMessage());
        }
    }

    @Override
    public ApiResult<List<RegisterResponseVO>> registerLogisticsNumber(RegisterTrackVO registerTrackVO) {
        List<LogisticsRegisterVO> logisticsRegisterVOS = registerTrackVO.getLogisticsRegisterVOS();
        Map<String, String> authMap = registerTrackVO.getAuthMap();
        String token = authMap.get("clientSecret");
        List<RegisterResponseVO> registerResponseVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(logisticsRegisterVOS)) {
            return failure("注册数据不能为空");
        }
        List<RegisterRequest> registerRequests = LogisticsChannelConverter.INSTANCE.registerTrackNoByTrack123(logisticsRegisterVOS);
        ValidatorUtil.validateEntity(registerRequests);
        try {
            RegisterResult registerResult = trackShipperService.registerLogisticsNumber(token, registerRequests);
            //成功
            if ("00000".equalsIgnoreCase(registerResult.getCode())) {
                RegisterResponse data = registerResult.getData();
                List<Accepted> accepted = data.getAccepted();
                if (CollectionUtils.isNotEmpty(accepted)) {
                    accepted.forEach(accepted1 -> {
                        registerResponseVOS.add(RegisterResponseVO.builder().trackNo(accepted1.getTrackNo()).trackStatus(true).build());
                    });
                }
                List<Rejected> rejected = data.getRejected();
                if (CollectionUtils.isNotEmpty(rejected)) {
                    rejected.forEach(rejected1 -> {
                        registerResponseVOS.add(RegisterResponseVO.builder().trackNo(rejected1.getTrackNo()).trackStatus(false)
                                .code(rejected1.getError().getCode())
                                .msg(rejected1.getError().getMsg()).build());
                    });
                }
                logisticsOperateService.pushOperateLog(authMap.get("id"),
                        UUID.randomUUID().toString(), BusinessTypeEnum.REGISTER_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(registerTrackVO), JSONUtil.toJsonStr(registerRequests));
                return success(registerResponseVOS);
            }else {
                logisticsOperateService.pullOperateLog(authMap.get("id"),
                        UUID.randomUUID().toString(), BusinessTypeEnum.REGISTER_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(registerTrackVO), JSONUtil.toJsonStr(registerRequests));
                return failure(registerResult.getMsg());
            }
        }catch (Exception e){
            logisticsOperateService.pullOperateLog(authMap.get("id"),
                    UUID.randomUUID().toString(), BusinessTypeEnum.REGISTER_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(registerTrackVO), JSONUtil.toJsonStr(e));
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
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        if (org.apache.commons.lang3.StringUtils.isBlank(platform)) return Collections.emptyList();
        //获取商铺配置信息
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.TRACK123_AUTHORIZE;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        Map<String, String> map = new HashMap<>();
        map.put("id", cfgAppClient.getId());
        map.put("logisticsPlatform", getPlatForm().getCode());
        map.put("clientSecret", cfgAppClient.getClientSecret());
        map.put("clientId", cfgAppClient.getClientId());
        return Collections.singletonList(map);
    }
    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.TRACK123;
    }
}
