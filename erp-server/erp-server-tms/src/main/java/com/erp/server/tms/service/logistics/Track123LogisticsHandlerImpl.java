package com.erp.server.tms.service.logistics;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.BeanMapperUtils;
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
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.DictBasicService;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.track123.model.request.ExtendField;
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
    @Resource
    private TrackShipperService trackShipperService;
    @Resource
    private TrackShipperOceanService trackShipperOceanService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsOperateService logisticsOperateService;
    @Resource
    private DictBasicService dictBasicService;
    private final static String HAS_BEEN_IMPORTED = "The order number has been imported";
    private final static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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
            if (Objects.nonNull(track) && "00000".equalsIgnoreCase(track.getCode())) {
                //查询成功的单号
                List<TrackDetail> accepted = track.getData().getAccepted().getContent();
                if (CollectionUtils.isNotEmpty(accepted)) {
                    accepted.forEach(trackDetail -> {
                        String trackingStatus = trackDetail.getTransitStatus();
                        List<TrackingDetail> trackingDetails = trackDetail.getLocalLogisticsInfo().getTrackingDetails();
                        if(CollectionUtils.isNotEmpty(trackingDetails)){
                            //本地物流
                            trackingDetails.forEach(trackingDetail -> {
                                LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
                                logisticsTrackEntity.setTrackNo(trackDetail.getTrackNo());
                                logisticsTrackEntity.setOrderStatus(convertTrackStatus(trackingStatus));//转换类型
                                logisticsTrackEntity.setStatus(convertTrackStatus(trackingDetail.getTransitSubStatus()));//转换类型
                                LocalDateTime eventTime = LocalDateTime.parse(trackingDetail.getEventTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
        return DigestUtil.md5Hex(trackingDetail.getTrackNo() + "-" + trackingDetail.getContent() + "-" + trackTime);
    }

    @Override
    public ApiResult<List<LogisticsTrackEntity>> getOceanTrack(List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> oceanTrackRequestList) {
        List<LogisticsTrackEntity> logisticsTrackList = new ArrayList<>();
        ValidatorUtil.validateEntity(oceanTrackRequestList);
        try {
            TrackOceanResponse<OceanResponseData> track = trackShipperOceanService.getTrack(oceanTrackRequestList.get(0).getAuthMap().get("clientSecret"), oceanTrackRequestList);
            //成功
            if ("00000".equalsIgnoreCase(track.getCode())) {
                //查询成功的单号
                List<OceanTrackInfo> accepted = track.getData().getAccepted();
                if (CollectionUtils.isNotEmpty(accepted)) {
                    accepted.forEach(trackDetail -> {
                        List<OceanContainerInfo> containerInfoList = trackDetail.getContainerInfo();
                        if (CollectionUtils.isNotEmpty(containerInfoList)){
                            containerInfoList.forEach(oceanContainerInfo -> {
                                List<OceanTrackingDetail> trackingDetails = oceanContainerInfo.getTrackingDetails();
                                if(CollectionUtils.isNotEmpty(trackingDetails)){
                                    //本地物流
                                    trackingDetails.forEach(trackingDetail -> {
                                        LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
                                        logisticsTrackEntity.setTrackNo(trackDetail.getTrackingNo());
                                        logisticsTrackEntity.setStatus(convertOceanTrackStatus(trackingDetail.getEventStatus()));//转换类型
                                        LocalDateTime eventTime = LocalDateTime.parse(trackingDetail.getEventTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                        logisticsTrackEntity.setTrackTime(eventTime);
                                        logisticsTrackEntity.setContent(trackingDetail.getEventDetails());
                                        logisticsTrackEntity.setTransportType(LogisticsTransportTypeEnum.OCEAN.getCode());
                                        logisticsTrackList.add(logisticsTrackEntity);
                                    });
                                }
//                                else{
//                                    LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
//                                    logisticsTrackEntity.setTrackNo(trackDetail.getTrackingNo());
//                                    logisticsTrackEntity.setStatus(LogisticTrackStatusEnum.OCEAN_TRACK_ING.getCode());//转换类型
//                                    LocalDateTime eventTime = LocalDateTime.parse(trackDetail.getCreateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//                                    logisticsTrackEntity.setTrackTime(eventTime);
//                                    logisticsTrackEntity.setContent("暂无信息");
//                                    logisticsTrackEntity.setTransportType(LogisticsTransportTypeEnum.OCEAN.getCode());
//                                    logisticsTrackList.add(logisticsTrackEntity);
//                                }
                            });
                        }
                    });
                }
                //查询失败的单号
                List<Rejected> rejecteds = track.getData().getRejected();
                if (CollectionUtils.isNotEmpty(rejecteds)) {
                    rejecteds.forEach(rejected -> {
                        LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
                        logisticsTrackEntity.setTrackNo(rejected.getTrackingNo());
                        logisticsTrackEntity.setStatus(LogisticTrackStatusEnum.NOT_FIND.getCode());
                        logisticsTrackEntity.setContent(rejected.getError().getCode() + ":" + rejected.getError().getMsg());
                        logisticsTrackEntity.setTrackTime(LocalDateTime.now());
                        logisticsTrackEntity.setTransportType(LogisticsTransportTypeEnum.OCEAN.getCode());
                        logisticsTrackList.add(logisticsTrackEntity);
                    });
                }
                logisticsOperateService.pullOperateLog(null,
                        null, BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(oceanTrackRequestList), JSONUtil.toJsonStr(track));
                return success(logisticsTrackList);
            } else {
                logisticsOperateService.pullOperateLog(null,
                        null, BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(oceanTrackRequestList), JSONUtil.toJsonStr(track));
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + track.getMsg());
            }
        } catch (Exception e) {
            logisticsOperateService.pullOperateLog(null,
                    null, BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(oceanTrackRequestList), JSONUtil.toJsonStr(e));
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + e.getMessage());
        }
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
        String token = authMap.get("clientSecret");
        List<RegisterResponseVO> registerResponseVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(logisticsRegisterVOS)) {
            return failure("注册数据不能为空");

        }
        List<RegisterRequest> registerRequests = new ArrayList<>();
        logisticsRegisterVOS.forEach(e ->{
            RegisterRequest registerRequest = new RegisterRequest();
            BeanMapperUtils.copy(e, registerRequest);
            if (CharSequenceUtil.isBlank(e.getPhoneSuffix())){
                registerRequest.setExtendFieldMap(null);
            }else {
                ExtendField extendFieldMap = new ExtendField();
                extendFieldMap.setPhoneSuffix(getPhoneSuffix4(e.getPhoneSuffix()));
                registerRequest.setExtendFieldMap(extendFieldMap);
            }
            registerRequests.add(registerRequest);
        });
        ValidatorUtil.validateEntity(registerRequests);
        try {
            log.warn("注册运单号：{}", JSONUtil.toJsonStr(registerRequests));
            RegisterResult registerResult = trackShipperService.registerLogisticsNumber(token, registerRequests);
            //成功
            if (Objects.nonNull(registerResult) && "00000".equalsIgnoreCase(registerResult.getCode())) {
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
                        if (Objects.nonNull(rejected1.getError()) && StringUtils.isNotEmpty(rejected1.getError().getMsg())
                        && rejected1.getError().getMsg().equals(HAS_BEEN_IMPORTED)){
                            //已导入的运单号，返回成功
                            registerResponseVOS.add(RegisterResponseVO.builder().trackNo(rejected1.getTrackNo()).trackStatus(true).build());
                        }else {
                            registerResponseVOS.add(RegisterResponseVO.builder().trackNo(rejected1.getTrackNo()).trackStatus(false)
                                    .code(rejected1.getError().getCode())
                                    .msg(rejected1.getError().getMsg()).build());
                        }
                    });
                }
                logisticsOperateService.pushOperateLog(null,
                        null, BusinessTypeEnum.REGISTER_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(registerTrackVO), JSONUtil.toJsonStr(registerResult), false);
                return success(registerResponseVOS);
            }else {
                logisticsOperateService.pushOperateLog(null,
                        null, BusinessTypeEnum.REGISTER_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(registerTrackVO), JSONUtil.toJsonStr(registerResult),false);
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + registerResult.getMsg());
            }
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(null,
                    null, BusinessTypeEnum.REGISTER_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(registerTrackVO), JSONUtil.toJsonStr(e),true);
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    @Override
    public ApiResult<List<RegisterResponseVO>> updateTrack(RegisterTrackVO registerTrackVO) {
        List<LogisticsRegisterVO> logisticsRegisterVOS = registerTrackVO.getLogisticsRegisterVOS();
        Map<String, String> authMap = registerTrackVO.getAuthMap();
        String token = authMap.get("clientSecret");
        if (CollectionUtils.isEmpty(logisticsRegisterVOS)) {
            return failure("注册数据不能为空");

        }
        List<RegisterRequest> registerRequests = new ArrayList<>();
        logisticsRegisterVOS.forEach(e ->{
            RegisterRequest registerRequest = new RegisterRequest();
            BeanMapperUtils.copy(e, registerRequest);
            if (CharSequenceUtil.isBlank(e.getPhoneSuffix())){
                registerRequest.setExtendFieldMap(null);
            }else {
                ExtendField extendFieldMap = new ExtendField();
                extendFieldMap.setPhoneSuffix(getPhoneSuffix4(e.getPhoneSuffix()));
                registerRequest.setExtendFieldMap(extendFieldMap);
            }
            registerRequests.add(registerRequest);
        });
        ValidatorUtil.validateEntity(registerRequests);
        try {
            log.warn("更新运单号请求：token:{},request:{}", token, JSONUtil.toJsonStr(registerRequests));
//            RegisterResult result = trackShipperService.updateTrack(token, registerRequests);
            log.warn("更新运单号结果：{}",JSONUtil.toJsonStr(null));
            return success();
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(null,
                    null, BusinessTypeEnum.UPDATE_TRACK.getCode(), LogisticsPlatformEnum.TRACK123.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(registerTrackVO), JSONUtil.toJsonStr(e),true);
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + e.getMessage());
        }
    }


    private String getPhoneSuffix4(String phoneSuffix){
        if (StringUtils.isEmpty(phoneSuffix)){
            return "";
        }
        //获取手机号后四位
        if (phoneSuffix.length()<=4){
            return phoneSuffix;
        }else {
            return phoneSuffix.substring(phoneSuffix.length() - 4);
        }
    }
    @Override
    public ApiResult<List<RegisterResponseVO>> oceanRegisterLogisticsNumber(List<LogisticsTrackBaseDTO.OceanRegisterRequestDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return failure("注册数据不能为空");
        }
        Map<String, String> authMap = list.get(0).getAuthMap();
        String token = authMap.get("clientSecret");
        List<RegisterResponseVO> registerResponseVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return failure("注册数据不能为空");
        }
        List<OceanRegisterRequest> registerRequests = handleOceanRegisterRequest(list);
        ValidatorUtil.validateEntity(registerRequests);
        try {
            OceanRegisterResult registerResult = trackShipperOceanService.registerLogisticsNumber(token, registerRequests);
            //成功
            if ("00000".equalsIgnoreCase(registerResult.getCode())) {
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
        map.put("clientSecret", cfgAppClient.getClientSecret());
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
            TrackResponse trackResponse = trackShipperService.getCourierList(authMap.get("clientSecret"));
            if (!"00000".equalsIgnoreCase(trackResponse.getCode()))  {
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
