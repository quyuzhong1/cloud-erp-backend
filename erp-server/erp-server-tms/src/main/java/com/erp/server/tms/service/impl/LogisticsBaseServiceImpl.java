package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.enums.TrackQueryTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.AuthTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import com.erp.model.tms.dto.LogisticsTrackBaseDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import com.erp.model.tms.enums.LogisticsThirdChannelRefPushTypeEnum;
import com.erp.model.tms.enums.TrackPlatformTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.RegisterResponseVO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.convert.LogisticsAddressConverter;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.*;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.address.SellerResponse;
import com.erp.tms.aliexpress.model.order.request.Address;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import com.erp.tms.aliexpress.util.ApiException;
import com.google.common.collect.Lists;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyAddressResp;
import com.sdk.oms.tiktok.service.TikTokFullService;
import com.xxl.job.core.context.XxlJobHelper;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName LogisticsBaseServiceImpl
 * @date 2023年11月15日
 * @version: 1.0
 */
@Slf4j
@Service
public class LogisticsBaseServiceImpl implements LogisticsBaseService {
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
    private TikTokFullService tikTokFullService;
    @Resource
    private LogisticsThirdChannelRefService logisticsThirdChannelRefService;
    @Resource
    private DictBasicService dictBasicService;

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
        } else if (LogisticsPlatformEnum.SPT.getCode().equalsIgnoreCase(platform)) {
          return syncSingleChannel(platform);
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
        if (CollectionUtils.isEmpty(records) || StringUtils.isBlank(platformType)){
            return Collections.emptyList();
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>(records.size());
        LogisticsService service = logisticsRegistry.getHandler(platformType);
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platformType);
        if (CollectionUtils.isEmpty(mapList)) {
            return resultDTOS;
        }
        //跟据类型判断走小包、海运
        ApiResult<List<LogisticsTrackEntity>> track;
        if (CharSequenceUtil.equals(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode(),transportType)) {
            track = processTrackExpressDeliveryData(mapList, records, service);
        } else {
            track = processTrackOceanData(mapList,records,service);
        }
        if (track.isSuccess()) {
            List<LogisticsTrackEntity> data = track.getData();
            if (CollectionUtils.isEmpty(data)){
                return resultDTOS;
            }
            //排序分组
            Map<String, List<LogisticsTrackEntity>> collect = data.stream().sorted(Comparator.comparing(LogisticsTrackEntity::getTrackTime)).collect(Collectors.groupingBy(LogisticsTrackEntity::getTrackNo));
            //根据记录进行更新物流信息
            for (LogisticsTrackDTO.UpdateTrackDTO record : records) {
                BatchResultDTO dto = new BatchResultDTO();
                String trackNo = TrackQueryTypeEnum.TRACK_NO.getCode().equals(record.getTrackQueryType()) && CharSequenceUtil.isNotBlank(record.getTrackNo()) ? record.getTrackNo() : record.getTransportNo();
                if (CharSequenceUtil.isBlank(trackNo) && CharSequenceUtil.isNotBlank(record.getTrackNo())){
                    trackNo = record.getTrackNo();
                }
                if (CharSequenceUtil.isBlank(trackNo)){
                    continue;
                }
                //获取对应编号的轨迹
                List<LogisticsTrackEntity> newList = collect.get(trackNo);
                if (CollectionUtils.isEmpty(newList)){
                    continue;
                }
                //增量数据库记录
                logisticsTrackService.saveIncrementTrackData(trackNo, newList);
                //获取最新记录
                LogisticsTrackEntity maxTrack = newList.stream().max(Comparator.comparing(LogisticsTrackEntity::getTrackTime)).orElse(null);
                //根据跟踪号进行更新操作
                logisticsBillDetailService.updateLogisticsBillDetailByTrackNo(maxTrack);
                dto.setCode(trackNo);
                dto.setSuccess(true);
                resultDTOS.add(dto);
            }
        } else {
            records.forEach(record -> {
                BatchResultDTO dto = new BatchResultDTO();
                dto.setId(record.getId());
                String trackNo = TrackQueryTypeEnum.TRACK_NO.getCode().equals(record.getTrackQueryType()) && CharSequenceUtil.isNotBlank(record.getTrackNo()) ? record.getTrackNo() : record.getTransportNo();
                if (CharSequenceUtil.isBlank(trackNo) && CharSequenceUtil.isNotBlank(record.getTrackNo())){
                    trackNo = record.getTrackNo();
                }
                dto.setCode(trackNo);
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
        for (LogisticsTrackDTO.UpdateTrackDTO record : records) {
            String trackNo = TrackQueryTypeEnum.TRACK_NO.getCode().equals(record.getTrackQueryType()) && CharSequenceUtil.isNotBlank(record.getTrackNo()) ? record.getTrackNo() : record.getTransportNo();
            if (CharSequenceUtil.isBlank(trackNo) && CharSequenceUtil.isNotBlank(record.getTrackNo())){
                trackNo = record.getTrackNo();
            }
            if (CharSequenceUtil.isBlank(trackNo)){
                continue;
            }
            logisticsRegisterVOS.add(LogisticsRegisterVO.builder()
                    .trackNo(trackNo)
                    .phoneSuffix(record.getTelNumber())
                    .build());
        }
        if (CollectionUtils.isEmpty(logisticsRegisterVOS)){
            return ApiResult.success(null);
        }
        LogisticsTrackVO logisticsTrackVO = LogisticsTrackVO.builder()
                .authMap(mapList.get(0))
                .trackNos(logisticsRegisterVOS.stream().map(LogisticsRegisterVO::getTrackNo).distinct().collect(Collectors.toList()))
                .build();
        return service.getTrack(logisticsTrackVO);
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
            String orderNo = logisticsBillDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), updateTrackDTO.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getPlatformOrderNo())).orElse("");
            if (CharSequenceUtil.isBlank(orderNo)) {
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
    public List<LogisticsBillDetailDTO.BillDetailDTO> processRegisterData(String platformType, List<LogisticsTrackDTO.UpdateTrackDTO> records, String transportType, List<LogisticsThirdChannelRefDTO.PagingVO> channelRefList) {
        if (CollectionUtils.isEmpty(records)){
            return Collections.emptyList();
        }
        LogisticsService service = logisticsRegistry.getHandler(platformType);
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platformType);
        if (CollectionUtils.isEmpty(mapList)) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        //查询过滤单号开头配置
        List<DictBasicEntity> dictList = dictBasicService.getByKeyList(Collections.singletonList("trackNoFilterPrefix"));
        List<String> prefixList = dictList.stream().map(DictBasicEntity::getCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        //构建注册数据
        List<LogisticsTrackDTO.UpdateTrackDTO> record2s = buildRegisterData(records,channelRefList,prefixList);
        //跟据类型判断走小包、海运
        ApiResult<List<RegisterResponseVO>> listApiResult;
        if (CharSequenceUtil.equals(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode(),transportType)) {
            listApiResult = processRegisterExpressDeliveryData(mapList, record2s, service);
        } else {
            listApiResult = processRegisterOceanData(mapList,record2s,service);
        }
        if (Objects.isNull(listApiResult)){
            return Collections.emptyList();
        }
        List<LogisticsBillDetailDTO.BillDetailErrorDTO> errorList = new ArrayList<>();
        List<LogisticsBillDetailDTO.BillDetailDTO> sucessList = new ArrayList<>();
        for (LogisticsTrackDTO.UpdateTrackDTO record : record2s) {
            if (!listApiResult.isSuccess() || CollectionUtils.isEmpty(listApiResult.getData())) {
                continue;
            }
            String trackNo = TrackQueryTypeEnum.TRACK_NO.getCode().equals(record.getTrackQueryType()) && CharSequenceUtil.isNotBlank(record.getTrackNo()) ? record.getTrackNo() : record.getTransportNo();
            if (CharSequenceUtil.isBlank(trackNo) && CharSequenceUtil.isNotBlank(record.getTrackNo())){
                trackNo = record.getTrackNo();
            }
            if (CharSequenceUtil.isBlank(trackNo)){
                continue;
            }
            String finalTrackNo = trackNo;
            RegisterResponseVO registerResponseVO = listApiResult.getData().stream().filter(e -> Objects.equals(finalTrackNo, e.getTrackNo())).findFirst().orElse(null);
            if (Objects.isNull(registerResponseVO)){
                continue;
            }
            if (Objects.nonNull(registerResponseVO.getTrackStatus()) && registerResponseVO.getTrackStatus()){
                sucessList.add(LogisticsBillDetailDTO.BillDetailDTO.builder().trackNo(trackNo).platformOrderNo(record.getPlatformOrderNo()).build());
            }else {
                errorList.add(LogisticsBillDetailDTO.BillDetailErrorDTO.builder().id(record.getId()).errorMsg(registerResponseVO.getMsg()).build());
            }
        }
        if (CollectionUtils.isNotEmpty(errorList)){
            logisticsBillDetailService.updateRegisterStatus(errorList, -1);
        }
        if (CollectionUtils.isNotEmpty(sucessList)){
            logisticsBillDetailService.updateRegisterStatusByParams(sucessList, 1);
        }
        return sucessList;
    }

    private List<LogisticsTrackDTO.UpdateTrackDTO> buildRegisterData(List<LogisticsTrackDTO.UpdateTrackDTO> records, List<LogisticsThirdChannelRefDTO.PagingVO> channelRefList, List<String> prefixList) {
        if (CollUtil.isEmpty(records)){
            return Collections.emptyList();
        }
        List<LogisticsTrackDTO.UpdateTrackDTO> record2s = new ArrayList<>();
        List<String> detailIds = new ArrayList<>();
        for (LogisticsTrackDTO.UpdateTrackDTO record : records) {
            String trackNo = TrackQueryTypeEnum.TRACK_NO.getCode().equals(record.getTrackQueryType()) && CharSequenceUtil.isNotBlank(record.getTrackNo()) ? record.getTrackNo() : record.getTransportNo();
            if (CharSequenceUtil.isBlank(trackNo) && CharSequenceUtil.isNotBlank(record.getTrackNo())){
                trackNo = record.getTrackNo();
            }
            if (CharSequenceUtil.isBlank(trackNo)){
                detailIds.add(record.getId());
                continue;
            }
            //根据配置过滤是否符合配置
            if (CollUtil.isNotEmpty(prefixList)){
                //判断是否符合配置
                if (prefixList.stream().anyMatch(trackNo::startsWith)){
                    detailIds.add(record.getId());
                    continue;
                }
            }
            if (CollUtil.isEmpty(channelRefList)){
                continue;
            }
            //处理供应商编码和手机号
//            List<LogisticsThirdChannelRefDTO.PagingVO> collect = channelRefList.stream().filter(e -> e.getLogisticsChannelName().equals(record.getChannelName()) && !e.getDisabled()).collect(Collectors.toList());
            List<LogisticsThirdChannelRefDTO.PagingVO> collect = channelRefList.stream().filter(e->Objects.equals(e.getId(),record.getThirdRefId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(collect)){
                record.setTelNumber("");
                record.setThirdSupplierCode("");
                record2s.add(record);
                continue;
            }
            LogisticsThirdChannelRefDTO.PagingVO pagingVO = collect.get(0);
            Boolean isPushMobile = pagingVO.getIsPushMobile();
            if (!isPushMobile){
                record.setTelNumber("");
                record.setThirdSupplierCode(pagingVO.getThirdSupplierCode());
                record.setThirdRefId(pagingVO.getId());
                record2s.add(record);
                continue;
            }
            String pushType = pagingVO.getPushType();
            record.setThirdSupplierCode(pagingVO.getThirdSupplierCode());
            if (LogisticsThirdChannelRefPushTypeEnum.SENDER.getCode().equals(pushType) || LogisticsThirdChannelRefPushTypeEnum.RECEIVER.getCode().equals(pushType)){
                record.setTelNumber(pagingVO.getMobile());
                record.setThirdRefId(pagingVO.getId());
                record2s.add(record);
            }else if (LogisticsThirdChannelRefPushTypeEnum.SHOP_SENDER.getCode().equals(pushType)){
                //销售出库单把客户id传递到了物流单店铺id上
                LogisticsThirdChannelRefDTO.PagingVO pagingVO1 = collect.stream().filter(e -> e.getCustomerId().equals(record.getShopId()) || e.getShopId().equals(record.getShopId())).findFirst().orElse(null);
                if (Objects.nonNull(pagingVO1)){
                    record.setTelNumber(pagingVO1.getMobile());
                    record.setThirdRefId(pagingVO.getId());
                    record2s.add(record);
                }else {
                    record2s.add(record);
                }
            }else if (LogisticsThirdChannelRefPushTypeEnum.PLATFORM_SENDER.getCode().equals(pushType)){
                LogisticsThirdChannelRefDTO.PagingVO pagingVO1 = collect.stream().filter(e -> e.getDictPlatform().equals(record.getSalesPlatform())).findFirst().orElse(null);
                if (Objects.nonNull(pagingVO1)) {
                    record.setTelNumber(pagingVO1.getMobile());
                    record.setThirdRefId(pagingVO.getId());
                    record2s.add(record);
                }else {
                    record2s.add(record);
                }
            }else if (LogisticsThirdChannelRefPushTypeEnum.ORDER_RECEIVER.getCode().equals(pushType)){
                record.setThirdRefId(pagingVO.getId());
                record2s.add(record);
            }
        }
        if (CollUtil.isNotEmpty(detailIds)){
            logisticsBillDetailService.updateTrackEnableByIds(detailIds);
        }
        //更新注册手机号和关联关系
        List<LogisticsTrackDTO.UpdateTrackDTO> refList = record2s.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getThirdRefId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(refList)){
            logisticsBillDetailService.updateRegisterParams(refList);
        }
        return record2s;
    }

    /**
     * @param mapList
     * @param records
     * @param service
     * @return ApiResult<List < LogisticsTrackEntity>>
     * @description: 获取快递运单轨迹
     * @author Will
     * @date: 2024/4/8 14:44
     */
    private ApiResult<List<RegisterResponseVO>> processRegisterExpressDeliveryData  (List<Map<String, String>> mapList, List<LogisticsTrackDTO.UpdateTrackDTO> records, LogisticsService service) {
        if (CollectionUtils.isEmpty(records)){
            return ApiResult.success(null);
        }
        List<LogisticsRegisterVO> logisticsRegisterVOS = new ArrayList<>();
        //根据配置进行组装注册数据
        for (LogisticsTrackDTO.UpdateTrackDTO record : records) {
            String trackNo = TrackQueryTypeEnum.TRACK_NO.getCode().equals(record.getTrackQueryType()) && CharSequenceUtil.isNotBlank(record.getTrackNo()) ? record.getTrackNo() : record.getTransportNo();
            if (CharSequenceUtil.isBlank(trackNo) && CharSequenceUtil.isNotBlank(record.getTrackNo())){
                trackNo = record.getTrackNo();
            }
            if (CharSequenceUtil.isBlank(trackNo)){
                continue;
            }
            logisticsRegisterVOS.add(LogisticsRegisterVO.builder()
                    .trackNo(trackNo)
                    .phoneSuffix(record.getTelNumber())
                    .build());
        }
        if (CollectionUtils.isEmpty(logisticsRegisterVOS)){
            return ApiResult.success(null);
        }
        RegisterTrackVO registerTrackVO = RegisterTrackVO.builder().authMap(mapList.get(0)).logisticsRegisterVOS(logisticsRegisterVOS).build();
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

    /**
     * 批量更新物流轨迹信息（增强版：支持多平台自动识别、注册与分片同步）
     * <p>
     * 核心流程：按平台枚举逐一处理 → 查询待注册/已注册单据 → 尝试注册 → 将注册成功单据并入已注册池 → 分片同步轨迹
     * <p>
     * 注意：每轮平台的待同步任务池（readyToSync）必须在循环内独立构建，严禁提升至循环外共享，
     * 否则会将其他平台的单据以错误的 platformType 调用接口，导致数据错乱。
     * <p>
     * ===================== 性能优化指南（dtos 数量较大时必读）=====================
     * <p>
     * 【优化点 1】渠道配置提前批量缓存（当前：N次 DB/Feign 查询 → 优化后：1次）
     *   当前实现在每轮平台枚举内调用 listByPlatform()，导致有多少个平台枚举就发起多少次查询。
     *   渠道配置属于低频变更的配置类数据，应在循环外一次性加载所有平台配置，
     *   按 platformCode 分组为 Map&lt;String, List&lt;PagingVO&gt;&gt;，循环内直接 Map.get() 取用。
     *   参考：logisticsThirdChannelRefService.listAll() + Collectors.groupingBy(PagingVO::getPlatform)
     * <p>
     * 【优化点 2】轨迹分片同步改为并行执行（当前：最坏 dtos.size() 次串行 HTTP → 优化后：并发执行）
     *   当 batchSize=1（如快递100等平台）时，500条数据对应 500次串行 HTTP 请求，
     *   按每次 800ms 估算总耗时约 400秒，在定时任务场景下极易触发超时或任务积压。
     *   建议引入专用线程池（如 trackSyncExecutor），使用 CompletableFuture.supplyAsync()
     *   并行提交所有分片任务，并统一设置超时（如 30s）防止单次 HTTP 无限阻塞。
     *   注意：并行化后需确认下游接口的 QPS 限制，避免并发过高触发限流。
     * <p>
     * 【优化点 3】dtos 按平台预分组，减少 IN 查询数据量（当前：每平台查全量 trackNos）
     *   当前实现将所有 trackNos 传入每个平台的 listWaitingRegisterByConfig() 查询，
     *   若 DTO 本身已携带平台标识字段（如 trackQueryType），可提前按平台分组，
     *   每轮查询只传入当前平台相关的 trackNos，大幅缩小 IN 子句的参数规模。
     * =========================================================================
     *
     * @param dtos          待更新的轨迹数据列表
     * @param transportType 运输类型（小包/海运），对应 {@link com.common.business.enums.LogisticsTransportTypeEnum}
     * @return 更新结果清单
     * @author jack
     * @date 2026-04-02
     */
    @Override
    public List<BatchResultDTO> batchUpdateTrackInfo(List<LogisticsTrackDTO.UpdateTrackDTO> dtos, String transportType) {
        if (CollUtil.isEmpty(dtos)) {
            return Collections.emptyList();
        }

        List<BatchResultDTO> resultList = new ArrayList<>(dtos.size());
        // 提取所有有效跟踪单号，用于后续按平台统一查询（仅取 trackNo 字段，transportNo 由 service 层自行处理）
        List<String> trackNos = dtos.stream()
                .map(LogisticsTrackDTO.UpdateTrackDTO::getTrackNo)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        for (TrackPlatformTypeEnum typeEnums : TrackPlatformTypeEnum.values()) {
            // 【第一步】查询当前平台下所有待处理的单据（包含已注册与待注册两类）
            LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                    .trackQueryMode(typeEnums.getCode())
                    .trackNoList(trackNos)
                    .trackEnable(true)
                    .transportType(transportType)
                    .build();
            List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillDetailService.listWaitingRegisterByConfig(query, typeEnums.getCode());
            log.warn("【{}】批量更新物流轨迹信息查询到：{} 条", typeEnums.getName(), list.size());

            // 【第二步】按注册状态拆分：registerStatus=1 已注册，registerStatus=0 待注册
            List<LogisticsTrackDTO.UpdateTrackDTO> registered = list.stream()
                    .filter(e -> e.getRegisterStatus() == 1)
                    .collect(Collectors.toList());
            log.warn("【{}】批量更新物流轨迹信息查询到已注册数：{}", typeEnums.getName(), registered.size());

            List<LogisticsTrackDTO.UpdateTrackDTO> unregistered = list.stream()
                    .filter(e -> e.getRegisterStatus() == 0)
                    .collect(Collectors.toList());
            log.warn("【{}】批量更新物流轨迹信息查询到待注册数：{}", typeEnums.getName(), unregistered.size());

            // 【第三步】对待注册单据执行注册动作，获取本轮注册成功的单据集合
            List<LogisticsThirdChannelRefDTO.PagingVO> configs = logisticsThirdChannelRefService.listByPlatform(typeEnums.getCode());
            List<LogisticsBillDetailDTO.BillDetailDTO> successList = processRegisterData(typeEnums.getCode(), unregistered, transportType, configs);

            Set<String> successNos = CollUtil.isEmpty(successList) ? Collections.emptySet() :
                    successList.stream().map(LogisticsBillDetailDTO.BillDetailDTO::getTrackNo).collect(Collectors.toSet());

            // 将本轮注册成功的任务对象找出，用于后续轨迹同步（实现"即注册即查"）
            List<LogisticsTrackDTO.UpdateTrackDTO> successTasks = unregistered.stream()
                    .filter(m -> successNos.contains(m.getTrackNo()))
                    .collect(Collectors.toList());

            // 汇总本轮注册失败的结果：注册失败则无法查轨迹，直接标记失败并跳过
            unregistered.stream()
                    .filter(m -> !successNos.contains(m.getTrackNo()))
                    .forEach(m -> resultList.add(BatchResultDTO.fail(m.getId(), m.getTrackNo(), typeEnums.getName() + "自动匹配平台注册失败")));

            // 【第四步】构建本轮平台的待同步任务池：已注册单据 + 本轮注册成功单据
            // 关键：readyToSync 为循环内局部变量，严禁提升至循环外，防止跨平台数据污染
            List<LogisticsTrackDTO.UpdateTrackDTO> readyToSync = new ArrayList<>(registered.size() + successTasks.size());
            readyToSync.addAll(registered);
            readyToSync.addAll(successTasks);

            if (CollUtil.isEmpty(readyToSync)) {
                continue;
            }
            // Track123 支持批量查询（100条/批），快递100 等仅支持单次查询（1条/批,请求频率30次/秒）
            int batchSize = LogisticsPlatformEnum.TRACK123.getCode().equals(typeEnums.getCode()) ? 100 : 30;
            List<List<LogisticsTrackDTO.UpdateTrackDTO>> chunks = Lists.partition(readyToSync, batchSize);
            for (List<LogisticsTrackDTO.UpdateTrackDTO> chunk : chunks) {
                resultList.addAll(processTrackData(typeEnums.getCode(), chunk, transportType));
            }
        }
        return resultList;
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
            result = shopInfoFeign.getShopListByParam(AuthTypeEnum.SHOP.getCode(), AuthStatusEnum.ALREADY.getCode(),"");
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
        String shopName = authMap.get("shopName");
        try {
            sellerInfo = aliExpressShipperService.getLogisticsAddress(authMap);
        } catch (ApiException e) {
            XxlJobHelper.log("获取店铺:{}物流地址异常：{}", authMap.get("shopId"), e.getMessage());
            return;
        }
        SellerResponse responseMsg = JSON.parseObject(sellerInfo.getBody(), SellerResponse.class);
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
                sender.setName(sender.getName() + "-" + shopName);
                list.add(sender);
            });
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(pickups)) {
            List<LogisticsAddressEntity> addressEntities = LogisticsAddressConverter.INSTANCE.sellerAddressToLogisticsAddress(pickups);
            addressEntities.forEach(sender -> {
                sender.setType(LogisticsAddressTypeEnum.COLLECT);
                sender.setIsBySync(true);
                sender.setShopId(shopId);
                sender.setName(sender.getName() + "-" + shopName);
                list.add(sender);
            });
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(refunds)) {
            List<LogisticsAddressEntity> addressEntities = LogisticsAddressConverter.INSTANCE.sellerAddressToLogisticsAddress(refunds);
            addressEntities.forEach(sender -> {
                sender.setType(LogisticsAddressTypeEnum.REFUND);
                sender.setIsBySync(true);
                sender.setShopId(shopId);
                sender.setName(sender.getName() + "-" + shopName);
                list.add(sender);
            });
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)) {
            logisticsAddressService.batchSaveOrUpdateLogisticsAddress(list);
        }
    }

    @Override
    public void syncTikTokLogisticsAddress(String shopId, String shopName) {
        List<LogisticsAddressEntity> dbList = logisticsAddressService.listByTypeAndShopId(LogisticsAddressTypeEnum.COLLECT, shopId);
        List<TikTokFullyAddressResp.DataDTO.AddressesDTO> tiktokFullyAddressList = tikTokFullService.listAddress(shopId);
        List<LogisticsAddressEntity> saveOrUpdateList = new ArrayList<>();
        List<String> tiktokFullyAddressesIds = tiktokFullyAddressList.stream().map(TikTokFullyAddressResp.DataDTO.AddressesDTO::getId).collect(Collectors.toList());
        List<String> deleteIdList = dbList.stream()
                .filter(dbAddress -> !tiktokFullyAddressesIds.contains(dbAddress.getAddressId()))
                .map(BaseEntity::getId)
                .collect(Collectors.toList());
        for (TikTokFullyAddressResp.DataDTO.AddressesDTO addressesDTO : tiktokFullyAddressList) {
            LogisticsAddressEntity logisticsAddressEntity = dbList.stream()
                    .filter(dbAddress -> dbAddress.getAddressId().equals(addressesDTO.getId()))
                    .findFirst()
                    .orElse(new LogisticsAddressEntity());
            logisticsAddressEntity.setShopId(shopId);
            logisticsAddressEntity.setName(addressesDTO.getContactName() + "-" + shopName);
            logisticsAddressEntity.setType(LogisticsAddressTypeEnum.COLLECT);
            logisticsAddressEntity.setContact(addressesDTO.getContactName());
            logisticsAddressEntity.setAddressFirst(addressesDTO.getFullAddress());
            logisticsAddressEntity.setTelNumber(addressesDTO.getPhoneNumber());
            logisticsAddressEntity.setAddressId(addressesDTO.getId());
            logisticsAddressEntity.setCountryName(addressesDTO.getDetail().getCountryName());
            logisticsAddressEntity.setProvinceName(addressesDTO.getDetail().getProvinceName());
            logisticsAddressEntity.setCityName(addressesDTO.getDetail().getCityName());
            logisticsAddressEntity.setDistrictName(addressesDTO.getDetail().getDistrictName());
            logisticsAddressEntity.setStreet(addressesDTO.getDetail().getTownName());
            logisticsAddressEntity.setAddressSecond(addressesDTO.getDetail().getBuilding());
            logisticsAddressEntity.setIsBySync(true);
            saveOrUpdateList.add(logisticsAddressEntity);
        }
        if(CollectionUtils.isNotEmpty(deleteIdList)){
            logisticsAddressService.removeByIds(deleteIdList);
        }
        if (CollectionUtils.isNotEmpty(saveOrUpdateList)){
            logisticsAddressService.saveOrUpdateBatch(saveOrUpdateList);
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
}
