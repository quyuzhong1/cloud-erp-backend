package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundPlan;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.Shipment;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.CfgAppClientService;
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
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * FBA入库计划信息
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmazonFbaInboundPlanShipmentsInitHandler extends DmpInputInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isBlank(parentStorageName)) {
            return Collections.emptyList();
        }
        // 查询报告文档信息
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
        if (CollectionUtils.isEmpty(findMongoData)) {
            ServiceException.runError("未找到mongo信息:taskId=" + dmpInputTaskEntity.getParentTaskId());
        }

        String shopId = findMongoData.get(0).get("nextLevelId").toString();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);

        FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);

        List<JSONObject> dataList = new ArrayList<>();
        for (Map<String, Object> findMongo : findMongoData) {
            String inboundPlanId = findMongo.get("inboundPlanId").toString();
            String fbaShipmentId = findMongo.get("fbaShipmentId").toString();

            long sleepTime = 1000;
            int count = 0;
            boolean requestAgain = true;
            while (requestAgain) {
                try {
                    Shipment sourceEntity = api.getShipment(inboundPlanId, fbaShipmentId);
                    JSONObject data = (JSONObject) JSON.toJSON(sourceEntity);
                    dataList.add(data);
                    requestAgain = false;
                } catch (Exception e) {
                    if (e instanceof ApiException) {
                        ApiException apiError = (ApiException) e;
                        if (429 == apiError.getCode()) {
                            if (10 == count) {
                                throw new ServiceException("调用亚马逊入库计划h货件接口重试" + count + "失败");
                            }
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException ie) {
                                log.error("调用亚马逊入库计划接口重试睡眠异常:{}", shopInfoDTO.getPlatformShopCode());
                                Thread.currentThread().interrupt();
                            }
                            sleepTime = sleepTime + 1000;
                            count = count + 1;
                        }
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(dataList)));
    }
}
