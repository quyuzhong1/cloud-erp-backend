package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputDhtUserInitHandler extends DmpInputAmzCommonInitHandler {
    @Resource
    private RedisUtil redisUtil;


    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> findMongoData = getParentStorageMongoData();
        if (CollectionUtils.isEmpty(findMongoData)) {
//            ServiceException.runError("获取订货通客户信息：未找到授权信息");
            return Collections.emptyList();
        }
        String nextLevelId = findMongoData.get(0).getOrDefault("nextLevelId", "").toString();
        if (StringUtils.isBlank(nextLevelId)){
            ServiceException.runError("未找到mongo中nextLevelId信息:taskId=" + dmpInputTaskEntity.getId());
        }
        String tokenKey = CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.DHT.getCode(), nextLevelId);
        Object obj = redisUtil.get(tokenKey);
        if (null == obj){
            ServiceException.runError("未找到订货通授权缓存信息");
        }
        String mobile = "";
        String extendJson = dmpCfgInputEntity.getExtendJson();
        if (StringUtils.isNotBlank(extendJson)){
            JSONObject extendJsonObj = JSON.parseObject(extendJson);
            mobile = extendJsonObj.getString("mobile");
        }
        if (StringUtils.isBlank(mobile)){
            ServiceException.runError("配置订货通手机号为空");
        }
        JSONObject tokenJsonObj = (JSONObject) obj;
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(dmpCfgInputEntity.getTypeId());
        String apiType = dmpCfgApiEntity.getApiType();

        String corpAccessToken = tokenJsonObj.getString("corpAccessToken");
        String corpId = tokenJsonObj.getString("corpId");
        String url = tokenJsonObj.getString("url");

        Map<String, String> headerMap = new HashMap<>();
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("corpAccessToken", corpAccessToken);
        bodyMap.put("corpId", corpId);
        bodyMap.put("mobile", mobile);
        String bodyStr = OkHttpUtils.doPostJson(url + apiType, bodyMap, headerMap);
        JSONObject respJson = JSON.parseObject(bodyStr);
        Object empListObj = respJson.get("empList");
        if (null == empListObj){
            ServiceException.runError("调用订货通查询用户信息接口失败，未找到empList节点");
        }
        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(empListObj));
        if (!CollectionUtils.isEmpty(jsonArray)){
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(jsonArray.get(0)));
            tokenJsonObj.put("enterpriseId", jsonObject.get("enterpriseId"));
            tokenJsonObj.put("openUserId", jsonObject.get("openUserId"));
            tokenJsonObj.put("account", jsonObject.get("account"));
            tokenJsonObj.put("name", jsonObject.get("name"));
            tokenJsonObj.put("mobile", jsonObject.get("mobile"));
            tokenJsonObj.put("status", jsonObject.get("status"));
            Long expireTimeSecond = redisUtil.getExpire(tokenKey);
            if (null != expireTimeSecond && expireTimeSecond > 0) {
                redisUtil.set(tokenKey, tokenJsonObj, expireTimeSecond);
            }
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(jsonArray)));
    }

}
