package com.erp.model.tms.dto;

import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.entity.InitFirstMileAllocationDetailEntity;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class CurrentPeriodAllocatedCostDTO {
    private final FirstMileSkuCostAllocationDetailEntity detailEntity;
    private final FirstMileCostAllocationDTO.JudgeReconciliationDTO judgeReconciliationDTO;
    private final InitFirstMileAllocationDetailEntity initEntity;
    private final BigDecimal productAllocatedAmount;
    private final int receiveQty;
    private final Integer deliveryQty;
    // 当月签收数量
    private final int currentMonthReceiveQty;
    private final TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity;
    //截止本月签收数量
    private final int asCurrentMonthReceiveQty;
    private final Integer initReceiveQty;
    // 截止上月签收数量
    private final Integer asLastMonthReceiveQty;
    private final LocalDate reconciliationMonth;
    private final LocalDate reportPeriodMonth;
    private final FirstMileCostAllocationEntity entity;

    /**
     * @param entity
     * @param detailEntity
     * @param judgeReconciliationDTO
     * @param initEntity
     * @param productAllocatedAmount
     * @param receiveQty
     * @param deliveryQty
     * @param currentMonthReceiveQty
     * @param reconciliationDetailEntity
     * @param asCurrentMonthReceiveQty
     * @param initReceiveQty
     */
    public CurrentPeriodAllocatedCostDTO(FirstMileCostAllocationEntity entity, FirstMileSkuCostAllocationDetailEntity detailEntity, FirstMileCostAllocationDTO.JudgeReconciliationDTO judgeReconciliationDTO,
                                         InitFirstMileAllocationDetailEntity initEntity, BigDecimal productAllocatedAmount, int receiveQty, Integer deliveryQty,
                                         int currentMonthReceiveQty, TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity, int asCurrentMonthReceiveQty,
                                         Integer initReceiveQty, Integer asLastMonthReceiveQty, LocalDate reportPeriodMonth, LocalDate reconciliationMonth) {
        this.entity = entity;
        this.detailEntity = detailEntity;
        this.judgeReconciliationDTO = judgeReconciliationDTO;
        this.initEntity = initEntity;
        this.productAllocatedAmount = productAllocatedAmount;
        this.receiveQty = receiveQty;
        this.deliveryQty = deliveryQty;
        this.currentMonthReceiveQty = currentMonthReceiveQty;
        this.reconciliationDetailEntity = reconciliationDetailEntity;
        this.asCurrentMonthReceiveQty = asCurrentMonthReceiveQty;
        this.initReceiveQty = initReceiveQty;
        this.asLastMonthReceiveQty = asLastMonthReceiveQty;
        this.reportPeriodMonth = reportPeriodMonth;
        this.reconciliationMonth = reconciliationMonth;
    }

}
