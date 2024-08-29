package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.AmzReportTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.AmzReportHandleService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
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
    private AmzReportHandleService amzReportHandleService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isBlank(parentStorageName)) {
            return Collections.emptyList();
        }
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
        if (CollectionUtils.isEmpty(findMongoData)) {
            ServiceException.runError("未找到mongo报告信息:taskId=" + dmpInputTaskEntity.getId());
        }
        Map<String, Object> mongoObjectMap = findMongoData.get(0);
        // 请求获取亚马逊报告文档信息
        ReportDocument reportDocument = queryReportDocument(mongoObjectMap);

        // 文档ID
        Object reportTypeObj = mongoObjectMap.get("reportType");
        if (null == reportTypeObj) {
            ServiceException.runError("未找到报告类型:taskId=" + dmpInputTaskEntity.getId());
        }
        String reportType = (String) reportTypeObj;
        // 报告ID
        Object reportIdObj = mongoObjectMap.get("reportId");
        if (null == reportIdObj) {
            ServiceException.runError("未找到店铺ID:taskId=" + dmpInputTaskEntity.getId());
        }
        String reportId = (String) reportIdObj;

        // 根据url下载到FastDFS
        String compressionAlgorithm = null == reportDocument.getCompressionAlgorithm() ? "" : reportDocument.getCompressionAlgorithm().getValue();
        String fileName = StrUtil.subBetween(reportDocument.getUrl(), ".com/", "?");
        String fastDFSUrl = AmazonSpApiReportUtils.downloadAndUploadFastDFS(reportDocument.getUrl(), compressionAlgorithm, fileName, reportDocument.getReportDocumentId(), reportType);

        // 记录报告路径信息
        JSONObject jsonObject = (JSONObject) JSON.toJSON(reportDocument);
        // 补充其他信息
        jsonObject.put("filePath", fastDFSUrl);
        jsonObject.put("reportId", reportId);

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(jsonObject)));
    }

    /**
     * 请求亚马逊报告文档信息
     *
     * @param mongoObjectMap mongo报告信息
     * @return 亚马逊报告文档对象
     */
    private ReportDocument queryReportDocument(Map<String, Object> mongoObjectMap) {
        Object shopIdObj = mongoObjectMap.get("shopId");
        if (null == shopIdObj) {
            ServiceException.runError("未找到店铺ID:taskId=" + dmpInputTaskEntity.getParentTaskId());
        }
        String shopId = (String) shopIdObj;

        Object reportDocumentIdObj = mongoObjectMap.get("reportDocumentId");
        if (null == reportDocumentIdObj) {
            ServiceException.runError("未找到店铺ID:taskId=" + dmpInputTaskEntity.getParentTaskId());
        }
        String reportDocumentId = (String) reportDocumentIdObj;
        // 查询文档信息
        return amzReportHandleService.queryAmzReportDocument(shopId, reportDocumentId, dmpInputTaskEntity.getParentTaskId(), AmzReportTaskStatusEnum.DOWNLOAD.getCode());

    }
}
