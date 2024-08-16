package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.erp.model.dmp.enums.AmzReportTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.client.JSON;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.AmzReportHandleService;
import com.erp.server.dmp.service.DmpAmzReportInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * dmp输入init任务基础处理器下的亚马逊api获取数据方式
 * 下载报告
 *
 * @author Jim
 */
@Service
@Scope("prototype")
public class DmpInputAmzReportDownloadApiInitHandler extends DmpInputInitHandler {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private DmpAmzReportInfoService dmpAmzReportInfoService;
    @Resource
    private AmzReportHandleService amzReportHandleService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if(StringUtils.isBlank(parentStorageName)) {
            return Collections.emptyList();
        }
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
        if(CollectionUtils.isEmpty(findMongoData)) {
            ServiceException.runError("未找到mongo报告信息:taskId=" + dmpInputTaskEntity.getParentTaskId());
        }
        Map<String, Object> mongoObjectMap = findMongoData.get(0);
        Object reportIdObj = mongoObjectMap.get("reportId");
        if (null == reportIdObj){
            ServiceException.runError("未找到报告ID:taskId=" + dmpInputTaskEntity.getParentTaskId());
        }
        String reportId = (String) reportIdObj;
        DmpAmzReportInfoEntity reportInfo = dmpAmzReportInfoService.getByReportId(reportId, Report.ProcessingStatusEnum.DONE.getValue());
        if (null == reportInfo){
            ServiceException.runError("未找到报告信息dmp_amz_report_info:ReportId=" + reportId);
        }
        if (StringUtils.isNotBlank(reportInfo.getFilePath())){
            // 报告已存在不下载
            // TODO 更新
            dmpInputTaskEntity.setErrorMessage("报告已存在-停止后续任务");
            return Collections.emptyList();
        }

        // 查询文档信息
        ReportDocument reportDocument = amzReportHandleService.queryAmzReportDocument(reportInfo, dmpInputTaskEntity.getParentTaskId(), AmzReportTaskStatusEnum.DOWNLOAD.getCode());

        // 根据url下载到FastDFS
        String compressionAlgorithm = null == reportDocument.getCompressionAlgorithm() ? "" : reportDocument.getCompressionAlgorithm().getValue();
        String fileName = StrUtil.subBetween(reportDocument.getUrl(), ".com/", "?");
        String fastDFSUrl = AmazonSpApiReportUtils.downloadAndUploadFastDFS(reportDocument.getUrl(), compressionAlgorithm, fileName, reportDocument.getReportDocumentId(), reportInfo.getReportType());

        // 更新报告信息
        reportInfo.setReportUrl(reportDocument.getUrl());
        reportInfo.setFilePath(fastDFSUrl);

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJsonStr(reportInfo)));
    }
}
