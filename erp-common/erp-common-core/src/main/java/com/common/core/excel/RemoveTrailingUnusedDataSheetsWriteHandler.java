package com.common.core.excel;

import com.alibaba.excel.write.handler.AbstractWorkbookWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 在 {@code ExcelWriter.finish()} 写出前，按「实际写入到的最后一张数据 sheet 下标」移除尾部未 fill 的预留 sheet。
 * <p>
 * 用于分页导出预克隆了多张数据 sheet、但实际未全部写入的场景，避免成品 Excel 末尾残留带占位符的模板克隆页。
 * 须在 {@code finish()} 前调用 {@link #prepareFinish(int)} 传入写循环结束时的 lastUsed 物理 sheet 下标（0-based，
 * 即 workbook 中最后一张已写入数据的 sheet 索引，通常为 {@code dataSheetIndexes.get(lastUsedLogical)}）。
 * <p>
 * 依赖 {@code expandTemplateWithDataSheetCopies} 约定：数据 sheet 物理下标连续为 0..n-1。
 */
public class RemoveTrailingUnusedDataSheetsWriteHandler extends AbstractWorkbookWriteHandler {

    private int lastUsedDataSheetIndex = -1;

    /**
     * @param lastUsedDataSheetIndex 最后一张写入过数据的 sheet 在 workbook 中的物理下标（含仅写入表头/占位替换的 sheet）
     */
    public void prepareFinish(int lastUsedDataSheetIndex) {
        this.lastUsedDataSheetIndex = lastUsedDataSheetIndex;
    }

    @Override
    public void afterWorkbookDispose(WriteWorkbookHolder writeWorkbookHolder) {
        if (lastUsedDataSheetIndex < 0) {
            return;
        }
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        if (workbook == null) {
            return;
        }
        while (workbook.getNumberOfSheets() - 1 > lastUsedDataSheetIndex) {
            workbook.removeSheetAt(workbook.getNumberOfSheets() - 1);
        }
    }
}
