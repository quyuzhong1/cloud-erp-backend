package com.erp.server.wms.schedule;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 销售出库单组合品价格重算
 */
@Component
@Slf4j
public class SoOutStockBomPriceJob {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @XxlJob("SoOutStockBomPriceJob")
    public void SoOutStockBomPriceJob() {
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
            //查询当天的自发货出库单
            List<String> sourceTypeList = Arrays.asList("soB2cDelivery","thirdWarehouseCreateOutboundBill");
            List<SoOutstockEntity> soOutstockEntityList = soOutstockService.lambdaQuery().in(SoOutstockEntity::getSourceType, sourceTypeList)
                    .between(SoOutstockEntity::getCreateTime, startDate.atStartOfDay(), startDate.plusDays(1).atStartOfDay())
                    .list();
            if (CollectionUtils.isEmpty(soOutstockEntityList)) {
                XxlJobHelper.log("没有找到当天的出库单，日期={}", startDate);
                continue;
            }
            //查询销售订单
            List<String> soIds = soOutstockEntityList.stream().map(SoOutstockEntity::getSoId).collect(Collectors.toList());
            //2000个一批查询
            List<SoB2cDetailEntity> soB2cDetailEntityList = new ArrayList<>();
            List<List<String>> partitions = org.apache.commons.collections4.ListUtils.partition(soIds, 2000);
            for (List<String> part : partitions) {
                List<SoB2cDetailEntity> tempList = soB2cFeign.listDetailByMainIds(part);
                if (CollectionUtils.isNotEmpty(tempList)) {
                    soB2cDetailEntityList.addAll(tempList);
                }
            }
            if(CollectionUtils.isEmpty(soB2cDetailEntityList)){
                XxlJobHelper.log("没有找到当天的销售订单明细数据，日期={}", startDate);
                continue;
            }
            //查询出库单明细单价为0，没有销售订单明细id的数据
            List<String> ids = soOutstockEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
            List<SoOutstockDetailEntity> soOutstockDetailEntityList = new ArrayList<>();
            List<List<String>> partitionIds = org.apache.commons.collections4.ListUtils.partition(ids, 2000);
            for (List<String> part : partitionIds) {
                List<SoOutstockDetailEntity> soOutDetailList = soOutstockDetailService.lambdaQuery().in(SoOutstockDetailEntity::getMainId,part).list();
                if (CollectionUtils.isNotEmpty(soOutDetailList)) {
                    soOutstockDetailEntityList.addAll(soOutDetailList);
                }
            }
            XxlJobHelper.log("待处理出库单，数量={}，日期={}", soOutstockEntityList.size(), startDate);
            Map<String,List<SoOutstockDetailEntity>> map = soOutstockDetailEntityList.stream().collect(Collectors.groupingBy(SoOutstockDetailEntity::getMainId));
            List<SoOutstockDetailEntity> updateList = new ArrayList<>();
            map.forEach((mainId,detailList)->{
                SoOutstockEntity soOutstock = soOutstockEntityList.stream().filter(v->v.getId().equals(mainId)).findFirst().orElse(null);
                if(soOutstock == null){
                    return;
                }
                List<SoB2cDetailEntity> soB2cDetails = soB2cDetailEntityList.stream().filter(v->v.getMainId().equals(soOutstock.getSoId())).collect(Collectors.toList());
                Set<String> soB2cSku = soB2cDetails.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toSet());
                Set<String> soOutSku = detailList.stream().map(SoOutstockDetailEntity::getSkuId).collect(Collectors.toSet());
                if(soB2cSku.equals(soOutSku)){
                    return;
                }
                soOutstockDetailService.handleB2cDetailData(detailList,soOutstock);
                List<SoOutstockDetailEntity> needUpdateList = detailList.stream().filter(v->StringUtils.isNotBlank(v.getSoDetailId())).collect(Collectors.toList());
                updateList.addAll(needUpdateList);
            });
            if(CollectionUtils.isNotEmpty(updateList)){
                XxlJobHelper.log("开始更新出库单明细数据，数量={}", updateList.size());
                soOutstockDetailService.updateBatchById(updateList);
            }else{
                XxlJobHelper.log("没有需要更新的出库单明细数据");
            }
        }
    }
}
