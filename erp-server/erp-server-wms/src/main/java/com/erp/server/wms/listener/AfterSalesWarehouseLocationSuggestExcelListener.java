package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.AfterSalesWarehouseLocationSuggestExcelDto;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * 仓位售后推荐导入监听
 *
 * @author liuchao
 * @date 2026-05-06
 */
@Getter
public class AfterSalesWarehouseLocationSuggestExcelListener extends AnalysisEventListener<LinkedHashMap<Integer, String>> {
    /**
     * 导入正确数据
     */
    @Getter
    private List<AfterSalesWarehouseLocationSuggestExcelDto> successList = new ArrayList<>();

    /**
     * 导入数据，用于判断导入是否为空
     */
    @Getter
    private List<AfterSalesWarehouseLocationSuggestExcelDto> allList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    @Getter
    private List<AfterSalesWarehouseLocationSuggestExcelDto> errorList = new ArrayList<>();

    /** 启用数据中按名称未匹配到库区 */
    private static final String MSG_WAREHOUSE_AREA_NOT_FOUND = "未能找到仓库库区，请确定库区是否正确";
    /** 启用数据中库区下按编码未匹配到仓位 */
    private static final String MSG_WAREHOUSE_LOCATION_NOT_FOUND = "未能找到仓库仓位，请确定仓位是否正确";
    /**
     * 与 {@link AfterSalesWarehouseLocationSuggestExcelDto} 中 {@code @ExcelProperty} 的标题一致，缺任一列即拒绝导入。
     */
    private static final List<String> REQUIRED_IMPORT_HEADER_TITLES = Collections.unmodifiableList(Arrays.asList(
            "sku编码",
            "产品名称",
            "所属仓库",
            "所属库区名称",
            "推荐仓位编码",
            "优先级",
            "状态"
    ));
    private final PlmTaskFeign plmTaskFeign = SpringUtil.getBean(PlmTaskFeign.class);
    private final WarehouseLocationService warehouseLocationService = SpringUtil.getBean(WarehouseLocationService.class);
    private final WarehouseService warehouseService = SpringUtil.getBean(WarehouseService.class);
    /**
     * 解析行临时序号（同一引用一行一条，不修改 Excel DTO 类本身）
     */
    private final IdentityHashMap<AfterSalesWarehouseLocationSuggestExcelDto, Long> importRowTempIdMap = new IdentityHashMap<>();
    /**
     * 每行各异常块累积（格式 + 重复 + 业务），解析阶段写入格式；收尾阶段合并重复与业务后再统一生成 errorMsg。
     */
    private final IdentityHashMap<AfterSalesWarehouseLocationSuggestExcelDto, EnumMap<ImportErrorBlock, String>> rowAccumulatedErrors = new IdentityHashMap<>();
    /**
     * 表头标题（去空白后）→ 列下标，在 {@link #invokeHeadMap} 中构建并校验必填列齐全。
     */
    private Map<String, Integer> headerTitleToColumnIndex = Collections.emptyMap();
    private long importRowTempIdSeq = 1L;

    private static String normalizeHeaderTitle(CharSequence raw) {
        return raw == null ? "" : CharSequenceUtil.trim(raw);
    }

    private static String firstSkuFormatError(AfterSalesWarehouseLocationSuggestExcelDto dto) {
        if (CharSequenceUtil.isBlank(dto.getSkuNo())) {
            return "SKU编码不能为空";
        }
        if (dto.getSkuNo().length() > 255) {
            return "SKU编码过长";
        }
        return null;
    }

    private static String firstProductNameFormatError(AfterSalesWarehouseLocationSuggestExcelDto dto) {
        if (CharSequenceUtil.isNotBlank(dto.getProductName()) && dto.getProductName().length() > 255) {
            return "产品名称过长";
        }
        return null;
    }

