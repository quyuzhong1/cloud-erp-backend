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
import com.erp.server.dmp.push.service.wdt.WdtOtherOutStockService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 旺店通其他出库单消费
 * @date 2024-05-24
 * @author tanmujin
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
        CreateOtherStockoutRequest request = JSON.parseObject(JSONUtil.toJsonStr(ext), CreateOtherStockoutRequest.class);

        //查询同一个来源单据下的推送任务
        List<String> sourceIdList = Collections.singletonList(request.getSourceId());
        DmpSyncTaskDTO.ListDTO listDTO = new DmpSyncTaskDTO.ListDTO(sourceIdList, request.getTargetPlatformName(), request.getSourcePlatformName());
        List<DmpPushTaskEntity> taskList = dmpPushTaskService.listByParam(listDTO);

        //保证数据推送的先后顺序:
        //1.同一个单据的审核操作必须早于反审核操作推送数据
        if(StringUtils.equals(SyncOperateEnum.OPERATE_DISAPPROVE.getCode(), request.getOperateCode())){
            long count = taskList.stream()
                    .filter(task -> StringUtils.equals(SyncOperateEnum.OPERATE_APPROVE.getCode(), task.getSyncOperate())
                            && !StringUtils.equals(task.getStatus(), SyncStatusEnum.SUCCESS_SYNC.getCode())
                            && !StringUtils.equals(task.getStatus(), SyncStatusEnum.NO_NEED_SYNC.getCode()))
                    .count();
            if(count > 0){
                return ApiResult.error(ApiError.ERROR_WDT_CANCEL_PUSH.code, String.format("前序任务未完成, 其他出库单取消推送: %s", request));
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
        if(count > 0){
            return ApiResult.error(ApiError.ERROR_WDT_CANCEL_PUSH.code, String.format("前序任务未完成, 其他出库单取消推送: %s", request));
        }

        //请求旺店通
        wdtService.executeConsumer(request);
        return ApiResult.success();
    }
}
