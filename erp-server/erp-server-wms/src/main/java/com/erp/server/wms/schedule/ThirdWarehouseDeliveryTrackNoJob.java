package com.erp.server.wms.schedule;

import com.common.business.enums.SourceTypeEnum;
import com.erp.server.wms.mapper.ThirdWarehouseDeliveryMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;

/**
 * 三方仓发货单跟踪号历史数据修复任务
 */
@Component
@Slf4j
public class ThirdWarehouseDeliveryTrackNoJob {

    @Resource
    private ThirdWarehouseDeliveryMapper thirdWarehouseDeliveryMapper;

    @XxlJob("ThirdWarehouseDeliveryTrackNoJob")
    public void updateThirdWarehouseDeliveryTrackNo() {
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", jobParam);
        if (StringUtils.isBlank(jobParam)) {
            XxlJobHelper.log("任务参数为空，无法执行三方仓发货单跟踪号修复任务");
            return;
        }
        String[] split = jobParam.split(",");
        if (split.length < 2) {
            XxlJobHelper.log("任务参数格式错误，示例：2025-01-01,2025-01-31");
            return;
        }

        LocalDate startDate = LocalDate.parse(split[0].trim());
        LocalDate endDate = LocalDate.parse(split[1].trim());
        if (startDate.isAfter(endDate)) {
            XxlJobHelper.log("任务参数日期范围错误，startDate={}, endDate={}", startDate, endDate);
            return;
        }

        while (!startDate.isAfter(endDate)) {
            LocalDate nextDate = startDate.plusDays(1);
            int updateCount = thirdWarehouseDeliveryMapper.updateTrackNoByCreateTime(
                    startDate.atStartOfDay(),
                    nextDate.atStartOfDay(),
                    SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode());
            XxlJobHelper.log("三方仓发货单跟踪号修复完成，日期={}，更新数量={}", startDate, updateCount);
            startDate = nextDate;
        }
    }
}
