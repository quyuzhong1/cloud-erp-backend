package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.client.JSON;
import com.erp.sdk.oms.amz.spapi.dto.ReportSuperMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.AmzReportHandleService;
import com.erp.server.dmp.service.CfgAmzReportFieldService;
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
 *
 * @author Jim
 */
@Service
@Scope("prototype")
public class DmpInputAmzReportParseApiInitHandler extends DmpInputInitHandler {
    @Resource
    private DmpAmzReportInfoService dmpAmzReportInfoService;
    @Resource
    private CfgAmzReportFieldService cfgAmzReportFieldService;

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
        // 查询报告配置map<报告列表名, mongo保存字段名>
        Map<String, String> columnMap = cfgAmzReportFieldService.mayByReportType(reportInfo.getReportType());
        if (columnMap.isEmpty()) {
            throw new ServiceException("报告类型列表配置不存在, recordType=" + reportInfo.getReportType());
        }

        // 从FastDFS下载后解析
        JSONArray jsonArray = AmazonSpApiReportUtils.downloadFromFastDFSAndParse(reportInfo.getFilePath(), columnMap, reportInfo.getReportType());
        // 当前报告类型
        AmazonReportRecordTypeEnum recordType = AmazonReportRecordTypeEnum.getByRecordType(reportInfo.getReportType());
        // 根据报告类型获取解析的实体
        Class<? extends ReportSuperMongoDTO> mongoDTOClass = recordType.getAndCheckMongoDTOClass();
        // 解析对应报告内容
        List<? extends ReportSuperMongoDTO> mongoDTOList = JSONUtil.toList(jsonArray, mongoDTOClass);
        // 填充报告信息和生成唯一键
        List<? extends ReportSuperMongoDTO> allMongoDTOList = ReportSuperMongoDTO.fillReportData(mongoDTOList, reportInfo, recordType, reportInfo.getPlatformShopCode());

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJsonStr(allMongoDTOList)));
    }
}
