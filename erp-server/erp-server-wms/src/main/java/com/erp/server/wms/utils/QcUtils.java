package com.erp.server.wms.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * 质检工具类
 * @CreateTime: 2023-06-21  16:33
 * @Author: zhangchunlin
 */
public class QcUtils {

    public static void createQcDailyRptTitle(Integer rowNo, XSSFSheet sheet, CellStyle titleStyle) {
        // 标题
        XSSFRow rowTitle0 = sheet.createRow(rowNo);
        Cell cell;
        cell = rowTitle0.createCell(0);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("日期");
        cell = rowTitle0.createCell(1);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("内外检验");
        cell = rowTitle0.createCell(2);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("SKU");
        cell = rowTitle0.createCell(3);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("产品状态");
        cell = rowTitle0.createCell(4);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("产品图片");
        cell = rowTitle0.createCell(5);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("箱唛图");
        cell = rowTitle0.createCell(6);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("供应商");
        cell = rowTitle0.createCell(7);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("产品名称");
        cell = rowTitle0.createCell(8);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("回仓数量");
        cell = rowTitle0.createCell(9);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("验货结果");
        cell = rowTitle0.createCell(10);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("验货员");
        cell = rowTitle0.createCell(11);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("质检数量");
        cell = rowTitle0.createCell(12);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("不良品数量");
        cell = rowTitle0.createCell(13);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("抽检不良率");
        cell = rowTitle0.createCell(14);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("问题属性");
        cell = rowTitle0.createCell(15);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("不良现象");
        cell = rowTitle0.createCell(16);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("不良附图");
        cell = rowTitle0.createCell(17);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("处理方式");
        cell = rowTitle0.createCell(18);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("处理结果");
        cell = rowTitle0.createCell(19);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("产品尺寸（cm）");
        cell = rowTitle0.createCell(20);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("产品重量（g）");
        cell = rowTitle0.createCell(21);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("外箱尺寸（cm）");
        cell = rowTitle0.createCell(22);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("外箱重量（kg)");
        cell = rowTitle0.createCell(23);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("整箱数量（个)");
        cell = rowTitle0.createCell(24);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("备注");
        cell = rowTitle0.createCell(25);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("报告");
    }

}