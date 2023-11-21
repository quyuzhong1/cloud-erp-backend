package com.erp.server.tms.service.impl;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.RegisterResponseVO;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.*;
import com.google.common.collect.Lists;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName LogisticsBaseServiceImpl
 * @description: TODO
 * @date 2023年11月15日
 * @version: 1.0
 */
@Slf4j
@Service
public class LogisticsBaseServiceImpl implements LogisticsBaseService {
    @Resource
    private ShopeeFeign shopeeFeign;
    @Resource
    private LogisticsRegistry logisticsRegistry;
    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;
    @Resource
    private LogisticsTrackService logisticsTrackService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;


    @Override
    public ApiResult syncLogisticsChannel(String platform) {
        if (LogisticsPlatformEnum.SHOPEE.getCode().equalsIgnoreCase(platform)) {
            return syncShoppeeChannel(platform);
        } else {
            return syncSingleChannel(platform);
        }
    }

    @Override
    public ApiResult syncAllLogisticsChannel() {
        log.info("====全部渠道同步开始=====");
        LogisticsPlatformEnum[] platformEnums = LogisticsPlatformEnum.values();
        for (LogisticsPlatformEnum platformEnum : platformEnums) {
            if (LogisticsPlatformEnum.SHOPEE.getCode().equalsIgnoreCase(platformEnum.getCode())) {
                syncShoppeeChannel(platformEnum.getCode());
            } else {
                syncSingleChannel(platformEnum.getCode());
            }
        }
        log.info("=====渠道同步结束=====");
        return ApiResult.success();
    }

