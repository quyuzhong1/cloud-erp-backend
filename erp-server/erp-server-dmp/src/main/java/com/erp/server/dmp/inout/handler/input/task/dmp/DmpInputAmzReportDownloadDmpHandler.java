package com.erp.server.dmp.inout.handler.input.task.dmp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 亚马逊报告查询字段映射转换
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportDownloadDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAmzReportDownloadHandler afterConvertData 处理");
    }
}
