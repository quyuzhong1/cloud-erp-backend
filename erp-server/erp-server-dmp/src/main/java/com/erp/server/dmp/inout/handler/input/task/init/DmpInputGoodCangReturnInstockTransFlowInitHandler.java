package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.goodcang.dto.request.GoodCangInventoryRequestDTO;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.utils.GoodCangUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputGoodCangReturnInstockTransFlowInitHandler extends DmpInputInitHandler {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Resource
    private DmpHandlerCache dmpHandlerCache;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 顶级mongo数据
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isBlank(parentStorageName)) {
            return Collections.emptyList();
        }
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        List<Map<String, Object>> parentData = mongoService.findMongoData(paramDataList, parentStorageName);
        if (CollectionUtils.isEmpty(parentData)) {
            // 主数据不存在明细无需处理
            return Collections.emptyList();
        }
        // 父级任务
        DmpInputTaskEntity parentTaskEntity = dmpInputTaskService.getById(dmpInputTaskEntity.getParentTaskId());

        // 退货单号列表
        List<String> returnOrderIdList = new LinkedList<>();
        for (Map<String, Object> parentDatum : parentData) {
            String asroCode = parentDatum.getOrDefault("asro_code", "").toString();
            if (StringUtils.isNotBlank(asroCode) && !returnOrderIdList.contains(asroCode)) {
                returnOrderIdList.add(asroCode);
            }
        }
        if (CollUtil.isEmpty(returnOrderIdList)) {
            // 来源数据异常找不到退货单号
            ServiceException.runError("来源数据异常找不到退货单号:" + dmpInputTaskEntity.getId());
        }

        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();

        int batchSize = 200;
        GoodCangInventoryRequestDTO requestDTO = new GoodCangInventoryRequestDTO();
        requestDTO.setCreate_date_from(parentTaskEntity.getStartTime().format(DATE_FORMATTER));
        requestDTO.setCreate_date_end(parentTaskEntity.getEndTime().format(DATE_FORMATTER));
        requestDTO.setApplication_code(6);
        requestDTO.setPageSize(batchSize);
        List<JSONObject> allResult = new ArrayList<>();
        List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.GOODCANG.getCode()));
        if (CollUtil.isEmpty(overseasProviderEntityList)) {
            throw new ServiceException("谷仓授权信息不存在");
        }
        // 取对应授权ID授权
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(parentData.get(0).getOrDefault("authId", "").toString()))
                .findFirst()
                .orElse(null);
        if(null == overseasProviderEntity) {
            throw new ServiceException("谷仓对应授权ID信息不存在");
        }
        String authId = overseasProviderEntity.getId();
        ThirdWarehouseContext.setAuthMap(overseasProviderEntity.getAuthJson());

        // 退货单号列表使用stream按200个分组
        List<List<String>> partitionedList = IntStream.range(0, (returnOrderIdList.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> returnOrderIdList.subList(i * batchSize, Math.min((i + 1) * batchSize, returnOrderIdList.size())))
                .collect(Collectors.toList());

        for (List<String> partReturnOrderIds : partitionedList) {
            requestDTO.setReference_no_list(partReturnOrderIds);
            requestDTO.setPage(1);
            log.debug("请求谷仓库存流水请求:{}", JSON.toJSONString(requestDTO));
            String response = GoodCangUtils.sendPost(apiType, JSON.toJSONString(requestDTO));
            log.debug("请求谷仓库存流水响应:{}", response);
            // 空数据处理
            // {"ask":"Failure","message":"没有数据(ERROR ID 99-UVU8HX)","Error":{"errCode":"400","errMessage":"没有数据(ERROR ID 99-UVU8HX)"}}
            JSONObject jsonObject = JSONObject.parseObject(response);
            JSONObject errorObj = jsonObject.getJSONObject("Error");
            if (null != errorObj) {
                String errCode = errorObj.getString("errCode");
                String errMessage = errorObj.getString("errMessage");
                if ("400".equalsIgnoreCase(errCode) && errMessage.contains("没有数据")) {
                    break;
                }
                ServiceException.runError("谷仓接口返回异常:" + response);
            }

            GoodCangResponse<List<JSONObject>> result = JSONObject.parseObject(response, new TypeReference<GoodCangResponse<List<Object>>>() {
            }.getType());
            List<?> data = result.getData();
            int size = data.size();
            if (size == 0) {
                continue;
            }
            List<JSONObject> jsonObjList = data.stream().map(e -> {
                JSONObject jsonItemObj = (JSONObject) JSON.toJSON(e);
                jsonItemObj.put("authId", authId);
                return jsonItemObj;}
            ).collect(Collectors.toList());
            allResult.addAll(jsonObjList);
        }

        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
        return Collections.singletonList(dmpInputTaskInitDTO);
    }
}
