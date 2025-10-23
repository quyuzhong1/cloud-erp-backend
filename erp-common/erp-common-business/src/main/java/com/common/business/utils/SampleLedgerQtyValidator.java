package com.common.business.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ApproveTypeEnum;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 样品台账数量校验工具类
 * 用于校验样品台账数量是否足够扣减
 * 
 * @author system
 * @since 2025-01-20
 */
@Slf4j
@Component
public class SampleLedgerQtyValidator {

    /**
     * 校验样品台账数量
     * 只有-X的操作才需要校验数量是否足够扣减
     * +X的操作不需要校验
     * 
     * @param sampleLedgerIds 样品台账ID列表
     * @param qtys 对应的数量列表（正数表示增加，负数表示减少）
     * @param approveType 审核类型
     * @param skuNos SKU编号列表（用于错误提示）
     * @param ledgerService 台账服务（用于查询当前数量）
     */
    public void validateQty(List<String> sampleLedgerIds, List<Integer> qtys, 
                           ApproveTypeEnum approveType, List<String> skuNos,
                           SampleLedgerService ledgerService) {
        
        if (CollUtil.isEmpty(sampleLedgerIds) || CollUtil.isEmpty(qtys)) {
            log.warn("样品台账ID列表或数量列表为空，跳过校验");
            return;
        }

        if (sampleLedgerIds.size() != qtys.size()) {
            throw new ServiceException("样品台账ID列表与数量列表长度不匹配");
        }

        // 只有审核通过（-X）才需要校验数量是否足够扣减
        if (!ApproveTypeEnum.PASS.equals(approveType)) {
            log.info("审核类型为{}，不需要校验数量扣减", approveType.getName());
            return;
        }

        log.info("开始校验样品台账数量，台账数量：{}", sampleLedgerIds.size());

        // 批量查询台账当前数量
        Map<String, Integer> ledgerQtyMap = ledgerService.getLedgerQtyMap(sampleLedgerIds);

        // 校验每个台账的数量
        for (int i = 0; i < sampleLedgerIds.size(); i++) {
            String ledgerId = sampleLedgerIds.get(i);
            Integer qty = qtys.get(i);
            String skuNo = CollUtil.isNotEmpty(skuNos) && i < skuNos.size() ? skuNos.get(i) : "未知SKU";

            if (StrUtil.isBlank(ledgerId) || qty == null) {
                continue;
            }

            // 只有负数（扣减）才需要校验
            if (qty >= 0) {
                log.debug("SKU【{}】数量为{}，不需要校验扣减", skuNo, qty);
                continue;
            }

            // 获取当前台账数量
            Integer currentQty = ledgerQtyMap.getOrDefault(ledgerId, 0);
            int deductQty = Math.abs(qty); // 转换为正数进行校验

            if (deductQty > currentQty) {
                String errorMsg = StrUtil.format("SKU【{}】扣减数量【{}】不能大于台账当前数量【{}】", 
                    skuNo, deductQty, currentQty);
                log.error(errorMsg);
                throw new ServiceException(errorMsg);
            }

            log.debug("SKU【{}】数量校验通过，当前数量：{}，扣减数量：{}", skuNo, currentQty, deductQty);
        }

        log.info("样品台账数量校验完成");
    }

    /**
     * 校验样品台账数量（单个台账）
     * 
     * @param sampleLedgerId 样品台账ID
     * @param qty 数量（正数表示增加，负数表示减少）
     * @param approveType 审核类型
     * @param skuNo SKU编号（用于错误提示）
     * @param ledgerService 台账服务
     */
    public void validateQty(String sampleLedgerId, Integer qty, ApproveTypeEnum approveType, 
                          String skuNo, SampleLedgerService ledgerService) {
        if (StrUtil.isBlank(sampleLedgerId) || qty == null) {
            return;
        }
        
        validateQty(java.util.Collections.singletonList(sampleLedgerId), 
                  java.util.Collections.singletonList(qty), 
                  approveType, 
                  java.util.Collections.singletonList(skuNo), 
                  ledgerService);
    }

    /**
     * 样品台账服务接口
     * 用于查询台账当前数量
     */
    public interface SampleLedgerService {
        /**
         * 批量查询台账当前数量
         * 
         * @param sampleLedgerIds 样品台账ID列表
         * @return 台账ID到当前数量的映射
         */
        Map<String, Integer> getLedgerQtyMap(List<String> sampleLedgerIds);
    }
}
