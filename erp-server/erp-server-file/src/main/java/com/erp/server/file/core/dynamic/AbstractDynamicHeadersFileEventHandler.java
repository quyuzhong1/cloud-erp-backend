package com.erp.server.file.core.dynamic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.file.entity.FileTask;
import com.erp.server.file.core.ExportTempFilesHandler;
import com.erp.server.file.core.FileEventHandler;
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
                outFile -> writePagedDynamicHeadersExcel(outFile, params, resolveSheetBaseName(fileTask)));
    }

    /**
     * sheet 基础名（sheet 标签用；多 sheet 溢出时后续 sheet 在其后追加序号）。
     * 默认取文件名；子类可重写（如还原旧版「日期+文件名」）。仅当未重写 {@link #getSheetName()}
     * 且数据侧未设置 {@link DynamicExcelDTO#getSheetName()} 时生效（见 {@link #resolveSheetName}）。
     */
    protected String resolveSheetBaseName(FileTask fileTask) {
        return fileTask.getFileName();
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
        // 用 forClass(baseType, implementationClass) 跨中间继承层把类型变量 P 绑定到具体类型；
        // 返回值必须用 resolve()（已绑定的具体 Class），不能用 getType()。
        // 注意：getType() 返回声明处原始 Type，跨继承层时即未解析的 TypeVariable，
        // Jackson constructType 会退回 Object 并反序列化成 LinkedHashMap，在使用处抛 ClassCastException。
        // 本仓库所有导出 Handler 的 P 均为非参数化 DTO，resolve() 不存在嵌套泛型丢失问题；
        // 若将来出现参数化 P，应改为由 ResolvableType 递归构造 Jackson JavaType，而非退回 getType()。
        ResolvableType param = ResolvableType.forClass(AbstractDynamicHeadersFileEventHandler.class, handlerClass).getGeneric(0);
        if (param == ResolvableType.NONE || param.resolve() == null) {
            return null;
        }
        return param.resolve();
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
        List<List<String>> header = convertHeadList(headers.values(), firstRowName());
        String sheetName = resolveSheetName(defaultSheetName, pageList);

        int totalRows = 0;
        int sheetNo = 0;
        long rowsInSheet = 0;
        int maxRowsPerSheet = maxRowsPerSheet();
        int maxSheetNum = maxSheetCount();
        ExcelPrintUtils excelPrintUtils = new ExcelPrintUtils();
        try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
            ExcelWriter excelWriter = excelPrintUtils.openDynamicHeadersWriter(outputStream, header, dynamicHeaderCellStyleStrategy());
            try {
                WriteSheet writeSheet = buildWriteSheet(sheetNo, sheetName, header);
                // 「按 sheet 逐行加工」状态：方法局部持有，单次导出独占，规避单例 Handler 字段并发；sheet 翻页时复位。
                Object sheetState = newSheetState();
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
                    List<LinkedHashMap<String, Object>> rowMaps = flattenPageData(pageList);
                    if (!CollectionUtils.isEmpty(pageList) && rowMaps.isEmpty() && totalCount > 0) {
                        throw new ServiceException("导出数据存在空行，请检查查询结果（页码=" + pageNo + "）");
                    }
                    if (rowMaps.isEmpty() && totalRows == 0) {
                        excelWriter.write(Collections.emptyList(), writeSheet);
                    }
                    for (int idx = 0; idx < rowMaps.size();) {
                        if (rowsInSheet >= maxRowsPerSheet) {
                            sheetNo++;
                            if (sheetNo >= maxSheetNum) {
                                if (maxSheetNum <= 1) {
                                    throw new ServiceException("导出数据量超过单 sheet 上限 " + maxRowsPerSheet
                                            + " 行，请缩小筛选范围导出。");
                                }
                                throw new ServiceException("导出数据超过当前系统可承载的 sheet 数，请缩小筛选范围导出。");
                            }
                            writeSheet = buildWriteSheet(sheetNo, sheetName, header);
                            rowsInSheet = 0;
                            sheetState = newSheetState();
                        }
                        int room = maxRowsPerSheet - (int) rowsInSheet;
                        int take = Math.min(room, rowMaps.size() - idx);
                        List<List<Object>> batch = new ArrayList<>(take);
                        for (int k = idx; k < idx + take; k++) {
                            LinkedHashMap<String, Object> rowMap = rowMaps.get(k);
                            decorateSheetRow(rowMap, sheetState, sheetNo);
                            batch.add(toRow(rowMap, headers.keySet()));
                        }
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
        // 末页 partial：实际写入行数 < 首查 totalCount（多因导出期间并发删除/数据漂移），不视为失败
        // （fileTask.count 已回填实际行数），与固定模板分页路径对称地显式告警，避免静默丢数难感知。
        if (totalCount > 0 && totalRows < totalCount) {
            log.warn("动态表头导出末页数据不足：handler={} 预期 totalCount={} 实际写入 totalRows={}，疑似导出期间数据并发变更",
                    getClass().getName(), totalCount, totalRows);
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

    /**
     * 单 sheet 数据行上限。默认 {@code file.storage.sheetMaxRows}（多 sheet 分页，默认 10 万）。
     * 单 sheet 变体 {@link AbstractSingleSheetDynamicHeadersFileEventHandler} 重写为 {@code file.storage.singleSheetMaxRows}。
     */
    protected int maxRowsPerSheet() {
        return Math.max(1, FileRegistry.sheetMaxRowsOrDefault());
    }

    /**
     * 最多数据 sheet 数。默认 {@code file.storage.maxSheetNum}；
     * 单 sheet 变体重写为 1：达到 {@link #maxRowsPerSheet()} 即显式失败、不再开新 sheet。
     */
    protected int maxSheetCount() {
        return FileRegistry.maxSheetNumOrDefault();
    }

    protected abstract PagingVO<DynamicExcelDTO> getPageData(PagingDTO<P> dto);

    /**
     * 动态表头导出的单元格样式策略钩子，默认返回 {@code null}（沿用通用样式，不影响其它动态表头单据）。
     * 子类可重写返回自定义样式（如旧版灰底加粗表头），仅作用于当前 Handler。
     */
    protected HorizontalCellStyleStrategy dynamicHeaderCellStyleStrategy() {
        return null;
    }

    protected int getPageSize() {
        // 动态表头单行体积通常大于固定模板行，使用专用且更保守的批次（默认 1000），避免大宽表导出内存/超时回归
        return FileRegistry.dynamicExportPageSize();
    }

    /**
     * 将本页各 {@link DynamicExcelDTO} 的数据行平铺为有序行 Map 列表，便于逐行（按 sheet 切片）写出与加工。
     * 行 Map 可含表头未声明的辅助列（如去重用 id），写出时仅取表头列。
     */
    private List<LinkedHashMap<String, Object>> flattenPageData(List<DynamicExcelDTO> pageList) {
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(pageList)) {
            return result;
        }
        for (DynamicExcelDTO dynamicExcelDTO : pageList) {
            if (dynamicExcelDTO == null || CollectionUtils.isEmpty(dynamicExcelDTO.getData())) {
                continue;
            }
            result.addAll(dynamicExcelDTO.getData());
        }
        return result;
    }

    private List<Object> toRow(LinkedHashMap<String, Object> map, Collection<String> headerKeys) {
        List<Object> row = new ArrayList<>(headerKeys.size());
        for (String key : headerKeys) {
            row.add(map.get(key));
        }
        return row;
    }

    /**
     * 「按 sheet 逐行加工」的 sheet 级状态工厂。默认返回 {@code null}（无状态）。
     * 每开启一个新 sheet 调用一次，返回对象作为方法局部状态在写出循环内持有：
     * 单次导出独占、方法局部天然线程安全，规避单例 {@code @Component} Handler 字段并发串数据。
     */
    protected Object newSheetState() {
        return null;
    }

    /**
     * 「按 sheet 逐行加工」回调：写出每行前调用，可基于 {@code sheetState} 做当前 sheet 内的判重/置空等。
     * 默认空实现。{@code rowMap} 可能含表头未声明的辅助列（如去重用 id），对其修改不影响最终列集（仅写出表头列）。
     *
     * @param rowMap     当前数据行（有序），可原地修改
     * @param sheetState 当前 sheet 的状态对象（来自 {@link #newSheetState()}）
     * @param sheetNo    当前 sheet 序号（从 0 开始）
     */
    protected void decorateSheetRow(LinkedHashMap<String, Object> rowMap, Object sheetState, int sheetNo) {
    }

    /**
     * 把扁平列名转为 EasyExcel 表头。
     * <p>
     * {@code firstRowName} 为空：每列单级表头 {@code [列名]}，表头仅列名一行（默认，不影响其它单据）。
     * {@code firstRowName} 非空：每列两级表头 {@code [首行名, 列名]}，首级各列同值，EasyExcel 横向合并出一行标题行；
     * 该 {@code header} 被每张 sheet 复用（见 {@link #buildWriteSheet}），故多 sheet 溢出时每张 sheet 均带标题行。
     */
    private List<List<String>> convertHeadList(Collection<String> headList, String firstRowName) {
        boolean withTitle = CharSequenceUtil.isNotBlank(firstRowName);
        return headList.stream()
                .map(h -> withTitle ? Arrays.asList(firstRowName, h) : Arrays.asList(h))
                .collect(Collectors.toList());
    }

    /**
     * 表头首行单据名（合并单元格，位于列名行之上）。与 {@link #getSheetName()}（sheet 标签名）职责分离：
     * 前者控制表内首行标题，后者控制 sheet 标签。默认 {@code null}（无标题行，表头仅列名一行，不影响未重写的单据）。
     * 子类可重写返回首行文本以还原「首行单据名」场景。
     */
    protected String firstRowName() {
        return null;
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
