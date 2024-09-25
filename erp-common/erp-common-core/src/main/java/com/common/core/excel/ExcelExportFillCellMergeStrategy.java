package com.common.core.excel;

import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Arrays;
import java.util.List;

public class ExcelExportFillCellMergeStrategy implements CellWriteHandler {

    // 需要从第几行开始合并，0表示第1行
    private int mergeRowIndex = 1;
    // 合并的哪些列，比如为4时，当前行id和上一行id相同则合并前五列
    private int mergeColumnRegion;
    // 判断合并的列
    private List<Integer> checkIndexList = Arrays.asList(0);

    public ExcelExportFillCellMergeStrategy() {
    }

    public ExcelExportFillCellMergeStrategy(int mergeColumnRegion,List<Integer> checkIndexList) {
        this.mergeColumnRegion = mergeColumnRegion;
        this.checkIndexList = checkIndexList;
    }
    public ExcelExportFillCellMergeStrategy(int mergeRowIndex, int mergeColumnRegion,List<Integer> checkIndexList) {
        this.mergeRowIndex = mergeRowIndex;
        this.mergeColumnRegion = mergeColumnRegion;
        this.checkIndexList = checkIndexList;
    }
    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head, Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {

    }

    @Override
    public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
//        // 隐藏id列
//        writeSheetHolder.getSheet().setColumnHidden(0, true);
    }

    @Override
    public void afterCellDataConverted(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, CellData cellData, Cell cell, Head head, Integer integer, Boolean aBoolean) {

    }

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<CellData> list, Cell cell, Head head, Integer integer, Boolean aBoolean) {
        //当前行
        int curRowIndex = cell.getRowIndex();
        //当前列
        int curColIndex = cell.getColumnIndex();

        if (curRowIndex > mergeRowIndex) {
            for (int i = 0; i < mergeColumnRegion; i++) {
                if (curColIndex == mergeColumnRegion) {
                    mergeWithPreviousRow(writeSheetHolder, cell, curRowIndex, curColIndex);
                    break;
                }
            }
        }
    }

    /**
     * 当前单元格向上合并：当前行的id和上一行的id相同则合并前面（mergeColumnRegion+1）列
     *
     * @param writeSheetHolder
     * @param cell             当前单元格
     * @param curRowIndex      当前行
     * @param curColIndex      当前列
     */
    private void mergeWithPreviousRow(WriteSheetHolder writeSheetHolder, Cell cell, int curRowIndex, int curColIndex) {
        boolean needMerge = true;
        for (Integer index : checkIndexList) {
            // 当前行
            Cell curFirstCell = cell.getSheet().getRow(curRowIndex).getCell(index);
            Object curFirstData = curFirstCell.getCellTypeEnum() == CellType.STRING ? curFirstCell.getStringCellValue() : curFirstCell.getNumericCellValue();
            // 上一行
            Cell preFirstCell = cell.getSheet().getRow(curRowIndex - 1).getCell(index);
            Object preFirstData = preFirstCell.getCellTypeEnum() == CellType.STRING ? preFirstCell.getStringCellValue() : preFirstCell.getNumericCellValue();
            needMerge = needMerge && curFirstData.equals(preFirstData);
        }


        // 当前行的id和上一行的id相同则合并前面（mergeColumnRegion+1）列
        if (needMerge) {
            for(int colIndex = 0;colIndex<=mergeColumnRegion;colIndex++){
                Sheet sheet = writeSheetHolder.getSheet();
                List<CellRangeAddress> mergeRegions = sheet.getMergedRegions();
                boolean isMerged = false;
                for (int i = 0; i < mergeRegions.size() && !isMerged; i++) {
                    CellRangeAddress cellRangeAddr = mergeRegions.get(i);
                    // 若上一个单元格已经被合并，则先移出原有的合并单元，再重新添加合并单元
                    if (cellRangeAddr.isInRange(curRowIndex - 1, colIndex)) {
                        sheet.removeMergedRegion(i);
                        cellRangeAddr.setLastRow(curRowIndex);
                        sheet.addMergedRegion(cellRangeAddr);
                        isMerged = true;
                    }
                }
                // 若上一个单元格未被合并，则新增合并单元
                if (!isMerged) {
                    CellRangeAddress cellRangeAddress = new CellRangeAddress(curRowIndex - 1, curRowIndex, colIndex, colIndex);
                    sheet.addMergedRegion(cellRangeAddress);
                }
            }
        }
    }
}