    private static String firstWarehouseChainFormatError(AfterSalesWarehouseLocationSuggestExcelDto dto) {
        if (CharSequenceUtil.isBlank(dto.getWarehouseName())) {
            return "仓库名称不能为空";
        }
        if (dto.getWarehouseName().length() > 255) {
            return "仓库名称过长";
        }
        if (CharSequenceUtil.isBlank(dto.getWarehouseAreaName())) {
            return "库区名称不能为空";
        }
        if (dto.getWarehouseAreaName().length() > 19) {
            return "库区名称过长";
        }
        if (CharSequenceUtil.isBlank(dto.getWarehouseLocationCode())) {
            return "推荐仓位编码不能为空";
        }
        if (dto.getWarehouseLocationCode().length() > 32) {
            return "推荐仓位编码过长";
        }
        return null;
    }

    private static String firstSortFormatError(AfterSalesWarehouseLocationSuggestExcelDto dto) {
        if (CharSequenceUtil.isBlank(dto.getSort())) {
            return "优先级不能为空";
        }
        if (!StrUtils.isInteger(dto.getSort())) {
            return "优先级请填写数字";
        }
        if (Integer.parseInt(dto.getSort()) > 9) {
            return "优先级的值不能大于9";
        }
        return null;
    }

    private static String firstStatusFormatError(AfterSalesWarehouseLocationSuggestExcelDto dto) {
        Set<String> allowed = new HashSet<>();
        allowed.add("启用");
        allowed.add("禁用");
        if (!allowed.contains(dto.getStatus())) {
            return "无法识别状态选择，仅可填“启用/禁用”";
        }
        return null;
    }

    private static void putFirstBlockError(EnumMap<ImportErrorBlock, String> blockErrors, ImportErrorBlock block, String msg) {
        if (CharSequenceUtil.isNotBlank(msg)) {
            blockErrors.putIfAbsent(block, msg);
        }
    }

    private static List<String> blockErrorsToSortedList(EnumMap<ImportErrorBlock, String> errors) {
        List<String> out = new ArrayList<>();
        for (ImportErrorBlock b : ImportErrorBlock.values()) {
            String m = errors.get(b);
            if (CharSequenceUtil.isNotBlank(m)) {
                out.add(m);
            }
        }
        return out;
    }

    /**
     * 解析阶段为该行分配的临时序号（与 DTO 引用绑定，未写入 Excel DTO 实体字段）。
     */
    public Long getImportTempRowId(AfterSalesWarehouseLocationSuggestExcelDto row) {
        return importRowTempIdMap.get(row);
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        Map<String, Integer> titleToIndex = new LinkedHashMap<>();
        if (headMap != null) {
            headMap.forEach((colIndex, rawTitle) -> {
                String title = normalizeHeaderTitle(rawTitle);
                if (CharSequenceUtil.isBlank(title)) {
                    return;
                }
                titleToIndex.putIfAbsent(title, colIndex);
            });
        }
        List<String> missing = new ArrayList<>();
        for (String required : REQUIRED_IMPORT_HEADER_TITLES) {
            if (!titleToIndex.containsKey(required)) {
                missing.add(required);
            }
        }
        if (!missing.isEmpty()) {
            throw new ServiceException("导入模板表头不完整，缺少列：{}", String.join("、", missing));
        }
        this.headerTitleToColumnIndex = titleToIndex;
    }

    @Override
    public void invoke(LinkedHashMap<Integer, String> data, AnalysisContext context) {
        if (headerTitleToColumnIndex == null || headerTitleToColumnIndex.isEmpty()) {
            throw new ServiceException("导入文件未识别到有效表头，请下载最新导入模板");
        }
        AfterSalesWarehouseLocationSuggestExcelDto dto = new AfterSalesWarehouseLocationSuggestExcelDto();
        dto.setSkuNo(cellByHeader(data, "sku编码"));
        dto.setProductName(cellByHeader(data, "产品名称"));
        dto.setWarehouseName(cellByHeader(data, "所属仓库"));
        dto.setWarehouseAreaName(cellByHeader(data, "所属库区名称"));
        dto.setWarehouseLocationCode(cellByHeader(data, "推荐仓位编码"));
        dto.setSort(cellByHeader(data, "优先级"));
        dto.setStatus(cellByHeader(data, "状态"));
        importRowTempIdMap.put(dto, importRowTempIdSeq++);

        EnumMap<ImportErrorBlock, String> rowErr = new EnumMap<>(ImportErrorBlock.class);
        verifyFieldFormat(dto, rowErr);
        rowAccumulatedErrors.put(dto, rowErr);
        allList.add(dto);
    }

