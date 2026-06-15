package com.erp.server.tms.service.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.erp.model.plm.dto.ProductPackDTO;
import com.erp.model.plm.enums.BomStateEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.rpc.plm.feign.ProductPackFeign;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物流费用导入重量预处理：复用 PLM 组合品/单品单位毛重，按出库单汇总订单重量。
 */
@Component
public class LogisticsOrderWeightSupport {

    @Resource
    private ProductPackFeign productPackFeign;

    public Map<String, BigDecimal> resolveSkuUnitGrossWeightMap(List<String> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        List<String> distinctSkuIds = skuIds.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(distinctSkuIds)) {
            return Collections.emptyMap();
        }
        //不考虑bom的审核状态
        Map<String, BigDecimal> weightMap = productPackFeign.listSingleBySkuIds(
                new ProductPackDTO.ListSingleBySkuIdsParam(distinctSkuIds, null));
        return ObjectUtil.defaultIfNull(weightMap, Collections.emptyMap());
    }

    public void fillOrderWeightByOutstockDetails(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList,
                                                 Map<String, List<SoOutstockDetailEntity>> outstockDetailMap,
                                                 Map<String, BigDecimal> orderWeightMap,
                                                 Map<String, List<String>> orderWeightErrorMap) {
        if (CollUtil.isEmpty(logisticsBillVoList)) {
            return;
        }
        Map<String, List<SoOutstockDetailEntity>> safeOutstockDetailMap =
                ObjectUtil.defaultIfNull(outstockDetailMap, Collections.emptyMap());
        List<String> allSkuIds = safeOutstockDetailMap.values().stream()
                .flatMap(list -> list == null ? java.util.stream.Stream.empty() : list.stream())
                .map(SoOutstockDetailEntity::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, BigDecimal> skuUnitWeightMap = resolveSkuUnitGrossWeightMap(allSkuIds);

        for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVoList) {
            if (CharSequenceUtil.isBlank(logisticsBillVo.getOutstockId()) || CharSequenceUtil.isBlank(logisticsBillVo.getDetailId())) {
                continue;
            }
            List<SoOutstockDetailEntity> detailList = safeOutstockDetailMap.get(logisticsBillVo.getOutstockId());
            if (CollUtil.isEmpty(detailList)) {
                addOrderWeightError(orderWeightErrorMap, logisticsBillVo.getDetailId(),
                        "无法获取上游出库明细用于重量分摊：" + logisticsBillVo.getOutstockCode());
                continue;
            }
            Set<String> problemSkuNos = new LinkedHashSet<>();
            for (SoOutstockDetailEntity detailEntity : detailList) {
                if (ObjectUtil.isNull(detailEntity.getActualQty())) {
                    problemSkuNos.add(CharSequenceUtil.blankToDefault(detailEntity.getSkuNo(), detailEntity.getSkuId()) + "(实发数量为空)");
                    continue;
                }
                BigDecimal unitWeight = skuUnitWeightMap.get(detailEntity.getSkuId());
                if (ObjectUtil.isNull(unitWeight) || unitWeight.compareTo(BigDecimal.ZERO) <= 0) {
                    problemSkuNos.add(CharSequenceUtil.blankToDefault(detailEntity.getSkuNo(), detailEntity.getSkuId()));
                }
            }
            if (CollUtil.isNotEmpty(problemSkuNos)) {
                addOrderWeightError(orderWeightErrorMap, logisticsBillVo.getDetailId(),
                        "出库单" + logisticsBillVo.getOutstockCode() + "存在SKU无法获取重量用于分摊："
                                + String.join("、", problemSkuNos));
                continue;
            }
            BigDecimal orderWeight = BigDecimal.ZERO;
            for (SoOutstockDetailEntity detailEntity : detailList) {
                BigDecimal unitWeight = skuUnitWeightMap.get(detailEntity.getSkuId());
                orderWeight = orderWeight.add(unitWeight.multiply(BigDecimal.valueOf(detailEntity.getActualQty())));
            }
            if (orderWeight.compareTo(BigDecimal.ZERO) <= 0) {
                addOrderWeightError(orderWeightErrorMap, logisticsBillVo.getDetailId(),
                        "订单重量为0，无法执行费用分摊：" + logisticsBillVo.getOutstockCode());
                continue;
            }
            orderWeightMap.put(logisticsBillVo.getDetailId(), orderWeight);
        }
    }

    public Map<String, BigDecimal> buildOrderWeightMap(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList,
                                                       Map<String, List<SoOutstockDetailEntity>> outstockDetailMap,
                                                       List<String> errorMsgList) {
        Map<String, BigDecimal> weightMap = new HashMap<>();
        if (CollUtil.isEmpty(logisticsBillVoList)) {
            return weightMap;
        }
        if (logisticsBillVoList.stream().anyMatch(vo -> CharSequenceUtil.isBlank(vo.getOutstockId()))) {
            errorMsgList.add("无法获取上游出库单用于重量分摊");
            return weightMap;
        }
        if (CollUtil.isEmpty(outstockDetailMap)) {
            errorMsgList.add("无法获取上游出库明细用于重量分摊");
            return weightMap;
        }
        Map<String, BigDecimal> orderWeightMap = new HashMap<>();
        Map<String, List<String>> orderWeightErrorMap = new HashMap<>();
        fillOrderWeightByOutstockDetails(logisticsBillVoList, outstockDetailMap, orderWeightMap, orderWeightErrorMap);

        int errorCountBefore = errorMsgList.size();
        for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVoList) {
            BigDecimal orderWeight = orderWeightMap.get(logisticsBillVo.getDetailId());
            if (ObjectUtil.isNotNull(orderWeight)) {
                weightMap.put(logisticsBillVo.getDetailId(), orderWeight);
                continue;
            }
            List<String> preQueryErrorList = orderWeightErrorMap.get(logisticsBillVo.getDetailId());
            if (CollUtil.isNotEmpty(preQueryErrorList)) {
                errorMsgList.addAll(preQueryErrorList);
            } else {
                errorMsgList.add("无法获取订单重量用于费用分摊：" + logisticsBillVo.getOutstockCode());
            }
        }
        if (logisticsBillVoList.size() > 1 && weightMap.size() != logisticsBillVoList.size()
                && errorMsgList.size() == errorCountBefore) {
            errorMsgList.add("同一识别分组存在物流单无法获取订单重量，无法合并分摊费用");
        }
        if (CollUtil.isEmpty(errorMsgList)) {
            BigDecimal totalWeight = weightMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
                errorMsgList.add("总订单重量为0，无法执行费用分摊");
            }
        }
        return weightMap;
    }

    /**
     * 按物流单明细聚合重量分摊错误，供导入预查询与重量 Support 共用。
     */
    public static void addOrderWeightError(Map<String, List<String>> orderWeightErrorMap, String detailId, String errorMsg) {
        if (CharSequenceUtil.isBlank(detailId) || CharSequenceUtil.isBlank(errorMsg)) {
            return;
        }
        orderWeightErrorMap.computeIfAbsent(detailId, key -> new ArrayList<>()).add(errorMsg);
    }
}
