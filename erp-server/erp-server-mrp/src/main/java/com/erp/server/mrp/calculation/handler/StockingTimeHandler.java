package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleExpireTimeDTO;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class StockingTimeHandler extends AbstractSkuCalculationHandler {
    @Resource
    private FbaUsableHandler fbaUsableHandler;

    @Override
    public SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r) {
        return fbaUsableHandler;
    }

    @Override
    public boolean shouldHandle(ReplenishmentResultDTO r) {
        return true;
    }

    @Override
    public void doHandle(ReplenishmentResultDTO replenishmentResultDTO, List<ReplenishmentResultDTO> r) {

        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = replenishmentResultDTO.getCfgRuleStrategy().getStockUpResult();
        CfgRuleExpireTimeDTO.StrategyResultDTO expireTimeResult = replenishmentResultDTO.getCfgRuleStrategy().getExpireTimeResult();
        buildBasicStockingTime(replenishmentResultDTO, stockUpResult, expireTimeResult);
        //FBA备货时长：
        //最短：本地发FBA时效（最短）+ FBA入库天数
        //默认：采购审批时长 + 生产周期 + 供应商发货时长 + 质检入库时长 + 本地发FBA时效（默认） + FBA入库天数
        //最长：采购审批时长 + 生产周期 + 供应商发货时长 + 质检入库时长 + 本地发FBA时效（最长） + FBA入库天数 + FBA安全天数 + 采购频率 + 发货频率
        //海外备货时长：
        //最短：本地发海外时效（最短）+ 海外仓入库天数
        //默认：采购审批时长 + 生产周期 + 供应商发货时长 + 质检入库时长 + 本地发海外时效（默认） + 海外仓入库天数
        //最长：采购审批时长 + 生产周期 + 供应商发货时长 + 质检入库时长 + 本地发海外时效（最长）  + 海外仓入库天数 + 海外仓安全天数 + 采购频率 + 发货频率
        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType()) || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
            replenishmentResultDTO.getReplenishmentDetail().setDeliveryMinDays(expireTimeResult.getLogisticsMinResult().getLogisticsDays());
            replenishmentResultDTO.getReplenishmentDetail().setDeliveryDefaultDays(expireTimeResult.getLogisticsResult().getLogisticsDays());
            replenishmentResultDTO.getReplenishmentDetail().setDeliveryMaxDays(expireTimeResult.getLogisticsMaxResult().getLogisticsDays());
            replenishmentResultDTO.getReplenishmentDetail().setLogisticsCycleDays(expireTimeResult.getLogisticsResult().getLogisticsCycleDays());
            replenishmentResultDTO.getReplenishmentDetail().setStockUpMinDays(expireTimeResult.getLogisticsMinResult().getLogisticsDays() + expireTimeResult.getInstockDays());
            replenishmentResultDTO.getReplenishmentDetail().setStockUpDefaultDays(expireTimeResult.getPurchaseApproveDays() +
                    expireTimeResult.getProductionDays() + expireTimeResult.getSupplierDeliveryDays()
                    + expireTimeResult.getQcDays() + expireTimeResult.getLogisticsResult().getLogisticsDays() + expireTimeResult.getInstockDays());
            replenishmentResultDTO.getReplenishmentDetail().setStockUpMaxDays(expireTimeResult.getPurchaseApproveDays() +
                    expireTimeResult.getProductionDays() + expireTimeResult.getSupplierDeliveryDays()
                    + expireTimeResult.getQcDays() + expireTimeResult.getLogisticsMaxResult().getLogisticsDays() + expireTimeResult.getInstockDays() + expireTimeResult.getPurchaseCycleDays()
                    + stockUpResult.getSafeDays() + expireTimeResult.getLogisticsMaxResult().getLogisticsCycleDays()
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
            replenishmentResultDTO.getReplenishmentDetail().setStockUpDefaultDays(expireTimeResult.getPurchaseApproveDays() +
                    expireTimeResult.getProductionDays() + expireTimeResult.getSupplierDeliveryDays()
                    + expireTimeResult.getQcDays());
            replenishmentResultDTO.getReplenishmentDetail().setStockUpMaxDays(expireTimeResult.getPurchaseApproveDays() +
                    expireTimeResult.getProductionDays() + expireTimeResult.getSupplierDeliveryDays()
                    + expireTimeResult.getQcDays() + expireTimeResult.getPurchaseCycleDays() + stockUpResult.getSafeDays()
            );
        }
    }

    /**
     * 构建基础补货数据
     *
     * @param replenishmentResultDTO 建议
     * @param stockUpResult          备货配置
     * @param expireTimeResult
     */
    private static void buildBasicStockingTime(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult, CfgRuleExpireTimeDTO.StrategyResultDTO expireTimeResult) {
        replenishmentResultDTO.getReplenishmentDetail().setPurchaseApproveDays(expireTimeResult.getPurchaseApproveDays());
        replenishmentResultDTO.getReplenishmentDetail().setProductionDays(expireTimeResult.getProductionDays());
        replenishmentResultDTO.getReplenishmentDetail().setSupplierDeliveryDays(expireTimeResult.getSupplierDeliveryDays());
        replenishmentResultDTO.getReplenishmentDetail().setQcDays(expireTimeResult.getQcDays());
        replenishmentResultDTO.getReplenishmentDetail().setPurchaseCycleDays(expireTimeResult.getPurchaseCycleDays());
        replenishmentResultDTO.getReplenishmentDetail().setSafeDays(stockUpResult.getSafeDays());
        replenishmentResultDTO.getReplenishmentDetail().setInstockDays(expireTimeResult.getInstockDays());
        replenishmentResultDTO.getReplenishmentDetail().setLogisticsMinCycleDays(expireTimeResult.getLogisticsMinResult().getLogisticsCycleDays());
        replenishmentResultDTO.getReplenishmentDetail().setLogisticsCycleDays(expireTimeResult.getLogisticsResult().getLogisticsCycleDays());
        replenishmentResultDTO.getReplenishmentDetail().setLogisticsMaxCycleDays(expireTimeResult.getLogisticsMaxResult().getLogisticsCycleDays());
        replenishmentResultDTO.getReplenishmentDetail().setLogisticsMinMethod(expireTimeResult.getLogisticsMinResult().getLogisticsMethod());
        replenishmentResultDTO.getReplenishmentDetail().setLogisticsMethod(expireTimeResult.getLogisticsResult().getLogisticsMethod());
        replenishmentResultDTO.getReplenishmentDetail().setLogisticsMaxMethod(expireTimeResult.getLogisticsMaxResult().getLogisticsMethod());
    }
}
