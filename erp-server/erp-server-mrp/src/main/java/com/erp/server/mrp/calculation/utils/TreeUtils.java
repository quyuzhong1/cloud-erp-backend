package com.erp.server.mrp.calculation.utils;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TreeUtils {

    private TreeUtils(){}

    // 缓存: code -> StrategyResultDTO
    private static final Map<String, CfgRuleCommonDTO.StrategyResultDTO> cache = new ConcurrentHashMap<>();

    public static void initCache(List<CfgRuleCommonDTO.StrategyResultDTO> rootList) {
        // 构建缓存
        for (CfgRuleCommonDTO.StrategyResultDTO root : rootList) {
            buildCache(root);
        }
    }


    public static CfgRuleCommonDTO.StrategyResultDTO findByCode(List<CfgRuleCommonDTO.StrategyResultDTO> rootList, String code){
        // 优先通过缓存查找
        CfgRuleCommonDTO.StrategyResultDTO result = findByCodeWithCache(code);
        if (result != null) {
            return result;
        }
        // 使用迭代法查找
        return findByCodeIteratively(rootList, code);
    }

    // 缓存构建（如果结构不会频繁变化，可以预构建缓存）
    private static void buildCache(CfgRuleCommonDTO.StrategyResultDTO node) {
        cache.put(node.getCode(), node);
        if (node.getChildrenList() != null) {
            for (CfgRuleCommonDTO.StrategyResultDTO child : node.getChildrenList()) {
                buildCache(child);  // 递归缓存子节点
            }
        }
    }

    // 优化的查找方法，先尝试从缓存中获取
    private static CfgRuleCommonDTO.StrategyResultDTO findByCodeWithCache(String code) {
        return cache.getOrDefault(code, null);  // 直接从缓存获取
    }

    // 基于迭代法的查找，避免递归栈溢出问题
    private static CfgRuleCommonDTO.StrategyResultDTO findByCodeIteratively(List<CfgRuleCommonDTO.StrategyResultDTO> rootList, String code) {
        Deque<CfgRuleCommonDTO.StrategyResultDTO> stack = new ArrayDeque<>();
        for (CfgRuleCommonDTO.StrategyResultDTO root : rootList) {
            stack.push(root);
        }

        while (!stack.isEmpty()) {
            CfgRuleCommonDTO.StrategyResultDTO currentNode = stack.pop();
            // 检查当前节点是否匹配
            if (currentNode.getCode().equals(code)) {
                return currentNode;
            }
            // 将子节点压入栈
            if (currentNode.getChildrenList() != null) {
                for (CfgRuleCommonDTO.StrategyResultDTO child : currentNode.getChildrenList()) {
                    stack.push(child);
                }
            }
        }
        return null;
    }

    public static CfgRuleCommonDTO.StrategyResultDTO findByCode(CfgRuleCommonDTO.StrategyResultDTO root, String code){
        // 优先通过缓存查找
        CfgRuleCommonDTO.StrategyResultDTO result = findByCodeWithCache(code);
        if (result != null) {
            return result;
        }
        // 使用迭代法查找
        return findByCodeIteratively(root, code);
    }

    private static CfgRuleCommonDTO.StrategyResultDTO findByCodeIteratively(CfgRuleCommonDTO.StrategyResultDTO root, String code) {
        Deque<CfgRuleCommonDTO.StrategyResultDTO> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            CfgRuleCommonDTO.StrategyResultDTO currentNode = stack.pop();
            // 检查当前节点是否匹配
            if (currentNode.getCode().equals(code)) {
                return currentNode;
            }
            // 将子节点压入栈
            if (currentNode.getChildrenList() != null) {
                for (CfgRuleCommonDTO.StrategyResultDTO child : currentNode.getChildrenList()) {
                    stack.push(child);
                }
            }
        }
        return null;
    }
}
