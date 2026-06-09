package com.erp.server.dmp.inout.handler.input.task.init.api.kuaidi100;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.TrackQueryTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.model.tms.entity.LogisticsThirdChannelRefDetailEntity;
import com.erp.model.tms.enums.LogisticsThirdChannelRefPushTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.erp.server.dmp.service.ForeignService;
import com.sdk.tms.kuaidi100.model.request.Kuaidi100QueryParam;
import com.sdk.tms.kuaidi100.model.response.Kuaidi100QueryResponse;
import com.sdk.tms.kuaidi100.service.Kuaidi100Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能描述：快递100海运物流轨迹拉取初始化处理器
 *
 * @author jack
 * @date 2026-04-02
 */
@Service
@Slf4j
@Scope("prototype")
public class Kuaidi100OceanLogisticsApiInitHandler implements DmpInputApiInitHandler {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final String PAGE_SIZE_PARAM = "pageSize";

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private Kuaidi100Service kuaidi100Service;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private LogisticsBillFeign logisticsBillFeign;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        // 1. 获取授权配置
        CfgAppClientEntity cfgAppClient = getCfgAppClient();
        if (Objects.isNull(cfgAppClient)) {
            log.warn("未找到快递100授权配置信息");
            return Collections.emptyList();
        }

        // 2. 构建查询条件
        int pageSize = getPageSizeValue(dmpInputApiInitRequest);
        // 轨迹时间范围（最近3个月）
        LocalDateTime trackStartTime = LocalDateTime.now().minusMonths(3);

        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(PlatformDictEnum.KUAIDI100.getCode())
                .size(pageSize)
                .current(1)
                .registerStatus(1) // 已注册
                .trackEnable(true)
                .trackTime(trackStartTime)
                .transportType(LogisticsTransportTypeEnum.OCEAN.getCode())
                .build();

        // 3. 执行单号查询（原始结果，未做渠道过滤）
        List<LogisticsTrackDTO.UpdateTrackDTO> rawList = logisticsBillFeign.listRegisterByConfig(query, query.getTrackQueryMode());
        if (CollUtil.isEmpty(rawList)) {
            return Collections.emptyList();
        }

        // 推进游标：拉取瞬间即把本批全部单据 update_time 刷为 now，使其在 ORDER BY update_time 队列中轮到队尾，
        // 避免查无渠道/查询失败的单据卡在队首导致重复拉取与积压。复用 Track123 的 topic/tag 与消费者，仅用于推进游标，与轨迹数据回写无关。
        List<String> rawIds = rawList.stream().map(LogisticsTrackDTO.UpdateTrackDTO::getId).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(rawIds)) {
            mqProducerService.asyncClassMsg(RocketMqTopic.TMS_123_LOGISTICS_TRACK, RocketMqTagEnum.ASYNC_GET_TRACK123_LOGISTICS_TRACK.getName(), rawIds, "getTrack");
        }

        List<LogisticsTrackDTO.UpdateTrackDTO> records = rawList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getThirdChannelName()))//过滤掉渠道为空的数据
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(records)) {
            return Collections.emptyList();
        }

        List<String> cfgIds = records.stream().filter(e -> Objects.equals(Boolean.TRUE, e.getIsPushMobile())).map(LogisticsTrackDTO.UpdateTrackDTO::getThirdRefId).collect(Collectors.toList());
        Map<String ,List<LogisticsThirdChannelRefDetailEntity>> refDetailEntities = new HashMap<>();
        if(CollUtil.isNotEmpty(cfgIds)){
            List<LogisticsThirdChannelRefDetailEntity> list = FeignQuery.create(LogisticsThirdChannelRefDetailEntity.class).in(LogisticsThirdChannelRefDetailEntity::getMainId, cfgIds).list();
            if(CollUtil.isNotEmpty(list)){
                refDetailEntities = list.stream().collect(Collectors.groupingBy(e -> e.getMainId()));
            }
        }

        String customer = cfgAppClient.getClientId();
        String key = cfgAppClient.getClientSecret();

        //查询过滤单号开头配置
        List<DictBasicEntity> dictList =FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType,"trackNoFilterPrefix").list();
        List<String> prefixList = dictList.stream().map(DictBasicEntity::getCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());

        for (LogisticsTrackDTO.UpdateTrackDTO record : records) {
            String trackNo = getTrackNo(record);
            if (StrUtil.isBlank(trackNo)) {
                continue;
            }
            String companyCode = record.getThirdChannelName();
            if(StrUtil.isBlank(companyCode)){
                continue;
            }
            //根据配置过滤是否符合配置
            if (CollUtil.isNotEmpty(prefixList)){
                //判断是否符合配置
                if (prefixList.stream().anyMatch(trackNo::startsWith)){
                    continue;
                }
            }

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
            if (Objects.nonNull(response)) {
                // 4. 封装结果返回给 DMP 流程
                DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
                dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(response));
                dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
            }
        }

        // 游标推进已在查询后通过 MQ 完成（刷 update_time）；轨迹数据回写由 DMP 输出链路异步处理，此处无需再回写。
        return dmpInputTaskInitDTOList;
    }

    private CfgAppClientEntity getCfgAppClient() {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.KUAIDI100_AUTHORIZE;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        try {
            return dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("获取快递100配置信息异常：{}", e.getMessage());
            return null;
        }
    }

    private String getTrackNo(LogisticsTrackDTO.UpdateTrackDTO record) {
        return TrackQueryTypeEnum.TRACK_NO.getCode().equals(record.getTrackQueryType()) && StrUtil.isNotBlank(record.getTrackNo())
                ? record.getTrackNo() : record.getTransportNo();
    }

    private int getPageSizeValue(DmpInputApiInitRequest dmpInputApiInitRequest) {
//        String requestParam = dmpInputApiInitRequest.getRequestParam();
//        if (StringUtils.isNotBlank(requestParam)) {
//            JSONObject jsonObject = JSON.parseObject(requestParam);
//            Integer intValue = jsonObject.getInteger(PAGE_SIZE_PARAM);
//            if (null != intValue) {
//                return intValue;
//            }
//        }
        return DEFAULT_PAGE_SIZE;
    }
}
