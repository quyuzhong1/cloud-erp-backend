package com.erp.model.tms.dto;

import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.entity.InitFirstMileAllocationDetailEntity;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;

import java.math.BigDecimal;

public class CurrentPeriodAllocatedCostDTO {
    private final FirstMileSkuCostAllocationDetailEntity detailEntity;
    private final FirstMileCostAllocationDTO.JudgeReconciliationDTO judgeReconciliationDTO;
    private final InitFirstMileAllocationDetailEntity initEntity;
    private final BigDecimal productAllocatedAmount;
    private final int receiveQty;
    private final Integer deliveryQty;
    private final int currentMonthReceiveQty;
    private final TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity;
    private final int asCurrentMonthReceiveQty;
    private final Integer initReceiveQty;

    /**
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
    public CurrentPeriodAllocatedCostDTO(FirstMileSkuCostAllocationDetailEntity detailEntity, FirstMileCostAllocationDTO.JudgeReconciliationDTO judgeReconciliationDTO, InitFirstMileAllocationDetailEntity initEntity, BigDecimal productAllocatedAmount, int receiveQty, Integer deliveryQty, int currentMonthReceiveQty, TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity, int asCurrentMonthReceiveQty, Integer initReceiveQty) {
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
    }

    public FirstMileSkuCostAllocationDetailEntity getDetailEntity() {
        return detailEntity;
    }

    public FirstMileCostAllocationDTO.JudgeReconciliationDTO getJudgeReconciliationDTO() {
        return judgeReconciliationDTO;
    }

    public InitFirstMileAllocationDetailEntity getInitEntity() {
        return initEntity;
    }

    public BigDecimal getProductAllocatedAmount() {
        return productAllocatedAmount;
    }

    public int getReceiveQty() {
        return receiveQty;
    }

    public Integer getDeliveryQty() {
        return deliveryQty;
    }

    public int getCurrentMonthReceiveQty() {
        return currentMonthReceiveQty;
    }

    public TmsFirstMileReconciliationDetailEntity getReconciliationDetailEntity() {
        return reconciliationDetailEntity;
    }

    public int getAsCurrentMonthReceiveQty() {
        return asCurrentMonthReceiveQty;
    }

    public Integer getInitReceiveQty() {
        return initReceiveQty;
    }
}
