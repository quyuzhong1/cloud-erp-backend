package com.erp.server.tms.schedule;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsAuthService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    // @XxlJob("getLogisticsTrackNo")
    public void getLogisticsTrackNo() {
        //获取到了为空的跟踪单号
        List<SoB2cLogisticsDTO.TrackNoDTO> list = soB2cFeign.listTrackNoEmptyList();
        List<String> channelIdList = list.stream().map(SoB2cLogisticsDTO.TrackNoDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());
        List<LogisticsChannelDTO.LogisticsPlatformDTO> platformList = logisticsChannelService.listChannelPlatform(channelIdList);
        //平台分组
        Map<String, List<LogisticsChannelDTO.LogisticsPlatformDTO>> platformMap = platformList.stream().
                collect(Collectors.groupingBy(LogisticsChannelDTO.LogisticsPlatformDTO::getLogisticsPlatform));


        for (Map.Entry<String, List<LogisticsChannelDTO.LogisticsPlatformDTO>> entry : platformMap.entrySet()) {
            //平台
            String logisticsPlatform = entry.getKey();
            List<LogisticsChannelDTO.LogisticsPlatformDTO> platformLogisticsList = entry.getValue();
            //这个平台对应的渠道id
            List<String> platformChannelIdList = platformLogisticsList.stream().map(LogisticsChannelDTO.LogisticsPlatformDTO::getChannelId).
                    distinct().collect(Collectors.toList());
            //查询的需要转换的
            List<SoB2cLogisticsDTO.TrackNoDTO> finalQueryList = list.stream().filter(l -> platformChannelIdList.contains(l.getLogisticsChannelId()))
                    .collect(Collectors.toList());
            LogisticsService logisticsService = logisticsRegistry.getHandler(logisticsPlatform);

            try {
                //真正查询跟踪号的
                List<LogisticsQueryBaseVO> logisticsQuery = listQuery(logisticsPlatform, finalQueryList, platformLogisticsList);
                ApiResult<List<LogisticsOrderResponseVO>> orderResponse = logisticsService.queryOrderList(logisticsQuery);
                if (orderResponse.isSuccess()) {
                    List<LogisticsOrderResponseVO> resultList = orderResponse.getData();
                    List<SoB2cLogisticsEntity> updateList = new ArrayList<>(resultList.size());
                    for (LogisticsOrderResponseVO item : resultList) {
                        List<String> trackNoList = new ArrayList<>(2);
                        String transportNo = item.getTransportNo();
                        String b2cLogisticsId = finalQueryList.stream().filter(f -> f.getTransportNo().equals(transportNo)).
                                map(SoB2cLogisticsDTO.TrackNoDTO::getId).findFirst().orElse("");
                    }


                }
            } catch (Exception e) {
                log.error("查询物流跟踪号异常>>>>{}", e);
            }


        }

    }


    /**
     * 获取查询条件
     *
     * @param list
     * @return
     */
    private List<LogisticsQueryBaseVO> listQuery(String logisticsPlatform, List<SoB2cLogisticsDTO.TrackNoDTO> list, List<LogisticsChannelDTO.LogisticsPlatformDTO> platformLogisticsList) {
        List<LogisticsQueryBaseVO> queryBaseList = new ArrayList<>(list.size());
        for (SoB2cLogisticsDTO.TrackNoDTO item : list) {
            String authId = platformLogisticsList.stream().filter(p -> p.getChannelId().
                    equals(item.getLogisticsChannelId())).findFirst().map(LogisticsChannelDTO.LogisticsPlatformDTO::getAuthId).orElse("");
            if (StringUtils.isNotBlank(authId)) {
                LogisticsQueryBaseVO queryBase = new LogisticsQueryBaseVO();
                queryBase.setTransportNo(item.getTransportNo());
                Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(authId, logisticsPlatform);
                authMap.put("token", item.getShopToken());
                queryBase.setAuthMap(authMap);
                queryBaseList.add(queryBase);
            }
        }
        return queryBaseList;
    }
}
