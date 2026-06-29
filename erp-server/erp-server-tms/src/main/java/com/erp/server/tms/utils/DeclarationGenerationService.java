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
     * 申报币种 -> 折算人民币汇率（1 单位币种 = rate 人民币）。
     * 单价极差需折算成人民币后再与 {@link #MAX_PRICE_TOLERANCE}（10 RMB）比较，
     * 否则美金等外币按原币值比较会把人民币价差远超 10 的明细误并到一行。
     */
    private final Map<String, BigDecimal> currencyToRmbRateMap;

    public DeclarationGenerationService() {
        this(Collections.emptyMap());
    }

    public DeclarationGenerationService(Map<String, BigDecimal> currencyToRmbRateMap) {
        this.currencyToRmbRateMap = currencyToRmbRateMap == null ? Collections.emptyMap() : currencyToRmbRateMap;
    }

    /**
     * 取币种折算人民币汇率：无汇率信息（含人民币本币）按 1:1 处理，退化为原币种极差比较。
     */
    private BigDecimal resolveCurrencyRate(String currency) {
        if (StringUtils.isBlank(currency)) {
            return BigDecimal.ONE;
        }
        BigDecimal rate = currencyToRmbRateMap.get(currency);
        return (rate == null || rate.signum() <= 0) ? BigDecimal.ONE : rate;
    }

    /**
     * 按默认五维度规则生成报关单算法结果
     * @author will
     * @date 2026/5/9 15:00
     * @param inputs 报关合并算法输入明细
     * @return java.util.List<com.erp.model.tms.dto.DeclarationGenerationDTO.OutputDeclarationDTO>
     */
    public List<DeclarationGenerationDTO.OutputDeclarationDTO> generateDeclarations(List<DeclarationGenerationDTO.InputDetailDTO> inputs) {
        return generateDeclarations(inputs, false);
    }

    /**
     * 按指定合并维度生成报关单算法结果
     * @author will
     * @date 2026/5/9 15:00
     * @param inputs 报关合并算法输入明细
     * @param includeSkuInMergeKey 是否将 SKU 纳入合并维度
     * @return java.util.List<com.erp.model.tms.dto.DeclarationGenerationDTO.OutputDeclarationDTO>
     */
    public List<DeclarationGenerationDTO.OutputDeclarationDTO> generateDeclarations(List<DeclarationGenerationDTO.InputDetailDTO> inputs,
                                                                                   boolean includeSkuInMergeKey) {
        return generateDeclarations(inputs, includeSkuInMergeKey, false);
    }

    /**
     * 按指定合并维度生成报关单算法结果。
     *
     * @param inputs 报关合并算法输入明细
     * @param includeSkuInMergeKey 是否将 SKU 纳入合并维度
     * @param mergeAcrossShipments 是否允许跨来源单据合并报关明细行
     * @return java.util.List<com.erp.model.tms.dto.DeclarationGenerationDTO.OutputDeclarationDTO>
     */
    public List<DeclarationGenerationDTO.OutputDeclarationDTO> generateDeclarations(List<DeclarationGenerationDTO.InputDetailDTO> inputs,
                                                                                   boolean includeSkuInMergeKey,
                                                                                   boolean mergeAcrossShipments) {
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
            List<DeclarationGenerationDTO.OutputDeclarationDTO> countryDeclarations = processCountry(country, countryInputs, includeSkuInMergeKey, mergeAcrossShipments);
            finalDeclarations.addAll(countryDeclarations);
        }

        return finalDeclarations;
    }

    /**
     * 按默认五维度规则生成合并报关结果
     * @author will
     * @date 2026/5/9 15:00
     * @param sourceDetails 来源发货明细
     * @param isMerge 是否合并报关
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDTO>
     */
    public List<TmsDeclareBillDTO.MergeDeclareBillDTO> generateMergeBills(
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails,
            Boolean isMerge) {
        return generateMergeBills(sourceDetails, isMerge, false);
    }

    /**
     * 按指定合并维度生成合并报关结果
     * @author will
     * @date 2026/5/9 15:00
     * @param sourceDetails 来源发货明细
     * @param isMerge 是否合并报关
     * @param includeSkuInMergeKey 是否将 SKU 纳入合并维度
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDTO>
     */
    public List<TmsDeclareBillDTO.MergeDeclareBillDTO> generateMergeBills(
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails,
            Boolean isMerge,
            boolean includeSkuInMergeKey) {
        if (sourceDetails == null || sourceDetails.isEmpty()) {
            return Collections.emptyList();
        }

        IdentityHashMap<TmsDeclareBillDTO.SourceDeliveryDetailDTO, String> sourceKeyMap = buildSourceKeyMap(sourceDetails);
        List<DeclarationGenerationDTO.OutputDeclarationDTO> declarations = new ArrayList<>();
        if (Boolean.TRUE.equals(isMerge)) {
            // 合并报关时允许不同来源单据按报关维度合并成同一行。
            declarations.addAll(generateDeclarations(toInputDetails(sourceDetails, sourceKeyMap), includeSkuInMergeKey, true));
        } else {
            Map<String, List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>> bySource = sourceDetails.stream()
                    .collect(Collectors.groupingBy(this::resolveShipmentKey));
            for (List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceGroup : bySource.values()) {
                declarations.addAll(generateDeclarations(toInputDetails(sourceGroup, sourceKeyMap), includeSkuInMergeKey));
            }
        }

        return toMergeDeclareBillDTOs(declarations, sourceDetails, sourceKeyMap);
    }

    /**
     * 按默认五维度规则生成合并报关明细视图
     * @author will
     * @date 2026/5/9 15:00
     * @param sourceDetails 来源发货明细
     * @param isMerge 是否合并报关
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDTO>
     */
    public List<TmsDeclareBillDTO.MergeDeclareBillDTO> generateMergeBillDetails(
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails,
            Boolean isMerge) {
        return generateMergeBills(sourceDetails, isMerge);
    }

    /**
     * 按指定合并维度生成合并报关明细视图
     * @author will
     * @date 2026/5/9 15:00
     * @param sourceDetails 来源发货明细
     * @param isMerge 是否合并报关
     * @param includeSkuInMergeKey 是否将 SKU 纳入合并维度
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDTO>
     */
    public List<TmsDeclareBillDTO.MergeDeclareBillDTO> generateMergeBillDetails(
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails,
            Boolean isMerge,
            boolean includeSkuInMergeKey) {
        return generateMergeBills(sourceDetails, isMerge, includeSkuInMergeKey);
    }

    /**
     * 构建来源明细和算法明细唯一键的映射
     * @author will
     * @date 2026/5/9 15:00
     * @param sourceDetails 来源发货明细
     * @return java.util.IdentityHashMap<com.erp.model.tms.dto.TmsDeclareBillDTO.SourceDeliveryDetailDTO,java.lang.String>
     */
    private IdentityHashMap<TmsDeclareBillDTO.SourceDeliveryDetailDTO, String> buildSourceKeyMap(
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails) {
        IdentityHashMap<TmsDeclareBillDTO.SourceDeliveryDetailDTO, String> sourceKeyMap = new IdentityHashMap<>();
        for (int i = 0; i < sourceDetails.size(); i++) {
            TmsDeclareBillDTO.SourceDeliveryDetailDTO source = sourceDetails.get(i);
            String shipmentKey = resolveShipmentKey(source);
            String sourceKey = String.join("|",
                    StringUtils.defaultString(shipmentKey),
                    StringUtils.defaultString(source.getBoxNo()),
                    StringUtils.defaultString(source.getSkuId()));
            if (StringUtils.isBlank(sourceKey.replace("|", ""))) {
                sourceKey = "ROW";
            }
            sourceKeyMap.put(source, sourceKey + "#" + i);
        }
        return sourceKeyMap;
    }

    /**
     * 将来源发货明细转换为报关合并算法输入明细
     * @author will
     * @date 2026/5/9 15:00
     * @param sourceDetails 来源发货明细
     * @param sourceKeyMap 来源明细唯一键映射
     * @return java.util.List<com.erp.model.tms.dto.DeclarationGenerationDTO.InputDetailDTO>
     */
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
            input.setSku(source.getSkuNo());
            input.setPrice(source.getUnitPrice());
            input.setQuantity(source.getQty());
            input.setBoxKey(buildDeclareBoxKey(source));
            inputs.add(input);
        }
        return inputs;
    }

    /**
     * 将算法输出结果转换为合并报关明细视图
     * @author will
     * @date 2026/5/9 15:00
     * @param declarations 算法输出报关单结果
     * @param sourceDetails 来源发货明细
     * @param sourceKeyMap 来源明细唯一键映射
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDTO>
     */
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
                linkedSourceList = mergeSourceDetailsByBoxSku(linkedSourceList);

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

    /**
     * 合并同一来源单据、箱号、SKU 的来源明细。
     *
     * @param sourceDetails 算法关联回来的来源明细
     * @return 按来源箱SKU聚合后的来源明细
     */
    private List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> mergeSourceDetailsByBoxSku(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails) {
        if (sourceDetails == null || sourceDetails.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, TmsDeclareBillDTO.SourceDeliveryDetailDTO> mergedMap = new LinkedHashMap<>();
        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : sourceDetails) {
            if (sourceDetail == null) {
                continue;
            }
            String key = buildSourceBoxSkuKey(sourceDetail);
            TmsDeclareBillDTO.SourceDeliveryDetailDTO merged = mergedMap.get(key);
            if (merged == null) {
                TmsDeclareBillDTO.SourceDeliveryDetailDTO copy = new TmsDeclareBillDTO.SourceDeliveryDetailDTO();
                org.springframework.beans.BeanUtils.copyProperties(sourceDetail, copy);
                mergedMap.put(key, copy);
                continue;
            }
            int currentQty = merged.getQty() == null ? 0 : merged.getQty();
            int appendQty = sourceDetail.getQty() == null ? 0 : sourceDetail.getQty();
            merged.setQty(currentQty + appendQty);
        }
        return new ArrayList<>(mergedMap.values());
    }

    private String buildSourceBoxSkuKey(TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail) {
        return String.join("|",
                resolveShipmentKey(sourceDetail),
                StringUtils.defaultString(sourceDetail.getBoxNo()),
                StringUtils.defaultString(sourceDetail.getSkuId()));
    }

    /**
     * 与 {@code TmsDeclareBillServiceImpl#buildDeclareBoxKey} 保持一致：sourceId|boxNo。
     */
    private String buildDeclareBoxKey(TmsDeclareBillDTO.SourceDeliveryDetailDTO source) {
        String sourceKey = StringUtils.defaultIfBlank(source.getSourceId(), source.getBusinessId());
        return StringUtils.defaultString(sourceKey) + "|" + StringUtils.defaultString(source.getBoxNo());
    }

    /**
     * 获取来源明细所属发货单维度键
     * @author will
     * @date 2026/5/9 15:00
     * @param source 来源发货明细
     * @return java.lang.String
     */
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

    /**
     * 构建业务单号和箱号的展示描述
     * @author will
     * @date 2026/5/9 15:00
     * @param source 来源发货明细
     * @return java.lang.String
     */
    private String buildBusinessDesc(TmsDeclareBillDTO.SourceDeliveryDetailDTO source) {
        String businessCode = source.getBusinessCode() == null ? "" : source.getBusinessCode();
        String boxNo = source.getBoxNo() == null ? "" : source.getBoxNo();
        if (businessCode.isEmpty()) {
            return boxNo;
        }
        if (boxNo.isEmpty()) {
            return businessCode;
        }
        return businessCode + "-" + boxNo;
    }

    /**
     * 构建合并规则提示语
     * @author will
     * @date 2026/5/9 15:00
     * @param sourceCount 参与合并的来源明细数量
     * @return java.lang.String
     */
    private String buildMergeRemark(int sourceCount) {
        if (sourceCount > 1) {
            return "按报关要素分组并校验单价极差折算人民币<=10后合并";
        }
        return "未触发合并，保持原单明细";
    }

    /**
     * 处理单一国家维度下的报关合并数据
     * @author will
     * @date 2026/5/9 15:00
     * @param country 国家编码
     * @param countryInputs 当前国家下的算法输入明细
     * @param includeSkuInMergeKey 是否将 SKU 纳入合并维度
     * @return java.util.List<com.erp.model.tms.dto.DeclarationGenerationDTO.OutputDeclarationDTO>
     */
    private List<DeclarationGenerationDTO.OutputDeclarationDTO> processCountry(String country,
                                                                               List<DeclarationGenerationDTO.InputDetailDTO> countryInputs,
                                                                               boolean includeSkuInMergeKey,
                                                                               boolean mergeAcrossShipments) {
        if (mergeAcrossShipments) {
            // 多来源合并时，先跨来源按合并维度聚合明细行，再按48行上限拆分报关单。
            List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> mergedDetails = processShipment(countryInputs, includeSkuInMergeKey);
            return buildDeclarationsByDetailLimit(country, mergedDetails);
        }

        // Step 3: 独立合并时，仅在单个发货单内聚合与价格极差切割。
        Map<String, List<DeclarationGenerationDTO.InputDetailDTO>> byShipment = countryInputs.stream()
                .filter(item -> item.getShipmentOrderId() != null)
                .collect(Collectors.groupingBy(DeclarationGenerationDTO.InputDetailDTO::getShipmentOrderId));

        List<DeclarationGenerationDTO.ShipmentBlock> shipmentBlocks = new ArrayList<>();

        for (Map.Entry<String, List<DeclarationGenerationDTO.InputDetailDTO>> shipmentEntry : byShipment.entrySet()) {
            String shipmentId = shipmentEntry.getKey();
            List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> mergedDetails = processShipment(shipmentEntry.getValue(), includeSkuInMergeKey);
            if (!mergedDetails.isEmpty()) {
                shipmentBlocks.add(new DeclarationGenerationDTO.ShipmentBlock(shipmentId, mergedDetails));
            }
        }

        // Step 4: 装箱与拆分决策树 (Bin Packing)
        return performBinPacking(country, shipmentBlocks);
    }

    /**
     * 处理单一发货单内的明细合并逻辑
     * @author will
     * @date 2026/5/9 15:00
     * @param shipmentInputs 单一发货单下的算法输入明细
     * @param includeSkuInMergeKey 是否将 SKU 纳入合并维度
     * @return java.util.List<com.erp.model.tms.dto.DeclarationGenerationDTO.OutputDeclarationDetailDTO>
     */
    private List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> processShipment(List<DeclarationGenerationDTO.InputDetailDTO> shipmentInputs,
                                                                                     boolean includeSkuInMergeKey) {
        // 1. 按合并键进行初次分组
        Map<DeclarationGenerationDTO.MergeKey, List<DeclarationGenerationDTO.InputDetailDTO>> groupedByKey = shipmentInputs.stream()
                .collect(Collectors.groupingBy(item -> DeclarationGenerationDTO.MergeKey.from(item, includeSkuInMergeKey)));

        List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> result = new ArrayList<>();

        // 2. 极差切割与聚合赋值
        for (List<DeclarationGenerationDTO.InputDetailDTO> groupItems : groupedByKey.values()) {
            // 6维度合并（B2B 按客户，SKU 已进合并键）按合并规则不做价差约束：
            // 同 SKU + 商品编码 + 品名 + 申报要素 + 单位 + 币种 直接聚合为一行。
            if (includeSkuInMergeKey) {
                result.add(aggregateGroup(groupItems));
                continue;
            }

            // 5维度合并（头程 / B2B 非客户）：同要素分组后按单价极差（折算人民币）≤10 切割。
            // 按单价升序排序以应用滑动窗口
            groupItems.sort(Comparator.comparing(
                    item -> item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO
            ));

            // 同一合并分组币种一致（币种是合并键的一部分），按该币种汇率把极差折算成人民币比较。
            String groupCurrency = groupItems.stream()
                    .map(DeclarationGenerationDTO.InputDetailDTO::getDeclarationCurrency)
                    .filter(StringUtils::isNotBlank)
                    .findFirst()
                    .orElse("");
            BigDecimal groupRate = resolveCurrencyRate(groupCurrency);

            List<DeclarationGenerationDTO.InputDetailDTO> currentWindow = new ArrayList<>();
            BigDecimal windowMinPrice = null;

            for (DeclarationGenerationDTO.InputDetailDTO item : groupItems) {
                BigDecimal itemPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;

                if (currentWindow.isEmpty()) {
                    windowMinPrice = itemPrice;
                    currentWindow.add(item);
                } else {
                    // 判断折算人民币后的极差是否超出容忍度 (折算后 max - min <= 10 RMB)
                    BigDecimal priceGapRmb = itemPrice.subtract(windowMinPrice).multiply(groupRate);
                    if (priceGapRmb.compareTo(MAX_PRICE_TOLERANCE) > 0) {
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
     * @author will
     * @date 2026/5/9 15:00
     * @param groupItems 待聚合的算法输入明细
     * @return com.erp.model.tms.dto.DeclarationGenerationDTO.OutputDeclarationDetailDTO
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

        Set<String> boxKeys = groupItems.stream()
                .map(DeclarationGenerationDTO.InputDetailDTO::getBoxKey)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        detail.setBoxKeys(boxKeys);

        return detail;
    }

    /**
     * 执行装箱与拆分：同箱明细必须在同一报关单，48 行为上限（单箱超 48 行时整箱保留在一票）。
     */
    private List<DeclarationGenerationDTO.OutputDeclarationDTO> performBinPacking(String country, List<DeclarationGenerationDTO.ShipmentBlock> blocks) {
        List<List<DeclarationGenerationDTO.OutputDeclarationDetailDTO>> chunks = new ArrayList<>();
        for (DeclarationGenerationDTO.ShipmentBlock block : blocks) {
            chunks.addAll(partitionDetailsByBoxAwareLimit(block.getDetails(), MAX_ROWS_PER_DECLARATION));
        }

        chunks.sort((c1, c2) -> Integer.compare(c2.size(), c1.size()));

        List<DeclarationGenerationDTO.OutputDeclarationDTO> finalDeclarations = new ArrayList<>();
        for (List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> chunk : chunks) {
            DeclarationGenerationDTO.OutputDeclarationDTO targetDeclaration = null;
            for (DeclarationGenerationDTO.OutputDeclarationDTO decl : finalDeclarations) {
                if (!decl.isLocked()
                        && decl.getDetails().size() + chunk.size() <= MAX_ROWS_PER_DECLARATION) {
                    targetDeclaration = decl;
                    break;
                }
            }
            if (targetDeclaration == null) {
                targetDeclaration = new DeclarationGenerationDTO.OutputDeclarationDTO(country);
                targetDeclaration.setLocked(chunk.size() >= MAX_ROWS_PER_DECLARATION);
                finalDeclarations.add(targetDeclaration);
            }
            targetDeclaration.getDetails().addAll(chunk);
            if (targetDeclaration.getDetails().size() >= MAX_ROWS_PER_DECLARATION) {
                targetDeclaration.setLocked(true);
            }
        }
        return finalDeclarations;
    }

    /**
     * 将已跨来源合并后的明细按报关单最大行数切分。
     *
     * @param country 国家编码
     * @param details 已合并报关明细行
     * @return 报关单算法输出
     */
    private List<DeclarationGenerationDTO.OutputDeclarationDTO> buildDeclarationsByDetailLimit(String country,
                                                                                               List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> details) {
        if (details == null || details.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeclarationGenerationDTO.OutputDeclarationDTO> declarations = new ArrayList<>();
        for (List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> detailGroup
                : partitionDetailsByBoxAwareLimit(details, MAX_ROWS_PER_DECLARATION)) {
            DeclarationGenerationDTO.OutputDeclarationDTO declaration = new DeclarationGenerationDTO.OutputDeclarationDTO(country);
            declaration.setLocked(detailGroup.size() >= MAX_ROWS_PER_DECLARATION);
            declaration.getDetails().addAll(detailGroup);
            declarations.add(declaration);
        }
        return declarations;
    }

    /**
     * 按 48 行上限拆票，且保证同一箱（及跨行合并绑定的多箱）不会拆到不同报关单。
     */
    private List<List<DeclarationGenerationDTO.OutputDeclarationDetailDTO>> partitionDetailsByBoxAwareLimit(
            List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> details,
            int maxRowsPerDeclaration) {
        if (details == null || details.isEmpty()) {
            return Collections.emptyList();
        }
        if (details.size() <= maxRowsPerDeclaration) {
            return Collections.singletonList(new ArrayList<>(details));
        }

        List<List<DeclarationGenerationDTO.OutputDeclarationDetailDTO>> rowClusters =
                buildBoxConnectedRowClusters(details);
        rowClusters.sort((c1, c2) -> Integer.compare(c2.size(), c1.size()));

        List<List<DeclarationGenerationDTO.OutputDeclarationDetailDTO>> bins = new ArrayList<>();
        for (List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> cluster : rowClusters) {
            boolean placed = false;
            for (List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> bin : bins) {
                if (bin.size() + cluster.size() <= maxRowsPerDeclaration) {
                    bin.addAll(cluster);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                bins.add(new ArrayList<>(cluster));
            }
        }
        return bins;
    }

    /**
     * 将共享同一箱维度键（或同一合并行内多箱绑定）的明细行聚为不可拆分的簇。
     */
    private List<List<DeclarationGenerationDTO.OutputDeclarationDetailDTO>> buildBoxConnectedRowClusters(
            List<DeclarationGenerationDTO.OutputDeclarationDetailDTO> details) {
        int rowCount = details.size();
        UnionFind unionFind = new UnionFind(rowCount);
        Map<String, Integer> boxKeyFirstRowMap = new HashMap<>();

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Set<String> boxKeys = extractBoxKeys(details.get(rowIndex));
            for (String boxKey : boxKeys) {
                if (StringUtils.isBlank(boxKey)) {
                    continue;
                }
                Integer firstRowIndex = boxKeyFirstRowMap.get(boxKey);
                if (firstRowIndex == null) {
                    boxKeyFirstRowMap.put(boxKey, rowIndex);
                } else {
                    unionFind.union(firstRowIndex, rowIndex);
                }
            }
        }

        Map<Integer, List<DeclarationGenerationDTO.OutputDeclarationDetailDTO>> clusterMap = new LinkedHashMap<>();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            int root = unionFind.find(rowIndex);
            clusterMap.computeIfAbsent(root, ignored -> new ArrayList<>()).add(details.get(rowIndex));
        }
        return new ArrayList<>(clusterMap.values());
    }

    private Set<String> extractBoxKeys(DeclarationGenerationDTO.OutputDeclarationDetailDTO detail) {
        if (detail == null) {
            return Collections.emptySet();
        }
        if (detail.getBoxKeys() != null && !detail.getBoxKeys().isEmpty()) {
            return detail.getBoxKeys();
        }
        if (detail.getLinkedDetailIds() == null || detail.getLinkedDetailIds().isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> boxKeys = new LinkedHashSet<>();
        for (String linkedDetailId : detail.getLinkedDetailIds()) {
            String boxKey = extractBoxKeyFromLinkedDetailId(linkedDetailId);
            if (StringUtils.isNotBlank(boxKey)) {
                boxKeys.add(boxKey);
            }
        }
        return boxKeys;
    }

    private String extractBoxKeyFromLinkedDetailId(String linkedDetailId) {
        if (StringUtils.isBlank(linkedDetailId)) {
            return "";
        }
        String idPart = linkedDetailId;
        int suffixIndex = linkedDetailId.indexOf('#');
        if (suffixIndex >= 0) {
            idPart = linkedDetailId.substring(0, suffixIndex);
        }
        String[] parts = idPart.split("\\|", -1);
        String sourceKey = parts.length > 0 ? parts[0] : "";
        String boxNo = parts.length > 1 ? parts[1] : "";
        return StringUtils.defaultString(sourceKey) + "|" + StringUtils.defaultString(boxNo);
    }

    private static final class UnionFind {
        private final int[] parent;

        private UnionFind(int size) {
            parent = IntStream.range(0, size).toArray();
        }

        private int find(int index) {
            if (parent[index] != index) {
                parent[index] = find(parent[index]);
            }
            return parent[index];
        }

        private void union(int left, int right) {
            int leftRoot = find(left);
            int rightRoot = find(right);
            if (leftRoot != rightRoot) {
                parent[rightRoot] = leftRoot;
            }
        }
    }
}