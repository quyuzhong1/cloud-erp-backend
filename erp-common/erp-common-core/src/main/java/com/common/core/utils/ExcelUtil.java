package com.common.core.utils;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
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
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.common.core.dto.MultiErrorExcelData;
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
import javax.xml.stream.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.common.core.enums.ApiError.FILE_EXCEL_PARSING_FIELD_EXCEPTION;

/**
 * @Classname ExcelUtil

 * @Date 2022-09-29 10:38
 * @Created by yl
 */
@Slf4j
public class ExcelUtil {
    private final static Integer BATCH_COUNT = 5000;

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
    public static void export(String filename,String sheetName, List<?> dataResult, Class<?> clazz, HttpServletResponse response,List<String> exportFields, WriteHandler writeHandler) {
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
            excelWriter = getExportExcelWriter(outputStream,excludeIndexes,writeHandler);

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
        ExcelUtil.export(filename,sheetName,dataResult,clazz,response,null,null);
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
    public static void export(String filename,String sheetName, List<?> dataResult, Class<?> clazz, HttpServletResponse response,List<String> exportFields) {
        ExcelUtil.export(filename,sheetName,dataResult,clazz,response,exportFields,null);
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
    public static void export(String filename,String sheetName, List<?> dataResult, Class<?> clazz, HttpServletResponse response, WriteHandler writeHandler) {
        ExcelUtil.export(filename,sheetName,dataResult,clazz,response,null,writeHandler);
    }




    /**
     * 根据不同策略生成不同的ExcelWriter对象， 可根据实际情况修改
     * @param outputStream  数据输出流
     * @return  数据导出ExcelWriter对象
     */
    private static ExcelWriter getExportExcelWriter(OutputStream outputStream, List<Integer> excludeIndexes, WriteHandler writeHandler){
        ExcelWriterBuilder builder =  EasyExcel.write(outputStream)
                .registerWriteHandler(getStyleStrategy())   //字体居中策略
                .excludeColumnIndexes(excludeIndexes);
        if(Objects.nonNull(writeHandler)){
            builder.registerWriteHandler(writeHandler);
        }
        return builder.build();
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
            throw new ServiceException(ApiError.HTTP_UNKNOWN);
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
            throw new ServiceException(ApiError.HTTP_UNKNOWN);
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
            throw new ServiceException(ApiError.HTTP_UNKNOWN);
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
            throw new ServiceException(ApiError.HTTP_UNKNOWN);
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
        File filePath = new File(tempDirectory,"template"+ LocalDate.now() +".xlsx");
        File outputFile = new File(tempDirectory,fileName);
        if (dataResult.size() > BATCH_COUNT){
            if(!filePath.exists()) {
                EasyExcel.write(filePath.getAbsolutePath(), clazz).sheet(sheetName).doWrite(new ArrayList<>());
            }
            List<? extends List<?>> partition = ListUtil.partition(dataResult, BATCH_COUNT);
            // 初始化写入器（append模式）
            ExcelWriter excelWriter = EasyExcel.write(outputFile, clazz)
                    .withTemplate(filePath) // 复用原文件
                    .inMemory(false) // 禁用内存缓存
                    .build();
            WriteSheet sheet = EasyExcel.writerSheet(sheetName).build();
            for (List<?> batch : partition) {
                excelWriter.write(batch, sheet); // 使用同一个sheet实例
            }
            // 必须关闭资源
            excelWriter.finish();
        }else {
            //生成本地文件
            EasyExcel.write(outputFile.getAbsolutePath(), clazz).sheet(sheetName).doWrite(dataResult);
        }
        return outputFile;

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
     * 导出文件
     * @param templatePath
     * @param fileName
     * @param dataResult
     * @return File
     */
    public static File exportFile(String templatePath,String fileName, List<?> dataResult) {
        // 1. 获取模板输入流（假设模板在 resources/excel 目录下）
        InputStream templateStream = ExcelUtil.class.getClassLoader().getResourceAsStream(templatePath);
        // 2. 修改后的导出代码
        File tempDirectory = FileUtils.getTempDirectory();
        File outputFile = new File(tempDirectory,fileName);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            // EasyExcel 模板填充逻辑
            ExcelWriter excelWriter = EasyExcel.write(fos)
                    .withTemplate(templateStream) // 绑定模板
                    .build();

            WriteSheet writeSheet = EasyExcel.writerSheet().build();

            // 填充数据（假设 errorList 是模板中的占位符数据）
            excelWriter.fill(dataResult, writeSheet);

            excelWriter.finish(); // 必须调用 finish 确保写入完成
            return outputFile;
        } catch (IOException e) {
            throw new ServiceException(ApiError.FILE_EXPORT_ERROR_DATA_FAILED);
        } finally {
            // 关闭模板流（重要！）
            if (templateStream != null) {
                try {
                    templateStream.close();
                } catch (IOException e) {
                    // 日志记录或处理异常
                }
            }
        }
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
                        throw new ServiceException(FILE_EXCEL_PARSING_FIELD_EXCEPTION);
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
                        throw new ServiceException(FILE_EXCEL_PARSING_FIELD_EXCEPTION);
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
                    "attchement;filename=" + URLEncoder.encode(excelName, StandardCharsets.UTF_8.name()));
            response.setContentType("application/msexcel");
            wb.write(output);
        } catch (Exception e) {
            log.error("ExcelUtil.downloadTemplate 下载失败 e={}", ExceptionUtil.stacktraceToString(e));
            throw new ServiceException(ApiError.FILE_IMPORT_TEMPLATE_DOWNLOAD_FAILED);
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
                    "attchement;filename=" + new String(configExcelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
        } catch (Exception e) {
            log.error(" downloadTemplate 下载失败 e={}", e.getMessage());throw new ServiceException(ApiError.FILE_IMPORT_TEMPLATE_DOWNLOAD_FAILED);
        }
    }
    public static File customExportUtil(String fileName, List<JSONObject> list, List<String> heads) {
        List<List<String>> hs = new ArrayList<>();
        for (String s : heads) {
            hs.add(Arrays.asList(s));
        }

        List<List<String>> list2 = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<String> objects = new ArrayList<>();
            JSONObject map = list.get(i);
            for (int j = 0; j < heads.size(); j++) {
                Object str = map.get(String.valueOf(j));
                objects.add(ObjectUtil.isEmpty(str) ? "" : str.toString());
            }
            list2.add(objects);
        }

        ExcelWriter excelWriter = null;
        try {
            File tempDirectory = FileUtils.getTempDirectory();
            FileUtils.forceMkdir(tempDirectory);

            String prefix = StringUtils.isBlank(fileName) ? "excel" : fileName;
            prefix = prefix.replaceAll("[\\\\/:*?\"<>|]", "_");
            if (prefix.length() < 3) {
                prefix = "excel_" + prefix;
            }

            File tempFile = File.createTempFile(prefix + "_", ".xlsx", tempDirectory);

            if (list2.size() > BATCH_COUNT) {
                excelWriter = EasyExcel.write(tempFile)
                        .head(hs)
                        .registerWriteHandler(getStyleStrategy())
                        .inMemory(false)
                        .build();

                WriteSheet sheet = EasyExcel.writerSheet(fileName).build();
                List<List<List<String>>> partition = ListUtil.partition(list2, BATCH_COUNT);
                for (List<List<String>> batch : partition) {
                    excelWriter.write(batch, sheet);
                }
            } else {
                EasyExcel.write(tempFile)
                        .head(hs)
                        .registerWriteHandler(getStyleStrategy())
                        .sheet(fileName)
                        .doWrite(list2);
            }

            return tempFile;
        } catch (IOException e) {
            log.error("创建Excel临时文件失败，fileName={}", fileName, e);
            throw new ServiceException("创建Excel临时文件失败：{}", e.getMessage());
        } catch (Exception e) {
            log.error("自定义Excel导出失败，fileName={}", fileName, e);
            throw new ServiceException(ApiError.HTTP_UNKNOWN);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    public static File generateTemplateFile(String fileName, List<MultiErrorExcelData> list) {
        try {
            File tempDirectory = FileUtils.getTempDirectory();
            File outputFile = new File(tempDirectory, fileName);

            // 创建ExcelWriter
            ExcelWriter excelWriter = EasyExcel.write(outputFile).build();

            try {
                for (MultiErrorExcelData multiErrorExcelData : list) {
                    // 写入每个sheet
                    WriteSheet writeSheet = EasyExcel.writerSheet(multiErrorExcelData.getSheetName())
                            .head(multiErrorExcelData.getClazz())
                            .build();
                    excelWriter.write(multiErrorExcelData.getDataResult(), writeSheet);
                }
            } finally {
                // 确保ExcelWriter被正确关闭
                if (excelWriter != null) {
                    excelWriter.finish();
                }
            }

            return outputFile;
        } catch (Exception e) {
            throw new ServiceException(ApiError.HTTP_UNKNOWN);
        }
    }

    /**
     * OLE2 复合文档文件头，用于轻量识别 .xls 格式（无需载入工作簿）。
     */
    private static final byte[] XLS_MAGIC = new byte[]{(byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0};

    /**
     * ZIP 文件头（PK\3\4），用于轻量识别 .xlsx 格式（.xlsx 本质是 ZIP 包）。
     */
    private static final byte[] XLSX_MAGIC = new byte[]{(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04};

    /**
     * 读取前清除 Excel 中的「公式错误单元格」（#VALUE!/#REF!/#N/A 等），并将结果写入临时文件，
     * 避免大文件在内存中生成清洗后的 byte[] 副本。入参文件不会被修改。
     * <p>
     * 背景：跨表公式失效后整列变成错误值，且常被向下填充到大量空行，
     * 这些行里唯一有内容的就是错误单元格，导致 EasyExcel 把它们当成有数据的行读出来（幽灵行），甚至直接报错。
     * 把错误单元格清空后，幽灵行变为完全空行，交由 EasyExcel 自带的「忽略空行」机制跳过，
     * 无需在监听器中判空；正常公式算出的值（缓存结果）原样保留，不受影响。
     * <p>
     * 按格式分别处理，兼顾内存安全：
     * <ul>
     *   <li>.xls(OLE2)：行数有上界（65536），用 POI 全量载入清理后写回临时文件，内存可控；</li>
     *   <li>.xlsx(ZIP)：行数无上限，改用「ZIP + StAX 流式」逐个单元格过滤，删除 t="e" 的错误单元格，
     *       其余字节原样透传写入临时文件，不构建内存工作簿对象模型，避免 OOM；</li>
     *   <li>其它/未知格式：原样返回不处理。</li>
     * </ul>
     *
     * @param inputFile 原始 Excel 临时文件
     * @param fileName  文件名（仅用于日志，可为空）
     * @return 可供 EasyExcel 读取的文件；无错误单元格或处理失败时返回原文件，否则返回新生成的清洗临时文件
     */
    public static File clearFormulaErrorCellsToTempFile(File inputFile, String fileName) {
        if (inputFile == null || !inputFile.exists() || inputFile.length() == 0) {
            throw new ServiceException(ApiError.COMMON_FILE_EMPTY, StringUtils.isNotBlank(fileName) ? fileName : "");
        }
        try {
            if (isXlsFormat(inputFile)) {
                return clearXlsFormulaErrorCellsToTempFile(inputFile, fileName);
            }
            if (isXlsxFormat(inputFile)) {
                return clearXlsxFormulaErrorCellsToTempFile(inputFile, fileName);
            }
            return inputFile;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("清除 Excel 公式错误单元格失败，文件名={}，将使用原始文件解析", fileName, e);
            return inputFile;
        }
    }

    /**
     * 文件版 .xls 清理：清理后写入新的临时文件，避免返回清洗后的 byte[]。
     */
    private static File clearXlsFormulaErrorCellsToTempFile(File inputFile, String fileName) {
        try (InputStream inputStream = new FileInputStream(inputFile);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            int clearedCount = clearWorkbookErrorCells(workbook);
            if (clearedCount == 0) {
                return inputFile;
            }
            File outputFile = File.createTempFile("formula-error-clean-", ".xls", FileUtils.getTempDirectory());
            try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                workbook.write(outputStream);
            }
            log.info("已清除 xls 公式错误单元格，文件名={}，清除数量={}", fileName, clearedCount);
            return outputFile;
        } catch (Exception e) {
            log.error("清除 xls 公式错误单元格失败，文件名={}，将使用原始文件解析", fileName, e);
            return inputFile;
        }
    }

    /**
     * 文件版 .xlsx 清理：先轻量扫描是否存在错误单元格；存在才以「ZIP + StAX 流式」重写到临时文件，
     * 不存在则直接返回原文件，避免无错误时仍重写整包造成的磁盘浪费。全程流式、不构建内存工作簿对象模型。
     */
    private static File clearXlsxFormulaErrorCellsToTempFile(File inputFile, String fileName) {
        File outputFile = null;
        try {
            if (!xlsxHasErrorCells(inputFile)) {
                return inputFile;
            }
            outputFile = File.createTempFile("formula-error-clean-", ".xlsx", FileUtils.getTempDirectory());
            int clearedCount;
            try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(inputFile), StandardCharsets.UTF_8);
                 ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputFile))) {
                clearedCount = writeXlsxWithoutErrorCells(zipIn, zipOut);
            }
            log.info("已清除 xlsx 公式错误单元格，文件名={}，清除数量={}", fileName, clearedCount);
            return outputFile;
        } catch (Exception e) {
            FileUtils.deleteQuietly(outputFile);
            log.error("清除 xlsx 公式错误单元格失败，文件名={}，将使用原始文件解析", fileName, e);
            return inputFile;
        }
    }

    /**
     * 轻量扫描 .xlsx 是否存在公式错误单元格（t="e"），命中即提前返回，避免无错误时仍重写整个压缩包。
     */
    private static boolean xlsxHasErrorCells(File inputFile) throws IOException, XMLStreamException {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(inputFile), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (isWorksheetEntry(entry.getName()) && worksheetHasErrorCell(zipIn)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * StAX 流式扫描单个 worksheet XML 是否含 t="e" 的错误单元格，命中即返回，不做写出。
     */
    private static boolean worksheetHasErrorCell(InputStream in) throws XMLStreamException {
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        XMLStreamReader reader = null;
        try {
            reader = inFactory.createXMLStreamReader(new NonClosingInputStream(in), StandardCharsets.UTF_8.name());
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT
                        && "c".equals(reader.getLocalName())
                        && "e".equals(reader.getAttributeValue(null, "t"))) {
                    return true;
                }
            }
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignore) {
                    // ignore
                }
            }
        }
    }

    /**
     * 写出清理后的 xlsx ZIP 内容，返回删除的错误单元格数量。
     */
    private static int writeXlsxWithoutErrorCells(ZipInputStream zipIn, ZipOutputStream zipOut) throws IOException, XMLStreamException {
        int clearedCount = 0;
        ZipEntry entry;
        while ((entry = zipIn.getNextEntry()) != null) {
            zipOut.putNextEntry(new ZipEntry(entry.getName()));
            if (isWorksheetEntry(entry.getName())) {
                clearedCount += filterWorksheetErrorCells(zipIn, zipOut);
            } else {
                copyStream(zipIn, zipOut);
            }
            zipOut.closeEntry();
        }
        zipOut.finish();
        return clearedCount;
    }

    /**
     * 对单个 worksheet XML 做 StAX 流式过滤：删除 t="e" 的错误单元格，其余节点原样复制。
     *
     * @param in  worksheet XML 输入流（ZIP 当前条目，不会被关闭）
     * @param out 目标输出流（ZIP 当前条目，不会被关闭）
     * @return 删除的错误单元格数量
     */
    private static int filterWorksheetErrorCells(InputStream in, OutputStream out) throws XMLStreamException {
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        outFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);

        XMLStreamReader reader = null;
        XMLStreamWriter writer = null;
        int clearedCount = 0;
        try {
            // 用「不关闭底层流」包装器隔离：JDK StAX 的 close() 会连带关闭底层流，
            // 否则处理完一个 worksheet 后 ZIP 流被关，后续 getNextEntry() 会抛 Stream closed。
            reader = inFactory.createXMLStreamReader(new NonClosingInputStream(in), StandardCharsets.UTF_8.name());
            writer = outFactory.createXMLStreamWriter(new NonClosingOutputStream(out), StandardCharsets.UTF_8.name());
            // skipDepth>0 表示当前正处于被删除的错误单元格元素内部，跳过其所有子节点
            int skipDepth = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                if (skipDepth > 0) {
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        skipDepth++;
                    } else if (event == XMLStreamConstants.END_ELEMENT) {
                        skipDepth--;
                    }
                    continue;
                }
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if ("c".equals(reader.getLocalName()) && "e".equals(reader.getAttributeValue(null, "t"))) {
                            // 命中错误单元格 <c t="e">，跳过整个元素（含 <f>/<v>）
                            skipDepth = 1;
                            clearedCount++;
                        } else {
                            copyStartElement(reader, writer);
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        writer.writeEndElement();
                        break;
                    case XMLStreamConstants.CHARACTERS:
                    case XMLStreamConstants.SPACE:
                        writer.writeCharacters(reader.getText());
                        break;
                    case XMLStreamConstants.CDATA:
                        writer.writeCData(reader.getText());
                        break;
                    case XMLStreamConstants.COMMENT:
                        writer.writeComment(reader.getText());
                        break;
                    case XMLStreamConstants.START_DOCUMENT:
                        writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        writer.writeEndDocument();
                        break;
                    default:
                        break;
                }
            }
            writer.flush();
        } finally {
            // 仅关闭 StAX 读写器本身（不会关闭底层 ZIP 流）
            if (writer != null) {
                try {
                    writer.close();
                } catch (XMLStreamException ignore) {
                    // ignore
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignore) {
                    // ignore
                }
            }
        }
        return clearedCount;
    }

    /**
     * 复制 StAX 当前 START_ELEMENT 的元素名与属性到写出器（命名空间交由 repairing 模式自动补全）。
     */
    private static void copyStartElement(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        String prefix = reader.getPrefix();
        String namespaceUri = reader.getNamespaceURI();
        String localName = reader.getLocalName();
        if (StringUtils.isNotEmpty(prefix)) {
            writer.writeStartElement(prefix, localName, namespaceUri);
        } else if (StringUtils.isNotEmpty(namespaceUri)) {
            writer.writeStartElement("", localName, namespaceUri);
        } else {
            writer.writeStartElement(localName);
        }
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attrPrefix = reader.getAttributePrefix(i);
            String attrNs = reader.getAttributeNamespace(i);
            String attrLocal = reader.getAttributeLocalName(i);
            String attrValue = reader.getAttributeValue(i);
            if (StringUtils.isNotEmpty(attrPrefix) && StringUtils.isNotEmpty(attrNs)) {
                writer.writeAttribute(attrPrefix, attrNs, attrLocal, attrValue);
            } else if (StringUtils.isNotEmpty(attrNs)) {
                writer.writeAttribute(attrNs, attrLocal, attrValue);
            } else {
                writer.writeAttribute(attrLocal, attrValue);
            }
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    /**
     * 包装输入流并屏蔽其 close()，防止 StAX 读取器关闭时连带关闭底层 ZIP 流。
     */
    private static final class NonClosingInputStream extends FilterInputStream {
        NonClosingInputStream(InputStream in) {
            super(in);
        }
        @Override
        public void close() {
            // no-op：底层 ZIP 流由外层 try-with-resources 统一关闭
        }
    }

    /**
     * 包装输出流并屏蔽其 close()，防止 StAX 写出器关闭时连带关闭底层 ZIP 流；同时直写以避免逐字节性能损耗。
     */
    private static final class NonClosingOutputStream extends FilterOutputStream {
        NonClosingOutputStream(OutputStream out) {
            super(out);
        }
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }
        @Override
        public void close() {
            // no-op：底层 ZIP 流由外层 try-with-resources 统一关闭
        }
    }

    /**
     * 判断 ZIP 条目是否为 worksheet 数据 XML（xl/worksheets/sheetN.xml），排除其关系文件目录(_rels)。
     */
    private static boolean isWorksheetEntry(String entryName) {
        return entryName != null
                && entryName.startsWith("xl/worksheets/")
                && entryName.endsWith(".xml")
                && !entryName.contains("/_rels/");
    }

    /**
     * 轻量判断文件是否为真正的旧版 .xls（OLE2 复合文档）格式，仅读文件头魔数，不载入工作簿。
     * 只认文件头魔数 {@link #XLS_MAGIC}，不依赖文件名后缀：所有 BIFF 格式的 .xls 文件头都以该魔数开头，
     * .xlsx 是 ZIP（魔数 PK..），因此即使把 .xlsx 改名为 .xls 也不会被误判。
     */
    private static boolean isXlsFormat(File file) throws IOException {
        return startsWithMagic(file, XLS_MAGIC);
    }

    /**
     * 轻量判断文件是否为 .xlsx（ZIP 包）格式，仅读文件头魔数。
     */
    private static boolean isXlsxFormat(File file) throws IOException {
        return startsWithMagic(file, XLSX_MAGIC);
    }

    private static boolean startsWithMagic(byte[] fileBytes, byte[] magic) {
        if (fileBytes.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (fileBytes[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean startsWithMagic(File file, byte[] magic) throws IOException {
        if (file.length() < magic.length) {
            return false;
        }
        byte[] header = new byte[magic.length];
        try (InputStream inputStream = new FileInputStream(file)) {
            int readLength = inputStream.read(header);
            if (readLength < magic.length) {
                return false;
            }
        }
        return startsWithMagic(header, magic);
    }

    /**
     * 清空工作簿中的错误单元格，返回清除数量。
     */
    private static int clearWorkbookErrorCells(Workbook workbook) {
        int clearedCount = 0;
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            for (Row row : sheet) {
                if (row == null) {
                    continue;
                }
                for (Cell cell : row) {
                    if (cell != null && isErrorCell(cell)) {
                        cell.setBlank();
                        clearedCount++;
                    }
                }
            }
        }
        return clearedCount;
    }

    /**
     * 判断单元格的值是否为错误类型（普通错误单元格，或缓存结果为错误的公式单元格）。
     *
     * @param cell 单元格
     * @return true 表示该单元格值为 #VALUE!/#REF! 等错误
     */
    private static boolean isErrorCell(Cell cell) {
        CellType valueType = cell.getCellType();
        if (valueType == CellType.FORMULA) {
            valueType = cell.getCachedFormulaResultType();
        }
        return valueType == CellType.ERROR;
    }
}
