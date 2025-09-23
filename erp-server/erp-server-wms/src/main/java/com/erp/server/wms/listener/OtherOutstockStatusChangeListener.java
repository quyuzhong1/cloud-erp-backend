package com.erp.server.wms.listener;

import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.wms.event.OtherOutstockStatusChangeEvent;
import com.erp.server.wms.service.SampleRecipientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 其他出库单状态变更事件监听器
 * 用于更新样品领用单的已出库数量
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Component
@Slf4j
public class OtherOutstockStatusChangeListener {

    @Autowired
    private SampleRecipientService sampleRecipientService;

    /**
     * 监听其他出库单状态变更事件
     *
     * @param event 状态变更事件
     */
    @EventListener
    @Async("outboundOrderDetailChangeEventPool")
    public void handleOtherOutstockStatusChange(OtherOutstockStatusChangeEvent event) {
        try {
            log.info("收到其他出库单状态变更事件，出库单ID：{}，状态：{} -> {}",
                event.getOutboundOrderId(), event.getOldStatus(), event.getNewStatus());

            // 根据状态变更类型处理
            if (ApproveTypeEnum.PASS.getStatus().equals(event.getNewStatus())) {
                // 审核通过：增加已出库数量
                handleApprove(event);
            } else if (ApproveTypeEnum.DIS_APPROVE.getStatus().equals(event.getNewStatus())) {
                // 反审核：减少已出库数量
                handleDisApprove(event);
            } else {
                log.info("其他出库单状态变更无需处理，状态：{} -> {}",
                    event.getOldStatus(), event.getNewStatus());
            }
        } catch (Exception e) {
            log.error("处理其他出库单状态变更事件失败，出库单ID：{}，错误：{}",
                event.getOutboundOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 处理审核通过事件
     *
     * @param event 状态变更事件
     */
    private void handleApprove(OtherOutstockStatusChangeEvent event) {
        log.info("处理其他出库单审核通过事件，出库单ID：{}", event.getOutboundOrderId());

        // 检查主表来源类型
        if (!SourceTypeEnum.SAMPLE_RECIPIENT.getCode().equals(event.getSourceType())) {
            log.info("其他出库单来源类型不是样品领用单，无需处理，来源类型：{}", event.getSourceType());
            return;
        }

        if (event.getDetailChanges() != null && !event.getDetailChanges().isEmpty()) {
            for (OtherOutstockStatusChangeEvent.OtherOutstockDetailChangeEvent detailChange : event.getDetailChanges()) {
                try {
                    // 调用服务更新样品领用单明细的已出库数量
                    sampleRecipientService.increaseDeliveryQty(
                        detailChange.getSourceDetailId(),
                        detailChange.getActualQty()
                    );
                    log.info("成功更新样品领用单明细已出库数量，明细ID：{}，增加数量：{}",
                        detailChange.getSourceDetailId(), detailChange.getActualQty());
                } catch (Exception e) {
                    log.error("更新样品领用单已出库数量失败，明细ID：{}，错误：{}",
                        detailChange.getSourceDetailId(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 处理反审核事件
     *
     * @param event 状态变更事件
     */
    private void handleDisApprove(OtherOutstockStatusChangeEvent event) {
        log.info("处理其他出库单反审核事件，出库单ID：{}", event.getOutboundOrderId());

        // 检查主表来源类型
        if (!SourceTypeEnum.SAMPLE_RECIPIENT.getCode().equals(event.getSourceType())) {
            log.info("其他出库单来源类型不是样品领用单，无需处理，来源类型：{}", event.getSourceType());
            return;
        }

        if (event.getDetailChanges() != null && !event.getDetailChanges().isEmpty()) {
            for (OtherOutstockStatusChangeEvent.OtherOutstockDetailChangeEvent detailChange : event.getDetailChanges()) {
                try {
                    // 调用服务减少样品领用单明细的已出库数量
                    sampleRecipientService.decreaseDeliveryQty(
                        detailChange.getSourceDetailId(),
                        detailChange.getActualQty()
                    );
                    log.info("成功减少样品领用单明细已出库数量，明细ID：{}，减少数量：{}",
                        detailChange.getSourceDetailId(), detailChange.getActualQty());
                } catch (Exception e) {
                    log.error("减少样品领用单已出库数量失败，明细ID：{}，错误：{}",
                        detailChange.getSourceDetailId(), e.getMessage(), e);
                }
            }
        }
    }
}
