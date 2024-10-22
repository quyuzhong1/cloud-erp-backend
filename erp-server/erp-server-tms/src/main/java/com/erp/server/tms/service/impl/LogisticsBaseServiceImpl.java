package com.erp.server.tms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.enums.TrackQueryTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.AuthTypeEnum;
import com.erp.model.tms.dto.LogisticsTrackBaseDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.RegisterResponseVO;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.erp.server.tms.convert.LogisticsAddressConverter;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.rocketmq.PlatformTrackConsumerService;
import com.erp.server.tms.service.*;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.address.SellerResponse;
import com.erp.tms.aliexpress.model.order.request.Address;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import com.erp.tms.aliexpress.util.ApiException;
import com.google.common.collect.Lists;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDetail;
import com.sdk.tms.track123.model.response.LocalLogisticsInfo;
import com.sdk.tms.track123.model.response.TrackDetail;
import com.sdk.tms.track123.model.response.TrackingDetail;
import com.xxl.job.core.context.XxlJobHelper;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private PlatformTrackConsumerService platformTrackConsumerService;

    @Override
    public List<BatchResultDTO> syncLogisticsChannel(String platform) {
        List<BatchResultDTO> batchResultDTOS = null;
        ApiResult<List<ShopAuthEntity>> shopeeShopList = null;
        if (LogisticsPlatformEnum.SHOPEE.getCode().equalsIgnoreCase(platform)) {
            return syncShoppeeChannel(platform);
        } else if (LogisticsPlatformEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(platform)) {
            return syncAliExpressChannel(platform, new HashMap<>());
        } else if (LogisticsPlatformEnum.SHOPIFY.getCode().equalsIgnoreCase(platform)) {
            return syncShopifyChannel(platform);
        } else if (LogisticsPlatformEnum.TIK_TOK.getCode().equalsIgnoreCase(platform)) {
            return syncTikTokChannel(platform);
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
    public List<BatchResultDTO> processTrackData(String platformType, List<LogisticsTrackDTO.UpdateTrackDTO> records,String transportType) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(records.size());
        LogisticsService service = logisticsRegistry.getHandler(platformType);
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platformType);
        if (CollectionUtils.isEmpty(mapList)) {
            Collections.emptyList();
        }
        //跟据类型判断走小包、海运
        ApiResult<List<LogisticsTrackEntity>> track;
        if (StrUtil.equals(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode(),transportType)) {
            track = processTrackExpressDeliveryData(mapList, records, service);
        } else {
            track = processTrackOceanData(mapList,records,service);
        }
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
                        //根据记录最新状态修改订单状态
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

    /**
     * @description: 获取快递运单轨迹
     * @author Will
     * @date: 2024/4/8 14:44
     * @param mapList
     * @param records
     * @param service
     * @return ApiResult<List<LogisticsTrackEntity>>
     */
    private ApiResult<List<LogisticsTrackEntity>> processTrackExpressDeliveryData (List<Map<String, String>> mapList,List<LogisticsTrackDTO.UpdateTrackDTO> records,LogisticsService service) {
        if (CollectionUtils.isEmpty(records)){
            return ApiResult.success(null);
        }
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
            return ApiResult.success(null);
        }
        LogisticsTrackVO logisticsTrackVO = LogisticsTrackVO.builder()
                .authMap(mapList.get(0))
                .trackNos(logisticsRegisterVOS.stream().map(LogisticsRegisterVO::getTrackNo).distinct().collect(Collectors.toList()))
                .build();
        ApiResult<List<LogisticsTrackEntity>> track = service.getTrack(logisticsTrackVO);
        return track;
    }

    /**
     * @description: 获取海运运单轨迹
     * @author Will
     * @date: 2024/4/8 14:43
     * @param mapList
     * @param records
     * @param service
     * @return ApiResult<List<LogisticsTrackEntity>>
     */
    private ApiResult<List<LogisticsTrackEntity>> processTrackOceanData (List<Map<String, String>> mapList,List<LogisticsTrackDTO.UpdateTrackDTO> records,LogisticsService service) {
        List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> list = new ArrayList<>();
        //查询海运orderNo
        List<String> ids = records.stream().map(LogisticsTrackDTO.UpdateTrackDTO::getId).collect(Collectors.toList());
        List<LogisticsBillDetailEntity> logisticsBillDetailList = logisticsBillDetailService.listByIds(ids);

        for (LogisticsTrackDTO.UpdateTrackDTO updateTrackDTO :records) {
            String orderNo = logisticsBillDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), updateTrackDTO.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getPlatformOrderNo())).orElse("");
            if (StrUtil.isBlank(orderNo)) {
               return new ApiResult<>(10000,"未发现跟踪单对应平台订单");
            }
            LogisticsTrackBaseDTO.OceanTrackRequestDTO oceanTrackRequestDTO = LogisticsTrackBaseDTO.OceanTrackRequestDTO.builder()
                    .trackingNo(updateTrackDTO.getTrackNo())
                    .orderNo(orderNo)
                    .type(MathUtil.THREE)
                    .authMap(mapList.get(0))
                    .build();
            list.add(oceanTrackRequestDTO);
        }
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException("未找到注册信息");
        }
        ApiResult<List<LogisticsTrackEntity>> track = service.getOceanTrack(list);
        return track;
    }

    @Override
    public List<BatchResultDTO> processRegisterData(String platformType, List<LogisticsTrackDTO.UpdateTrackDTO> records,String transportType) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(records.size());
        LogisticsService service = logisticsRegistry.getHandler(platformType);
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platformType);
        if (CollectionUtils.isEmpty(mapList)) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        //跟据类型判断走小包、海运
        ApiResult<List<RegisterResponseVO>> listApiResult;
        if (StrUtil.equals(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode(),transportType)) {
            listApiResult = processRegisterExpressDeliveryData(mapList, records, service);
        } else {
            listApiResult = processRegisterOceanData(mapList,records,service);
        }
        if (listApiResult.isSuccess()) {
            List<RegisterResponseVO> data = listApiResult.getData();
            if (CollectionUtils.isEmpty(data)) {
                return resultDTOS;
            }
            List<LogisticsBillDetailEntity> updateList = new ArrayList<>();
            List<String> detailIds = records.stream().map(LogisticsTrackDTO.UpdateTrackDTO::getId).distinct().collect(Collectors.toList());
            List<LogisticsBillDetailEntity> detailList = logisticsBillDetailService.listByIds(detailIds);
            data.forEach(registerResponseVO -> {
                //根据配置进行过滤符合条件的记录 增加渠道为空的情况处理
                List<LogisticsTrackDTO.UpdateTrackDTO> updateTrackDTOList = records.stream().filter(e -> Objects.nonNull(e)
                                && ((TrackQueryTypeEnum.TRACK_NO.getCode().equals(e.getTrackQueryType()) && registerResponseVO.getTrackNo().equals(e.getTrackNo()))
                                || (registerResponseVO.getTrackNo().equals(e.getTransportNo()))))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(updateTrackDTOList)){
                    updateTrackDTOList.forEach(updateTrackDTO -> {
                        BatchResultDTO dto = new BatchResultDTO();
                        String logisticsNo = updateTrackDTO.getTransportNo();
                        if (TrackQueryTypeEnum.TRACK_NO.getCode().equals(updateTrackDTO.getTrackQueryType())){
                            logisticsNo = updateTrackDTO.getTrackNo();
                        }
                        LogisticsBillDetailEntity logisticsBillDetailEntity = detailList.stream().filter(e -> e.getId().equals(updateTrackDTO.getId())).findFirst().orElse(null);
                        if (Objects.nonNull(logisticsBillDetailEntity)){
                            if (registerResponseVO.getTrackStatus()) {
                                logisticsBillDetailEntity.setRegisterStatus(1);
                                logisticsBillDetailEntity.setPlatformOrderNo(registerResponseVO.getOrderNo());
                                dto.setSuccess(true);
                            } else {
                                dto.setSuccess(false);
                                logisticsBillDetailEntity.setRegisterStatus(-1);
                                logisticsBillDetailEntity.setRegisterResult(registerResponseVO.getMsg());
                            }
                            dto.setId(updateTrackDTO.getId());
                            dto.setCode(logisticsNo);
                            dto.setMsg(registerResponseVO.getMsg());
                            resultDTOS.add(dto);
                            logisticsBillDetailEntity.setUpdateTime(LocalDateTime.now());
                            updateList.add(logisticsBillDetailEntity);
                        }
                    });
                }
            });
            if (CollectionUtils.isNotEmpty(updateList)){
                logisticsBillDetailService.updateBatchById(updateList);
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
     * @description: 获取快递运单轨迹
     * @author Will
     * @date: 2024/4/8 14:44
     * @param mapList
     * @param records
     * @param service
     * @return ApiResult<List<LogisticsTrackEntity>>
     */
    private ApiResult<List<RegisterResponseVO>> processRegisterExpressDeliveryData  (List<Map<String, String>> mapList,List<LogisticsTrackDTO.UpdateTrackDTO> records,LogisticsService service) {
        if (CollectionUtils.isEmpty(records)){
            return ApiResult.success(null);
        }
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
            return ApiResult.success(null);
        }
        RegisterTrackVO registerTrackVO = RegisterTrackVO.builder()
                .authMap(mapList.get(0))
                .logisticsRegisterVOS(logisticsRegisterVOS)
                .build();
        return service.registerLogisticsNumber(registerTrackVO);
    }

    /**
     * @description: 获取海运运单轨迹
     * @author Will
     * @date: 2024/4/8 14:43
     * @param mapList
     * @param records
     * @param service
     * @return ApiResult<List<LogisticsTrackEntity>>
     */
    private ApiResult<List<RegisterResponseVO>> processRegisterOceanData (List<Map<String, String>> mapList,List<LogisticsTrackDTO.UpdateTrackDTO> records,LogisticsService service) {
        List<LogisticsTrackBaseDTO.OceanRegisterRequestDTO> requestList = new ArrayList<>();
        for (LogisticsTrackDTO.UpdateTrackDTO updateTrackDTO :records) {
            LogisticsTrackBaseDTO.OceanRegisterRequestDTO oceanRegisterRequestDTO = LogisticsTrackBaseDTO.OceanRegisterRequestDTO.builder()
                    .trackNo(updateTrackDTO.getTrackNo())
                    .carrierCode(updateTrackDTO.getCarrierCode())
                    .id(updateTrackDTO.getId())
                    .type(MathUtil.THREE)
                    .authMap(mapList.get(0))
                    .build();
            requestList.add(oceanRegisterRequestDTO);
        }
        ApiResult<List<RegisterResponseVO>> track = service.oceanRegisterLogisticsNumber(requestList);
        return track;
    }

    @Override
    public List<BatchResultDTO> batchUpdateTrackInfo(List<LogisticsTrackDTO.UpdateTrackDTO> dtos,String transportType) {
        if (CollectionUtils.isNotEmpty(dtos)) {
            List<BatchResultDTO> dtoList = new ArrayList<>(dtos.size());
            LogisticsTrackDTO.UpdateTrackDTO dto = dtos.stream().filter(e -> StringUtils.isBlank(e.getTrackNo())).findFirst().orElse(null);
            if (Objects.nonNull(dto)) {
                throw new ServiceException(ApiError.BATCH_UPDATE_TRACK_INFO_HAS_EMPTY);
            }
            if (dtos.size() > 100) {
                List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = Lists.partition(dtos, 100);
                for (List<LogisticsTrackDTO.UpdateTrackDTO> entityList : partition) {
                    dtoList.addAll(processTrackData(LogisticsPlatformEnum.TRACK123.getCode(), entityList,transportType));
                }
            } else {
                dtoList.addAll(processTrackData(LogisticsPlatformEnum.TRACK123.getCode(), dtos,transportType));
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
        if (ObjectUtil.isEmpty(service)) {
            return Collections.emptyList();
        }
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platform);
        if (CollectionUtils.isEmpty(mapList)) {
            return Collections.emptyList();
        }
        List<BatchResultDTO> batchResultDTOS = new ArrayList<>(mapList.size());
        mapList.forEach(map -> {
            chanelQueryVO.setAuthMap(map);
            ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
            logisticsSaleChannelService.updateSaleChannelByPlatform(platform, MathUtil.ONE);
            //把结果存储数据库
            if (channels.isSuccess()) {
                channels.getData().forEach(logisticsSaleChannelEntity -> {
                    if (LogisticsPlatformEnum.ALI_EXPRESS.getCode().equals(platform)){
                        logisticsSaleChannelEntity.setServicePlatform("tms");
                    }
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
            result = shopeeFeign.getShopeeShopList(AuthTypeEnum.SHOP.getCode(), AuthStatusEnum.ALREADY.getCode());
        } catch (Exception e) {
            log.error("erp-oms服务接口getShopeeShopList异常：{}", e.getMessage());
        }
        List<BatchResultDTO> batchResultDTOS = new ArrayList<>();
        if (Objects.nonNull(result) && result.isSuccess()) {
            LogisticsService service = logisticsRegistry.getHandler(platform);
            List<ShopAuthEntity> data = result.getData();
            if (CollectionUtils.isEmpty(data)) {
                return batchResultDTOS;
            }
            //先暂停该渠道数据，然后进行更新动作
            logisticsSaleChannelService.updateSaleChannelByPlatform(platform, MathUtil.ONE);

            for (ShopAuthEntity shopAuthEntity : data) {
                ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
                Map<String, String> map = service.getLogisticsAuthConfigByShopId(shopAuthEntity.getShopId());
                if (CollectionUtils.isEmpty(map)) {
                    continue;
                }
                chanelQueryVO.setAuthMap(map);
                ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);

                if (channels.isSuccess()) {
                    channels.getData().forEach(logisticsSaleChannelEntity -> {
                        if (LogisticsPlatformEnum.ALI_EXPRESS.getCode().equals(platform)){
                            logisticsSaleChannelEntity.setServicePlatform("tms");
                        }
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
    public List<BatchResultDTO> syncAliExpressChannel(String platform, Map<String, String> map1) {
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
            if (CollectionUtils.isEmpty(data)) {
                return batchResultDTOS;
            }
            //先暂停该渠道数据，然后进行更新动作
//            logisticsSaleChannelService.updateSaleChannelByPlatform(platform, MathUtil.ONE); 速卖通需要进行渠道增量
            for (ShopAuthEntity shopAuthEntity : data) {
                ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
                Map<String, String> map = service.getLogisticsAuthConfigByShopId(shopAuthEntity.getShopId());
                if (CollectionUtils.isEmpty(map)) {
                    continue;
                }
                map.put("token", shopAuthEntity.getToken());
                //覆盖配置
                if (Objects.nonNull(map1.get("orderId"))){
                    map.put("orderId", map1.get("orderId"));
                }
                if (Objects.nonNull(map1.get("childOrderId"))){
                    map.put("childOrderId", map1.get("childOrderId"));
                }
                chanelQueryVO.setAuthMap(map);
                log.info("授权信息：{}",JSONObject.toJSON(chanelQueryVO));
                ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
                log.info("获取渠道结果：{}",JSONObject.toJSON(channels));

                if (channels.isSuccess() && Objects.nonNull(channels.getData())) {
                    channels.getData().forEach(logisticsSaleChannelEntity -> {
                        if (LogisticsPlatformEnum.ALI_EXPRESS.getCode().equals(platform)){
                            logisticsSaleChannelEntity.setServicePlatform("tms");
                        }
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

        //先暂停该渠道数据，然后进行更新动作
        logisticsSaleChannelService.updateSaleChannelByPlatform(platform, MathUtil.ONE);

        List<BatchResultDTO> batchResultDTOS = new ArrayList<>();
        LogisticsService service = logisticsRegistry.getHandler(platform);
        for (ShopAuthEntity shopAuthEntity : data) {
            ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
            Map<String, String> authMap = new HashMap<>();
            authMap.put("shopId", shopAuthEntity.getShopId());
            chanelQueryVO.setAuthMap(authMap);
            ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);

            if (channels.isSuccess()) {
                channels.getData().forEach(logisticsSaleChannelEntity -> {
                    if (LogisticsPlatformEnum.ALI_EXPRESS.getCode().equals(platform)){
                        logisticsSaleChannelEntity.setServicePlatform("tms");
                    }
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

    @Override
    public void processMongoTrackData(String platformType, List<LogisticsTrackDTO.UpdateTrackDTO> records, String transportType) {
        List<String> trackNoList = records.stream().map(LogisticsTrackDTO.UpdateTrackDTO::getTrackNo).distinct().collect(Collectors.toList());
        String result = dmpMongoDbFeign.listMongoTractDataByTrackNoList(trackNoList);
        if (StrUtil.isBlank(result)){
            return;
        }
        List<TrackDetail> trackDetails = JSONUtil.toList(result, TrackDetail.class);
        //构建mq消费实体
        List<PlatformTrackDTO> dtoList = new ArrayList<>();
        trackDetails.forEach(e -> {
            PlatformTrackDTO dto = new PlatformTrackDTO();
            dto.setTrackNo(e.getTrackNo());
            LocalLogisticsInfo localLogisticsInfo = e.getLocalLogisticsInfo();
            List<PlatformTrackDetail> details = new ArrayList<>();
            if (Objects.nonNull(localLogisticsInfo) || CollectionUtils.isNotEmpty(localLogisticsInfo.getTrackingDetails())){
//                PlatformTrackDetail detail = new PlatformTrackDetail();
//                detail.setTrackNo(e.getTrackNo());
//                detail.setStatus(convertTrackStatus(e.getTransitStatus()));//转换类型
//                LocalDateTime eventTime = LocalDateTime.parse(e.getCreateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//                detail.setTrackTime(eventTime);
//                detail.setContent("暂无信息");
//                details.add(detail);
//            }else {
                for (TrackingDetail trackingDetail : localLogisticsInfo.getTrackingDetails()) {
                    PlatformTrackDetail detail = new PlatformTrackDetail();
                    detail.setTrackNo(e.getTrackNo());
                    detail.setStatus(convertTrackStatus(trackingDetail.getTransitSubStatus()));//转换类型
                    LocalDateTime eventTime = LocalDateTime.parse(trackingDetail.getEventTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    detail.setTrackTime(eventTime);
                    detail.setContent(trackingDetail.getEventDetail());
                    details.add(detail);
                }
            }
            dto.setDetails(details);
            dtoList.add(dto);
        });
        if (CollectionUtils.isNotEmpty(dtoList)){
           dtoList.forEach(e -> logisticsTrackService.processTrackData(e));
        }
    }

    public List<BatchResultDTO> syncTikTokChannel(String platform) {
        log.info("{}渠道同步开始", platform);
        ApiResult<List<ShopAuthEntity>> result = null;
        try {
            result = shopInfoFeign.getAuthShopByPlatformType(platform);
        } catch (Exception e) {
            log.error("erp-oms服务接口getShopeeShopList异常：{}", e.getMessage());
        }
        List<BatchResultDTO> batchResultDTOS = new ArrayList<>();
        if (Objects.nonNull(result) && result.isSuccess()) {
            LogisticsService service = logisticsRegistry.getHandler(platform);
            List<ShopAuthEntity> data = result.getData();
            if (CollectionUtils.isEmpty(data)) return batchResultDTOS;
            //先暂停该渠道数据，然后进行更新动作
            logisticsSaleChannelService.updateSaleChannelByPlatform(platform, MathUtil.ONE);
            for (ShopAuthEntity shopAuthEntity : data) {
                ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
                Map<String, String> map = new HashMap<>();
                map.put("shopId", shopAuthEntity.getShopId());
                chanelQueryVO.setAuthMap(map);
                ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
                if (channels.isSuccess()) {
                    channels.getData().forEach(logisticsSaleChannelEntity -> {
                        if (LogisticsPlatformEnum.ALI_EXPRESS.getCode().equals(platform)){
                            logisticsSaleChannelEntity.setServicePlatform("tms");
                        }
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
}
