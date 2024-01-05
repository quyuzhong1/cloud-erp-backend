package com.erp.server.tms.schedule;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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

   // @XxlJob("getLogisticsTrackNo")
    public void getLogisticsTrackNo() {
        //获取到了为空的跟踪单号
        List<SoB2cLogisticsDTO.TrackNoDTO> list = soB2cFeign.listTrackNoEmptyList();
        List<LogisticsQueryBaseVO> logisticsQuery=listQuery(list);
        LogisticsService logisticsService=logisticsRegistry.getHandler("");
        ApiResult<List<LogisticsOrderResponseVO>> orderResponse = logisticsService.queryOrderList(logisticsQuery);
        if(orderResponse.isSuccess()){
            List<LogisticsOrderResponseVO> resultList=orderResponse.getData();
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
