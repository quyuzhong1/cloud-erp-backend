package com.erp.server.file.core;

import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.server.file.dto.DynamicExcelDTO;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractDynamicHeadersFileEventHandler implements FileEventHandler {

    @Override
    public void handle(FileTask fileTask) {
        DynamicExcelDTO excelDTO = getData(fileTask);
        LinkedHashMap<String, String> headers = excelDTO.getHeaders();
        List<List<String>> header = convertHeadList(headers.values());
        List<List<Object>> data = convertDataList(excelDTO.getData(), headers);
        fileTask.setCount(excelDTO.getData().size());
        StringBuilder sb = new StringBuilder();
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            byte[] bytes = new ExcelPrintUtils().exportDynamicHeadersExcel(name, header, data);
            String s = FastDFSClientUtil.uploadFile(bytes, sb.toString(), null);
            fileTask.setFileUrl(s);
        } catch (Exception e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 获取数据
     *
     * @param fileTask 入参
     */
    protected abstract DynamicExcelDTO getData(FileTask fileTask);

    private List<List<Object>> convertDataList(Map<String, Object> data, LinkedHashMap<String, String> headers) {
        ArrayList<List<Object>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            result.add(Collections.singletonList(data.get(entry.getKey())));
        }
        return result;
    }

    private List<List<String>> convertHeadList(Collection<String> headList) {
        return headList.stream().map(Arrays::asList).collect(Collectors.toList());
    }

}
