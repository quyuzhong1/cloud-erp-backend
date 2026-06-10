package com.erp.server.file.core;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.erp.server.file.handler.FileRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractDynamicHeadersFileEventHandler<P> implements FileEventHandler {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void handle(FileTask fileTask) {
        Path tempPath = null;
        try {
            P params = resolveExportParams(fileTask);
            tempPath = ExportTempFilesHandler.createTempPath(FileRegistry.getStorageTmpdir(), ".xlsx",
                    fileTask.getUniqueWithFileName());
            int total = writePagedDynamicHeadersExcel(tempPath.toFile(), params, fileTask.getFileName());
            fileTask.setCount(total);
            String url = FastDFSClientUtil.streamUploadFile(tempPath.toFile(), buildDownloadFileName(fileTask), null);
            fileTask.setFileUrl(url);
        } catch (Exception e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        } finally {
            ExportTempFilesHandler.deleteQuietly(tempPath);
        }
    }

    /**
     * 解析导出查询参数。默认根据 {@code AbstractDynamicHeadersFileEventHandler<P>} 的泛型 {@code P}
     * 从 {@link FileTask#getMetaInfo()} 反序列化。
     */
    @SuppressWarnings("unchecked")
    protected P resolveExportParams(FileTask fileTask) {
        Type paramType = resolvePagingParamType(getClass());
        if (paramType == null) {
            throw new IllegalStateException(getClass().getName()
                    + " 无法推断分页参数类型 P，请确保直接继承 AbstractDynamicHeadersFileEventHandler<P> 并指定具体 P，或重写 resolveExportParams");
        }
        JavaType javaType = objectMapper.getTypeFactory().constructType(paramType);
        return (P) readValue(fileTask.getMetaInfo(), javaType);
    }

    /**
     * 从当前 Handler 类解析 {@link AbstractDynamicHeadersFileEventHandler} 的类型参数 P。
     */
    static Type resolvePagingParamType(Class<?> handlerClass) {
        ResolvableType rt = ResolvableType.forClass(handlerClass);
        while (rt != ResolvableType.NONE) {
            if (rt.getRawClass() != null && rt.getRawClass() == AbstractDynamicHeadersFileEventHandler.class) {
                ResolvableType param = rt.getGeneric(0);
                if (param != ResolvableType.NONE && null != param.getType()) {
                    return param.getType();
                }
                return null;
            }
            rt = rt.getSuperType();
        }
        return null;
    }

    /**
     * 兼容旧调用：仍可将多页合并为 {@link DynamicExcelDTO}（大数据量慎用）。
     *
     * @deprecated 异步导出任务已改为分页写入临时文件，请使用 {@link #resolveExportParams(FileTask)} + 分页写出链路。
     */
    @Deprecated
    protected DynamicExcelDTO getData(FileTask fileTask) {
        return listSeqData(resolveExportParams(fileTask));
    }

    private String buildDownloadFileName(FileTask fileTask) {
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        return date + name + ".xlsx";
    }

    private String resolveSheetName(String defaultSheetName, List<DynamicExcelDTO> pageList) {
        List<String> sheetName = getSheetName();
        if (!CollectionUtils.isEmpty(sheetName) && !isBlank(sheetName.get(0))) {
            return sheetName.get(0);
        }
        String dataSheetName = resolveDataSheetName(pageList);
        return isBlank(dataSheetName) ? defaultSheetName : dataSheetName;
    }

    private int writePagedDynamicHeadersExcel(File outFile, P params, String defaultSheetName) throws IOException {
        PagingDTO<P> dto = new PagingDTO<>();
        dto.setPageSize(getPageSize());
        dto.setCurrPage(1);
        dto.setParams(params);

        PagingVO<DynamicExcelDTO> pageData = getPageData(dto);
        if (pageData == null) {
            throw new ServiceException("导出数据不能为空");
        }
        List<DynamicExcelDTO> pageList = pageData.getList();
        LinkedHashMap<String, String> headers = resolveHeaders(pageList);
        if (CollUtil.isEmpty(headers)) {
            throw new ServiceException("导出数据不能为空");
        }
        List<List<String>> header = convertHeadList(headers.values());
        String sheetName = resolveSheetName(defaultSheetName, pageList);

        int totalRows = 0;
        int sheetNo = 0;
        long rowsInSheet = 0;
        int maxRowsPerSheet = maxRowsPerSheet();
        int maxSheetNum = FileRegistry.getMaxSheetNum();
        ExcelPrintUtils excelPrintUtils = new ExcelPrintUtils();
        try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
            ExcelWriter excelWriter = excelPrintUtils.openDynamicHeadersWriter(outputStream, header);
            try {
                WriteSheet writeSheet = buildWriteSheet(sheetNo, sheetName, header);
                while (true) {
                    List<List<Object>> rows = convertPageDataList(pageList);
                    int idx = 0;
                    if (rows.isEmpty() && totalRows == 0) {
                        excelWriter.write(rows, writeSheet);
                    }
                    while (idx < rows.size()) {
                        if (rowsInSheet >= maxRowsPerSheet) {
                            sheetNo++;
                            if (sheetNo >= maxSheetNum) {
                                throw new BusinessException("导出数据超过当前系统可承载的 sheet 数，请缩小筛选范围导出。");
                            }
                            writeSheet = buildWriteSheet(sheetNo, sheetName, header);
                            rowsInSheet = 0;
                        }
                        int room = maxRowsPerSheet - (int) rowsInSheet;
                        int take = Math.min(room, rows.size() - idx);
                        List<List<Object>> batch = rows.subList(idx, idx + take);
                        excelWriter.write(batch, writeSheet);
                        totalRows += take;
                        rowsInSheet += take;
                        idx += take;
                    }

                    if (pageData.getTotalCount() <= dto.getCurrPage() * getPageSize()) {
                        break;
                    }
                    dto.setCurrPage(dto.getCurrPage() + 1);
                    pageData = getPageData(dto);
                    if (pageData == null) {
                        throw new ServiceException("导出数据不能为空");
                    }
                    pageList = pageData.getList();
                }
            } finally {
                excelWriter.finish();
            }
        }
        return totalRows;
    }

    private String resolveDataSheetName(List<DynamicExcelDTO> pageList) {
        if (CollectionUtils.isEmpty(pageList)) {
            return null;
        }
        for (DynamicExcelDTO dynamicExcelDTO : pageList) {
            if (dynamicExcelDTO != null && !isBlank(dynamicExcelDTO.getSheetName())) {
                return dynamicExcelDTO.getSheetName();
            }
        }
        return null;
    }

    private WriteSheet buildWriteSheet(int sheetNo, String sheetName, List<List<String>> header) {
        String currentSheetName = sheetNo == 0 ? sheetName : sheetName + (sheetNo + 1);
        return EasyExcel.writerSheet(sheetNo, currentSheetName).head(header).build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int maxRowsPerSheet() {
        return Math.max(1, FileRegistry.getSheetMaxRows());
    }

    private LinkedHashMap<String, String> resolveHeaders(List<DynamicExcelDTO> pageList) {
        if (CollectionUtils.isEmpty(pageList)) {
            return null;
        }
        for (DynamicExcelDTO dynamicExcelDTO : pageList) {
            if (dynamicExcelDTO != null && !CollUtil.isEmpty(dynamicExcelDTO.getHeaders())) {
                return dynamicExcelDTO.getHeaders();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public DynamicExcelDTO listSeqData(P p) {
        DynamicExcelDTO excelDTO = new DynamicExcelDTO();
        PagingDTO<P> dto = new PagingDTO<>();
        dto.setPageSize(getPageSize());
        dto.setCurrPage(1);
        ArrayList<LinkedHashMap<String, Object>> result = new ArrayList<>();
        boolean hasNext = true;
        while (hasNext) {
            dto.setParams(p);
            PagingVO<DynamicExcelDTO> data = getPageData(dto);
            if (!CollectionUtils.isEmpty(data.getList())) {
                List<DynamicExcelDTO> list = (List<DynamicExcelDTO>) data.getList();
                for (DynamicExcelDTO dynamicExcelDTO : list) {
                    excelDTO.setHeaders(dynamicExcelDTO.getHeaders());
                    result.addAll(dynamicExcelDTO.getData());
                }
            }
            int totalCount = data.getTotalCount();
            if (totalCount <= dto.getCurrPage() * getPageSize()) {
                hasNext = false;
            }
            dto.setCurrPage(dto.getCurrPage() + 1);
        }
        excelDTO.setData(result);
        return excelDTO;
    }

    protected abstract PagingVO<DynamicExcelDTO> getPageData(PagingDTO<P> dto);

    protected int getPageSize() {
        return 1000;
    }

    private List<List<Object>> convertPageDataList(List<DynamicExcelDTO> pageList) {
        List<List<Object>> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(pageList)) {
            return result;
        }
        for (DynamicExcelDTO dynamicExcelDTO : pageList) {
            if (dynamicExcelDTO == null || CollectionUtils.isEmpty(dynamicExcelDTO.getData())) {
                continue;
            }
            result.addAll(convertDataList(dynamicExcelDTO.getData()));
        }
        return result;
    }

    private List<List<Object>> convertDataList(List<LinkedHashMap<String, Object>> data) {
        List<List<Object>> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(data)) {
            return result;
        }
        for (LinkedHashMap<String, Object> map : data) {
            result.add((new ArrayList<>(map.values())));
        }
        return result;
    }

    private List<List<String>> convertHeadList(Collection<String> headList) {
        return headList.stream().map(Arrays::asList).collect(Collectors.toList());
    }

    public <R> R readValue(String params, TypeReference<R> type) {
        try {
            return objectMapper.readValue(params, type);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    protected <R> R readValue(String params, JavaType javaType) {
        try {
            return objectMapper.readValue(params, javaType);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
