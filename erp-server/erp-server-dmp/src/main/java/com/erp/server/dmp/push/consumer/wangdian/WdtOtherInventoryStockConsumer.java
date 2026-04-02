package com.erp.server.dmp.push.consumer.wangdian;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.InventoryOrderTypeEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.server.dmp.push.service.wdt.WdtOtherInStockService;
import com.erp.server.dmp.push.service.wdt.WdtOtherOutStockService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.erp.server.dmp.service.DmpWdtWarehouseInventoryRecordService;
import com.erp.server.dmp.service.ThirdMappingService;
import com.erp.server.dmp.service.ThirdWarehouseService;
import com.sdk.wangdian.enums.WdtInStockStatusEnum;
import com.sdk.wangdian.enums.WdtOutStockStatusEnum;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.OtherStockinResponse;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.StockoutOtherQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.python.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 旺店通库单消费(推送其他出入库单到旺店通)
 *
 * @author zdy
 * @date 2026-01-28
 */
@Component
@Slf4j
//@RocketMQMessageListener(topic = RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC,
//        selectorExpression = "wdt_other_in_stock_tag",
//        consumerGroup = RocketMqConsumerGroup.SYNC_WDT_OTHER_IN_STOCK,
//        consumeMode = ConsumeMode.ORDERLY)
public class WdtOtherInventoryStockConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private WdtOtherInStockService wdtOtherInStockService;
    @Resource
    private WdtOtherOutStockService wdtOtherOutStockService;
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private ThirdWarehouseService thirdWarehouseService;
    @Resource
    private DmpWdtWarehouseInventoryRecordService dmpWdtWarehouseInventoryRecordService;

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
        ApiResult<?> apiResult = null;
        try {
        apiResult = dataProcess(ext);
        }catch (Exception e){
            //操作异常
            log.error("WdtOtherInventoryStockConsumer handle, ext: {}, e:", ext, e);
            apiResult = ApiResult.error(e.getMessage().length() > 100 ? e.getMessage().substring(0, 100) : e.getMessage());
        }
        String requestStr = JSONUtil.toJsonStr(ext);
        // 处理参数中存在null字符串的数据
        requestStr = requestStr.replace("null", "");

        JSONObject parseObject = JSON.parseObject(requestStr);
        String batchNo = parseObject.getString("outerNo");

        String warehouseNo = parseObject.getString("warehouseNo");
        JSONArray goodsList = parseObject.getJSONArray("goodsList");
        List<String> skuNoList = null;
        if (CollUtil.isNotEmpty(goodsList)){
            skuNoList = goodsList.stream().map(item -> ((JSONObject) item).getString("specNo")).collect(Collectors.toList());
        }
        if (apiResult.isSuccess()){
            updateInventoryStatus(batchNo,warehouseNo,skuNoList, DmpInputTaskStatusEnum.FINISH.getCode(),"");
        }else {
            updateInventoryStatus(batchNo,warehouseNo,skuNoList, DmpInputTaskStatusEnum.ERROR.getCode(),apiResult.getMsg());
        }
        return apiResult;
    }

    private void updateInventoryStatus(String batchNo, String warehouseNo, List<String> skuNoList, String status, String msg) {
        if (CharSequenceUtil.isAllNotBlank(batchNo,warehouseNo,status) && CollUtil.isNotEmpty(skuNoList)){
            dmpWdtWarehouseInventoryRecordService.updateInventoryStatus(batchNo, warehouseNo, skuNoList, status, msg);
        }
    }

    private ApiResult<?> dataProcess(Object ext) {
        log.warn("WdtOtherInventoryStockConsumer handle, ext: {}", ext);
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

        JSONObject parseObject = JSON.parseObject(requestStr);
        String orderType = parseObject.getString("orderType");
        String sourceId = parseObject.getString("sourceId");
        String targetPlatformName = parseObject.getString("targetPlatformName");
        String sourcePlatformName = parseObject.getString("sourcePlatformName");
        String operateCode = parseObject.getString("operateCode");
        LocalDateTime createTime = LocalDateTime.parse(parseObject.getString("createTime"));
        String warehouseNo = parseObject.getString("warehouseNo");

        //查询同一个来源单据下的推送任务
        List<String> sourceCodeList = Collections.singletonList(sourceId);
        DmpSyncTaskDTO.ListCodeDTO listDTO = new DmpSyncTaskDTO.ListCodeDTO(sourceCodeList, targetPlatformName, sourcePlatformName);
        List<DmpPushTaskEntity> taskList = dmpPushTaskService.listByCodeParam(listDTO);

        //保证数据推送的先后顺序:
        //1.同一个单据的审核操作必须早于反审核操作推送数据
        if (StringUtils.equals(SyncOperateEnum.OPERATE_DISAPPROVE.getCode(), operateCode)) {
            long count = taskList.stream()
                    .filter(task -> StringUtils.equals(SyncOperateEnum.OPERATE_APPROVE.getCode(), task.getSyncOperate())
                            && !StringUtils.equals(task.getStatus(), SyncStatusEnum.SUCCESS_SYNC.getCode())
                            && !StringUtils.equals(task.getStatus(), SyncStatusEnum.NO_NEED_SYNC.getCode()))
                    .count();
            if (count > 0) {
                return ApiResult.error(ApiError.COMMON_WDT_PRE_TASK_NOT_FINISHED_CANCEL_EXECUTION.getCode(), String.format("前序任务未完成, 跳过本次推送: %s", requestStr));
            }
        }

        //2.在单据的单一操作内部, 比如调拨单的审核操作, 出库单必须早于入库单推送
        long count = taskList.stream()
                .filter(task -> StringUtils.equals(task.getSyncOperate(), operateCode)
                        && !StringUtils.equals(task.getStatus(), SyncStatusEnum.SUCCESS_SYNC.getCode())
                        && !StringUtils.equals(task.getStatus(), SyncStatusEnum.NO_NEED_SYNC.getCode())
                        && JSON.parseObject(task.getMqData(), CreateOtherStockinRequest.class).getCreateTime().isBefore(createTime))
                .count();
        //先创建的任务必须先完成
        if (count > 0) {
            return ApiResult.error(ApiError.COMMON_WDT_PRE_TASK_NOT_FINISHED_CANCEL_EXECUTION.getCode(), String.format("前序任务未完成, 跳过本次推送: %s", requestStr));
        }
        ThirdMappingEntity thirdMapping = thirdMappingService.getByThirdCodeAndType(warehouseNo, ThirdSysTypeEnum.WDT.getCode(), ThirdSysTypeEnum.WAREHOUSE.getCode());
        if (ObjectUtils.isEmpty(thirdMapping)) {
            throw new ServiceException(ApiError.COMMON_WDT_API_CALL_FAILED.getCode(), String.format("推送旺店通其他出库单失败: 三方仓库%s未映射", warehouseNo));
        }
        ThirdWarehouseEntity thirdWarehouse = thirdWarehouseService.getById(thirdMapping.getThirdInfoId());
        if (ObjectUtils.isEmpty(thirdWarehouse)) {
            throw new ServiceException(ApiError.COMMON_WDT_API_CALL_FAILED.getCode(), String.format("推送旺店通其他出库单失败: 三方仓库%s不存在", warehouseNo));
        }
        if (InventoryOrderTypeEnum.IN_STOCK.getCode().equals(orderType)) {
            CreateOtherStockinRequest request = JSON.parseObject(requestStr, CreateOtherStockinRequest.class);
            request.setOuterNo(request.getOuterNo() + warehouseNo);
            //查询其他入库单
            OtherStockinResponse.DataInfoDto dataInfoDto = wdtOtherInStockService.queryWithDetail(request);
            List<OtherStockinResponse.OrderInfoDto> order = dataInfoDto.getOrder();
            //旺店通已经存在这个单据
            if (order != null && !order.isEmpty()) {
                OtherStockinResponse.OrderInfoDto dto = dataInfoDto.getOrder().get(0);
                if (dto.getStatus().equals(80)) {
                    //修改推送任务状态为同步成功
                    return ApiResult.success();
                } else {
                    //修改任务的错误消息
                    String format = String.format("单据推送成功，当前状态：%s，请手动处理", WdtInStockStatusEnum.getName(String.valueOf(dto.getStatus())));
                    return ApiResult.error(format);
                }
            }
            wdtOtherInStockService.executeConsumer(request);
        } else if (InventoryOrderTypeEnum.OUT_STOCK.getCode().equals(orderType)) {
            CreateOtherStockoutRequest request = JSON.parseObject(requestStr, CreateOtherStockoutRequest.class);
            request.setOuterNo(request.getOuterNo() + warehouseNo);
            StockoutOtherQueryResponse queryResponse = wdtOtherOutStockService.queryWithDetail(request);
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
            wdtOtherOutStockService.executeConsumer(request);
        }
        return ApiResult.success();
    }
}
