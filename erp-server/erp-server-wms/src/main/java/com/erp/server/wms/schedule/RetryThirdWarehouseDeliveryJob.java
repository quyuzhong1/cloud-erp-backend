package com.erp.server.wms.schedule;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 三方仓发货单重试任务
 */
@Component
@Slf4j
public class RetryThirdWarehouseDeliveryJob {

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

    @XxlJob("RetryThirdWarehouseDeliveryJob")
    public void RetryThirdWarehouseDeliveryJob() {
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", jobParam);
        if(StringUtils.isBlank(jobParam)){
            XxlJobHelper.log("任务参数为空，无法执行重试任务");
            return;
        }
        String[] split = jobParam.split(",");
        if(split.length < 2){
            XxlJobHelper.log("任务参数格式错误，无法执行重试任务");
            return;
        }
        LocalDate startDate = LocalDate.parse(split[0]);
        LocalDate endDate = LocalDate.parse(split[1]);
        //分割成按天执行
        while (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
            XxlJobHelper.log("开始执行重试任务，日期={}", startDate);
            startDate = startDate.plusDays(1);
            //查询当天的平台仓出库单
            List<SoOutstockEntity> soOutstockEntityList = soOutstockService.lambdaQuery().eq(SoOutstockEntity::getSourceType, SourceTypeEnum.PLATFORM_SO_OUT_STOCK.getCode())
                    .between(SoOutstockEntity::getCreateTime, startDate.atStartOfDay(), startDate.plusDays(1).atStartOfDay())
                    .list();
            if (CollectionUtils.isEmpty(soOutstockEntityList)) {
                XxlJobHelper.log("没有找到当天的出库单，日期={}", startDate);
                continue;
            }
            List<String> sourceIds = soOutstockEntityList.stream()
                    .map(SoOutstockEntity::getSourceId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            List<ThirdWarehouseDeliveryEntity> thirdWarehouseEntities = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(sourceIds)){
                //查询已生成的三方仓发货单
                thirdWarehouseEntities = thirdWarehouseDeliveryService.listByIds(sourceIds);
            }
            List<String> existSourceIds = thirdWarehouseEntities.stream()
                    .map(ThirdWarehouseDeliveryEntity::getId)
                    .collect(Collectors.toList());
            soOutstockEntityList = soOutstockEntityList.stream()
                    .filter(soOutstockEntity -> !existSourceIds.contains(soOutstockEntity.getSourceId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(soOutstockEntityList)) {
                XxlJobHelper.log("没有找到当天的出库单，日期={}，且没有未生成的三方仓发货单", startDate);
                continue;
            }
            thirdWarehouseDeliveryService.batchGeneratePlatformDelivery(soOutstockEntityList);

        }

    }
}
