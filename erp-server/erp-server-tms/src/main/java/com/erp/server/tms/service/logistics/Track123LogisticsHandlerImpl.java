package com.erp.server.tms.service.logistics;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.dto.LogisticsTrackBaseDTO;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.LogisticsRegisterVO;
import com.erp.model.tms.vo.request.LogisticsTrackVO;
import com.erp.model.tms.vo.request.RegisterTrackVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.model.tms.vo.response.RegisterResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.track123.model.request.OceanRegisterRequest;
import com.sdk.tms.track123.model.request.RegisterRequest;
import com.sdk.tms.track123.model.request.TrackRequest;
import com.sdk.tms.track123.model.response.*;
import com.sdk.tms.track123.service.TrackShipperOceanService;
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
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String NUMBER = "00000";
    public static final String MESSAGE = "注册数据不能为空";
    @Resource
    private TrackShipperService trackShipperService;
    @Resource
    private TrackShipperOceanService trackShipperOceanService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsOperateService logisticsOperateService;
    private static final String HAS_BEEN_IMPORTED = "The order number has been imported";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    private static final  DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
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
            TrackResponse track = trackShipperService.getTrack(logisticsTrackVO.getAuthMap().get(CLIENT_SECRET), trackRequest);
            //成功
            if (Objects.nonNull(track) && NUMBER.equalsIgnoreCase(track.getCode())) {
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
                                LocalDateTime eventTime = LocalDateTime.parse(trackingDetail.getEventTime(), DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS));
                                logisticsTrackEntity.setTrackTime(eventTime);
                                logisticsTrackEntity.setAddress(trackingDetail.getAddress());
                                logisticsTrackEntity.setContent(trackingDetail.getEventDetail());
                                logisticsTrackEntity.setTransportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode());
                                logisticsTrackEntity.setMd5(getDataMd5(logisticsTrackEntity));
                                logisticsTrackEntities.add(logisticsTrackEntity);
                            });
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
                        logisticsTrackEntity.setTransportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode());
                        logisticsTrackEntities.add(logisticsTrackEntity);
                    });
                }
                logisticsOperateService.pullOperateLog(null,
                        null, BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsTrackVO), JSONUtil.toJsonStr(track));
                return success(logisticsTrackEntities);
            } else {
                logisticsOperateService.pullOperateLog(null,
                        null, BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsTrackVO), JSONUtil.toJsonStr(track));
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + track.getMsg());
            }
        } catch (Exception e) {
            logisticsOperateService.pullOperateLog(null,
                    null, BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsTrackVO), JSONUtil.toJsonStr(e));
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    /**
     * 获取唯一值
     * @param trackingDetail
     * @return
     */
    private String getDataMd5(LogisticsTrackEntity trackingDetail) {
        String trackTime = trackingDetail.getTrackTime().format(TIME_FORMAT);
        return DigestUtil.md5Hex(trackingDetail.getTrackNo() + trackingDetail.getContent() + trackTime);
    }

    @Override
    public ApiResult<List<LogisticsTrackEntity>> getOceanTrack(List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> oceanTrackRequestList) {
        List<LogisticsTrackEntity> logisticsTrackList = new ArrayList<>();
        ValidatorUtil.validateEntity(oceanTrackRequestList);

        try {
            TrackOceanResponse track = trackShipperOceanService.getTrack(oceanTrackRequestList.get(0).getAuthMap().get(CLIENT_SECRET), oceanTrackRequestList);
            if (NUMBER.equalsIgnoreCase(track.getCode())) {
                oceanProcessAcceptedTracks(track.getData().getAccepted(), logisticsTrackList);
                oceanProcessRejectedTracks(track.getData().getRejected(), logisticsTrackList);
                pushSuccessOperateLog(oceanTrackRequestList, track);
                return success(logisticsTrackList);
            } else {
                pushFailedOperateLog(oceanTrackRequestList, track);
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ": " + track.getMsg());
            }
        } catch (Exception e) {
            log.error("获取物流跟踪信息失败", e);
            pushFailedOperateLog(oceanTrackRequestList, e);
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ": " + e.getMessage());
        }
    }

    private void oceanProcessAcceptedTracks(List<OceanTrackInfo> accepted, List<LogisticsTrackEntity> logisticsTrackList) {
        if (CollectionUtils.isEmpty(accepted)) {
            return;
        }

        accepted.forEach(trackDetail -> {
            List<OceanContainerInfo> containerInfoList = trackDetail.getContainerInfo();
            if (CollectionUtils.isNotEmpty(containerInfoList)) {
                containerInfoList.forEach(oceanContainerInfo -> {
                    List<OceanTrackingDetail> trackingDetails = oceanContainerInfo.getTrackingDetails();
                    if (CollectionUtils.isNotEmpty(trackingDetails)) {
                        trackingDetails.forEach(trackingDetail -> {
                            LogisticsTrackEntity logisticsTrackEntity = createLogisticsTrackEntity(trackDetail, trackingDetail);
                            logisticsTrackList.add(logisticsTrackEntity);
                        });
                    }
                });
            }
        });
    }

    private void oceanProcessRejectedTracks(List<Rejected> rejected, List<LogisticsTrackEntity> logisticsTrackList) {
        if (CollectionUtils.isEmpty(rejected)) {
            return;
        }

        rejected.forEach(rejected1 -> {
            LogisticsTrackEntity logisticsTrackEntity = createRejectedLogisticsTrackEntity(rejected1);
            logisticsTrackList.add(logisticsTrackEntity);
        });
    }

    private LogisticsTrackEntity createLogisticsTrackEntity(OceanTrackInfo trackDetail, OceanTrackingDetail trackingDetail) {
        LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
        logisticsTrackEntity.setTrackNo(trackDetail.getTrackingNo());
        logisticsTrackEntity.setStatus(convertOceanTrackStatus(trackingDetail.getEventStatus()));
        LocalDateTime eventTime = LocalDateTime.parse(trackingDetail.getEventTime(), DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS));
        logisticsTrackEntity.setTrackTime(eventTime);
        logisticsTrackEntity.setContent(trackingDetail.getEventDetails());
        logisticsTrackEntity.setTransportType(LogisticsTransportTypeEnum.OCEAN.getCode());
        return logisticsTrackEntity;
    }

    private LogisticsTrackEntity createRejectedLogisticsTrackEntity(Rejected rejected) {
        LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
        logisticsTrackEntity.setTrackNo(rejected.getTrackingNo());
        logisticsTrackEntity.setStatus(LogisticTrackStatusEnum.NOT_FIND.getCode());
        logisticsTrackEntity.setContent(rejected.getError().getCode() + ": " + rejected.getError().getMsg());
        logisticsTrackEntity.setTrackTime(LocalDateTime.now());
        logisticsTrackEntity.setTransportType(LogisticsTransportTypeEnum.OCEAN.getCode());
        return logisticsTrackEntity;
    }

    private void pushSuccessOperateLog(List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> requestList, TrackOceanResponse response) {
        logisticsOperateService.pullOperateLog(null, null, BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                RequestStatusEnums.SUCCESS.getCode(), JSON.toJSONString(requestList), JSON.toJSONString(response));
    }

    private void pushFailedOperateLog(List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> requestList, TrackOceanResponse response) {
        logisticsOperateService.pullOperateLog(null, null, BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                RequestStatusEnums.FAILED.getCode(), JSON.toJSONString(requestList), JSON.toJSONString(response));
    }

    private void pushFailedOperateLog(List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> requestList, Exception e) {
        logisticsOperateService.pullOperateLog(null, null, BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                RequestStatusEnums.FAILED.getCode(), JSON.toJSONString(requestList), JSON.toJSONString(e));
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
    public ApiResult<List<RegisterResponseVO>> registerLogisticsNumber(RegisterTrackVO registerTrackVO) {
        List<LogisticsRegisterVO> logisticsRegisterVOS = registerTrackVO.getLogisticsRegisterVOS();
        Map<String, String> authMap = registerTrackVO.getAuthMap();
        String token = authMap.get(CLIENT_SECRET);

        if (CollectionUtils.isEmpty(logisticsRegisterVOS)) {
            return failure(MESSAGE);
        }

        List<RegisterRequest> registerRequests = prepareRegisterRequests(logisticsRegisterVOS);
        ValidatorUtil.validateEntity(registerRequests);

        try {
            RegisterResult registerResult = trackShipperService.registerLogisticsNumber(token, registerRequests);
            return handleRegisterResult(registerResult, registerTrackVO);
        } catch (Exception e) {
            log.error("注册物流单号失败", e);
            pushFailedOperateLog(registerTrackVO, e);
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ": " + e.getMessage());
        }
    }

    private List<RegisterRequest> prepareRegisterRequests(List<LogisticsRegisterVO> logisticsRegisterVOS) {
        List<RegisterRequest> registerRequests = LogisticsChannelConverter.INSTANCE.registerTrackNoByTrack123(logisticsRegisterVOS);
        registerRequests.forEach(this::filterExtendFieldMap);
        return registerRequests;
    }

    private void filterExtendFieldMap(RegisterRequest registerRequest) {
        String trackNo = registerRequest.getTrackNo();
        if (StringUtils.isEmpty(trackNo) || !trackNo.startsWith("SF")) {
            registerRequest.setExtendFieldMap(null);
        }
    }

    private ApiResult<List<RegisterResponseVO>> handleRegisterResult(RegisterResult registerResult, RegisterTrackVO registerTrackVO) {
        if (!NUMBER.equalsIgnoreCase(registerResult.getCode())) {
            pushFailedOperateLog(registerTrackVO, registerResult);
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ": " + registerResult.getMsg());
        }

        List<RegisterResponseVO> registerResponseVOS = new ArrayList<>();
        processAcceptedTracks(registerResult.getData().getAccepted(), registerResponseVOS);
        processRejectedTracks(registerResult.getData().getRejected(), registerResponseVOS);

        pushSuccessOperateLog(registerTrackVO, registerResult);
        return success(registerResponseVOS);
    }

    private void processAcceptedTracks(List<Accepted> accepted, List<RegisterResponseVO> registerResponseVOS) {
        if (CollectionUtils.isNotEmpty(accepted)) {
            accepted.forEach(accepted1 -> registerResponseVOS.add(
                    RegisterResponseVO.builder()
                            .trackNo(accepted1.getTrackNo())
                            .trackStatus(true)
                            .build()));
        }
    }

    private void processRejectedTracks(List<Rejected> rejected, List<RegisterResponseVO> registerResponseVOS) {
        if (CollectionUtils.isNotEmpty(rejected)) {
            rejected.forEach(rejected1 -> {
                boolean isImported = Objects.nonNull(rejected1.getError()) && StringUtils.isNotEmpty(rejected1.getError().getMsg())
                        && rejected1.getError().getMsg().equals(HAS_BEEN_IMPORTED);
                if (isImported) {
                    registerResponseVOS.add(RegisterResponseVO.builder()
                            .trackNo(rejected1.getTrackNo())
                            .trackStatus(true)
                            .build());
                } else {
                    registerResponseVOS.add(RegisterResponseVO.builder()
                            .trackNo(rejected1.getTrackNo())
                            .trackStatus(false)
                            .code(rejected1.getError().getCode())
                            .msg(rejected1.getError().getMsg())
                            .build());
                }
            });
        }
    }

    private void pushSuccessOperateLog(RegisterTrackVO registerTrackVO, RegisterResult registerResult) {
        logisticsOperateService.pushOperateLog(null, null, BusinessTypeEnum.REGISTER_TRACK.getCode(),
                LogisticsPlatformEnum.TRACK123.getCode(), RequestStatusEnums.SUCCESS.getCode(),
                JSON.toJSONString(registerTrackVO), JSON.toJSONString(registerResult), false);
    }

    private void pushFailedOperateLog(RegisterTrackVO registerTrackVO, Exception e) {
        logisticsOperateService.pushOperateLog(null, null, BusinessTypeEnum.REGISTER_TRACK.getCode(),
                LogisticsPlatformEnum.TRACK123.getCode(), RequestStatusEnums.FAILED.getCode(),
                JSON.toJSONString(registerTrackVO), JSON.toJSONString(e), true);
    }

    private void pushFailedOperateLog(RegisterTrackVO registerTrackVO, RegisterResult registerResult) {
        logisticsOperateService.pushOperateLog(null, null, BusinessTypeEnum.REGISTER_TRACK.getCode(),
                LogisticsPlatformEnum.TRACK123.getCode(), RequestStatusEnums.FAILED.getCode(),
                JSON.toJSONString(registerTrackVO), JSON.toJSONString(registerResult), false);
    }

    private ApiResult<List<RegisterResponseVO>> success(List<RegisterResponseVO> registerResponseVOS) {
        return ApiResult.success(registerResponseVOS);
    }


    @Override
    public ApiResult<List<RegisterResponseVO>> oceanRegisterLogisticsNumber(List<LogisticsTrackBaseDTO.OceanRegisterRequestDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return failure(MESSAGE);
        }
        Map<String, String> authMap = list.get(0).getAuthMap();
        String token = authMap.get(CLIENT_SECRET);
        List<RegisterResponseVO> registerResponseVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return failure(MESSAGE);
        }
        List<OceanRegisterRequest> registerRequests = handleOceanRegisterRequest(list);
        ValidatorUtil.validateEntity(registerRequests);
        try {
            OceanRegisterResult registerResult = trackShipperOceanService.registerLogisticsNumber(token, registerRequests);
            //成功
            if (NUMBER.equalsIgnoreCase(registerResult.getCode())) {
                OceanResponseData data = registerResult.getData();
                List<OceanTrackInfo> accepted = data.getAccepted();
                if (CollectionUtils.isNotEmpty(accepted)) {
                    accepted.forEach(accepted1 -> {
                        registerResponseVOS.add(RegisterResponseVO.builder().trackNo(accepted1.getTrackingNo()).orderNo(accepted1.getOrderNo()).trackStatus(true).build());
                    });
                }
                List<Rejected> rejected = data.getRejected();
                if (CollectionUtils.isNotEmpty(rejected)) {
                    rejected.forEach(rejected1 -> {
                        if (Objects.nonNull(rejected1.getError()) && StringUtils.isNotEmpty(rejected1.getError().getMsg())
                                && rejected1.getError().getMsg().equals(HAS_BEEN_IMPORTED)){
                            //已导入的运单号，返回成功
                            registerResponseVOS.add(RegisterResponseVO.builder().trackNo(rejected1.getTrackingNo()).orderNo(rejected1.getOrderNo()).trackStatus(true).build());
                        }else {
                            registerResponseVOS.add(RegisterResponseVO.builder().trackNo(rejected1.getTrackingNo()).trackStatus(false)
                                    .code(rejected1.getError().getCode())
                                    .msg(rejected1.getError().getMsg()).build());
                        }
                    });
                }
                logisticsOperateService.pushOperateLog(null,
                        null, BusinessTypeEnum.REGISTER_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(registerRequests), JSONUtil.toJsonStr(registerResult), false);
                return success(registerResponseVOS);
            }else {
                logisticsOperateService.pushOperateLog(null,
                        null, BusinessTypeEnum.REGISTER_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(registerRequests), JSONUtil.toJsonStr(registerResult),false);
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + registerResult.getMsg());
            }
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(null,
                    null, BusinessTypeEnum.REGISTER_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(registerRequests), JSONUtil.toJsonStr(e),true);
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    /**
     * @description: 格式化传参
     * @author Will
     * @date: 2024/4/9 17:14
     * @param list
     * @return List<OceanRegisterRequest>
     */
    private List<OceanRegisterRequest> handleOceanRegisterRequest (List<LogisticsTrackBaseDTO.OceanRegisterRequestDTO> list) {
        List<OceanRegisterRequest> registerRequests = new ArrayList<>();
        for (LogisticsTrackBaseDTO.OceanRegisterRequestDTO oceanRegisterRequestDTO : list) {
            OceanRegisterRequest build = OceanRegisterRequest.builder()
                    .trackingNo(oceanRegisterRequestDTO.getTrackNo())
                    .carrierCode(oceanRegisterRequestDTO.getCarrierCode())
                    .type(MathUtil.THREE)
                    .build();
            registerRequests.add(build);
        }
        return registerRequests;
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
        if (org.apache.commons.lang3.StringUtils.isBlank(platform)) {
            return Collections.emptyList();
        }
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
        map.put(CLIENT_SECRET, cfgAppClient.getClientSecret());
        map.put("clientId", cfgAppClient.getClientId());
        return Collections.singletonList(map);
    }
    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap){
        try {
            TrackResponse trackResponse = trackShipperService.getCourierList(authMap.get(CLIENT_SECRET));
            if (!NUMBER.equalsIgnoreCase(trackResponse.getCode()))  {
                //授权失败
                return failure("授权失败");
            }else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }
    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.TRACK123;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
