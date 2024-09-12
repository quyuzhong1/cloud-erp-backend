package com.erp.server.dmp.inout.handler.input.task.init;

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
        // 查询报告文档信息
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
        if(CollectionUtils.isEmpty(findMongoData)) {
            log.warn("亚马逊报告主任务taskId={},结果为空无需处理", dmpInputTaskEntity.getParentTaskId());
            return Collections.emptyList();
        }
        // 报告文档信息
        Map<String, Object> reportDocumentMongoObjectMap = findMongoData.get(0);
        // 报告文档ID
        String reportDocumentId = checkAndGetMongoValue(reportDocumentMongoObjectMap, "reportDocumentId");

        // 查询mongo报告信息
        List<ParamData> subParamDataList = new ArrayList<>();
        subParamDataList.add(new ParamData("reportDocumentId", "reportDocumentId", PannoEnum.EQ, reportDocumentId));
        List<Map<String, Object>> reportFindMongoData = mongoService.findMongoData(subParamDataList, "amazon_report_data");
        if(CollectionUtils.isEmpty(reportFindMongoData)) {
            ServiceException.runError("未找到mongo报告信息:taskId=" + dmpInputTaskEntity.getParentTaskId());
        }
        // 报告信息
        Map<String, Object> reportMongoObjectMap = reportFindMongoData.get(0);
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

    /**
     * 校验和获取指定字段
     */
    private String checkAndGetMongoValue(Map<String, Object> nongoObjectMap, String mongoFieldName) {
        Object reportDocumentIdObj = nongoObjectMap.get(mongoFieldName);
        if (null == reportDocumentIdObj) {
            String msg = StrUtil.format("未找到{}:taskId={}", mongoFieldName, dmpInputTaskEntity.getId());
            ServiceException.runError(msg);
        }
        return (String) reportDocumentIdObj;

    }
}
