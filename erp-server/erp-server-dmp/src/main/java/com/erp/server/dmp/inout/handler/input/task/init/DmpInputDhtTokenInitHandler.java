package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.service.CfgAppClientService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sdk.wms.goodcang.dto.request.GoodCangGetSkuReq;
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

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputDhtTokenInitHandler extends DmpInputInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        CfgAppClientEntity clientEntity = cfgAppClientService.getById(dmpInputTaskEntity.getNextLevelId());
        if (null == clientEntity) {
            throw new ServiceException("订货通对应授权ID信息不存在");
        }
        if (CollectionUtils.isEmpty(clientEntity.getExtendData())) {
            throw new ServiceException("订货通对应授权extendData配置为空");
        }
        String tokenKey = CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.DHT.getCode(),dmpInputTaskEntity.getNextLevelId());
        Map<String, String> headerMap = new HashMap<>();
        Map<String, Object> bodyMap = new HashMap<>(clientEntity.getExtendData());
        String bodyStr = OkHttpUtils.doPostJson(clientEntity.getUrl(), bodyMap, headerMap);
        JSONObject dto = JSON.parseObject(bodyStr);
        if (Objects.isNull(dto)) {
            log.error("订货通获取accessToken失败，返回结果：{}", bodyStr);
            throw new RuntimeException("订货通获取accessToken失败");
        }
        String errorCodeStr = dto.getOrDefault("errorCode", "999999").toString();
        if (Integer.parseInt(errorCodeStr) != 0) {
            String errorMessageStr = dto.getOrDefault("errorMessage", "无").toString();
            log.error("订货通获取accessToken失败，返回结果：{}", bodyStr);
            throw new RuntimeException("订货通获取accessToken失败，错误码：" +errorCodeStr + "，错误信息：" + errorMessageStr);
        }
        //缓存6900s 6600-7200s会获取新token  必须保证过期时间在这个范围内
        redisUtil.set(tokenKey, JSONUtil.toJsonStr(dto), 6900);
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(dto)));
    }

}
