package com.common.core.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.ConverterKeyBuild;
import com.alibaba.excel.converters.bytearray.ByteArrayImageConverter;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.common.core.enums.ApiError;
import com.common.core.excel.*;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.common.core.enums.ApiError.EXCEL_PARSING_FIELD_EXCEPTION;

/**
 * @Classname ExcelUtil

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
     * @param exportFields  导出的字段
     */
    public static void export(String filename,String sheetName, List<?> dataResult, Class<?> clazz, HttpServletResponse response,List<String> exportFields) {
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

            List<Integer> excludeIndexes = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(exportFields)) {
                // 反射获取字段属性
                Field[] declaredFields = clazz.getDeclaredFields();
                //过滤掉ExcelIgnore 注解
                List<Field> filteredFields = new ArrayList<>();
                for (Field field : declaredFields) {
                    // 如果字段不包含 @ExcelIgnore 注解，添加到 filteredFields 列表中
                    if (!field.isAnnotationPresent(ExcelIgnore.class)) {
                        filteredFields.add(field);
                    }
                }
                // 遍历过滤后的字段，匹配需要忽略的字段
                for (int i = 0; i < filteredFields.size(); i++) {
                    Field field = filteredFields.get(i);
                    if (!exportFields.contains(field.getName())) {
                        excludeIndexes.add(i);
                    }
                }
            }

            outputStream = response.getOutputStream();
            excelWriter = getExportExcelWriter(outputStream,excludeIndexes);

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
            // 图片转换器
            ByteArrayImageConverter byteArrayImageConverter = new ByteArrayImageConverter();
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(byteArrayImageConverter.supportJavaTypeKey()), byteArrayImageConverter);
            excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(byteArrayImageConverter.supportJavaTypeKey(), byteArrayImageConverter.supportExcelTypeKey()), byteArrayImageConverter);

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
     * 导出数据为excel文件
     *
     * @param filename   文件名称
     * @param sheetName sheet name
     * @param dataResult 集合内的bean对象类型要与clazz参数一致
     * @param clazz      集合内的bean对象类型要与clazz参数一致
     * @param response   HttpServlet响应对象
     */
    public static void export(String filename,String sheetName, List<?> dataResult, Class<?> clazz, HttpServletResponse response) {
        ExcelUtil.export(filename,sheetName,dataResult,clazz,response,null);
    }




    /**
     * 根据不同策略生成不同的ExcelWriter对象， 可根据实际情况修改
     * @param outputStream  数据输出流
     * @return  数据导出ExcelWriter对象
     */
    private static ExcelWriter getExportExcelWriter(OutputStream outputStream,List<Integer> excludeIndexes){
        return EasyExcel.write(outputStream)
                .registerWriteHandler(getStyleStrategy())   //字体居中策略
                .excludeColumnIndexes(excludeIndexes)
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
     * @description: 传jsonObject导出
     * @author Will
     * @date: 2024/5/11 16:02
     * @param heads
     * @param list
     * @param fileName
     * @param response
     */
    public static void customExportUtil(List<String> heads, List<JSONObject> list, String fileName, HttpServletResponse response){

        List<List<String>> hs = new ArrayList<>();
        for (String s : heads) {
            hs.add(Arrays.asList(s));
        }
        List<List<String>> list2 = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            List<String> objects = new ArrayList<>();
            JSONObject map = list.get(i);
            for (int j = 0; j < heads.size();j++) {
                Object str = map.get(String.valueOf(j));
                if (ObjectUtil.isEmpty(str)) {
                    objects.add("");
                } else {
                    objects.add(str.toString());
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

    public static byte[] easyUtilStr(List<String> heads,String head,List<LinkedHashMap<String, Object>> list,String fileName){
        List<List<String>> hs = new ArrayList<>();
        for (String s : heads) {
            hs.add(Arrays.asList(head,s));
        }
        Collection<Object> values;
        List<List<Object>> list2 = new ArrayList<>();

        for (LinkedHashMap<String, Object> stringObjectLinkedHashMap : list) {
            List<Object> objects = new ArrayList<>();
            values = stringObjectLinkedHashMap.values();
            for (Object value : values) {
                objects.add(value.toString());
            }
            list2.add(objects);
        }
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 这里需要设置不关闭流
            EasyExcelFactory.write(outputStream)
                    .head(hs)
                    .registerWriteHandler(getStyleStrategy())
                    // 设置 sheet
                    .autoCloseStream(Boolean.FALSE).sheet(fileName)
                    .sheetName(fileName)
                    //自定义注解
                    .doWrite(list2);
            return outputStream.toByteArray();
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


    public static File exportFile(String fileName, String sheetName, List<?> dataResult, List<String> heads) {
        List<List<String>> hs = new ArrayList<>();
        for (String s : heads) {
            hs.add(Arrays.asList(s));
        }
        File tempDirectory = FileUtils.getTempDirectory();
        File filePath = new File(tempDirectory,fileName);
        //生成本地文件
        EasyExcel.write(filePath).head(hs).sheet(sheetName).doWrite(dataResult);
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

    /**
     * 获取EXCEL单元格值
     * @param cell
     * @return
     */
    public static String convertCellValueToString(Cell cell) {
        if (null == cell) {
            return null;
        }
        String returnValue = null;
        switch (cell.getCellType()) {
            case STRING:
                //字符串
                returnValue = cell.getStringCellValue();
                break;
            case NUMERIC:
                //数字
                cell.setCellType(CellType.STRING);
                returnValue = cell.getStringCellValue();
                break;
            case BOOLEAN:
                //布尔
                boolean booleanCellValue = cell.getBooleanCellValue();
                returnValue = Boolean.toString(booleanCellValue);
                break;
            case BLANK:
                //空值
                break;
            case FORMULA:
                //公式
                returnValue = cell.getCellFormula();
                break;
            case ERROR:
                //故障
                break;
            default:
                break;
        }
        return StrUtils.null2EmptyWithTrim(returnValue);
    }

    /**
     * 处理合并单元格
     * @param data               解析数据
     * @param extraMergeInfoList 合并单元格信息
     * @param headRowNumber      起始行
     * @return 填充好的解析数据
     */
    public static <T> List<T> explainMergeData(List<T> data, List<CellExtra> extraMergeInfoList, Integer headRowNumber) {
        //循环所有合并单元格信息
        extraMergeInfoList.forEach(cellExtra -> {
            int firstRowIndex = cellExtra.getFirstRowIndex() - headRowNumber;
            int lastRowIndex = cellExtra.getLastRowIndex() - headRowNumber;
            int firstColumnIndex = cellExtra.getFirstColumnIndex();
            int lastColumnIndex = cellExtra.getLastColumnIndex();
            //获取初始值
            Object initValue = getInitValueFromList(firstRowIndex, firstColumnIndex, data);
            //设置值
            for (int i = firstRowIndex; i <= lastRowIndex; i++) {
                for (int j = firstColumnIndex; j <= lastColumnIndex; j++) {
                    setInitValueToList(initValue, i, j, data);
                }
            }
        });
        return data;
    }

    /**
     * 设置合并单元格的值
     * @param filedValue  值
     * @param rowIndex    行
     * @param columnIndex 列
     * @param data        解析数据
     */
    private static <T> void setInitValueToList(Object filedValue, Integer rowIndex, Integer columnIndex, List<T> data) {
        T object = data.get(rowIndex);
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            if (annotation != null) {
                if (annotation.index() == columnIndex) {
                    try {
                        field.set(object, filedValue);
                        break;
                    } catch (IllegalAccessException e) {
                        throw new ServiceException(EXCEL_PARSING_FIELD_EXCEPTION);
                    }
                }
            }
        }
    }


    /**
     * 获取合并单元格的初始值
     * rowIndex对应list的索引
     * columnIndex对应实体内的字段
     * @param firstRowIndex    起始行
     * @param firstColumnIndex 起始列
     * @param data             列数据
     * @return 初始值
     */
    private static <T> Object getInitValueFromList(Integer firstRowIndex, Integer firstColumnIndex, List<T> data) {
        Object filedValue = null;
        T object = data.get(firstRowIndex);
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            if (annotation != null) {
                if (annotation.index() == firstColumnIndex) {
                    try {
                        filedValue = field.get(object);
                        break;
                    } catch (IllegalAccessException e) {
                        throw new ServiceException(EXCEL_PARSING_FIELD_EXCEPTION);
                    }
                }
            }
        }
        return filedValue;
    }


    /**
     * 模板下载
     * @param path 代码Excel文件路径
     * @param excelName Excel文件名
     * @param response 响应体
     */
    public static void downloadTemplate(String path, String excelName, HttpServletResponse response) {
        try (InputStream inputStream =  new DefaultResourceLoader().getResource(path).getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(inputStream)) {
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
        } catch (Exception e) {
            log.error(" downloadTemplate 下载失败 e={}", e.getMessage());throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    /**
     * 动态模板下载
     * @param headerName 动态列表名称>
     * @param configExcelName Excel文件名
     * @param response 响应体
     */
    public static void downloadDynamicTemplate(LinkedList<String> headerName, String configExcelName, HttpServletResponse response) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            // 创建工作表
            Sheet sheet = wb.createSheet("sheet1");
            Row row = sheet.createRow(0); // 创建第一行
            // 写入数据
            for (int i = 0; i < headerName.size(); i++) {
                Cell cell = row.createCell(i); // 创建单元格
                cell.setCellValue(headerName.get(i)); // 写入名称
                // 自适应列宽
                // 计算内容宽度并设置单元格宽度
                int contentWidth = headerName.get(i).getBytes(StandardCharsets.UTF_8).length * 400; // 中文字符宽度按照字节数计算
                sheet.setColumnWidth(i, contentWidth); // 设置列宽度
            }
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(configExcelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
        } catch (Exception e) {
            log.error(" downloadTemplate 下载失败 e={}", e.getMessage());throw new ServiceException(ApiError.ERROR_95131);
        }
    }
}
