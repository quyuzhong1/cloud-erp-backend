package com.erp.server.mrp.calculation.strategy.sales;

import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.CfgSettingDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgSettingEnum;
import com.erp.model.mrp.enums.SalesEstimateTypeEnum;
import com.erp.server.mrp.es.entity.CustomerSalesEstimateEsEntity;
import com.erp.server.mrp.es.service.CustomerSalesEstimateEsService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.SalesEstimateTypeEnum.CUSTOMER;

@Component
public class CustomerStrategy implements SalesEstimateStrategy {


    @Resource
    private CustomerSalesEstimateEsService customerSalesEstimateEsService;
    @Override
    public SalesEstimateTypeEnum getType() {
        return CUSTOMER;
    }

    @Override
    public void process(ReplenishmentResultDTO dto) {
        ReplenishmentResultDTO.BasicDTO replenishment = dto.getReplenishment();
        LocalDate basicCalcDate = LocalDate.parse(dto.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        CfgRuleStrategyDTO cfgRuleStrategyDTO = dto.getCfgRuleStrategy();
        List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates = new ArrayList<>();
        //计算天数
        int days = cfgRuleStrategyDTO.getSettings()
                .stream().filter(v -> v.getKey().equals(CfgSettingEnum.CALCULATION_DAYS.getCode()))
                .map(CfgSettingDTO::getDataJson)
                .map(Integer::parseInt)
                .findFirst().orElse(0);
        List<CustomerSalesEstimateEsEntity> salesEstimateList = customerSalesEstimateEsService.listByShopSkuIdAndDate(Collections.singletonList(replenishment.getShopId() + "-" + replenishment.getSkuId()),
                basicCalcDate, basicCalcDate.plusDays(days));
        Map<LocalDate, BigDecimal> saleQtyMap = salesEstimateList.stream()
                .collect(Collectors.toMap(CustomerSalesEstimateEsEntity::getDate, CustomerSalesEstimateEsEntity::getSalesQty, (o1, o2) -> o1));
        for (int i = 0; i < days; i++) {
            LocalDate calcDate = basicCalcDate.plusDays(i);
            BigDecimal saleQty = Optional.ofNullable(saleQtyMap.get(calcDate)).orElse(BigDecimal.ZERO);
            ReplenishmentResultDTO.SalesEstimateDTO salesEstimateDTO = ReplenishmentResultDTO.SalesEstimateDTO.buildSalesEstimateDTO(calcDate, saleQty);
            salesEstimates.add(salesEstimateDTO);
        }
        dto.setSalesEstimates(salesEstimates);
    }
}
