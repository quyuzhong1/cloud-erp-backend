package com.common.core.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.converters.ConverterKeyBuild;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.common.core.enums.ApiError;
import com.common.core.excel.*;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

/**
 * @Classname ExcelUtil
 * @Description TODO
 * @Date 2022-09-29 10:38
 * @Created by yl
 */
@Slf4j
public class ExcelUtil {

    /**
     * 导出数据为excel文件
     *
     * @param filename   文件名称
     * @param sheetName sheet name
     * @param dataResult 集合内的bean对象类型要与clazz参数一致
     * @param clazz      集合内的bean对象类型要与clazz参数一致
     * @param response   HttpServlet响应对象
     */
    public static void export(String filename,String sheetName, List<?> dataResult, Class<?> clazz, HttpServletResponse response) {
        response.setStatus(200);
        OutputStream outputStream = null;
        ExcelWriter excelWriter = null;
        try {
            if (StringUtils.isBlank(filename)) {
                throw new RuntimeException("'filename' 不能为空");
            }
            String fileName = filename.concat(".xlsx");
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            outputStream = response.getOutputStream();
            excelWriter = getExportExcelWriter(outputStream);
            WriteTable writeTable = EasyExcel.writerTable(0).head(clazz).needHead(true).build();
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();

            // LocalDateTime转化器，导入导出都可以使用
            EasyExcelLocalTimeConverter localTimeDateConverter = new EasyExcelLocalTimeConverter();
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localTimeDateConverter.supportJavaTypeKey()), localTimeDateConverter);
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localTimeDateConverter.supportJavaTypeKey(), localTimeDateConverter.supportExcelTypeKey()), localTimeDateConverter);
            // LocalDate转化器，导入导出都可以使用
            EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);
            // LocalDateTime转化器，导入导出都可以使用
            LocalDateTimeConverter localDateTimeConverter = new LocalDateTimeConverter();
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeConverter.supportJavaTypeKey()), localDateTimeConverter);
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeConverter.supportJavaTypeKey(), localDateTimeConverter.supportExcelTypeKey()), localDateTimeConverter);
            // 写出数据
            excelWriter.write(dataResult, writeSheet, writeTable);

        } catch (Exception e) {
            log.error("导出excel数据异常：", e);
            throw new RuntimeException(e);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    log.error("导出数据关闭流异常", e);
                }
            }
        }
    }




    /**
     * 根据不同策略生成不同的ExcelWriter对象， 可根据实际情况修改
     * @param outputStream  数据输出流
     * @return  数据导出ExcelWriter对象
     */
    private static ExcelWriter getExportExcelWriter(OutputStream outputStream){
        return EasyExcel.write(outputStream)
                .registerWriteHandler(getStyleStrategy())   //字体居中策略
                .build();
    }

    /**
     *  设置表格内容居中显示策略
     * @return
     */
    private static HorizontalCellStyleStrategy getStyleStrategy(){
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置背景颜色
        headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        //设置头字体
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short)13);
        headWriteFont.setBold(true);
        headWriteCellStyle.setWriteFont(headWriteFont);
        //设置头居中
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 内容策略
        WriteCellStyle writeCellStyle = new WriteCellStyle();
        // 设置内容水平居中
        writeCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        return new HorizontalCellStyleStrategy(headWriteCellStyle, writeCellStyle);
    }

    /**
     * 导入后导出
     */
    public static void easyUtil(List<String> heads,String head,List<Map<Integer, String>> list,String fileName,HttpServletResponse response){

        List<List<String>> hs = new ArrayList<>();
        for (String s : heads) {
            hs.add(Arrays.asList(head,s));
        }
        List<List<String>> list2 = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            List<String> objects = new ArrayList<>();
            Map<Integer, String> map = list.get(i);
            for (int j = 0; j < heads.size();j++) {
                String str = map.get(j);
                if (StringUtils.isBlank(str)) {
                    objects.add("");
                } else {
                    objects.add(str);
                }
            }
            list2.add(objects);
        }
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            // 这里需要设置不关闭流
            EasyExcel.write(response.getOutputStream())
                    .head(hs)
                    .registerWriteHandler(getStyleStrategy())
                    // 设置 sheet
                    .autoCloseStream(Boolean.FALSE).sheet(fileName)
                    .sheetName(fileName)
                    //自定义注解
                    .doWrite(list2);
        } catch (Exception e) {
          throw new ServiceException(ApiError.Default);
        }
    }

    /**
     * 单独导出
     */
    public static void easyUtilStr(List<String> heads,String head,List<LinkedHashMap<String, Object>> list,String fileName,HttpServletResponse response){

        List<List<String>> hs = new ArrayList<>();
        for (String s : heads) {
            hs.add(Arrays.asList(head,s));
        }
        Collection<Object> values;
        List<List<Object>> list2 = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            List<Object> objects = new ArrayList<>();
            values = list.get(i).values();
            for (Object value : values) {
                objects.add(value.toString());
            }
            list2.add(objects);
        }
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            // 这里需要设置不关闭流
            EasyExcel.write(response.getOutputStream())
                    .head(hs)
                    .registerWriteHandler(getStyleStrategy())
                    // 设置 sheet
                    .autoCloseStream(Boolean.FALSE).sheet(fileName)
                    .sheetName(fileName)
                    //自定义注解
                    .doWrite(list2);

        } catch (Exception e) {
            throw new ServiceException(ApiError.Default);
        }
    }

    /**
     * 返回文件
     * @author yl
     * @date 2023-02-14 12:15
     * @param fileName
     * @param sheetName
     * @param dataResult
     * @param clazz
     * @return java.lang.String
     */
    public static File exportFile(String fileName, String sheetName, List<?> dataResult, Class<?> clazz) {
        File tempDirectory = FileUtils.getTempDirectory();
        File filePath = new File(tempDirectory,fileName);
        //生成本地文件
        EasyExcel.write(filePath.getAbsolutePath(), clazz).sheet(sheetName).doWrite(dataResult);
        return filePath;

    }

    /**
     * 导出数据为excel文件（按内容自适应列宽）
     *
     * @param filename   文件名称
     * @param sheetName sheet name
     * @param dataResult 集合内的bean对象类型要与clazz参数一致
     * @param clazz      集合内的bean对象类型要与clazz参数一致
     * @param response   HttpServlet响应对象
     * @param contentHorizontalAlignment（内容水平样式，传null则默认居中）
     */
    public static void exportAdapt(String filename,String sheetName, List<?> dataResult, Class<?> clazz, HttpServletResponse response, HorizontalAlignment contentHorizontalAlignment) {
        response.setStatus(200);
        OutputStream outputStream = null;
        ExcelWriter excelWriter = null;
        try {
            if (StringUtils.isBlank(filename)) {
                throw new RuntimeException("'filename' 不能为空");
            }
            String fileName = filename.concat(".xlsx");
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            outputStream = response.getOutputStream();
            // 注册自定义样式
            excelWriter = EasyExcel.write(outputStream).registerWriteHandler(new CustomCellWriteHandler()).registerWriteHandler(new CustomCellStyleHandler(contentHorizontalAlignment)).build();
            WriteTable writeTable = EasyExcel.writerTable(0).head(clazz).needHead(true).build();
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();

            // LocalDateTime转化器，导入导出都可以使用
            EasyExcelLocalTimeConverter localTimeDateConverter = new EasyExcelLocalTimeConverter();
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localTimeDateConverter.supportJavaTypeKey()), localTimeDateConverter);
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localTimeDateConverter.supportJavaTypeKey(), localTimeDateConverter.supportExcelTypeKey()), localTimeDateConverter);
            // LocalDate转化器，导入导出都可以使用
            EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);
            // LocalDateTime转化器，导入导出都可以使用
            LocalDateTimeConverter localDateTimeConverter = new LocalDateTimeConverter();
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeConverter.supportJavaTypeKey()), localDateTimeConverter);
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeConverter.supportJavaTypeKey(), localDateTimeConverter.supportExcelTypeKey()), localDateTimeConverter);
            // 写出数据
            excelWriter.write(dataResult, writeSheet, writeTable);

        } catch (Exception e) {
            log.error("导出excel数据异常：", e);
            throw new RuntimeException(e);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    log.error("导出数据关闭流异常", e);
                }
            }
        }
    }
}
