package com.erp.server.tms.service.logistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.TrackQueryTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.model.tms.entity.LogisticsThirdChannelRefDetailEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.enums.LogisticsThirdChannelRefPushTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.LogisticsTrackVO;
import com.erp.model.tms.vo.request.RegisterTrackVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.model.tms.vo.response.RegisterResponseVO;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsOperateService;
import com.erp.server.tms.service.LogisticsThirdChannelRefService;
import com.sdk.tms.kuaidi100.model.request.Kuaidi100QueryParam;
import com.sdk.tms.kuaidi100.model.response.Kuaidi100QueryResponse;
import com.sdk.tms.kuaidi100.service.Kuaidi100Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能描述：快递100物流处理类（轻量化实现）
 * <p>
 * 该类实现了伪注册逻辑，满足ERP内部单据状态流转的一致性需求。
 * 轨迹拉取逻辑主要由DMP端的集成链路完成。
 * </p>
 *
 * @author jack
 * @date 2026-04-02
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.KUAIDI100)
public class Kuaidi100LogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private Kuaidi100Service kuaidi100Service;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private LogisticsThirdChannelRefService logisticsThirdChannelRefService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    @Resource
    private LogisticsOperateService logisticsOperateService;


    /**
     * 轨迹拉取（轻量化处理）
     * 快递100的轨迹同步主要通过DMP组件进行，此处作为接口实现的兜底。
     *
     * @param logisticsTrackVO 物流轨迹查询参数
     * @return 轨迹实体列表
     * @author jack
     * @date 2026-04-02
     */
    @Override
    public ApiResult<List<LogisticsTrackEntity>> getTrack(LogisticsTrackVO logisticsTrackVO) {
        List<String> trackNos = logisticsTrackVO.getTrackNos();
        if (CollUtil.isEmpty(trackNos)) {
            return success(Collections.emptyList());
        }

        // 频率限制：拉取 30 分钟内未更新的单据
        LocalDateTime updateTimeLimit = LocalDateTime.now().minusMinutes(60);
        // 轨迹时间范围（最近3个月）
        LocalDateTime trackStartTime = LocalDateTime.now().minusMonths(3);

        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(PlatformDictEnum.KUAIDI100.getCode())
                .registerStatus(1) // 已注册
                .trackEnable(true)
                .trackTime(trackStartTime)
                .trackNoList(trackNos)
                .trackEndTime(updateTimeLimit) // 过滤频率
                .transportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode())
                .build();

        // 3. 执行单号循环查询
        List<LogisticsTrackDTO.UpdateTrackDTO> records = logisticsBillDetailService.listWaitingRegisterByConfig(query, query.getTrackQueryMode())
                .stream().filter(e -> StringUtils.isNotBlank(e.getThirdChannelName()))//过滤掉渠道为空的数据
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(records)) {
            return success(Collections.emptyList());
        }

        List<String> cfgIds = records.stream().filter(e -> Objects.equals(Boolean.TRUE, e.getIsPushMobile())).map(LogisticsTrackDTO.UpdateTrackDTO::getThirdRefId).collect(Collectors.toList());
        Map<String ,List<LogisticsThirdChannelRefDetailEntity>> refDetailEntities = new HashMap<>();
        if(CollUtil.isNotEmpty(cfgIds)){
            List<LogisticsThirdChannelRefDetailEntity> list = FeignQuery.create(LogisticsThirdChannelRefDetailEntity.class).in(LogisticsThirdChannelRefDetailEntity::getMainId, cfgIds).list();
            if(CollUtil.isNotEmpty(list)){
                refDetailEntities = list.stream().collect(Collectors.groupingBy(e -> e.getMainId()));
            }
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, String> authMap = logisticsTrackVO.getAuthMap();
        String customer = authMap.get("customer");
        String key = authMap.get("key");

        //查询过滤单号开头配置
        List<DictBasicEntity> dictList =FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType,"trackNoFilterPrefix").list();
        List<String> prefixList = dictList.stream().map(DictBasicEntity::getCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        //设置为暂不查询
        List<String> detailIds = new ArrayList<>();
        List<LogisticsBillDetailDTO.BillDetailErrorDTO> errorList = new ArrayList<>();
        List<LogisticsBillDetailDTO.BillDetailDTO> sucessList = new ArrayList<>();
        List<LogisticsTrackEntity> allTracks = new ArrayList<>();
        for (LogisticsTrackDTO.UpdateTrackDTO record : records) {
            String trackNo = getTrackNo(record);
            if (StrUtil.isBlank(trackNo)) {
                detailIds.add(record.getId());
                continue;
            }
            String companyCode = record.getThirdChannelName();
            if(StrUtil.isBlank(companyCode)){
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

            // 构建查询参数
            if(record.getIsPushMobile()){
                String pushType = record.getPushType();

                List<LogisticsThirdChannelRefDetailEntity> detailEntities = refDetailEntities.get(record.getThirdRefId());
                if(CollUtil.isEmpty(detailEntities)){
                    continue;
                }

                if (LogisticsThirdChannelRefPushTypeEnum.SENDER.getCode().equals(pushType) || LogisticsThirdChannelRefPushTypeEnum.RECEIVER.getCode().equals(pushType)){
                    record.setTelNumber(detailEntities.get(0).getMobile());
                }else if (LogisticsThirdChannelRefPushTypeEnum.SHOP_SENDER.getCode().equals(pushType)){
                    //销售出库单把客户id传递到了物流单店铺id上
                    LogisticsThirdChannelRefDetailEntity logisticsThirdChannelRefDetailEntity = detailEntities.stream().filter(e -> e.getCustomerId().equals(record.getShopId()) || e.getShopId().equals(record.getShopId())).findFirst().orElse(null);
                    if (Objects.nonNull(logisticsThirdChannelRefDetailEntity)){
                        record.setTelNumber(logisticsThirdChannelRefDetailEntity.getMobile());

                    }
                }else if (LogisticsThirdChannelRefPushTypeEnum.PLATFORM_SENDER.getCode().equals(pushType)){
                    LogisticsThirdChannelRefDetailEntity logisticsThirdChannelRefDetailEntity = detailEntities.stream().filter(e -> e.getDictPlatform().equals(record.getSalesPlatform())).findFirst().orElse(null);
                    if (Objects.nonNull(logisticsThirdChannelRefDetailEntity)) {
                        record.setTelNumber(logisticsThirdChannelRefDetailEntity.getMobile());

                    }
                }else if (LogisticsThirdChannelRefPushTypeEnum.ORDER_RECEIVER.getCode().equals(pushType)){

                }else {
                    LogisticsThirdChannelRefDetailEntity logisticsThirdChannelRefDetailEntity = detailEntities.stream().filter(e -> e.getDictPlatform().equals(record.getSalesPlatform())).findFirst().orElse(null);
                    if (Objects.nonNull(logisticsThirdChannelRefDetailEntity)) {
                        record.setTelNumber(logisticsThirdChannelRefDetailEntity.getMobile());

                    }
                }
            }
            //构建查询参数
            Kuaidi100QueryParam param = kuaidi100Service.buildKuaidi100QueryParam(companyCode, trackNo, record.getIsPushMobile(), record.getTelNumber());

            log.error("快递100实时查询请求参数组装：{}", param);
            Kuaidi100QueryResponse response = kuaidi100Service.getTrack(customer, key, param);
            if (response != null && "200".equals(response.getStatus()) && CollUtil.isNotEmpty(response.getData())) {
                for (Kuaidi100QueryResponse.Kuaidi100TrackData data : response.getData()) {
                    LogisticsTrackEntity entity = new LogisticsTrackEntity();
                    entity.setTrackNo(trackNo);
                    LocalDateTime trackTime = LocalDateTime.parse(data.getTime(), dateTimeFormatter);
                    entity.setTrackTime(trackTime);
                    entity.setStatus(kuaidi100Service.convertTrackStatus(response.getStatus()));//转换类型
                    entity.setContent(data.getContext());
                    entity.setTransportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode());
                    allTracks.add(entity);
                }
                logisticsOperateService.pullOperateLog(null,
                        null, BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.KUAIDI100.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsTrackVO), JSONUtil.toJsonStr(response));
            }else {
                LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
                logisticsTrackEntity.setTrackNo(trackNo);
                logisticsTrackEntity.setStatus(LogisticTrackStatusEnum.NOT_FIND.getCode());
                logisticsTrackEntity.setContent("【"+response.getReturnCode() + "】:【" + response.getMessage()+"】");
                logisticsTrackEntity.setTrackTime(LocalDateTime.now());
                logisticsTrackEntity.setTransportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode());
                allTracks.add(logisticsTrackEntity);

                logisticsOperateService.pullOperateLog(null,
                        null, BusinessTypeEnum.GET_TRACK.getCode(), LogisticsPlatformEnum.KUAIDI100.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsTrackVO), JSONUtil.toJsonStr(response));
            }
        }
        return success(allTracks);
    }


    private String getTrackNo(LogisticsTrackDTO.UpdateTrackDTO record) {
        return TrackQueryTypeEnum.TRACK_NO.getCode().equals(record.getTrackQueryType()) && StrUtil.isNotBlank(record.getTrackNo())
                ? record.getTrackNo() : record.getTransportNo();
    }

    /**
     * 注册物流单号（伪注册实现）
     * 由于快递100无需在官方平台执行“注册”动作，此处直接返回所有单号注册成功。
     * 目的是为了让单据在ERP内部的 register_status 状态从 0（待注册）变为 1（已注册）。
     *
     * @param registerTrackVO 注册参数，包含待处理的物流单号
     * @return 注册结果列表
     * @author jack
     * @date 2026-04-02
     */
    @Override
    public ApiResult<List<RegisterResponseVO>> registerLogisticsNumber(RegisterTrackVO registerTrackVO) {
        log.info("快递100执行标准化伪注册流程，处理单数：{}", registerTrackVO.getLogisticsRegisterVOS().size());
        
        // 伪注册核心：直接将所有传入单号标记为注册成功
        List<RegisterResponseVO> responseList = registerTrackVO.getLogisticsRegisterVOS().stream()
                .map(vo -> RegisterResponseVO.builder()
                        .trackNo(vo.getTrackNo())
                        .trackStatus(true) // 标识注册成功
                        .build())
                .collect(Collectors.toList());
        
        return success(responseList);
    }

    /**
     * 获取指定物流平台的授权配置
     *
     * @param platform 平台代码
     * @return 包含授权参数的Map列表
     * @author jack
     * @date 2026-04-02
     */
    @Override
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.KUAIDI100_AUTHORIZE;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (cfgAppClient == null) {
            log.warn("未查询到快递100的授权配置（AppClientEnum.KUAIDI100_AUTHORIZE）");
            return Collections.emptyList();
        }
        
        Map<String, String> map = new HashMap<>();
        map.put("id", cfgAppClient.getId());
        map.put("logisticsPlatform", getPlatForm().getCode());
        map.put("customer", cfgAppClient.getClientId()); // Kuaidi100 对应客户标识
        map.put("key", cfgAppClient.getClientSecret());   // Kuaidi100 对应接口密钥
        
        return Collections.singletonList(map);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.KUAIDI100;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }
}
