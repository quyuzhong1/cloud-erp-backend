package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class StockingTimeHandler extends AbstractSkuCalculationHandler {
    @Resource
    private FbaUsableHandler fbaUsableHandler;
    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return fbaUsableHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = cfgRuleStrategyDTO.getStockUpResult();
        replenishmentResultDTO.getReplenishmentDetail().setPurchaseApproveDays(stockUpResult.getPurchaseApproveDays());
        replenishmentResultDTO.getReplenishmentDetail().setProductionDays(stockUpResult.getProductionDays());
        replenishmentResultDTO.getReplenishmentDetail().setSupplierDeliveryDays(stockUpResult.getSupplierDeliveryDays());
        replenishmentResultDTO.getReplenishmentDetail().setQcDays(stockUpResult.getQcDays());
        replenishmentResultDTO.getReplenishmentDetail().setPurchaseCycleDays(stockUpResult.getPurchaseCycleDays());
        replenishmentResultDTO.getReplenishmentDetail().setSafeDays(stockUpResult.getSafeDays());
        replenishmentResultDTO.getReplenishmentDetail().setInstockDays(stockUpResult.getInstockDays());
        //FBA备货时长：
        //最短：本地发FBA时效（最短）+ FBA入库天数
        //默认：采购审批时长 + 生产周期 + 供应商发货时长 + 质检入库时长 + 本地发FBA时效（默认） + FBA入库天数
        //最长：采购审批时长 + 生产周期 + 供应商发货时长 + 质检入库时长 + 本地发FBA时效（最长） + FBA入库天数 + FBA安全天数 + 采购频率 + 发货频率
        //海外备货时长：
        //最短：本地发海外时效（最短）+ 海外仓入库天数
        //默认：采购审批时长 + 生产周期 + 供应商发货时长 + 质检入库时长 + 本地发海外时效（默认） + 海外仓入库天数
        //最长：采购审批时长 + 生产周期 + 供应商发货时长 + 质检入库时长 + 本地发海外时效（最长）  + 海外仓入库天数 + 海外仓安全天数 + 采购频率 + 发货频率
        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType()) || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
            replenishmentResultDTO.getReplenishmentDetail().setDeliveryMinDays(stockUpResult.getLogisticsMinResult().getLogisticsDays());
            replenishmentResultDTO.getReplenishmentDetail().setDeliveryDefaultDays(stockUpResult.getLogisticsResult().getLogisticsDays());
            replenishmentResultDTO.getReplenishmentDetail().setDeliveryMaxDays(stockUpResult.getLogisticsMaxResult().getLogisticsDays());
            replenishmentResultDTO.getReplenishmentDetail().setLogisticsCycleDays(stockUpResult.getLogisticsResult().getLogisticsCycleDays());
            replenishmentResultDTO.getReplenishmentDetail().setStockUpMinDays(stockUpResult.getLogisticsMinResult().getLogisticsDays() + stockUpResult.getInstockDays());
            replenishmentResultDTO.getReplenishmentDetail().setStockUpDefaultDays(stockUpResult.getPurchaseApproveDays() +
                    stockUpResult.getProductionDays() + stockUpResult.getSupplierDeliveryDays()
                    + stockUpResult.getQcDays() + stockUpResult.getLogisticsResult().getLogisticsDays() + stockUpResult.getInstockDays());
            replenishmentResultDTO.getReplenishmentDetail().setStockUpMaxDays(stockUpResult.getPurchaseApproveDays() +
                    stockUpResult.getProductionDays() + stockUpResult.getSupplierDeliveryDays()
                    + stockUpResult.getQcDays() + stockUpResult.getLogisticsMaxResult().getLogisticsDays() + stockUpResult.getPurchaseCycleDays()
                    + stockUpResult.getSafeDays() + stockUpResult.getLogisticsMaxResult().getLogisticsCycleDays()
            );
        }
        //本地备货时长：
        //最短：0
        //默认：采购审批时长 + 生产周期 + 供应商发货时长 + 质检入库时长
        //最长：采购审批时长 + 生产周期 + 供应商发货时长 + 质检入库时长 + 本地备货安全天数 + 采购频率
        if (CfgRulePlatformTypeEnum.B2B.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType()) || CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
            replenishmentResultDTO.getReplenishmentDetail().setDeliveryMinDays(0);
            replenishmentResultDTO.getReplenishmentDetail().setDeliveryDefaultDays(0);
            replenishmentResultDTO.getReplenishmentDetail().setDeliveryMaxDays(0);
            replenishmentResultDTO.getReplenishmentDetail().setLogisticsCycleDays(0);
            replenishmentResultDTO.getReplenishmentDetail().setStockUpMinDays(0);
            replenishmentResultDTO.getReplenishmentDetail().setStockUpDefaultDays(stockUpResult.getPurchaseApproveDays() +
                    stockUpResult.getProductionDays() + stockUpResult.getSupplierDeliveryDays()
                    + stockUpResult.getQcDays());
            replenishmentResultDTO.getReplenishmentDetail().setStockUpMaxDays(stockUpResult.getPurchaseApproveDays() +
                    stockUpResult.getProductionDays() + stockUpResult.getSupplierDeliveryDays()
                    + stockUpResult.getQcDays() + stockUpResult.getPurchaseCycleDays() + stockUpResult.getSafeDays()
            );
        }
    }
}
