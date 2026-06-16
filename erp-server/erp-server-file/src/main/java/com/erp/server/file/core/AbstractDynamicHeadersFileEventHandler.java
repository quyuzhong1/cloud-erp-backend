package com.erp.server.file.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.file.entity.FileTask;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractDynamicHeadersFileEventHandler<P> implements FileEventHandler {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void handle(FileTask fileTask) {
        P params = resolveExportParams(fileTask);
        String displayName = buildDownloadFileName(fileTask);
        ExportTempFilesHandler.exportToTempAndUpload(fileTask, ".xlsx", displayName,
                outFile -> writePagedDynamicHeadersExcel(outFile, params, fileTask.getFileName()));
    }

    /**
     * 解析导出查询参数。默认根据 {@code AbstractDynamicHeadersFileEventHandler<P>} 的泛型 {@code P}
     * 从 {@link FileTask#getMetaInfo()} 反序列化。
     */
    @SuppressWarnings("unchecked")
    protected P resolveExportParams(FileTask fileTask) {
        Type paramType = resolvePagingParamType(getClass());
        if (paramType == null) {
            throw new ServiceException(getClass().getName()
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

    /**
     * sheet 名解析顺序：Handler 重写的 {@link #getSheetName()} → 数据侧 {@link DynamicExcelDTO#getSheetName()}
     * → {@code defaultSheetName}（文件名）。因此 Handler 未重写 {@code getSheetName()} 时不会丢失 sheet 名，
     * 仍会读取每批 {@link DynamicExcelDTO} 上业务设置的 sheet 名。
     */
    private String resolveSheetName(String defaultSheetName, List<DynamicExcelDTO> pageList) {
        List<String> sheetName = getSheetName();
        if (!CollectionUtils.isEmpty(sheetName) && CharSequenceUtil.isNotBlank(sheetName.get(0))) {
            return sheetName.get(0);
        }
        String dataSheetName = resolveDataSheetName(pageList);
        return CharSequenceUtil.isBlank(dataSheetName) ? defaultSheetName : dataSheetName;
    }

    private int writePagedDynamicHeadersExcel(File outFile, P params, String defaultSheetName) throws IOException {
        PagingDTO<P> dto = new PagingDTO<>();
        int pageSize = getPageSize();
        dto.setPageSize(pageSize);
        dto.setCurrPage(1);
        dto.setParams(params);

        PagingVO<DynamicExcelDTO> pageData = getPageData(dto);
        if (pageData == null) {
            throw new ServiceException("导出分页查询失败，页码=1");
        }
        int totalCount = pageData.getTotalCount();
        int totalPage = computeTotalPage(totalCount, pageSize);
        // 表头以首页为准、不在后续分页重复 mergeHeaders：本链路为标准 OFFSET 分页（totalCount>0 时首页必有数据），
        // 且各页 DynamicExcelDTO 由同一查询/同一响应类产出，动态列集合在分页间保持一致（仅数据行不同），故首页表头即为全量表头。
        // 后续页若出现首页未包含的新列，由 ensureNoNewHeaderKeys 在写出循环中显式抛错兜底，而非静默丢列。
        List<DynamicExcelDTO> pageList = pageData.getList();
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        mergeHeaders(headers, pageList);
        if (CollUtil.isEmpty(headers)) {
            // 区分两类根因，避免「分页异常」被笼统归为「无数据」掩盖真实问题：
            // 1) totalCount<=0：查询结果确实为空，属正常的「无可导出数据」；
            // 2) totalCount>0 但首页表头/数据为空：首页与总数不一致（Feign 分页异常、并发删数、游标/OFFSET 不一致等），属数据异常，需排查上游分页查询。
            if (totalCount <= 0) {
                throw new ServiceException("导出数据不能为空");
            }
            throw new ServiceException("导出失败：总记录数=" + totalCount + " 但首页(页码=1, pageSize=" + pageSize
                    + ")未返回表头/数据，疑似分页查询异常或数据并发变更，请稍后重试或联系开发排查上游分页接口。");
        }
        List<List<String>> header = convertHeadList(headers.values());
        String sheetName = resolveSheetName(defaultSheetName, pageList);

        int totalRows = 0;
        int sheetNo = 0;
        long rowsInSheet = 0;
        int maxRowsPerSheet = maxRowsPerSheet();
        int maxSheetNum = FileRegistry.maxSheetNumOrDefault();
        ExcelPrintUtils excelPrintUtils = new ExcelPrintUtils();
        try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
            ExcelWriter excelWriter = excelPrintUtils.openDynamicHeadersWriter(outputStream, header);
            try {
                WriteSheet writeSheet = buildWriteSheet(sheetNo, sheetName, header);
                for (int pageNo = 1; pageNo <= totalPage; pageNo++) {
                    if (pageNo > 1) {
                        dto.setCurrPage(pageNo);
                        pageData = getPageData(dto);
                        if (pageData == null) {
                            throw new ServiceException("导出分页查询失败，页码=" + pageNo);
                        }
                        pageList = pageData.getList();
                        // 中间页空列表防静默丢数：pageNo 在 [1,totalPage] 内本应有数据，返回空属上游分页异常/数据并发变更；
                        // 若放过会以不完整 Excel「成功」结束、totalRows 偏小，故显式失败（首页空已在上方按 totalCount 兜底）。
                        if (CollectionUtils.isEmpty(pageList)) {
                            throw new ServiceException("导出分页数据缺失：页码=" + pageNo + "（共 " + totalPage
                                    + " 页）返回空列表，但 totalCount=" + totalCount + " 预期仍有数据，疑似分页查询异常或数据并发变更，请重试或排查上游分页接口。");
                        }
                    }
                    ensureNoNewHeaderKeys(headers, pageList, pageNo);
                    List<List<Object>> rows = convertPageDataList(pageList, headers.keySet());
                    if (!CollectionUtils.isEmpty(pageList) && rows.isEmpty() && totalCount > 0) {
                        throw new ServiceException("导出数据存在空行，请检查查询结果（页码=" + pageNo + "）");
                    }
                    if (rows.isEmpty() && totalRows == 0) {
                        excelWriter.write(rows, writeSheet);
                    }
                    for (int idx = 0; idx < rows.size();) {
                        if (rowsInSheet >= maxRowsPerSheet) {
                            sheetNo++;
                            if (sheetNo >= maxSheetNum) {
                                throw new ServiceException("导出数据超过当前系统可承载的 sheet 数，请缩小筛选范围导出。");
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
                    // 上游单页超发（返回行数超过 pageSize）时已写满 totalCount：提前结束，
                    // 避免后续 pageNo 取到空列表被上方中间页守卫误判为「数据缺失」。
                    if (totalRows >= totalCount) {
                        break;
                    }
                }
                if (totalCount > 0 && totalRows == 0) {
                    throw new ServiceException("导出失败：totalCount=" + totalCount
                            + " 但未写入任何数据行，疑似分页查询异常或数据全为空行，请检查上游分页接口。");
                }
            } finally {
                excelWriter.finish();
            }
        }
        return totalRows;
    }

    private void mergeHeaders(LinkedHashMap<String, String> target, List<DynamicExcelDTO> pageList) {
        if (CollectionUtils.isEmpty(pageList)) {
            return;
        }
        for (DynamicExcelDTO dynamicExcelDTO : pageList) {
            if (dynamicExcelDTO == null || CollUtil.isEmpty(dynamicExcelDTO.getHeaders())) {
                continue;
            }
            dynamicExcelDTO.getHeaders().forEach(target::putIfAbsent);
        }
    }

    /**
     * 校验后续分页是否引入了首页表头未包含的动态列。
     * <p>
     * 表头按首页一次性确定，本链路约定各页动态列一致；若后续页出现新列，
     * 继续按既有表头写出会静默丢列，故此处显式抛 {@link ServiceException} 提示，避免导出结果缺列且难排查。
     */
    private void ensureNoNewHeaderKeys(LinkedHashMap<String, String> headers, List<DynamicExcelDTO> pageList, int pageNo) {
        if (CollectionUtils.isEmpty(pageList)) {
            return;
        }
        for (DynamicExcelDTO dynamicExcelDTO : pageList) {
            if (dynamicExcelDTO == null || CollUtil.isEmpty(dynamicExcelDTO.getHeaders())) {
                continue;
            }
            for (String key : dynamicExcelDTO.getHeaders().keySet()) {
                if (!headers.containsKey(key)) {
                    throw new ServiceException("导出失败：第 " + pageNo + " 页出现首页表头未包含的动态列[" + key
                            + "]，各页动态列不一致，请缩小筛选范围后重试或联系开发处理。");
                }
            }
        }
    }

    private int computeTotalPage(int totalCount, int pageSize) {
        if (totalCount <= 0) {
            return 1;
        }
        return (int) ((totalCount + (long) pageSize - 1) / pageSize);
    }

    private String resolveDataSheetName(List<DynamicExcelDTO> pageList) {
        if (CollectionUtils.isEmpty(pageList)) {
            return null;
        }
        for (DynamicExcelDTO dynamicExcelDTO : pageList) {
            if (dynamicExcelDTO != null && CharSequenceUtil.isNotBlank(dynamicExcelDTO.getSheetName())) {
                return dynamicExcelDTO.getSheetName();
            }
        }
        return null;
    }

    private WriteSheet buildWriteSheet(int sheetNo, String sheetName, List<List<String>> header) {
        String currentSheetName = sheetNo == 0 ? sheetName : sheetName + (sheetNo + 1);
        return EasyExcel.writerSheet(sheetNo, currentSheetName).head(header).build();
    }

    private int maxRowsPerSheet() {
        return Math.max(1, FileRegistry.sheetMaxRowsOrDefault());
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
        int totalCount = 0;
        while (hasNext) {
            dto.setParams(p);
            PagingVO<DynamicExcelDTO> data = getPageData(dto);
            if (data == null) {
                throw new ServiceException("导出分页查询失败，页码=" + dto.getCurrPage());
            }
            if (totalCount == 0) {
                totalCount = data.getTotalCount();
            }
            int pageListSize = CollectionUtils.isEmpty(data.getList()) ? 0 : data.getList().size();
            long coveredBeforeThisPage = (long) (dto.getCurrPage() - 1) * getPageSize();
            if (pageListSize == 0 && coveredBeforeThisPage < totalCount) {
                throw new ServiceException("导出分页数据缺失：页码=" + dto.getCurrPage()
                        + " 返回空列表，但 totalCount=" + totalCount + " 预期仍有数据，疑似分页查询异常或数据并发变更，请重试或排查上游分页接口。");
            }
            if (!CollectionUtils.isEmpty(data.getList())) {
                int rowsBefore = result.size();
                List<DynamicExcelDTO> list = (List<DynamicExcelDTO>) data.getList();
                for (DynamicExcelDTO dynamicExcelDTO : list) {
                    excelDTO.setHeaders(dynamicExcelDTO.getHeaders());
                    if (dynamicExcelDTO != null && !CollectionUtils.isEmpty(dynamicExcelDTO.getData())) {
                        result.addAll(dynamicExcelDTO.getData());
                    }
                }
                if (result.size() == rowsBefore) {
                    throw new ServiceException("导出数据存在空行，请检查查询结果（页码=" + dto.getCurrPage() + "）");
                }
            }
            // 与主写循环对齐：已写满 totalCount（含上游单页超发）即结束，避免空页被误判为缺数。
            if (result.size() >= totalCount || totalCount <= (long) dto.getCurrPage() * getPageSize()) {
                hasNext = false;
            }
            dto.setCurrPage(dto.getCurrPage() + 1);
        }
        if (totalCount > 0 && result.isEmpty()) {
            throw new ServiceException("导出失败：totalCount=" + totalCount
                    + " 但未写入任何数据行，疑似分页查询异常或数据全为空行，请检查上游分页接口。");
        }
        excelDTO.setData(result);
        return excelDTO;
    }

    protected abstract PagingVO<DynamicExcelDTO> getPageData(PagingDTO<P> dto);

    protected int getPageSize() {
        // 动态表头单行体积通常大于固定模板行，使用专用且更保守的批次（默认 1000），避免大宽表导出内存/超时回归
        return FileRegistry.dynamicExportPageSize();
    }

    private List<List<Object>> convertPageDataList(List<DynamicExcelDTO> pageList, Collection<String> headerKeys) {
        List<List<Object>> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(pageList)) {
            return result;
        }
        for (DynamicExcelDTO dynamicExcelDTO : pageList) {
            if (dynamicExcelDTO == null || CollectionUtils.isEmpty(dynamicExcelDTO.getData())) {
                continue;
            }
            result.addAll(convertDataList(dynamicExcelDTO.getData(), headerKeys));
        }
        return result;
    }

    private List<List<Object>> convertDataList(List<LinkedHashMap<String, Object>> data, Collection<String> headerKeys) {
        List<List<Object>> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(data)) {
            return result;
        }
        for (LinkedHashMap<String, Object> map : data) {
            List<Object> row = new ArrayList<>(headerKeys.size());
            for (String key : headerKeys) {
                row.add(map.get(key));
            }
            result.add(row);
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
            ServiceException ex = new ServiceException("导出参数解析失败：" + e.getOriginalMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    protected <R> R readValue(String params, JavaType javaType) {
        try {
            return objectMapper.readValue(params, javaType);
        } catch (JsonProcessingException e) {
            ServiceException ex = new ServiceException("导出参数解析失败：" + e.getOriginalMessage());
            ex.initCause(e);
            throw ex;
        }
    }
}