    /**
     * 按表头标题取单元格字符串（首尾空白已去掉），列下标由表头行解析得到。
     */
    private String cellByHeader(LinkedHashMap<Integer, String> row, String headerTitle) {
        Integer col = headerTitleToColumnIndex.get(headerTitle);
        if (col == null) {
            return null;
        }
        return CharSequenceUtil.trim(row.get(col));
    }

    /**
     * 字段格式校验：按 {@link ImportErrorBlock} 聚合，同一异常块内只保留一条说明（块内递进顺序取首条）。
     */
    private void verifyFieldFormat(AfterSalesWarehouseLocationSuggestExcelDto dto, EnumMap<ImportErrorBlock, String> blockErrors) {
        putFirstBlockError(blockErrors, ImportErrorBlock.SKU, firstSkuFormatError(dto));
        putFirstBlockError(blockErrors, ImportErrorBlock.PRODUCT_NAME, firstProductNameFormatError(dto));
        putFirstBlockError(blockErrors, ImportErrorBlock.WAREHOUSE_CHAIN, firstWarehouseChainFormatError(dto));
        putFirstBlockError(blockErrors, ImportErrorBlock.SORT, firstSortFormatError(dto));
        putFirstBlockError(blockErrors, ImportErrorBlock.STATUS, firstStatusFormatError(dto));

        if (!blockErrors.containsKey(ImportErrorBlock.STATUS) && CharSequenceUtil.isNotBlank(dto.getStatus())) {
            switch (dto.getStatus()) {
                case "启用":
                    dto.setDisabled(Boolean.FALSE);
                    break;
                case "禁用":
                    dto.setDisabled(Boolean.TRUE);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (CollectionUtils.isEmpty(allList)) {
            return;
        }
        Function<AfterSalesWarehouseLocationSuggestExcelDto, String> businessKeyFunc =
                dto -> dto.getSkuNo() + "|" + dto.getWarehouseName() + "|" + dto.getWarehouseAreaName() + "|" + dto.getWarehouseLocationCode();

        Map<String, Long> countMap = allList.stream().collect(Collectors.groupingBy(businessKeyFunc, Collectors.counting()));

        allList.forEach(dto -> {
            if (countMap.get(businessKeyFunc.apply(dto)) > 1) {
                rowAccumulatedErrors.get(dto).put(ImportErrorBlock.DUPLICATE, "sku+仓位在导入表中重复了，请调整");
            }
        });

        List<String> skuNoList = allList.stream().map(AfterSalesWarehouseLocationSuggestExcelDto::getSkuNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> warehouseNameList = allList.stream().map(AfterSalesWarehouseLocationSuggestExcelDto::getWarehouseName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuVOS = CollUtil.isNotEmpty(skuNoList) ? plmTaskFeign.listBySkuNos(skuNoList) : Collections.emptyList();
        List<WarehouseDTO.ListDTO> warehouseVOS = CollUtil.isNotEmpty(warehouseNameList) ? warehouseService.listByNames(warehouseNameList) : Collections.emptyList();

        Map<String, WarehouseDTO.ListDTO> warehouseNameAndIdMap = warehouseVOS.stream().collect(Collectors.toMap(WarehouseDTO.ListDTO::getName, item -> item));

        List<String> warehouseIdList = warehouseNameAndIdMap.values().stream().map(WarehouseDTO.ListDTO::getId).collect(Collectors.toList());

        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIds(warehouseIdList);

        Map<String, List<WarehouseLocationEntity>> warehouseLocationMap = warehouseLocationList.stream().collect(Collectors.groupingBy(WarehouseLocationEntity::getWarehouseId));

        for (AfterSalesWarehouseLocationSuggestExcelDto excelDTO : allList) {
            EnumMap<ImportErrorBlock, String> merged = rowAccumulatedErrors.get(excelDTO);

            ProductDetailEntity productDetail = skuVOS.stream().filter(e -> Objects.equals(e.getSkuNo(), excelDTO.getSkuNo())).findFirst().orElse(null);
            if (Objects.isNull(productDetail)) {
                merged.putIfAbsent(ImportErrorBlock.SKU, "无法识别sku，请确定sku编码是否正确");
            } else {
                excelDTO.setSkuId(productDetail.getId());
                if (CharSequenceUtil.isNotBlank(excelDTO.getProductName())) {
                    String importName = excelDTO.getProductName().trim();
                    String dbName = productDetail.getName() == null ? "" : productDetail.getName().trim();
                    if (!importName.equals(dbName)) {
                        merged.putIfAbsent(ImportErrorBlock.PRODUCT_NAME, "产品名称与SKU对应品名不一致");
                    }
                }
            }

            WarehouseDTO.ListDTO warehouseDto = warehouseNameAndIdMap.get(excelDTO.getWarehouseName());
            if (warehouseDto == null || Boolean.TRUE.equals(warehouseDto.getDisabled())) {
                merged.putIfAbsent(ImportErrorBlock.WAREHOUSE_CHAIN, "未能找到仓库，请确定仓库是否正确/已启用");
            } else {
                String warehouseId = warehouseDto.getId();
                excelDTO.setWarehouseId(warehouseId);

                List<WarehouseLocationEntity> rawList = Optional.ofNullable(warehouseLocationMap.get(warehouseId)).orElse(Collections.emptyList());
                List<WarehouseLocationEntity> enabledList = rawList.stream()
                        .filter(e -> !Boolean.TRUE.equals(e.getDisabled()))
                        .collect(Collectors.toList());

                String areaType = WarehouseLocationTypeEnum.AREA.getCode();
                String locationType = WarehouseLocationTypeEnum.LOCATION.getCode();

                WarehouseLocationEntity matchedArea = enabledList.stream()
                        .filter(e -> areaType.equals(e.getType()) && Objects.equals(e.getName(), excelDTO.getWarehouseAreaName()))
                        .findFirst()
                        .orElse(null);

                if (matchedArea == null) {
                    merged.putIfAbsent(ImportErrorBlock.WAREHOUSE_CHAIN, MSG_WAREHOUSE_AREA_NOT_FOUND);
                } else {
                    List<WarehouseLocationEntity> locationsUnderArea = enabledList.stream()
                            .filter(e -> locationType.equals(e.getType()) && Objects.equals(matchedArea.getId(), e.getParentId()))
                            .collect(Collectors.toList());

                    List<WarehouseLocationEntity> matchedLocations = locationsUnderArea.stream()
                            .filter(l -> Objects.equals(l.getCode(), excelDTO.getWarehouseLocationCode()))
                            .collect(Collectors.toList());

                    if (CollUtil.isEmpty(matchedLocations)) {
                        merged.putIfAbsent(ImportErrorBlock.WAREHOUSE_CHAIN, MSG_WAREHOUSE_LOCATION_NOT_FOUND);
                    } else if (matchedLocations.size() > 1) {
                        merged.putIfAbsent(ImportErrorBlock.WAREHOUSE_CHAIN, "该库区下有多个仓位编码相同的仓位");
                    } else {
                        WarehouseLocationEntity loc = matchedLocations.get(0);
                        excelDTO.setWarehouseAreaId(matchedArea.getId());
                        excelDTO.setWarehouseAreaCode(matchedArea.getCode());
                        excelDTO.setWarehouseLocationId(loc.getId());
                        excelDTO.setWarehouseLocationCode(loc.getCode());
                    }
                }
            }
            if (!merged.isEmpty()) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(blockErrorsToSortedList(merged)));
                errorList.add(excelDTO);
            } else {
                successList.add(excelDTO);
            }
        }
    }

    /**
     * 导入行校验异常块：同一异常块内只保留一条说明；多块并存时按此枚举顺序（与模板列业务层级一致）输出后再
     * {@link FieldValidUtil#getMsgSort(List)} 编号拼接。
     * <p>顺序：1.SKU编码 2.产品名称 3.仓库/库区/仓位 4.优先级 5.状态 6.重复行。</p>
     */
    private enum ImportErrorBlock {
        SKU,
        PRODUCT_NAME,
        WAREHOUSE_CHAIN,
        SORT,
        STATUS,
        DUPLICATE
    }
}
