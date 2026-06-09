package com.erp.server.wms.schedule;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.enums.PackageForecastCollectModeEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.erp.server.wms.service.PackageForecastService;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyLogisticResp;
import com.sdk.oms.tiktok.service.TikTokFullService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName PackageForecastJob
 * @description: 组包详情状态更新
 * @date 2024年02月20日
 * @version: 1.0
 */
@Component
@Slf4j
public class PackageForecastJob {
    @Resource
    private PackageForecastService packageForecastService;
    @Resource
    private PackageForecastDetailService packageForecastDetailService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private TikTokFullService tikTokFullService;
    /**
     * 同步组包订单详情
     */
    @XxlJob(value = "syncPackageForecastInfo")
    public void SyncPackageForecastInfo() throws Exception {
        XxlJobHelper.log("syncPackageForecastInfo start : {}", LocalDateTime.now());
        DateTime dateTime = DateUtil.offsetMonth(DateUtil.date(), -3);
        //根据订单查询组包明细  默认查询 3月内的组包数据
        List<String> handoverStatusList = new ArrayList<>();
        String jobParam = XxlJobHelper.getJobParam();
        String handoverStatusStr = StrUtils.null2EmptyWithTrim(jobParam);
        if (CharSequenceUtil.isNotBlank(handoverStatusStr)){
            handoverStatusList = Arrays.stream(handoverStatusStr.split(",")).distinct().collect(Collectors.toList());
        }
        handoverStatusList.add(CharSequenceUtil.EMPTY);
        List<PackageForecastEntity> orders = packageForecastService.lambdaQuery()
                .in(PackageForecastEntity::getHandoverStatus,handoverStatusList)
                .gt(PackageForecastEntity::getBillDate, dateTime)
                .list();
        if (CollectionUtils.isEmpty(orders)){
            XxlJobHelper.log("syncPackageForecastInfo end : {}", LocalDateTime.now());
            return;
        }
        List<String> mainIds = orders.stream()
                .map(PackageForecastEntity::getId)
                .distinct()
                .collect(Collectors.toList());
        List<PackageForecastDetailEntity> detailEntityList = packageForecastDetailService.listDbByMainIds(mainIds);
        List<String> soIds = detailEntityList.stream()
                .map(PackageForecastDetailEntity::getSoId)
                .distinct()
                .collect(Collectors.toList());
        Map<String,List<PackageForecastDetailEntity>> detailMap = detailEntityList.stream()
                .collect(Collectors.groupingBy(PackageForecastDetailEntity::getMainId));
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        List<PackageForecastDetailEntity> tiktokFullyDetailList = new ArrayList<>();
        List<PackageForecastEntity> tiktokFullyList = new ArrayList<>();
        //处理速卖通
        detailMap.forEach((mainId,detailList)->{
            PackageForecastEntity packageForecast = orders.stream()
                    .filter(packageForecastEntity -> packageForecastEntity.getId().equals(mainId))
                    .findFirst()
                    .orElse(null);
            SoB2cEntity soB2cEntity = soB2cEntityList.stream()
                    .filter(soB2cEntity1 -> soB2cEntity1.getId().equals(detailList.get(0).getSoId()))
                    .findFirst()
                    .orElse(null);
            if(Objects.isNull(packageForecast) || Objects.isNull(soB2cEntity)){
                return;
            }
            if(soB2cEntity.getDictPlatform().equals(PlatformDictEnum.ALI_EXPRESS.getCode())
             && StringUtils.isNotBlank(packageForecast.getHandoverNo())){
                packageForecastService.queryAliExpressInfo(packageForecast);
                XxlJobHelper.log("syncPackageForecastInfo update : {}", packageForecast.getHandoverNo());
            }

            if(soB2cEntity.getDictPlatform().equals(PlatformDictEnum.TIK_TOK_FULLY.getCode())
             && packageForecast.getCollectMode().equals(PackageForecastCollectModeEnum.TO_HOME.getCode())){
                packageForecast.setShopId(soB2cEntity.getShopId());
                tiktokFullyList.add(packageForecast);
                tiktokFullyDetailList.addAll(detailList);
            }
        });
        List<PackageForecastEntity> updateList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(tiktokFullyList) && CollectionUtils.isNotEmpty(tiktokFullyDetailList)){
            List<String> allHandoverNos = tiktokFullyList.stream()
                    .map(PackageForecastEntity::getHandoverNo)
                    .filter(StrUtils::isNotEmpty)
                    .distinct()
                    .collect(Collectors.toList());
            if(CollectionUtils.isEmpty(allHandoverNos)){
                return;
            }
            String shopId = tiktokFullyList.get(0).getShopId();
            List<List<String>> handoverNoList = ListUtil.partition(allHandoverNos,50);
            for (List<String> handoverNos : handoverNoList) {
                try {
                    TikTokFullyLogisticResp tikTokFullyLogisticResp = tikTokFullService.queryLogistics(shopId,handoverNos);
                    List<TikTokFullyLogisticResp.DataDTO.LogisticsOrdersDTO> logisticsOrders = tikTokFullyLogisticResp.getData().getLogisticsOrders();
                    for (TikTokFullyLogisticResp.DataDTO.LogisticsOrdersDTO logisticsOrder : logisticsOrders) {
                        List<PackageForecastEntity> packageForecast = tiktokFullyList.stream()
                                .filter(packageForecastEntity -> packageForecastEntity.getHandoverNo().equals(logisticsOrder.getCode()))
                                .collect(Collectors.toList());
                        if(CollectionUtils.isEmpty(packageForecast)){
                            continue;
                        }
                        //返回的运单号可能有多个，拼接起来
                        List<String> transportNoList = logisticsOrder.getLogisticsSubOrders().stream()
                                .map(TikTokFullyLogisticResp.DataDTO.LogisticsOrdersDTO.LogisticsSubOrdersDTO::getTrackingNumber)
                                .collect(Collectors.toList());
                        String transportNo = String.join(",", transportNoList);
                        List<String> subLogisticCodeList = logisticsOrder.getLogisticsSubOrders().stream()
                                .map(TikTokFullyLogisticResp.DataDTO.LogisticsOrdersDTO.LogisticsSubOrdersDTO::getCode)
                                .collect(Collectors.toList());
                        String subLogisticCode = String.join(",", subLogisticCodeList);
                        for (PackageForecastEntity entity : packageForecast) {
                            entity.setPlatformPackageNo(subLogisticCode);
                            entity.setTransportNo(transportNo);
                            entity.setHandoverStatus(logisticsOrder.getStatus());
                            entity.setPlatformNo(entity.getHandoverNo() + "/" + entity.getPlatformPackageNo());
                        }
                        updateList.addAll(packageForecast);
                    }
                }catch (Exception e){
                    log.error("查询tiktok全托管异常 : ",e);
                    XxlJobHelper.log("查询tiktok全托管异常 : ",e );
                }
            }
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            packageForecastService.updateBatchById(updateList);
        }
        XxlJobHelper.log("syncPackageForecastInfo end : {}", LocalDateTime.now());
    }
}
