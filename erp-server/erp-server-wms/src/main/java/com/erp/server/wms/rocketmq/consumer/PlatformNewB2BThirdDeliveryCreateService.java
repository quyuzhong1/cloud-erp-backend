package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.model.wms.enums.B2BThirdDeliveryCancelResultEnum;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.service.B2bThirdDeliveryService;
import com.erp.server.wms.service.ThirdWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author zdy
 * @version 1.0
 * @date 2025/12/01 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_B2B_THIRD_DELIVERY_CREATE_ERP_TOPIC,
        selectorExpression = "${spring.cloud.nacos.discovery.namespace}-erp_b2b_third_delivery_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-erp_dmp_group",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewB2BThirdDeliveryCreateService extends AbstractNewPlatformConsumerHandler {
    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;
    @Resource
    private B2bThirdDeliveryService b2bThirdDeliveryService;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_SECONDS = 10000;

    @Override
    public String getBizName() {
        return "创建B2B三方仓出库单";
    }

    @Override
    public void handle(String data) {
        log.warn("B2BThirdDelivery创建订单请求参数：{}", data);
        JSONObject jsonObject = JSONUtil.parseObj(data);
        ThirdWarehouseCreateFbaOutboundReq req = JSONUtil.toBean(jsonObject, ThirdWarehouseCreateFbaOutboundReq.class);
        String sourceId = req.getSourceId();
        B2bThirdDeliveryEntity entity = b2bThirdDeliveryService.getById(sourceId);
        if (Objects.isNull(entity)) {
            b2bThirdDeliveryService.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), CharSequenceUtil.format(ApiError.NOT_EXIST.msg, req.getSourceCode()), "", "", "");
            return;
        }

        ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(req.getThirdWarehouseProvideCode());
        if (Objects.isNull(service)) {
            b2bThirdDeliveryService.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), CharSequenceUtil.format(ApiError.OVERSEAS_PROVIDE_NOT_SERVICE.msg, req.getThirdWarehouseProvideCode()), "", "", "");
            return;
        }
        ApiResult<String> fbaOutboundBill = createFbaOutboundBill(service, req, 0);
        if (fbaOutboundBill.isSuccess()) {
            // 创建成功
            b2bThirdDeliveryService.updateStatus(sourceId, ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(), "", fbaOutboundBill.getData(), "", "");
            return;
        }
        // 创建失败，尝试查询是否实际已创建成功
        ThirdWarehouseQueryFbaOutboundReq queryOutboundReq = new ThirdWarehouseQueryFbaOutboundReq();
        queryOutboundReq.setErpOrderCodeList(Collections.singletonList(req.getReferenceNo()));
        queryOutboundReq.setAuthId(req.getAuthId());
        queryOutboundReq.setThirdWarehouseProvideCode(req.getThirdWarehouseProvideCode());

        ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryResult = service.queryFbaOutboundBill(queryOutboundReq, req.getAuthId());

        String platformOrderCode = getPlatformOrderCode(queryResult);
        String trackNo = getTrackNo(queryResult);

        if (CharSequenceUtil.isNotBlank(platformOrderCode)) {
            // 查询发现订单实际已创建成功
            b2bThirdDeliveryService.updateStatus(sourceId, ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(), "", platformOrderCode, "", trackNo);
        } else {
            // 确认创建失败
            String errorMsg = CharSequenceUtil.format(ApiError.FBA_OUTBOUND_BILL_CREATE_FAILED.msg, fbaOutboundBill.getMsg());
            b2bThirdDeliveryService.updateStatus(sourceId, ThirdDeliveryStatusEnum.FAILED.getCode(), errorMsg, "", "", trackNo);
        }
    }

    // 提取平台订单号的辅助方法
    private String getPlatformOrderCode(ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> result) {
        if (result.isSuccess() && CollUtil.isNotEmpty(result.getData())) {
            return result.getData().get(0).getPlatformOrderCode();
        }
        return null;
    }
    private String getTrackNo(ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> result) {
        if (result.isSuccess() && CollUtil.isNotEmpty(result.getData())) {
            return result.getData().get(0).getTrackNo();
        }
        return null;
    }
    private ApiResult<String> createFbaOutboundBill(ThirdWarehouseService service, ThirdWarehouseCreateFbaOutboundReq req, final int retryCount) {
        ThirdWarehouseQueryFbaOutboundReq queryOutboundReq = new ThirdWarehouseQueryFbaOutboundReq();
        queryOutboundReq.setErpOrderCodeList(Collections.singletonList(req.getReferenceNo()));
        queryOutboundReq.setAuthId(req.getAuthId());
        queryOutboundReq.setThirdWarehouseProvideCode(req.getThirdWarehouseProvideCode());
        ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> listApiResult = service.queryFbaOutboundBill(queryOutboundReq, req.getAuthId());
        String platformOrderCode = getPlatformOrderCode(listApiResult);
        if (CharSequenceUtil.isNotBlank(platformOrderCode)){
            return ApiResult.success(platformOrderCode);
        }

        try {
            return service.createFbaOutboundBill(req, req.getAuthId());
        } catch (Exception e) {
            log.warn("第{}次执行失败: {}", retryCount + 1, e.getMessage());

            if (retryCount + 1 < MAX_RETRY_COUNT) {
                try {
                    log.info("{}秒后进行第{}次重试", RETRY_DELAY_SECONDS / 1000, retryCount + 2);
                    Thread.sleep(RETRY_DELAY_SECONDS);
                    return createFbaOutboundBill(service, req, retryCount + 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return ApiResult.error(-1, "重试被中断");
                }
            } else {
                log.error("已达到最大重试次数{}次，停止重试", MAX_RETRY_COUNT);
                return ApiResult.error(-1, e.getMessage());
            }
        }
    }
}
