package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.server.wms.service.B2bThirdDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * B2B三方仓出库状态消费服务
 */
@Slf4j
@Service
public class B2bThirdOutboundConsumerService {

    @Resource
    private B2bThirdDeliveryService b2bThirdDeliveryService;
    @Resource
    private RedissonClient redissonClient;

    public ApiResult<?> handle(String data) {
        PlatformOutboundDTO dto = JSONUtil.toBean(data, PlatformOutboundDTO.class);
        String referenceNo = dto.getReferenceNo();
        if (CharSequenceUtil.isBlank(referenceNo)) {
            return ApiResult.success();
        }
        if (!referenceNo.startsWith(BusinessNoConstant.SFFH)) {
            log.warn("B2B三方仓出库状态消息忽略，referenceNo={}", referenceNo);
            return ApiResult.success();
        }
        String lockKey = "lock:b2b:third:outbound:" + referenceNo;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(30, -1, TimeUnit.SECONDS)) {
                log.error("B2B三方仓出库状态消息获取锁超时，referenceNo={}", referenceNo);
                return ApiResult.error("系统繁忙，请稍后重试");
            }
            B2bThirdDeliveryEntity entity = b2bThirdDeliveryService.getLatestByCode(referenceNo);
            if (Objects.isNull(entity)) {
                log.warn("未找到B2B三方发货单，referenceNo={}", referenceNo);
                return ApiResult.success();
            }
            ThirdWarehouseQueryFbaOutboundResponse response = new ThirdWarehouseQueryFbaOutboundResponse();
            response.setCode(referenceNo);
            response.setPlatformOrderCode(dto.getOrderCode());
            response.setTrackNo(dto.getTrackNo());
            response.setStatus(dto.getThirdOrderStatus());
            response.setErrorType(dto.getAbnormalProblemReason());
            response.setDeliveryTimeStr(Objects.nonNull(dto.getOutBoundTime()) ? dto.getOutBoundTime().toString() : null);
            b2bThirdDeliveryService.handleResultData(entity.getId(), response);
            return ApiResult.success();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("B2B三方仓出库状态消息获取锁被中断，referenceNo={}", referenceNo, e);
            return ApiResult.error("系统异常");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
