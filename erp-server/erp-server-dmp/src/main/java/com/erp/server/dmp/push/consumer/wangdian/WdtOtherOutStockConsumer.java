package com.erp.server.dmp.push.consumer.wangdian;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.dmp.enums.WdtWarehouseTypeEnum;
import com.erp.server.dmp.push.service.wdt.WdtOtherOutStockService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.erp.server.dmp.service.ThirdMappingService;
import com.erp.server.dmp.service.ThirdWarehouseService;
import com.sdk.wangdian.enums.WdtExtOutStockStatusEnum;
import com.sdk.wangdian.enums.WdtOutStockStatusEnum;
import com.sdk.wangdian.sdk.api.wms.external.out.StockExternalOutResponse;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.StockoutOtherQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.python.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 旺店通其他出库单消费
 *
 * @author tanmujin
 * @date 2024-05-24
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC,
        selectorExpression = "wdt_other_out_stock_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_WDT_OTHER_OUT_STOCK,
        consumeMode = ConsumeMode.ORDERLY)
public class WdtOtherOutStockConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private WdtOtherOutStockService wdtService;

    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private ThirdWarehouseService thirdWarehouseService;


    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpPushTaskService.updateStatus(paramDTO);
    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {

    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpPushTaskService.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        RateLimiter limiter = RateLimiter.create(1, 1, TimeUnit.SECONDS);
        if (!limiter.tryAcquire()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("限流失败：{}", e.getMessage(), e);
            }
        }
        String requestStr = JSONUtil.toJsonStr(ext);
        // 处理参数中存在null字符串的数据
        requestStr = requestStr.replace("null", "");
        CreateOtherStockoutRequest request = JSON.parseObject(requestStr, CreateOtherStockoutRequest.class);

        //查询同一个来源单据下的推送任务
        List<String> sourceCodeList = Collections.singletonList(request.getSourceId());
        DmpSyncTaskDTO.ListCodeDTO listDTO = new DmpSyncTaskDTO.ListCodeDTO(sourceCodeList, request.getTargetPlatformName(), request.getSourcePlatformName());
        List<DmpPushTaskEntity> taskList = dmpPushTaskService.listByCodeParam(listDTO);

        //保证数据推送的先后顺序:
        //1.同一个单据的审核操作必须早于反审核操作推送数据
        if (StringUtils.equals(SyncOperateEnum.OPERATE_DISAPPROVE.getCode(), request.getOperateCode())) {
            long count = taskList.stream()
                    .filter(task -> StringUtils.equals(SyncOperateEnum.OPERATE_APPROVE.getCode(), task.getSyncOperate())
                            && !StringUtils.equals(task.getStatus(), SyncStatusEnum.SUCCESS_SYNC.getCode())
                            && !StringUtils.equals(task.getStatus(), SyncStatusEnum.NO_NEED_SYNC.getCode()))
                    .count();
            if (count > 0) {
                return ApiResult.error(ApiError.ERROR_WDT_CANCEL_PUSH.code, String.format("前序任务未完成, 跳过本次推送: %s", request));
            }
        }

        //2.在单据的单一操作内部, 比如调拨单的审核操作, 出库单必须早于入库单推送
        long count = taskList.stream()
                .filter(task -> StringUtils.equals(task.getSyncOperate(), request.getOperateCode())
                        && !StringUtils.equals(task.getStatus(), SyncStatusEnum.SUCCESS_SYNC.getCode())
                        && !StringUtils.equals(task.getStatus(), SyncStatusEnum.NO_NEED_SYNC.getCode())
                        && JSON.parseObject(task.getMqData(), CreateOtherStockoutRequest.class).getCreateTime().isBefore(request.getCreateTime()))
                .count();
        //先创建的任务必须先完成
        if (count > 0) {
            return ApiResult.error(ApiError.ERROR_WDT_CANCEL_PUSH.code, String.format("前序任务未完成, 跳过本次推送: %s", request));
        }

        ThirdMappingEntity thirdMapping = thirdMappingService.getByThirdCodeAndType(request.getWarehouseNo(), ThirdSysTypeEnum.WDT.getCode(), ThirdSysTypeEnum.WAREHOUSE.getCode());
        if (ObjectUtils.isEmpty(thirdMapping)) {
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他出库单失败: 三方仓库%s未映射", request.getWarehouseNo()));
        }
        ThirdWarehouseEntity thirdWarehouse = thirdWarehouseService.getById(thirdMapping.getThirdInfoId());
        if (ObjectUtils.isEmpty(thirdWarehouse)) {
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他出库单失败: 三方仓库%s不存在", request.getWarehouseNo()));
        }
        //根据旺店通仓库类型，决定调用的API
        if (WdtWarehouseTypeEnum.SELF_TRANSFER.getCode().equals(thirdWarehouse.getType())) {
            StockExternalOutResponse stockExternalOutResponse = wdtService.querySelfOut(request);
            if (ObjectUtils.isNotEmpty(stockExternalOutResponse) && CollectionUtils.isNotEmpty(stockExternalOutResponse.getOrder())) {
                StockExternalOutResponse.Order order = stockExternalOutResponse.getOrder().get(0);
                if (WdtExtOutStockStatusEnum.finish().contains(order.getStatus())) {
                    return ApiResult.success();
                } else {
                    //修改任务的错误消息
                    String format = String.format("单据推送成功，当前状态：%s，请手动处理", WdtExtOutStockStatusEnum.getName(order.getStatus()));
                    return ApiResult.error(format);
                }
            }
            wdtService.executeSelfConsumer(request);
        } else {
            StockoutOtherQueryResponse queryResponse = wdtService.queryWithDetail(request);
            List<StockoutOtherQueryResponse.OrderItem> order = queryResponse.getOrder();
            //旺店通已经存在这个单据
            if (!order.isEmpty()) {
                StockoutOtherQueryResponse.OrderItem orderInfoDto = order.get(0);
                if (orderInfoDto.getStatus().equals(110)) {
                    return ApiResult.success();
                } else {
                    //修改任务的错误消息
                    String format = String.format("单据推送成功，当前状态：%s，请手动处理", WdtOutStockStatusEnum.getName(String.valueOf(orderInfoDto.getStatus())));
                    return ApiResult.error(format);
                }
            }
            wdtService.executeConsumer(request);
        }
        return ApiResult.success();
    }
}
