package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.CfgSettingDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgSettingEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

@Component
public class RealSellableDaysHandler extends AbstractSkuCalculationHandler {
    @Resource
    private DeliverySuggestHandler deliverySuggestHandler;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return deliverySuggestHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        //补货建议的真实可售天数（最近断货日期-今天）
        ReplenishmentResultDTO.RptOutOfStockDTO rptOutOfStockDTO = replenishmentResultDTO.getRptOutOfStocks()
                .stream()
                .min(Comparator.comparing(ReplenishmentResultDTO.RptOutOfStockDTO::getDate))
                .orElse(null);
        int days;
        if (ObjectUtils.isEmpty(rptOutOfStockDTO)) {
            days = cfgRuleStrategyDTO.getSettings()
                    .stream().filter(v -> v.getKey().equals(CfgSettingEnum.CALCULATION_DAYS.getCode()))
                    .map(CfgSettingDTO::getDataJson)
                    .map(Integer::parseInt)
                    .findFirst().orElse(0);
            replenishmentResultDTO.getReplenishmentDetail().setSellableDays(days);
        } else {
            days = (int) ChronoUnit.DAYS.between(LocalDate.now(), rptOutOfStockDTO.getDate());
        }
        replenishmentResultDTO.getReplenishmentDetail().setSellableDays(Math.max(0, days));
    }
}
