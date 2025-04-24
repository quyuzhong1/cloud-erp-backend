package com.erp.model.tms.dto;

import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.entity.InitFirstMileAllocationDetailEntity;

import java.math.BigDecimal;


public class FirstMileCostAllocationParamDTO {
    private final FirstMileSkuCostAllocationDetailEntity detailEntity;
    private final FirstMileCostAllocationDTO.JudgeReconciliationDTO judgeReconciliationDTO;
    private final BigDecimal productAllocatedAmount;
    private final int currentMonthReceiveQty;
    private final int receiveQty;
    private final Integer deliveryQty;
    private final int asLastMonthReceiveQty;
    private final InitFirstMileAllocationDetailEntity initEntity;

    /**
     * @param detailEntity
     * @param judgeReconciliationDTO
     * @param productAllocatedAmount
     * @param currentMonthReceiveQty
     * @param receiveQty
     * @param deliveryQty
     * @param asLastMonthReceiveQty
     * @param initEntity
     */
    public FirstMileCostAllocationParamDTO(FirstMileSkuCostAllocationDetailEntity detailEntity, FirstMileCostAllocationDTO.JudgeReconciliationDTO judgeReconciliationDTO, BigDecimal productAllocatedAmount, int currentMonthReceiveQty, int receiveQty, Integer deliveryQty, int asLastMonthReceiveQty, InitFirstMileAllocationDetailEntity initEntity) {
        this.detailEntity = detailEntity;
        this.judgeReconciliationDTO = judgeReconciliationDTO;
        this.productAllocatedAmount = productAllocatedAmount;
        this.currentMonthReceiveQty = currentMonthReceiveQty;
        this.receiveQty = receiveQty;
        this.deliveryQty = deliveryQty;
        this.asLastMonthReceiveQty = asLastMonthReceiveQty;
        this.initEntity = initEntity;
    }

    public FirstMileSkuCostAllocationDetailEntity getDetailEntity() {
        return detailEntity;
    }

    public FirstMileCostAllocationDTO.JudgeReconciliationDTO getJudgeReconciliationDTO() {
        return judgeReconciliationDTO;
    }

    public BigDecimal getProductAllocatedAmount() {
        return productAllocatedAmount;
    }

    public int getCurrentMonthReceiveQty() {
        return currentMonthReceiveQty;
    }

    public int getReceiveQty() {
        return receiveQty;
    }

    public Integer getDeliveryQty() {
        return deliveryQty;
    }

    public int getAsLastMonthReceiveQty() {
        return asLastMonthReceiveQty;
    }

    public InitFirstMileAllocationDetailEntity getInitEntity() {
        return initEntity;
    }
}
