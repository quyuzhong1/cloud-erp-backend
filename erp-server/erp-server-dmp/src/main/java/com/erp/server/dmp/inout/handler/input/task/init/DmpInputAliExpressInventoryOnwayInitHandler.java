package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAliExpressInventoryOnwayInitHandler extends DmpInputInitHandler {

    public static final String NEXT_LEVEL_ID = "nextLevelId";
    public static final String SC_ITEM_ID = "sc_item_id";

    @Resource
    private AliExpressOrderService aliExpressOrderService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> findMongoData = null;
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isNotBlank(parentStorageName)) {
            List<ParamData> paramDataList = new ArrayList<>();
            paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
            findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
        }
        if (CollUtil.isEmpty(findMongoData)) {
            return new ArrayList<>();
        }
        AliExpressShopInfoDTO aliExpressShopInfoDTO = aliExpressOrderService.getShopInfoByShopId(findMongoData.get(0).get(NEXT_LEVEL_ID).toString());

        List<String> scItemIdList = findMongoData.stream().map(e -> e.get(SC_ITEM_ID).toString()).distinct().collect(Collectors.toList());


        String appKey = aliExpressShopInfoDTO.getClientId();
        String appSecret = aliExpressShopInfoDTO.getClientSecret();
        String baseUrl = aliExpressShopInfoDTO.getBaseUrl();
        String token = aliExpressShopInfoDTO.getToken();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);

        IopRequest request = new IopRequest();
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);

        String apiType = dmpCfgApiEntity.getApiType();
        request.setApiName(apiType);
        request.addApiParameter("simplify", "true");

        // 按数量分组
        List<List<String>> partition = ListUtil.partition(scItemIdList, 30);

        JSONArray result = new JSONArray();
        for (List<String> curList : partition) {
            // 库存类型(1 采购在途，2 调拨在途，3 销售在途，4 销退在途)
            int inventoryType1 = 1;
            JSONObject data = queryOnWayInfo(curList,inventoryType1, request, client, token, apiType);
            result.addAll(data.getJSONArray("data_list"));

            int inventoryType4 = 4;
            JSONObject data4 = queryOnWayInfo(curList,inventoryType4, request, client, token, apiType);
            result.addAll(data4.getJSONArray("data_list"));
        }


        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        result.forEach(o -> {
            JSONObject j = (JSONObject) o;
            j.put("authId", nextLevelId);
        });
        dmpInputTaskInitDTO.setMsg(result.toJSONString());
        return Collections.singletonList(dmpInputTaskInitDTO);
    }

    /**
     * 请求响应数据
     */
    private JSONObject queryOnWayInfo(List<String> curList,Integer inventoryType, IopRequest request, IopClient client, String token, String apiType) {
        Map<String, Object> paramMap = new HashMap<>();
        // 账套编码
        paramMap.put("biz_type", 288000);
        // 货品Id列表，最多30个
        paramMap.put("sc_item_id_list", curList);
        // 库存类型(1 采购在途，2 调拨在途，3 销售在途，4 销退在途)
        paramMap.put("inventory_type", inventoryType);
        // 请求DTO
        request.addApiParameter("on_way_inventory_query_dto", JSON.toJSONString(paramMap));

        JSONObject data = null;
        long sleepTime = 1000;
        int count = 0;
        while (data == null) {
            data = this.execute(client, request, token, apiType);
            log.debug("速卖通在途库存查询结果:{}", JSON.toJSONString(data));
            if (data == null) {
                if (count == 10) {
                    throw new ServiceException("调用速卖通" + apiType + "接口重试" + count + "失败");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                sleepTime = sleepTime + 1000;
                count = count + 1;
            }
        }
        return data;
    }

    private JSONObject execute(IopClient client, IopRequest request, String token, String apiType) {
        IopResponse response = null;
        try {
            response = client.execute(request, token, Protocol.TOP);
        } catch (ApiException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SSLHandshakeException || cause instanceof SocketTimeoutException) {
                return null;
            }
            throw new ServiceException("调用速卖通" + apiType + "接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
        }
        JSONObject body = JSON.parseObject(response.getBody());
        JSONObject data = body.getJSONObject("result");
        if (data == null) {
            JSONObject errorResponse = body.getJSONObject("error_response");
            if (errorResponse == null) {
                return null;
            }
            String code = errorResponse.getString("code");
            if (!"ApiCallLimit".equals(code) && !"15".equals(code) && !"UnknownRuntimeException".equals(code)) {
                throw new ServiceException("调用速卖通" + apiType + "接口报错，错误原因：" + errorResponse.getString("msg"));
            }
        }
        return data;
    }
}
