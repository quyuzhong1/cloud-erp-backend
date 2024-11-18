package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.client.JSON;
import com.erp.sdk.oms.amz.spapi.dto.ReportSuperMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.CfgAmzReportFieldService;
import com.erp.server.dmp.service.DmpAmzReportInfoService;
import lombok.extern.slf4j.Slf4j;
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
 *
 * @author Jim
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportParseApiInitHandler extends DmpInputAmzCommonInitHandler {

    @Resource
    private CfgAmzReportFieldService cfgAmzReportFieldService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> findMongoData = getParentStorageMongoData();
        if(CollectionUtils.isEmpty(findMongoData)) {
            log.warn("亚马逊报告主任务taskId={},结果为空无需处理", dmpInputTaskEntity.getParentTaskId());
            return Collections.emptyList();
        }
        // 报告信息
        Map<String, Object> reportMongoObjectMap = findMongoData.get(0);
        // 状态
        String processingStatus = checkAndGetMongoValue(reportMongoObjectMap, "processingStatus");
        // 取消状态=无数据跳过
        if (!Report.ProcessingStatusEnum.DONE.getValue().equalsIgnoreCase(processingStatus)){
            log.warn("亚马逊报告下载,报告状态非完成,无法解析:{}", JSONUtil.toJsonStr(reportMongoObjectMap));
            return Collections.emptyList();
        }

        // 报告文档信息
//        Map<String, Object> reportDocumentMongoObjectMap = findMongoData.get(0);
        String reportId = checkAndGetMongoValue(reportMongoObjectMap, "reportId");
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData("reportId", "reportId", PannoEnum.EQ, reportId));
        List<Map<String, Object>> documentMongoDataList = mongoService.findMongoData(paramDataList, "amazon_report_document_data");
        if (CollUtil.isEmpty(documentMongoDataList)) {
            ServiceException.runError("未找到主单数据, taskId=" + dmpInputTaskEntity.getId());
        }
        Map<String, Object> reportDocumentMongoObjectMap = documentMongoDataList.get(0);

        // 报告文档ID
//        String reportDocumentId = checkAndGetMongoValue(reportDocumentMongoObjectMap, "reportDocumentId");

        // 报告类型
        String reportType = checkAndGetMongoValue(reportMongoObjectMap, "reportType");
        // 店铺ID
        String requestShopId = checkAndGetMongoValue(reportMongoObjectMap, "shopId");
        // 账号ID
        String platformShopCode = checkAndGetMongoValue(reportMongoObjectMap, "platformShopCode");
        // 报告站点IDS
        String marketplaceIds = checkAndGetMongoValue(reportMongoObjectMap, "marketplaceIds");

        // 查询报告配置map<报告列表名, mongo保存字段名>
        Map<String, String> columnMap = cfgAmzReportFieldService.mayByReportType(reportType);
        if (columnMap.isEmpty()) {
            throw new ServiceException("报告类型列表配置不存在, recordType=" + reportType);
        }
        // 报告路径
        String filePath = checkAndGetMongoValue(reportDocumentMongoObjectMap, "filePath");
        // 从FastDFS下载后解析
        JSONArray jsonArray = AmazonSpApiReportUtils.downloadFromFastDFSAndParse(filePath, columnMap, reportType);
        // 兼容报告内容为空
        if (CollectionUtils.isEmpty(jsonArray)){
            return Collections.emptyList();
        }

        // 补充请求店铺ID和账号站点
        jsonArray.forEach(obj->{
            if (obj instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) obj;
                jsonObj.set("requestShopId", requestShopId);
                jsonObj.set("platformShopCode", platformShopCode);
                jsonObj.set("marketplaceIds", marketplaceIds);
            }
        });
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJsonStr(jsonArray)));
    }
}
