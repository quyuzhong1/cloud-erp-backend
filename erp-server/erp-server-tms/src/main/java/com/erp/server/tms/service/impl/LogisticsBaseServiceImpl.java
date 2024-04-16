package com.erp.server.tms.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.RegisterResponseVO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.erp.server.tms.convert.LogisticsAddressConverter;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.*;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.address.SellerResponse;
import com.erp.tms.aliexpress.model.order.request.Address;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import com.erp.tms.aliexpress.util.ApiException;
import com.google.common.collect.Lists;
import com.xxl.job.core.context.XxlJobHelper;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private LogisticsRegistry logisticsRegistry;
    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;
    @Resource
    private LogisticsTrackService logisticsTrackService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    @Resource
    private AliExpressShipperService aliExpressShipperService;
    @Resource
    private LogisticsAddressService logisticsAddressService;


    @Override
    public List<BatchResultDTO> syncLogisticsChannel(String platform) {
        List<BatchResultDTO> batchResultDTOS = null;
        ApiResult<List<ShopAuthEntity>> shopeeShopList = null;
        if (LogisticsPlatformEnum.SHOPEE.getCode().equalsIgnoreCase(platform)) {
            return syncShoppeeChannel(platform);
        } else if (LogisticsPlatformEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(platform)) {
            return syncAliExpressChannel(platform);
        } else if (LogisticsPlatformEnum.SHOPIFY.getCode().equalsIgnoreCase(platform)) {
            return syncShopifyChannel(platform);
        } else {
            return syncSingleChannel(platform);
        }
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
    public List<BatchResultDTO> processTrackData(String platformType, List<LogisticsTrackDTO.UpdateTrackDTO> records) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(records.size());
        LogisticsService service = logisticsRegistry.getHandler(platformType);
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platformType);
        if (CollectionUtils.isEmpty(mapList)) Collections.emptyList();
        LogisticsTrackVO logisticsTrackVO = LogisticsTrackVO.builder()
                .authMap(mapList.get(0))
                .trackNos(records.stream().map(LogisticsTrackDTO.UpdateTrackDTO::getTrackNo).collect(Collectors.toList()))
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
    public List<BatchResultDTO> processRegisterData(String platformType, List<LogisticsTrackDTO.UpdateTrackDTO> records) {
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
                Map<String, LogisticsTrackDTO.UpdateTrackDTO> collect = records.stream().collect(Collectors.toMap(LogisticsTrackDTO.UpdateTrackDTO::getTrackNo, Function.identity()));
                data.forEach(registerResponseVO -> {
                    BatchResultDTO dto = new BatchResultDTO();
                    LogisticsTrackDTO.UpdateTrackDTO updateTrackDTO = collect.get(registerResponseVO.getTrackNo());
                    LogisticsBillDetailEntity logisticsBillDetailEntity = logisticsBillDetailService.getById(updateTrackDTO.getId());
                    if (registerResponseVO.getTrackStatus()) {
                        logisticsBillDetailEntity.setRegisterStatus(1);
                        dto.setSuccess(true);
                    } else {
                        //已注册
//                        if (registerResponseVO.getCode().equalsIgnoreCase("A0400")) {
//                            dto.setSuccess(true);
//                            logisticsBillDetailEntity.setRegisterStatus(1);
//                            logisticsBillDetailEntity.setRegisterResult(registerResponseVO.getMsg());
//                        } else {
                        dto.setSuccess(false);
                        logisticsBillDetailEntity.setRegisterStatus(-1);
                        logisticsBillDetailEntity.setRegisterResult(registerResponseVO.getMsg());
//                        }
                    }
                    dto.setId(updateTrackDTO.getId());
                    dto.setCode(updateTrackDTO.getTrackNo());
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
    private List<LogisticsRegisterVO> convertRegisterData(List<LogisticsTrackDTO.UpdateTrackDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        return LogisticsChannelConverter.INSTANCE.convertRegisterDataByTrack123(records);
    }

    @Override
    public List<BatchResultDTO> batchUpdateTrackInfo(List<LogisticsTrackDTO.UpdateTrackDTO> dtos) {
        if (CollectionUtils.isNotEmpty(dtos)) {
            List<BatchResultDTO> dtoList = new ArrayList<>(dtos.size());
            LogisticsTrackDTO.UpdateTrackDTO dto = dtos.stream().filter(e -> StringUtils.isBlank(e.getTrackNo())).findFirst().orElse(null);
            if (Objects.nonNull(dto)) throw new ServiceException(ApiError.BATCH_UPDATE_TRACK_INFO_HAS_EMPTY);
            if (dtos.size() > 100) {
                List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = Lists.partition(dtos, 100);
                for (List<LogisticsTrackDTO.UpdateTrackDTO> entityList : partition) {
                    dtoList.addAll(processTrackData(LogisticsPlatformEnum.TRACK123.getCode(), entityList));
                }
            } else {
                dtoList.addAll(processTrackData(LogisticsPlatformEnum.TRACK123.getCode(), dtos));
            }
            return dtoList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<BatchResultDTO> syncSingleChannel(String platform) {
        log.info("{}渠道同步开始", platform);
        ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
        LogisticsService service = logisticsRegistry.getHandler(platform);
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platform);
        if (CollectionUtils.isEmpty(mapList)) return Collections.emptyList();
        List<BatchResultDTO> batchResultDTOS = new ArrayList<>(mapList.size());
        mapList.forEach(map -> {
            chanelQueryVO.setAuthMap(map);
            ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
            logisticsSaleChannelService.updateSaleChannelByPlatform(platform, MathUtil.ONE);
            //把结果存储数据库
            if (channels.isSuccess()) {
                channels.getData().forEach(logisticsSaleChannelEntity -> {
//                    logisticsSaleChannelEntity.setAuthId(map.get("id"));
                    logisticsSaleChannelEntity.setChannelStatus(MathUtil.ZERO);
                    logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
                });
                batchResultDTOS.add(BatchResultDTO.success(map.get("id"), String.valueOf(channels.getCode()), "同步成功"));
            } else {
                batchResultDTOS.add(BatchResultDTO.fail(map.get("id"), String.valueOf(channels.getCode()), channels.getMsg()));
                log.error("渠道查询异常：{}", channels.getMsg());
            }
        });
        log.info("{}渠道同步结束", platform);
        return batchResultDTOS;
    }

    @Override
    public List<BatchResultDTO> syncShoppeeChannel(String platform) {
        log.info("{}渠道同步开始", platform);
        ApiResult<List<ShopAuthEntity>> result = null;
        try {
            result = shopeeFeign.getShopeeShopList("shopee_shop", "already");
        } catch (Exception e) {
            log.error("erp-oms服务接口getShopeeShopList异常：{}", e.getMessage());
        }
        List<BatchResultDTO> batchResultDTOS = new ArrayList<>();
        if (Objects.nonNull(result) && result.isSuccess()) {
            LogisticsService service = logisticsRegistry.getHandler(platform);
            List<ShopAuthEntity> data = result.getData();
            if (CollectionUtils.isEmpty(data)) return batchResultDTOS;
            for (ShopAuthEntity shopAuthEntity : data) {
                ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
                Map<String, String> map = service.getLogisticsAuthConfig(shopAuthEntity.getShopId());
                if (CollectionUtils.isEmpty(map)) continue;
                chanelQueryVO.setAuthMap(map);
                ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
                //先暂停该渠道数据，然后进行更新动作
                logisticsSaleChannelService.updateSaleChannelByPlatform(platform, MathUtil.ONE);
                if (channels.isSuccess()) {
                    channels.getData().forEach(logisticsSaleChannelEntity -> {
//                        logisticsSaleChannelEntity.setAuthId(shopAuthEntity.getShopId());
                        logisticsSaleChannelEntity.setChannelStatus(MathUtil.ZERO);
                        logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
                    });
                    batchResultDTOS.add(BatchResultDTO.success(shopAuthEntity.getShopId(), String.valueOf(channels.getCode()), "同步成功"));
                } else {
                    batchResultDTOS.add(BatchResultDTO.fail(shopAuthEntity.getShopId(), String.valueOf(channels.getCode()), channels.getMsg()));
                    log.error(channels.getMsg());
                }
            }
        }
        log.info("{}渠道同步结束", platform);
        return batchResultDTOS;
    }

    @Override
    public List<BatchResultDTO> syncAliExpressChannel(String platform) {
        log.info("{}渠道同步开始", platform);
        ApiResult<List<ShopAuthEntity>> result = null;
        try {
            result = shopInfoFeign.getAuthShopByPlatformType(platform);
            log.info("获取店铺结果：{}",JSONObject.toJSON(result));
        } catch (Exception e) {
            log.error("erp-oms服务接口getShopeeShopList异常：{}", e.getMessage());
        }
        List<BatchResultDTO> batchResultDTOS = new ArrayList<>();
        if (Objects.nonNull(result) && result.isSuccess()) {
            LogisticsService service = logisticsRegistry.getHandler(platform);
            List<ShopAuthEntity> data = result.getData();
            if (CollectionUtils.isEmpty(data)) return batchResultDTOS;
            for (ShopAuthEntity shopAuthEntity : data) {
                ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
                Map<String, String> map = service.getLogisticsAuthConfig(shopAuthEntity.getShopId());
                if (CollectionUtils.isEmpty(map)) continue;
                map.put("token", shopAuthEntity.getToken());
                chanelQueryVO.setAuthMap(map);
                log.info("授权信息：{}",JSONObject.toJSON(chanelQueryVO));
                ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
                log.info("获取渠道结果：{}",JSONObject.toJSON(channels));
                //先暂停该渠道数据，然后进行更新动作
                logisticsSaleChannelService.updateSaleChannelByPlatform(platform, MathUtil.ONE);
                if (channels.isSuccess()) {
                    channels.getData().forEach(logisticsSaleChannelEntity -> {
                        logisticsSaleChannelEntity.setChannelStatus(MathUtil.ZERO);
                        logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
                    });
                    batchResultDTOS.add(BatchResultDTO.success(shopAuthEntity.getShopId(), String.valueOf(channels.getCode()), "同步成功"));
                } else {
                    batchResultDTOS.add(BatchResultDTO.fail(shopAuthEntity.getShopId(), String.valueOf(channels.getCode()), channels.getMsg()));
                    log.error(channels.getMsg());
                }
            }
        }
        log.info("{}渠道同步结束", platform);
        return batchResultDTOS;
    }

    @Override
    public List<BatchResultDTO> syncShopifyChannel(String platform) {
        log.info("{}渠道同步开始", platform);
        ApiResult<List<ShopAuthEntity>> result = shopInfoFeign.getAuthShopByPlatformType(platform);

        if (Objects.isNull(result)) {
            log.warn("{}渠道同步结束:查询已授权店铺为空", platform);
            return Collections.emptyList();
        }
        if (!result.isSuccess()){
            log.warn("{}渠道同步结束：查询已授权店铺异常：result={}", platform, JSONUtil.toJsonStr(result));
            return Collections.emptyList();
        }
        List<ShopAuthEntity> data = result.getData();
        if (CollectionUtils.isEmpty(data)){
            log.warn("{}渠道同步结束:无已授权店铺", platform);
            return Collections.emptyList();
        }

        List<BatchResultDTO> batchResultDTOS = new ArrayList<>();
        LogisticsService service = logisticsRegistry.getHandler(platform);
        for (ShopAuthEntity shopAuthEntity : data) {
            ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
            Map<String, String> authMap = new HashMap<>();
            authMap.put("shopId", shopAuthEntity.getShopId());
            chanelQueryVO.setAuthMap(authMap);
            ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
            //先暂停该渠道数据，然后进行更新动作
            logisticsSaleChannelService.updateSaleChannelByPlatform(platform, MathUtil.ONE);
            if (channels.isSuccess()) {
                channels.getData().forEach(logisticsSaleChannelEntity -> {
                    logisticsSaleChannelEntity.setChannelStatus(MathUtil.ZERO);
                    logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
                });
                batchResultDTOS.add(BatchResultDTO.success(shopAuthEntity.getShopId(), String.valueOf(channels.getCode()), "同步成功"));
            } else {
                batchResultDTOS.add(BatchResultDTO.fail(shopAuthEntity.getShopId(), String.valueOf(channels.getCode()), channels.getMsg()));
                log.error(channels.getMsg());
            }
        }

        log.info("{}渠道同步结束", platform);
        return batchResultDTOS;
    }

    @Override
    public void syncLogisticsAddress(Map<String, String> authMap) {
        IopResponse sellerInfo = null;
        String shopId = authMap.get("shopId");
        try {
            sellerInfo = aliExpressShipperService.getLogisticsAddress(authMap);
        } catch (ApiException e) {
            XxlJobHelper.log("获取店铺:{}物流地址异常：{}", authMap.get("shopId"), e.getMessage());
            return;
        }
        SellerResponse responseMsg = JSONObject.parseObject(sellerInfo.getBody(), SellerResponse.class);
        List<Address> senders = responseMsg.getSenders();
        List<Address> pickups = responseMsg.getPickups();
        List<Address> refunds = responseMsg.getRefunds();
        List<LogisticsAddressEntity> list = new ArrayList<>();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(senders)) {
            List<LogisticsAddressEntity> addressEntities = LogisticsAddressConverter.INSTANCE.sellerAddressToLogisticsAddress(senders);
            addressEntities.forEach(sender -> {
                sender.setType(LogisticsAddressTypeEnum.DELIVER);
                sender.setIsBySync(true);
                sender.setShopId(shopId);
                list.add(sender);
            });
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(pickups)) {
            List<LogisticsAddressEntity> addressEntities = LogisticsAddressConverter.INSTANCE.sellerAddressToLogisticsAddress(pickups);
            addressEntities.forEach(sender -> {
                sender.setType(LogisticsAddressTypeEnum.COLLECT);
                sender.setIsBySync(true);
                sender.setShopId(shopId);
                list.add(sender);
            });
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(refunds)) {
            List<LogisticsAddressEntity> addressEntities = LogisticsAddressConverter.INSTANCE.sellerAddressToLogisticsAddress(refunds);
            addressEntities.forEach(sender -> {
                sender.setType(LogisticsAddressTypeEnum.REFUND);
                sender.setIsBySync(true);
                sender.setShopId(shopId);
                list.add(sender);
            });
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)) {
            logisticsAddressService.batchSaveOrUpdateLogisticsAddress(list);
        }
    }
}
