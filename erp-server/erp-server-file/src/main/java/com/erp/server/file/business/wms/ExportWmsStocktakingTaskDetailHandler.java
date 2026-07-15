package com.erp.server.file.business.wms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.file.entity.FileTask;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractFileEventHandler;
import com.erp.server.file.core.ExportTempFilesHandler;
import com.erp.server.file.handler.FileRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_STOCKTAKING_TASK_DETAIL;

/**
 * 盘点任务明细导出：按库区拆分多 sheet，首行写库区/盘点人等元信息。
 */
@Component
@Slf4j
public class ExportWmsStocktakingTaskDetailHandler extends AbstractFileEventHandler<StocktakingTaskDetailDTO.ExportDTO> {

    private static final String EMPTY_AREA_NAME = "空仓位";

    private static final List<List<String>> DETAIL_HEAD = Arrays.asList(
            Collections.singletonList("盘点任务单号"),
            Collections.singletonList("盘点方式"),
            Collections.singletonList("仓库"),
            Collections.singletonList("仓位"),
            Collections.singletonList("SKU"),
            Collections.singletonList("产品名称"),
            Collections.singletonList("可用库存"),
            Collections.singletonList("冻结库存"),
            Collections.singletonList("初盘数量"),
            Collections.singletonList("*盘点库存"),
            Collections.singletonList("差异数量"),
            Collections.singletonList("盘点人")
    );

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/wms/StocktakingTaskDetail.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_STOCKTAKING_TASK_DETAIL;
    }

    @Override
    public void handle(FileTask fileTask) {
        StocktakingTaskDTO.BaseIdDTO params = readValue(fileTask.getMetaInfo(),
                new TypeReference<StocktakingTaskDTO.BaseIdDTO>() {
                });
        String displayName = buildDownloadFileName(fileTask, getExcelPath());
        ExportTempFilesHandler.exportToTempAndUpload(fileTask, ".xlsx", displayName,
                outFile -> writeByAreaSheets(outFile, params));
    }

    private int writeByAreaSheets(File outFile, StocktakingTaskDTO.BaseIdDTO params) {
        List<StocktakingTaskDetailDTO.ExportDTO> rows = loadAllRows(params);
        if (CollUtil.isEmpty(rows)) {
            throw new ServiceException("导出数据为空");
        }

        Map<String, List<StocktakingTaskDetailDTO.ExportDTO>> areaGroups = rows.stream()
                .collect(Collectors.groupingBy(
                        r -> CharSequenceUtil.blankToDefault(r.getWarehouseAreaName(), EMPTY_AREA_NAME),
                        LinkedHashMap::new,
                        Collectors.toList()));

        int maxRowsPerSheet = Math.max(1, FileRegistry.sheetMaxRowsOrDefault() - 2);
        int maxSheets = FileRegistry.maxSheetNumOrDefault();
        if (areaGroups.size() > maxSheets) {
            throw new ServiceException("导出库区数量 " + areaGroups.size() + " 超过系统 sheet 上限 " + maxSheets
                    + "，请缩小范围或联系管理员调整配置。");
        }

        Set<String> usedSheetNames = new HashSet<>();
        int totalDataRows = 0;
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(outFile).build();
            int sheetIndex = 0;
            for (Map.Entry<String, List<StocktakingTaskDetailDTO.ExportDTO>> entry : areaGroups.entrySet()) {
                String areaName = entry.getKey();
                List<StocktakingTaskDetailDTO.ExportDTO> areaRows = entry.getValue();
                if (areaRows.size() > maxRowsPerSheet) {
                    throw new ServiceException("库区【" + areaName + "】明细 " + areaRows.size()
                            + " 行超过单 sheet 上限 " + maxRowsPerSheet + "，请拆分盘点任务后导出。");
                }
                String sheetName = uniqueSheetName(sanitizeSheetName(areaName), usedSheetNames);
                String stocktakingUserName = areaRows.get(0).getStocktakingUserName();
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetIndex++, sheetName)
                        .head(DETAIL_HEAD)
                        .relativeHeadRowIndex(1)
                        .registerWriteHandler(new AreaMetaSheetWriteHandler(areaName, stocktakingUserName))
                        .build();
                excelWriter.write(toDataRows(areaRows), writeSheet);
                totalDataRows += areaRows.size();
            }
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
        return totalDataRows;
    }

    private List<StocktakingTaskDetailDTO.ExportDTO> loadAllRows(StocktakingTaskDTO.BaseIdDTO params) {
        PagingDTO<StocktakingTaskDTO.BaseIdDTO> dto = new PagingDTO<>();
        dto.setParams(params);
        dto.setCurrPage(1);
        // WMS 侧一次返回全量明细，pageSize 仅用于构造 PagingVO
        dto.setPageSize(FileRegistry.exportPageSize());
        PagingVO<StocktakingTaskDetailDTO.ExportDTO> page = exportWmsFeign.exportStocktakingTaskDetail(dto);
        if (page == null || CollUtil.isEmpty(page.getList())) {
            return Collections.emptyList();
        }
        return page.getList();
    }

    private static List<List<Object>> toDataRows(List<StocktakingTaskDetailDTO.ExportDTO> areaRows) {
        List<List<Object>> data = new ArrayList<>(areaRows.size());
        for (StocktakingTaskDetailDTO.ExportDTO row : areaRows) {
            data.add(Arrays.asList(
                    row.getCode(),
                    row.getStocktakingModeName(),
                    row.getWarehouseName(),
                    row.getWarehouseLocation(),
                    row.getSkuNo(),
                    row.getProductName(),
                    row.getUsableQty(),
                    row.getFrozenQty(),
                    row.getFirstQty(),
                    row.getQty(),
                    row.getDiffQty(),
                    row.getStocktakingUserName()
            ));
        }
        return data;
    }

    private static String sanitizeSheetName(String name) {
        String raw = CharSequenceUtil.blankToDefault(name, EMPTY_AREA_NAME);
        String sanitized = raw.replaceAll("[:\\\\/?*\\[\\]]", "_");
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 31);
        }
        if (CharSequenceUtil.isBlank(sanitized)) {
            sanitized = EMPTY_AREA_NAME;
        }
        return sanitized;
    }

    private static String uniqueSheetName(String baseName, Set<String> used) {
        String candidate = baseName;
        int suffix = 2;
        while (used.contains(candidate)) {
            String suffixStr = "_" + suffix++;
            int maxBase = Math.max(1, 31 - suffixStr.length());
            candidate = (baseName.length() > maxBase ? baseName.substring(0, maxBase) : baseName) + suffixStr;
        }
        used.add(candidate);
        return candidate;
    }

    /**
     * 在 sheet 第 1 行写入库区/盘点人等元信息（表头在 relativeHeadRowIndex=1 即第 2 行）。
     */
    private static class AreaMetaSheetWriteHandler implements SheetWriteHandler {
        private final String areaName;
        private final String stocktakingUserName;

        private AreaMetaSheetWriteHandler(String areaName, String stocktakingUserName) {
            this.areaName = areaName;
            this.stocktakingUserName = CharSequenceUtil.nullToEmpty(stocktakingUserName);
        }

        @Override
        public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
            // no-op
        }

        @Override
        public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
            Sheet sheet = writeSheetHolder.getSheet();
            Row row = sheet.getRow(0);
            if (row == null) {
                row = sheet.createRow(0);
            }
            row.createCell(0).setCellValue("库区：");
            row.createCell(1).setCellValue(areaName);
            row.createCell(2).setCellValue("盘点人：");
            row.createCell(3).setCellValue(stocktakingUserName);
            row.createCell(4).setCellValue("仓管员：");
            row.createCell(5).setCellValue("");
            row.createCell(6).setCellValue("仓库主管：");
            row.createCell(7).setCellValue("");
            applyColumnWidths(sheet);
        }
    }

    /**
     * 明细列宽（字符宽度，与历史 {@code StocktakingTaskDetailExcelDTO} @ColumnWidth 量级对齐并按字段语义微调）。
     * POI 单位为 1/256 字符宽。
     */
    private static void applyColumnWidths(Sheet sheet) {
        // 盘点任务单号、盘点方式、仓库、仓位、SKU、产品名称、可用、冻结、初盘、盘点、差异、盘点人
        int[] charWidths = {20, 12, 18, 16, 22, 30, 12, 12, 12, 12, 12, 18};
        for (int i = 0; i < charWidths.length; i++) {
            sheet.setColumnWidth(i, charWidths[i] * 256);
        }
    }
}
