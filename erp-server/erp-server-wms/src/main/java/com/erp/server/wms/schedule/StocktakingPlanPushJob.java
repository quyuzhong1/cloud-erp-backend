package com.erp.server.wms.schedule;

import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.erp.server.wms.mapper.StocktakingPlanMapper;
import com.erp.server.wms.service.StocktakingPlanService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2026/2/3 9:28
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
@Slf4j
public class StocktakingPlanPushJob {

    @Resource
    private StocktakingPlanService stocktakingPlanService;

    @Resource
    private StocktakingPlanMapper stocktakingPlanMapper;

    /**
     * 定时下推盘点任务
     */
    @XxlJob(value = "stocktakingPlanPushJob")
    public void stocktakingPlanPushJob(){
        XxlJobHelper.log("stocktakingPlanPushJob start : {}", LocalDateTime.now());
        BaseIdsDTO.IdsDTO idsDTO = new BaseIdsDTO.IdsDTO();
        List<StocktakingPlanDTO.AllowPushDTO> allowPushDTOList = stocktakingPlanMapper.allowPushStocktakingPlan();
        List<String> idList = allowPushDTOList.stream().map(item -> item.getId()).collect(Collectors.toList());
        if (!idList.isEmpty()) {
            //过滤不能下推的盘点任务
            Map<String, String> resultMap = stocktakingPlanService.filterStocktakingPlan(idList);
            resultMap.forEach((key, value) ->
                    XxlJobHelper.log("stocktakingPlanPushJob filter id: {}, filter reason: {}", key, value)
            );
            if (!idList.isEmpty()) {
                idsDTO.setIds(idList);
                XxlJobHelper.log("stocktakingPlanPushJob push idList : {}", idList.toString());
                stocktakingPlanService.pushStockingTaskByJob(idsDTO);
            }
        }
        XxlJobHelper.log("stocktakingPlanPushJob end : {}", LocalDateTime.now());
    }
}