    @Override
    public List<LogisticsOrderResponseVO> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<LogisticsOrderResponseVO> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(logisticsQueryVOList)) {
            logisticsQueryVOList.forEach(logisticsQueryBaseVO -> {
                Map<String, String> authMap = logisticsQueryBaseVO.getAuthMap();
                if (Objects.nonNull(authMap.get("logisticsPlatform"))) {
                    LogisticsService service = logisticsRegistry.getHandler(authMap.get("logisticsPlatform"));
                    ApiResult<List<LogisticsOrderResponseVO>> listApiResult = service.queryOrderList(logisticsQueryVOList);
                    if (listApiResult.isSuccess()) {
                        list.addAll(listApiResult.getData());
                    }
                }
            });
        }

        return list;
    }

    /**
     * 根据平台类型获取物流轨迹
     *
     * @param platformType
     * @param records
     */
    @Override
    public List<BatchResultDTO> processTrackData(String platformType, List<LogisticsBillDetailEntity> records) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(records.size());
        LogisticsService service = logisticsRegistry.getHandler(platformType);
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platformType);
        if (CollectionUtils.isEmpty(mapList)) Collections.emptyList();
        LogisticsTrackVO logisticsTrackVO = LogisticsTrackVO.builder()
                .authMap(mapList.get(0))
                .trackNos(records.stream().map(LogisticsBillDetailEntity::getTrackNo).collect(Collectors.toList()))
                .build();
        ApiResult<List<LogisticsTrackEntity>> track = service.getTrack(logisticsTrackVO);
        if (track.isSuccess()) {
            List<LogisticsTrackEntity> data = track.getData();
            if (CollectionUtils.isNotEmpty(data)) {
                Map<String, List<LogisticsTrackEntity>> collect = data.stream().sorted(Comparator.comparing(LogisticsTrackEntity::getTrackTime)).collect(Collectors.groupingBy(LogisticsTrackEntity::getTrackNo));
                //根据记录进行更新物流信息
                records.forEach(logisticsBillDetailEntity -> {
                    BatchResultDTO dto = new BatchResultDTO();
                    //获取对应编号的轨迹
                    List<LogisticsTrackEntity> logisticsTrackEntities = collect.get(logisticsBillDetailEntity.getTrackNo());
                    //先物理删除  再新增
                    if (CollectionUtils.isNotEmpty(logisticsTrackEntities)) {
                        //删除
                        logisticsTrackService.deleteByTrackNo(logisticsBillDetailEntity.getTrackNo());
                        //新增
                        logisticsTrackService.saveBatch(logisticsTrackEntities);
                        //TODO 根据记录最新状态修改订单状态
                        //Student latest = Collections.max(studentList,
                        //                                 Comparator.comparing(s -> s.getDate()));
                        LogisticsTrackEntity max = Collections.max(logisticsTrackEntities, Comparator.comparing(LogisticsTrackEntity::getTrackTime));
                        logisticsTrackService.checkTrackStatus(max);
                    }
                    dto.setId(logisticsBillDetailEntity.getId());
                    dto.setCode(logisticsBillDetailEntity.getTrackNo());
                    dto.setSuccess(true);
                    resultDTOS.add(dto);
                });
                data.forEach(logisticsTrackEntity -> {
                    logisticsTrackService.saveOrUpdate(logisticsTrackEntity);
                });
            }
        } else {
            records.forEach(logisticsBillDetailEntity -> {
                BatchResultDTO dto = new BatchResultDTO();
                dto.setId(logisticsBillDetailEntity.getId());
                dto.setCode(logisticsBillDetailEntity.getTrackNo());
                dto.setSuccess(false);
                dto.setMsg(track.getMsg());
                resultDTOS.add(dto);
            });
        }
        return resultDTOS;
    }

    @Override
    public List<BatchResultDTO> processRegisterData(String platformType, List<LogisticsBillDetailEntity> records) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(records.size());
        LogisticsService service = logisticsRegistry.getHandler(platformType);
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platformType);
        if (CollectionUtils.isEmpty(mapList)) return Collections.emptyList();
        if (CollectionUtils.isEmpty(records)) return Collections.emptyList();
        RegisterTrackVO registerTrackVO = RegisterTrackVO.builder()
                .authMap(mapList.get(0))
                .logisticsRegisterVOS(convertRegisterData(records))
                .build();
        ApiResult<List<RegisterResponseVO>> listApiResult = service.registerLogisticsNumber(registerTrackVO);
        if (listApiResult.isSuccess()) {
            List<RegisterResponseVO> data = listApiResult.getData();
            if (CollectionUtils.isNotEmpty(data)) {
                Map<String, LogisticsBillDetailEntity> collect = records.stream().collect(Collectors.toMap(LogisticsBillDetailEntity::getTrackNo, Function.identity()));
                data.forEach(registerResponseVO -> {
                    BatchResultDTO dto = new BatchResultDTO();
                    LogisticsBillDetailEntity logisticsBillDetailEntity = collect.get(registerResponseVO.getTrackNo());
                    if (registerResponseVO.getTrackStatus()) {
                        logisticsBillDetailEntity.setRegisterStatus(1);
                        dto.setSuccess(true);
                    } else {
                        //已注册
                        if (registerResponseVO.getCode().equalsIgnoreCase("A0400")) {
                            dto.setSuccess(true);
                            logisticsBillDetailEntity.setRegisterStatus(1);
                            logisticsBillDetailEntity.setRegisterResult(registerResponseVO.getMsg());
                        } else {
                            dto.setSuccess(false);
                            logisticsBillDetailEntity.setRegisterStatus(-1);
                            logisticsBillDetailEntity.setRegisterResult(registerResponseVO.getMsg());
                        }
                    }
                    dto.setId(logisticsBillDetailEntity.getId());
                    dto.setCode(logisticsBillDetailEntity.getTrackNo());
                    dto.setMsg(registerResponseVO.getMsg());
                    resultDTOS.add(dto);
                    logisticsBillDetailEntity.setUpdateTime(LocalDateTime.now());
                    logisticsBillDetailService.updateById(logisticsBillDetailEntity);
                });
            }
        } else {
            records.forEach(logisticsBillDetailEntity -> {
                BatchResultDTO dto = new BatchResultDTO();
                dto.setId(logisticsBillDetailEntity.getId());
                dto.setCode(logisticsBillDetailEntity.getTrackNo());
                dto.setSuccess(false);
                dto.setMsg(listApiResult.getMsg());
                resultDTOS.add(dto);
            });
        }
        return resultDTOS;
    }

    /**
     * 数据转换
     *
     * @param records
     * @return
     */
    private List<LogisticsRegisterVO> convertRegisterData(List<LogisticsBillDetailEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        return LogisticsChannelConverter.INSTANCE.convertRegisterDataByTrack123(records);
    }

    @Override
    public List<BatchResultDTO> batchUpdateTrackInfo(List<LogisticsBillDetailEntity> logisticsBillDetailEntities) {
        if (CollectionUtils.isNotEmpty(logisticsBillDetailEntities)) {
            List<BatchResultDTO> dtoList = new ArrayList<>(logisticsBillDetailEntities.size());
            LogisticsBillDetailEntity detailEntity = logisticsBillDetailEntities.stream().filter(e -> StringUtils.isBlank(e.getTrackNo())).findFirst().orElse(null);
            if (Objects.nonNull(detailEntity)) throw new ServiceException(ApiError.BATCH_UPDATE_TRACK_INFO_HAS_EMPTY);
            if (logisticsBillDetailEntities.size() > 100) {
                List<List<LogisticsBillDetailEntity>> partition = Lists.partition(logisticsBillDetailEntities, 100);
                for (List<LogisticsBillDetailEntity> entityList : partition) {
                    dtoList.addAll(processTrackData(LogisticsPlatformEnum.TRACK123.getCode(), entityList));
                }
            } else {
                dtoList.addAll(processTrackData(LogisticsPlatformEnum.TRACK123.getCode(), logisticsBillDetailEntities));
            }
            return dtoList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public ApiResult syncSingleChannel(String platform) {
        log.info("{}渠道同步开始", platform);
        ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
        LogisticsService service = logisticsRegistry.getHandler(platform);
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platform);
        if (CollectionUtils.isEmpty(mapList)) return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR);
        mapList.forEach(map -> {
            chanelQueryVO.setAuthMap(map);
            ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
            //把结果存储数据库
            if (channels.isSuccess()) {
                channels.getData().forEach(logisticsSaleChannelEntity -> {
                    logisticsSaleChannelEntity.setAuthId(map.get("id"));
                    logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
                });
            } else {
                log.error("渠道查询异常：{}", channels.getMsg());
            }
        });
        log.info("{}渠道同步结束", platform);
        return ApiResult.success();
    }

    public ApiResult syncShoppeeChannel(String platform) {
        log.info("{}渠道同步开始", platform);
        ApiResult<List<ShopAuthEntity>> result = null;
        try {
            result = shopeeFeign.getShopeeShopList("shopee_shop", "already");
        }catch (Exception e){
            log.error("erp-oms服务接口getShopeeShopList异常：{}",e.getMessage());
        }

        if (Objects.nonNull(result) && result.isSuccess()) {
            LogisticsService service = logisticsRegistry.getHandler(platform);
            result.getData().forEach(shopAuthEntity -> {
                ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
                Map<String, String> map = service.getLogisticsAuthConfig(shopAuthEntity.getShopId());
                chanelQueryVO.setAuthMap(map);
                ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
                if (channels.isSuccess()) {
                    channels.getData().forEach(logisticsSaleChannelEntity -> {
                        logisticsSaleChannelEntity.setAuthId(shopAuthEntity.getShopId());
                        logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
                    });

                } else {
                    log.error(channels.getMsg());
                }
            });
        }
        log.info("{}渠道同步结束", platform);
        return ApiResult.success();
    }
}
