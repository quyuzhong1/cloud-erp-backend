package com.erp.server.dmp.push.consumer.wangdian;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.server.dmp.push.service.wdt.WdtOtherInStockService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
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
 * 旺店通其他入库单消费(推送其他入库单到旺店通)
 * @date 2024-05-24
 * @author tanmujin
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC,
        selectorExpression = "wdt_other_in_stock_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_WDT_OTHER_IN_STOCK,
        consumeMode = ConsumeMode.ORDERLY)
public class WdtOtherInStockConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private WdtOtherInStockService wdtPushOtherInStockService;
    private final RateLimiter limiter = RateLimiter.create(1, 1, TimeUnit.SECONDS);

    @Override
    public void updateSyncTaskStatus(String syncTaskId, SyncStatusEnum code, String msg) {
        dmpPushTaskService.updateStatus(new DmpSyncMqDTO.ParamDTO(syncTaskId, code.getCode(), msg));
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
        CreateOtherStockinRequest request = JSON.parseObject(JSONUtil.toJsonStr(ext), CreateOtherStockinRequest.class);

        //查询同一个来源单据下的推送任务
        List<String> sourceCodeList = Collections.singletonList(request.getSourceId());
        DmpSyncTaskDTO.ListCodeDTO listDTO = new DmpSyncTaskDTO.ListCodeDTO(sourceCodeList, request.getTargetPlatformName(), request.getSourcePlatformName());
        List<DmpPushTaskEntity> taskList = dmpPushTaskService.listByCodeParam(listDTO);

        //保证数据推送的先后顺序:
        //1.同一个单据的审核操作必须早于反审核操作推送数据
        if(StringUtils.equals(SyncOperateEnum.OPERATE_DISAPPROVE.getCode(), request.getOperateCode())){
            long count = taskList.stream()
                    .filter(task -> StringUtils.equals(SyncOperateEnum.OPERATE_APPROVE.getCode(), task.getSyncOperate())
                            && !StringUtils.equals(task.getStatus(), SyncStatusEnum.SUCCESS_SYNC.getCode())
                            && !StringUtils.equals(task.getStatus(), SyncStatusEnum.NO_NEED_SYNC.getCode()))
                    .count();
            if(count > 0){
                return ApiResult.error(ApiError.ERROR_WDT_CANCEL_PUSH.code, String.format("前序任务未完成, 跳过本次推送: %s", request));
            }
        }

        //2.在单据的单一操作内部, 比如调拨单的审核操作, 出库单必须早于入库单推送
        long count = taskList.stream()
                .filter(task -> StringUtils.equals(task.getSyncOperate(), request.getOperateCode())
                        && !StringUtils.equals(task.getStatus(), SyncStatusEnum.SUCCESS_SYNC.getCode())
                        && !StringUtils.equals(task.getStatus(), SyncStatusEnum.NO_NEED_SYNC.getCode())
                        && JSON.parseObject(task.getMqData(), CreateOtherStockinRequest.class).getCreateTime().isBefore(request.getCreateTime()))
                .count();
        //先创建的任务必须先完成
        if(count > 0){
            return ApiResult.error(ApiError.ERROR_WDT_CANCEL_PUSH.code, String.format("前序任务未完成, 跳过本次推送: %s", request));
        }

        //请求旺店通
        limiter.acquire();
        wdtPushOtherInStockService.executeConsumer(request);


        return ApiResult.success();
    }
}
