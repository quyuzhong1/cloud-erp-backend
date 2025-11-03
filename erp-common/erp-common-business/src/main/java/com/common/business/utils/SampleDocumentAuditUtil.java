package com.common.business.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ApproveTypeEnum;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 样品单据审核工具类
 * 提供通用的样品单据审核和反审核时的分布式锁和数量校验功能
 * 
 * @author system
 * @since 2025-01-20
 */
@Slf4j
@Component
public class SampleDocumentAuditUtil {

    @Autowired
    private SampleLedgerLockUtil sampleLedgerLockUtil;

    @Autowired
    private SampleLedgerQtyValidator sampleLedgerQtyValidator;

    /**
     * 样品单据明细接口
     * 所有样品单据明细实体类需要实现此接口
     */
    public interface SampleDocumentDetail {
        /**
         * 获取样品台账ID
         * @return 样品台账ID
         */
        String getSampleLedgerId();
        
        /**
         * 获取数量（每个单据类型可能字段名不同，由实体类自己实现）
         * @return 数量
         */
        Integer getQty();
        
        /**
         * 获取SKU编号
         * @return SKU编号
         */
        String getSkuNo();
    }

    /**
     * 样品单据审核时的数量校验
     * 根据图片中的规则：
     * - 样品领用单：审核+X，反审核-X
     * - 样品报废单：审核-X，反审核+X  
     * - 样品借用单：借入人审核+X，反审核-X；借出人审核-X，反审核+X
     * - 样品归还单：归还人审核-X，反审核+X；接收人审核+X，反审核-X
     * - 样品期初台账单：审核+X（若导入为负数则为-X），反审核-X（若导入为负数则为+X）
     * - 样品退回单：审核-X，反审核+X
     * - 样品展会订单：审核-X，反审核+X
     * 
     * @param documentCode 单据编号
     * @param documentType 单据类型
     * @param detailList 明细列表（实现了SampleDocumentDetail接口）
     * @param approveType 审核类型
     * @param ledgerService 台账服务
     */
    public void validateSampleDocumentQty(String documentCode, String documentType, 
                                        List<? extends SampleDocumentDetail> detailList,
                                        ApproveTypeEnum approveType,
                                        SampleLedgerQtyValidator.SampleLedgerService ledgerService) {
        
        if (CollUtil.isEmpty(detailList)) {
            log.info("{}明细为空，跳过数量校验，单据编号：{}", documentType, documentCode);
            return;
        }

        // 收集需要校验的台账ID和数量
        List<String> sampleLedgerIds = new ArrayList<>();
        List<Integer> qtys = new ArrayList<>();
        List<String> skuNos = new ArrayList<>();
        
        for (SampleDocumentDetail detail : detailList) {
            String sampleLedgerId = detail.getSampleLedgerId();
            Integer qty = detail.getQty();
            String skuNo = detail.getSkuNo();
            
            if (StrUtil.isNotBlank(sampleLedgerId) && qty != null) {
                sampleLedgerIds.add(sampleLedgerId);
                // 根据单据类型和审核类型计算数量
                Integer calculatedQty = calculateQtyByDocumentType(qty, documentType, approveType);
                qtys.add(calculatedQty);
                skuNos.add(skuNo != null ? skuNo : "未知SKU");
            }
        }

        if (CollUtil.isEmpty(sampleLedgerIds)) {
            log.info("没有需要校验的样品台账，跳过数量校验，单据编号：{}", documentCode);
            return;
        }

        log.info("开始使用分布式锁校验{}数量，单据编号：{}，台账数量：{}，审核类型：{}", 
                documentType, documentCode, sampleLedgerIds.size(), approveType.getName());

        // 使用分布式锁进行数量校验
        sampleLedgerLockUtil.executeWithLock(sampleLedgerIds, () -> {
            // 执行数量校验
            sampleLedgerQtyValidator.validateQty(sampleLedgerIds, qtys, approveType, skuNos, ledgerService);
            
            log.info("{}数量校验通过，单据编号：{}，审核类型：{}", documentType, documentCode, approveType.getName());
            return null;
        });
    }

    /**
     * 根据单据类型和审核类型计算数量
     * 
     * @param originalQty 原始数量
     * @param documentType 单据类型
     * @param approveType 审核类型
     * @return 计算后的数量
     */
    private Integer calculateQtyByDocumentType(Integer originalQty, String documentType, ApproveTypeEnum approveType) {
        if (originalQty == null) {
            return 0;
        }

        // 根据单据类型和审核类型计算数量
        switch (documentType) {
            case "样品领用单":
                // 审核+X，反审核-X
                return ApproveTypeEnum.PASS.equals(approveType) ? originalQty : -originalQty;
                
            case "样品报废单":
                // 审核-X，反审核+X
                return ApproveTypeEnum.PASS.equals(approveType) ? -originalQty : originalQty;
                
            case "样品借用单":
                // 借入人：审核+X，反审核-X；借出人：审核-X，反审核+X
                // 这里需要根据具体业务逻辑判断是借入人还是借出人
                // 暂时按借入人处理，实际使用时需要传入更多参数
                return ApproveTypeEnum.PASS.equals(approveType) ? originalQty : -originalQty;
                
            case "样品归还单":
                // 归还人：审核-X，反审核+X；接收人：审核+X，反审核-X
                // 这里需要根据具体业务逻辑判断是归还人还是接收人
                // 暂时按归还人处理，实际使用时需要传入更多参数
                return ApproveTypeEnum.PASS.equals(approveType) ? -originalQty : originalQty;
                
            case "样品期初台账单":
                // 审核+X（若导入为负数则为-X），反审核-X（若导入为负数则为+X）
                // 这里需要根据原始数据判断是否为负数
                // 暂时按正数处理，实际使用时需要传入更多参数
                return ApproveTypeEnum.PASS.equals(approveType) ? originalQty : -originalQty;
                
            case "样品退回单":
                // 审核-X，反审核+X
                return ApproveTypeEnum.PASS.equals(approveType) ? -originalQty : originalQty;
                
            case "样品展会订单":
                // 审核-X，反审核+X
                return ApproveTypeEnum.PASS.equals(approveType) ? -originalQty : originalQty;
                
            default:
                log.warn("未知的单据类型：{}，使用默认计算方式", documentType);
                return ApproveTypeEnum.PASS.equals(approveType) ? -originalQty : originalQty;
        }
    }
}
