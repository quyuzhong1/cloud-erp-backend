package com.erp.server.tms.utils;

import com.erp.model.tms.dto.DeclarationGenerationDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeclarationGenerationService {

    private static final int MAX_ROWS_PER_DECLARATION = 48;
    private static final BigDecimal MAX_PRICE_TOLERANCE = new BigDecimal("10.00");

    /**
     * 核心服务入口：从头程发货单明细生成报关单
     *
     * @param inputs 头程发货单明细列表
     * @return 报关单集合
     */
    public List<DeclarationGenerationDTO.OutputDeclarationDTO> generateDeclarations(List<DeclarationGenerationDTO.InputDetailDTO> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return Collections.emptyList();
        }

        // Step 2: 国家维度绝对物理隔离
        Map<String, List<DeclarationGenerationDTO.InputDetailDTO>> byCountry = inputs.stream()
                .collect(Collectors.groupingBy(item -> item.getDestinationCountry() == null ? "" : item.getDestinationCountry()));

        List<DeclarationGenerationDTO.OutputDeclarationDTO> finalDeclarations = new ArrayList<>();

        for (Map.Entry<String, List<DeclarationGenerationDTO.InputDetailDTO>> countryEntry : byCountry.entrySet()) {
            String country = countryEntry.getKey();
            List<DeclarationGenerationDTO.InputDetailDTO> countryInputs = countryEntry.getValue();

            // 在单一国家维度内处理
            List<DeclarationGenerationDTO.OutputDeclarationDTO> countryDeclarations = processCountry(country, countryInputs);
            finalDeclarations.addAll(countryDeclarations);
        }

        return finalDeclarations;
    }

    /**
     * 对外门面：基于发货明细生成合并报关结果。
     */
    public List<TmsDeclareBillDTO.MergeDeclareBillDTO> generateMergeBills(
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails,
            Boolean isMerge) {
        if (sourceDetails == null || sourceDetails.isEmpty()) {
            return Collections.emptyList();
        }

        IdentityHashMap<TmsDeclareBillDTO.SourceDeliveryDetailDTO, String> sourceKeyMap = buildSourceKeyMap(sourceDetails);
        List<DeclarationGenerationDTO.OutputDeclarationDTO> declarations = new ArrayList<>();
        if (Boolean.TRUE.equals(isMerge)) {
            declarations.addAll(generateDeclarations(toInputDetails(sourceDetails, sourceKeyMap)));
        } else {
            Map<String, List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>> bySource = sourceDetails.stream()
                    .collect(Collectors.groupingBy(this::resolveShipmentKey));
            for (List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceGroup : bySource.values()) {
                declarations.addAll(generateDeclarations(toInputDetails(sourceGroup, sourceKeyMap)));
            }
        }

        return toMergeDeclareBillDTOs(declarations, sourceDetails, sourceKeyMap);
    }

    public List<TmsDeclareBillDTO.MergeDeclareBillDTO> generateMergeBillDetails(
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails,
            Boolean isMerge) {
        return generateMergeBills(sourceDetails, isMerge);
    }

    private IdentityHashMap<TmsDeclareBillDTO.SourceDeliveryDetailDTO, String> buildSourceKeyMap(
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails) {
        IdentityHashMap<TmsDeclareBillDTO.SourceDeliveryDetailDTO, String> sourceKeyMap = new IdentityHashMap<>();
        for (int i = 0; i < sourceDetails.size(); i++) {
            TmsDeclareBillDTO.SourceDeliveryDetailDTO source = sourceDetails.get(i);
            String sourceDetailId = source.getSourceDetailId();
            if (sourceDetailId == null || sourceDetailId.trim().isEmpty()) {
                sourceDetailId = "ROW";
            }
            sourceKeyMap.put(source, sourceDetailId + "#" + i);
        }
        return sourceKeyMap;
    }

    private List<DeclarationGenerationDTO.InputDetailDTO> toInputDetails(
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails,
            Map<TmsDeclareBillDTO.SourceDeliveryDetailDTO, String> sourceKeyMap) {
        List<DeclarationGenerationDTO.InputDetailDTO> inputs = new ArrayList<>();
        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO source : sourceDetails) {
            DeclarationGenerationDTO.InputDetailDTO input = new DeclarationGenerationDTO.InputDetailDTO();
            input.setId(sourceKeyMap.get(source));
            input.setShipmentOrderId(resolveShipmentKey(source));
            input.setDestinationCountry(source.getCountryId());
            input.setHsCode(source.getHsCode());
            input.setCustomsName(source.getProductNameCn());
            input.setDeclarationElements(source.getDeclareElement());
            input.setDeclarationUnit(source.getUnit());
            input.setDeclarationCurrency(source.getDeclareCurrency());
            input.setModel(source.getSkuNo());
            input.setSku(source.getSkuNo());
            input.setPrice(source.getUnitPrice());
            input.setQuantity(source.getQty());
            inputs.add(input);
        }
        return inputs;
    }

    private List<TmsDeclareBillDTO.MergeDeclareBillDTO> toMergeDeclareBillDTOs(
            List<DeclarationGenerationDTO.OutputDeclarationDTO> declarations,
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails,
            Map<TmsDeclareBillDTO.SourceDeliveryDetailDTO, String> sourceKeyMap) {
        if (declarations == null || declarations.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailMap = sourceDetails.stream()
                .collect(Collectors.toMap(sourceKeyMap::get, d -> d, (a, b) -> a));

        List<TmsDeclareBillDTO.MergeDeclareBillDTO> result = new ArrayList<>();
        for (DeclarationGenerationDTO.OutputDeclarationDTO declaration : declarations) {
            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> declareBillList = new ArrayList<>();
            for (DeclarationGenerationDTO.OutputDeclarationDetailDTO detail : declaration.getDetails()) {
                List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> linkedSourceList = detail.getLinkedDetailIds().stream()
                        .map(sourceDetailMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (linkedSourceList.isEmpty()) {
                    continue;
                }

                TmsDeclareBillDTO.SourceDeliveryDetailDTO first = linkedSourceList.get(0);
                String businessDesc = linkedSourceList.stream()
                        .map(this::buildBusinessDesc)
                        .distinct()
                        .collect(Collectors.joining("、"));
                String businessOrderNos = linkedSourceList.stream()
                        .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getBusinessCode)
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining(","));
                String skuNo = detail.getSkus() == null ? "" : detail.getSkus().stream().sorted().collect(Collectors.joining(","));

                BigDecimal unitPriceVal = detail.getUnitPrice() != null ? detail.getUnitPrice() : BigDecimal.ZERO;
                int qtyVal = detail.getTotalQuantity() != null ? detail.getTotalQuantity() : 0;
                BigDecimal totalAmount = unitPriceVal.multiply(BigDecimal.valueOf(qtyVal)).setScale(4, RoundingMode.HALF_UP);

                String sourceCargoEff = StringUtils.isNotBlank(first.getSourceCargo())
                        ? first.getSourceCargo() : DeclareMergeDefaults.DEFAULT_SOURCE_CARGO;
                String exemptionEff = StringUtils.isNotBlank(first.getExemption())
                        ? first.getExemption() : DeclareMergeDefaults.DEFAULT_EXEMPTION;

                TmsDeclareBillDTO.MergeDeclareBillDetailDTO mergeDetail = TmsDeclareBillDTO.MergeDeclareBillDetailDTO.builder()
                        .businessDesc(businessDesc)
                        .businessOrderNos(businessOrderNos)
                        .leadSkuId(first.getSkuId())
                        .skuNo(skuNo)
                        .hsCode(first.getHsCode())
                        .productNameCn(first.getProductNameCn())
                        .declareElement(first.getDeclareElement())
                        .unit(first.getUnit())
                        .unitName(first.getUnitName())
                        .unitPrice(detail.getUnitPrice())
                        .qty(detail.getTotalQuantity())
                        .totalAmount(totalAmount)
                        .sourceCountry(first.getSourceCountry())
                        .sourceCountryName(first.getSourceCountryName())
                        .toCountry(first.getCountryId())
                        .toCountryName(first.getCountryName())
                        .sourceCargo(sourceCargoEff)
                        .exemption(exemptionEff)
                        .declareCurrency(first.getDeclareCurrency())
                        .declareCurrencyName(first.getDeclareCurrencyName())
                        .declareCurrencySymbol(first.getDeclareCurrencySymbol())
                        .mergeRemark(buildMergeRemark(linkedSourceList.size()))
                        .sourceDeliveryDetailList(linkedSourceList)
                        .build();
                declareBillList.add(mergeDetail);
            }
            result.add(TmsDeclareBillDTO.MergeDeclareBillDTO.builder().declareBillList(declareBillList).build());
        }
        return result;
    }

    private String resolveShipmentKey(TmsDeclareBillDTO.SourceDeliveryDetailDTO source) {
        if (source.getSourceId() != null && !source.getSourceId().trim().isEmpty()) {
            return source.getSourceId();
        }
        if (source.getBusinessId() != null && !source.getBusinessId().trim().isEmpty()) {
            return source.getBusinessId();
        }
        if (source.getSourceCode() != null && !source.getSourceCode().trim().isEmpty()) {
            return source.getSourceCode();
        }
        return "UNKNOWN_SHIPMENT";
    }

    private String buildBusinessDesc(TmsDeclareBillDTO.SourceDeliveryDetailDTO source) {
        String businessCode = source.getBusinessCode() == null ? "" : source.getBusinessCode();
        String boxNo = source.getBoxNo() == null ? "" : source.getBoxNo();
        if (businessCode.isEmpty()) {
            return boxNo;
        }
        if (boxNo.isEmpty()) {
            return businessCode;
        }
        return businessCode + "+" + boxNo;
    }

    private String buildMergeRemark(int sourceCount) {
        if (sourceCount > 1) {
            return "按报关要素分组并校验单价极差<=10后合并";
        }
        return "未触发合并，保持原单明细";
    }

    /**
     * 处理单一国家维度下的数据
     */
    private List<DeclarationGenerationDTO.OutputDeclarationDTO> processCountry(String country, List<DeclarationGenerationDTO.InputDetailDTO> countryInputs) {
        // Step 3: 发货单内聚合与价格极差切割
        Map<String, List<DeclarationGenerationDTO.InputDetailDTO>> byShipment = countryInputs.stream()
                .filter(item -> item.getShipmentOrderId() != null)
                .collect(Collectors.groupingBy(DeclarationGenerationDTO.InputDetailDTO::getShipmentOrderId));

        List<DeclarationGenerationDTO.ShipmentBlock> shipmentBlocks = new ArrayList<>();

        for (Map.Entry<String, List<DeclarationGenerationDTO.InputDetailDTO>> shipmentEntry : byShipment.entrySet()) {
            String shipmentId = shipmentEntry.getKey();
            List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> mergedDetails = processShipment(shipmentEntry.getValue());
            if (!mergedDetails.isEmpty()) {
                shipmentBlocks.add(new DeclarationGenerationDTO.ShipmentBlock(shipmentId, mergedDetails));
            }
        }

        // Step 4: 装箱与拆分决策树 (Bin Packing)
        return performBinPacking(country, shipmentBlocks);
    }

    /**
     * 处理单一发货单内的合并与拆分逻辑
     */
    private List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> processShipment(List<DeclarationGenerationDTO.InputDetailDTO> shipmentInputs) {
        // 1. 按合并键进行初次分组
        Map<DeclarationGenerationDTO.MergeKey, List<DeclarationGenerationDTO.InputDetailDTO>> groupedByKey = shipmentInputs.stream()
                .collect(Collectors.groupingBy(DeclarationGenerationDTO.MergeKey::from));

        List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> result = new ArrayList<>();

        // 2. 极差切割与聚合赋值
        for (List<DeclarationGenerationDTO.InputDetailDTO> groupItems : groupedByKey.values()) {
            // 按单价升序排序以应用滑动窗口
            groupItems.sort(Comparator.comparing(
                    item -> item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO
            ));

            List<DeclarationGenerationDTO.InputDetailDTO> currentWindow = new ArrayList<>();
            BigDecimal windowMinPrice = null;

            for (DeclarationGenerationDTO.InputDetailDTO item : groupItems) {
                BigDecimal itemPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;

                if (currentWindow.isEmpty()) {
                    windowMinPrice = itemPrice;
                    currentWindow.add(item);
                } else {
                    // 判断是否超出极差容忍度 (max - min <= 10.00)
                    if (itemPrice.subtract(windowMinPrice).compareTo(MAX_PRICE_TOLERANCE) > 0) {
                        // 超出极差，结算当前窗口
                        result.add(aggregateGroup(currentWindow));
                        // 开启新窗口
                        currentWindow.clear();
                        windowMinPrice = itemPrice;
                        currentWindow.add(item);
                    } else {
                        // 未超出极差，加入当前窗口
                        currentWindow.add(item);
                    }
                }
            }
            // 结算最后遗留的窗口
            if (!currentWindow.isEmpty()) {
                result.add(aggregateGroup(currentWindow));
            }
        }

        return result;
    }

    /**
     * 将符合规则的一组明细聚合成一个报关单明细行
     */
    private DeclarationGenerationDTO.OutputDeclarationDetailDTO aggregateGroup(List<DeclarationGenerationDTO.InputDetailDTO> groupItems) {
        DeclarationGenerationDTO.OutputDeclarationDetailDTO detail = new DeclarationGenerationDTO.OutputDeclarationDetailDTO();

        // 提取最高单价
        BigDecimal maxPrice = groupItems.stream()
                .map(item -> item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        detail.setUnitPrice(maxPrice);

        // 累加数量
        int totalQty = groupItems.stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
        detail.setTotalQuantity(totalQty);

        // 聚合去重的SKU编号
        Set<String> skus = groupItems.stream()
                .map(DeclarationGenerationDTO.InputDetailDTO::getSku)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        detail.setSkus(skus);

        // 关联原发货单明细ID
        List<String> linkedIds = groupItems.stream()
                .map(DeclarationGenerationDTO.InputDetailDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        detail.setLinkedDetailIds(linkedIds);

        return detail;
    }

    /**
     * FFD 装箱算法与超限拆分逻辑
     */
    private List<DeclarationGenerationDTO.OutputDeclarationDTO> performBinPacking(String country, List<DeclarationGenerationDTO.ShipmentBlock> blocks) {
        List<DeclarationGenerationDTO.OutputDeclarationDTO> finalDeclarations = new ArrayList<>();

        // 区分超限逻辑块与可打包逻辑块
        List<DeclarationGenerationDTO.ShipmentBlock> oversizedBlocks = blocks.stream()
                .filter(b -> b.getSize() > MAX_ROWS_PER_DECLARATION)
                .collect(Collectors.toList());

        List<DeclarationGenerationDTO.ShipmentBlock> packableBlocks = blocks.stream()
                .filter(b -> b.getSize() <= MAX_ROWS_PER_DECLARATION)
                .sorted((b1, b2) -> Integer.compare(b2.getSize(), b1.getSize())) // FFD: 按体积从大到小排序
                .collect(Collectors.toList());

        // 1. 处理超限逻辑块 (硬截断，不可合并锁定状态)
        for (DeclarationGenerationDTO.ShipmentBlock block : oversizedBlocks) {
            List<List<DeclarationGenerationDTO.OutputDeclarationDetailDTO>> partitions = partitionList(block.getDetails(), MAX_ROWS_PER_DECLARATION);
            for (List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> partition : partitions) {
                DeclarationGenerationDTO.OutputDeclarationDTO decl = new DeclarationGenerationDTO.OutputDeclarationDTO(country);
                decl.setLocked(true); // 锁定状态，严禁追加
                decl.getDetails().addAll(partition);
                finalDeclarations.add(decl);
            }
        }

        // 2. 处理可打包逻辑块 (贪婪紧凑打包)
        for (DeclarationGenerationDTO.ShipmentBlock block : packableBlocks) {
            DeclarationGenerationDTO.OutputDeclarationDTO targetDeclaration = null;

            // 寻找当前未锁定且容量足够容纳当前块的报关单
            for (DeclarationGenerationDTO.OutputDeclarationDTO decl : finalDeclarations) {
                if (!decl.isLocked() && (MAX_ROWS_PER_DECLARATION - decl.getDetails().size()) >= block.getSize()) {
                    targetDeclaration = decl;
                    break; // First-Fit
                }
            }

            // 若无可用，则新建
            if (targetDeclaration == null) {
                targetDeclaration = new DeclarationGenerationDTO.OutputDeclarationDTO(country);
                targetDeclaration.setLocked(false);
                finalDeclarations.add(targetDeclaration);
            }

            // 发货单整体装入
            targetDeclaration.getDetails().addAll(block.getDetails());
        }

        return finalDeclarations;
    }

    /**
     * 辅助方法：将 List 按指定大小分块
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        return IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> list.subList(i * batchSize, Math.min((i + 1) * batchSize, list.size())))
                .collect(Collectors.toList());
    }
}