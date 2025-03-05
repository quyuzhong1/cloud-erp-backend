package com.erp.server.mrp.calculation.strategy.sales;

import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.CfgSettingDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgSettingEnum;
import com.erp.model.mrp.enums.SalesEstimateTypeEnum;
import com.erp.server.mrp.es.entity.CustomerSalesEstimateEsEntity;
import com.erp.server.mrp.es.service.CustomerSalesEstimateEsService;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.SalesEstimateTypeEnum.UPDATE_IMPORT;

@Component
public class UpdateImportStrategy implements SalesEstimateStrategy {
    @Resource
    private CustomerSalesEstimateEsService customerSalesEstimateEsService;

    @Override
    public SalesEstimateTypeEnum getType() {
        return UPDATE_IMPORT;
    }

    @Override
    public void process(ReplenishmentResultDTO dto) {
        ReplenishmentResultDTO.BasicDTO replenishment = dto.getReplenishment();
        LocalDate basicCalcDate = LocalDate.parse(dto.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        CfgRuleStrategyDTO cfgRuleStrategyDTO = dto.getCfgRuleStrategy();
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> formulaResults = cfgRuleStrategyDTO.getSalesQtyResult().getFormulaResults();
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults = cfgRuleStrategyDTO.getSalesQtyResult().getDefaultFormulaResults();
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
            BigDecimal saleQty = saleQtyMap.get(calcDate);
            if (ObjectUtils.isEmpty(saleQty)) {
                //获取最大优先级的规则 优先取 sku 固定规则，其次sku动态规则，其次sku默认规则，取不到则取系统动态规则，其次系统默认规则
                CfgRuleSalesQtyDTO.StrategyFormulaResultDTO formulaResult = SystemStrategy.getStrategyFormulaResultDTO(formulaResults, calcDate, defaultFormulaResults);
                if (ObjectUtils.isEmpty(formulaResult)) {
                    continue;
                }
                saleQty = SystemStrategy.getSaleQty(dto.getSalesInfos(), dto.getAvgTimePeriodSales(), formulaResult, basicCalcDate);
                ReplenishmentResultDTO.SalesEstimateDTO salesEstimateDTO = ReplenishmentResultDTO.SalesEstimateDTO.buildSalesEstimateDTO(calcDate, saleQty, formulaResult);
                salesEstimates.add(salesEstimateDTO);
            } else {
                ReplenishmentResultDTO.SalesEstimateDTO salesEstimateDTO = ReplenishmentResultDTO.SalesEstimateDTO.buildSalesEstimateDTO(calcDate, saleQty);
                salesEstimates.add(salesEstimateDTO);
            }
        }
        dto.setSalesEstimates(salesEstimates);
    }
}
