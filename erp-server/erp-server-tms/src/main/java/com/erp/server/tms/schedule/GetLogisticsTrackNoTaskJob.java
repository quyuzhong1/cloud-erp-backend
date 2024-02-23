package com.erp.server.tms.schedule;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.plm.entity.ProjectTaskSysEntity;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsAuthService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
        List<LogisticsChannelDTO.LogisticsPlatformDTO> platformList=logisticsChannelService.listChannelPlatform(channelIdList);
        //物流权限
        List<LogisticsSupplierDTO.AuthChannelViewDTO> logisticsAuthList = logisticsAuthService.listAuthChannelView(channelIdList);
        //平台分组
        Map<String,List<LogisticsChannelDTO.LogisticsPlatformDTO>> platformMap= platformList.stream().
                collect(Collectors.groupingBy(LogisticsChannelDTO.LogisticsPlatformDTO::getLogisticsPlatform));



        for (Map.Entry<String, List<LogisticsChannelDTO.LogisticsPlatformDTO>> entry : platformMap.entrySet()) {
            String logisticsPlatform = entry.getKey();
            List<LogisticsChannelDTO.LogisticsPlatformDTO> platformLogisticsList = entry.getValue();
            //这个平台对应的渠道id
            List<String> platformChannelIdList=platformLogisticsList.stream().map(LogisticsChannelDTO.LogisticsPlatformDTO::getChannelId).
                    distinct().collect(Collectors.toList());
            //需要查找的集合
            List<SoB2cLogisticsDTO.TrackNoDTO>  queryBaseList=list.stream().filter(item->platformChannelIdList.contains(item.getLogisticsChannelId())).collect(Collectors.toList());
            LogisticsService logisticsService=logisticsRegistry.getHandler(logisticsPlatform);
            //真正查询跟踪号的
            List<LogisticsQueryBaseVO> logisticsQuery = listQuery(queryBaseList);
            ApiResult<List<LogisticsOrderResponseVO>> orderResponse = logisticsService.queryOrderList(logisticsQuery);
            if(orderResponse.isSuccess()){
                List<LogisticsOrderResponseVO> resultList = orderResponse.getData();
            }
        }

    }


    /**
     * 获取查询条件
     * @param list
     * @return
     */
    private List<LogisticsQueryBaseVO> listQuery(List<SoB2cLogisticsDTO.TrackNoDTO> list) {

        return null;
    }
}
