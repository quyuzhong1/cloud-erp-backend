package com.erp.server.file.business.wms.hanldler;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.AbstractCellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.common.core.utils.FastDFSClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;

import java.io.InputStream;
import java.util.List;

@Slf4j
public class HyperlinkWriteHandler extends AbstractCellWriteHandler {


    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<CellData> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        if (Boolean.TRUE.equals(isHead) || cell == null) {
            return; // 跳过表头和空单元格
        }
        String rowData = cellDataList.get(0).getStringValue();
        if (ObjectUtil.isEmpty(rowData)) {
            return; // 如果数据为空，不做处理
        }
        switch (cell.getColumnIndex()) {
            case 25:
                handleHyperlink(cell, rowData);
                break;

            case 4:
            case 5:
            case 16:
                handleImageOrHyperlink(cell, rowData, writeSheetHolder, relativeRowIndex);
                break;

            default:
                break;
        }
    }

    private void handleHyperlink(Cell cell, String rowData) {
        String[] split = rowData.split(",");
        if (split.length > 1) {
            Workbook workbook = cell.getSheet().getWorkbook();
            CreationHelper creationHelper = workbook.getCreationHelper();
            Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress(FastDFSClientUtil.publicUrl + split[1]);
            cell.setHyperlink(hyperlink);
            cell.setCellStyle(getCellStyle(workbook));
            cell.setCellValue(split[0]);
        }
    }

    private void handleImageOrHyperlink(Cell cell, String rowData, WriteSheetHolder writeSheetHolder, Integer relativeRowIndex) {
        String[] split = rowData.split(",");

        // 处理图片插入
        if (split.length == 1) {
            insertImage(cell, split[0], writeSheetHolder, relativeRowIndex);
        }
        // 处理超链接
        else if (split.length == 2) {
            handleHyperlink(cell, rowData);
        }
    }

    private void insertImage(Cell cell, String imageUrl, WriteSheetHolder writeSheetHolder, Integer relativeRowIndex) {
        try {
            Workbook workbook = cell.getSheet().getWorkbook();
            InputStream inputStream = FastDFSClientUtil.getInputStream(imageUrl);
            assert inputStream != null;
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);

            Drawing<?> drawingPatriarch = writeSheetHolder.getSheet().createDrawingPatriarch();
            CreationHelper helper = workbook.getCreationHelper();
            ClientAnchor anchor = getClientAnchor(helper, cell.getColumnIndex(), relativeRowIndex);

            Picture picture = drawingPatriarch.createPicture(anchor, pictureIdx);
            picture.resize(); // 自动调整图片大小
        } catch (Exception e) {
            log.error("写入图片失败", e);
        }
    }

    private static ClientAnchor getClientAnchor(CreationHelper helper, int col1, Integer relativeRowIndex) {
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setDx1(0);
        anchor.setDy1(0);
        anchor.setDx2(255);
        anchor.setDy2(255);
        anchor.setCol1(col1);
        anchor.setCol2(col1 + 1);
        anchor.setRow1(relativeRowIndex);
        anchor.setRow2(relativeRowIndex + 1);
        return anchor;
    }

    private CellStyle getCellStyle(Workbook workbook) {
        // 超链接样式
        CellStyle cellStyle = workbook.createCellStyle();
        // 水平居左
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        //垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //自动换行
        cellStyle.setWrapText(true);
        //下边框
        cellStyle.setBorderBottom(BorderStyle.THIN);
        //左边框
        cellStyle.setBorderLeft(BorderStyle.THIN);
        //上边框
        cellStyle.setBorderTop(BorderStyle.THIN);
        //右边框
        cellStyle.setBorderRight(BorderStyle.THIN);

        Font hyperFont = workbook.createFont();
        hyperFont.setFontHeightInPoints((short) 12);
        hyperFont.setColor(IndexedColors.BLUE.getIndex());
        hyperFont.setUnderline(Font.U_SINGLE);
        cellStyle.setFont(hyperFont);
        return cellStyle;
    }
}
