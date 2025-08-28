package com.erp.server.wms.schedule;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryDetailService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import com.sdk.oms.temu.service.TemuClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 三方仓发货单重试任务
 */
@Component
@Slf4j
public class SoOutStockPriceJob {

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Resource
    private TemuClient temuClient;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

    @Resource
    private ThirdWarehouseDeliveryDetailService thirdWarehouseDeliveryDetailService;

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
            //查询出库单明细单价为0，没有销售订单明细id的数据
            List<String> ids = soOutstockEntityList.stream().map(v->v.getId()).collect(Collectors.toList());;
            List<SoOutstockDetailEntity> soOutDetailList = soOutstockDetailService.lambdaQuery().in(SoOutstockDetailEntity::getMainId,ids)
                    .eq(SoOutstockDetailEntity::getPrice, BigDecimal.ZERO)
                    .eq(SoOutstockDetailEntity::getSoDetailId,"")
                    .list();
            if(CollectionUtils.isEmpty(soOutDetailList)){
                XxlJobHelper.log("没有找到当天的出库单明细单价为0，没有销售订单明细id的数据，日期={}", startDate);
                continue;
            }
            XxlJobHelper.log("找到当天的出库单明细单价为0，没有销售订单明细id的数据，数量={}，日期={}", soOutDetailList.size(), startDate);
            List<String> detailSourceIds = soOutDetailList.stream().map(v->v.getSourceDetailId()).collect(Collectors.toList());
            Map<String,List<SoOutstockDetailEntity>> map = soOutDetailList.stream().collect(Collectors.groupingBy(SoOutstockDetailEntity::getMainId));

            List<ThirdWarehouseDeliveryDetailEntity> thirdWarehouseDeliveryDetailEntityList = thirdWarehouseDeliveryDetailService.listByIds(detailSourceIds);

            List<SoOutstockDetailEntity> updateList = new ArrayList<>();
            List<ThirdWarehouseDeliveryDetailEntity> updateThirdWarehouseDetailList = new ArrayList<>();
            map.forEach((mainId,detailList)->{
                SoOutstockEntity soOutstock = soOutstockEntityList.stream().filter(v->v.getId().equals(mainId)).findFirst().orElse(null);
                if(soOutstock == null){
                    return;
                }
                soOutstockDetailService.handleB2cDetailData(detailList,soOutstock);
                List<SoOutstockDetailEntity> needUpdateList = detailList.stream().filter(v->StringUtils.isNotBlank(v.getSoDetailId())).collect(Collectors.toList());
                updateList.addAll(needUpdateList);
                for (SoOutstockDetailEntity detailEntity : needUpdateList) {
                    ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity = thirdWarehouseDeliveryDetailEntityList.stream()
                            .filter(v -> v.getId().equals(detailEntity.getSourceDetailId()))
                            .findFirst()
                            .orElse(null);
                    if (thirdWarehouseDeliveryDetailEntity != null) {
                        thirdWarehouseDeliveryDetailEntity.setSoDetailId(detailEntity.getSoDetailId());
                        updateThirdWarehouseDetailList.add(thirdWarehouseDeliveryDetailEntity);
                    }
                }
            });
            if(CollectionUtils.isNotEmpty(updateList)){
                XxlJobHelper.log("开始更新出库单明细数据，数量={}", updateList.size());
                soOutstockDetailService.updateBatchById(updateList);
            }
            if(CollectionUtils.isNotEmpty(updateThirdWarehouseDetailList)){
                XxlJobHelper.log("开始更新三方仓发货单明细数据，数量={}", updateThirdWarehouseDetailList.size());
                thirdWarehouseDeliveryDetailService.updateBatchById(updateThirdWarehouseDetailList);
            }
        }

    }
}
