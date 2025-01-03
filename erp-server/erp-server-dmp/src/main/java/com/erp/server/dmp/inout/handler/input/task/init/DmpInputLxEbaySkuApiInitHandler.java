package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.ThirdShopService;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxEbaySkuApiInitHandler extends DmpInputInitHandler {

    @Resource
    private ThirdShopService thirdShopService;

    /**
     * 公共入库
     */
    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();
        String extendJson = dmpCfgInputDetailEntity.getExtendJson();
        List<String> storeIds = new ArrayList<>();
        if (StringUtils.isNotBlank(extendJson)){
            JSONObject jsonObject = JSON.parseObject(extendJson);
            JSONArray jsonArray = jsonObject.getJSONArray("storeIds");
            if (CollectionUtils.isNotEmpty(jsonArray)){
                storeIds = jsonArray.stream().map(Object::toString).collect(Collectors.toList());;
            }
        }
        // 查下所有绑定店铺
        if (CollectionUtils.isEmpty(storeIds)){
            List<ThirdShopEntity> list = thirdShopService.lambdaQuery()
                    .eq(ThirdShopEntity::getSysType, PlatformEnum.LINGXING.getCode())
                    .eq(ThirdShopEntity::getPlatformId, "10003")
                    .eq(ThirdShopEntity::getDisabled, false)
                    .eq(ThirdShopEntity::getAuthState, true)
                    .list();
            storeIds = list.stream().map(e -> e.getSubPlatformId()).collect(Collectors.toList());
        }
        TreeMap<String, Object> requestMap = new TreeMap<>();
        requestMap.put("store_ids", storeIds);

        Result<Object> result = LingxingApiUtils.postRequestData(apiType, requestMap);
        Object data = result.getData();
        if (null == data){
            return Collections.emptyList();
        }
        Map<String, Object> dataResultMap = (Map<String, Object>) data;
        Object totalObj = dataResultMap.get("total");
        Object listObj = dataResultMap.get("list");

        List<JSONObject> resultList = new LinkedList<>();
        JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(totalObj));

        int total = Integer.parseInt(listObj.toString());

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(listObj)));
    }
}
