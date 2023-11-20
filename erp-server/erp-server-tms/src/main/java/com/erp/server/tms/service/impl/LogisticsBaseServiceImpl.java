package com.erp.server.tms.service.impl;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.request.LogisticsTrackVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsBaseService;
import com.erp.server.tms.service.LogisticsSaleChannelService;
import com.erp.server.tms.service.LogisticsService;
import com.erp.server.tms.service.LogisticsTrackService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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
    public void processTrackData(String platformType, List<LogisticsBillDetailEntity> records) {
        LogisticsService service = logisticsRegistry.getHandler(platformType);
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platformType);
        if (CollectionUtils.isEmpty(mapList)) return;
        LogisticsTrackVO logisticsTrackVO = LogisticsTrackVO.builder()
                .authMap(mapList.get(0))
                .trackNos(records.stream().map(LogisticsBillDetailEntity::getTrackNo).collect(Collectors.toList()))
                .build();
        ApiResult<List<LogisticsTrackEntity>> track = service.getTrack(logisticsTrackVO);
        if (track.isSuccess()){
            List<LogisticsTrackEntity> data = track.getData();
            if (CollectionUtils.isNotEmpty(data)){
                Map<String, List<LogisticsTrackEntity>> collect = data.stream().sorted(Comparator.comparing(LogisticsTrackEntity::getTrackTime)).collect(Collectors.groupingBy(LogisticsTrackEntity::getTrackNo));
                //根据记录进行更新物流信息
                records.forEach(logisticsBillDetailEntity -> {
                    //获取对应编号的轨迹
                    List<LogisticsTrackEntity> logisticsTrackEntities = collect.get(logisticsBillDetailEntity.getTrackNo());
                    //先物理删除  再新增
                    if (CollectionUtils.isNotEmpty(logisticsTrackEntities)){
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
                });
                data.forEach(logisticsTrackEntity -> {
                    logisticsTrackService.saveOrUpdate(logisticsTrackEntity);
                });
            }
        }
    }

    private ApiResult syncSingleChannel(String platform) {
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

    private ApiResult syncShoppeeChannel(String platform) {
        log.info("{}渠道同步开始", platform);
        ApiResult<List<ShopAuthEntity>> result = shopeeFeign.getShopeeShopList("shopee_shop", "already");
        if (result.isSuccess()) {
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
