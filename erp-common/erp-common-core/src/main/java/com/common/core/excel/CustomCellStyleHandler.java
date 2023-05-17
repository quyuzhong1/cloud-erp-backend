package com.common.core.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.style.AbstractCellStyleStrategy;
import org.apache.poi.ss.usermodel.*;

import java.util.Objects;

/**
 * @Classname CustomCellWriteHandler
 * @Description EasyExcel自定义样式（标题和内容行）
 * @Date 2023-05-17 17:02:00
 * @Created by zhangchunlin
 */
public class CustomCellStyleHandler extends AbstractCellStyleStrategy implements CellWriteHandler {

    private Workbook workbook;

    //标题样式
    private CellStyle titleStyle = null;

    //内容样式
    private CellStyle contentStyle = null;

    //内容水平样式
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;

    public CustomCellStyleHandler() {

    }

    public CustomCellStyleHandler(HorizontalAlignment horizontalAlignment) {
        if(Objects.nonNull(horizontalAlignment)) {
            this.horizontalAlignment = horizontalAlignment;
        }
    }

    @Override
    protected void initCellStyle(Workbook workbook) {
        this.workbook = workbook;
        Font font = this.workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short) 13);
        //设置粗体
        font.setBold(true);
        titleStyle = this.workbook.createCellStyle();
        //设置水平居中
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直对齐的样式为居中对齐;
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置背景颜色
        titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setFont(font);

        contentStyle = this.workbook.createCellStyle();
        //设置水平对齐的样式
        contentStyle.setAlignment(horizontalAlignment);
        //设置垂直对齐的样式为居中对齐;
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    /**
     * 设置标题样式
     * @param cell
     * @param head
     * @param integer
     */
    @Override
    protected void setHeadCellStyle(Cell cell, Head head, Integer integer) {
        cell.setCellStyle(titleStyle);
    }

    @Override
    protected void setContentCellStyle(Cell cell, Head head, Integer integer) {
        cell.setCellStyle(contentStyle);
    }
}
