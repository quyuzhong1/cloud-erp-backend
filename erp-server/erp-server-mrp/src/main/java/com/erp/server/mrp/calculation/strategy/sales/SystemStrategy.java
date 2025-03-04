package com.erp.server.mrp.calculation.strategy.sales;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.SalesEstimateTypeEnum;
import org.springframework.stereotype.Component;

import static com.erp.model.mrp.enums.SalesEstimateTypeEnum.SYSTEM;

@Component
public class SystemStrategy implements SalesEstimateStrategy {
    @Override
    public SalesEstimateTypeEnum getType() {
        return SYSTEM;
    }

    @Override
    public void process(ReplenishmentResultDTO dto) {

    }
}
