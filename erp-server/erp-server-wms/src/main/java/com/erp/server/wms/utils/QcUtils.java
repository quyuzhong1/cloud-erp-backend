package com.erp.server.wms.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.PicFormatEnum;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.QcReportDetailDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.xssf.usermodel.*;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 质检工具类
 * @CreateTime: 2023-06-21  16:33
 * @Author: zhangchunlin
 */
@Slf4j
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
        cell.setCellValue("质检员");
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

    /**
     * 供应商报表生成内容行
     * @param rowNo
     * @param resultList
     * @param filePublicUrl
     * @param sheet
     * @param wb
     * @param contentCellStyle
     * @param hyperContentCellStyle
     */
    public static void createQcDailyRptContent(Integer rowNo, List<QcInfoDTO.QcDailyReportDTO> resultList, String filePublicUrl,
                                               XSSFSheet sheet, XSSFWorkbook wb, CellStyle contentCellStyle, XSSFCellStyle hyperContentCellStyle) {
        for(int i = 0;i < resultList.size();i++) {
            QcInfoDTO.QcDailyReportDTO data =  resultList.get(i);

            ++rowNo;

            XSSFRow rowContent = sheet.createRow(rowNo);
            rowContent.setHeight((short) (40 * 60));
            rowContent.setHeightInPoints((short) 50);

            Cell cell = rowContent.createCell(0);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(Objects.nonNull(data.getQcDate()) ? data.getQcDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");

            cell = rowContent.createCell(1);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(data.getQcInsideTypeName());

            cell = rowContent.createCell(2);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(data.getSkuNo());

            cell = rowContent.createCell(3);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(data.getFirstMassProductName());

            cell = rowContent.createCell(4);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue("");
            // 产品图片
            if(CollUtil.isNotEmpty(data.getProductImgUrl())) {
                try {
                    // 暂只取一张图片
                    InputStream inputStream = FastDFSClientUtil.getInputStream(data.getProductImgUrl().get(0));
                    XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 255, 255,
                            4, rowNo, 4 + 1, rowNo + 1);
                    // 图片自适应单元格大小
                    anchor.setAnchorType(ClientAnchor.AnchorType.byId(0));
                    XSSFDrawing patriarch = sheet.createDrawingPatriarch();
                    patriarch.createPicture(anchor, wb.addPicture(inputStream, XSSFWorkbook.PICTURE_TYPE_WPG));
                } catch (Exception e) {
                    log.error("写入图片失败", e);
                }
            }

            // 箱唛图
            cell = rowContent.createCell(5);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue("");
            if(CollUtil.isNotEmpty(data.getBoxMarkImgUrl())) {
                try {
                    // 暂只取一张图片
                    InputStream inputStream = FastDFSClientUtil.getInputStream(data.getBoxMarkImgUrl().get(0));
                    XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 255, 255,
                            5, rowNo, 5 + 1, rowNo + 1);
                    // 图片自适应单元格大小
                    anchor.setAnchorType(ClientAnchor.AnchorType.byId(0));
                    XSSFDrawing patriarch = sheet.createDrawingPatriarch();
                    patriarch.createPicture(anchor, wb.addPicture(inputStream, XSSFWorkbook.PICTURE_TYPE_WPG));
                } catch (Exception e) {
                    log.error("写入图片失败", e);
                }
            }

            cell = rowContent.createCell(6);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getSupplierName()));

            cell = rowContent.createCell(7);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getProductName()));

            cell = rowContent.createCell(8);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getTotalQty()));

            cell = rowContent.createCell(9);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getQcResultName()));

            cell = rowContent.createCell(10);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getQcUserName()));

            cell = rowContent.createCell(11);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getQcQty()));

            cell = rowContent.createCell(12);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getQcBadQty()));

            cell = rowContent.createCell(13);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getQcBadRate()));

            cell = rowContent.createCell(14);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getQcProblemName()));

            cell = rowContent.createCell(15);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getBadDescription()));

            // 不良附图
            cell = rowContent.createCell(16);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue("");
            if(CollUtil.isNotEmpty(data.getBadAttachments())) {
                // 判断是否有图片，有图片则显示图片，没有图片则添加超链接
                List<String> picFormats = Arrays.asList(PicFormatEnum.values()).stream().map(PicFormatEnum::getCode).collect(Collectors.toList());
                WmsAttachmentDTO.UpdateDTO badAttachment = data.getBadAttachments().stream().filter(r->
                        r.getAttachUrl().contains(".") && picFormats.contains(r.getAttachUrl().substring(r.getAttachUrl().lastIndexOf(".") + 1))
                ).findFirst().orElse(null);
                if(Objects.nonNull(badAttachment)) {
                    try {
                        // 暂只取一张图片
                        InputStream inputStream = FastDFSClientUtil.getInputStream(badAttachment.getAttachUrl());
                        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 255, 255,
                                16, rowNo, 16 + 1, rowNo + 1);
                        // 图片自适应单元格大小
                        anchor.setAnchorType(ClientAnchor.AnchorType.byId(0));
                        XSSFDrawing patriarch = sheet.createDrawingPatriarch();
                        patriarch.createPicture(anchor, wb.addPicture(inputStream, XSSFWorkbook.PICTURE_TYPE_WPG));
                    } catch (Exception e) {
                        log.error("写入图片失败", e);
                    }
                } else {
                    // 写入超链接（只能写一个）
                    badAttachment = data.getBadAttachments().get(0);
                    Hyperlink hyperlink = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
                    hyperlink.setAddress(filePublicUrl + badAttachment.getAttachUrl());
                    rowContent.getCell(16).setCellStyle(hyperContentCellStyle);
                    rowContent.getCell(16).setHyperlink(hyperlink);
                    rowContent.getCell(16).setCellValue(badAttachment.getAttachName());
                }
            }

            cell = rowContent.createCell(17);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getHandleModeName()));

            cell = rowContent.createCell(18);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getHandleResultName()));

            cell = rowContent.createCell(19);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(CharSequenceUtil.format("{}*{}*{}", Objects.isNull(data.getProductLength()) ? "0" : data.getProductLength(),
                    Objects.isNull(data.getProductWidth()) ? "0" : data.getProductWidth(),
                    Objects.isNull(data.getProductHeight()) ? "0" : data.getProductHeight()));

            cell = rowContent.createCell(20);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getProductNetWeight()));

            cell = rowContent.createCell(21);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(CharSequenceUtil.format("{}*{}*{}", Objects.isNull(data.getBoxLength()) ? "0" : data.getBoxLength(),
                    Objects.isNull(data.getBoxWidth()) ? "0" : data.getBoxWidth(),
                    Objects.isNull(data.getBoxHeight()) ? "0" : data.getBoxHeight()));

            cell = rowContent.createCell(22);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getBoxWeight()));

            // 整箱数量
            cell = rowContent.createCell(23);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getFullBoxQty()));

            cell = rowContent.createCell(24);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getRemark()));

            // 报告
            cell = rowContent.createCell(25);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue("");
            if(CollUtil.isNotEmpty(data.getReportList())) {
                // 写入超链接（只能写一个）
                QcReportDetailDTO.ViewDTO reportAttachment = data.getReportList().stream().filter(r->CollUtil.isNotEmpty(r.getReportUrlList())).findFirst().orElse(null);
                if(Objects.nonNull(reportAttachment)) {
                    Hyperlink hyperlink = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
                    hyperlink.setAddress(filePublicUrl + reportAttachment.getReportUrlList().get(0));
                    rowContent.getCell(25).setCellStyle(hyperContentCellStyle);
                    rowContent.getCell(25).setHyperlink(hyperlink);
                    rowContent.getCell(25).setCellValue(reportAttachment.getReportNameList().get(0));
                }
            }

        }
    }

}