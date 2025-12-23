package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.service.*;
import com.sdk.oms.temu.dto.TemuLogisticShipmentDTO;
import com.sdk.oms.temu.dto.TemuOrderDTO;
import com.sdk.oms.temu.dto.TemuOrderReq;
import com.sdk.oms.temu.dto.TemuResp;
import com.sdk.oms.temu.service.TemuClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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
    @Resource
    private B2bThirdDeliveryService b2bThirdDeliveryService;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;


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

    /**
     * 查询B2B三方仓发货单状态
     */
    @XxlJob("queryB2bThirdWarehouseDeliveryStatusJob")
    public void queryB2bThirdWarehouseDeliveryStatusJob() {
        XxlJobHelper.log("开始查询B2B三方仓发货单状态");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", jobParam);

        //获取需要查询的订单数据
        List<String> statusList = Arrays.asList(ThirdDeliveryStatusEnum.INTERCEPTING.getCode(), ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode());
        List<B2bThirdDeliveryEntity> entityList = b2bThirdDeliveryService.queryDeliveryStatus(Boolean.TRUE, statusList);
        if(CollectionUtils.isEmpty(entityList)){
            XxlJobHelper.log("没有找到需要查询的B2B三方仓发货单，日期={}", LocalDate.now());
            return;
        }
        List<String> warehouseIds = entityList.stream().map(B2bThirdDeliveryEntity::getDeliveryWarehouseId).distinct().collect(Collectors.toList());
        List<OverseasProviderWarehouseDTO.ViewDTO> warehouseList = overseasProviderWarehouseService.listByWarehouseIdList(warehouseIds);
        Map<String, List<OverseasProviderWarehouseDTO.ViewDTO>> authMap = warehouseList.stream().collect(Collectors.groupingBy(OverseasProviderWarehouseDTO.ViewDTO::getMainId));
        for (String mainId :authMap.keySet()){
            List<OverseasProviderWarehouseDTO.ViewDTO> viewDTOS = authMap.get(mainId);
            List<String> warehouseId1s = viewDTOS.stream().map(OverseasProviderWarehouseDTO.ViewDTO::getWarehouseId).collect(Collectors.toList());
            List<String> erpOrderCodeList = entityList.stream()
                    .filter(entity -> warehouseId1s.contains(entity.getDeliveryWarehouseId())).map(B2bThirdDeliveryEntity::getCode).distinct()
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(erpOrderCodeList)){
                continue;
            }
            List<List<String>> partition = ListUtil.partition(erpOrderCodeList, 100);
            for (List<String> subList : partition){
                //相同授权统一处理
                ThirdWarehouseQueryFbaOutboundReq queryOutboundReq = new ThirdWarehouseQueryFbaOutboundReq();
                queryOutboundReq.setErpOrderCodeList(subList);
                queryOutboundReq.setAuthId(mainId);
                queryOutboundReq.setThirdWarehouseProvideCode(viewDTOS.get(0).getProviderCode());
                ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(viewDTOS.get(0).getProviderCode());
                ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> listApiResult = service.queryFbaOutboundBill(queryOutboundReq, mainId);
                //处理返回结果
                if (listApiResult.isSuccess()){
                    List<ThirdWarehouseQueryFbaOutboundResponse> responses = listApiResult.getData();
                    if (CollUtil.isNotEmpty(responses)){
                        b2bThirdDeliveryService.updateQueryResult(responses, viewDTOS.get(0).getProviderCode(),subList);
                    }
                }
            }

        }
        XxlJobHelper.log("结束查询B2B三方仓发货单状态");
    }
}
