package com.erp.server.tms.schedule;

import cn.hutool.core.util.IdUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsAuthService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsService;
import com.google.common.collect.Lists;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname GetLogisticsTrackNoTaskJOB
 * @Description TODO
 * @Date 2024-01-04 15:45
 * @Created by yl
 */
@Slf4j
@Component
public class GetLogisticsTrackNoTaskJob {


    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private LogisticsRegistry logisticsRegistry;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private LogisticsAuthService logisticsAuthService;
    @Resource
    private MQProducerService mqProducerService;

    @XxlJob("getLogisticsTrackNo")
    public void getLogisticsTrackNo() {
        //获取到了为空的跟踪单号
        List<SoB2cLogisticsDTO.TrackNoDTO> list = soB2cFeign.listTrackNoEmptyList();
        List<String> channelIdList = list.stream().map(SoB2cLogisticsDTO.TrackNoDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());
        List<LogisticsChannelDTO.LogisticsPlatformDTO> platformList = logisticsChannelService.listChannelPlatform(channelIdList);
        //平台分组
        Map<String, List<LogisticsChannelDTO.LogisticsPlatformDTO>> platformMap = platformList.stream().
                collect(Collectors.groupingBy(LogisticsChannelDTO.LogisticsPlatformDTO::getLogisticsPlatform));
        String aliExpress = PlatformDictEnum.ALI_EXPRESS.getCode();

        for (Map.Entry<String, List<LogisticsChannelDTO.LogisticsPlatformDTO>> entry : platformMap.entrySet()) {
            //平台
            String logisticsPlatform = entry.getKey();

            Boolean isAliExpress = aliExpress.equals(logisticsPlatform);

            List<LogisticsChannelDTO.LogisticsPlatformDTO> platformLogisticsList = entry.getValue();
            //这个平台对应的渠道id
            List<String> platformChannelIdList = platformLogisticsList.stream().map(LogisticsChannelDTO.LogisticsPlatformDTO::getChannelId).
                    distinct().collect(Collectors.toList());
            //查询的需要转换的
            List<SoB2cLogisticsDTO.TrackNoDTO> finalQueryList = list.stream().filter(l -> platformChannelIdList.contains(l.getLogisticsChannelId()))
                    .collect(Collectors.toList());
            LogisticsService logisticsService = logisticsRegistry.getHandler(logisticsPlatform);

            //真正查询跟踪号的
            List<LogisticsQueryBaseVO> logisticsQueryList = listQuery(isAliExpress, logisticsPlatform, finalQueryList, platformLogisticsList);
            List<List<LogisticsQueryBaseVO>> logisticsPartitionList = Lists.partition(logisticsQueryList, 20);
            for (List<LogisticsQueryBaseVO> logisticsQuery : logisticsPartitionList) {
                try {
                    //查询跟踪号
                    ApiResult<List<LogisticsOrderResponseVO>> orderResponse = logisticsService.queryOrderList(logisticsQuery);
                    List<LogisticsOrderResponseVO> resultList = orderResponse.getData();
                    if (CollectionUtils.isEmpty(resultList)){
                        continue;
                    }
                    List<LogisticsBillDTO.TrackDTO> updateList = new ArrayList<>(resultList.size());
                    for (LogisticsOrderResponseVO item : resultList) {
                        SoB2cLogisticsDTO.TrackNoDTO trackNoDTO = finalQueryList.stream().filter(f -> f.getTransportNo().equals(item.getTransportNo())).findFirst().orElse(null);

                        String b2cLogisticsId = Objects.nonNull(trackNoDTO) ? trackNoDTO.getId() : "";
//                                finalQueryList.stream().filter(f -> f.getTransportNo().equals(item.getTransportNo())).
//                                map(SoB2cLogisticsDTO.TrackNoDTO::getId).findFirst().orElse("");
                        if (StringUtils.isNotBlank(b2cLogisticsId) && StringUtils.isNotBlank(item.getTrackNo())){
                            LogisticsBillDTO.TrackDTO dto = LogisticsBillDTO.TrackDTO.builder()
                                    .transportNo(item.getTransportNo())
                                    .trackNo(item.getTrackNo())
                                    .id(b2cLogisticsId)
                                    .build();
                            updateList.add(dto);
                            //下单成功发送异步请求保存面单
                            if (Objects.nonNull(isAliExpress) && isAliExpress && Objects.nonNull(trackNoDTO)){
                                LogisticsBillDTO.PrintLogisticsWaybillDTO waybillDTO = new LogisticsBillDTO.PrintLogisticsWaybillDTO();
                                waybillDTO.setChannelId(trackNoDTO.getLogisticsChannelId());
                                waybillDTO.setB2cSoId(trackNoDTO.getSoB2cId());
                                waybillDTO.setDeliveryNo(trackNoDTO.getSoCode());
                                waybillDTO.setShopId(trackNoDTO.getShopId());
                                waybillDTO.setTransportNo(trackNoDTO.getTransportNo());
                                mqProducerService.asyncClassMsg(RocketMqTopic.ASYNC_GET_PLATFORM_LABEL_TOPIC, RocketMqTagEnum.ASYNC_GET_PLATFORM_LABEL_TAG.getName(), waybillDTO, IdUtil.simpleUUID());
                            }
                        }

                    }
                    if (CollectionUtils.isNotEmpty(updateList)) {
                        soB2cFeign.updateTrackNoByTransportNo(updateList);
                    }
                } catch (Exception e) {
                    log.error("查询物流跟踪号异常>>>>{}", e);
                }
            }
        }
    }


    /**
     * 获取查询条件
     *
     * @param list
     * @return
     */
    private List<LogisticsQueryBaseVO> listQuery(Boolean isAliExpress, String logisticsPlatform, List<SoB2cLogisticsDTO.TrackNoDTO> list, List<LogisticsChannelDTO.LogisticsPlatformDTO> platformLogisticsList) {
        List<LogisticsQueryBaseVO> queryBaseList = new ArrayList<>(list.size());
        for (SoB2cLogisticsDTO.TrackNoDTO item : list) {
            String authId = platformLogisticsList.stream().filter(p -> p.getChannelId().
                    equals(item.getLogisticsChannelId())).findFirst().map(LogisticsChannelDTO.LogisticsPlatformDTO::getAuthId).orElse("");
            if (StringUtils.isNotBlank(authId)) {
                LogisticsQueryBaseVO queryBase = new LogisticsQueryBaseVO();
                queryBase.setOrderId(item.getSoB2cId());
                queryBase.setTransportNo(item.getTransportNo());
                String deliveryNo = item.getSoCode();
                if (isAliExpress) {
                    deliveryNo = item.getPlatformCode();
                }
                if (isAliExpress && StringUtils.isBlank(deliveryNo)) {
                    continue;
                }
                item.setDeliveryNo(deliveryNo);
                queryBase.setDeliveryNo(deliveryNo);
                queryBase.setPlatformCode(item.getPlatformCode());
                Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(authId,item.getShopId(), logisticsPlatform);
                authMap.put("token", item.getShopToken());
                queryBase.setAuthMap(authMap);
                queryBaseList.add(queryBase);
            }
        }
        return queryBaseList;
    }
}
