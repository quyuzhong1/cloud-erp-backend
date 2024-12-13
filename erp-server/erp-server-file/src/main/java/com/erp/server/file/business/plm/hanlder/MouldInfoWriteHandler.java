package com.erp.server.file.business.plm.hanlder;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.AbstractCellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.common.core.utils.FastDFSClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;

import java.io.InputStream;
import java.util.List;

@Slf4j
public class MouldInfoWriteHandler extends AbstractCellWriteHandler {

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<CellData> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        if (Boolean.TRUE.equals(isHead) || cell == null) {
            return; // 跳过表头和空单元格
        }
        String rowData = cellDataList.get(0).getStringValue();
        if (ObjectUtil.isEmpty(rowData)) {
            return; // 如果数据为空，不做处理
        }
        if (cell.getColumnIndex() == 7) {
            try {
                Workbook workbook = cell.getSheet().getWorkbook();
                InputStream inputStream = FastDFSClientUtil.getInputStream(rowData);
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
}
