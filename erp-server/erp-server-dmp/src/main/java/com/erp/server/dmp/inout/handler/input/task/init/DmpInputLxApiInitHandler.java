package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxApiInitHandler extends DmpInputInitHandler {

    /**
     * 公共入库
     */
    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();
        String extendJson = dmpCfgInputEntity.getExtendJson();
        TreeMap<String, Object> requestMap = new TreeMap<>();
        if (StringUtils.isNotBlank(extendJson)){
            requestMap = JSON.parseObject(extendJson, TreeMap.class);
        }

        Result<Object> result = LingxingApiUtils.postAndSign(apiType, requestMap);
        if (! "0".equalsIgnoreCase(result.getCode())) {
            String errorMsg = StrUtil.format("请求领星{}接口:, result={}", apiType, JSONUtil.toJsonStr(result));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        Object data = result.getData();
        if (null == data){
            return Collections.emptyList();
        }
        Map<String, Object> dataResultMap = (Map<String, Object>) data;
        Object listObj = dataResultMap.get("list");
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(listObj)));
    }

}
