package com.erp.server.file.core;

import cn.hutool.core.collection.CollUtil;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractDynamicHeadersFileEventHandler<P> implements FileEventHandler {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void handle(FileTask fileTask) {
        DynamicExcelDTO excelDTO = getData(fileTask);
        LinkedHashMap<String, String> headers = excelDTO.getHeaders();
        if (CollUtil.isEmpty(headers)) {
            throw new ServiceException("导出数据不能为空");
        }
        List<List<String>> header = convertHeadList(headers.values());
        List<List<Object>> data = convertDataList(excelDTO.getData());
        fileTask.setCount(excelDTO.getData().size());
        StringBuilder sb = new StringBuilder();
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        sb.append(".xlsx");
        List<String> sheetName = getSheetName();
        String sheet = CollectionUtils.isEmpty(sheetName) ? name : sheetName.get(0);
        Path tempPath = null;
        try {
            tempPath = ExportTempFilesHandler.createTempPath(FileRegistry.getStorageTmpdir(), ".xlsx",fileTask.getUniqueWithFileName());
            new ExcelPrintUtils().exportDynamicHeadersExcelToFile(tempPath.toFile(), sheet, header, data);
            String url = FastDFSClientUtil.streamUploadFile(tempPath.toFile(), sb.toString(), null);
            fileTask.setFileUrl(url);
        } catch (Exception e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        } finally {
            ExportTempFilesHandler.deleteQuietly(tempPath);
        }
    }

    protected abstract DynamicExcelDTO getData(FileTask fileTask);

    @SuppressWarnings("unchecked")
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

    private List<List<Object>> convertDataList(List<LinkedHashMap<String, Object>> data) {
        List<List<Object>> result = new ArrayList<>();
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
}
